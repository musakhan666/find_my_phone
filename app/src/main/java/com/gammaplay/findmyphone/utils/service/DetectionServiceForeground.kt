package com.gammaplay.findmyphone.utils.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.presentation.main.MainActivity
import com.gammaplay.findmyphone.presentation.overlay.RippleEffectOverlayScreen
import com.gammaplay.findmyphone.utils.AppStatusManager
import com.gammaplay.findmyphone.utils.ClapApi
import com.musicg.wave.WaveHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/**
 * Foreground Service that detects claps and speech based on the activation type.
 * Handles "clap", "speech", and "both" activation modes.
 */
class DetectionServiceForeground : LifecycleService(), SavedStateRegistryOwner {

    companion object {
        const val TAG = "DetectionService"
        const val NOTIFICATION_CHANNEL_ID = "speech_recognition_channel"
        private const val NOTIFICATION_ID = 1
        private const val DETECTION_DURATION: Long = 10000 // 10 seconds
    }


    // Detection Mode Flags
    private var isClapDetectionActive = false
    private var isSpeechDetectionActive = false
    private var isDeviceFound = false

    private lateinit var audioRecord: AudioRecord
    private lateinit var clapApi: ClapApi
    private val buffer = ByteArray(4096)
    private var isRecording = false

    // Coroutine Scope for managing coroutines
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val serviceJob = Job()
    private val serviceFlashScope = CoroutineScope(Dispatchers.IO + serviceJob)


    private val serviceVibJob = Job()
    private val serviceVibScope = CoroutineScope(Dispatchers.IO + serviceVibJob)

    // MediaPlayer for ringtone playback
    private var mediaPlayer: MediaPlayer? = null

    // Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null


    // App Status Manager for retrieving preferences (Assuming you have this class implemented)
    private lateinit var appStatusManager: AppStatusManager

    // Activation Settings
    private var activationType: String = "clap"
    private var keywords: String? = null
    private var language: String = "en"
    private var vibrationMode: Int? = null
    private var flashlightMode: Int? = null
    private var sensitivityLevel: Int? = null
    private var duration: Int? = null

    private var isAllowedFlashing: Boolean = false
    private var isAllowedVibration: Boolean = false

    // Handler for scheduling mode switches
    private val handler = Handler(Looper.getMainLooper())

    private val savedStateRegistryController = SavedStateRegistryController.create(this)


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")

        // init your SavedStateRegistryController
        savedStateRegistryController.performAttach() // you can ignore this line, becase performRestore method will auto call performAttach() first.
        savedStateRegistryController.performRestore(null)


        appStatusManager = AppStatusManager(context = this)

        // Initialize Notification and start foreground
        val notification = initNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Initialize Speech Recognizer
        initializeSpeechRecognizer()

        // Initialize Recognition Intent
        initializeRecognitionIntent()

        // Check and apply activation settings
        activationType = appStatusManager.getActivationType()
        checkStatus()

    }

    private fun sendAlarmDetectedBroadcast() {
        val intent = Intent("com.gammaplay.findmyphone.ALARM_DETECTION")
        // You can also add extras if needed
        intent.putExtra("extra_data", "alarm_detected")
        sendBroadcast(intent)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service Started")

        when (activationType) {
            "none" -> {
                stopAllDetection()
            }

            "clap" -> {
                if (!isClapDetectionActive) {
                    startClapDetection()
                }
            }

            "speech" -> {
                if (!isSpeechDetectionActive) {
                    startSpeechDetection()
                }
            }

            "both" -> {
                if (!isClapDetectionActive && !isSpeechDetectionActive) {
                    startClapDetection()
                    scheduleSwitchToSpeech()
                }
            }

            else -> {
                Log.e(TAG, "Unknown activation type: $activationType")
            }
        }
        return START_STICKY
    }

    private fun startClapDetection() {
        val handler = Handler(Looper.getMainLooper()) // Create a handler for the main thread
        handler.postDelayed({
            initAudioRecorder() // Initialize audio recorder
            isRecording = true
            Thread { detectClaps() }.start() // Start the clap detection in a separate thread
            isClapDetectionActive = true
            Log.d(TAG, "Clap detection started")
        }, 500) // Delay execution by 2000 milliseconds (2 seconds)
    }

    private var contentView: ComposeView? = null

    private fun showOverlay(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        contentView = ComposeView(this).apply {
            setViewTreeSavedStateRegistryOwner(this@DetectionServiceForeground)
            setViewTreeLifecycleOwner(this@DetectionServiceForeground)
            setContent {
                RippleEffectOverlayScreen {
                    removeOverlay()
                    // Always bring the app to the foreground when the overlay appears
                    openApp(context)
                    restartService(context)


                }
            }
        }


        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(contentView, params)
    }

    // Function to bring the app to the foreground (or launch it if not running)
    private fun openApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }


    // Function to remove the overlay
    private fun removeOverlay() {
        contentView?.let {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            contentView = null
        }
    }

    private fun initAudioRecorder() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            encoding,
            AudioRecord.getMinBufferSize(sampleRate, channelConfig, encoding)
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed")
            return
        }

        val waveHeader = WaveHeader().apply {
            channels = 1
            bitsPerSample = 16
            this.sampleRate = sampleRate
        }
        clapApi = ClapApi(waveHeader)
        audioRecord.startRecording()
        Log.d(TAG, "Audio recording started")
    }

    // Detect claps
    private fun detectClaps() {
        var firstClapTime: Long? = null  // To store the time of the first clap
        val maxIntervalBetweenClaps = 500  // Max interval between claps (in ms)

        // Adjust the sensitivity of the ClapApi
        sensitivityLevel?.let { clapApi.setSensitivity(it) }

        try {
            while (isRecording) {
                val readBytes = audioRecord.read(buffer, 0, buffer.size)
                if (readBytes > 0) {
                    // Clap detection based on the adjusted sensitivity
                    if (clapApi.isClap(buffer)) {
                        val currentTime = System.currentTimeMillis()

                        // If this is the first clap, save the time
                        if (firstClapTime == null) {
                            firstClapTime = currentTime
                            Log.d(
                                "Clap Detection",
                                "First clap detected at sensitivity: ${getString(sensitivityLevel ?: 0)}"
                            )
                        } else {
                            // Check if the second clap happens within the allowed interval
                            val timeSinceFirstClap = currentTime - firstClapTime
                            if (timeSinceFirstClap <= maxIntervalBetweenClaps) {
                                Log.d(
                                    "Clap Detection",
                                    "Second clap detected! Double clap confirmed at sensitivity: ${
                                        getString(sensitivityLevel ?: 0)
                                    }."
                                )
                                onDetection()  // Trigger detection event for double clap
                                firstClapTime = null  // Reset for next double clap detection
                            } else {
                                // If the time interval is too long, reset and treat as a new first clap
                                Log.d(
                                    "Clap Detection",
                                    "Time interval too long. Resetting clap detection."
                                )
                                firstClapTime = currentTime
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in clap detection: ${e.message}")
        } finally {
            stopRecording()
        }
    }

    private fun stopRecording() {
        try {
            if (isRecording) {
                audioRecord.stop()
                audioRecord.release()
                Log.d(TAG, "Audio recording stopped")
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error stopping audio recording: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        clearAllData()


    }

    private fun clearAllData() {
        stopAllFunctions()
        isRecording = false
        stopRecording()
        serviceScope.cancel()
        serviceFlashScope.cancel()
        serviceVibScope.cancel()
    }


    /**
     * Initialize the NotificationChannel and build the notification.
     */
    private fun initNotification(): Notification {
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = "Clap or Speech Recognition"
        val description = "Find my phone service is running"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, pendingIntentFlags
        )

        return NotificationCompat.Builder(this, channelId).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(channelName).setContentText(description)
            .setContentIntent(pendingIntent).setOngoing(true).build()
    }

    /**
     * Initialize the SpeechRecognizer and set the RecognitionListener.
     */
    private fun initializeSpeechRecognizer() {
        muteSpeechRecognizerMicBeepSound(true, context = this@DetectionServiceForeground)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(mRecognitionListener)
            }
            Log.d(TAG, "SpeechRecognizer initialized")
        } else {
            Log.e(TAG, "Speech recognition is not available on this device.")
            stopSelf()
        }
    }

    /**
     * Initialize the Recognition Intent for SpeechRecognizer.
     */
    private fun initializeRecognitionIntent() {

        language = appStatusManager.getLanguage()

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        Log.d(TAG, "Recognition Intent initialized - lang $language")
    }

    /**
     * Retrieve activation settings and preferences.
     */
    private fun checkStatus() {
        try {
            keywords = appStatusManager.getKeywordForVoiceRecognition()
            isAllowedFlashing = appStatusManager.isFlashActive()
            isAllowedVibration = appStatusManager.isVibrationActive()
            flashlightMode = appStatusManager.getFlashlightMode()
            vibrationMode = appStatusManager.getVibrationMode()
            sensitivityLevel = appStatusManager.getSensitivityLevel()
            duration = appStatusManager.getDurationMode()


            Log.d(TAG, "Activation type: $activationType")
            Log.d(TAG, "Keyword: $keywords")
            Log.d(TAG, "Flash mode: ${getString(flashlightMode ?: 0)}")
            Log.d(TAG, "Vibration mode: ${getString(vibrationMode ?: 0)}")
            Log.d(TAG, "Sensitivity Level: ${getString(sensitivityLevel ?: 0)}")
            Log.d(TAG, "Duration Level: ${getString(duration ?: 0)}")

        } catch (ex: Exception) {
            Log.e(TAG, "Error in checkStatus: ${ex.message}")
        }
    }


    /**
     * Start Speech Detection by resetting the SpeechRecognizer and starting to listen.
     */
    private fun startSpeechDetection() {
        Log.d(TAG, "Starting speech detection")

        isSpeechDetectionActive = true
        if (isDeviceFound) return
        resetSpeechRecognizer()
        startListening()
        isDeviceFound = true
    }

    /**
     * Stop Clap Detection by stopping DetectorThread and RecorderThread.
     */
    private fun stopClapDetection() {
        Log.d(TAG, "Stopping clap detection")
        isClapDetectionActive = false

        try {
            if (this::audioRecord.isInitialized && isRecording) {
                audioRecord.let {
                    it.stop()
                    it.release()
                    Log.d(TAG, "Audio recording stopped")
                }
                isRecording = false
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error stopping audio recording: ${e.message}")
        }

    }

    /**
     * Stop Speech Detection by stopping and destroying the SpeechRecognizer.
     */
    private fun stopSpeechDetection() {
        Log.d(TAG, "Stopping speech detection")
        isSpeechDetectionActive = false

        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
            Log.d(TAG, "SpeechRecognizer destroyed")
        }
        speechRecognizer = null
    }

    /**
     * Schedule switching to Speech Detection after DETECTION_DURATION.
     */
    private fun scheduleSwitchToSpeech() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isClapDetectionActive) {
                stopClapDetection()
                startSpeechDetection()
                scheduleSwitchToClap()
            }
        }, DETECTION_DURATION)
    }

    /**
     * Schedule switching to Clap Detection after DETECTION_DURATION.
     */
    private fun scheduleSwitchToClap() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isSpeechDetectionActive) {
                stopSpeechDetection()
                startClapDetection()
                scheduleSwitchToSpeech()
            }
        }, DETECTION_DURATION)
    }


    /**
     * Handles the detection event by triggering the alarm, playing the ringtone,
     * vibrating, and flashing the flashlight for the specified duration.
     */
    private fun onDetection() {
        Log.d(TAG, "Detection triggered")

        checkStatus()

        val context = this

        serviceScope.launch {
            // Broadcast the alarm detection event
            sendAlarmDetectedBroadcast()
            showOverlay(context)
        }
        // Get the ringtone URI from preferences
        val ringtoneUri = getRingtoneUri()

        Log.d(TAG, "Detection triggered with duration: $duration")

        // Perform actions based on the selected duration mode
        when (duration) {
            R.string.duration_5s -> {
                startAlarmActions(ringtoneUri)
                timeoutAll(5000L) // Stop after 5 seconds
            }

            R.string.duration_10s -> {
                startAlarmActions(ringtoneUri)
                timeoutAll(10000L) // Stop after 10 seconds
            }

            R.string.duration_30s -> {
                startAlarmActions(ringtoneUri)
                timeoutAll(30000L) // Stop after 30 seconds
            }

            R.string.duration_1min -> {
                startAlarmActions(ringtoneUri)
                timeoutAll(60000L) // Stop after 1 minute
            }

            R.string.duration_loop -> {
                startAlarmActions(ringtoneUri) // Continuous loop, no timeout
                // No need for timeout in loop mode
            }

            else -> {
                startAlarmActions(ringtoneUri)
            }
        }
        stopAllDetection()
    }

    private fun restartService(context: Context) {
        // Create an intent for restarting the service
        val restartIntent = Intent(context, DetectionServiceForeground::class.java)

        // Stop the service (this will trigger the onDestroy callback)
        stopSelf()

        // Start the service again after a short delay (optional)
        Handler(Looper.getMainLooper()).postDelayed({
            context.startService(restartIntent)
        }, 0) // Delay restart by few seconds
    }

    /**
     * Starts the alarm actions: ringtone, vibration, and flashlight.
     */
    private fun startAlarmActions(ringtoneUri: Uri) {
        playRingtoneLoop(ringtoneUri)
        vibrateInLoop(getVibrator())
        flashInLoop(getCameraManager(), getCameraId())
    }

    private fun getVibrator(): Vibrator {
        return getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun getCameraManager(): CameraManager {
        return getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun getCameraId(): String {
        return getCameraManager().cameraIdList[0]
    }


    /**
     * Stop all detection modes.
     */
    private fun stopAllDetection() {
        stopClapDetection()
        stopSpeechDetection()
    }

    /**
     * Stop all functionalities including detections, ringtone, and remove callbacks.
     */
    private fun stopAllFunctions() {
        Log.d(TAG, "Stopping all functionalities")
        muteSpeechRecognizerMicBeepSound(false, context = this@DetectionServiceForeground)

        stopAllDetection()
        stopRingtone()
        handler.removeCallbacksAndMessages(null)
        stopSelf()
    }

    /**
     * Plays the ringtone in a continuous loop.
     */
    private fun playRingtoneLoop(ringtoneUri: Uri) {
        if (mediaPlayer?.isPlaying == true) return // Avoid starting if already playing

        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, ringtoneUri)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            isLooping = true // Play continuously
            prepare()
            start()
            Log.d(TAG, "Ringtone started playing in loop")
        }
    }

    /**
     * Vibrates the device in a continuous loop based on the selected vibration mode.
     */
    private fun vibrateInLoop(vibrator: Vibrator) {
        if (!isAllowedVibration || vibrationMode == null) return

        serviceVibScope.launch {
            try {
                while (isActive) { // Loop vibration until manually stopped
                    when (vibrationMode) {
                        R.string.vibration_mode_wave -> vibrateWithPattern(
                            vibrator, longArrayOf(0, 300, 200, 400, 200)
                        ) // Custom wave pattern
                        R.string.vibration_mode_heartbeat -> vibrateWithPattern(
                            vibrator, longArrayOf(0, 100, 100, 200, 100, 100)
                        ) // Heartbeat-like pulse
                        R.string.vibration_mode_short_pulse -> vibrateForDuration(
                            vibrator, 500
                        ) // Short pulse vibration for 0.5 seconds
                        R.string.vibration_mode_long_pulse -> vibrateForDuration(
                            vibrator, 1500
                        ) // Long pulse vibration for 1.5 seconds
                        R.string.vibration_mode_ramp -> vibrateWithPattern(
                            vibrator, longArrayOf(0, 100, 150, 200, 250, 300)
                        ) // Gradually increasing pattern
                    }
                    delay(2000L) // Delay between vibrations (can adjust as needed)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in vibration loop: ${e.message}")
            } finally {
                vibrator.cancel() // Ensure vibration stops if coroutine is cancelled
            }
        }
    }

    /**
     * Flashes the camera's flashlight in a continuous loop based on the selected flashlight mode.
     */
    private fun flashInLoop(cameraManager: CameraManager, cameraId: String) {
        if (!isAllowedFlashing || flashlightMode == null) return

        serviceFlashScope.launch {
            try {
                while (isActive) { // Loop flashlight until manually stopped
                    when (flashlightMode) {
                        R.string.flashlight_mode_short_blink -> flashForDuration(
                            cameraManager, cameraId, 200L
                        ) // Short blink
                        R.string.flashlight_mode_long_blink -> flashForDuration(
                            cameraManager, cameraId, 1000L
                        ) // Long blink
                        R.string.flashlight_mode_pulse -> flashForPattern(
                            cameraManager, cameraId, longArrayOf(300, 300)
                        ) // Pulse pattern
                        R.string.flashlight_mode_sos -> flashForPattern(
                            cameraManager,
                            cameraId,
                            longArrayOf(100, 100, 100, 100, 500, 100, 100, 500, 100)
                        ) // SOS pattern
                        R.string.flashlight_mode_continuous_on -> cameraManager.setTorchMode(
                            cameraId, true
                        ) // Continuous on
                        R.string.flashlight_mode_strobe -> flashForPattern(
                            cameraManager, cameraId, longArrayOf(100, 100)
                        ) // Strobe pattern
                        R.string.flashlight_mode_firefly -> flashForPattern(
                            cameraManager, cameraId, longArrayOf(200, 800)
                        ) // Firefly pattern
                    }
                    delay(500L) // General delay between flash patterns (adjust as needed)
                }
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error using camera flash: ${e.message}")
            } finally {
                cameraManager.setTorchMode(
                    cameraId, false
                ) // Ensure the flashlight turns off when the loop ends
            }
        }
    }

    /**
     * Cancels all ongoing operations (ringtone, vibration, flashlight) after the specified timeout.
     */
    private fun timeoutAll(timeout: Long) {
        // Launch a coroutine to handle the timeout
        serviceScope.launch {
            delay(timeout) // Wait for the specified timeout

            // Stop the ringtone
            stopRingtone()

            // Cancel the vibration coroutine scope
            serviceVibScope.cancel()

            // Cancel the flashlight coroutine scope
            serviceFlashScope.cancel()

            Log.d(TAG, "All operations stopped after $timeout ms")
        }
    }

    /**
     * Stops the ringtone by releasing the MediaPlayer resources.
     */
    private fun stopRingtone() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                Log.d(TAG, "Ringtone stopped")
            }
            it.reset()
            it.release()
        }
        mediaPlayer = null
    }

    /**
     * Plays a vibration pattern for the specified vibrator.
     */
    private fun vibrateWithPattern(vibrator: Vibrator, pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, -1) // No repeat
            vibrator.vibrate(vibrationEffect)
            Log.d(TAG, "Vibrating with custom pattern")
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(pattern, -1) // No repeat for older versions
            Log.d(TAG, "Vibrating with custom pattern (deprecated method)")
        }
    }

    /**
     * Vibrates for a specified duration.
     */
    private fun vibrateForDuration(vibrator: Vibrator, duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect =
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            Log.d(TAG, "Vibrating for $duration ms")
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(duration)
            Log.d(TAG, "Vibrating for $duration ms (deprecated method)")
        }
    }

    /**
     * Flashes the camera for a specific duration.
     */
    private suspend fun flashForDuration(
        cameraManager: CameraManager, cameraId: String, duration: Long
    ) {
        cameraManager.setTorchMode(cameraId, true)
        Log.d(TAG, "Flash turned on for $duration ms")
        delay(duration)
        cameraManager.setTorchMode(cameraId, false)
        Log.d(TAG, "Flash turned off after $duration ms")
    }

    /**
     * Flashes the camera with a custom pattern.
     */
    private suspend fun flashForPattern(
        cameraManager: CameraManager, cameraId: String, pattern: LongArray
    ) {
        for (i in pattern.indices step 2) {
            cameraManager.setTorchMode(cameraId, true)
            Log.d(TAG, "Flash turned on for ${pattern[i]} ms")
            delay(pattern[i])

            if (i + 1 < pattern.size) {
                cameraManager.setTorchMode(cameraId, false)
                Log.d(TAG, "Flash turned off for ${pattern[i + 1]} ms")
                delay(pattern[i + 1])
            } else {
                cameraManager.setTorchMode(cameraId, false)
                Log.d(TAG, "Flash turned off at the end of the pattern")
            }
        }
    }

    /**
     * RecognitionListener for SpeechRecognizer callbacks.
     */
    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "SpeechRecognizer ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "SpeechRecognizer began speaking")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "SpeechRecognizer ended speech")
        }

        override fun onError(error: Int) {
            Log.e(TAG, "SpeechRecognizer error: $error")
            resetSpeechRecognizer()
            startListening()
        }

        override fun onResults(results: Bundle?) {
            Log.d(TAG, "SpeechRecognizer results received")

            results?.let { resultBundle ->
                val matches = resultBundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                matches?.forEach { recognizedText ->

                    Log.d(TAG, "Recognized speech: $recognizedText - $keywords")

                    // Check for an exact match with any keyword or phrase
                    if (recognizedText.equals(keywords?.trim(), ignoreCase = true)) {
                        Log.d(TAG, "Exact phrase or keyword detected: $recognizedText")
                        onDetection()
                        return@forEach // Exit loop after detection to avoid multiple triggers
                    }
                }
            }
//            startListening() // Restart listening after results
        }


        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun muteSpeechRecognizerMicBeepSound(mute: Boolean, context: Context) {
        val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        if (mute) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.ADJUST_MUTE,
                0
            )
        } else {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.ADJUST_UNMUTE,
                0
            )
        }


    }

    /**
     * Reset the SpeechRecognizer to its initial state.
     */
    private fun resetSpeechRecognizer() {
        speechRecognizer?.let {
            it.stopListening()
            it.cancel()
            it.destroy()
            Log.d(TAG, "SpeechRecognizer destroyed")
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(mRecognitionListener)
        }
        Log.d(TAG, "SpeechRecognizer reset and initialized")
    }

    /**
     * Start listening for speech.
     */
    private fun startListening() {
        speechRecognizer?.startListening(recognizerIntent)
        Log.d(TAG, "SpeechRecognizer started listening")
    }

    /**
     * Get the ringtone URI from app preferences or use a default one.
     */
    private fun getRingtoneUri(): Uri {
        val ringtone = appStatusManager.getRingtone()
        return if (ringtone != -1) {
            Uri.parse("android.resource://$packageName/$ringtone")
        } else {
            // Use default system ringtone
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
    }

    // override savedStateRegistry property from SavedStateRegistryOwner interface.
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}

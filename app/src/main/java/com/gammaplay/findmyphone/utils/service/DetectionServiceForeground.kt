package com.gammaplay.findmyphone.utils.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gammaplay.findmyphone.ui.main.MainActivity
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.utils.AppStatusManager
import kotlinx.coroutines.*


/**
 * Foreground Service that detects claps and speech based on the activation type.
 * Handles "clap", "speech", and "both" activation modes.
 */
class DetectionServiceForeground : Service(),
    OnSignalsDetectedListener {

    companion object {
        const val ACTION_STOP_FUNCTIONALITY = "com.gammaplay.findmyphone.ACTION_STOP_FUNCTIONALITY"
        private const val TAG = "DetectionService"
        private const val NOTIFICATION_CHANNEL_ID = "speech_recognition_channel"
        private const val NOTIFICATION_ID = 1
        private const val DETECTION_DURATION: Long = 10000 // 10 seconds
        private const val DOUBLE_CLAP_THRESHOLD: Long = 500 // milliseconds
        private const val DETECTION_COOLDOWN: Long = 2000 // milliseconds
    }

    // Detection Mode Flags
    private var isClapDetectionActive = false
    private var isSpeechDetectionActive = false

    // Coroutine Scope for managing coroutines
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // MediaPlayer for ringtone playback
    private var mediaPlayer: MediaPlayer? = null

    // Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    // Broadcast Receiver to stop functionalities
    private lateinit var stopFunctionalityReceiver: BroadcastReceiver

    // App Status Manager for retrieving preferences (Assuming you have this class implemented)
    private lateinit var appStatusManager: AppStatusManager

    // Activation Settings
    private var activationType: String = "clap"
    private var keyword: String? = null
    private var isAllowedFlashing: Boolean = false
    private var isAllowedVibration: Boolean = false

    // Handler for scheduling mode switches
    private val handler = Handler(Looper.getMainLooper())

    // Clap Detection Variables
    private var lastClapTime: Long = 0
    private var isDoubleClap: Boolean = false
    private var canDetect: Boolean = true

    // Recorder and Detector Threads
    private var detectorThread: DetectorThread? = null
    private var recorderThread: RecorderThread? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        appStatusManager = AppStatusManager(context = this)

        // Initialize Notification and start foreground
        val notification = initNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Initialize BroadcastReceiver
        initBroadcastReceiver()

        // Initialize Speech Recognizer
        initializeSpeechRecognizer()

        // Initialize Recognition Intent
        initializeRecognitionIntent()

        // Check and apply activation settings
        checkStatus()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        stopAllFunctions()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopFunctionalityReceiver)
        serviceScope.cancel() // Cancel all coroutines
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Initialize and register the BroadcastReceiver to stop functionalities.
     */
    private fun initBroadcastReceiver() {
        stopFunctionalityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "Broadcast received to stop functionalities")
                stopAllFunctions()
            }
        }
        val filter = IntentFilter(ACTION_STOP_FUNCTIONALITY)
        LocalBroadcastManager.getInstance(this).registerReceiver(stopFunctionalityReceiver, filter)
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
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(channelName)
            .setContentText(description)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Initialize the SpeechRecognizer and set the RecognitionListener.
     */
    private fun initializeSpeechRecognizer() {
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
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        Log.d(TAG, "Recognition Intent initialized")
    }

    /**
     * Retrieve activation settings and preferences.
     */
    private fun checkStatus() {
        try {
            keyword = appStatusManager.getKeywordForVoiceRecognition()
            activationType = appStatusManager.getActivationType()
            isAllowedFlashing = appStatusManager.isFlashActive()
            isAllowedVibration = appStatusManager.isVibrationActive()

            Log.d(TAG, "Activation type: $activationType")
            Log.d(TAG, "Keyword: $keyword")
            Log.d(TAG, "Flash allowed: $isAllowedFlashing")
            Log.d(TAG, "Vibration allowed: $isAllowedVibration")
        } catch (ex: Exception) {
            Log.e(TAG, "Error in checkStatus: ${ex.message}")
        }
    }

    /**
     * Start Clap Detection by initializing RecorderThread and DetectorThread.
     */
    private fun startClapDetection() {
        Log.d(TAG, "Starting clap detection")
        isClapDetectionActive = true

        // Initialize and start the RecorderThread
        recorderThread = RecorderThread()
            .apply {
            start()
            Log.d(TAG, "RecorderThread started")
        }

        // Initialize and start the DetectorThread with the recorder thread and a preference setting
        detectorThread = DetectorThread(
            recorderThread,
            "YES"
        ).apply {
            setOnSignalsDetectedListener(this@DetectionServiceForeground)
            start()
            Log.d(TAG, "DetectorThread started")
        }
    }

    /**
     * Start Speech Detection by resetting the SpeechRecognizer and starting to listen.
     */
    private fun startSpeechDetection() {
        Log.d(TAG, "Starting speech detection")
        isSpeechDetectionActive = true
        resetSpeechRecognizer()
        startListening()
    }

    /**
     * Stop Clap Detection by stopping DetectorThread and RecorderThread.
     */
    private fun stopClapDetection() {
        Log.d(TAG, "Stopping clap detection")
        isClapDetectionActive = false

        detectorThread?.let {
            it.stopDetection()
            Log.d(TAG, "DetectorThread stopped")
        }
        detectorThread = null

        recorderThread?.let {
            it.stopRecording()
            Log.d(TAG, "RecorderThread stopped")
        }
        recorderThread = null
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
     * Handle detections by playing ringtone, vibrating, and flashing.
     */
    private fun onDetection() {
        Log.d(TAG, "Detection triggered")
        serviceScope.launch {
            playRingtone(getRingtoneUri())
            vibrate()
            flash()
        }
    }

    /**
     * Play the ringtone for a specified duration.
     */
    private suspend fun playRingtone(ringtoneUri: Uri) {
        withContext(Dispatchers.Main) {
            stopRingtone() // Stop existing ringtone before playing a new one
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                prepare()
                start()
                Log.d(TAG, "Ringtone started playing")
            }

            // Delay to stop the ringtone after 3 seconds
            delay(3000)
            stopRingtone()
        }
    }

    /**
     * Stop and release the MediaPlayer.
     */
    private fun stopRingtone() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                Log.d(TAG, "Ringtone stopped")
            }
            it.reset()
            it.release()
            Log.d(TAG, "MediaPlayer released")
        }
        mediaPlayer = null
    }

    /**
     * Vibrate the device with a waveform pattern.
     */
    private fun vibrate() {
        if (!isAllowedVibration) return

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (it.hasVibrator()) { // Check if the device has a vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For Android Oreo and above
                    val vibrationEffect = VibrationEffect.createOneShot(
                        1000, // Duration in milliseconds (1 second)
                        VibrationEffect.DEFAULT_AMPLITUDE // Use the default vibration strength
                    )
                    it.vibrate(vibrationEffect)
                    Log.d(TAG, "Vibrating for 1 second using VibrationEffect")
                } else {
                    // For Android versions below Oreo
                    @Suppress("DEPRECATION")
                    it.vibrate(1000) // Duration in milliseconds (1 second)
                    Log.d(TAG, "Vibrating for 1 second using deprecated method")
                }
            } else {
                Log.e(TAG, "Device does not support vibration")
            }
        } ?: run {
            Log.e(TAG, "Vibrator service not available")
        }
    }

    /**
     * Flash the camera's torch for a specified pattern.
     */
    private fun flash() {
        if (!isAllowedFlashing) return
        serviceScope.launch(Dispatchers.IO) {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, true)
                Log.d(TAG, "Flash turned on")

                // Flash for 3 times with delay
                repeat(3) {
                    delay(500L) // Adjust delay as needed
                    cameraManager.setTorchMode(cameraId, false) // Turn off
                    Log.d(TAG, "Flash turned off")
                    delay(500L) // Pause between flashes
                    cameraManager.setTorchMode(cameraId, true) // Turn on
                    Log.d(TAG, "Flash turned on")
                }

                // Ensure the torch is turned off after flashing
                cameraManager.setTorchMode(cameraId, false)
                Log.d(TAG, "Flash turned off after pattern")
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error using camera flash: ${e.message}")
            }
        }
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
        stopAllDetection()
        stopRingtone()
        handler.removeCallbacksAndMessages(null)
        stopSelf()
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

        override fun onRmsChanged(rmsdB: Float) {
            // Handle RMS level changes if needed
        }

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
            results?.let {
                val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let { resultList ->
                    if (resultList.any { it.contains(keyword ?: "", ignoreCase = true) }) {
                        Log.d(TAG, "Keyword '${keyword}' detected in speech")
                        onDetection()
                    }
                }
            }
            startListening() // Restart listening after results
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
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


    /**
     * Handle whistle (clap) detection.
     */
    override fun onWhistleDetected() {
        // Check if detection is allowed (not in cooldown)
        if (!canDetect) {
            Log.d(TAG, "Detection is in cooldown, ignoring this clap")
            return // Exit if in cooldown period
        }

        Log.d(TAG, "onWhistleDetected")

        val currentTime = System.currentTimeMillis()
        val timeSinceLastClap = currentTime - lastClapTime
        lastClapTime = currentTime

        isDoubleClap = if (timeSinceLastClap <= DOUBLE_CLAP_THRESHOLD) {
            // Double clap detected
            Log.d(TAG, "onWhistleDetected Double")
            true
        } else {
            // Single clap detected, reset the double clap flag
            Log.d(TAG, "onWhistleDetected Single")
            false
        }

        // If a double clap is detected, trigger the detection
        if (isDoubleClap) {
            onDetection()
            // Set detection to cooldown mode
            canDetect = false
            // Start cooldown timer
            handler.postDelayed({
                canDetect = true // Allow detection after cooldown
                Log.d(TAG, "Cooldown period ended, detection re-enabled")
            }, DETECTION_COOLDOWN)
        }
    }
}
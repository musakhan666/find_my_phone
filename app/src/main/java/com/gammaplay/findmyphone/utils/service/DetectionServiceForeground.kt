package com.gammaplay.findmyphone.utils.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.presentation.main.MainActivity
import com.gammaplay.findmyphone.utils.AppStatusManager
import com.musicg.api.ClapApi
import com.musicg.wave.WaveHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Foreground Service that detects claps and speech based on the activation type.
 * Handles "clap", "speech", and "both" activation modes.
 */
class DetectionServiceForeground : Service() {

    companion object {
        const val ACTION_STOP_FUNCTIONALITY = "com.gammaplay.findmyphone.ACTION_STOP_FUNCTIONALITY"
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

    // Broadcast Receiver to stop functionalities
    private lateinit var stopFunctionalityReceiver: BroadcastReceiver

    // App Status Manager for retrieving preferences (Assuming you have this class implemented)
    private lateinit var appStatusManager: AppStatusManager

    // Activation Settings
    private var activationType: String = "clap"
    private var keywords: String? = null
    private var isAllowedFlashing: Boolean = false
    private var isAllowedVibration: Boolean = false

    // Handler for scheduling mode switches
    private val handler = Handler(Looper.getMainLooper())


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

    private fun sendAlarmDetectedBroadcast() {
        val intent = Intent("com.gammaplay.findmyphone.ALARM_DETECTION")
        // You can also add extras if needed
        intent.putExtra("extra_data", "alarm_detected")
        sendBroadcast(intent)
        Log.d(TAG, "Alarm detected broadcast sent")
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

    private fun startClapDetection() {
        initAudioRecorder()
        isRecording = true
        Thread { detectClaps() }.start()
        isClapDetectionActive = true
        Log.d(TAG, "Clap detection started")
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

        try {
            while (isRecording) {
                val readBytes = audioRecord.read(buffer, 0, buffer.size)
                if (readBytes > 0) {
                    if (clapApi.isClap(buffer)) {
                        val currentTime = System.currentTimeMillis()

                        // If this is the first clap, save the time
                        if (firstClapTime == null) {
                            firstClapTime = currentTime
                            Log.d(TAG, "First clap detected!")
                        } else {
                            // Check if the second clap happens within the allowed interval
                            val timeSinceFirstClap = currentTime - firstClapTime
                            if (timeSinceFirstClap <= maxIntervalBetweenClaps) {
                                Log.d(TAG, "Second clap detected! Double clap confirmed.")
                                onDetection()  // Trigger detection event for double clap
                                firstClapTime = null  // Reset for next double clap detection
                            } else {
                                // If the time interval is too long, reset and treat as a new first clap
                                Log.d(TAG, "Time interval too long. Resetting clap detection.")
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopFunctionalityReceiver)
        isRecording = false
        stopRecording()
        serviceScope.cancel()
        serviceFlashScope.cancel()
        serviceVibScope.cancel()
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
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(stopFunctionalityReceiver, filter)
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

//        muteSpeechRecognizerMicBeepSound(true)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
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
            keywords = appStatusManager.getKeywordForVoiceRecognition()
            activationType = appStatusManager.getActivationType()
            isAllowedFlashing = appStatusManager.isFlashActive()
            isAllowedVibration = appStatusManager.isVibrationActive()

            Log.d(TAG, "Activation type: $activationType")
            Log.d(TAG, "Keyword: $keywords")
            Log.d(TAG, "Flash allowed: $isAllowedFlashing")
            Log.d(TAG, "Vibration allowed: $isAllowedVibration")
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
     * Handle detections by playing ringtone, vibrating, and flashing.
     */
    private fun onDetection() {
        Log.d(TAG, "Detection triggered")
        serviceScope.launch {
            sendAlarmDetectedBroadcast()
            stopAllDetection()
            playRingtone(getRingtoneUri())
            vibrate()
            flash()
        }
    }


    /**
     * Play the ringtone for a specified duration.
     */
    private suspend fun playRingtone(ringtoneUri: Uri) {
        if (mediaPlayer?.isPlaying == true) return
        withContext(Dispatchers.Main) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                isLooping = true
                prepare()
                start()
                Log.d(TAG, "Ringtone started playing")
            }

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

        serviceVibScope.launch {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (it.hasVibrator()) { // Check if the device has a vibrator
                    try {
                        while (isActive) { // Keep vibrating as long as the coroutine is active
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
                                @Suppress("DEPRECATION") it.vibrate(1000) // Duration in milliseconds (1 second)
                                Log.d(TAG, "Vibrating for 1 second using deprecated method")
                            }

                            delay(1500L) // Delay between vibrations (1.5 seconds)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in vibration loop: ${e.message}")
                    } finally {
                        it.cancel() // Ensure vibrator is cancelled when exiting
                    }
                } else {
                    Log.e(TAG, "Device does not support vibration")
                }
            } ?: run {
                Log.e(TAG, "Vibrator service not available")
            }
        }
    }

    /**
     * Flash the camera's torch for a specified pattern.
     */
    private fun flash() {
        if (!isAllowedFlashing) return

        serviceFlashScope.launch {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            try {

                // Start an indefinite flashing loop
                while (isActive) {
                    // Turn on the flashlight
                    cameraManager.setTorchMode(cameraId, true)
                    Log.d(TAG, "Flash turned on")

                    // Pause before turning off
                    delay(500L)

                    // Turn off the flashlight
                    cameraManager.setTorchMode(cameraId, false)
                    Log.d(TAG, "Flash turned off")

                    // Pause before turning on again
                    delay(500L)
                }
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error using camera flash: ${e.message}")
            } finally {
                // Ensure the torch is turned off when the coroutine exits
                cameraManager.setTorchMode(cameraId, false)
                Log.d(TAG, "Flash turned off after loop ended")
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
                matches?.let { resultList ->
                    resultList.forEach { recognizedText ->
                        Log.d(TAG, "Recognized speech: $recognizedText")

                        if (keywords?.equals(recognizedText, ignoreCase = true) == true) {
                            Log.d(TAG, "Keyword or phrase detected in speech")
                            muteSpeechRecognizerMicBeepSound(false)
                            onDetection()
                            return@forEach
                        }
                    }
                }
            }
            startListening() // Restart listening after results
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun muteSpeechRecognizerMicBeepSound(mute: Boolean) {
        val manager = getSystemService(AUDIO_SERVICE) as AudioManager
        manager.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute)
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
}

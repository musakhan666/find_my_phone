import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.musicg.api.ClapApi
import com.musicg.wave.WaveHeader

class ClapDetectionService : Service() {

    private val TAG = "ClapDetectionService"
    private lateinit var audioRecord: AudioRecord
    private lateinit var clapApi: ClapApi
    private val buffer = ByteArray(2048)
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startClapDetections()
        return START_STICKY
    }

    private fun startForegroundService() {
        // Create a NotificationChannel for devices running Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "ClapDetectionServiceChannel"
            val channel = NotificationChannel(
                channelId,
                "Clap Detection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Clap Detection Service")
                .setContentText("Listening for claps...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()

            startForeground(1, notification)
        }
    }

    private fun startClapDetections() {
        initAudioRecorder()
        isRecording = true
        Thread { detectClaps() }.start()
        Log.d(TAG, "Clap detection started")
    }

    private fun initAudioRecorder() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
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

    private fun detectClaps() {
        try {
            while (isRecording) {
                val readBytes = audioRecord.read(buffer, 0, buffer.size)
                if (readBytes > 0) {
                    if (clapApi.isClap(buffer)) {
                        Log.d(TAG, "Clap detected!")
                        // Notify about clap detection (e.g., via broadcast)
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
        isRecording = false
        stopRecording()
        Log.d(TAG, "Clap detection service stopped")
        super.onDestroy()
    }
}

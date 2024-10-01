package com.gammaplay.findmyphone.utils

import android.content.Context
import android.media.AudioManager

class AudioHelper(private val context: Context) {
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalSystemVolume: Int = 0

    fun muteSystemSounds() {
        // Save the original system volume
        originalSystemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
        // Set the system volume to 0
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
    }

    fun restoreSystemSounds() {
        // Restore the original system volume
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0)
    }
}

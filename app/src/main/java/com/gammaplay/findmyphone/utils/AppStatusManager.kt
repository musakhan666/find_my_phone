package com.gammaplay.findmyphone.utils

import android.content.Context
import android.content.SharedPreferences

class AppStatusManager(context: Context) {

    companion object {
        private const val PREF_NAME = "service_status_pref"
        private const val KEY_IS_ACTIVE = "is_service_active"
        private const val KEY_IS_FLASH = "is_flash_active"
        private const val KEY_IS_VIBRATION = "is_vibration_active"
        private const val KEY_IS_KEYWORD = "is_keyword"
        private const val KEY_IS_RINGTONE = "ring_tone"

    }

    fun hasShownTutorial(): Boolean {
        return sharedPreferences.getBoolean("has_shown_tutorial", false)
    }

    // Function to mark the tutorial as shown
    fun setTutorialShown() {
        sharedPreferences.edit().putBoolean("has_shown_tutorial", true).apply()
        setPreference("flash", "YES")
        setPreference("vibration", "YES")
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Set the service activation status.
     */
    fun setServiceActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_ACTIVE, isActive).apply()
        if (isActive.not()) setPreference("startButton", "YES") else setPreference("startButton", "NO")
    }

    private fun setFlashActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_FLASH, isActive).apply()
        if (isActive) setPreference("flash", "YES") else setPreference("vibration", "NO")

    }

    private fun setVibrationActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_VIBRATION, isActive).apply()
        if (isActive) setPreference("vibration", "YES") else setPreference("vibration", "NO")

    }


    private fun setPreference(string: String, value: String) {
        sharedPreferences.edit().putString(string, value).apply()

    }

    /**
     * Get the current service activation status.
     * Default value is `false` (inactive).
     */

    fun isServiceActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ACTIVE, true)
    }

    fun isFlashActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FLASH, true)
    }

    fun isVibrationActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_VIBRATION, true)
    }

    // Function to get the current activation type
    fun getActivationType(): String {
        return sharedPreferences.getString("activation_type", "clap") ?: "clap"
    }

    // Function to set the activation type based on the index
    fun setActivationType(index: Int) {
        val type = when (index) {
            0 -> "clap"
            1 -> "speech"
            2 -> "both"
            else -> "clap"
        }
        sharedPreferences.edit().putString("activation_type", type).apply()
    }

    /**
     * Toggle the service activation status.
     */
    fun toggleServiceStatus() {
        val currentStatus = isServiceActive()
        setServiceActive(!currentStatus)
    }

    fun toggleFlashStatus() {
        val currentStatus = isFlashActive()
        setFlashActive(!currentStatus)
    }

    fun toggleVibrationStatus() {
        val currentStatus = isVibrationActive()
        setVibrationActive(!currentStatus)
    }

    fun setKeywordForVoiceRecognition(keyword: String?) {
        sharedPreferences.edit().putString(KEY_IS_KEYWORD, keyword).apply()
    }

    fun getKeywordForVoiceRecognition(): String {
        val gettingKeyword = sharedPreferences.getString(KEY_IS_KEYWORD, "") ?: ""
        return gettingKeyword
    }

    fun getRingtone(): Int {
        val ringtone = sharedPreferences.getInt(KEY_IS_RINGTONE, -1)
        return ringtone
    }

    fun setRingTone(ringtone: Int) {
        sharedPreferences.edit().putInt(KEY_IS_RINGTONE, ringtone).apply()
    }
}

package com.gammaplay.findmyphone.utils

import android.content.Context
import android.content.SharedPreferences
import com.gammaplay.findmyphone.R

class AppStatusManager(val context: Context) {

    companion object {
        private const val PREF_NAME = "service_status_pref"
        private const val KEY_IS_ACTIVE = "is_service_active"
        private const val KEY_IS_FLASH = "is_flash_active"
        private const val KEY_IS_VIBRATION = "is_vibration_active"
        private const val KEY_IS_KEYWORD = "is_keyword"
        private const val KEY_IS_RINGTONE = "ring_tone"

        // Keys for modes and settings
        private const val KEY_VIBRATION_MODE = "vibration_mode"
        private const val KEY_DURATION_MODE = "duration_mode"
        private const val KEY_SENSITIVITY_LEVEL = "sensitivity_level"
        private const val KEY_FLASHLIGHT_MODE = "flashlight_mode"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Tutorial handling
     */
    fun hasShownTutorial(): Boolean {
        return sharedPreferences.getBoolean("has_shown_tutorial", false)
    }

    fun setTutorialShown() {
        sharedPreferences.edit().putBoolean("has_shown_tutorial", true).apply()
        setPreference("flash", "YES")
        setPreference("vibration", "YES")
    }

    /**
     * Service status management
     */
    fun setServiceActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_ACTIVE, isActive).apply()
        setPreference("startButton", if (isActive) "NO" else "YES")
    }

    fun isServiceActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ACTIVE, true)
    }

    fun toggleServiceStatus() {
        val currentStatus = isServiceActive()
        setServiceActive(!currentStatus)
    }

    /**
     * Flashlight management
     */
    fun setFlashActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_FLASH, isActive).apply()
        setPreference("flash", if (isActive) "YES" else "NO")
    }

    fun isFlashActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FLASH, true)
    }

    fun toggleFlashStatus() {
        val currentStatus = isFlashActive()
        setFlashActive(!currentStatus)
    }

    fun setFlashlightMode(mode: Int) {
        sharedPreferences.edit().putInt(KEY_FLASHLIGHT_MODE, mode).apply()
    }

    fun getFlashlightMode(): Int {
        return sharedPreferences.getInt(KEY_FLASHLIGHT_MODE, R.string.flashlight_mode_short_blink) ?: 0
    }

    /**
     * Vibration management
     */
    fun setVibrationActive(isActive: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_VIBRATION, isActive).apply()
        setPreference("vibration", if (isActive) "YES" else "NO")
    }

    fun isVibrationActive(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_VIBRATION, true)
    }

    fun toggleVibrationStatus() {
        val currentStatus = isVibrationActive()
        setVibrationActive(!currentStatus)
    }

    fun setVibrationMode(mode: Int) {
        sharedPreferences.edit().putInt(KEY_VIBRATION_MODE, mode).apply()
    }

    fun getVibrationMode(): Int {
        return sharedPreferences.getInt(KEY_VIBRATION_MODE, R.string.vibration_mode_wave) ?: 0
    }

    /**
     * Duration management
     */
    fun setDurationMode(duration: Int) {
        sharedPreferences.edit().putInt(KEY_DURATION_MODE, duration).apply()
    }

    fun getDurationMode(): Int {
        return sharedPreferences.getInt(KEY_DURATION_MODE, R.string.duration_loop) ?: 0
    }

    /**
     * Sensitivity management
     */
    fun setSensitivityLevel(level: Int) {
        sharedPreferences.edit().putInt(KEY_SENSITIVITY_LEVEL, level).apply()
    }

    fun getSensitivityLevel(): Int {
        return sharedPreferences.getInt(KEY_SENSITIVITY_LEVEL, R.string.sensitivity_medium) ?: 0
    }

    /**
     * Activation type management
     */
    fun getActivationType(): String {
        return sharedPreferences.getString("activation_type", context.getString(R.string.clap))
            ?: context.getString(R.string.clap)
    }

    fun setActivationType(index: Int) {
        val type = when (index) {
            0 -> context.getString(R.string.clap)
            1 -> context.getString(R.string.speech)
            2 -> context.getString(R.string.both)
            else -> context.getString(R.string.clap)
        }
        sharedPreferences.edit().putString("activation_type", type).apply()
    }

    /**
     * Keyword management for voice recognition
     */
    fun setKeywordForVoiceRecognition(keyword: String?) {
        sharedPreferences.edit().putString(KEY_IS_KEYWORD, keyword).apply()
    }

    fun getKeywordForVoiceRecognition(): String {
        return sharedPreferences.getString(KEY_IS_KEYWORD, "") ?: ""
    }

    /**
     * Ringtone management
     */
    fun getRingtone(): Int { return sharedPreferences.getInt(KEY_IS_RINGTONE, R.raw.cat) }

    private fun setPreference(key: String, value: String) { sharedPreferences.edit().putString(key, value).apply() }

    fun setRingTone(ringtone: Int) { sharedPreferences.edit().putInt(KEY_IS_RINGTONE, ringtone).apply() }
}

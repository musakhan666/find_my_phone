package com.gammaplay.findmyphone.presentation.settings

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.lifecycle.ViewModel
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.data.SettingsGeneralItem
import com.gammaplay.findmyphone.data.SettingsItem
import com.gammaplay.findmyphone.utils.AppStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(@ApplicationContext private val application: Context) :
    ViewModel() {
    private val serviceStatusManager = AppStatusManager(application.applicationContext)

    private val _languageActiveCardIndex = MutableStateFlow(0)
    val languageActiveCardIndex: StateFlow<Int> = _languageActiveCardIndex

    // Function to update the active card index
    fun setActiveCardIndex(index: Int) {
        _languageActiveCardIndex.value = index
        OtherItems[0].subtitle = languageContentDescriptionIds[index]
    }

    fun fetchValues() {
        generalItems[0].subtitle = serviceStatusManager.getFlashlightMode()
        generalItems[1].subtitle = serviceStatusManager.getVibrationMode()
        generalItems[2].subtitle = serviceStatusManager.getDurationMode()
        generalItems[3].subtitle = serviceStatusManager.getSensitivityLevel()

    }

    fun setFlashlightMode(mode: Int) {
        generalItems[0].subtitle = mode
        serviceStatusManager.setFlashlightMode(mode)

    }

    fun setVibrationMode(mode: Int) {
        generalItems[1].subtitle = mode
        serviceStatusManager.setVibrationMode(mode)

    }

    fun setDurationMode(mode: Int) {
        generalItems[2].subtitle = mode
        serviceStatusManager.setDurationMode(mode)

    }

    fun setSensitivityMode(mode: Int) {
        generalItems[3].subtitle = mode
        serviceStatusManager.setSensitivityLevel(mode)

    }


    val flashlightModes = listOf(
        R.string.flashlight_mode_long_blink,
        R.string.flashlight_mode_short_blink,
        R.string.flashlight_mode_pulse,
        R.string.flashlight_mode_sos,
        R.string.flashlight_mode_strobe,
        R.string.flashlight_mode_firefly,
        R.string.flashlight_mode_continuous_on
    )


    // Vibration Modes using string resource IDs
    val vibrationModes = listOf(
        R.string.vibration_mode_wave,
        R.string.vibration_mode_heartbeat,
        R.string.vibration_mode_short_pulse,
        R.string.vibration_mode_long_pulse,
        R.string.vibration_mode_ramp
    )

    // Duration Modes using string resource IDs
    val durationModes = listOf(
        R.string.duration_5s,
        R.string.duration_10s,
        R.string.duration_30s,
        R.string.duration_1min,
        R.string.duration_loop
    )

    // Sensitivity Levels using string resource IDs
    val sensitivityLevels = listOf(
        R.string.sensitivity_low,
        R.string.sensitivity_medium,
        R.string.sensitivity_high,
        R.string.sensitivity_maximum
    )


    val OtherItems = listOf(
        SettingsItem(
            title = R.string.language,
            icon = Icons.Filled.Language,
            subtitle = R.string.english
        ), SettingsItem(
            title = R.string.rate_us,
            icon = Icons.Filled.StarRate,
        ), SettingsItem(
            title = R.string.share_app,
            icon = Icons.Filled.Share,
        )
    )

    val generalItems = listOf(
        SettingsGeneralItem(
            title = R.string.flashlight,
            icon = R.drawable.ic_flashlight
        ), SettingsGeneralItem(
            title = R.string.vibration,
            icon = R.drawable.ic_vibration
        ), SettingsGeneralItem(
            title = R.string.Duration,
            icon = R.drawable.ic_timelapse
        ),
        SettingsGeneralItem(
            title = R.string.sensitivity,
            icon = R.drawable.ic_sensor
        )
    )

    val languageIconIds = listOf(
        R.drawable.english,
        R.drawable.spain,
        R.drawable.french,
        R.drawable.german,
        R.drawable.italian,
        R.drawable.polish,
        R.drawable.russian,
        R.drawable.sweden,
        R.drawable.czech
    )

    val languageContentDescriptionIds = listOf(
        R.string.english,
        R.string.spanish,
        R.string.french,
        R.string.german,
        R.string.italian,
        R.string.polish,
        R.string.russian,
        R.string.swedish,
        R.string.czech
    )

}
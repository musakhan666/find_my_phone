package com.gammaplay.findmyphone.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.utils.service.DetectionServiceForeground
import com.gammaplay.findmyphone.utils.AppStatusManager

class HomeViewModel(application: Context) : ViewModel() {
    private val serviceStatusManager = AppStatusManager(application.applicationContext)

    private val _activeCardIndex = mutableIntStateOf(0)
    val activeCardIndex: State<Int> get() = _activeCardIndex

    private val _activationActiveCardIndex = MutableLiveData(updateIndexBasedOnActivationType())

    val activationActiveCardIndex: LiveData<Int> get() = _activationActiveCardIndex
    private val _textFieldValue = mutableStateOf(serviceStatusManager.getKeywordForVoiceRecognition())
    val textFieldValue: State<String> get() = _textFieldValue



    // Function to update the text value
    fun updateTextFieldValue(newValue: String) {
        Log.d("Textfield", "$newValue test")
        _textFieldValue.value = newValue
    }

    fun setTextFieldValue() {
        val keyword = serviceStatusManager.getKeywordForVoiceRecognition()
        if (keyword.isEmpty()) return
        _textFieldValue.value = keyword
    }

    private fun updateIndexBasedOnActivationType(): Int {
        val activationType = serviceStatusManager.getActivationType()

        return when (activationType) {
            "clap" -> 0
            "speech" -> 1
            "both" -> 2
            else -> 0 // Default case
        }
    }


    // LiveData to observe the service status in the UI
    private val _isActivated = MutableLiveData(serviceStatusManager.isServiceActive())
    val isActivated: LiveData<Boolean> get() = _isActivated

    // Function to toggle activation status
    fun toggleActivation(context: Context) {
        serviceStatusManager.toggleServiceStatus()
        _isActivated.value = serviceStatusManager.isServiceActive()
        if (_isActivated.value != true) startService(context = context) else stopService(context)

    }

    // LiveData to observe the service status in the UI
    private val _isActivatedFlashLight = MutableLiveData(serviceStatusManager.isFlashActive())
    val isActivatedFlash: LiveData<Boolean> get() = _isActivatedFlashLight

    // Function to toggle activation status
    fun toggleFlashActivation() {
        serviceStatusManager.toggleFlashStatus()
        _isActivatedFlashLight.value = serviceStatusManager.isFlashActive()

    }

    private val _isActivatedVibration = MutableLiveData(serviceStatusManager.isVibrationActive())
    val isActivatedVibration: LiveData<Boolean> get() = _isActivatedVibration

    // Function to toggle activation status
    fun toggleActivatedVibration() {
        serviceStatusManager.toggleVibrationStatus()
        _isActivatedVibration.value = serviceStatusManager.isVibrationActive()

    }

    private fun startService(context: Context) {
        val serviceIntent = Intent(context, DetectionServiceForeground::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopService(context: Context) {
        val serviceIntent = Intent(context, DetectionServiceForeground::class.java)
        context.stopService(serviceIntent)
    }

    // Sound resource IDs
    val soundIcons = listOf(
        R.drawable.cat,
        R.drawable.dog,
        R.drawable.bird,
        R.drawable.alarm,
        R.drawable.fanfare,
        R.drawable.drum,
        R.drawable.guitar,
        R.drawable.siren,
        R.drawable.bell,
        R.drawable.harp,
        R.drawable.claxon,
        R.drawable.electro_guitar
    )
    val sound = listOf(
        R.raw.cat,
        R.raw.dog,
        R.raw.bird,
        R.raw.alarm_clock,
        R.raw.trumpet,
        R.raw.drum,
        R.raw.guitar,
        R.raw.siren,
        R.raw.bell,
        R.raw.harp,
        R.raw.claxon,
        R.raw.electric_guitar
    )

    private val _soundContentDescriptions = listOf(
        "cat", "dog", "bird", "alarm", "fanfare",
        "drum", "guitar", "siren", "bell", "harp",
        "claxon", "electro_guitar"
    )

    val soundContentDescriptions: List<String> get() = _soundContentDescriptions

    // Activation icons and descriptions
    private val _activationIconRes = listOf(
        R.drawable.clap,
        R.drawable.voice,
        R.drawable.voice_and_clap
    )
    val activationIconRes: List<Int> get() = _activationIconRes

    private val _activationContentDescriptions = listOf(
        "Just claps", "Just voice", "Both"
    )
    val activationContentDescriptions: List<String> get() = _activationContentDescriptions


    fun setActiveCardIndex(index: Int) {
        _activeCardIndex.intValue = index
    }

    fun setActivationActiveCardIndex(index: Int) {
        _activationActiveCardIndex.value = index
        serviceStatusManager.setActivationType(index)
    }

    fun submit(keyword: String?) {
        if (keyword.isNullOrEmpty().not()) serviceStatusManager.setKeywordForVoiceRecognition(
            keyword
        )
    }

    fun setSound() {
        val ringtone = sound[activeCardIndex.value]
        serviceStatusManager.setRingTone(ringtone)
    }
}

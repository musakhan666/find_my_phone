package com.gammaplay.findmyphone.ui.permission

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel

private const val PERMISSION_CAMERA = "android.permission.CAMERA"
private const val PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO"
private const val PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"
private const val REQUEST_CODE_PERMISSIONS = 123 // Arbitrary request code

class PermissionsViewModel : ViewModel() {
    val requiredPermissions = mutableListOf(
        PERMISSION_CAMERA,
        PERMISSION_RECORD_AUDIO,
        PERMISSION_READ_PHONE_STATE
    )

    fun checkAndRequestPermissions(context: Context) {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PermissionChecker.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            val activity = context as Activity
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }
}

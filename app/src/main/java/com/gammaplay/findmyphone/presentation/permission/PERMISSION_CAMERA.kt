package com.gammaplay.findmyphone.presentation.permission

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel

private const val REQUEST_CODE_PERMISSIONS = 123 // Arbitrary request code

class PermissionsViewModel : ViewModel() {


    val requiredPermissions = mutableListOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_PHONE_STATE",
        "android.permission.POST_NOTIFICATIONS" // New for Android 13+
    )

    fun checkPermissions(context: Context, callback: (Boolean) -> Unit) {
        val missingPermissions = requiredPermissions.filter {
            val permissionGranted = ContextCompat.checkSelfPermission(context, it) == PermissionChecker.PERMISSION_GRANTED
            Log.d("PermissionsCheck", "Permission: $it, Granted: $permissionGranted")
            !permissionGranted
        }

        if (missingPermissions.isNotEmpty()) {
            // Show the dialog
            callback.invoke(true) // Permissions are missing
        } else {
            // All permissions are granted
            callback.invoke(false)
        }
    }

    fun requestPermissions(context: Context) {
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


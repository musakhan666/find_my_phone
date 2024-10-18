package com.gammaplay.findmyphone.presentation.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel

private const val REQUEST_CODE_PERMISSIONS = 123 // Arbitrary request code

class PermissionsViewModel : ViewModel() {

    val requiredPermissions = mutableListOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.POST_NOTIFICATIONS" // New for Android 13+
    )

    // Check permissions and return the missing ones
    fun checkPermissions(context: Context, callback: (List<String>) -> Unit) {
        val missingPermissions = requiredPermissions.filter {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                it
            ) == PermissionChecker.PERMISSION_GRANTED
            Log.d("PermissionsCheck", "Permission: $it, Granted: $permissionGranted")
            !permissionGranted
        }

        callback(missingPermissions)  // Return the list of missing permissions
    }

    // Request specific permissions
    fun requestPermissions(context: Context, permissions: List<String>) {
        if (permissions.isNotEmpty()) {
            val activity = context as Activity
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }

        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)

        }


    }
}

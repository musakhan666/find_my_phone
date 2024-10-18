package com.gammaplay.findmyphone.presentation.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.gammaplay.findmyphone.presentation.permission.PermissionsViewModel

@Composable
fun showPermissionsDialog(
    missingPermissions: List<String>,
    viewModel: PermissionsViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit
) {
    if (missingPermissions.size == viewModel.requiredPermissions.size && !Settings.canDrawOverlays(
            context
        )
    ) {
        // Show main permission dialog if all permissions are missing
        PermissionDialog(
            onDismiss = onDismiss,
            onGrantPermissions = {
                viewModel.requestPermissions(context, missingPermissions) // Request all permissions

                onGrantPermissions.invoke()
            }
        )
    } else {
        // Show specific permission dialog based on which one is missing
        when {
            missingPermissions.contains("android.permission.RECORD_AUDIO") -> {
                MicrophonePermissionDialog(
                    onDismiss = onDismiss,
                    onGrantPermissions = {
                        viewModel.requestPermissions(
                            context,
                            listOf("android.permission.RECORD_AUDIO")
                        )
                        onGrantPermissions.invoke()
                    }
                )
            }



            missingPermissions.contains("android.permission.CAMERA") -> {
                CameraPermissionDialog(
                    onDismiss = onDismiss,
                    onGrantPermissions = {
                        viewModel.requestPermissions(context, listOf("android.permission.CAMERA"))
                        onGrantPermissions.invoke()
                    }
                )
            }

            missingPermissions.contains("android.permission.POST_NOTIFICATIONS") -> {
                NotificationPermissionDialog(
                    onDismiss = onDismiss,
                    onGrantPermissions = {
                        viewModel.requestPermissions(
                            context,
                            listOf("android.permission.POST_NOTIFICATIONS")
                        )
                        onGrantPermissions.invoke()
                    }
                )
            }

            !Settings.canDrawOverlays(context) -> {

                OverlayPermissionDialog(
                    onDismiss = onDismiss,
                    onGrantPermissions = {
                        viewModel.requestPermissions(
                            context,
                            listOf("android.permission.POST_NOTIFICATIONS")
                        )
                        onGrantPermissions.invoke()
                    }
                )


            }
        }
    }
}

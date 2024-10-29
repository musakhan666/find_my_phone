package com.gammaplay.findmyphone.presentation.bottomsheets

import ModesGrid
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gammaplay.findmyphone.R

@Composable
fun VibrationModesBottomSheetContent(
    vibrationModes: List<Int>,  // List of vibration modes as string resource IDs
    onOptionSelected: (Int) -> Unit,  // Callback when an option is selected
    onDismiss: () -> Unit,             // Callback to dismiss the bottom sheet
    selectedMode: Int
) {
    // Keep track of the selected vibration mode
    var selectedMode by remember { mutableStateOf(selectedMode) }
    val context = LocalContext.current

    // Bottom Sheet content layout
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(id = R.string.choose_vibration_mode),  // You need to add this string
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Vibration mode buttons (reuse FlashlightModeButton with minor adjustments if necessary)
        ModesGrid(
            isFlashLight = false,
            modes = vibrationModes,
            selectedMode = selectedMode
        ) { selectedMode = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Play Test and OK buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            // "Play Test" button
            Button(
                onClick = {
                    playTestVibrationMode(
                        context,
                        selectedMode
                    )
                },  // Implement the logic for vibration play test
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp)
            ) {
                Text(text = stringResource(id = R.string.play_test))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Ok" button
            Button(
                onClick = {
                    onOptionSelected(selectedMode)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp)
            ) {
                Text(text = stringResource(id = R.string.ok))
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

    }
}


fun playTestVibrationMode(context: Context, mode: Int) {
    // Get the Vibrator system service
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (!vibrator.hasVibrator()) {
        println("Device does not support vibration")
        return
    }

    // Define vibration patterns for each mode (approximately 2 seconds each)
    val vibrationPattern = when (mode) {
        R.string.vibration_mode_wave -> longArrayOf(0, 300, 200, 300, 200, 300, 700) // Wave pattern
        R.string.vibration_mode_heartbeat -> longArrayOf(
            0,
            400,
            200,
            400,
            1000
        ) // Heartbeat-like pulses
        R.string.vibration_mode_short_pulse -> longArrayOf(
            0,
            200,
            200,
            200,
            400,
            1000
        ) // Short quick pulses
        R.string.vibration_mode_long_pulse -> longArrayOf(
            0,
            800,
            400,
            800
        ) // Long pulses with pauses
        R.string.vibration_mode_ramp -> longArrayOf(
            0,
            400,
            400,
            400,
            400,
            400
        ) // Increasing intensity
        else -> longArrayOf(0) // Default case, no vibration
    }

    // Trigger vibration based on the device API level
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // For API 26 and above, use VibrationEffect
        vibrator.vibrate(
            VibrationEffect.createWaveform(vibrationPattern, -1)  // -1 indicates no repeat
        )
    } else {
        // For older devices
        vibrator.vibrate(vibrationPattern, -1)  // -1 indicates no repeat
    }
}

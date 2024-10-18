import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gammaplay.findmyphone.R

@Composable
fun FlashlightModesBottomSheetContent(
    flashlightModes: List<Int>,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,             // Callback to dismiss the bottom sheet
    selectedMode: Int
) {
    var selectedMode by remember { mutableStateOf(selectedMode) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.choose_flashlight_mode),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Flashlight mode buttons
        ModesGrid(modes = flashlightModes, selectedMode = selectedMode) {
            selectedMode = it
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            // "Play Test" button
            Button(
                onClick = { playTestFlashlightMode(context, selectedMode) },
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
    }
}

@Composable
fun ModesGrid(
    modes: List<Int>,
    selectedMode: Int,
    onSelectMode: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp), // Adaptive layout with minimum item width
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(5.dp)
    ) {
        items(modes.size) { index ->
            val mode = modes[index]
            Box(
                modifier = Modifier
                    .wrapContentSize() // Ensures each item has its own size
            ) {
                ButtonModeChip(
                    mode = mode,
                    isSelected = selectedMode == mode,
                    onSelectMode = { onSelectMode(mode) }
                )
            }
        }
    }

}

@Composable
fun ButtonModeChip(mode: Int, isSelected: Boolean, onSelectMode: () -> Unit) {
    val text = stringResource(id = mode) // Assuming mode is a string resource ID

    FilterChip(
        modifier = Modifier.padding(vertical = 3.dp).height(50.dp),
        onClick = { onSelectMode() },
        label = {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp, // Font size for readability
                maxLines = 1, // Ensures the text stays in one line
                overflow = TextOverflow.Ellipsis, // Shows ellipsis (three dots) when text is too long
            )

        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Selected icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    tint = Color.White
                )
            }
        } else null, // No icon when unselected
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) colorResource(id = R.color.big_btn_active) else Color.White,
            labelColor = if (isSelected) Color.White else colorResource(id = R.color.black),
            selectedLeadingIconColor = Color.White,
            selectedContainerColor = colorResource(id = R.color.big_btn_active)
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colorResource(id = R.color.black),
            selectedBorderColor = colorResource(id = R.color.big_btn_active),
            enabled = true,
            selected = isSelected
        ),
    )
}



fun playTestFlashlightMode(context: Context, mode: Int) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    if (cameraId == null) {
        println(context.getString(R.string.no_flashlight_found))
        return
    }

    when (mode) {
        R.string.flashlight_mode_short_blink -> runBlinkMode(cameraManager, cameraId, 200, 200, 2000)
        R.string.flashlight_mode_long_blink -> runBlinkMode(cameraManager, cameraId, 500, 500, 2000)
        R.string.flashlight_mode_pulse -> runPulseMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_sos -> runSOSMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_continuous_on -> runContinuousMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_strobe -> runStrobeMode(cameraManager, cameraId, 50, 2000)
        R.string.flashlight_mode_firefly -> runBlinkMode(cameraManager, cameraId, 100, 500, 2000)
    }
}

// Continuous Mode: run for max 2 seconds
fun runContinuousMode(cameraManager: CameraManager, cameraId: String, duration: Long) {
    try {
        cameraManager.setTorchMode(cameraId, true)
        Handler(Looper.getMainLooper()).postDelayed({
            cameraManager.setTorchMode(cameraId, false)
        }, duration)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Blink Mode: run for max 2 seconds
fun runBlinkMode(
    cameraManager: CameraManager,
    cameraId: String,
    durationOn: Long,
    durationOff: Long,
    totalDuration: Long
) {
    CoroutineScope(Dispatchers.Main).launch {
        var elapsed = 0L
        while (elapsed < totalDuration) {
            cameraManager.setTorchMode(cameraId, true)
            delay(durationOn)
            cameraManager.setTorchMode(cameraId, false)
            delay(durationOff)
            elapsed += durationOn + durationOff
        }
    }
}

// Pulse Mode: max 2 seconds
fun runPulseMode(cameraManager: CameraManager, cameraId: String, totalDuration: Long) {
    CoroutineScope(Dispatchers.Main).launch {
        var elapsed = 0L
        while (elapsed < totalDuration) {
            cameraManager.setTorchMode(cameraId, true)
            delay(300)
            cameraManager.setTorchMode(cameraId, false)
            delay(100)
            cameraManager.setTorchMode(cameraId, true)
            delay(500)
            cameraManager.setTorchMode(cameraId, false)
            delay(300)
            elapsed += 1200  // The total time for one pulse
        }
    }
}

// SOS Mode: run for max 2 seconds
fun runSOSMode(cameraManager: CameraManager, cameraId: String, totalDuration: Long) {
    CoroutineScope(Dispatchers.Main).launch {
        val sosPattern = listOf(
            200L, 200L, 200L,  // Short blinks (S)
            500L, 500L, 500L,  // Long blinks (O)
            200L, 200L, 200L   // Short blinks (S)
        )
        var elapsed = 0L
        for (i in sosPattern.indices) {
            if (elapsed >= totalDuration) break
            cameraManager.setTorchMode(cameraId, i % 2 == 0)
            delay(sosPattern[i])
            elapsed += sosPattern[i]
        }
        cameraManager.setTorchMode(cameraId, false)
    }
}

// Strobe Mode: run for max 2 seconds
fun runStrobeMode(cameraManager: CameraManager, cameraId: String, strobeInterval: Long, totalDuration: Long) {
    CoroutineScope(Dispatchers.Main).launch {
        var elapsed = 0L
        while (elapsed < totalDuration) {
            cameraManager.setTorchMode(cameraId, true)
            delay(strobeInterval)
            cameraManager.setTorchMode(cameraId, false)
            delay(strobeInterval)
            elapsed += strobeInterval * 2
        }
    }
}


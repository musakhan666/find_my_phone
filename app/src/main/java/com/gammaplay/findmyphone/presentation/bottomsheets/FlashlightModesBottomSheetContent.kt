import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gammaplay.findmyphone.R
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.*

@Composable
fun FlashlightModesBottomSheetContent(
    flashlightModes: List<Int>,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    selectedMode: Int
) {
    var selectedMode by remember { mutableStateOf(selectedMode) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.choose_flashlight_mode),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.title_text)
        )

        // Flashlight mode buttons in a grid
        Spacer(modifier = Modifier.height(16.dp))
        ModesGrid(
            modes = flashlightModes,
            selectedMode = selectedMode
        ) { selectedMode = it }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            // "Play Test" button
            Button(
                onClick = {
                    playTestFlashlightMode(
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

@Composable
fun ModesGrid(
    isFlashLight: Boolean = true,
    modes: List<Int>,
    selectedMode: Int,
    onSelectMode: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Adaptive grid with minimum item width
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(5.dp)
    ) {
        val size = when {
            isFlashLight -> modes.size - 1
            else -> modes.size
        }
        items(size) { index ->
            val mode = modes[index]
            ModeChip(
                mode = mode,
                isSelected = selectedMode == mode,
                onSelectMode = { onSelectMode(mode) }
            )
        }
        // give the last item the size of one and half
        if (isFlashLight)
            item(span = { GridItemSpan(3) }) {
                ModeChip(
                    mode = modes.last(),
                    isSelected = selectedMode == modes.last(),
                    modifier = Modifier.wrapContentWidth(),
                    textModifier = Modifier.wrapContentWidth(),
                    onSelectMode = { onSelectMode(modes.last()) },
                )
            }
    }
}

@Composable
fun ModeChip(
    mode: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
        .padding(horizontal = 2.dp)
        .fillMaxWidth(),
    textModifier: Modifier = Modifier.fillMaxWidth(),
    onSelectMode: () -> Unit
) {
    val backgroundColor =
        if (isSelected) colorResource(id = R.color.big_btn_active) else Color.Transparent

    FilterChip(
        modifier = modifier,
        onClick = onSelectMode,
        label = {
            Text(
                text = stringResource(id = mode),
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontSize = 14.sp,
                modifier = textModifier
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = colorResource(id = R.color.title_text),
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            selectedContainerColor = colorResource(id = R.color.big_btn_active),
            containerColor = backgroundColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colorResource(id = R.color.title_text),
            selectedBorderColor = colorResource(id = R.color.big_btn_active),
            enabled = true,
            selected = isSelected
        )
    )
}

fun playTestFlashlightMode(context: Context, mode: Int) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    if (cameraId == null) {
        // Handle if flashlight is not available
        println("No flashlight available on this device")
        return
    }

    // Run different flashlight modes based on the selected mode
    when (mode) {
        R.string.flashlight_mode_short_blink -> runBlinkMode(
            cameraManager,
            cameraId,
            200,
            200,
            2000
        )

        R.string.flashlight_mode_long_blink -> runBlinkMode(cameraManager, cameraId, 500, 500, 2000)
        R.string.flashlight_mode_pulse -> runPulseMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_sos -> runSOSMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_continuous_on -> runContinuousMode(cameraManager, cameraId, 2000)
        R.string.flashlight_mode_strobe -> runStrobeMode(cameraManager, cameraId, 50, 2000)
        R.string.flashlight_mode_firefly -> runBlinkMode(cameraManager, cameraId, 100, 500, 2000)
    }
}

// Continuous Mode: turns on flashlight continuously for a specified duration
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

// Blink Mode: flashlight blinks on and off at specified intervals
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

// Pulse Mode: pulses flashlight with varying intervals
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
            elapsed += 1200 // The total time for one pulse cycle
        }
    }
}

// SOS Mode: emits SOS pattern in Morse code
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

// Strobe Mode: fast blinking at specified intervals
fun runStrobeMode(
    cameraManager: CameraManager, cameraId: String, strobeInterval: Long, totalDuration: Long
) {
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
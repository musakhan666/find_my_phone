package com.gammaplay.findmyphone.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gammaplay.findmyphone.presentation.main.Graph
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.utils.VolumeButtonsHandler
import com.gammaplay.findmyphone.utils.drawBigCircleShadow
import com.gammaplay.findmyphone.utils.drawTopSectionShadow
import com.gammaplay.findmyphone.utils.setStatusBarColor
import com.gammaplay.findmyphone.utils.setStatusBarIconsColor
import com.gammaplay.findmyphone.utils.shadowEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(openAndPopUp: (String) -> Unit, homeViewModel: HomeViewModel) {
    val context = LocalContext.current

    // Register the BroadcastReceiver
    val alarmDetectionReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("HomeScreen", "Alarm detected!")
                homeViewModel.setAlarmActivated(true)

                // Handle the detection event (e.g., show a notification or update the UI)
            }
        }
    }


    DisposableEffect(Unit) {
        val filter = IntentFilter("com.gammaplay.findmyphone.ALARM_DETECTION")
        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.RECEIVER_NOT_EXPORTED
        } else {
            0 // Default flag for older versions
        }

        // Registering the receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(alarmDetectionReceiver, filter, receiverFlags)
        } else {
            context.registerReceiver(alarmDetectionReceiver, filter)
        }


        onDispose {
            context.unregisterReceiver(alarmDetectionReceiver)
        }
    }

    val isAlarmActivated by homeViewModel.isAlarmActivated.observeAsState(initial = false)


    if (isAlarmActivated) VolumeButtonsHandler(isActivated = true)
    else VolumeButtonsHandler(isActivated = false)

    val activeCardIndex = homeViewModel.activeCardIndex
    val activationActiveCardIndex by homeViewModel.activationActiveCardIndex.observeAsState(0)
    val isActivated by homeViewModel.isActivated.observeAsState(initial = false)


    val isFlashActivated by homeViewModel.isActivatedFlash.observeAsState(initial = true)
    val isVibrationActivated by homeViewModel.isActivatedVibration.observeAsState(initial = true)

    val sound = homeViewModel.sound
    val mediaPlayer = MediaPlayer.create(context, sound[homeViewModel.activeCardIndex.value])
        .apply { isLooping = true }

    val soundIcon = homeViewModel.soundIcons
    val soundContentDescription = homeViewModel.soundContentDescriptions
    val activationIcon = homeViewModel.activationIconRes
    val activationContentDescription = homeViewModel.activationContentDescriptions


    setStatusBarColor(color = Color.Transparent)
    setStatusBarIconsColor(true)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_main_screen))
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
            .drawBehind { drawTopSectionShadow() }
            .clip(shape = RoundedCornerShape(0.dp, 0.dp, 84.dp, 84.dp))
            .background(Color.White),
            contentAlignment = Alignment.Center) {
            if (isActivated) {
                Image(
                    painter = painterResource(id = R.drawable.circles),
                    contentDescription = "",
                    modifier = Modifier.scale(1.8f)
                )
            }
            //Big circle button
            Box(modifier = Modifier
                .size(200.dp)
                .drawBehind { drawBigCircleShadow(isActivated) }
                .clip(shape = CircleShape)
                .background(if (isActivated) colorResource(id = R.color.big_btn_active) else colorResource(id = R.color.bg_main_screen))
                .clickable { homeViewModel.toggleActivation(context) }, contentAlignment = Alignment.Center

            ) {
                Text(
                    text = if (isActivated) "Activate" else "Deactivate",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActivated) Color.White else colorResource(id = R.color.title_text).copy(alpha = 0.3f)
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorResource(id = R.color.title_text),
                ), title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                }, actions = {
                    IconButton(onClick = { openAndPopUp.invoke(Graph.TUTORIAL) }) {
                        Icon(
                            imageVector = Icons.Outlined.QuestionMark,
                            contentDescription = "How to use",
                            tint = colorResource(id = R.color.title_text)
                        )
                    }
                    IconButton(onClick = { openAndPopUp.invoke(Graph.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = colorResource(id = R.color.title_text)
                        )
                    }
                })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp, 0.dp, 14.dp, 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChipOption(true, isVibrationActivated, homeViewModel)
                    FilterChipOption(false, isFlashActivated, homeViewModel)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(14.dp, 32.dp, 14.dp, 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MenuItem(
                    title = "Sound",
                    soundIcon = homeViewModel.soundIcons,
                    soundContentDescription = soundContentDescription,
                    activationIcon = activationIcon,
                    activationContentDescription = activationContentDescription,
                    activeCardIndex = activeCardIndex,
                    activationActiveCardIndex = activationActiveCardIndex,
                    mediaPlayer = mediaPlayer,
                    homeViewModel = homeViewModel
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MenuItem(
                    title = "Activation",
                    soundIcon = soundIcon,
                    soundContentDescription = soundContentDescription,
                    activationIcon = activationIcon,
                    activationContentDescription = activationContentDescription,
                    activeCardIndex = activeCardIndex,
                    activationActiveCardIndex = activationActiveCardIndex,
                    mediaPlayer = mediaPlayer,
                    homeViewModel
                )
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItem(
    title: String,
    soundIcon: List<Int>,
    soundContentDescription: List<String>,
    activationIcon: List<Int>,
    activationContentDescription: List<String>,
    activeCardIndex: State<Int>,
    activationActiveCardIndex: Int,
    mediaPlayer: MediaPlayer,
    homeViewModel: HomeViewModel
) {
    val soundSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSoundSheetOpen by rememberSaveable { mutableStateOf(false) }
    val activationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isActivationSheetOpen by rememberSaveable { mutableStateOf(false) }
    val cornerRadius = 14.dp
    val isSoundPlaying = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(2.dp)
            .shadowEffect(cornerRadius)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .clickable {
                when (title) {
                    "Sound" -> isSoundSheetOpen = true
                    "Activation" -> isActivationSheetOpen = true
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconResId =
                if (title == "Sound") soundIcon[activeCardIndex.value] else activationIcon[activationActiveCardIndex]
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = title,
                color = colorResource(id = R.color.title_text),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (isSoundSheetOpen) {
        SoundSelectionBottomSheet(
            soundSheetState = soundSheetState,
            isSoundPlaying = isSoundPlaying,
            mediaPlayer = mediaPlayer,
            context = context,
            soundIcon = soundIcon,
            soundContentDescription = soundContentDescription,
            activeCardIndex = activeCardIndex,
            homeViewModel = homeViewModel,
            onDismiss = {
                isSoundSheetOpen = false
                isSoundPlaying.value = false
            }
        )
    }

    if (isActivationSheetOpen) {
        ActivationTypeSelectionBottomSheet(
            activationSheetState = activationSheetState,
            activationIcon = activationIcon,
            activationContentDescription = activationContentDescription,
            activationActiveCardIndex = activationActiveCardIndex,
            homeViewModel = homeViewModel,
            onDismiss = { isActivationSheetOpen = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSelectionBottomSheet(
    soundSheetState: SheetState,
    isSoundPlaying: MutableState<Boolean>,
    mediaPlayer: MediaPlayer,
    context: Context,
    soundIcon: List<Int>,
    soundContentDescription: List<String>,
    activeCardIndex: State<Int>,
    homeViewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var systemVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()) }

    ModalBottomSheet(
        sheetState = soundSheetState,
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background)
    ) {
        Text(
            text = "Choose sound",
            modifier = Modifier.padding(14.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.title_text)
        )

        VolumeControl(
            audioManager = audioManager,
            systemVolume = systemVolume,
            onVolumeChange = { newVolume -> systemVolume = newVolume }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(10.dp),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(12) { item ->
                SoundCard(
                    soundIcon = soundIcon[item],
                    description = soundContentDescription[item],
                    isSelected = activeCardIndex.value == item,
                    isPlaying = isSoundPlaying.value, // Use the value of isSoundPlaying
                    onClick = {
                        if (activeCardIndex.value == item) {
                            if (isSoundPlaying.value) {
                                mediaPlayer.pause()
                            } else {
                                mediaPlayer.start()
                            }
                            isSoundPlaying.value = !isSoundPlaying.value // Toggle playing state
                        } else {
                            isSoundPlaying.value = false
                            mediaPlayer.pause()
                            homeViewModel.setActiveCardIndex(item)
                        }
                    }
                )
            }
        }

        Button(
            onClick = {
                isSoundPlaying.value = false
                mediaPlayer.pause()
                homeViewModel.setSound()
                onDismiss()
            },
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.accent),
                contentColor = Color.White
            )
        ) {
            Text(text = "Okay")
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationTypeSelectionBottomSheet(
    activationSheetState: SheetState,
    activationIcon: List<Int>,
    activationContentDescription: List<String>,
    activationActiveCardIndex: Int,
    homeViewModel: HomeViewModel,
    onDismiss: () -> Unit
) {

    ModalBottomSheet(
        sheetState = activationSheetState,
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background)
    ) {
        Text(
            text = "Choose activation type",
            modifier = Modifier.padding(14.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.title_text)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(10.dp),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(3) { item ->
                ActivationTypeCard(
                    activationIcon = activationIcon[item],
                    description = activationContentDescription[item],
                    isSelected = activationActiveCardIndex == item,
                    onClick = { homeViewModel.setActivationActiveCardIndex(item) }
                )
            }
        }

        TextFieldWithButton(
            activationActiveCardIndex = activationActiveCardIndex,
            homeViewModel = homeViewModel,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun VolumeControl(
    audioManager: AudioManager,
    systemVolume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Outlined.VolumeUp,
            contentDescription = "Volume",
            tint = colorResource(id = R.color.title_text)
        )
        Slider(
            value = systemVolume,
            onValueChange = { newVolume ->
                onVolumeChange(newVolume)
                val newSystemVolume =
                    (newVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newSystemVolume, 0)
            },
            modifier = Modifier.size(width = 230.dp, height = 32.dp),
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.accent),
                activeTrackColor = colorResource(id = R.color.title_text),
                inactiveTrackColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f)
            )
        )
        Text(
            text = (1 + (systemVolume * 99)).toInt().toString(),
            color = colorResource(id = R.color.title_text),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SoundCard(
    soundIcon: Int,
    description: String,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.White
    val elevation = if (isSelected) 8.dp else 4.dp

    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() }, // Make card clickable
        elevation = CardDefaults.cardElevation(elevation), // Elevation changes based on selection
        shape = RoundedCornerShape(8.dp), // Optional: Add rounded corners
        colors = CardDefaults.cardColors(containerColor = backgroundColor) // Set the background color
    ) {
        Box(
            modifier = Modifier
                .size(84.dp),
            contentAlignment = Alignment.Center
        ) {
            // Display sound icon
            Image(
                painter = painterResource(id = soundIcon),
                contentDescription = description
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play sound"
                    )
                }
            }
        }
    }
}


@Composable
fun ActivationTypeCard(
    activationIcon: Int,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.White
    val elevation = if (isSelected) 8.dp else 4.dp

    Card(
        modifier = Modifier
            .size(100.dp) // Adjust size as needed
            .clickable { onClick() },
        colors = CardDefaults.cardColors(backgroundColor),
        elevation = CardDefaults.cardElevation(elevation), // Elevation based on selection
        shape = RoundedCornerShape(8.dp) // Rounded corners
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp), // Padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = activationIcon),
                contentDescription = description,
                modifier = Modifier.size(50.dp) // Adjust image size as needed
            )
            Text(
                text = description,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.title_text)
            )
        }
    }
}


@Composable
fun TextFieldWithButton(
    activationActiveCardIndex: Int,
    homeViewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    val textFieldValue by remember { homeViewModel.textFieldValue }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { homeViewModel.updateTextFieldValue(it) },
        enabled = activationActiveCardIndex != 0,
        label = {
            Text(
                text = "Type activation word or phrase",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (activationActiveCardIndex == 0) colorResource(id = R.color.title_text).copy(
                    0.2f
                ) else colorResource(id = R.color.title_text)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(id = R.color.accent),
            unfocusedBorderColor = colorResource(id = R.color.title_text),
            disabledBorderColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f)
        )
    )

    Button(
        onClick = {
            homeViewModel.submit(textFieldValue)
            onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        enabled = !(activationActiveCardIndex != 0 && textFieldValue.isEmpty()),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.accent),
            contentColor = Color.White,
            disabledContainerColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f),
            disabledContentColor = Color.White
        )
    ) {
        Text(text = "Okay")
    }

    Spacer(modifier = Modifier.height(48.dp))
}


@Composable
fun FilterChipOption(isVibration: Boolean, selected: Boolean, homeViewModel: HomeViewModel) {
    val text = if (isVibration) "Vibration" else "Flashlight"
    FilterChip(onClick = { if (isVibration) homeViewModel.toggleActivatedVibration() else homeViewModel.toggleFlashActivation() },
        label = {
            Text(text)
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            labelColor = colorResource(id = R.color.title_text),
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            selectedContainerColor = colorResource(id = R.color.big_btn_active)

        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colorResource(id = R.color.title_text),
            enabled = true,
            selected = selected
        ))
}

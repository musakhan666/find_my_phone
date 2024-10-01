package com.gammaplay.findmyphone.ui.home

import android.graphics.Paint
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gammaplay.findmyphone.ui.main.Graph
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.utils.VolumeButtonsHandler
import com.gammaplay.findmyphone.utils.drawBigCircleShadow
import com.gammaplay.findmyphone.utils.drawTopSectionShadow
import com.gammaplay.findmyphone.utils.setStatusBarColor
import com.gammaplay.findmyphone.utils.setStatusBarIconsColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, homeViewModel: HomeViewModel) {
    val activeCardIndex = homeViewModel.activeCardIndex
    val activationActiveCardIndex by homeViewModel.activationActiveCardIndex.observeAsState(0)
    val isActivated by homeViewModel.isActivated.observeAsState(initial = false)


    val isFlashActivated by homeViewModel.isActivatedFlash.observeAsState(initial = true)
    val isVibrationActivated by homeViewModel.isActivatedVibration.observeAsState(initial = true)

    val sound = homeViewModel.sound
    val context = LocalContext.current
    val mediaPlayer = MediaPlayer.create(context, sound[homeViewModel.activeCardIndex.value])
        .apply { isLooping = true }

    val soundIcon = homeViewModel.soundIcons
    val soundContentDescription = homeViewModel.soundContentDescriptions
    val activationIcon = homeViewModel.activationIconRes

    val activationContentDescription = homeViewModel.activationContentDescriptions
    Log.d("Key events", "$isActivated isActivated")

    if (isActivated) VolumeButtonsHandler(isActivated = false)
    else
        VolumeButtonsHandler(isActivated = true)

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
            .drawBehind {
                drawTopSectionShadow()
            }
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
                .drawBehind {
                    drawBigCircleShadow(isActivated)

                }
                .clip(shape = CircleShape)
                .background(
                    if (isActivated) colorResource(id = R.color.big_btn_active) else colorResource(
                        id = R.color.bg_main_screen
                    )
                )
                .clickable {

                    homeViewModel.toggleActivation(context)

                }, contentAlignment = Alignment.Center

            ) {
                Text(
                    text = if (isActivated) "Activate" else "Deactivate",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActivated) Color.White else colorResource(id = R.color.title_text).copy(
                        alpha = 0.3f
                    )
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
                    IconButton(onClick = { navController.navigate(Graph.TUTORIAL) }) {
                        Icon(
                            imageVector = Icons.Outlined.QuestionMark,
                            contentDescription = "How to use",
                            tint = colorResource(id = R.color.title_text)
                        )
                    }
                    IconButton(onClick = { navController.navigate(Graph.SETTINGS) }) {
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
    var isSoundPlaying by remember { mutableStateOf(false) }




    Box(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .padding(2.dp, 0.dp, 2.dp, 0.dp)
        .drawBehind {
            val size = this.size
            drawContext.canvas.nativeCanvas.apply {
                drawRoundRect(0f,
                    0f,
                    size.width,
                    size.height,
                    cornerRadius.toPx(),
                    cornerRadius.toPx(),
                    Paint().apply {
                        color = Color.Transparent.toArgb()
                        setShadowLayer(
                            4.dp.toPx(),
                            0.dp.toPx(),
                            2.dp.toPx(),
                            Color.Blue
                                .copy(
                                    alpha = 0.25f, red = 0.35f, green = 0.44f, blue = 0.91f
                                )
                                .toArgb()
                        )
                    })
            }
        }
        .clip(shape = RoundedCornerShape(cornerRadius))
        .background(Color.White)
        .clickable {
            when (title) {
                "Sound" -> isSoundSheetOpen = true
                "Activation" -> isActivationSheetOpen = true
            }
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp, 4.dp, 32.dp, 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = if (title == "Sound") painterResource(id = soundIcon[activeCardIndex.value]) else painterResource(
                    id = activationIcon[activationActiveCardIndex]
                ), contentDescription = title, modifier = Modifier.size(64.dp)
            )
            Text(
                text = title,
                color = colorResource(id = R.color.title_text),
                fontWeight = FontWeight.SemiBold
            )


        }
    }

    //================================SOUND SELECTION BOTTOM SHEET===============================
    if (isSoundSheetOpen) {
        ModalBottomSheet(
            sheetState = soundSheetState, onDismissRequest = {
                isSoundPlaying = false
                mediaPlayer.pause()
                isSoundSheetOpen = false

            }, containerColor = colorResource(id = R.color.background)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Choose sound",
                    modifier = Modifier.padding(14.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.title_text)
                )
            }

            var sliderPosition by remember { mutableFloatStateOf(0.7f) }

            Row(
                modifier = Modifier
                    .padding(32.dp, 8.dp, 32.dp, 8.dp)
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
                    modifier = Modifier
                        .padding(14.dp, 0.dp, 14.dp, 0.dp)
                        .size(width = 230.dp, height = 32.dp),
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    colors = SliderDefaults.colors(
                        thumbColor = colorResource(id = R.color.accent),
                        activeTrackColor = colorResource(id = R.color.title_text),
                        inactiveTrackColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f)
                    )
                )
                Text(
                    text = (1 + (sliderPosition * 99)).toInt().toString(),
                    color = colorResource(id = R.color.title_text),
                    fontWeight = FontWeight.Bold
                )
            }

            LazyVerticalGrid(
                modifier = Modifier.padding(10.dp, 14.dp, 10.dp, 14.dp),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(12) { item ->
                    Box(modifier = Modifier
                        .size(width = 84.dp, height = 84.dp)
                        .drawBehind {
                            val size = this.size
                            drawContext.canvas.nativeCanvas.apply {
                                drawRoundRect(0f,
                                    0f,
                                    size.width,
                                    size.height,
                                    cornerRadius.toPx(),
                                    cornerRadius.toPx(),
                                    Paint().apply {
                                        color = Color.Transparent.toArgb()
                                        setShadowLayer(
                                            4.dp.toPx(),
                                            0.dp.toPx(),
                                            2.dp.toPx(),
                                            Color.Blue
                                                .copy(
                                                    alpha = 0.25f,
                                                    red = 0.35f,
                                                    green = 0.44f,
                                                    blue = 0.91f
                                                )
                                                .toArgb()
                                        )
                                    })
                            }
                        }
                        .clip(shape = RoundedCornerShape(cornerRadius))
                        .background(Color.White)
                        .border(
                            width = if (activeCardIndex.value == item) 3.dp else -1.dp,
                            color = colorResource(id = R.color.accent),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                        .clickable {
                            if (activeCardIndex.value == item) {
                                //change icon to pause
                                if (isSoundPlaying) {
                                    mediaPlayer.pause()
                                } else {
                                    mediaPlayer.start()
                                }
                                isSoundPlaying = !isSoundPlaying

                            } else {
                                isSoundPlaying = false
                                mediaPlayer.pause()
                                homeViewModel.setActiveCardIndex(item)
                            }
                        }, contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = soundIcon[item]),
                            contentDescription = soundContentDescription[item],
                        )
                        if (activeCardIndex.value == item) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(shape = CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .border(
                                        3.dp,
                                        colorResource(id = R.color.title_text).copy(alpha = 0.5f),
                                        CircleShape
                                    ), contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = if (isSoundPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play sound",
                                    tint = colorResource(id = R.color.title_text)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isSoundPlaying = false
                    mediaPlayer.pause()
                    isSoundSheetOpen = false
                    homeViewModel.setSound()
                },
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.accent),
                    contentColor = Color.White,
                    disabledContainerColor = colorResource(id = R.color.accent),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Okay")
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
    //================================ACTIVATION TYPE SELECTION BOTTOM SHEET===============================
    if (isActivationSheetOpen) {
        ModalBottomSheet(
            sheetState = activationSheetState,
            onDismissRequest = { isActivationSheetOpen = false },
            containerColor = colorResource(id = R.color.background)
        ) {
            val textFieldValue by remember { homeViewModel.textFieldValue }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Choose activation type",
                        modifier = Modifier.padding(14.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.title_text)
                    )
                }
                LazyVerticalGrid(
                    modifier = Modifier.padding(10.dp, 14.dp, 10.dp, 14.dp),
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(3) { item ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    val size = this.size
                                    drawContext.canvas.nativeCanvas.apply {
                                        drawRoundRect(0f,
                                            0f,
                                            size.width,
                                            size.height,
                                            cornerRadius.toPx(),
                                            cornerRadius.toPx(),
                                            Paint().apply {
                                                color = Color.Transparent.toArgb()
                                                setShadowLayer(
                                                    4.dp.toPx(),
                                                    0.dp.toPx(),
                                                    2.dp.toPx(),
                                                    Color.Blue
                                                        .copy(
                                                            alpha = 0.25f,
                                                            red = 0.35f,
                                                            green = 0.44f,
                                                            blue = 0.91f
                                                        )
                                                        .toArgb()
                                                )
                                            })
                                    }
                                }
                                .clip(shape = RoundedCornerShape(cornerRadius))
                                .background(Color.White)
                                .border(
                                    width = if (activationActiveCardIndex == item) 3.dp else -1.dp,
                                    color = colorResource(id = R.color.accent),
                                    shape = RoundedCornerShape(cornerRadius)
                                )
                                .clickable {
                                    homeViewModel.setActivationActiveCardIndex(item)
                                }, contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = activationIcon[item]),
                                    contentDescription = activationContentDescription[item],
                                )
                            }
                            Text(
                                text = activationContentDescription[item],
                                modifier = Modifier.padding(14.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.title_text)
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { homeViewModel.updateTextFieldValue(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp, 0.dp, 14.dp, 48.dp),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.accent),
                            unfocusedBorderColor = colorResource(id = R.color.title_text),
                            disabledBorderColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f),

                            focusedTextColor = colorResource(id = R.color.title_text),
                            unfocusedTextColor = colorResource(id = R.color.title_text),
                            disabledTextColor = colorResource(id = R.color.title_text).copy(alpha = 0.2f),
                        )
                    )
                }

                Button(
                    onClick = {
                        isActivationSheetOpen = false
                        homeViewModel.submit(textFieldValue)
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    enabled = !(activationActiveCardIndex != 0 && textFieldValue == ""),
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

        }
    }

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

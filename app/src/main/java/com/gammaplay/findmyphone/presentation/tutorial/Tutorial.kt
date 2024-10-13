package com.gammaplay.findmyphone.presentation.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.presentation.main.Graph
import com.gammaplay.findmyphone.ui.theme.AppTypography
import com.gammaplay.findmyphone.ui.theme.CustomFontFamily
import com.gammaplay.findmyphone.utils.AppStatusManager

@Composable
fun TutorialScreen(openAndPopUp: (String) -> Unit) {
    val activeDotIndex = remember { mutableStateOf(1) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_main_screen)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Box
        TutorialTitleBox()

        // Content Box with images
        TutorialContentBox(activeDotIndex.value)

        // Text Instructions
        TutorialTextInstructions(activeDotIndex.value)

        // Dot Indicators
        TutorialDotIndicators(activeDotIndex.value) { index -> activeDotIndex.value = index }

        // Navigation Buttons
        TutorialNavigationButtons(activeDotIndex, {
            openAndPopUp.invoke(Graph.HOME)
            val settings = AppStatusManager(context = context)
            settings.setTutorialShown()
        }) {
            activeDotIndex.value--
        }
    }
}

@Composable
fun TutorialTitleBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.how_it_work),
            modifier = Modifier.padding(38.dp),
            style = AppTypography.titleLarge,
            color = colorResource(id = R.color.title_text)
        )
    }
}

@Composable
fun TutorialContentBox(activeDotIndex: Int) {
    Box(
        modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,  // Set shadow elevation
                shape = RoundedCornerShape(84.dp), // Apply the rounded corner shadow
                clip = false // Shadow outside the bounds
            )
            .background(
                color = Color.White, // Apply background color
                shape = RoundedCornerShape(84.dp) // Rounded corners for the background
            )
            .clip(RoundedCornerShape(84.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = when (activeDotIndex) {
                1 -> painterResource(id = R.drawable.how_it_works)
                2 -> painterResource(id = R.drawable.select_sound)
                3 -> painterResource(id = R.drawable.activation_type)
                4 -> painterResource(id = R.drawable.vibration_flashlight)
                else -> painterResource(id = R.drawable.how_it_works)
            }, contentDescription = null
        )
    }

}

@Composable
fun TutorialTextInstructions(activeDotIndex: Int) {
    val text = when (activeDotIndex) {
        1 -> stringResource(id = R.string.text_how_it_works)
        2 -> stringResource(id = R.string.text_sound_selection)
        3 -> stringResource(id = R.string.text_activation_type)
        else -> stringResource(id = R.string.text_additional_options)
    }

    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(38.dp),
        style = AppTypography.bodyLarge
    )
}

@Composable
fun TutorialDotIndicators(activeDotIndex: Int, onDotClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..4) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                modifier = Modifier
                    .padding(12.dp)
                    .size(if (activeDotIndex == i) 16.dp else 10.dp)
                    .clickable { onDotClick(i) },
                tint = if (activeDotIndex == i) colorResource(id = R.color.accent) else colorResource(
                    id = R.color.title_text
                ).copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun TutorialNavigationButtons(
    activeDotIndex: MutableState<Int>,
    navController: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (activeDotIndex.value > 1) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    modifier = Modifier.clickable { onBackClick() },
                    tint = colorResource(id = R.color.title_text).copy(alpha = 0.4f)
                )
            }
        }
        Box(modifier = Modifier.weight(2f)) {
            Button(
                onClick = {
                    if (activeDotIndex.value == 4) {
                        navController.invoke()
                    } else activeDotIndex.value++
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.accent), contentColor = Color.White
                )
            ) {
                Text(
                    text = if (activeDotIndex.value == 4) stringResource(id = R.string.got_it) else stringResource(
                        id = R.string.next
                    ), style = AppTypography.bodyLarge
                )
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.skip),
                modifier = Modifier.clickable(onClick = navController),
                color = colorResource(id = R.color.title_text).copy(alpha = 0.4f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = CustomFontFamily
            )
        }
    }
    Spacer(modifier = Modifier.height(64.dp))
}



package com.gammaplay.findmyphone.ui.tutorial

import android.content.Context
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gammaplay.findmyphone.ui.main.Graph
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.utils.AppStatusManager

//@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavHostController) {
    val activeDotIndex = remember { mutableStateOf(1) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_main_screen)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.how_it_work),
                modifier = Modifier.padding(38.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.title_text)
            )
        }
        Box(modifier = Modifier
            .padding(14.dp, 0.dp, 14.dp, 0.dp)
            .fillMaxWidth()
            .weight(1f)
            .drawBehind {
                val size = this.size
                drawContext.canvas.nativeCanvas.apply {
                    drawRoundRect(
                        0f,
                        0f,
                        size.width,
                        size.height,
                        84.dp.toPx(),
                        84.dp.toPx(),
                        Paint().apply {
                            color = Color.Transparent.toArgb()
                            setShadowLayer(
                                16.dp.toPx(),
                                0.dp.toPx(),
                                16.dp.toPx(),
                                Color.Blue
                                    .copy(
                                        alpha = 0.25f, red = 0.35f, green = 0.44f, blue = 0.91f
                                    )
                                    .toArgb()
                            )
                        })
                }
            }
            .clip(shape = RoundedCornerShape(84.dp))
            .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = when (activeDotIndex.value) {
                    1 -> painterResource(id = R.drawable.how_it_works)
                    2 -> painterResource(id = R.drawable.select_sound)
                    3 -> painterResource(id = R.drawable.activation_type)
                    4 -> painterResource(id = R.drawable.vibration_flashlight)
                    else -> painterResource(id = R.drawable.how_it_works)
                },
                contentDescription = "",
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val textStyleNormal = SpanStyle(
                color = colorResource(id = R.color.title_text).copy(alpha = 0.5f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            val textStyleBold = SpanStyle(
                colorResource(id = R.color.title_text),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            val textHowItWorks = AnnotatedString.Builder().apply {
                withStyle(textStyleNormal) {
                    append("Can't find your ")
                }
                withStyle(textStyleBold) {
                    append("phone? ")
                }
                withStyle(textStyleNormal) {
                    append("Not a problem! Just ")
                }
                withStyle(textStyleBold) {
                    append("clap ")
                }
                withStyle(textStyleNormal) {
                    append("your hands and your phone will immediatly ")
                }
                withStyle(textStyleBold) {
                    append("respond!")
                }
            }.toAnnotatedString()
            val textSoundSelection = AnnotatedString.Builder().apply {
                withStyle(textStyleNormal) {
                    append("Select a ")
                }
                withStyle(textStyleBold) {
                    append("sound ")
                }
                withStyle(textStyleNormal) {
                    append("that the phone will play and its ")
                }
                withStyle(textStyleBold) {
                    append("volume ")
                }
            }.toAnnotatedString()
            val textActivationType = AnnotatedString.Builder().apply {
                withStyle(textStyleNormal) {
                    append("You can set phone reaction to the ")
                }
                withStyle(textStyleBold) {
                    append("claps, voice ")
                }
                withStyle(textStyleNormal) {
                    append("or ")
                }
                withStyle(textStyleBold) {
                    append("both ")
                }
            }.toAnnotatedString()
            val textAdditionalOptions = AnnotatedString.Builder().apply {
                withStyle(textStyleNormal) {
                    append("With these options enabled, the phone will ")
                }
                withStyle(textStyleBold) {
                    append("vibrate ")
                }
                withStyle(textStyleNormal) {
                    append("and blink with a ")
                }
                withStyle(textStyleBold) {
                    append("flashlight ")
                }
            }.toAnnotatedString()

            Text(
                text = if (activeDotIndex.value == 1) textHowItWorks
                else if (activeDotIndex.value == 2) textSoundSelection
                else if (activeDotIndex.value == 3) textActivationType
                else textAdditionalOptions,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(38.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                Icon(
                    imageVector = if (activeDotIndex.value == i) Icons.Filled.Circle else Icons.Filled.Circle,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(12.dp, 0.dp, 12.dp, 16.dp)
                        .size(if (activeDotIndex.value == i) 16.dp else 10.dp)
                        .clickable { activeDotIndex.value = i },
                    tint = if (activeDotIndex.value == i) colorResource(id = R.color.accent) else colorResource(
                        id = R.color.title_text
                    ).copy(alpha = 0.2f)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (activeDotIndex.value > 1) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { activeDotIndex.value-- },
                        tint = colorResource(id = R.color.title_text).copy(alpha = 0.4f),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(2f)
            ) {
                Button(
                    onClick = {
                        if (activeDotIndex.value == 4) {
                            navigateHome(navController, context = context)
                        } else activeDotIndex.value++
                    },
                    Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.accent),
                        contentColor = Color.White,
                        disabledContainerColor = colorResource(id = R.color.accent),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (activeDotIndex.value == 4) stringResource(id = R.string.got_it) else stringResource(
                            id = R.string.next
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.skip),
                    modifier = Modifier.clickable { navigateHome(navController, context = context) },
                    color = colorResource(id = R.color.title_text).copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.ExtraBold
                )
            }


        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

fun navigateHome(navController: NavHostController, context: Context) {
    navController.navigate(Graph.HOME)
    val settings = AppStatusManager(context = context)
    settings.setTutorialShown()
}

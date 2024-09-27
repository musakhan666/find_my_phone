package com.gammaplay.findmyphone.ui.settings

import android.graphics.Paint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.ui.main.Graph

data class SettingsItem(
    val title: String,
    val icon: ImageVector
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val languageActiveCardIndex = remember { mutableStateOf(0) }
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLanguageSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }
    val cornerRadius = 14.dp
    val items = listOf(
        SettingsItem(
            title = "Language",
            icon = Icons.Filled.Language,
        ),
        SettingsItem(
            title = "Rate us",
            icon = Icons.Filled.StarRate,
        ),
        SettingsItem(
            title = "Share app",
            icon = Icons.Filled.Share,
        )
    )
    val languageIcon = listOf(
        painterResource(id = R.drawable.english),
        painterResource(id = R.drawable.spain),
        painterResource(id = R.drawable.french),
        painterResource(id = R.drawable.german),
        painterResource(id = R.drawable.italian),
        painterResource(id = R.drawable.polish),
        painterResource(id = R.drawable.russian),
        painterResource(id = R.drawable.sweden),
        painterResource(id = R.drawable.czech)
    )
    val languageContentDescription = listOf(
        "english",
        "spain",
        "french",
        "german",
        "italian",
        "polish",
        "russian",
        "sweden",
        "czech"
    )
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = colorResource(id = R.color.title_text),
            ),
            title = {
                Text(
                    text = stringResource(id = R.string.settings),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate(Graph.HOME) }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.title_text)
                    )
                }
            }
        )
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 2.dp, 14.dp, 2.dp)
                    .drawBehind {
                        val size = this.size
                        drawContext.canvas.nativeCanvas.apply {
                            drawRoundRect(0f,
                                0f,
                                size.width,
                                size.height,
                                cornerRadius.toPx(),
                                cornerRadius.toPx(),
                                Paint()
                                    .apply {
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
                    .clickable {
                        when (item.title) {
                            "Language" -> isLanguageSheetOpen = true
                            "Rate us" -> showRateUsDialog = true
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp, 14.dp, 28.dp, 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = colorResource(id = R.color.title_text),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = colorResource(id = R.color.title_text)
                    )
                }
                //================================LANGUAGE SELECTION BOTTOM SHEET===============================
                if (isLanguageSheetOpen) {
                    ModalBottomSheet(
                        sheetState = languageSheetState,
                        onDismissRequest = { isLanguageSheetOpen = false },
                        containerColor = colorResource(id = R.color.background)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Choose language",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.title_text)
                            )
                        }
                        LazyVerticalGrid(
                            modifier = Modifier
                                .padding(10.dp, 14.dp, 10.dp, 14.dp),
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(9) { item ->
                                Box(
                                    modifier = Modifier
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
                                                    Paint()
                                                        .apply {
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
                                            width = if (languageActiveCardIndex.value == item) 3.dp else -1.dp,
                                            color = colorResource(id = R.color.accent),
                                            shape = RoundedCornerShape(cornerRadius)
                                        )
                                        .clickable {
                                            languageActiveCardIndex.value = item
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = languageIcon[item],
                                        contentDescription = languageContentDescription[item],
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { isLanguageSheetOpen = false },
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.accent),
                                contentColor = Color.White,
                                disabledContainerColor = colorResource(id = R.color.accent),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text(text = "Ok!")
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
                // RATE US DIALOG
                if (showRateUsDialog) {
                    AlertDialog(
                        containerColor = Color.White,
                        onDismissRequest = { showRateUsDialog = false },
                        title = {
                            Text(
                                text = "Let us know how we're doing?",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.title_text)
                            )
                                },
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                                ) {
                                val activeStarIndex = remember { mutableStateOf(5) }
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if( activeStarIndex.value < i) Icons.Outlined.StarBorder else Icons.Filled.Star,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(48.dp, 48.dp)
                                            .clickable { activeStarIndex.value = i },
                                        tint = if( activeStarIndex.value < i) colorResource(id = R.color.title_text) else colorResource(id = R.color.accent)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRateUsDialog = false
                                }
                            ) {
                                Text(
                                    text = "Rate us",
                                    color = colorResource(id = R.color.accent)
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showRateUsDialog = false
                                }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = colorResource(id = R.color.title_text)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.data.SettingsGeneralItem
import com.gammaplay.findmyphone.data.SettingsItem
import com.gammaplay.findmyphone.presentation.bottomsheets.DurationBottomSheetContent
import com.gammaplay.findmyphone.presentation.bottomsheets.SensitivityBottomSheetContent
import com.gammaplay.findmyphone.presentation.bottomsheets.VibrationModesBottomSheetContent
import com.gammaplay.findmyphone.presentation.settings.SettingsViewModel
import com.gammaplay.findmyphone.ui.theme.CustomFontFamily
import com.gammaplay.findmyphone.utils.shadowEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit, settingsViewModel: SettingsViewModel) {
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLanguageSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }
    val languageActiveCardIndex by settingsViewModel.languageActiveCardIndex.collectAsState()
    val cornerRadius = 14.dp

    val otherItems = settingsViewModel.OtherItems
    val generalItems = settingsViewModel.generalItems
    val languageIcons = settingsViewModel.languageIconIds
    val languageContentDescriptions = settingsViewModel.languageContentDescriptionIds

    var showFlashlightSheet by remember { mutableStateOf(false) }
    var showVibrationSheet by remember { mutableStateOf(false) }
    var showDurationSheet by remember { mutableStateOf(false) }
    var showSensitivitySheet by remember { mutableStateOf(false) }

    LaunchedEffect("fetchAllValues") {
        settingsViewModel.fetchValues()
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = colorResource(id = R.color.title_text),
            ),
            title = {
                Text(
                    text = stringResource(id = R.string.settings), fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                        tint = colorResource(id = R.color.title_text)
                    )
                }
            }
        )

        Text(
            text = stringResource(id = R.string.general),
            fontFamily = CustomFontFamily,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp)
        )

        generalItems.forEachIndexed { _, item ->
            SettingsGeneralItemBox(item, cornerRadius) {
                when (item.title) {
                    R.string.flashlight -> showFlashlightSheet = true
                    R.string.vibration -> showVibrationSheet = true
                    R.string.sensitivity -> showSensitivitySheet = true
                    R.string.Duration -> showDurationSheet = true

                }
            }
        }

        Text(
            text = stringResource(id = R.string.others),
            fontFamily = CustomFontFamily,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp)
        )
        otherItems.forEachIndexed { _, item ->
            SettingsItemBox(item, cornerRadius) {
                when (item.title) {
                    R.string.language -> {
                        isLanguageSheetOpen = true
                    }

                    R.string.rate_us -> {
                        showRateUsDialog = true
                    }
                }
            }
        }

        // Language Selection Bottom Sheet
        if (isLanguageSheetOpen) {
            LanguageBottomSheet(
                sheetState = languageSheetState,
                onDismiss = { isLanguageSheetOpen = false },
                languagesIcons = languageIcons,
                contentDescriptions = languageContentDescriptions,
                viewModel = settingsViewModel,
                cornerRadius = cornerRadius,
                languageActiveCardIndex
            )
        }

        // Rate Us Dialog
        if (showRateUsDialog) {
            CustomRateUsDialog(onDismiss = { showRateUsDialog = false },
                onSubmit = { showRateUsDialog = false })
        }

        // Flashlight Mode Bottom Sheet
        // Show Flashlight Mode Bottom Sheet when tapping the Flashlight item
        if (showFlashlightSheet) {
            ModalBottomSheet(containerColor = Color.White,
                onDismissRequest = { showFlashlightSheet = false }) {
                FlashlightModesBottomSheetContent(
                    selectedMode = settingsViewModel.generalItems[0].subtitle!!,
                    flashlightModes = settingsViewModel.flashlightModes,
                    onOptionSelected = {
                        // Handle the selected flashlight mode here
                        settingsViewModel.setFlashlightMode(mode = it)
                    },
                    onDismiss = { showFlashlightSheet = false }
                )
            }
        }


        if (showVibrationSheet) {
            ModalBottomSheet(containerColor = Color.White,
                onDismissRequest = { showVibrationSheet = false }) {
                VibrationModesBottomSheetContent(
                    selectedMode = settingsViewModel.generalItems[1].subtitle!!,
                    vibrationModes = settingsViewModel.vibrationModes,
                    onOptionSelected = { selectedMode ->
                        // Handle selected mode
                        settingsViewModel.setVibrationMode(selectedMode)
                    },
                    onDismiss = {
                        showVibrationSheet = false
                    }
                )
            }

        }
        if (showDurationSheet) {
            ModalBottomSheet(containerColor = Color.White,
                onDismissRequest = { showDurationSheet = false }) {
                DurationBottomSheetContent(
                    selectedMode = settingsViewModel.generalItems[2].subtitle!!,
                    durationModes = settingsViewModel.durationModes,
                    onOptionSelected = { selectedMode ->
                        // Handle selected mode
                        settingsViewModel.setDurationMode(selectedMode)
                    },
                    onDismiss = {
                        showDurationSheet = false
                    }
                )
            }

        }
        if (showSensitivitySheet) {
            ModalBottomSheet(containerColor = Color.White,
                onDismissRequest = { showSensitivitySheet = false }) {
                SensitivityBottomSheetContent(
                    selectedMode = settingsViewModel.generalItems[3].subtitle!!,
                    sensitivityLevels = settingsViewModel.sensitivityLevels,
                    onOptionSelected = { selectedMode ->
                        // Handle selected mode
                        settingsViewModel.setSensitivityMode(selectedMode)
                    },
                    onDismiss = {
                        showSensitivitySheet = false
                    }
                )
            }

        }


    }
}

@Composable
fun SettingsItemBox(item: SettingsItem, cornerRadius: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 2.dp, 14.dp, 2.dp)
            .shadowEffect(cornerRadius)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp, 14.dp, 28.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon at the start
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(id = item.title),
                tint = colorResource(id = R.color.title_text),
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

            // Column for title and subtitle
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = item.title),
                    color = colorResource(id = R.color.title_text),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                item.subtitle?.let {
                    Text(
                        text = stringResource(id = it),
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }

            }
        }
    }
}


@Composable
fun SettingsGeneralItemBox(item: SettingsGeneralItem, cornerRadius: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 2.dp, 14.dp, 2.dp)
            .shadowEffect(cornerRadius)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp, 14.dp, 28.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon at the start
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = stringResource(id = item.title),
                tint = colorResource(id = R.color.title_text),
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

            // Column for title and subtitle
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = item.title),
                    color = colorResource(id = R.color.title_text),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                item.subtitle?.let {
                    Text(
                        text = stringResource(id = it),
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    languagesIcons: List<Int>,
    contentDescriptions: List<Int>,
    viewModel: SettingsViewModel,
    cornerRadius: Dp,
    languageActiveCardIndex: Int
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background)
    ) {
        Text(
            text = stringResource(id = R.string.choose_language),
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.title_text)
        )

        LazyVerticalGrid(
            modifier = Modifier.padding(10.dp),
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(languagesIcons.size) { index ->
                LanguageCard(
                    painter = painterResource(id = languagesIcons[index]),
                    description = stringResource(id = contentDescriptions[index]),
                    isSelected = languageActiveCardIndex == index,
                    onClick = { viewModel.setActiveCardIndex(index) },
                    cornerRadius = cornerRadius
                )
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.accent), contentColor = Color.White
            )
        ) {
            Text(text = stringResource(id = R.string.ok))
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}


@Composable
fun LanguageCard(
    painter: Painter,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cornerRadius: Dp
) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) colorResource(id = R.color.accent) else Color.Gray,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = description,
            modifier = Modifier.size(64.dp)  // Adjust size if needed
        )
    }
}




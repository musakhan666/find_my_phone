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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.gammaplay.findmyphone.data.SettingsItem
import com.gammaplay.findmyphone.utils.shadowEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val languageActiveCardIndex = remember { mutableIntStateOf(0) }
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLanguageSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }
    val cornerRadius = 14.dp
    val items = listOf(
        SettingsItem(
            title = stringResource(id = R.string.language),
            icon = Icons.Filled.Language,
        ), SettingsItem(
            title = stringResource(id = R.string.rate_us),
            icon = Icons.Filled.StarRate,
        ), SettingsItem(
            title = stringResource(id = R.string.share_app),
            icon = Icons.Filled.Share,
        )
    )
    // Precompute string resources
    val languageTitle = stringResource(id = R.string.language)
    val rateUsTitle = stringResource(id = R.string.rate_us)
    val shareAppTitle = stringResource(id = R.string.share_app)

    val languageIcons = listOf(
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
    val languageContentDescriptions = listOf(
        stringResource(id = R.string.english),
        stringResource(id = R.string.spanish),
        stringResource(id = R.string.french),
        stringResource(id = R.string.german),
        stringResource(id = R.string.italian),
        stringResource(id = R.string.polish),
        stringResource(id = R.string.russian),
        stringResource(id = R.string.swedish),
        stringResource(id = R.string.czech)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = colorResource(id = R.color.title_text),
        ), title = {
            Text(
                text = stringResource(id = R.string.settings), fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = colorResource(id = R.color.title_text)
                )
            }
        })
        items.forEachIndexed { _, item ->
            SettingsItemBox(item, cornerRadius) {
                when (item.title) {
                    languageTitle -> {
                        isLanguageSheetOpen = true
                    }

                    rateUsTitle -> {
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
                activeCardIndex = languageActiveCardIndex,
                cornerRadius = cornerRadius
            )
        }

        // Rate Us Dialog
        if (showRateUsDialog) {
            RateUsDialog(onDismiss = { showRateUsDialog = false },
                onConfirm = { showRateUsDialog = false })
        }
    }
}

@Composable
fun SettingsItemBox(item: SettingsItem, cornerRadius: Dp, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp, 2.dp, 14.dp, 2.dp)
        .shadowEffect(cornerRadius)
        .clip(RoundedCornerShape(cornerRadius))
        .background(Color.White)
        .clickable { onClick() }) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    languagesIcons: List<Painter>,
    contentDescriptions: List<String>,
    activeCardIndex: MutableState<Int>,
    cornerRadius: Dp
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
                    painter = languagesIcons[index],
                    description = contentDescriptions[index],
                    isSelected = activeCardIndex.value == index,
                    onClick = { activeCardIndex.value = index },
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

@Composable
fun RateUsDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(containerColor = Color.White, onDismissRequest = onDismiss, title = {
        Text(
            text = stringResource(id = R.string.let_us_know),
            modifier = Modifier.padding(14.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.title_text)
        )
    }, text = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeStarIndex = remember { mutableStateOf(5) }
            for (i in 1..5) {
                Icon(
                    imageVector = if (activeStarIndex.value < i) Icons.Outlined.StarBorder else Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { activeStarIndex.value = i },
                    tint = if (activeStarIndex.value < i) colorResource(id = R.color.title_text)
                    else colorResource(id = R.color.accent)
                )
            }
        }
    }, confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(
                text = stringResource(id = R.string.rate_us_confirm),
                color = colorResource(id = R.color.accent)
            )
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = stringResource(id = R.string.cancel),
                color = colorResource(id = R.color.title_text)
            )
        }
    })
}



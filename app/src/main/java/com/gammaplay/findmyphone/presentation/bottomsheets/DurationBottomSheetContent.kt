package com.gammaplay.findmyphone.presentation.bottomsheets


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gammaplay.findmyphone.R

@Composable
fun DurationBottomSheetContent(
    durationModes: List<Int>,  // List of duration modes as string resource IDs
    onOptionSelected: (Int) -> Unit,  // Callback when an option is selected
    onDismiss: () -> Unit,             // Callback to dismiss the bottom sheet
    selectedMode: Int           // Callback to dismiss the bottom sheet
) {
    // Keep track of the selected duration
    var selectedDuration by remember { mutableStateOf(selectedMode) }

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
            text = stringResource(id = R.string.choose_duration),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                items(durationModes.size) { index ->
                    val mode = durationModes[index]
                    DurationButton(
                        mode = mode,
                        isSelected = selectedDuration == mode,
                        onSelectMode = { selectedDuration = mode }
                    )
                }
            }


        }
        Spacer(modifier = Modifier.height(16.dp))

        // "Ok" button
        Button(
            onClick = {
                onOptionSelected(selectedDuration)
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.ok))
        }
    }
}

@Composable
fun DurationButton(mode: Int, isSelected: Boolean, onSelectMode: () -> Unit) {
    Button(
        onClick = { onSelectMode() },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) colorResource(id = R.color.big_btn_active) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        border = if (isSelected) null else BorderStroke(1.dp, SolidColor(Color.Black)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(horizontal = 1.5.dp)
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = stringResource(id = mode),
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

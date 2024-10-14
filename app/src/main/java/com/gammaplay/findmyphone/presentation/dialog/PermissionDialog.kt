package com.gammaplay.findmyphone.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.gammaplay.findmyphone.R
import com.gammaplay.findmyphone.ui.theme.AppTypography

import androidx.compose.ui.res.stringResource

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit
) {
    // Dialog box structure
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)) // semi-transparent background
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(30.dp)
        ) {
            // Title or App Name
            Icon(
                painter = painterResource(id = R.drawable.permission_ic),
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(44.dp)
                    .align(Alignment.CenterHorizontally),
                tint = Color.Black
            )
            Text(
                text = stringResource(id = R.string.permission_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permission items
            PermissionItem(icon = R.drawable.bolt, text = stringResource(id = R.string.permission_camera_flash))
            PermissionItem(icon = R.drawable.mic, text = stringResource(id = R.string.permission_mic_claps))

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onGrantPermissions,
                modifier = Modifier.fillMaxWidth()
            )
            {
                Text(text = stringResource(id = R.string.grant_permissions_button))
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.not_now_button))
            }

        }
    }
}

@Composable
fun PermissionItem(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp, fontStyle = AppTypography.bodyMedium.fontStyle)
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionDialogPreview() {
    PermissionDialog(onDismiss = {}, onGrantPermissions = {})
}

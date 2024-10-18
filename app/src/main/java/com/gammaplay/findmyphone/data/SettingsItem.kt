package com.gammaplay.findmyphone.data

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsItem(
    val title: Int,
    val icon: ImageVector,
    var subtitle: Int ?= null
)

data class SettingsGeneralItem(
    val title: Int,
    val icon: Int,
    var subtitle: Int ?= null
)

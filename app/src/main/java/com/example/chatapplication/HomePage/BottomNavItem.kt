package com.example.chatapplication.HomePage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Person, "Home")
    object Group : BottomNavItem("group", Icons.Default.Favorite, "Group")
    object Channel : BottomNavItem("channel", Icons.Default.Search, "Channel")
}

fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem.Home, BottomNavItem.Group, BottomNavItem.Channel
    )
}
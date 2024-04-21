package com.example.chatapplication.HomePage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import com.example.chatapplication.R


sealed class BottomNavItem(val route: String,  val label: String) {
    object Home : BottomNavItem("home", "Home")
    object Group : BottomNavItem("group",  "Group")
    object Channel : BottomNavItem("channel",  "Channel")
}

fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem.Home, BottomNavItem.Group, BottomNavItem.Channel
    )
}
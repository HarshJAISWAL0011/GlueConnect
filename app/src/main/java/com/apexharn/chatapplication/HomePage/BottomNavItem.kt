package com.apexharn.chatapplication.HomePage


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
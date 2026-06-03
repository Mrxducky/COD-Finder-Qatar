package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.screens.CodOverviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MachineListScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.TalabatOrange
import com.example.ui.theme.TalabatOrangeAlpha
import com.example.ui.theme.DarkBackground

sealed class BottomTab(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object HomeTab : BottomTab("HOME", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object MapTab : BottomTab("MAP", "Map", Icons.Filled.Map, Icons.Outlined.Map)
    object MachinesTab : BottomTab("MACHINES", "Machines", Icons.Filled.Search, Icons.Outlined.Search)
    object HistoryTab : BottomTab("HISTORY", "History", Icons.Filled.History, Icons.Outlined.History)
    object ProfileTab : BottomTab("PROFILE", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHostScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf<BottomTab>(BottomTab.HomeTab) }
    var forcePremiumShowInProfile by remember { mutableStateOf(false) }

    val tabs = listOf(
        BottomTab.HomeTab,
        BottomTab.MapTab,
        BottomTab.MachinesTab,
        BottomTab.HistoryTab,
        BottomTab.ProfileTab
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131722),
                tonalElevation = 8.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                tabs.forEach { tab ->
                    val isSelected = activeTab.route == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label,
                                tint = if (isSelected) TalabatOrange else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) TalabatOrange else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = TalabatOrangeAlpha
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                BottomTab.HomeTab -> {
                    CodOverviewScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToTab = { targetRoute ->
                            val matched = tabs.find { it.route == targetRoute }
                            if (matched != null) activeTab = matched
                        }
                    )
                }
                BottomTab.MapTab -> {
                    HomeScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToTab = { targetRoute ->
                            val matched = tabs.find { it.route == targetRoute }
                            if (matched != null) activeTab = matched
                        },
                        onShowUpgradePremiumDialog = {
                            forcePremiumShowInProfile = true
                            activeTab = BottomTab.ProfileTab
                        }
                    )
                }
                BottomTab.MachinesTab -> {
                    MachineListScreen(
                        viewModel = viewModel,
                        user = user,
                        onShowUpgradePremiumDialog = {
                            forcePremiumShowInProfile = true
                            activeTab = BottomTab.ProfileTab
                        }
                    )
                }
                BottomTab.HistoryTab -> {
                    HistoryScreen(
                        onNavigateToTab = { targetRoute ->
                            val matched = tabs.find { it.route == targetRoute }
                            if (matched != null) activeTab = matched
                        }
                    )
                }
                BottomTab.ProfileTab -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        user = user,
                        showUpgradeDialogImmediately = forcePremiumShowInProfile,
                        onDismissUpgradeImmediately = { forcePremiumShowInProfile = false }
                    )
                }
            }
        }
    }
}

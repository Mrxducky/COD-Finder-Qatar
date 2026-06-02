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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*

sealed class BottomTab(val route: String, val label: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object MapTab : BottomTab("MAP", "Map Home", Icons.Default.Map, Icons.Default.Map)
    object ListTab : BottomTab("LIST", "Search Kiosks", Icons.Default.Search, Icons.Default.Search)
    object TrackerTab : BottomTab("TRACKER", "Sentry Tracker", Icons.Default.Wallet, Icons.Default.Wallet)
    object FavoritesTab : BottomTab("FAVORITES", "Favorites", Icons.Default.Favorite, Icons.Default.FavoriteBorder)
    object ProfileTab : BottomTab("PROFILE", "Rider Profile", Icons.Default.Person, Icons.Default.Person)
    object AdminTab : BottomTab("ADMIN", "Admin Portal", Icons.Default.AdminPanelSettings, Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHostScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf<BottomTab>(BottomTab.MapTab) }
    var forcePremiumShowInProfile by remember { mutableStateOf(false) }

    val tabs = listOf(
        BottomTab.MapTab,
        BottomTab.ListTab,
        BottomTab.TrackerTab,
        BottomTab.FavoritesTab,
        BottomTab.ProfileTab,
        BottomTab.AdminTab
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
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
                                tint = if (isSelected) TalabatOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) TalabatOrange else MaterialTheme.colorScheme.onSurfaceVariant
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
                BottomTab.MapTab -> {
                    HomeScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToTab = { route ->
                            val matched = tabs.find { it.route == route }
                            if (matched != null) activeTab = matched
                        },
                        onShowUpgradePremiumDialog = {
                            forcePremiumShowInProfile = true
                            activeTab = BottomTab.ProfileTab
                        }
                    )
                }
                BottomTab.ListTab -> {
                    MachineListScreen(
                        viewModel = viewModel,
                        user = user,
                        onShowUpgradePremiumDialog = {
                            forcePremiumShowInProfile = true
                            activeTab = BottomTab.ProfileTab
                        }
                    )
                }
                BottomTab.TrackerTab -> {
                    CodTrackerScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToTab = { route ->
                            val matched = tabs.find { it.route == route }
                            if (matched != null) activeTab = matched
                        }
                    )
                }
                BottomTab.FavoritesTab -> {
                    FavoritesScreen(
                        viewModel = viewModel,
                        user = user,
                        onNavigateToTab = { route ->
                            val matched = tabs.find { it.route == route }
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
                BottomTab.AdminTab -> {
                    AdminDashboardScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

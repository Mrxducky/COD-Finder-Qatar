package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodMachineEntity
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineListScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    onShowUpgradePremiumDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    val sortStrategy by viewModel.selectedSort.collectAsState()
    val compatibility by viewModel.selectedCompatibility.collectAsState()

    val riderPos by viewModel.riderLocation.collectAsState()
    val sortedMachines by viewModel.filteredMachines.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val selectedMachineForDetail by viewModel.selectedMachineForDetail.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper search and sorting parameters panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SEARCH & FILTER MACHINES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TalabatOrange
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sorting drop-down selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Distance/Alphabetical Sort Dropdown
                    var showSortDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showSortDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sort: $sortStrategy", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            val options = listOf("Nearest", "Alphabetical", "Popular")
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.setSort(option)
                                        showSortDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Bike/Car Compatibility filter Dropdown
                    var showCompDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showCompDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(compatibility, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = showCompDropdown,
                            onDismissRequest = { showCompDropdown = false }
                        ) {
                            val options = listOf("All", "Bike Friendly", "Car Friendly")
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.setCompatibility(option)
                                        showCompDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active filter status info strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sortedMachines.size} machines match filters",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            if (query.isNotEmpty() || category != "All" || compatibility != "All" || sortStrategy != "Nearest") {
                Text(
                    text = "Clear Filters",
                    fontSize = 12.sp,
                    color = TalabatOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            viewModel.setSearchQuery("")
                            viewModel.setCategory("All")
                            viewModel.setCompatibility("All")
                            viewModel.setSort("Nearest")
                        }
                        .padding(4.dp)
                )
            }
        }

        // Machines Lazy List
        if (sortedMachines.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No COD Kiosks Found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Adjust your search terms or filter constraints",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedMachines) { machine ->
                    val dist = viewModel.calculateDistance(riderPos.first, riderPos.second, machine.latitude, machine.longitude)
                    val eta = viewModel.getTravelEtaMinutes(dist, user.riderType)
                    val isFavorite = user.favoriteMachines.split(",").contains(machine.machineId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectMachineForDetail(machine) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon Accent Circle Logo
                            val catColor = when (machine.category) {
                                "Ooredoo" -> Color(0xFF10B981)
                                "Vodafone" -> Color(0xFFEF4444)
                                "QNB" -> Color(0xFF3B82F6)
                                "CBQ" -> Color(0xFF8B5CF6)
                                else -> Color(0xFFF59E0B)
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(catColor.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (machine.category) {
                                        "Ooredoo", "Vodafone" -> Icons.Default.SmartButton
                                        else -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details texts
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = machine.machineName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${machine.branchName} • ${machine.area}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bike compatibility indicator tag
                                    Icon(
                                        imageVector = if (machine.isBikeFriendly) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = TalabatOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (machine.isBikeFriendly) "Bike OK" else "Car Parking Preferred",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Distance and ETA info widget
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = String.format("%.2f km", dist),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = TalabatOrange
                                )
                                Text(
                                    text = "$eta min drive",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) TalabatOrange else Color.LightGray,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.toggleFavorite(machine.machineId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Reuse Dialog details
    selectedMachineForDetail?.let { active ->
        val computedDistance = viewModel.calculateDistance(riderPos.first, riderPos.second, active.latitude, active.longitude)
        val computedEta = viewModel.getTravelEtaMinutes(computedDistance, user.riderType)
        val isFavorite = user.favoriteMachines.split(",").contains(active.machineId)

        MachineDetailDialog(
            machine = active,
            riderLat = riderPos.first,
            riderLng = riderPos.second,
            isFavorite = isFavorite,
            isPremium = isPremium,
            onToggleFavorite = { viewModel.toggleFavorite(active.machineId) },
            onDismiss = { viewModel.selectMachineForDetail(null) },
            onUnlockPremiumClick = onShowUpgradePremiumDialog,
            distanceKm = computedDistance,
            etaMinutes = computedEta
        )
    }
}

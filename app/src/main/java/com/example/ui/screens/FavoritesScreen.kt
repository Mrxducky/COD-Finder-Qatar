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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodMachineEntity
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val riderPos by viewModel.riderLocation.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val selectedMachineForDetail by viewModel.selectedMachineForDetail.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()

    // Match machines which are marked as favorites in userEntity
    val favoriteIds = remember(user.favoriteMachines) {
        user.favoriteMachines.split(",").filter { it.isNotEmpty() }
    }

    val favoriteMachines = remember(favoriteIds, allMachines) {
        allMachines.filter { favoriteIds.contains(it.machineId) }
    }

    // Secondary simulated "Recently Visited" list (2 random items for visual variety)
    val recentlyVisitedMachines = remember(allMachines) {
        if (allMachines.size >= 2) {
            allMachines.take(2)
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Core header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "BOOKMARKED STATIONS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = TalabatOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Your Favorite COD Kiosks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
        }

        if (favoriteMachines.isEmpty()) {
            // High UX Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Favorites Saved Yet",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Assign kiosks you deposit cash most frequently directly from the main Map or Search listings for accelerated navigation access.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onNavigateToTab("MAP") },
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Search Map Now", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "ACTIVE FAVORITES (${favoriteMachines.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                items(favoriteMachines) { machine ->
                    val dist = viewModel.calculateDistance(riderPos.first, riderPos.second, machine.latitude, machine.longitude)
                    val eta = viewModel.getTravelEtaMinutes(dist, user.riderType)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectMachineForDetail(machine) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val catColor = when (machine.category) {
                                "Ooredoo" -> Color(0xFF10B981)
                                "Vodafone" -> Color(0xFFEF4444)
                                "QNB" -> Color(0xFF3B82F6)
                                "CBQ" -> Color(0xFF8B5CF6)
                                else -> Color(0xFFF59E0B)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(catColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = TalabatOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = machine.machineName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${machine.branchName} • ${machine.area}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${String.format("%.1f km", dist)} (${eta}m drive)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TalabatOrange
                                )
                            }

                            // Quick delete favorite
                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(machine.machineId)
                                    Toast.makeText(context, "Favorite REMOVED", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkRemove,
                                    tint = Color.Gray,
                                    contentDescription = "Remove Bookmark"
                                )
                            }
                        }
                    }
                }

                // Recent visited Section
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "RECENTLY DEPOSITED RECOLLECTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                items(recentlyVisitedMachines) { machine ->
                    val dist = viewModel.calculateDistance(riderPos.first, riderPos.second, machine.latitude, machine.longitude)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectMachineForDetail(machine) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(machine.machineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${machine.branchName} • ${String.format("%.1f km", dist)}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // details Dialog
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
            onUnlockPremiumClick = { onNavigateToTab("PROFILE") },
            distanceKm = computedDistance,
            etaMinutes = computedEta
        )
    }
}

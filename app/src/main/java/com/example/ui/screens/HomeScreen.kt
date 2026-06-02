package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
fun HomeScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    onNavigateToTab: (String) -> Unit, // Allows clicking suggestions to trigger tab swaps
    onShowUpgradePremiumDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    val compatibility by viewModel.selectedCompatibility.collectAsState()
    val sortStrategy by viewModel.selectedSort.collectAsState()

    val riderPos by viewModel.riderLocation.collectAsState()
    val currentHubName by viewModel.selectedHubName.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val sortedMachines by viewModel.filteredMachines.collectAsState()
    val nearestMachine by viewModel.nearestMachine.collectAsState()
    val isVoiceActive by viewModel.isVoiceActive.collectAsState()
    val voiceStatusText by viewModel.voiceStatusText.collectAsState()
    val selectedMachineForDetail by viewModel.selectedMachineForDetail.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()

    // Threshold cash alerts
    val limitMax = if (user.riderType == "BIKE") 900.0 else 2300.0
    val isThresholdReached = user.currentCash >= (limitMax * 0.8)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Search Bar & Voice Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header text + active profile hub selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hi, ${user.name} • ${user.riderType} Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TalabatOrange
                            )
                            Text(
                                text = currentHubName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Quick Hub Swapper Button
                        var showHubDropdown by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showHubDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    tint = TalabatOrange,
                                    contentDescription = "Simulate Location GPS Hub"
                                )
                            }
                            DropdownMenu(
                                expanded = showHubDropdown,
                                onDismissRequest = { showHubDropdown = false }
                            ) {
                                Text(
                                    text = "SELECT SIMULATED RIDER GPS COORDS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                viewModel.presetHubs.forEach { hub ->
                                    DropdownMenuItem(
                                        text = { Text(hub.name, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.selectPresetHub(hub)
                                            showHubDropdown = false
                                            Toast.makeText(context, "Location updated to ${hub.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Query Search Field
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search by Area, ID, Branch...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                     viewModel.startVoiceSim()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Simulate Voice Search",
                                    tint = if (isVoiceActive) TalabatOrange else TextMuted
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TalabatOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rapid Category Pills
                    val categories = listOf("All", "Ooredoo", "Vodafone", "QNB", "CBQ")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) TalabatOrange else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setCategory(cat) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Map Area (Pans, zooms, clicks)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Background interactive render canvas
                InteractiveQatarMap(
                    riderLat = riderPos.first,
                    riderLng = riderPos.second,
                    machines = sortedMachines,
                    nearestMachine = nearestMachine,
                    onRiderLocationChanged = { lat, lng ->
                        viewModel.setRiderLocation(lat, lng)
                    },
                    onMachineSelected = { mach ->
                        viewModel.selectMachineForDetail(mach)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Voice Sim Feedback Overlay Dialog Box
                if (isVoiceActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = TalabatOrange,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "COD VOICE SEARCH ASSIST",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = voiceStatusText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.startVoiceSim("MALL OF QATAR") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text("Mall of Qatar", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Button(
                                        onClick = { viewModel.startVoiceSim("OOREDOO") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text("Ooredoo", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.cancelVoiceSim() }) {
                                    Text("Cancel", color = AlertDanger)
                                }
                            }
                        }
                    }
                }

                // 80% Cash Limit REACHED banner
                if (isThresholdReached && nearestMachine != null) {
                    val dist = viewModel.calculateDistance(riderPos.first, riderPos.second, nearestMachine!!.latitude, nearestMachine!!.longitude)
                    val eta = viewModel.getTravelEtaMinutes(dist, user.riderType)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECE0)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(TalabatOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = TalabatOrange
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nearest COD Recommended",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFD43F00)
                                )
                                Text(
                                    text = "${nearestMachine!!.machineName} (${nearestMachine!!.area}) • ${String.format("%.1f km", dist)} (${eta}m)",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Button(
                                onClick = { viewModel.selectMachineForDetail(nearestMachine) },
                                colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp).wrapContentWidth()
                            ) {
                                Text("Go", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (FAB on bottom right corner)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Emergency Quick Find Button
            FloatingActionButton(
                onClick = {
                    if (nearestMachine != null) {
                        viewModel.selectMachineForDetail(nearestMachine)
                        Toast.makeText(context, "Navigating to: ${nearestMachine!!.machineName}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No machines found. Check search filters.", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = AlertSafe,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Emergency Quick Find"
                )
            }

            // Primary Nearest FAB button
            ExtendedFloatingActionButton(
                onClick = {
                    if (nearestMachine != null) {
                        viewModel.selectMachineForDetail(nearestMachine)
                    } else {
                        Toast.makeText(context, "No machines found.", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = TalabatOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nearest COD", fontWeight = FontWeight.Bold)
            }
        }

        // Modal Details Dialog Launcher representation
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
}

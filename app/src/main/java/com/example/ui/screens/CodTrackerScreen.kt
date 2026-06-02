package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodTrackerScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val riderPos by viewModel.riderLocation.collectAsState()
    val nearestMachine by viewModel.nearestMachine.collectAsState()

    // Configuration Limits
    val limitMax = if (user.riderType == "BIKE") 900.0 else 2300.0
    val currentCash = user.currentCash
    val remainingCash = (limitMax - currentCash).coerceAtLeast(0.0)

    val progressFraction = (currentCash / limitMax).coerceIn(0.0, 1.0)

    // Color code determination
    val indicatorColor = when {
        progressFraction >= 1.0 -> AlertDanger
        progressFraction >= 0.8 -> AlertWarning
        else -> AlertSafe
    }

    val indicatorBg = when {
        progressFraction >= 1.0 -> Color(0xFFFFEAEA)
        progressFraction >= 0.8 -> Color(0xFFFEF3C7)
        else -> Color(0xFFECFDF5)
    }

    val systemMessage = when {
        progressFraction >= 1.0 -> "LIMIT BREACHED! You cannot collect more cash. Deposit immediately!"
        progressFraction >= 0.8 -> "WARNING: Cash is in 80% zone. Deposit recommended to prevent suspension."
        else -> "Safe Zone. You have adequate cash balance remaining."
    }

    var textInputCash by remember { mutableStateOf(currentCash.toString()) }

    // Keep state synchronous when DB changes
    LaunchedEffect(currentCash) {
        textInputCash = currentCash.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Intro Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TalabatOrangeAlpha, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Wallet, contentDescription = null, tint = TalabatOrange)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "COD CASH SENTRY",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = TalabatOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Manage Wallet & Limits",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Visual Balance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Cash limits settings toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(if (user.riderType == "BIKE") TalabatOrange else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.updateRiderProfile(user.name, user.email, "BIKE")
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bike (900 Max)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (user.riderType == "BIKE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(if (user.riderType == "CAR") TalabatOrange else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.updateRiderProfile(user.name, user.email, "CAR")
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Car (2300 Max)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (user.riderType == "CAR") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Radial dashboard-like telemetry counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CURRENT CASH", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%.0f QAR", currentCash),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = indicatorColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(indicatorBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (progressFraction >= 1.0) "BREACHED" else if (progressFraction >= 0.8) "NEAR LIMIT" else "SAFE SECURE",
                            color = indicatorColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Linear Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.toFloat())
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Remaining details stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Remaining: ${String.format("%.0f", remainingCash)} QAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Limit: ${String.format("%.0f", limitMax)} QAR",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning Alert Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = indicatorBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        progressFraction >= 1.0 -> Icons.Default.Cancel
                        progressFraction >= 0.8 -> Icons.Default.Warning
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = indicatorColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = systemMessage,
                    fontSize = 12.sp,
                    color = if (progressFraction >= 0.8) Color(0xFF7F1D1D) else Color(0xFF064E3B),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Enter current cash numeric adjusting widgets
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ENTER COLLECTED CASH DETAIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Text entry field
                OutlinedTextField(
                    value = textInputCash,
                    onValueChange = {
                        textInputCash = it
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) {
                            viewModel.updateRiderCash(parsed)
                        }
                    },
                    suffix = { Text("QAR", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TalabatOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fast incremental quick pads (+100 QAR, +200 QAR, Clear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAmounts = listOf(100.0, 250.0, 500.0)
                    quickAmounts.forEach { amt ->
                        Button(
                            onClick = {
                                val current = user.currentCash
                                val target = (current + amt).coerceAtMost(limitMax + 200.0)
                                viewModel.updateRiderCash(target)
                                textInputCash = target.toString()
                                Toast.makeText(context, "+$amt QAR Added to Sentry", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+$amt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Clear quick widget button
                    Button(
                        onClick = {
                            viewModel.updateRiderCash(0.0)
                            textInputCash = "0.0"
                            Toast.makeText(context, "Wallet cleared", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertDanger.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp, color = AlertDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recommend Nearest deposit booth link if they are above 80% limit
        if (progressFraction >= 0.8 && nearestMachine != null) {
            Spacer(modifier = Modifier.height(20.dp))

            val dist = viewModel.calculateDistance(riderPos.first, riderPos.second, nearestMachine!!.latitude, nearestMachine!!.longitude)
            val eta = viewModel.getTravelEtaMinutes(dist, user.riderType)

            Card(
                colors = CardDefaults.cardColors(containerColor = TalabatOrangeAlpha),
                border = BorderStroke(1.dp, TalabatOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECOMMENDED DEPOSIT BOOTH",
                        fontWeight = FontWeight.Bold,
                        color = TalabatOrangeDark,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nearestMachine!!.machineName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${nearestMachine!!.branchName} • ${nearestMachine!!.area}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = TalabatOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$eta mins drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp), tint = TalabatOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = String.format("%.2f km away", dist), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.selectMachineForDetail(nearestMachine)
                            onNavigateToTab("MAP")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View and Navigate Now", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Avoid navigation cutoff
    }
}

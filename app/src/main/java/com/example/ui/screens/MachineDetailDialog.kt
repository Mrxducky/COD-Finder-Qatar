package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CodMachineEntity
import com.example.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailDialog(
    machine: CodMachineEntity,
    riderLat: Double,
    riderLng: Double,
    isFavorite: Boolean,
    isPremium: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onUnlockPremiumClick: () -> Unit,
    distanceKm: Double,
    etaMinutes: Int
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    val badgeColor = Color(0xFFEF4444)
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = machine.category,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Favorite Button
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) TalabatOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Close Button
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Machine Name & Branch
                Text(
                    text = machine.machineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${machine.branchName} • ${machine.area}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Dashboard (Distance & ETA)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = "Distance",
                            tint = TalabatOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f km", distanceKm),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Distance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = TalabatOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$etaMinutes min",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Estimated Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (machine.isBikeFriendly) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = TalabatOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (machine.isBikeFriendly && machine.isCarFriendly) "All Riders"
                                   else if (machine.isBikeFriendly) "Bike OK"
                                   else "Car OK",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Suitability",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // One-click actions
                Button(
                    onClick = {
                        val destinationUri = Uri.parse("geo: ${machine.latitude},${machine.longitude}?q=${Uri.encode(machine.machineName)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, destinationUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Fallback to normal browser maps
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(machine.googleMapsUrl))
                            context.startActivity(fallbackIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Google Maps", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Waze Integrator
                    OutlinedButton(
                        onClick = {
                            val wazeUri = Uri.parse("waze://?ll=${machine.latitude},${machine.longitude}&navigate=yes")
                            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
                            try {
                                context.startActivity(wazeIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Waze application is not installed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open in Waze")
                    }

                    // Copy coordinates button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "${machine.latitude}, ${machine.longitude}"
                            val clip = ClipData.newPlainText("Machine Location", clipText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Coordinates Copied: $clipText", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Location")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider and QR Share graphics
                Text(
                    text = "QR CO-WORKER SHARE CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Custom procedural grid drawing of a real looking QR code (complete with positional anchors)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val machineSeed = machine.machineId.hashCode()
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val matrixSize = 21 // Version 1 QR code is 21x21 modules
                        val moduleSizeWidth = size.width / matrixSize
                        val moduleSizeHeight = size.height / matrixSize

                        // A. Procedurally draw dark modules
                        val prng = Random(machineSeed)
                        for (row in 0 until matrixSize) {
                            for (col in 0 until matrixSize) {
                                // Exclude finder patterns coordinates to keep the 3 corner cubes clear to draw properly
                                val isFinderPattern = (row < 7 && col < 7) || (row < 7 && col >= matrixSize - 7) || (row >= matrixSize - 7 && col < 7)
                                if (!isFinderPattern) {
                                    if (prng.nextBoolean()) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(col * moduleSizeWidth, row * moduleSizeHeight),
                                            size = Size(moduleSizeWidth, moduleSizeHeight)
                                        )
                                    }
                                }
                            }
                        }

                        // B. Helper to draw standard QR Anchor cubes on corners
                        fun drawFinderPattern(colOffset: Int, rowOffset: Int) {
                            val startX = colOffset * moduleSizeWidth
                            val startY = rowOffset * moduleSizeHeight

                            // Outer 7x7 square
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(startX, startY),
                                size = Size(moduleSizeWidth * 7, moduleSizeHeight * 7)
                            )
                            // Inner 5x5 white cutout
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(startX + moduleSizeWidth, startY + moduleSizeHeight),
                                size = Size(moduleSizeWidth * 5, moduleSizeHeight * 5)
                            )
                            // Core 3x3 black eye
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(startX + moduleSizeWidth * 2, startY + moduleSizeHeight * 2),
                                size = Size(moduleSizeWidth * 3, moduleSizeHeight * 3)
                            )
                        }

                        // Top-Left Finder
                        drawFinderPattern(0, 0)
                        // Top-Right Finder
                        drawFinderPattern(matrixSize - 7, 0)
                        // Bottom-Left Finder
                        drawFinderPattern(0, matrixSize - 7)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ID: ${machine.machineId}\nLet another rider scan this code to navigate to this machine.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Extra monetization highlight if they are in Free Tier
                if (!isPremium) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        onClick = onUnlockPremiumClick,
                        colors = CardDefaults.cardColors(containerColor = TalabatOrangeAlpha),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = TalabatOrange
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unlock Premium Navigation",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TalabatOrangeDark
                                )
                                Text(
                                    text = "Get real-time traffic, route optimization & limitless favorites.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TalabatOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

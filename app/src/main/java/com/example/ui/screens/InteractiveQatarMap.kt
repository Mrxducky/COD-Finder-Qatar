package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodMachineEntity
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InteractiveQatarMap(
    riderLat: Double,
    riderLng: Double,
    machines: List<CodMachineEntity>,
    nearestMachine: CodMachineEntity?,
    onRiderLocationChanged: (Double, Double) -> Unit,
    onMachineSelected: (CodMachineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Widen coordinates of mapping representation to encompass the whole State of Qatar
    val minLat = 24.3000 // South boundary (border region)
    val maxLat = 26.3000 // North boundary (Al Ruwais)
    val minLng = 50.7000 // West boundary (Dukhan coast)
    val maxLng = 51.8000 // East boundary (Doha Ports coast)

    // Interaction Gestures State
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var scale by remember { mutableStateOf(1.8f) }
    var isTrackingRider by remember { mutableStateOf(true) }

    // Selected machine detail card overlay internal state
    var activeDetailMachine by remember { mutableStateOf<CodMachineEntity?>(null) }

    // Sync activeDetailMachine if nearestMachine is changed by system
    LaunchedEffect(nearestMachine) {
        if (nearestMachine != null && activeDetailMachine == null) {
            activeDetailMachine = nearestMachine
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color(0xFF090D16)) // Deep Premium Dark Blue Space
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    isTrackingRider = false // Manual pan/zoom releases auto-tracking
                    scale = (scale * zoom).coerceIn(0.8f, 15.0f)
                    panOffset += pan
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Continuous alignment onto rider's GPS location when tracking is enabled.
        LaunchedEffect(riderLat, riderLng, width, height, scale, isTrackingRider) {
            if (isTrackingRider && width > 0f && height > 0f) {
                val progressX = (riderLng - minLng) / (maxLng - minLng)
                val progressY = 1.0 - ((riderLat - minLat) / (maxLat - minLat))
                panOffset = Offset(
                    x = (width / 2f) - (progressX * width * scale).toFloat(),
                    y = (height / 2f) - (progressY * height * scale).toFloat()
                )
            }
        }

        // Translation function from Geographical latitude & longitude to Screen Pixels
        fun getPixelCoords(lat: Double, lng: Double): Offset {
            val progressX = (lng - minLng) / (maxLng - minLng)
            // Latitude coordinates run North-wards, while screen axis coordinates run downwards
            val progressY = 1.0 - ((lat - minLat) / (maxLat - minLat))

            val x = (progressX * width * scale).toFloat() + panOffset.x
            val y = (progressY * height * scale).toFloat() + panOffset.y
            return Offset(x, y)
        }

        // Draw Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(machines, riderLat, riderLng, scale, panOffset) {
                    detectTapGestures { tapOffset ->
                        var clicked: CodMachineEntity? = null
                        // Determine if user tapped closer than 45dp bounds to any machine pin
                        for (machine in machines) {
                            val pinPos = getPixelCoords(machine.latitude, machine.longitude)
                            val dist = (tapOffset - pinPos).getDistance()
                            // Expand tap Target size relative to scales
                            val hitRadius = if (scale < 2.0f) 35f else 48f
                            if (dist < hitRadius) {
                                clicked = machine
                                break
                            }
                        }
                        if (clicked != null) {
                            activeDetailMachine = clicked
                            onMachineSelected(clicked)
                        }
                    }
                }
        ) {
            // A. Draw Land Mass of the state of Qatar (Clockwise geofenced path outline)
            val qatarPath = Path().apply {
                val pt1 = getPixelCoords(24.62, 51.48) // East border corner
                moveTo(pt1.x, pt1.y)
                val pt2 = getPixelCoords(24.90, 51.55) // Mesaieed
                lineTo(pt2.x, pt2.y)
                val pt3 = getPixelCoords(25.17, 51.61) // Al Wakrah
                lineTo(pt3.x, pt3.y)
                val pt4 = getPixelCoords(25.285, 51.545) // Doha
                lineTo(pt4.x, pt4.y)
                val pt5 = getPixelCoords(25.33, 51.52)  // Corniche
                lineTo(pt5.x, pt5.y)
                val pt6 = getPixelCoords(25.38, 51.56)  // West Bay / Pearl
                lineTo(pt6.x, pt6.y)
                val pt7 = getPixelCoords(25.50, 51.49)  // Lusail / Simaisma
                lineTo(pt7.x, pt7.y)
                val pt8 = getPixelCoords(25.68, 51.52)  // Al Khor
                lineTo(pt8.x, pt8.y)
                val pt9 = getPixelCoords(26.00, 51.38)  // Fuwayrit 
                lineTo(pt9.x, pt9.y)
                val pt10 = getPixelCoords(26.15, 51.21) // Al Ruwais (Northeastern Point)
                lineTo(pt10.x, pt10.y)
                val pt11 = getPixelCoords(26.05, 51.05) // North-West coast
                lineTo(pt11.x, pt11.y)
                val pt12 = getPixelCoords(25.98, 51.02) // Al Zubarah fort region
                lineTo(pt12.x, pt12.y)
                val pt13 = getPixelCoords(25.43, 50.78) // Dukhan West coast
                lineTo(pt13.x, pt13.y)
                val pt14 = getPixelCoords(24.65, 50.80) // Salwa South-West border
                lineTo(pt14.x, pt14.y)
                val pt15 = getPixelCoords(24.50, 51.00) // Southern mainland base
                lineTo(pt15.x, pt15.y)
                val pt16 = getPixelCoords(24.50, 51.40) // Southern base line edge
                lineTo(pt16.x, pt16.y)
                close()
            }

            // Fill Peninsula with a premium dark gray/blue land color
            drawPath(
                path = qatarPath,
                color = Color(0xFF161F30) // Slate-Sand Core
            )

            // Outline coastal land boundary
            drawPath(
                path = qatarPath,
                color = Color(0xFF334155).copy(alpha = 0.6f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )

            // B. Draw Interactive Grid Lines for high-tech HUD styling
            val latGrid = listOf(24.50, 25.00, 25.50, 26.00)
            val lngGrid = listOf(50.80, 51.10, 51.40, 51.70)
            for (lat in latGrid) {
                val start = getPixelCoords(lat, 50.70)
                val end = getPixelCoords(lat, 51.80)
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = start,
                    end = end,
                    strokeWidth = 1f
                )
            }
            for (lng in lngGrid) {
                val start = getPixelCoords(24.30, lng)
                val end = getPixelCoords(26.30, lng)
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = start,
                    end = end,
                    strokeWidth = 1f
                )
            }

            // C. Draw Main Highway network lines
            val highways = listOf(
                // Al Shamal Main Highway (Doha -> Al Kharaitiyat -> Lusail -> Al Khor -> Al Ruwais)
                listOf(
                    Pair(25.285, 51.53), Pair(25.35, 51.48), Pair(25.42, 51.45),
                    Pair(25.55, 51.43), Pair(25.68, 51.45), Pair(25.85, 51.35),
                    Pair(26.15, 51.21)
                ),
                // Salwa Road Link (Doha -> Industrial Area -> West boundary)
                listOf(
                    Pair(25.285, 51.53), Pair(25.25, 51.47), Pair(25.21, 51.39),
                    Pair(25.08, 51.25), Pair(24.85, 50.98), Pair(24.65, 50.80)
                ),
                // Al Majd West Trunk (South-Mesaieed -> West-Bypass -> North)
                listOf(
                    Pair(24.90, 51.50), Pair(25.05, 51.32), Pair(25.25, 51.25),
                    Pair(25.50, 51.28), Pair(25.75, 51.35)
                )
            )

            for (highway in highways) {
                val roadPath = Path().apply {
                    highway.getOrNull(0)?.let { first ->
                        val start = getPixelCoords(first.first, first.second)
                        moveTo(start.x, start.y)
                    }
                    for (i in 1 until highway.size) {
                        val pt = getPixelCoords(highway[i].first, highway[i].second)
                        lineTo(pt.x, pt.y)
                    }
                }
                drawPath(
                    path = roadPath,
                    color = Color(0xFF2E3E59).copy(alpha = 0.5f),
                    style = Stroke(width = 3f * scale.coerceAtMost(3.0f), cap = StrokeCap.Round)
                )
            }

            // D. Draw district circles that help riders identify their reference points
            val referenceCities = listOf(
                Triple("DOH", 25.2854, 51.5310),
                Triple("IND", 25.1950, 51.4110),
                Triple("WAK", 25.1764, 51.6033),
                Triple("LUS", 25.4215, 51.5225),
                Triple("KHO", 25.6880, 51.5030),
                Triple("RAY", 25.2950, 51.4250)
            )

            for (city in referenceCities) {
                val pos = getPixelCoords(city.second, city.third)
                drawCircle(
                    color = Color(0xFF22D3EE).copy(alpha = 0.08f),
                    radius = 28f * scale.coerceAtMost(2.5f),
                    center = pos
                )
            }

            // E. Draw Travel Navigation Line to currently selected machine
            activeDetailMachine?.let { selected ->
                val riderPos = getPixelCoords(riderLat, riderLng)
                val machPos = getPixelCoords(selected.latitude, selected.longitude)

                val navPath = Path().apply {
                    moveTo(riderPos.x, riderPos.y)
                    // Beautiful architectural organic bezier curve for realistic routing feeling
                    val ctrlX = (riderPos.x + machPos.x) / 2f + 35f
                    val ctrlY = (riderPos.y + machPos.y) / 2f - 40f
                    quadraticTo(ctrlX, ctrlY, machPos.x, machPos.y)
                }

                // Shadow trace
                drawPath(
                    path = navPath,
                    color = Color.Black.copy(alpha = 0.4e-1f),
                    style = Stroke(width = 12f * scale.coerceAtMost(2.0f), cap = StrokeCap.Round)
                )

                // Highlighted live route
                drawPath(
                    path = navPath,
                    color = TalabatOrange.copy(alpha = 0.85f),
                    style = Stroke(width = 5f * scale.coerceAtMost(2.5f), cap = StrokeCap.Round)
                )
            }

            // F. Plot All 131 machine database locations
            for (machine in machines) {
                val pinPos = getPixelCoords(machine.latitude, machine.longitude)
                val isSelected = machine.machineId == activeDetailMachine?.machineId
                val isNearest = machine.machineId == nearestMachine?.machineId

                // Render pulsating locator rings on the targeted/nearest kiosk
                if (isSelected || isNearest) {
                    val radiusPulse = (24f + sin(System.currentTimeMillis() / 140.0).toFloat() * 6f) * scale.coerceAtMost(4.0f)
                    drawCircle(
                        color = if (isSelected) Color(0xFF22D3EE).copy(alpha = 0.25f) else TalabatOrange.copy(alpha = 0.2f),
                        radius = radiusPulse,
                        center = pinPos
                    )
                }

                // Multi-scale proportional sizing to prevent massive overlapping blobs
                val baseRadius = if (scale < 1.5f) 5.5f else if (scale < 3.0f) 8.5f else 11f
                val pinColor = if (isSelected) Color(0xFF00FBFF) else if (isNearest) TalabatOrange else Color(0xFFEF4444)

                // Pin base drop shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.6f),
                    radius = baseRadius * 1.3f,
                    center = pinPos + Offset(1f * scale.coerceAtMost(2.0f), 1.5f * scale.coerceAtMost(2.0f))
                )

                // Pin Outer shell Ring
                drawCircle(
                    color = pinColor,
                    radius = baseRadius,
                    center = pinPos
                )

                // White concentric focus point
                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.45f,
                    center = pinPos
                )
            }

            // G. Draw Rider Location Dot
            val riderPos = getPixelCoords(riderLat, riderLng)
            // Pulse flare shadow
            drawCircle(
                color = TalabatOrange.copy(alpha = 0.2f),
                radius = 32f * scale.coerceAtMost(2.0f),
                center = riderPos
            )
            // Border shield
            drawCircle(
                color = Color.White,
                radius = 12f * scale.coerceAtMost(2.0f),
                center = riderPos
            )
            // Core
            drawCircle(
                color = TalabatOrange,
                radius = 8.5f * scale.coerceAtMost(2.0f),
                center = riderPos
            )
        }

        // --- MAP HUD FLOATING CONTROLS (Right hand side vertical stack) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom In Action
            FloatingActionButton(
                onClick = {
                    scale = (scale * 1.35f).coerceAtMost(15.0f)
                    isTrackingRider = false
                },
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("zoom_in_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
            }

            // Zoom Out Action
            FloatingActionButton(
                onClick = {
                    scale = (scale / 1.35f).coerceAtLeast(0.8f)
                    isTrackingRider = false
                },
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("zoom_out_button")
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            // Recenter Live Rider Lock GPS Toggle
            FloatingActionButton(
                onClick = {
                    isTrackingRider = !isTrackingRider
                    if (isTrackingRider) {
                        scale = 2.4f
                    }
                },
                containerColor = if (isTrackingRider) TalabatOrange else Color(0xFF1E293B),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("gps_tracking_toggle")
            ) {
                Icon(
                    imageVector = if (isTrackingRider) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                    contentDescription = "Recenter Rider Position"
                )
            }
        }

        // --- QUICK TELEPORT WARP NAVIGATION BADGES (Top Row Filter) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp)
                .fillMaxWidth()
                .horizontalScrollEnabled(), // Seamless scrolling if view narrows
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickWarpDestinations = listOf(
                "Doha Center" to Pair(25.2854, 51.5310),
                "Sanniya Area" to Pair(25.1950, 51.4110),
                "Al Wakrah" to Pair(25.1764, 51.6033),
                "Al Khor" to Pair(25.6880, 51.5030),
                "Al Rayyan" to Pair(25.2950, 51.4250),
                "Lusail City" to Pair(25.4215, 51.5225)
            )

            for (warp in quickWarpDestinations) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.82f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .clickable {
                            isTrackingRider = false
                            // Smooth manual teleportation jump
                            scale = 3.2f
                            val progressX = (warp.second.second - minLng) / (maxLng - minLng)
                            val progressY = 1.0 - ((warp.second.first - minLat) / (maxLat - minLat))
                            panOffset = Offset(
                                x = (width / 2f) - (progressX * width * scale).toFloat(),
                                y = (height / 2f) - (progressY * height * scale).toFloat()
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = warp.first,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // --- MAP STATUS HUD BADGES (Bottom Left overlay) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = if (activeDetailMachine != null) 165.dp else 12.dp)
                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isTrackingRider) Color(0xFF10B981) else Color(0xFFFBBF24), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTrackingRider) "Live Tracker GPS Sync Active" else "Manual Map Navigation Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${machines.size} Registered Vodafone Kiosks",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // --- EXPANDED SEAMLESS BOTTOM DETAIL HUD CARD (Sliding drawer) ---
        AnimatedVisibility(
            visible = activeDetailMachine != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            activeDetailMachine?.let { machine ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B) // Dark Premium Slate Card
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Title row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = machine.machineName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(TalabatOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ID: ${machine.machineId}",
                                            color = TalabatOrange,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "📍 ${machine.area}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Dismiss close box
                            IconButton(
                                onClick = { activeDetailMachine = null },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss info",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Address Row detail description
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Store Branch Details",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = machine.branchName,
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Interactive Quick Links Intent Triggers
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Direct Google Maps Navigator Link Intent
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(machine.googleMapsUrl))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF34D399) // Google Green highlight accent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = "Map icon",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Google Map", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Direct Waze/Standard secondary web navigator Intent link
                            Button(
                                onClick = {
                                    val fallbackWazeUrl = "https://waze.com/ul?ll=${machine.latitude},${machine.longitude}&navigate=yes"
                                    val targetUrl = if (machine.googleMapsUrl.contains("waze")) machine.googleMapsUrl else fallbackWazeUrl
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E1FF) // Waze Cyan blue accent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Waze navigation icon",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Waze Nav", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple modifier extension helper supporting scroll for row elements
@Composable
fun Modifier.horizontalScrollEnabled(): Modifier {
    val state = rememberScrollState()
    return this.horizontalScroll(state)
}

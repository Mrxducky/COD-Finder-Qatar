package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.local.CodMachineEntity
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

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
    // Spatial mapping bounds for Qatar (specifically the high density Doha segment)
    val minLat = 25.1000 // South (Al Wakrah)
    val maxLat = 25.4500 // North (Lusail/Place Vendôme)
    val minLng = 51.3000 // West (Al Rayyan/Mall of Qatar)
    val maxLng = 51.6500 // East (Doha Ports/Coast)

    // Pan and zoom states
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var scale by remember { mutableStateOf(1.2f) }

    BoxWithConstraints(
        modifier = modifier
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffset += dragAmount
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Helper function to map GPS coordinates to Canvas pixel coordinates
        fun getPixelCoords(lat: Double, lng: Double): Offset {
            val progressX = (lng - minLng) / (maxLng - minLng)
            // Latitude points north, but canvas Y coordinates point down
            val progressY = 1.0 - ((lat - minLat) / (maxLat - minLat))

            val x = (progressX * width * scale).toFloat() + panOffset.x
            val y = (progressY * height * scale).toFloat() + panOffset.y
            return Offset(x, y)
        }

        // Helper function to reverse map Canvas pixel Coordinates to GPS
        fun getGpsCoords(pixelOffset: Offset): Pair<Double, Double> {
            val relativeX = (pixelOffset.x - panOffset.x) / (width * scale)
            val relativeY = 1.0 - ((pixelOffset.y - panOffset.y) / (height * scale))

            val lng = minLng + (relativeX * (maxLng - minLng))
            val lat = minLat + (relativeY * (maxLat - minLat))
            return Pair(lat.toDouble(), lng.toDouble())
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(machines, riderLat, riderLng, scale, panOffset) {
                    detectTapGestures { tapOffset ->
                        // 1. Check if user tapped a machine pin
                        var clickedMachine: CodMachineEntity? = null
                        for (machine in machines) {
                            val pinPos = getPixelCoords(machine.latitude, machine.longitude)
                            val distancePixels = (tapOffset - pinPos).getDistance()
                            if (distancePixels < 28f) { // ~12dp radius tap target
                                clickedMachine = machine
                                break
                            }
                        }

                        if (clickedMachine != null) {
                            onMachineSelected(clickedMachine)
                        } else {
                            // 2. Otherwise, move rider's location to this clicked point
                            val targetGps = getGpsCoords(tapOffset)
                            // Bound user coordinate changes
                            val boundedLat = targetGps.first.coerceIn(minLat, maxLat)
                            val boundedLng = targetGps.second.coerceIn(minLng, maxLng)
                            onRiderLocationChanged(boundedLat, boundedLng)
                        }
                    }
                }
        ) {
            // A. Draw Doha coastline background (Gulf Bay)
            val coastPath = Path().apply {
                val p1 = getPixelCoords(25.10, 51.62)
                moveTo(p1.x, p1.y)
                val p2 = getPixelCoords(25.18, 51.61)
                lineTo(p2.x, p2.y)
                val p3 = getPixelCoords(25.26, 51.58)
                quadraticTo(p3.x, p3.y, getPixelCoords(25.28, 51.54).x, getPixelCoords(25.28, 51.54).y)
                val p4 = getPixelCoords(25.33, 51.52) // Corniche curve
                quadraticTo(p4.x, p4.y, getPixelCoords(25.35, 51.55).x, getPixelCoords(25.35, 51.55).y)
                val p5 = getPixelCoords(25.38, 51.56) // Pearl Qatar
                lineTo(p5.x, p5.y)
                val p6 = getPixelCoords(25.42, 51.54) // Lusail Coast
                lineTo(p6.x, p6.y)
                val p7 = getPixelCoords(25.45, 51.53)
                lineTo(p7.x, p7.y)
                // Fill the water right side of the screen
                lineTo(width * scale + panOffset.x, getPixelCoords(25.45, 51.53).y)
                lineTo(width * scale + panOffset.x, getPixelCoords(25.10, 51.62).y)
                close()
            }
            drawPath(
                path = coastPath,
                color = Color(0xFF1D2B3F), // Dark ocean blue
            )

            // B. Draw Main Expressways in Doha
            // 1. D-Ring Road & Al Shamal Rd
            val shamalPath = Path().apply {
                val start = getPixelCoords(25.15, 51.54)
                moveTo(start.x, start.y)
                val mid = getPixelCoords(25.29, 51.48)
                lineTo(mid.x, mid.y)
                val end = getPixelCoords(25.45, 51.46)
                lineTo(end.x, end.y)
            }
            drawPath(
                path = shamalPath,
                color = Color(0xFF3F3F46),
                style = Stroke(width = 4f * scale, cap = StrokeCap.Round)
            )

            // 2. Sabah Al Ahmad Corridor (Diagonal)
            val corridorPath = Path().apply {
                val start = getPixelCoords(25.20, 51.46)
                moveTo(start.x, start.y)
                val mid = getPixelCoords(25.31, 51.45)
                lineTo(mid.x, mid.y)
                val end = getPixelCoords(25.35, 51.49)
                lineTo(end.x, end.y)
            }
            drawPath(
                path = corridorPath,
                color = Color(0xFF3F3F46),
                style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
            )

            // C. Draw District Names
            val districts = listOf(
                Triple("West Bay", 25.3262, 51.5303),
                Triple("Souq Waqif", 25.2867, 51.5333),
                Triple("Al Wakrah", 25.1764, 51.6033),
                Triple("Al Rayyan", 25.3000, 51.4000),
                Triple("Lusail", 25.4208, 51.5218),
                Triple("Industrial Area", 25.1952, 51.4112)
            )
            for (dist in districts) {
                val pos = getPixelCoords(dist.second, dist.third)
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = 32f * scale,
                    center = pos
                )
            }

            // D. Draw Optimized Travel Route line to nearest recommended machine
            if (nearestMachine != null) {
                val riderPos = getPixelCoords(riderLat, riderLng)
                val machPos = getPixelCoords(nearestMachine.latitude, nearestMachine.longitude)

                // Render Route Line with animated feel
                val routePath = Path().apply {
                    moveTo(riderPos.x, riderPos.y)
                    // Draw a scenic curved detour reflecting standard road network
                    val controlX = (riderPos.x + machPos.x) / 2f + 40f
                    val controlY = (riderPos.y + machPos.y) / 2f - 30f
                    quadraticTo(controlX, controlY, machPos.x, machPos.y)
                }

                // Draw route shadow
                drawPath(
                    path = routePath,
                    color = Color.Black.copy(alpha = 0.4f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Draw active route highlight
                drawPath(
                    path = routePath,
                    color = TalabatOrange, // Route is Talabat Orange highlight!
                    style = Stroke(width = 4f * scale, cap = StrokeCap.Round)
                )
            }

            // E. Draw COD Machine Locations (Pins)
            for (machine in machines) {
                val pinPos = getPixelCoords(machine.latitude, machine.longitude)

                // Decide color based on category
                val pinColor = when (machine.category) {
                    "Ooredoo" -> Color(0xFF10B981) // Emerald Green for Ooredoo
                    "Vodafone" -> Color(0xFFEF4444) // Intense Red for Vodafone
                    "QNB" -> Color(0xFF3B82F6) // Royal Blue for QNB
                    "CBQ" -> Color(0xFF8B5CF6) // Purple for Commercial Bank
                    else -> Color(0xFFF59E0B) // Amber
                }

                val isNearest = machine.machineId == nearestMachine?.machineId

                if (isNearest) {
                    // Pulsating radar ring for nearest machine
                    drawCircle(
                        color = TalabatOrange.copy(alpha = 0.3f),
                        radius = (18f + sin(System.currentTimeMillis() / 150.0).toFloat() * 4f) * scale,
                        center = pinPos
                    )
                }

                // Main pin body
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 12f * scale,
                    center = pinPos + Offset(2f, 2f) // Shadow
                )
                drawCircle(
                    color = pinColor,
                    radius = 10f * scale,
                    center = pinPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f * scale,
                    center = pinPos
                )
            }

            // F. Draw Rider Location Avatar
            val riderPos = getPixelCoords(riderLat, riderLng)
            // Ripple halo
            drawCircle(
                color = TalabatOrange.copy(alpha = 0.25f),
                radius = 24f * scale,
                center = riderPos
            )
            // Avatar background border
            drawCircle(
                color = Color.White,
                radius = 11f * scale,
                center = riderPos
            )
            // Rider Center Dot
            drawCircle(
                color = TalabatOrange,
                radius = 8f * scale,
                center = riderPos
            )
        }

        // Overlay Guide text on top of Map
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.75f), MaterialTheme.shapes.small)
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Text(
                text = "📍 Drag/Tap map to simulate location",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981)).aspectRatio(1f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ooredoo", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444)).aspectRatio(1f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vodafone", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF3B82F6)).aspectRatio(1f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Banks", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

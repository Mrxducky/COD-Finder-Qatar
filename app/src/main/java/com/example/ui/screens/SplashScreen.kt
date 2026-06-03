package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TalabatOrange
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animateStart by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateStart = true
        delay(2500)
        onSplashComplete()
    }

    val scale by animateTransition(animateStart)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight().padding(vertical = 40.dp)
        ) {
            // Invisible spacer to balance top area or custom brand label
            Text(
                text = "TALABAT PLATFORM",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = TalabatOrange.copy(alpha = 0.6f),
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 20.dp)
            )

            // Center Logo block and slogan
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
            ) {
                // Customized COD RIDER logo with Map Pin inside
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "C",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = TalabatOrange,
                        letterSpacing = (-1).sp
                    )
                    
                    // Stylized O as Map Pin
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TalabatOrange,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Dot inside Pin
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(DarkBackground)
                        )
                    }

                    Text(
                        text = "D",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = TalabatOrange,
                        letterSpacing = (-1).sp
                    )
                }

                Text(
                    text = "RIDER",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 10.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 10.dp) // Offset center gap for tracking
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Find Nearest COD Machines\nSave Time. Deliver Better.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Custom graphic illustration of a driver riding scooter
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(TalabatOrange.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = TalabatOrange,
                        modifier = Modifier.size(72.dp)
                    )
                    // Custom Delivery Box representation
                    Box(
                        modifier = Modifier
                            .offset(x = (-22).dp, y = (-12).dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TalabatOrange)
                    )
                }
            }

            // Progress loading line on bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                val progressWidth by animateProgress(animateStart)
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(progressWidth)
                            .clip(CircleShape)
                            .background(TalabatOrange)
                    )
                }
            }
        }
    }
}

@Composable
fun animateTransition(animate: Boolean): State<Float> {
    return animateFloatAsState(
        targetValue = if (animate) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

@Composable
fun animateProgress(animate: Boolean): State<androidx.compose.ui.unit.Dp> {
    return animateDpAsState(
        targetValue = if (animate) 130.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 2300,
            easing = LinearEasing
        )
    )
}

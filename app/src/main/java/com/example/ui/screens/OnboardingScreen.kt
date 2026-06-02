package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TalabatOrange

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentWord: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OnboardingStep(
            "Find Machines in",
            "Instantly locate Ooredoo, Vodafone, QNB, or CBQ cash deposit kiosks closest to your coordinates.",
            Icons.Default.Map,
            "Seconds"
        ),
        OnboardingStep(
            "Track Cash",
            "Monitor cash limits (900 QAR Bike / 2300 QAR Car) with real-time risk trackers and alerts to avoid order suspension.",
            Icons.Default.MonetizationOn,
            "Limits"
        ),
        OnboardingStep(
            "One-Tap",
            "Route immediately via Google Maps or Waze with optimal traffic routing tailored for bikes and cars.",
            Icons.Default.Navigation,
            "Navigation"
        )
    )

    var currentStepIdx by remember { mutableStateOf(0) }
    val step = steps[currentStepIdx]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Core Content (Individually Animated sliding blocks)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle Icon Accent
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(TalabatOrange.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = TalabatOrange,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Main typography
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = step.title + " ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = step.accentWord,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TalabatOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 24.sp
            )
        }

        // Stepper dots and Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lower Page stepper dots
            Row {
                repeat(steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentStepIdx) 12.dp else 8.dp)
                            .background(
                                color = if (index == currentStepIdx) TalabatOrange else Color.DarkGray,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Button controls
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentStepIdx < steps.size - 1) {
                    TextButton(onClick = onOnboardingComplete) {
                        Text("Skip", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { currentStepIdx++ },
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onOnboardingComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

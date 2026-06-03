package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TalabatOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CodViewModel,
    user: UserEntity,
    showUpgradeDialogImmediately: Boolean,
    onDismissUpgradeImmediately: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val riderType = user.riderType
    val codLimitText = if (riderType == "BIKE") "900 QAR" else "2300 QAR"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Settings Open Checked", Toast.LENGTH_SHORT).show() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Giant Avatar with Person Silhouette Icon matching Mockup 10
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(TalabatOrange.copy(alpha = 0.15f))
                    .border(2.dp, TalabatOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TalabatOrange,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info headers
            Text(
                text = user.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = user.email, // Phone number doubles here
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Tag Badge representing active zone vehicle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, TalabatOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .background(TalabatOrange.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (riderType == "BIKE") Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = TalabatOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (riderType == "BIKE") "Bike Rider" else "Car Rider",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TalabatOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Menu Section containing rows matching mockup perfectly
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Vehicle Type (Toggles vehicle state)
                ProfileMenuRowItem(
                    icon = Icons.Default.TwoWheeler,
                    title = "Vehicle Type",
                    valueText = if (riderType == "BIKE") "Bike" else "Car",
                    onClick = {
                        viewModel.toggleRiderTypeSetting()
                        Toast.makeText(context, "Swapped Vehicle Type to ${if (riderType == "BIKE") "CAR" else "BIKE"}", Toast.LENGTH_SHORT).show()
                    }
                )

                // Row 2: COD Limit (Dynamic info indicator)
                ProfileMenuRowItem(
                    icon = Icons.Default.Shield,
                    title = "COD Limit",
                    valueText = codLimitText,
                    onClick = {
                        Toast.makeText(context, "Limit capped according to selected Vehicle", Toast.LENGTH_SHORT).show()
                    }
                )

                // Row 3: Language selection
                ProfileMenuRowItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    valueText = "English",
                    onClick = {
                        Toast.makeText(context, "English selected as priority Arabic content translation ready", Toast.LENGTH_SHORT).show()
                    }
                )

                // Row 4: Support
                ProfileMenuRowItem(
                    icon = Icons.Default.HeadsetMic,
                    title = "Help & Support",
                    valueText = "",
                    onClick = {
                        Toast.makeText(context, "Opening instant Talabat dispatcher portal...", Toast.LENGTH_SHORT).show()
                    }
                )

                // Row 5: About App
                ProfileMenuRowItem(
                    icon = Icons.Default.Info,
                    title = "About App",
                    valueText = "v1.2.0",
                    onClick = {
                        Toast.makeText(context, "COD Rider Qatar - Certified Production Build v1.2.0", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Row 6: Logout (Red warning row)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1315)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Restart app to change sessions successfully", Toast.LENGTH_LONG).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color(0xFFEF4444), // Crimson logout red
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Logout",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileMenuRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    valueText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TalabatOrange.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TalabatOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (valueText.isNotEmpty()) {
                    Text(
                        text = valueText,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
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

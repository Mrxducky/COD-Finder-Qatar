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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.UserEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val isPremium by viewModel.isPremiumUser.collectAsState()

    var nameVal by remember { mutableStateOf(user.name) }
    var emailVal by remember { mutableStateOf(user.email) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Synchronize states
    LaunchedEffect(user) {
        nameVal = user.name
        emailVal = user.email
    }

    // Trigger upgrade dialog if navigated to from elsewhere
    LaunchedEffect(showUpgradeDialogImmediately) {
        if (showUpgradeDialogImmediately) {
            showCheckoutDialog = true
            onDismissUpgradeImmediately()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large circular avatar badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(TalabatOrangeAlpha, CircleShape)
                .border(2.dp, TalabatOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (user.riderType == "BIKE") Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = TalabatOrange,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = user.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isPremium) Color(0xFF10B981) else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPremium) "PREMIUM ACCOUNT ACTIVE" else "FREE ACCOUNT PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPremium) Color(0xFF10B981) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Monetization Promo Card
        if (!isPremium) {
            Card(
                onClick = { showCheckoutDialog = true },
                colors = CardDefaults.cardColors(containerColor = TalabatOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Diamond, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upgrade to COD Pro",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlock professional dispatching upgrades: traffic router integration, unlimited favorited stations caching, and advanced daily metrics graphs for just 29 QAR/month.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unlock Premium for 29 QAR", fontWeight = FontWeight.ExtraBold, color = TalabatOrange)
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("COD Finder Pro Active", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("Unlimited bookmarks, Route optimization, and Analytics unlocked.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Editable profile credentials form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EDIT RIDER PROFILE DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameVal,
                    onValueChange = { nameVal = it },
                    label = { Text("Display Name") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TalabatOrange),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = emailVal,
                    onValueChange = { emailVal = it },
                    label = { Text("Portal Email") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TalabatOrange),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (nameVal.isEmpty() || emailVal.isEmpty()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateRiderProfile(nameVal, emailVal, user.riderType)
                        Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Avoid final cutoffs
    }

    // High fidelity premium checkout Dialog container
    if (showCheckoutDialog) {
        Dialog(onDismissRequest = { showCheckoutDialog = false }) {
            var payingStatus by remember { mutableStateOf("READY") } // "READY", "PROCESSING", "SUCCESS"

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (payingStatus) {
                        "READY" -> {
                            Icon(imageVector = Icons.Default.Diamond, contentDescription = null, tint = TalabatOrange, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("COD Finder Premium Upgrade", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            Text("Unlock ultimate dispatch tools to boost your Qatar hourly delivery speeds.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 6.dp))

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment Mock Form Details
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Simulated Qatar Payment Gateway", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("RIDER ACCOUNT: ${user.email}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("AMOUNT DUE: 29.00 QAR / month", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TalabatOrange)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    payingStatus = "PROCESSING"
                                    coroutineScope.launch {
                                        delay(1500)
                                        payingStatus = "SUCCESS"
                                        delay(1500)
                                        viewModel.setPremiumStatus(true)
                                        showCheckoutDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pay 29.00 QAR", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(onClick = { showCheckoutDialog = false }) {
                                Text("Cancel", color = AlertDanger)
                            }
                        }
                        "PROCESSING" -> {
                            CircularProgressIndicator(color = TalabatOrange, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verifying transaction credentials...", fontWeight = FontWeight.Bold)
                            Text("Processing banking token via CBQ Gateway", fontSize = 11.sp, color = Color.Gray)
                        }
                        "SUCCESS" -> {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AlertSafe, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("PAYMENT COMPLETED!", fontWeight = FontWeight.Bold, color = AlertSafe)
                            Text("Your COD Finder Pro features are now fully enabled.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

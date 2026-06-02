package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit, // returns name, email, riderType
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var riderType by remember { mutableStateOf("BIKE") } // Default Bike rider
    var isRegisterMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(TalabatOrangeAlpha, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = TalabatOrange,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isRegisterMode) "Rider Registration" else "Talabat Rider Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Enter your portal credentials to sync COD machines",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Name Field (only in register/profile creation mode)
            if (isRegisterMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TalabatOrange) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = TalabatOrange,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Rider Portal Email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TalabatOrange) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TalabatOrange,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TalabatOrange) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TalabatOrange,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rider Type Selection Group
            Text(
                text = "SELECT RIDER ZONE TYPE",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(Color(0xFF27272A), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                // BIKE RIDER Option
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (riderType == "BIKE") TalabatOrange else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { riderType = "BIKE" },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = if (riderType == "BIKE") Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Bike Rider",
                        fontWeight = FontWeight.Bold,
                        color = if (riderType == "BIKE") Color.White else Color.Gray,
                        fontSize = 13.sp
                    )
                }

                // CAR RIDER Option
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (riderType == "CAR") TalabatOrange else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { riderType = "CAR" },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCarWash,
                        contentDescription = null,
                        tint = if (riderType == "CAR") Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Car Rider",
                        fontWeight = FontWeight.Bold,
                        color = if (riderType == "CAR") Color.White else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty() || (isRegisterMode && name.isEmpty())) {
                        Toast.makeText(context, "Please fill in all details", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalName = if (name.isEmpty()) "Standard Rider" else name
                    onLoginSuccess(finalName, email, riderType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isRegisterMode) "Create Rider Account" else "Sign In", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle mode link
            Text(
                text = if (isRegisterMode) "Already have an account? Sign In" else "New Talabat Rider? Create Profile",
                color = TalabatOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { isRegisterMode = !isRegisterMode }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Social Logins Divider
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
                Text("OR CONTINUE WITH", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mock Google Login
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable {
                            onLoginSuccess("Google Rider", "google.rider@talabat.qa", riderType)
                            Toast.makeText(context, "Logged in with Google ID", Toast.LENGTH_SHORT).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Google Link", color = Color.LightGray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // Apple/Guest login
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable {
                            onLoginSuccess("Guest Rider", "guest.rider@talabat.qa", riderType)
                            Toast.makeText(context, "Continuing as Guest Rider", Toast.LENGTH_SHORT).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Guest Mode", color = Color.LightGray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

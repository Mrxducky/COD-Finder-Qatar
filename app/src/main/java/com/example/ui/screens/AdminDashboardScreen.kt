package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodMachineEntity
import com.example.ui.viewmodel.CodViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: CodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allMachines by viewModel.allMachines.collectAsState()

    // Form variable states for ADDING/EDITING
    var formId by remember { mutableStateOf("") }
    var formName by remember { mutableStateOf("") }
    var formBranch by remember { mutableStateOf("") }
    var formArea by remember { mutableStateOf("") }
    var formLat by remember { mutableStateOf("") }
    var formLng by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Vodafone") } // Options: Vodafone
    var formIsBikeSelected by remember { mutableStateOf(true) }
    var formIsCarSelected by remember { mutableStateOf(true) }

    // CSV text area state
    var csvInputText by remember { mutableStateOf("") }

    val sampleCsv = """#machine_id,machine_name,branch_name,area,latitude,longitude,google_maps_url,category
VF-LUS-50,Vodafone Smart Vendor,Lusail Boulevard,Lusail,25.4211,51.5303,https://maps.google.com,Vodafone,true,true
VF-SWQ-51,Vodafone Box,Souq Waqif Art Center,Souq Waqif,25.2872,51.5340,https://maps.google.com,Vodafone,true,false
VF-AWK-52,Vodafone CDM,Al Wakrah Souq,Al Wakrah,25.1712,51.6111,https://maps.google.com,Vodafone,false,true
VF-CCD-53,Vodafone CDM,City Center Tower B,West Bay,25.3268,51.5290,https://maps.google.com,Vodafone,true,true"""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dashboard Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF3F51B5).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF3F51B5))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "QATAR SYSTEM PORTAL",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = Color(0xFF3F51B5),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Web Admin Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Analytics Cards (Active Users, Turnarounds, Deposits)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val stats = listOf(
                Triple("Active Riders", "384", Icons.Default.People),
                Triple("Total Kiosks", allMachines.size.toString(), Icons.Default.LocalHospital),
                Triple("Daily Deposits", "16,420 QAR", Icons.Default.AccountBalance)
            )

            stats.forEach { stat ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = stat.third, contentDescription = null, tint = TalabatOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = stat.second, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = stat.first, fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Visual Popularity Analytics Bar Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "POPULAR MACHINE DEPOSIT TRAFFIC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                val sortedPopular = allMachines.sortedByDescending { it.popularity }.take(4)
                if (sortedPopular.isEmpty()) {
                    Text("No machines available.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                } else {
                    val maxTraffic = sortedPopular.first().popularity.coerceAtLeast(1)
                    sortedPopular.forEach { mach ->
                        val pct = mach.popularity.toFloat() / maxTraffic
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${mach.machineName} (${mach.area})", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(text = "${mach.popularity} deposits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TalabatOrange)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(TalabatOrange)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Add/Edit Machine CRUD Form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADD / EDIT LOCATION (CRUD)",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = formId,
                    onValueChange = { formId = it },
                    label = { Text("Machine ID (e.g., SSM-99)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = formName,
                    onValueChange = { formName = it },
                    label = { Text("Machine Name (e.g., Vodafone Smart Kiosk)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formBranch,
                        onValueChange = { formBranch = it },
                        label = { Text("Branch") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = formArea,
                        onValueChange = { formArea = it },
                        label = { Text("Area (e.g. West Bay)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formLat,
                        onValueChange = { formLat = it },
                        label = { Text("Latitude") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = formLng,
                        onValueChange = { formLng = it },
                        label = { Text("Longitude") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(bottom = 8.dp)
                    )
                }

                // Category selection dropdown
                var showCatDrop by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Button(
                        onClick = { showCatDrop = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category: $formCategory", color = MaterialTheme.colorScheme.onSurface)
                    }
                    DropdownMenu(expanded = showCatDrop, onDismissRequest = { showCatDrop = false }) {
                        val cats = listOf("Vodafone")
                        cats.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { formCategory = cat; showCatDrop = false })
                        }
                    }
                }

                // Friendly toggles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = formIsBikeSelected, onCheckedChange = { formIsBikeSelected = it })
                        Text("Bike Friendly", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = formIsCarSelected, onCheckedChange = { formIsCarSelected = it })
                        Text("Car Friendly", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Create/Update Button
                    Button(
                        onClick = {
                            if (formId.isEmpty() || formName.isEmpty() || formBranch.isEmpty() || formArea.isEmpty()) {
                                Toast.makeText(context, "Please fill required text spaces", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val latVal = formLat.toDoubleOrNull() ?: 25.2854
                            val lngVal = formLng.toDoubleOrNull() ?: 51.5310

                            viewModel.addOrUpdateMachine(
                                id = formId,
                                name = formName,
                                branch = formBranch,
                                area = formArea,
                                lat = latVal,
                                lng = lngVal,
                                url = "https://maps.google.com/?q=$latVal,$lngVal",
                                cat = formCategory,
                                isBike = formIsBikeSelected,
                                isCar = formIsCarSelected
                            )

                            // Reset form fields
                            formId = ""
                            formName = ""
                            formBranch = ""
                            formArea = ""
                            formLat = ""
                            formLng = ""
                            Toast.makeText(context, "Machine ID Saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save/Update", color = Color.White)
                    }

                    // Delete button
                    Button(
                        onClick = {
                            if (formId.isEmpty()) {
                                Toast.makeText(context, "Insert ID to delete", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.deleteMachine(formId)
                            formId = ""
                            Toast.makeText(context, "Machine Removed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertDanger),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete ID", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // EXCEL/CSV Bulk Upload Area
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BULK CSV IMPORT GATEWAY",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Paste comma-separated rows of machine attributes below to override.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = csvInputText,
                    onValueChange = { csvInputText = it },
                    placeholder = { Text("machine_id, machine_name, branch, area, lat, lng, url, category") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = TalabatOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Demo CSV injector
                    Button(
                        onClick = {
                            csvInputText = sampleCsv
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Load Sample CSV", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }

                    // Run Import Parser
                    Button(
                        onClick = {
                            if (csvInputText.isEmpty()) {
                                Toast.makeText(context, "Csv text field is empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.importCSVBulk(csvInputText) { count ->
                                Toast.makeText(context, "$count Machines Imported!", Toast.LENGTH_SHORT).show()
                                csvInputText = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TalabatOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Proceed Import", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

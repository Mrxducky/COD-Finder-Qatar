package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.CodMachineEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "cod_finder_qatar.db"
    ).build()

    private val userDao = db.userDao()
    private val codMachineDao = db.codMachineDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Run seed operations asynchronously
        repositoryScope.launch {
            seedInitialData()
        }
    }

    // --- Users ---
    fun getUser(userId: String = "default_user"): Flow<UserEntity?> {
        return userDao.getUser(userId)
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    // --- Machines ---
    fun getAllMachines(): Flow<List<CodMachineEntity>> {
        return codMachineDao.getAllMachines()
    }

    suspend fun insertMachine(machine: CodMachineEntity) = withContext(Dispatchers.IO) {
        codMachineDao.insertMachine(machine)
    }

    suspend fun deleteMachine(machineId: String) = withContext(Dispatchers.IO) {
        codMachineDao.deleteMachineById(machineId)
    }

    suspend fun clearAllMachines() = withContext(Dispatchers.IO) {
        codMachineDao.clearAll()
    }

    suspend fun bulkImportCsv(csvText: String): Int = withContext(Dispatchers.IO) {
        // Format of CSV: machine_id,machine_name,branch_name,area,latitude,longitude,google_maps_url,category,isBikeFriendly,isCarFriendly
        val lines = csvText.lines()
        var importCount = 0
        val importedList = mutableListOf<CodMachineEntity>()
        
        for (line in lines) {
            val tokens = line.split(",").map { it.trim() }
            if (tokens.size >= 8) {
                val id = tokens[0]
                if (id.isEmpty() || id.startsWith("#") || id.startsWith("machine_id")) continue
                try {
                    val name = tokens[1]
                    val branch = tokens[2]
                    val area = tokens[3]
                    val lat = tokens[4].toDoubleOrNull() ?: 25.2854
                    val lng = tokens[5].toDoubleOrNull() ?: 51.5310
                    val url = tokens[6]
                    val cat = tokens[7]
                    val isBike = tokens.getOrNull(8).toBoolean() ?: true
                    val isCar = tokens.getOrNull(9).toBoolean() ?: true
                    
                    importedList.add(
                        CodMachineEntity(
                            machineId = id,
                            machineName = name,
                            branchName = branch,
                            area = area,
                            latitude = lat,
                            longitude = lng,
                            googleMapsUrl = url,
                            category = cat,
                            isBikeFriendly = isBike,
                            isCarFriendly = isCar
                        )
                    )
                    importCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (importedList.isNotEmpty()) {
            codMachineDao.insertMachines(importedList)
        }
        return@withContext importCount
    }

    private suspend fun seedInitialData() {
        // 1. Seed Default User if not present
        val existingUser = userDao.getUser("default_user").firstOrNull()
        if (existingUser == null) {
            userDao.insertUser(
                UserEntity(
                    userId = "default_user",
                    name = "Talabat Ranger",
                    riderType = "BIKE",
                    email = "ranger.qatar@talabat-riders.qa",
                    currentCash = 120.0,
                    favoriteMachines = "SSM-MOQ-01,QNB-VIL-04" // Mock favorites to begin with
                )
            )
        }

        // 2. Seed Default Machines if database is empty or has been cleared
        val machinesFlow = codMachineDao.getAllMachines()
        val currentMachines = machinesFlow.firstOrNull() ?: emptyList()
        if (currentMachines.isEmpty()) {
            val initialMachines = listOf(
                CodMachineEntity(
                    machineId = "SSM-MOQ-01",
                    machineName = "Ooredoo Self Service Machine",
                    branchName = "Mall of Qatar Kiosk",
                    area = "Al Rayyan",
                    latitude = 25.3223,
                    longitude = 51.3488,
                    googleMapsUrl = "https://maps.google.com/?q=25.3223,51.3488",
                    category = "Ooredoo",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 45
                ),
                CodMachineEntity(
                    machineId = "SSM-CCD-02",
                    machineName = "Ooredoo Self Service Machine",
                    branchName = "City Center Doha (Ground)",
                    area = "West Bay",
                    latitude = 25.3262,
                    longitude = 51.5303,
                    googleMapsUrl = "https://maps.google.com/?q=25.3262,51.5303",
                    category = "Ooredoo",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 92
                ),
                CodMachineEntity(
                    machineId = "VF-LM-03",
                    machineName = "Vodafone Smart Kiosk",
                    branchName = "Landmark Mall Food Court",
                    area = "Al Gharrafa",
                    latitude = 25.3371,
                    longitude = 51.4795,
                    googleMapsUrl = "https://maps.google.com/?q=25.3371,51.4795",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 28
                ),
                CodMachineEntity(
                    machineId = "QNB-VIL-04",
                    machineName = "QNB Cash Deposit Machine",
                    branchName = "Villaggio Mall Elite Branch",
                    area = "Baaya",
                    latitude = 25.2608,
                    longitude = 51.4437,
                    googleMapsUrl = "https://maps.google.com/?q=25.2608,51.4437",
                    category = "QNB",
                    isBikeFriendly = false, // Security locks at VIP entrances (hard to park bikes)
                    isCarFriendly = true,
                    popularity = 120
                ),
                CodMachineEntity(
                    machineId = "CBQ-GM-05",
                    machineName = "Commercial Bank CDM",
                    branchName = "Gulf Mall Level 1",
                    area = "Al Gharrafa",
                    latitude = 25.3340,
                    longitude = 51.4770,
                    googleMapsUrl = "https://maps.google.com/?q=25.3340,51.4770",
                    category = "CBQ",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 34
                ),
                CodMachineEntity(
                    machineId = "SSM-AWK-06",
                    machineName = "Ooredoo Smart Kiosk",
                    branchName = "Al Wakrah Main Branch",
                    area = "Al Wakrah",
                    latitude = 25.1764,
                    longitude = 51.6033,
                    googleMapsUrl = "https://maps.google.com/?q=25.1764,51.6033",
                    category = "Ooredoo",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 54
                ),
                CodMachineEntity(
                    machineId = "QNB-HIA-07",
                    machineName = "QNB Deposit Machine",
                    branchName = "Hamad Intl Airport Arrivals",
                    area = "Airport Area",
                    latitude = 25.2631,
                    longitude = 51.6111,
                    googleMapsUrl = "https://maps.google.com/?q=25.2631,51.6111",
                    category = "QNB",
                    isBikeFriendly = false,
                    isCarFriendly = true,
                    popularity = 10
                ),
                CodMachineEntity(
                    machineId = "VF-DRG-08",
                    machineName = "Vodafone Smart Box",
                    branchName = "Lulu Hypermarket D-Ring Road",
                    area = "Nuaija",
                    latitude = 25.2575,
                    longitude = 51.5544,
                    googleMapsUrl = "https://maps.google.com/?q=25.2575,51.5544",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 75
                ),
                CodMachineEntity(
                    machineId = "CBQ-PVM-09",
                    machineName = "Commercial Bank CDM",
                    branchName = "Place Vendôme Mall Lusail",
                    area = "Lusail",
                    latitude = 25.4208,
                    longitude = 51.5218,
                    googleMapsUrl = "https://maps.google.com/?q=25.4208,51.5218",
                    category = "CBQ",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 63
                ),
                CodMachineEntity(
                    machineId = "SSM-MAN-10",
                    machineName = "Ooredoo SSM",
                    branchName = "Al Meera Mansoura",
                    area = "Al Mansoura",
                    latitude = 25.2789,
                    longitude = 51.5431,
                    googleMapsUrl = "https://maps.google.com/?q=25.2789,51.5431",
                    category = "Ooredoo",
                    isBikeFriendly = true,
                    isCarFriendly = false, // Heavy traffic, parking impossible for cars
                    popularity = 145
                ),
                CodMachineEntity(
                    machineId = "SSM-AKM-11",
                    machineName = "Ooredoo Kiosk",
                    branchName = "Al Khor Mall",
                    area = "Al Khor",
                    latitude = 25.6888,
                    longitude = 51.5033,
                    googleMapsUrl = "https://maps.google.com/?q=25.6888,51.5033",
                    category = "Ooredoo",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 19
                ),
                CodMachineEntity(
                    machineId = "QNB-IND-12",
                    machineName = "QNB CDM",
                    branchName = "Industrial Area Street 24",
                    area = "Industrial Area",
                    latitude = 25.1952,
                    longitude = 51.4112,
                    googleMapsUrl = "https://maps.google.com/?q=25.1952,51.4112",
                    category = "QNB",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 87
                )
            )
            codMachineDao.insertMachines(initialMachines)
        }
    }
}

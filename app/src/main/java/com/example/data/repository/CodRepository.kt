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
                    favoriteMachines = "10000135,10000141" // Mock favorites with real IDs
                )
            )
        }

        // 2. Seed Default Machines if database is empty or has non-Vodafone machines
        val machinesFlow = codMachineDao.getAllMachines()
        val currentMachines = machinesFlow.firstOrNull() ?: emptyList()
        val hasNonVodafone = currentMachines.any { it.category != "Vodafone" }
        if (currentMachines.size < 30 || hasNonVodafone) {
            codMachineDao.clearAll()
            val initialMachines = listOf(
                CodMachineEntity(
                    machineId = "10000135",
                    machineName = "KeyBS Merchant - Vodafone",
                    branchName = "Bin Omran Branch",
                    area = "Bin Omran",
                    latitude = 25.3090,
                    longitude = 51.5030,
                    googleMapsUrl = "https://maps.app.goo.gl/r7JNNza8Vt18HLtJ9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 50
                ),
                CodMachineEntity(
                    machineId = "10000140",
                    machineName = "Fresh Way Supermarket - Vodafone",
                    branchName = "Al Rayyan Branch",
                    area = "Al Rayyan",
                    latitude = 25.2950,
                    longitude = 51.4250,
                    googleMapsUrl = "https://maps.app.goo.gl/kA3MRiBFLtp4pq3T6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 65
                ),
                CodMachineEntity(
                    machineId = "10000141",
                    machineName = "Zadak Hypermarket - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.1950,
                    longitude = 51.4110,
                    googleMapsUrl = "https://goo.gl/maps/bn8WJx1AxRbPD8Ar6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 95
                ),
                CodMachineEntity(
                    machineId = "10000142",
                    machineName = "LU Grocery - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.1960,
                    longitude = 51.4120,
                    googleMapsUrl = "https://goo.gl/maps/8fut43DTvXjzqBaE9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 45
                ),
                CodMachineEntity(
                    machineId = "10000144",
                    machineName = "Khayrat Al Doha Hypermarket - Vodafone",
                    branchName = "Mamoura Branch",
                    area = "Al Mamoura",
                    latitude = 25.2500,
                    longitude = 51.4950,
                    googleMapsUrl = "https://goo.gl/maps/9scqJvk5i3BG6CuY6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 75
                ),
                CodMachineEntity(
                    machineId = "10000149",
                    machineName = "Point Eight Supermarket - Vodafone",
                    branchName = "Al Wukair Branch",
                    area = "Al Wukair",
                    latitude = 25.1380,
                    longitude = 51.5700,
                    googleMapsUrl = "https://maps.app.goo.gl/4CorJYkmoEjpqR9c9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 85
                ),
                CodMachineEntity(
                    machineId = "10000153",
                    machineName = "Prime Touch Delivery - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.1970,
                    longitude = 51.4130,
                    googleMapsUrl = "https://waze.com/ul/hthkx5p855",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 40
                ),
                CodMachineEntity(
                    machineId = "10000155",
                    machineName = "Cassava Mini Mart - Vodafone",
                    branchName = "Al Saad Branch",
                    area = "Al Sadd",
                    latitude = 25.2850,
                    longitude = 51.5020,
                    googleMapsUrl = "https://goo.gl/maps/wFZ4UWUJMMyKpNZv5",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 70
                ),
                CodMachineEntity(
                    machineId = "10000156",
                    machineName = "Al Abid Supermarket - Vodafone",
                    branchName = "Al Khor Branch",
                    area = "Al Khor",
                    latitude = 25.6880,
                    longitude = 51.5030,
                    googleMapsUrl = "https://maps.app.goo.gl/LuxwVrj1dpLwANZL8",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 60
                ),
                CodMachineEntity(
                    machineId = "10000159",
                    machineName = "Dafna Prime Mart - Vodafone",
                    branchName = "Dafna Branch",
                    area = "Dafna",
                    latitude = 25.3200,
                    longitude = 51.5300,
                    googleMapsUrl = "https://maps.app.goo.gl/r9vtQPJSNLFNX96p8",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 80
                ),
                CodMachineEntity(
                    machineId = "10000160",
                    machineName = "Madeena Hypermarket - Vodafone",
                    branchName = "Wakra Branch",
                    area = "Al Wakrah",
                    latitude = 25.1764,
                    longitude = 51.6033,
                    googleMapsUrl = "https://goo.gl/maps/v5aaGiHGVVuCjuL38",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 110
                ),
                CodMachineEntity(
                    machineId = "10000164",
                    machineName = "Al Shariyas Food Center - Vodafone",
                    branchName = "Al Saad Branch",
                    area = "Al Sadd",
                    latitude = 25.2860,
                    longitude = 51.5040,
                    googleMapsUrl = "https://goo.gl/maps/B9ysVozHbCorZuhBA",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 68
                ),
                CodMachineEntity(
                    machineId = "10000165",
                    machineName = "Wadi Al Dahab - Vodafone",
                    branchName = "Freej Abdul Azeez Branch",
                    area = "Freej Abdul Azeez",
                    latitude = 25.2750,
                    longitude = 51.5380,
                    googleMapsUrl = "https://goo.gl/maps/FzvMpcUvYTXArkQF7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 58
                ),
                CodMachineEntity(
                    machineId = "10000166",
                    machineName = "Muraikh Food Complex - Vodafone",
                    branchName = "Muaither Branch",
                    area = "Muaither",
                    latitude = 25.2900,
                    longitude = 51.4000,
                    googleMapsUrl = "https://goo.gl/maps/QxQBknV6eHHHi8K1A",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 82
                ),
                CodMachineEntity(
                    machineId = "10000167",
                    machineName = "Mobile Point - Vodafone",
                    branchName = "Wakra Branch",
                    area = "Al Wakrah",
                    latitude = 25.1780,
                    longitude = 51.6050,
                    googleMapsUrl = "https://goo.gl/maps/tX3tzXWTEoZk6afj7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 73
                ),
                CodMachineEntity(
                    machineId = "10000168",
                    machineName = "Al Hamed Grocery - Vodafone",
                    branchName = "Najma Branch",
                    area = "Najma",
                    latitude = 25.2760,
                    longitude = 51.5430,
                    googleMapsUrl = "https://goo.gl/maps/cTPwzQJCkwp54Qpq5",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 88
                ),
                CodMachineEntity(
                    machineId = "10000169",
                    machineName = "Metro Mart - Vodafone",
                    branchName = "Souq Al Jaber Branch",
                    area = "Souq Al Jaber",
                    latitude = 25.2870,
                    longitude = 51.5350,
                    googleMapsUrl = "https://maps.app.goo.gl/Mt5AYkuGCsXcQigV8",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 90
                ),
                CodMachineEntity(
                    machineId = "10000171",
                    machineName = "Wusail Shopping Centre - Vodafone",
                    branchName = "Um Salal Ali Branch",
                    area = "Um Salal Ali",
                    latitude = 25.4200,
                    longitude = 51.4000,
                    googleMapsUrl = "https://maps.app.goo.gl/AF31MsdtqxjwUJDm6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 47
                ),
                CodMachineEntity(
                    machineId = "10000172",
                    machineName = "Sanniya Food City - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.1980,
                    longitude = 51.4140,
                    googleMapsUrl = "https://maps.app.goo.gl/mFy3X27RC1AkFQou5",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 115
                ),
                CodMachineEntity(
                    machineId = "10000175",
                    machineName = "Al Hadoud Supermarket - Vodafone",
                    branchName = "Al Khor Branch",
                    area = "Al Khor",
                    latitude = 25.6900,
                    longitude = 51.5050,
                    googleMapsUrl = "https://goo.gl/maps/YkQroFZcndARTu7f9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 52
                ),
                CodMachineEntity(
                    machineId = "10000180",
                    machineName = "Al Danube Food Stuff - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.1990,
                    longitude = 51.4150,
                    googleMapsUrl = "https://maps.app.goo.gl/mMGvBRX9CDP9UomZ7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 93
                ),
                CodMachineEntity(
                    machineId = "10000181",
                    machineName = "Rawiya Supermarket - Vodafone",
                    branchName = "Al Kharaitiyat Branch",
                    area = "Al Kharaitiyat",
                    latitude = 25.3800,
                    longitude = 51.4600,
                    googleMapsUrl = "https://goo.gl/maps/7gduuobToyPgCgQZ6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 69
                ),
                CodMachineEntity(
                    machineId = "10000182",
                    machineName = "Info Madeena Mobile - Vodafone",
                    branchName = "Madeena Khalifa Branch",
                    area = "Madina Khalifa",
                    latitude = 25.3180,
                    longitude = 51.4850,
                    googleMapsUrl = "https://goo.gl/maps/foXf53BdmhWYiH3i7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 76
                ),
                CodMachineEntity(
                    machineId = "10000183",
                    machineName = "Go Do Delivery-1 - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.2010,
                    longitude = 51.4160,
                    googleMapsUrl = "https://maps.app.goo.gl/dNTckpE2uWWW9bXr7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 61
                ),
                CodMachineEntity(
                    machineId = "10000185",
                    machineName = "Al Ikhlas Supermarket - Vodafone",
                    branchName = "Al Rayyan Branch",
                    area = "Al Rayyan",
                    latitude = 25.2970,
                    longitude = 51.4270,
                    googleMapsUrl = "https://maps.app.goo.gl/W3LFxmbUSDfDzoQu8",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 71
                ),
                CodMachineEntity(
                    machineId = "10000187",
                    machineName = "Mishal Supermarket - Vodafone",
                    branchName = "Al Saad Branch",
                    area = "Al Sadd",
                    latitude = 25.2870,
                    longitude = 51.5050,
                    googleMapsUrl = "https://maps.app.goo.gl/fxJdJ1BDHqXDNf8t5",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 78
                ),
                CodMachineEntity(
                    machineId = "10000193",
                    machineName = "Mr.Delivery - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.2020,
                    longitude = 51.4170,
                    googleMapsUrl = "https://goo.gl/maps/PVzhkiSaubiMNdC78",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 84
                ),
                CodMachineEntity(
                    machineId = "10000194",
                    machineName = "Falcon Big Mart - Vodafone",
                    branchName = "Bin Omran Branch",
                    area = "Bin Omran",
                    latitude = 25.3110,
                    longitude = 51.5050,
                    googleMapsUrl = "https://maps.app.goo.gl/JTuiEmcdZK2ybdcu7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 89
                ),
                CodMachineEntity(
                    machineId = "10000195",
                    machineName = "Blue Diamond Supermarket - Vodafone",
                    branchName = "Muglina Branch",
                    area = "Al Muglina",
                    latitude = 25.2800,
                    longitude = 51.5500,
                    googleMapsUrl = "https://maps.app.goo.gl/oDy9iPSK8yiMLWdi8",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 92
                ),
                CodMachineEntity(
                    machineId = "10000197",
                    machineName = "Jaal Phone - Vodafone",
                    branchName = "Al Khor Branch",
                    area = "Al Khor",
                    latitude = 25.6920,
                    longitude = 51.5070,
                    googleMapsUrl = "https://goo.gl/maps/J3QQYuVU3aKMGXwYA",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 55
                ),
                CodMachineEntity(
                    machineId = "10000198",
                    machineName = "Cosmo Supermarket - Vodafone",
                    branchName = "Ain Khalid Branch",
                    area = "Ain Khalid",
                    latitude = 25.2400,
                    longitude = 51.4400,
                    googleMapsUrl = "https://goo.gl/maps/KMi1o6PZDudHgjvPA",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 63
                ),
                CodMachineEntity(
                    machineId = "10000199",
                    machineName = "Fresh Choice Supermarket - Vodafone",
                    branchName = "Al Aziziya Branch",
                    area = "Al Aziziya",
                    latitude = 25.2550,
                    longitude = 51.4550,
                    googleMapsUrl = "https://maps.app.goo.gl/Y9bXSRRRQRQNPvh2A",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 75
                ),
                CodMachineEntity(
                    machineId = "10000201",
                    machineName = "Green Fresh Supermarket - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.2030,
                    longitude = 51.4180,
                    googleMapsUrl = "https://maps.app.goo.gl/zwVKVTAPf5XsYANT7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 67
                ),
                CodMachineEntity(
                    machineId = "10000202",
                    machineName = "Marzam Supermarket - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.2040,
                    longitude = 51.4190,
                    googleMapsUrl = "https://maps.app.goo.gl/QhZ6fTvYJAHWceibA",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 71
                ),
                CodMachineEntity(
                    machineId = "10000204",
                    machineName = "Grandex Supermarket - Vodafone",
                    branchName = "Al Azizia Branch",
                    area = "Al Aziziya",
                    latitude = 25.2570,
                    longitude = 51.4570,
                    googleMapsUrl = "https://goo.gl/maps/NhcbxMKV2danYDQe9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 81
                ),
                CodMachineEntity(
                    machineId = "10000211",
                    machineName = "Bathool Supermarket - Vodafone",
                    branchName = "Lusail Branch",
                    area = "Lusail",
                    latitude = 25.4215,
                    longitude = 51.5225,
                    googleMapsUrl = "https://maps.app.goo.gl/PubPTcHtkb8EBeoN7",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 53
                ),
                CodMachineEntity(
                    machineId = "10000212",
                    machineName = "Swift Delivery-1 - Vodafone",
                    branchName = "Al Wukair Branch-Ezdan 29",
                    area = "Al Wukair",
                    latitude = 25.1400,
                    longitude = 51.5720,
                    googleMapsUrl = "https://goo.gl/maps/xBcoQB8XW9nnuK548",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 46
                ),
                CodMachineEntity(
                    machineId = "10000213",
                    machineName = "Al Fajer Shopping Center - Vodafone",
                    branchName = "Najma Branch",
                    area = "Najma",
                    latitude = 25.2780,
                    longitude = 51.5450,
                    googleMapsUrl = "https://goo.gl/maps/bFq46uZt9L98Ge539",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 125
                ),
                CodMachineEntity(
                    machineId = "10000221",
                    machineName = "Masters Supermarket - Vodafone",
                    branchName = "Simaisma Branch",
                    area = "Simaisma",
                    latitude = 25.5700,
                    longitude = 51.4880,
                    googleMapsUrl = "https://maps.app.goo.gl/AtRFG6cN8p9szFpA6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 38
                ),
                CodMachineEntity(
                    machineId = "10000222",
                    machineName = "Deshi Taza Hypermarket - Vodafone",
                    branchName = "Al Aziziya Branch",
                    area = "Al Aziziya",
                    latitude = 25.2590,
                    longitude = 51.4590,
                    googleMapsUrl = "https://maps.app.goo.gl/sp3MGcDxGpZhH8ss5",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 104
                ),
                CodMachineEntity(
                    machineId = "10000224",
                    machineName = "Clear Zone Supermarket - Vodafone",
                    branchName = "Al Kheesa Branch",
                    area = "Al Kheesa",
                    latitude = 25.3850,
                    longitude = 51.4650,
                    googleMapsUrl = "https://goo.gl/maps/CFtegk5cJVDGvFes9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 66
                ),
                CodMachineEntity(
                    machineId = "10000225",
                    machineName = "Shop Well Supermarket - Vodafone",
                    branchName = "Sanniya Branch",
                    area = "Sanniya",
                    latitude = 25.2060,
                    longitude = 51.4210,
                    googleMapsUrl = "https://maps.app.goo.gl/kHpWgLM7jWenpDde6",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 91
                ),
                CodMachineEntity(
                    machineId = "10000227",
                    machineName = "Garden Fresh Supermarket - Vodafone",
                    branchName = "Muntazah Branch",
                    area = "Al Muntazah",
                    latitude = 25.2750,
                    longitude = 51.5150,
                    googleMapsUrl = "https://goo.gl/maps/MGwEpzhbq7aN8YAC9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 72
                ),
                CodMachineEntity(
                    machineId = "1000418",
                    machineName = "HFM Supermarket - Vodafone",
                    branchName = "Ain Khalid Branch",
                    area = "Ain Khalid",
                    latitude = 25.2420,
                    longitude = 51.4420,
                    googleMapsUrl = "https://maps.app.goo.gl/Ey7j7yxWDf19UYbBA",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 83
                ),
                CodMachineEntity(
                    machineId = "1000419",
                    machineName = "Nana Food Mart - Vodafone",
                    branchName = "UmSalal Ali Branch",
                    area = "Um Salal Ali",
                    latitude = 25.4220,
                    longitude = 51.4020,
                    googleMapsUrl = "https://maps.app.goo.gl/JyajYxxscgdsiHRj9",
                    category = "Vodafone",
                    isBikeFriendly = true,
                    isCarFriendly = true,
                    popularity = 44
                )
            )
            codMachineDao.insertMachines(initialMachines)
        }
    }
}

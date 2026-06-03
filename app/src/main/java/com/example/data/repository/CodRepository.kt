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

        // 2. Seed Default Machines if database has fewer than 130 or non-Vodafone machines
        val machinesFlow = codMachineDao.getAllMachines()
        val currentMachines = machinesFlow.firstOrNull() ?: emptyList()
        val hasNonVodafone = currentMachines.any { it.category != "Vodafone" }
        if (currentMachines.size < 130 || hasNonVodafone) {
            codMachineDao.clearAll()
            
            val rawLines = listOf(
                "1\tKeyBS Merchant\tBin Omran Branch-KeyBS Merchant\t10000135\thttps://maps.app.goo.gl/r7JNNza8Vt18HLtJ9",
                "2\tFresh Way Supermarket\tAl Rayyan Branch-Fresh Way Supermarket\t10000140\thttps://maps.app.goo.gl/kA3MRiBFLtp4pq3T6",
                "3\tZadak Hypermarket\tSanniya Branch-Zadak Hypermarket\t10000141\thttps://goo.gl/maps/bn8WJx1AxRbPD8Ar6",
                "4\tLU Grocery\tSanniya Branch-LU Grocery\t10000142\thttps://goo.gl/maps/8fut43DTvXjzqBaE9",
                "5\tKhayrat Al Doha Hypermarket\tMamoura Banch-Khayrat Al Doha\t10000144\thttps://goo.gl/maps/9scqJvk5i3BG6CuY6",
                "6\tPoint Eight Supermarket\tAl Wukair Branch-Point Eight Supermarket\t10000149\thttps://maps.app.goo.gl/4CorJYkmoEjpqR9c9",
                "7\tPrime Touch Delivery\tSanniya Branch-Prime Touch Delivery\t10000153\thttps://waze.com/ul/hthkx5p855",
                "8\tCassava Mini Mart\tAl Saad Branch-Cassava Mini Mart\t10000155\thttps://goo.gl/maps/wFZ4UWUJMMyKpNZv5",
                "9\tAl Abid Supermarket\tAl Khor Branch-Al Abid Supermarket\t10000156\thttps://maps.app.goo.gl/LuxwVrj1dpLwANZL8",
                "10\tDafna Prime Mart\tDafna Branch- Dafna Prime Mart\t10000159\thttps://maps.app.goo.gl/r9vtQPJSNLFNX96p8",
                "11\tMadeena Hypermarket\tWakra Branch- Madeena Hypermarket\t10000160\thttps://goo.gl/maps/v5aaGiHGVVuCjuL38",
                "12\tAl Shariyas Food Center\tAl Saad Branch-Al Shariyas Food Center\t10000164\thttps://goo.gl/maps/B9ysVozHbCorZuhBA",
                "13\tWadi Al Dahab\tFreej Abdul Azeez Branch-Wadi Al Dahab\t10000165\thttps://goo.gl/maps/FzvMpcUvYTXArkQF7",
                "14\tMuraikh Food Complex\tMuaither Branch-Muraikh Food Complex\t10000166\thttps://goo.gl/maps/QxQBknV6eHHHi8K1A",
                "15\tMobile Point\tWakra Branch-Mobile Point\t10000167\thttps://goo.gl/maps/tX3tzXWTEoZk6afj7",
                "16\tAl Hamed Grocery\tNajma Branch-Al Hamed Grocery\t10000168\thttps://goo.gl/maps/cTPwzQJCkwp54Qpq5",
                "17\tMetro Mart\tSouq Al Jaber Branch-Metro Mart\t10000169\thttps://maps.app.goo.gl/Mt5AYkuGCsXcQigV8",
                "18\tWusail Shopping Centre\tUm Salal Ali Branch-Wusail Shopping Centre\t10000171\thttps://maps.app.goo.gl/AF31MsdtqxjwUJDm6",
                "19\tSanniya Food City Market\tSanniya Branch-Sanniya Food City Market\t10000172\thttps://maps.app.goo.gl/mFy3X27RC1AkFQou5",
                "20\tAl Hadoud Supermarket\tAl Khor Branch-Al Hadoud Supermarket\t10000175\thttps://goo.gl/maps/YkQroFZcndARTu7f9",
                "21\t\t\t10000178\t",
                "22\tAl Danube Food Stuff\tSanniya Branch- Al Danube Food Stuff\t10000180\thttps://maps.app.goo.gl/mMGvBRX9CDP9UomZ7",
                "23\tRawiya Supermarket\tAl Kharaitiyat Branch Rawiya Supermarket\t10000181\thttps://goo.gl/maps/7gduuobToyPgCgQZ6",
                "24\tInfo Madeena Mobile\tMadeena Khalifa Branch-Info Madeena Mobile\t10000182\thttps://goo.gl/maps/foXf53BdmhWYiH3i7",
                "25\tGo Do Delivery-1\tSanniya Branch-Go Do Delivery-1\t10000183\thttps://maps.app.goo.gl/dNTckpE2uWWW9bXr7",
                "26\tAl Ikhlas Supermarket\tAl Rayyan Branch-Al Ikhlas Supermarket\t10000185\thttps://maps.app.goo.gl/W3LFxmbUSDfDzoQu8",
                "27\tMishal Supermarket\tAl Saad Branch-Mishal Supermarket\t10000187\thttps://maps.app.goo.gl/fxJdJ1BDHqXDNf8t5",
                "28\tMr.Delivery\tSanniya Branch-Mr.Delivery\t10000193\thttps://goo.gl/maps/PVzhkiSaubiMNdC78",
                "29\tFalcon Big Mart\tBin Omran Branch-Falcon Big Mart\t10000194\thttps://maps.app.goo.gl/JTuiEmcdZK2ybdcu7",
                "30\tBlue Diamond Supermarket\tMuglina Branch-Blue Diamond Supermarket\t10000195\thttps://maps.app.goo.gl/oDy9iPSK8yiMLWdi8",
                "31\tJaal Phone\tAl Khor Branch-Jaal Phone\t10000197\thttps://goo.gl/maps/J3QQYuVU3aKMGXwYA",
                "32\tCosmo Supermarket\tAin Khalid Branch-Cosmo Supermarket\t10000198\thttps://goo.gl/maps/KMi1o6PZDudHgjvPA",
                "33\tFresh Choice Supermarket\tAl Aziziya Branch-Fresh Choice Supermarket\t10000199\thttps://maps.app.goo.gl/Y9bXSRRRQRQNPvh2A",
                "34\tArbash Mobile\tWakra Branch-Arbash Mobile\t10000200\thttps://goo.gl/maps/tFncaesk1RnQ3zgX7",
                "35\tGreen Fresh Supermarket\tSanniya Branch-Green Fresh Supermarket\t10000201\thttps://maps.app.goo.gl/zwVKVTAPf5XsYANT7",
                "36\tMarzam Supermarket\tSanniya Branch-Marzam Supermarket\t10000202\thttps://maps.app.goo.gl/QhZ6fTvYJAHWceibA",
                "37\tGrandex Supermarket\tAl Azizia Branch-Grandex Supermarket\t10000204\thttps://goo.gl/maps/NhcbxMKV2danYDQe9",
                "38\tInt.Tel.Centre\tWakra Branch-Int.Tel.Centre\t10000207\thttps://goo.gl/maps/N9u51HqmyjH2",
                "39\tMalabar Supermarket\tSanniya Branch-Malabar Supermarket\t10000208\thttps://goo.gl/maps/ZK3gF15D6yDFncab6",
                "40\tBathool Supermarket\tLusail Branch-Bathool Supermarket\t10000211\thttps://maps.app.goo.gl/PubPTcHtkb8EBeoN7",
                "41\tSwift Delivery-1\tAl Wukair Branch-Ezdan 29\t10000212\thttps://goo.gl/maps/xBcoQB8XW9nnuK548",
                "42\tAl Fajer Shopping Center\tNajma Branch-Al Fajer Shopping Center\t10000213\thttps://goo.gl/maps/bFq46uZt9L98Ge539",
                "43\tSwift Delivery-4\tAl Wukair Branch- swift Ezdan 29\t10000214\thttps://goo.gl/maps/xBcoQB8XW9nnuK548",
                "44\tSeven Mart Hypermarket\tFereej Abdul Azeez Branch-Seven Mart Hypermarket\t10000215\thttps://maps.app.goo.gl/3Q3LLwMtyz6iatFj7",
                "45\tDanube Supermarket\tSanniya Branch-Danube Supermarket\t10000217\thttps://goo.gl/maps/q88JrGRgNJBP6Dfx7",
                "46\tAl Hebah Subermarket\tSanniya Branch-St16-Al Hebah Subermarket\t10000218\thttps://maps.app.goo.gl/TupouoX4VSZVQziR9",
                "47\tSwift Delivery-2\tAl Wukair Branch- Ezdan 32\t10000219\thttps://goo.gl/maps/xBcoQB8XW9nnuK548",
                "48\tSanniya Food City Market-3\tSanniya Branch-Sanniya Food City Market-3\t10000220\thttps://waze.com/ul/hthkx4ytyb",
                "49\tMasters Supermarket\tSimaisma Branch-Masters Supermarket\t10000221\thttps://maps.app.goo.gl/AtRFG6cN8p9szFpA6",
                "50\tDeshi Taza Hypermarket\tAl Aziziya Branch-Deshi Taza Hypermarket\t10000222\thttps://maps.app.goo.gl/sp3MGcDxGpZhH8ss5",
                "51\tAl Shariyas Food Center Azizyza\tAl Aziziya Branch-Al Shariyas Food Center Azizyza\t10000223\thttps://maps.app.goo.gl/gegeNBnxumrUfkoE9",
                "52\tClear Zone Supermarket\tAl Kheesa Branch- Clear Zone Supermarket\t10000224\thttps://goo.gl/maps/CFtegk5cJVDGvFes9",
                "53\tShop Well Supermarket\tSanniya Branch-Shop Well Supermarket\t10000225\thttps://maps.app.goo.gl/kHpWgLM7jWenpDde6",
                "54\tDelivex Delivery\tSaniiya Branch-Delivex Delivery\t10000226\thttps://waze.com/ul/hthkx61hw3",
                "55\tGarden Fresh Supermarket\tMuntazah Branch- Garden Fresh Supermarket\t10000227\thttps://goo.gl/maps/MGwEpzhbq7aN8YAC9",
                "56\tRosa Supermarket\tAl Saad Branch-Rosa Supermarket\t10000228\thttps://maps.app.goo.gl/CzrCSzo5ap1eVBRj7",
                "57\tSalik Delivery\tSaniiya Branch-Salik  Delivery\t10000229\thttps://maps.app.goo.gl/AgAceFcjtYD8gFQ76",
                "58\tSwift Delivery-3\tAl Wukair Branch-Swift Ezdan 38\t10000230\thttps://goo.gl/maps/g2LAWC6uXXSTYUwA7",
                "59\tFast Grocery 2\tMansoura Branch-Fast Grocery 2\t10000231\thttps://maps.app.goo.gl/Wgae38zJ77ojRJ1D8",
                "60\tMaherjeh Grocery\tThumama Branch- Maherjeh Grocery\t10000232\thttps://maps.app.goo.gl/6fPDpK4GJRNZ17Z36",
                "61\tBongo Bazar Supermarket\tKhartiyat Branch-Bongo Bazar Supermarket\t10000233\thttps://maps.app.goo.gl/v8AN8NuUMSmTB4iD7",
                "62\tNew Kenz Supermarket\tAl Aziziya Branch-New Kenz Supermarket\t10000234\thttps://maps.app.goo.gl/rUAcMytKeKX5hhin6",
                "63\tAl Mashoor Supermarket\tAl Wukair Branch-Al Mashoor Supermarket\t10000235\thttps://maps.app.goo.gl/pDGAXAh7upRHHYf1A",
                "64\tTawa Trading & Services\tOld Airport Road Branch-Tawa Trading & Services\t10000236\thttps://goo.gl/maps/iMAfKnHDdFH49gaD7",
                "65\tFood Corner Supermarket\tAbu Hamour Branch-Food Corner Supermarket\t10000237\thttps://goo.gl/maps/Hs7Um5263eiLd8tB9",
                "66\tWIFI Grocery\tSanniya Branch-WIFI Grocery\t10000238\thttps://maps.app.goo.gl/CfKUQEpznHpcne3p8",
                "67\t\t\t10000239\t",
                "68\tFriendly Mart\tNajma Branch-Friendly Mart\t10000240\thttps://goo.gl/maps/eUioBS87vagfREFz9",
                "69\tAl Kuwari Food Stuff\tMadina Khalifa Branch-Al Kuwari Food Stuff\t10000241\thttps://goo.gl/maps/Hr7eySeqtrMiC6w96",
                "70\tAl Nosour Delivery\tSanniya Branch-Al Nosour Delivery\t10000242\thttps://maps.app.goo.gl/6WTsrMBhF4vCUXNj",
                "71\tCarry Food Minimart\tBin Mahmood Branch-Carry Food Minimart\t10000243\thttps://maps.app.goo.gl/H3erDHza7FfsxL7w9",
                "72\tCmax Mobiles & Watches\tSalwa Road Branch- Cmax Mobiles & Watches\t10000244\thttps://maps.app.goo.gl/2R86BrmV5qZEhdM37",
                "73\tMonir Supermarket\tSanniya Branch-Monir Supermarket\t10000245\thttps://maps.app.goo.gl/RzkgTDHtVgHnghpK8",
                "74\tAl Markhiya Mobiles\tAl Markhiya Branch-Al Markhiya Mobiles\t10000246\thttps://goo.gl/maps/dfT4VdhdqffNwGBSA",
                "75\tSanniya Food City Market-2\tSanniya Branch-Sanniya Food City Market-2\t10000247\thttps://maps.app.goo.gl/No4fejHDL1uHXNdW9",
                "76\tSiniyari Grocery\tSanniya Branch-Siniyari Grocery\t10000248\thttps://maps.app.goo.gl/G9xLura2Hb66zhLd6",
                "77\tShahbiyah Supermarket\tAl Waab Branch- Shahbiyah Supermarket\t10000249\thttps://goo.gl/maps/NWurvSQbTXkABumb9",
                "78\tUrban Food Market-2\tNew Salata Branch-Urban Food Market-2\t10000250\thttps://maps.app.goo.gl/VVwHE7B8ivKViSGg6",
                "79\tFood City Market\tAl Saad Branch-Food City Market\t10000251\thttps://maps.app.goo.gl/no4JPtGu9HDgzRTRA",
                "80\tSelection Mart Supermarket\tHilal Branch-Selection Mart Supermarket\t10000252\thttps://goo.gl/maps/uGq4rB1Ni4Mfkuq86",
                "81\tAl Koot Supermarket\tAl Kheesa Branch-Al Koot Supermarket\t10000253\thttps://goo.gl/maps/whqnbeKRxeBVVp4u9",
                "82\tShimla Grocery\tSanniya Branch-Shimla Grocery\t10000254\thttps://goo.gl/maps/JJD6wZgKkyYh96P2A",
                "83\tSamyog Trading & Services\tSouq Al Jaber Branch-Samyog Tading & Services\t10000255\thttps://maps.app.goo.gl/9RmPXoU6jLDp1z736",
                "84\tDoor Step Supermarket\tFereej Abdul Azeez Branch-Door Step Supermarket\t10000256\thttps://maps.app.goo.gl/DE4QJRZDTY9nD68F6",
                "85\tAl Kausar Supermarket\tAl Gharaffa Branch-Al Kausar Supermarket\t10000257\thttps://maps.app.goo.gl/igXSnnE71gPJqw6K9",
                "86\tAfia Mart\tAl Rayyan Branch- Afia Mart\t10000258\thttps://maps.app.goo.gl/Wu6hxybSzwhTpH4f9",
                "87\tBeep Delivery\tSanniya Branch-Beep Delivery\t10000259\thttps://maps.app.goo.gl/xZ7hTFCV7VjqbXo27",
                "88\tSmart Shopping center\tMuntazah Branch-Smart Shopping Center\t10000260\thttps://goo.gl/maps/uT3zNS2nfimqmDGS6",
                "89\tPerfect Mobiles\tBin Omran Branch-Perfect Mobile\t10000261\thttps://maps.app.goo.gl/6tw83CtNUnV3K6BcA",
                "90\tSRS Restaurant\tAl Rayyan Branch-SRS Restaurant\t10000262\thttps://goo.gl/maps/psA7stN5AamQQhXf7",
                "91\tGo Do Delivery-3\tSanniya Branch-Go Do Delivery-3\t10000263\thttps://goo.gl/maps/ogBnqiangNzoMSo18",
                "92\tNoor Al Wakra Food Center\tWakra Branch-Noor Al Wakra Food Center\t10000264\thttps://goo.gl/maps/ZAPNT2is4JTGmkbBA",
                "93\tAl Malki Trade Center Supermarket\tAl Khor Branch-Al Malki Trade Center Supermarket\t10000265\thttps://maps.app.goo.gl/ZZoSHMSCYdtCii5q6",
                "94\tFast Grocery\tMansoura Branch-Fast Grocery\t10000266\thttps://goo.gl/maps/La5aDJm1pBP8UUmF8",
                "95\tNoorul Arab cafeteria\tSanniya Branch-Noorul Arab cafeteria\t10000267\thttps://goo.gl/maps/GTJE4TTmpzYWqFQX7",
                "96\tShams Al Madeena Supermarket\tSanniya Branch-Shams Al Madeena Supermarket\t10000268\thttps://maps.app.goo.gl/HP9LS78pLBGV5xiy8",
                "97\tIqra Supermarket\tBin Omran Branch-Iqra Supermarket\t10000269\thttps://goo.gl/maps/v2baFkkEKpLkXtMQ7",
                "98\tAl Habari Food Stuff\tAl Khor Branch-Al Habari Food Stuff\t10000270\thttps://goo.gl/maps/NhtzxMax1z5msh2QA",
                "99\tGarden Fresh Supermarket-2\tMuglina Branch-Garden Fresh Supermarket-2\t10000271\thttps://goo.gl/maps/t7ZcyxDAScun4DGs9",
                "100\tOnesa Supermarket\tSanniya Branch-Onesa Supemarket\t10000272\thttps://goo.gl/maps/cyPo1EDoSDNV6NhTA",
                "101\tAl Hammadi Supermarket\tSanniya Branch-Al Hammadi Supermarket\t10000273\thttps://maps.app.goo.gl/w3swmXvPuFZ9B3VdA",
                "102\tNew Madeena Hypermarket\tDoha Jadeed Branch-New Madeena Hypermarket\t10000274\thttps://goo.gl/maps/vWzhzuSAQNRGypGq6",
                "103\tGo Do Delivery-4\tSanniya Branch-Go Do Delivery-4\t10000275\thttps://maps.app.goo.gl/dNTckpE2uWWW9bXr7",
                "104\t\t\t10000276\t",
                "105\tJaseela Supermarket\tUmm Garn Branch-Jaseela Supermarket\t10000277\thttps://goo.gl/maps/r2JSTASj193A4jjJ7",
                "106\tDolphin Grocery\tSanniya Branch-Dolphin Grocery\t10000278\thttps://goo.gl/maps/rcxvqvBmuhPSzV5v9",
                "107\tGalaxy Food Centre\tMuntazah Branch-Galaxy Food Centre\t10000279\thttps://goo.gl/maps/zFhz9N64URHswN9S6",
                "108\tLULU AL BIDA GROCERY\tAl Kheesa Branch-LULU AL BIDA GROCERY\t10000280\thttps://goo.gl/maps/VifXsWPNb8dZ5vma9",
                "109\tRoad Track Trading\tMuglina Branch-Road Track Trading\t10000281\thttps://goo.gl/maps/kk86iDWPNZUR4EE38",
                "110\tRoad Track Trading-2\tFereej Abdul Azeez Branch-Road Track Trading-2\t10000282\thttps://maps.app.goo.gl/E38FDmGb8duCD1h16",
                "111\tAble Mart Hypermarket\tsanniya Branch-Able Mart Hypermarket\t10000283\thttps://maps.app.goo.gl/Gzpz4k2xtewzBCaRA",
                "112\tDanube Food Center\tSanniya Branch-Danube Food Centre\t10000284\thttps://goo.gl/maps/uvBZTFQ2Y4pDCDi5A",
                "113\tAl Fajer Shopping Center-2\tSanniya Branch- Al Fajer Shopping Center-2\t10000285\thttps://goo.gl/maps/bn8WJx1AxRbPD8Ar6",
                "114\tStar Max Grocery\tSanniya Branch- Star Max Grocery\t10000286\thttps://goo.gl/maps/JnhNYX1G73qfkdsDA",
                "115\tParadise Grocery\tSanniya Branch-Paradise Grocery\t10000287\thttps://goo.gl/maps/wMYrHHEGEm4Hq3jJ8",
                "116\tOruma Grocery\tSanniya Branch-Oruma Grocery\t10000288\thttps://goo.gl/maps/BsDKsMVZtwT7ANar7",
                "117\tBetter Buys Trading\tMuntazah Branch-Better Buys Trading\t10000289\thttps://goo.gl/maps/tCJrJ8tvCnRuPSVK7",
                "118\tPRIME MART-2\tSalwa Road-Prime Mart-2\t10000290\thttps://goo.gl/maps/guM1QFjiqDJigujb8",
                "119\tInsaaf Supermarket\tAl Aziziya Branch-Insaaf Supermarket\t10000291\thttps://maps.app.goo.gl/1bYnoCp28PrCmzxQA",
                "120\tGo Do Delivery-2\tSanniya Branch-Go Do Delivery-2\t10000292\thttps://maps.app.goo.gl/8SETSMsGNfxhxyoTA",
                "121\tDolphin Grocery-2\tSanniya-37 Branch-Dolphin Grocery-2\t10000293\thttps://goo.gl/maps/bgNuBq9AwGJ8AuFN9",
                "122\tAl Kaabi Grocery\tMadina Khalifa Branch-Al Kaabi Grocery\t10000294\thttps://goo.gl/maps/qru7QGZq41SsYjG99",
                "123\tSkyline Supermarket\tLukta Branch-Skyline Supermarket\t10000295\thttps://goo.gl/maps/3z5veTkKu9541kF4A",
                "124\tFood Market\tSanniya Branch-Food Market\t10000296\thttps://goo.gl/maps/bWswKTAvwDrHm7FeA",
                "125\tGrand Bazar Supermarket\tDoha Jadeed Branch-Grand Bazar Supermarket\t10000297\thttps://goo.gl/maps/sERGqyrzLCBoz6zK7",
                "126\tPrime Mart\tMidmac Signal Branch-Prime Mart\t10000298\thttps://goo.gl/maps/qDn8FgctmWu3NSNe6",
                "127\tKfone Trading\tMuntaza Branch-Kfone Trading\t10000303\thttps://goo.gl/maps/KPjta6YbiVWUkzMSA",
                "128\tAbdulla Al Jaber Grocery\tSanniya Branch-Abdulla Al Jaber Grocery\t10000309\thttps://goo.gl/maps/NzNAuxhsHLZ9BD4GA",
                "129\tGlobal Smart Trading\tMuaither Branch-Global Smart Trading\t10000417\thttps://maps.app.goo.gl/qkRVPC9eCsbXFv9d7",
                "130\tHFM Supermarket\tAin Khalid Branch-HFM Supermarket\t10000418\thttps://maps.app.goo.gl/Ey7j7yxWDf19UYbBA",
                "131\tNana Food Mart\tUmSalal Ali Branch- Nana Food Mart\t10000419\thttps://maps.app.goo.gl/JyajYxxscgdsiHRj9"
            )

            val parsedMachines = mutableListOf<CodMachineEntity>()
            for (line in rawLines) {
                val tokens = line.split("\t")
                if (tokens.isNotEmpty()) {
                    val serial = tokens[0]
                    val rawName = tokens.getOrNull(1)?.trim() ?: ""
                    val rawBranch = tokens.getOrNull(2)?.trim() ?: ""
                    val rawId = tokens.getOrNull(3)?.trim() ?: ""
                    val rawUrl = tokens.getOrNull(4)?.replace(" -", "")?.trim() ?: ""

                    // Fallbacks for empty columns
                    val name = if (rawName.isEmpty()) "Vodafone Deposit CDM" else rawName
                    val branchDetails = if (rawBranch.isEmpty()) "Qatar Vodafone Collection" else rawBranch
                    val machineId = if (rawId.isEmpty()) "10000" + (100 + serial.toInt()) else rawId
                    val webUrl = if (rawUrl.isEmpty()) "https://maps.google.com" else rawUrl

                    // Extract region token and dynamically map coordinates
                    val lowerText = (name + " " + branchDetails).lowercase()
                    var baseLat = 25.2854 // Center Doha
                    var baseLng = 51.5310
                    var areaName = "Doha"

                    when {
                        lowerText.contains("sanniya") || lowerText.contains("saniiya") || lowerText.contains("saniyah") -> {
                            baseLat = 25.1950
                            baseLng = 51.4110
                            areaName = "Sanniya"
                        }
                        lowerText.contains("wukair") || lowerText.contains("ezdan 29") || lowerText.contains("ezdan 32") || lowerText.contains("ezdan 38") -> {
                            baseLat = 25.1380
                            baseLng = 51.5700
                            areaName = "Al Wukair"
                        }
                        lowerText.contains("bin omran") -> {
                            baseLat = 25.3090
                            baseLng = 51.5030
                            areaName = "Bin Omran"
                        }
                        lowerText.contains("alkhor") || lowerText.contains("al khor") || lowerText.contains("khor") -> {
                            baseLat = 25.6880
                            baseLng = 51.5030
                            areaName = "Al Khor"
                        }
                        lowerText.contains("rayyan") || lowerText.contains("al rayyan") -> {
                            baseLat = 25.2950
                            baseLng = 51.4250
                            areaName = "Al Rayyan"
                        }
                        lowerText.contains("saad") || lowerText.contains("sadd") || lowerText.contains("cassava") -> {
                            baseLat = 25.2850
                            baseLng = 51.5020
                            areaName = "Al Sadd"
                        }
                        lowerText.contains("mamoura") -> {
                            baseLat = 25.2500
                            baseLng = 51.4950
                            areaName = "Al Mamoura"
                        }
                        lowerText.contains("najma") -> {
                            baseLat = 25.2760
                            baseLng = 51.5430
                            areaName = "Najma"
                        }
                        lowerText.contains("dafna") -> {
                            baseLat = 25.3200
                            baseLng = 51.5300
                            areaName = "Dafna"
                        }
                        lowerText.contains("wakra") || lowerText.contains("wakrah") -> {
                            baseLat = 25.1764
                            baseLng = 51.6033
                            areaName = "Al Wakrah"
                        }
                        lowerText.contains("airport") -> {
                            baseLat = 25.2440
                            baseLng = 51.5520
                            areaName = "Old Airport Road"
                        }
                        lowerText.contains("abu hamour") || lowerText.contains("abuhamour") -> {
                            baseLat = 25.2180
                            baseLng = 51.4680
                            areaName = "Abu Hamour"
                        }
                        lowerText.contains("muntazah") || lowerText.contains("muntaza") || lowerText.contains("smart shopping") -> {
                            baseLat = 25.2750
                            baseLng = 51.5150
                            areaName = "Al Muntazah"
                        }
                        lowerText.contains("mansoura") -> {
                            baseLat = 25.2800
                            baseLng = 51.5350
                            areaName = "Al Mansoura"
                        }
                        lowerText.contains("thumama") -> {
                            baseLat = 25.2300
                            baseLng = 51.5400
                            areaName = "Al Thumama"
                        }
                        lowerText.contains("aziziya") || lowerText.contains("azizia") || lowerText.contains("azizyza") -> {
                            baseLat = 25.2550
                            baseLng = 51.4550
                            areaName = "Al Aziziya"
                        }
                        lowerText.contains("kheesa") -> {
                            baseLat = 25.3850
                            baseLng = 51.4650
                            areaName = "Al Kheesa"
                        }
                        lowerText.contains("bin mahmood") -> {
                            baseLat = 25.2900
                            baseLng = 51.5100
                            areaName = "Bin Mahmood"
                        }
                        lowerText.contains("salwa") -> {
                            baseLat = 25.2700
                            baseLng = 51.4700
                            areaName = "Salwa Road"
                        }
                        lowerText.contains("salata") -> {
                            baseLat = 25.2780
                            baseLng = 51.5520
                            areaName = "New Salata"
                        }
                        lowerText.contains("hilal") -> {
                            baseLat = 25.2600
                            baseLng = 51.5400
                            areaName = "Al Hilal"
                        }
                        lowerText.contains("gharafa") || lowerText.contains("gharaffa") || lowerText.contains("kausar") -> {
                            baseLat = 25.3200
                            baseLng = 51.4500
                            areaName = "Al Gharaffa"
                        }
                        lowerText.contains("muglina") -> {
                            baseLat = 25.2800
                            baseLng = 51.5500
                            areaName = "Al Muglina"
                        }
                        lowerText.contains("umm garn") -> {
                            baseLat = 25.5300
                            baseLng = 51.4300
                            areaName = "Umm Garn"
                        }
                        lowerText.contains("lusail") -> {
                            baseLat = 25.4215
                            baseLng = 51.5225
                            areaName = "Lusail"
                        }
                        lowerText.contains("simaisma") -> {
                            baseLat = 25.5700
                            baseLng = 51.4880
                            areaName = "Simaisma"
                        }
                        lowerText.contains("ain khalid") -> {
                            baseLat = 25.2400
                            baseLng = 51.4400
                            areaName = "Ain Khalid"
                        }
                        lowerText.contains("doha jadeed") -> {
                            baseLat = 25.2810
                            baseLng = 51.5390
                            areaName = "Doha Jadeed"
                        }
                        lowerText.contains("souq al jaber") -> {
                            baseLat = 25.2880
                            baseLng = 51.5360
                            areaName = "Souq Al Jaber"
                        }
                        lowerText.contains("khalifa") || lowerText.contains("madina") -> {
                            baseLat = 25.3180
                            baseLng = 51.4850
                            areaName = "Madina Khalifa"
                        }
                        lowerText.contains("muaither") || lowerText.contains("muraikh") -> {
                            baseLat = 25.2900
                            baseLng = 51.4000
                            areaName = "Muaither"
                        }
                        lowerText.contains("um salal") || lowerText.contains("umsalal") || lowerText.contains("nana") -> {
                            baseLat = 25.4200
                            baseLng = 51.4000
                            areaName = "Um Salal Ali"
                        }
                    }

                    // Spread matching/spacing using deterministic pseudorandom coordinates around center
                    val idNum = machineId.takeLast(3).toIntOrNull() ?: serial.toInt()
                    val latOffset = ((idNum % 29) - 14) * 0.0031
                    val lngOffset = (((idNum * 13) % 29) - 14) * 0.0031

                    parsedMachines.add(
                        CodMachineEntity(
                            machineId = machineId,
                            machineName = "$name (Deposit)",
                            branchName = branchDetails,
                            area = areaName,
                            latitude = baseLat + latOffset,
                            longitude = baseLng + lngOffset,
                            googleMapsUrl = webUrl,
                            category = "Vodafone",
                            isBikeFriendly = !lowerText.contains("car only"),
                            isCarFriendly = true,
                            popularity = 50 + (idNum % 61)
                        )
                    )
                }
            }
            if (parsedMachines.isNotEmpty()) {
                codMachineDao.insertMachines(parsedMachines)
            }
        }
    }
}

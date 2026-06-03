package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CodMachineEntity
import com.example.data.local.UserEntity
import com.example.data.repository.CodRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

data class LocationHub(val name: String, val lat: Double, val lng: Double)

class CodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CodRepository(application)
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    private var locationCallback: LocationCallback? = null

    private val _isLiveGpsActive = MutableStateFlow(false)
    val isLiveGpsActive: StateFlow<Boolean> = _isLiveGpsActive.asStateFlow()

    // Preset hubs (empty as simulated location is removed)
    val presetHubs = emptyList<LocationHub>()

    // Current Rider location (device GPS only, defaults to Doha Souq Waqif location for initial cold start)
    private val _riderLocation = MutableStateFlow(Pair(25.2867, 51.5333))
    val riderLocation: StateFlow<Pair<Double, Double>> = _riderLocation.asStateFlow()

    private val _selectedHubName = MutableStateFlow("Live GPS Active")
    val selectedHubName: StateFlow<String> = _selectedHubName.asStateFlow()

    // Selected machine for dedicated details sheet
    private val _selectedMachineForDetail = MutableStateFlow<CodMachineEntity?>(null)
    val selectedMachineForDetail: StateFlow<CodMachineEntity?> = _selectedMachineForDetail.asStateFlow()

    // Search and Filter variables
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow("Nearest") // "Nearest", "Alphabetical", "Popular"
    val selectedSort: StateFlow<String> = _selectedSort.asStateFlow()

    private val _selectedCompatibility = MutableStateFlow("All") // "All", "Bike Friendly", "Car Friendly"
    val selectedCompatibility: StateFlow<String> = _selectedCompatibility.asStateFlow()

    // Voice simulation
    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _voiceStatusText = MutableStateFlow("")
    val voiceStatusText: StateFlow<String> = _voiceStatusText.asStateFlow()

    // Premium Account Simulation
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    // Core Database Flows
    val currentUser: StateFlow<UserEntity?> = repository.getUser("default_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMachines: StateFlow<List<CodMachineEntity>> = repository.getAllMachines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered / Sorted / Enriched Machines Flow
    val filteredMachines: StateFlow<List<CodMachineEntity>> = combine(
        allMachines,
        _searchQuery,
        _selectedCategory,
        _selectedSort,
        _selectedCompatibility,
        _riderLocation,
        currentUser
    ) { args: Array<Any?> ->
        val rawMachines = args[0] as List<CodMachineEntity>
        val query = args[1] as String
        val category = args[2] as String
        val sort = args[3] as String
        val compatibility = args[4] as String
        val location = args[5] as Pair<Double, Double>
        val user = args[6] as UserEntity?

        var list = rawMachines

        // 1. Compatibility Filter
        if (compatibility == "Bike Friendly") {
            list = list.filter { it.isBikeFriendly }
        } else if (compatibility == "Car Friendly") {
            list = list.filter { it.isCarFriendly }
        }

        // 2. Category Filter
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 3. Search Query (Area, Branch, ID, name)
        if (query.isNotEmpty()) {
            list = list.filter {
                it.machineName.contains(query, ignoreCase = true) ||
                it.branchName.contains(query, ignoreCase = true) ||
                it.area.contains(query, ignoreCase = true) ||
                it.machineId.contains(query, ignoreCase = true)
            }
        }

        // 4. Sort Strategy
        val (lat, lng) = location
        when (sort) {
            "Alphabetical" -> {
                list = list.sortedBy { it.machineName }
            }
            "Popular" -> {
                list = list.sortedByDescending { it.popularity }
            }
            "Nearest" -> {
                list = list.sortedBy { m ->
                    calculateDistance(lat, lng, m.latitude, m.longitude)
                }
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live nearest machine calculation based on current locations
    val nearestMachine: StateFlow<CodMachineEntity?> = combine(
        allMachines,
        _riderLocation
    ) { list, location ->
        val (lat, lng) = location
        list.sortedBy { m ->
            calculateDistance(lat, lng, m.latitude, m.longitude)
        }.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Actions ---

    @SuppressLint("MissingPermission")
    fun startLocationTracking() {
        if (locationCallback != null) return // Already tracking

        val context = getApplication<Application>()
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            _selectedHubName.value = "Permissions Missing"
            return
        }

        _isLiveGpsActive.value = true
        _selectedHubName.value = "GPS Tracking Active"

        // Update location every 5 seconds or every 20 meters movement.
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
            setMinUpdateDistanceMeters(20f)
            setMinUpdateIntervalMillis(3000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lastLoc = result.lastLocation ?: return
                _riderLocation.value = Pair(lastLoc.latitude, lastLoc.longitude)
                _selectedHubName.value = "GPS Tracking Active"
                _isLiveGpsActive.value = true
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                android.os.Looper.getMainLooper()
            )

            // Direct initialize
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    _riderLocation.value = Pair(loc.latitude, loc.longitude)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationTracking() {
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            locationCallback = null
        }
        _isLiveGpsActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }

    fun setRiderLocation(lat: Double, lng: Double) {
        _riderLocation.value = Pair(lat, lng)
        _selectedHubName.value = "GPS Simulated Position"
    }

    fun selectPresetHub(hub: LocationHub) {
        _riderLocation.value = Pair(hub.lat, hub.lng)
        _selectedHubName.value = hub.name
    }

    fun selectMachineForDetail(machine: CodMachineEntity?) {
        _selectedMachineForDetail.value = machine
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSort(sort: String) {
        _selectedSort.value = sort
    }

    fun setCompatibility(comp: String) {
        _selectedCompatibility.value = comp
    }

    fun toggleFavorite(machineId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val currentFavs = user.favoriteMachines.split(",").filter { it.isNotEmpty() }.toMutableList()
            if (currentFavs.contains(machineId)) {
                currentFavs.remove(machineId)
            } else {
                // Unlimited for Premium, Limit to 3 for Free tier
                if (!_isPremiumUser.value && currentFavs.size >= 3) {
                    // Trigger upgrade notification logic (handled in UI via Toast)
                } else {
                    currentFavs.add(machineId)
                }
            }
            repository.saveUser(user.copy(favoriteMachines = currentFavs.joinToString(",")))
        }
    }

    fun updateRiderProfile(name: String, email: String, riderType: String) {
        val user = currentUser.value ?: UserEntity()
        viewModelScope.launch {
            repository.saveUser(user.copy(name = name, email = email, riderType = riderType))
        }
    }

    fun updateRiderCash(cashAmount: Double) {
        val user = currentUser.value ?: UserEntity()
        viewModelScope.launch {
            repository.saveUser(user.copy(currentCash = cashAmount))
        }
    }

    fun toggleRiderTypeSetting() {
        val user = currentUser.value ?: return
        val nextType = if (user.riderType == "BIKE") "CAR" else "BIKE"
        viewModelScope.launch {
            repository.saveUser(user.copy(riderType = nextType))
        }
    }

    fun setPremiumStatus(isPremium: Boolean) {
        _isPremiumUser.value = isPremium
    }

    // --- Admin CRUD Panel Actions ---

    fun addOrUpdateMachine(
        id: String,
        name: String,
        branch: String,
        area: String,
        lat: Double,
        lng: Double,
        url: String,
        cat: String,
        isBike: Boolean,
        isCar: Boolean
    ) {
        viewModelScope.launch {
            repository.insertMachine(
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
        }
    }

    fun deleteMachine(id: String) {
        viewModelScope.launch {
            repository.deleteMachine(id)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearAllMachines()
            // The repository's seed check will run or we can re-insert
            // Let's just restore default list from repository init or let user reload
        }
    }

    fun importCSVBulk(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.bulkImportCsv(csvText)
            onComplete(count)
        }
    }

    // --- Voice simulation trigger ---
    fun startVoiceSim(phraseSelected: String? = null) {
        _isVoiceActive.value = true
        _voiceStatusText.value = "Listening..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            if (phraseSelected != null) {
                _voiceStatusText.value = "Searching for: \"$phraseSelected\""
                kotlinx.coroutines.delay(850)
                _searchQuery.value = phraseSelected
            } else {
                // Sample random phrase
                val sampleQueries = listOf("Sanniya Vodafone", "nearest deposit", "Al Rayyan branch", "Bin Omran kiosk")
                val chosen = sampleQueries.random()
                _voiceStatusText.value = "Recognized: \"$chosen\""
                kotlinx.coroutines.delay(850)
                _searchQuery.value = chosen
            }
            _isVoiceActive.value = false
        }
    }

    fun cancelVoiceSim() {
        _isVoiceActive.value = false
    }

    // --- Calculations ---

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun getTravelEtaMinutes(distanceKm: Double, riderType: String): Int {
        val speedKmh = if (riderType == "BIKE") 40.0 else 50.0 // 40km/h for motorbikes, 50km/h for cars in traffic
        val hours = distanceKm / speedKmh
        return max(1, (hours * 60).roundToInt())
    }
}

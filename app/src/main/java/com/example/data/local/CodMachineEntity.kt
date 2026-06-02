package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cod_machines")
data class CodMachineEntity(
    @PrimaryKey val machineId: String,
    val machineName: String,
    val branchName: String,
    val area: String,
    val latitude: Double,
    val longitude: Double,
    val googleMapsUrl: String,
    val category: String, // "Ooredoo", "Vodafone", "QNB", "CBQ", "Other"
    val isActive: Boolean = true,
    val isBikeFriendly: Boolean = true,
    val isCarFriendly: Boolean = true,
    val popularity: Int = 1, // Number of simulated visits
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

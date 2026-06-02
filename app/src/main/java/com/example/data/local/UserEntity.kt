package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String = "default_user",
    val name: String = "Qatar Rider",
    val riderType: String = "BIKE", // "BIKE" or "CAR"
    val email: String = "rider@talabat.qa",
    val currentCash: Double = 0.0,
    val favoriteMachines: String = "" // Comma separated IDs
)

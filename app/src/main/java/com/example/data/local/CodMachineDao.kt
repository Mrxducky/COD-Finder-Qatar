package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CodMachineDao {
    @Query("SELECT * FROM cod_machines")
    fun getAllMachines(): Flow<List<CodMachineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: CodMachineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachines(machines: List<CodMachineEntity>)

    @Query("DELETE FROM cod_machines WHERE machineId = :machineId")
    suspend fun deleteMachineById(machineId: String)
    
    @Query("SELECT * FROM cod_machines WHERE machineId = :machineId LIMIT 1")
    suspend fun getMachineById(machineId: String): CodMachineEntity?

    @Query("DELETE FROM cod_machines")
    suspend fun clearAll()
}

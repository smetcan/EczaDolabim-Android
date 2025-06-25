package com.example.eczadolabim

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KisiDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(kisi: Kisi)

    // EKSİK OLAN FONKSİYON
    @Delete
    suspend fun delete(kisi: Kisi)

    @Query("SELECT * FROM kisiler_tablosu ORDER BY isim ASC")
    fun getAllKisiler(): Flow<List<Kisi>>
}
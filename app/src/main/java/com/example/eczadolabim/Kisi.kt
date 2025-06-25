package com.example.eczadolabim

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kisiler_tablosu")
data class Kisi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val isim: String
)
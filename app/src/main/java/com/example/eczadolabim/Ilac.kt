package com.example.eczadolabim

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ilaclar_tablosu")
data class Ilac(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "ilac_adi")
    val ilacAdi: String,

    // YENİ EKLENEN ALAN
    @ColumnInfo(name = "etken_maddesi")
    val etkenMaddesi: String?=null,

    @ColumnInfo(name = "son_kullanma_tarihi")
    val sonKullanmaTarihi: Long,

    @ColumnInfo(name = "aciklama")
    val aciklama: String,

    @ColumnInfo(name = "kimin_icin")
    val kiminIcin: String,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null
)
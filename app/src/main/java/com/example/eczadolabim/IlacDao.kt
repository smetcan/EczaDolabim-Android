package com.example.eczadolabim

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IlacDao {

    @Insert
    suspend fun insert(ilac: Ilac)

    @Update
    suspend fun update(ilac: Ilac)

    @Delete
    suspend fun delete(ilac: Ilac)

    @Query("SELECT * FROM ilaclar_tablosu WHERE id = :id")
    fun getIlacById(id: Int): Flow<Ilac>

    // DEĞİŞİKLİK BURADA: Arama artık 3 kolonda yapılıyor.
    @Query("SELECT * FROM ilaclar_tablosu WHERE (:kisiFiltresi = 'TÜMÜ' OR kimin_icin = :kisiFiltresi) AND (ilac_adi LIKE :aramaMetni OR etken_maddesi LIKE :aramaMetni OR aciklama LIKE :aramaMetni) ORDER BY ilac_adi ASC")
    fun getIlaclarSiraliIsim(aramaMetni: String, kisiFiltresi: String): Flow<List<Ilac>>

    // DEĞİŞİKLİK BURADA: Arama artık 3 kolonda yapılıyor.
    @Query("SELECT * FROM ilaclar_tablosu WHERE (:kisiFiltresi = 'TÜMÜ' OR kimin_icin = :kisiFiltresi) AND (ilac_adi LIKE :aramaMetni OR etken_maddesi LIKE :aramaMetni OR aciklama LIKE :aramaMetni) ORDER BY son_kullanma_tarihi ASC")
    fun getIlaclarSiraliTarih(aramaMetni: String, kisiFiltresi: String): Flow<List<Ilac>>
}
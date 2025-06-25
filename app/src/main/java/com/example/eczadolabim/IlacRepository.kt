package com.example.eczadolabim

import kotlinx.coroutines.flow.Flow

class IlacRepository(private val ilacDao: IlacDao, private val kisiDao: KisiDao) {

    // --- İlaç Fonksiyonları ---
    fun getIlaclarSiraliIsim(aramaMetni: String, kisiFiltresi: String): Flow<List<Ilac>> {
        return ilacDao.getIlaclarSiraliIsim(aramaMetni, kisiFiltresi)
    }

    fun getIlaclarSiraliTarih(aramaMetni: String, kisiFiltresi: String): Flow<List<Ilac>> {
        return ilacDao.getIlaclarSiraliTarih(aramaMetni, kisiFiltresi)
    }

    suspend fun insert(ilac: Ilac) {
        ilacDao.insert(ilac)
    }

    suspend fun update(ilac: Ilac) {
        ilacDao.update(ilac)
    }

    suspend fun delete(ilac: Ilac) {
        ilacDao.delete(ilac)
    }

    // EKSİK OLAN FONKSİYON BURAYA EKLENDİ
    fun getIlacById(id: Int): Flow<Ilac> {
        return ilacDao.getIlacById(id)
    }


    // --- Kişi Fonksiyonları ---
    val allKisiler: Flow<List<Kisi>> = kisiDao.getAllKisiler()

    suspend fun insertKisi(kisi: Kisi) {
        kisiDao.insert(kisi)
    }

    suspend fun deleteKisi(kisi: Kisi) {
        kisiDao.delete(kisi)
    }
}
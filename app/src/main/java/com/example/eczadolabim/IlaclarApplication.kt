// IlaclarApplication.kt
package com.example.eczadolabim

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.eczadolabim.ExpiryCheckWorker
import com.example.eczadolabim.IlacDatabase
import com.example.eczadolabim.IlacRepository
import java.util.concurrent.TimeUnit

class IlaclarApplication : Application() {
    val database by lazy { IlacDatabase.getDatabase(this) }
    val repository by lazy { IlacRepository(database.ilacDao(), database.kisiDao()) }


    override fun onCreate() {
        super.onCreate()
        scheduleExpiryCheck()
    }

    private fun scheduleExpiryCheck() {
        // Günde bir kez çalışacak periyodik bir görev oluşturuyoruz.
        val expiryCheckRequest =
            PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
                .build()

        // Bu görevi, daha önce eklenmediyse, WorkManager'a ekliyoruz.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiryCheckWork",
            ExistingPeriodicWorkPolicy.KEEP, // Eğer görev zaten varsa, eskisini koru, yenisini ekleme.
            expiryCheckRequest
        )
    }
}
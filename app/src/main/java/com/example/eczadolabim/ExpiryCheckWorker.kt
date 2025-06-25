package com.example.eczadolabim

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ExpiryCheckWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Repository'yi ve Ayarları al
        val repository = (applicationContext as IlaclarApplication).repository
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Değeri artık direkt olarak Integer olarak okuyoruz. Bulamazsa varsayılan 30'u kullanır.
        val uyariGunSayisi = sharedPreferences.getInt("uyari_gunu", 30)

        // 2. Veritabanı işlemlerini güvenli bir blok içinde yap
        return try {
            // Tüm ilaçların anlık listesini al
            val ilacListesi = repository.getIlaclarSiraliIsim("%", "TÜMÜ").first()

            // Uyarı tarihini, ayarlardan okunan güne göre hesapla
            val uyariTarihi = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, uyariGunSayisi) }
            val bugun = Calendar.getInstance()

            // SKT'si geçmiş veya yaklaşan ilaçları filtrele
            val yaklasanIlaclar = ilacListesi.filter {
                val ilacTarihi = Calendar.getInstance().apply { timeInMillis = it.sonKullanmaTarihi }
                ilacTarihi.before(uyariTarihi) // Hem tarihi geçmişleri hem de yaklaşanları kapsar
            }.map { it.ilacAdi }


            // Eğer uyarı verilecek ilaç varsa, bildirim gönder
            if (yaklasanIlaclar.isNotEmpty()) {
                sendNotification(yaklasanIlaclar)
            }

            Log.d("ExpiryCheckWorker", "Work finished successfully. Checked for $uyariGunSayisi days.")
            Result.success()
        } catch (e: Exception) {
            Log.e("ExpiryCheckWorker", "Work failed", e)
            Result.failure()
        }
    }


    private fun sendNotification(yaklasanIlaclar: List<String>) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ILAC_SKT_CHANNEL_ID"

        // Android 8.0 ve üzeri için Bildirim Kanalı oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "İlaç Son Kullanma Tarihi Uyarıları",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Son kullanma tarihi yaklaşan ilaçlar için bildirimler."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Bildirime tıklandığında MainActivity'yi açacak Intent
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val bildirimIcerigi = "SKT'si yaklaşanlar: ${yaklasanIlaclar.joinToString(", ")}"
        val bildirimBasligi = "Ecza Dolabım - SKT Uyarısı"

        // Bildirimi oluştur
        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(bildirimBasligi)
            .setContentText(bildirimIcerigi)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bildirimIcerigi))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Bildirim izni kontrolü ve bildirimi gösterme
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(appContext).notify(1, builder.build())
        }
    }
}
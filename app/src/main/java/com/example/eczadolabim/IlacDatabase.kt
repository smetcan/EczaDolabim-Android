package com.example.eczadolabim

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DEĞİŞİKLİK 1: Versiyonu 3'ten 4'e çıkarıyoruz.
@Database(entities = [Ilac::class, Kisi::class], version = 4, exportSchema = false)
abstract class IlacDatabase : RoomDatabase() {

    abstract fun ilacDao(): IlacDao
    abstract fun kisiDao(): KisiDao

    companion object {
        @Volatile
        private var INSTANCE: IlacDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ilaclar_tablosu ADD COLUMN image_path TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `kisiler_tablosu` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `isim` TEXT NOT NULL)")
            }
        }

        // DEĞİŞİKLİK 2: Versiyon 3'ten 4'e geçerken yeni 'etken_maddesi' kolonunu ekliyoruz.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ilaclar_tablosu ADD COLUMN etken_maddesi TEXT")
            }
        }

        fun getDatabase(context: Context): IlacDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IlacDatabase::class.java,
                    "ilac_database"
                )
                    // DEĞİŞİKLİK 3: Yeni migration planını listeye ekliyoruz.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
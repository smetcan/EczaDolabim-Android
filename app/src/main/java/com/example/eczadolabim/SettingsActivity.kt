package com.example.eczadolabim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.eczadolabim.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding'i bağlıyoruz
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar'ı Action Bar olarak ayarlıyoruz
        setSupportActionBar(binding.toolbarSettings)
        // Geri okunu gösteriyoruz
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    // Geri okuna basıldığında ne olacağını yönetiyoruz.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Eğer basılan öğe geri oku ise (android.R.id.home),
        // bu aktiviteyi bitir ve bir önceki ekrana dön.
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
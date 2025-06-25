package com.example.eczadolabim

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    private val ilacViewModel: IlacViewModel by activityViewModels {
        IlacViewModelFactory((requireActivity().application as IlaclarApplication).repository)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // --- HATALI KOD BLOĞU BURADAN KALDIRILDI ---
        // val uyariGunuPref = findPreference<EditTextPreference>("uyari_gunu") satırları ve
        // ona ait olan listener artık yok. Çünkü SeekBarPreference'ın buna ihtiyacı yok.
        // ---

        // "Kişileri Yönet" preference'ını bul ve tıklama olayını yönet.
        val managePeoplePref: Preference? = findPreference("yonet_kisiler")
        managePeoplePref?.setOnPreferenceClickListener {
            showManagePeopleDialog()
            true
        }
    }

    private fun showManagePeopleDialog() {
        // Bu metodun geri kalanı tamamen aynı, bir değişiklik yok.
        val dialogView = layoutInflater.inflate(R.layout.dialog_manage_people, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView_kisiler)
        val yeniKisiAdi = dialogView.findViewById<EditText>(R.id.editText_yeniKisiDialog)
        val ekleButton = dialogView.findViewById<Button>(R.id.button_yeniKisiEkleDialog)

        val kisiListAdapter = KisiListAdapter { kisi ->
            ilacViewModel.deleteKisi(kisi)
        }
        recyclerView.adapter = kisiListAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        ilacViewModel.allKisiler.observe(viewLifecycleOwner) { kisiler ->
            kisiler?.let { kisiListAdapter.submitList(it) }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Kişileri Yönet")
            .setView(dialogView)
            .setPositiveButton("Kapat", null)
            .create()

        ekleButton.setOnClickListener {
            val isim = yeniKisiAdi.text.toString()
            if (isim.isNotBlank()) {
                ilacViewModel.insertKisi(isim)
                yeniKisiAdi.text.clear()
                Toast.makeText(context, "'$isim' eklendi.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}
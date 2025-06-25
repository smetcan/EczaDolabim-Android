package com.example.eczadolabim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.eczadolabim.databinding.ActivityAddIlacBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddIlacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIlacBinding
    private var currentIlac: Ilac? = null
    private var latestTmpUri: Uri? = null

    private val ilacViewModel: IlacViewModel by viewModels {
        IlacViewModelFactory((application as IlaclarApplication).repository)
    }

    private val getFotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            latestTmpUri?.let { handleImageSelection(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIlacBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddIlac)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val currentIlacId = intent.getIntExtra(EXTRA_ILAC_ID, -1)

        ilacViewModel.allKisiler.observe(this) { kisiler ->
            updateChipGroup(kisiler)
        }

        if (currentIlacId != -1) {
            supportActionBar?.title = "İlaç Düzenle"
            ilacViewModel.getIlacById(currentIlacId).observe(this) { ilac ->
                ilac?.let {
                    currentIlac = it
                    updateUI(it)
                }
            }
        } else {
            supportActionBar?.title = "Yeni İlaç Ekle"
            currentIlac = Ilac(id = 0, ilacAdi = "", etkenMaddesi = null, aciklama = "", kiminIcin = "Genel", sonKullanmaTarihi = 0L, imagePath = null)
        }

        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        binding.buttonFotoEkle.setOnClickListener {
            showPhotoPickerDialog()
        }
        binding.editTextSkt.setOnClickListener {
            showDatePicker()
        }
        binding.buttonKaydet.setOnClickListener {
            kaydetVeCik()
        }
    }

    private fun updateUI(ilac: Ilac) {
        binding.editTextIlacAdi.setText(ilac.ilacAdi)
        binding.editTextEtkenMadde.setText(ilac.etkenMaddesi)
        binding.editTextAciklama.setText(ilac.aciklama)

        if (ilac.sonKullanmaTarihi > 0) {
            binding.editTextSkt.setText(formatTarih(ilac.sonKullanmaTarihi))
        }
        if (ilac.imagePath != null) {
            binding.imageViewIlacFoto.load(File(ilac.imagePath))
        } else {
            binding.imageViewIlacFoto.setImageResource(R.drawable.ic_medication)
        }
        selectChipByName(ilac.kiminIcin)
    }

    private fun updateChipGroup(kisiler: List<Kisi>?) {
        val chipGroup = binding.chipGroupKiminIcin
        val seciliChipText = if (chipGroup.checkedChipId != -1) {
            chipGroup.findViewById<Chip>(chipGroup.checkedChipId)?.text?.toString()
        } else {
            currentIlac?.kiminIcin
        }

        chipGroup.removeAllViews()

        val genelChip = Chip(this).apply {
            text = "Genel"
            isCheckable = true
            id = View.generateViewId()
        }
        chipGroup.addView(genelChip)

        kisiler?.forEach { kisi ->
            val kisiChip = Chip(this).apply {
                text = kisi.isim
                isCheckable = true
                id = View.generateViewId()
            }
            chipGroup.addView(kisiChip)
        }
        selectChipByName(seciliChipText)
    }

    // HATA 2 İÇİN DÜZELTME: Fonksiyon artık null bir String de kabul ediyor.
    private fun selectChipByName(isim: String?) {
        if (isim == null) {
            binding.chipGroupKiminIcin.clearCheck()
            return
        }

        for (i in 0 until binding.chipGroupKiminIcin.childCount) {
            val chip = binding.chipGroupKiminIcin.getChildAt(i) as Chip
            if (chip.text.toString().equals(isim, ignoreCase = true)) {
                binding.chipGroupKiminIcin.check(chip.id)
                return
            }
        }
        // Eğer isim listede bulunamazsa, "Genel" çipini seçili yap.
        (binding.chipGroupKiminIcin.getChildAt(0) as? Chip)?.isChecked = true
    }

    private fun showDatePicker() {
        val secim = currentIlac?.sonKullanmaTarihi?.takeIf { it > 0 } ?: MaterialDatePicker.todayInUtcMilliseconds()
        val tarihSecici = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Son Kullanma Tarihi Seçin")
            .setSelection(secim)
            .build()
        tarihSecici.show(supportFragmentManager, "DATE_PICKER_TAG")
        tarihSecici.addOnPositiveButtonClickListener { tarih ->
            currentIlac = currentIlac?.copy(sonKullanmaTarihi = tarih)
            binding.editTextSkt.setText(formatTarih(tarih))
        }
    }

    private fun showPhotoPickerDialog() {
        val options = arrayOf("Kameradan Çek", "Galeriden Seç")
        MaterialAlertDialogBuilder(this)
            .setTitle("Fotoğraf Kaynağı")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takeImage()
                    1 -> selectImageFromGallery()
                }
            }
            .show()
    }

    private fun selectImageFromGallery() = getFotoLauncher.launch("image/*")

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    // HATA 1 İÇİN DÜZELTME: BuildConfig sınıfını import ettiğimiz için bu satır artık çalışacak.
    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.fileprovider", tmpFile)
    }

    private fun handleImageSelection(uri: Uri) {
        val kaliciPath = saveImageToInternalStorage(uri)
        currentIlac = currentIlac?.copy(imagePath = kaliciPath)
        kaliciPath?.let {
            binding.imageViewIlacFoto.load(File(it))
        }
    }

    private fun kaydetVeCik() {
        val ilacAdi = binding.editTextIlacAdi.text.toString()
        val etkenMaddesi = binding.editTextEtkenMadde.text.toString()
        val aciklama = binding.editTextAciklama.text.toString()

        val seciliChipId = binding.chipGroupKiminIcin.checkedChipId
        val kiminIcin = if (seciliChipId != -1) {
            findViewById<Chip>(seciliChipId)?.text?.toString() ?: "Genel"
        } else {
            "Genel"
        }

        if (ilacAdi.isNotBlank() && (currentIlac?.sonKullanmaTarihi ?: 0L) > 0L) {
            val ilacToSave = currentIlac?.copy(
                ilacAdi = ilacAdi,
                etkenMaddesi = etkenMaddesi,
                aciklama = aciklama,
                kiminIcin = kiminIcin
            ) ?: return

            if (ilacToSave.id != 0) {
                ilacViewModel.update(ilacToSave)
            } else {
                ilacViewModel.insert(ilacToSave)
            }
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "ilac_foto_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatTarih(tarih: Long): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
        return format.format(Date(tarih))
    }

    companion object {
        const val EXTRA_ILAC_ID = "com.example.eczadolabim.ILAC_ID"
    }
}
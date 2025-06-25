package com.example.eczadolabim

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eczadolabim.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: IlacListAdapter

    // YENİ: Veritabanından gelen kişi listesini tutmak için bir sınıf değişkeni
    private var tumKisiler: List<Kisi> = emptyList()

    private val ilacViewModel: IlacViewModel by viewModels {
        IlacViewModelFactory((application as IlaclarApplication).repository)
    }

    private val addIlacActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // LiveData sayesinde liste otomatik güncellenir.
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // İzin verildi, harika!
            } else {
                Toast.makeText(this, "Bildirim izni, son kullanma tarihi uyarıları için gereklidir.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        observeViewModel()
        setupActionButtons()
        setupSwipeToDelete()
        askNotificationPermission()
    }

    private fun setupRecyclerView() {
        adapter = IlacListAdapter { clickedIlac ->
            val intent = Intent(this@MainActivity, AddIlacActivity::class.java)
            intent.putExtra(AddIlacActivity.EXTRA_ILAC_ID, clickedIlac.id)
            addIlacActivityLauncher.launch(intent)
        }
        binding.recyclerViewIlaclar.adapter = adapter
        binding.recyclerViewIlaclar.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        ilacViewModel.allIlaclar.observe(this) { ilaclar ->
            adapter.submitList(ilaclar)
            if (ilaclar.isNullOrEmpty()) {
                binding.recyclerViewIlaclar.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerViewIlaclar.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }
        // Kişi listesini gözlemleyip sınıf değişkenimize kaydediyoruz.
        ilacViewModel.allKisiler.observe(this) { kisiler ->
            this.tumKisiler = kisiler ?: emptyList()
        }
    }

    private fun setupActionButtons() {
        binding.fabAddIlac.setOnClickListener {
            val intent = Intent(this@MainActivity, AddIlacActivity::class.java)
            addIlacActivityLauncher.launch(intent)
        }

        binding.fabFiltre.setOnClickListener {
            showFiltreDialog()
        }
    }

    private fun showFiltreDialog() {
        // Artık LiveData'nın anlık değerine değil, her zaman güncel olan sınıf değişkenimize güveniyoruz.
        val kisiler = this.tumKisiler

        val secenekler = mutableListOf("TÜMÜ")
        kisiler.forEach { kisi -> secenekler.add(kisi.isim) }

        MaterialAlertDialogBuilder(this)
            .setTitle("Kişiye Göre Filtrele")
            .setItems(secenekler.toTypedArray()) { dialog, which ->
                val secilenKisi = secenekler[which]
                ilacViewModel.filtrele(secilenKisi)
                Toast.makeText(this, "'$secilenKisi' filtresi uygulandı.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val silinecekIlac = adapter.currentList[position]

                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("İlaç Sil")
                    .setMessage("'${silinecekIlac.ilacAdi}' ilacını silmek istediğinizden emin misiniz?")
                    .setNegativeButton("Hayır") { _, _ -> adapter.notifyItemChanged(position) }
                    .setPositiveButton("Evet") { _, _ -> ilacViewModel.delete(silinecekIlac) }
                    .show()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!
                val backgroundColor = ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.delete_color))
                val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                when {
                    dX > 0 -> { // Sağa kaydırma
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        backgroundColor.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt() + viewHolder.itemView.width, itemView.bottom)
                    }
                    dX < 0 -> { // Sola kaydırma
                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        backgroundColor.setBounds(itemView.right + dX.toInt() - viewHolder.itemView.width, itemView.top, itemView.right, itemView.bottom)
                    }
                    else -> {
                        backgroundColor.setBounds(0, 0, 0, 0)
                        icon.setBounds(0, 0, 0, 0)
                    }
                }
                backgroundColor.draw(c)
                icon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewIlaclar)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                ilacViewModel.ara(newText.orEmpty())
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                ilacViewModel.siralamaTuruAyarla(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date -> {
                ilacViewModel.siralamaTuruAyarla(SortOrder.BY_DATE)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
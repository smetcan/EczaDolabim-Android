package com.example.eczadolabim

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.eczadolabim.databinding.ListItemIlacBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IlacListAdapter(private val onItemClicked: (Ilac) -> Unit) :
    ListAdapter<Ilac, IlacListAdapter.IlacViewHolder>(IlaclarComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IlacViewHolder {
        val binding =
            ListItemIlacBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IlacViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: IlacViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class IlacViewHolder(
        private val binding: ListItemIlacBinding,
        private val onItemClicked: (Ilac) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentIlac: Ilac? = null
        private val context = binding.root.context
        private val UYARI_GUN_SAYISI = 30

        init {
            itemView.setOnClickListener {
                currentIlac?.let {
                    onItemClicked(it)
                }
            }
        }

        fun bind(ilac: Ilac) {
            currentIlac = ilac

            // View Binding ile tüm view'lara "binding" nesnesi üzerinden erişiyoruz.
            binding.textViewIlacAdi.text = ilac.ilacAdi
            binding.textViewAciklama.text = ilac.aciklama
            binding.textViewKiminIcin.text = ilac.kiminIcin

            binding.textViewSkt.text = formatTarih(ilac.sonKullanmaTarihi)
            binding.textViewSkt.setTextColor(getTarihRengi(ilac.sonKullanmaTarihi))

            if (!ilac.imagePath.isNullOrEmpty()) {
                binding.imageViewIlacOnizleme.load(File(ilac.imagePath)) {
                    placeholder(R.drawable.ic_medication)
                    error(R.drawable.ic_medication)
                }
            } else {
                binding.imageViewIlacOnizleme.setImageResource(R.drawable.ic_medication)
            }

            if (!ilac.etkenMaddesi.isNullOrBlank()) {
                binding.textViewEtkenMadde.visibility = View.VISIBLE
                binding.textViewEtkenMadde.text = ilac.etkenMaddesi
            } else {
                // Eğer etken maddesi boşsa, alanı tamamen gizle.
                binding.textViewEtkenMadde.visibility = View.GONE
            }


        }

        private fun formatTarih(tarih: Long): String {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
            return format.format(Date(tarih))
        }

        private fun getTarihRengi(tarih: Long): Int {
            val bugun = Calendar.getInstance()
            val uyariTarihi = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, UYARI_GUN_SAYISI) }
            val ilacTarihi = Calendar.getInstance().apply { timeInMillis = tarih }

            val renkId = when {
                ilacTarihi.before(bugun) -> R.color.delete_color
                ilacTarihi.before(uyariTarihi) -> R.color.warning_color
                else -> R.color.safe_color
            }
            return ContextCompat.getColor(context, renkId)
        }
    }

    class IlaclarComparator : DiffUtil.ItemCallback<Ilac>() {
        override fun areItemsTheSame(oldItem: Ilac, newItem: Ilac): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ilac, newItem: Ilac): Boolean {
            return oldItem == newItem
        }
    }
}
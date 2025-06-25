package com.example.eczadolabim

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class IlacViewModel(private val repository: IlacRepository) : ViewModel() {

    private val aramaMetni = MutableStateFlow("")
    private val siralamaTuru = MutableStateFlow(SortOrder.BY_NAME)
    private val filtreKisi = MutableStateFlow("TÜMÜ") // YENİ: Filtre durumunu tutar

    fun ara(query: String) { aramaMetni.value = query }
    fun siralamaTuruAyarla(sortOrder: SortOrder) { siralamaTuru.value = sortOrder }
    fun filtrele(kisi: String) { filtreKisi.value = kisi } // YENİ: Filtreyi güncelleyen fonksiyon

    // ÜÇ FARKLI FLOW'U BİRLEŞTİRİYORUZ
    val allIlaclar: LiveData<List<Ilac>> = combine(
        aramaMetni,
        siralamaTuru,
        filtreKisi
    ) { query, order, filter ->
        // Bu üç bilgiyi bir arada tutan bir Triple (üçlü) oluşturuyoruz.
        Triple(query, order, filter)
    }.flatMapLatest { (query, order, filter) ->
        val queryWithWildcards = "%$query%"
        when (order) {
            SortOrder.BY_NAME -> repository.getIlaclarSiraliIsim(queryWithWildcards, filter)
            SortOrder.BY_DATE -> repository.getIlaclarSiraliTarih(queryWithWildcards, filter)
        }
    }.asLiveData()

    // ... Kişi ve İlaç ekleme/silme/güncelleme fonksiyonları aynı kalıyor ...
    val allKisiler: LiveData<List<Kisi>> = repository.allKisiler.asLiveData()
    fun insertKisi(isim: String) = viewModelScope.launch { if (isim.isNotBlank()) { repository.insertKisi(Kisi(isim = isim)) } }
    fun deleteKisi(kisi: Kisi) = viewModelScope.launch { repository.deleteKisi(kisi) }
    fun insert(ilac: Ilac) = viewModelScope.launch { repository.insert(ilac) }
    fun update(ilac: Ilac) = viewModelScope.launch { repository.update(ilac) }
    fun delete(ilac: Ilac) = viewModelScope.launch { repository.delete(ilac) }
    fun getIlacById(id: Int): LiveData<Ilac> = repository.getIlacById(id).asLiveData()
}
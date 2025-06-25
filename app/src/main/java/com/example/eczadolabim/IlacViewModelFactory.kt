package com.example.eczadolabim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Bu Factory, IlacViewModel'ın nasıl oluşturulacağını sisteme söyler.
class IlacViewModelFactory(private val repository: IlacRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Eğer sistemin istediği ViewModel, IlacViewModel sınıfı ise...
        if (modelClass.isAssignableFrom(IlacViewModel::class.java)) {
            // ...ona Repository'yi vererek yeni bir IlacViewModel oluştur ve döndür.
            @Suppress("UNCHECKED_CAST")
            return IlacViewModel(repository) as T
        }
        // Eğer tanımadığımız başka bir ViewModel isterse, hata fırlat.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
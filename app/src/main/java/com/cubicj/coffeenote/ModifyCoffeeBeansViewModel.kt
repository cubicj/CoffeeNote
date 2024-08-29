package com.cubicj.coffeenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModifyCoffeeBeansViewModel(
    private val coffeeBeansDao: CoffeeBeansDao,
    private val coffeeBeansNoteDao: CoffeeBeansNoteDao
) : ViewModel() {

    val _coffeeBean = MutableStateFlow<CoffeeBeans?>(null)
    val coffeeBean: StateFlow<CoffeeBeans?> = _coffeeBean.asStateFlow()

    fun loadCoffeeBean(beanId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _coffeeBean.value = coffeeBeansDao.getById(beanId)
        }
    }

    fun updateCoffeeBean(updatedBean: CoffeeBeans) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansDao.update(updatedBean)
        }
    }
}

class ModifyCoffeeBeansViewModelFactory(
    private val coffeeBeansDao: CoffeeBeansDao,
    private val coffeeBeansNoteDao: CoffeeBeansNoteDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModifyCoffeeBeansViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModifyCoffeeBeansViewModel(coffeeBeansDao, coffeeBeansNoteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
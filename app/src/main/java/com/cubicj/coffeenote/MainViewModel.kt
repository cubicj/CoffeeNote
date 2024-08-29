package com.cubicj.coffeenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainViewModel(
    private val coffeeBeansDao: CoffeeBeansDao
) : ViewModel() {
    private val _coffeeBeans = MutableStateFlow<List<CoffeeBeans>>(emptyList())
    val coffeeBeans: StateFlow<List<CoffeeBeans>> = _coffeeBeans

    init {
        viewModelScope.launch {
            coffeeBeansDao.getAllOrderByNameAscFlow().collectLatest { beans ->
                _coffeeBeans.value = beans
            }
        }
    }

    fun insertCoffeeBeans(coffeeBeans: CoffeeBeans) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansDao.insert(coffeeBeans)
        }
    }

    fun deleteCoffeeBeans(coffeeBeans: CoffeeBeans) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansDao.delete(coffeeBeans)

            // _coffeeBeans 업데이트 (UI 갱신 트리거, 불변성 유지)
            _coffeeBeans.value = _coffeeBeans.value.toMutableList().apply {
                remove(coffeeBeans)
            }
        }
    }

    fun updateCoffeeBeanNotes(beanId: Long, newNotes: List<CoffeeBeansNote>) {
        viewModelScope.launch {
            val updatedBean = coffeeBeansDao.getById(beanId)?.copy(
                noteNameColorMap = newNotes.associate { it.notename to it.colorcode }
            )
            updatedBean?.let { coffeeBeansDao.update(it) }
        }
    }

    fun getCoffeeBeanById(id: Long): Flow<CoffeeBeans?> {
        return flow {
            emit(coffeeBeansDao.getById(id))
        }.flowOn(Dispatchers.IO) // IO 스레드에서 실행
    }
}



class MainViewModelFactory(
    private val coffeeBeansDao: CoffeeBeansDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(coffeeBeansDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
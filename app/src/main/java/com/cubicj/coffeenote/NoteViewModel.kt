package com.cubicj.coffeenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class CoffeeNoteViewModel(
    private val coffeeBeansNoteDao: CoffeeBeansNoteDao,
    private val coffeeBeansDao: CoffeeBeansDao
) : ViewModel() {

    // selectedBeanId 상태 추가
    private val _selectedBeanId = MutableStateFlow<Long?>(null)

    // selectedNoteNames 상태 추가
    private val _selectedNoteNames = MutableStateFlow<List<String>>(emptyList())
    val selectedNoteNames: StateFlow<List<String>> = _selectedNoteNames
    val _allNotes = MutableStateFlow<List<CoffeeBeansNote>>(emptyList())

    // checkedNotes를 ViewModel에서 관리
    private val _checkedNotes = MutableStateFlow<List<CoffeeBeansNote>>(emptyList())
    val checkedNotes: StateFlow<List<CoffeeBeansNote>> = _checkedNotes.asStateFlow()

    private val _searchText = MutableStateFlow("")

    val filteredNotes: StateFlow<List<CoffeeBeansNote>> =
        combine(_allNotes, _searchText, selectedNoteNames, _checkedNotes) { allNotes, searchText, selectedNoteNames, checkedNotes ->
            val filtered = if (searchText.isBlank()) {
                allNotes
            } else {
                allNotes.filter { it.notename.contains(searchText, ignoreCase = true) }
            }
            // 필터링된 노트 목록에서 checkedNotes에 포함된 노트는 항상 isChecked를 true로 설정
            filtered.map { note ->
                val isChecked = note in checkedNotes || note.notename in selectedNoteNames
                note.copy(isChecked = isChecked)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteListState: StateFlow<List<CoffeeBeansNote>> =
        combine(_allNotes, selectedNoteNames) { allNotes, selectedNoteNames ->
            allNotes.map { note ->
                note.copy(isChecked = note.notename in selectedNoteNames)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // selectedBeanId 설정 함수 추가
    fun setSelectedBeanId(beanId: Long?) {
        _selectedBeanId.value = beanId
    }
    init {
        viewModelScope.launch {
            _selectedBeanId.collectLatest { beanId ->
                if (beanId != null) {
                    val noteNameColorMap = withContext(Dispatchers.IO) {
                        coffeeBeansDao.getCoffeeBeanById(beanId)?.noteNameColorMap ?: emptyMap()
                    }
                    _selectedNoteNames.value = noteNameColorMap.keys.toList()
                } else {
                    _selectedNoteNames.value = emptyList()
                }
                coffeeBeansNoteDao.getAllFlow()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
                    .collect { allNotes ->
                        _allNotes.value = allNotes
                    }
            }
        }
    }

    fun setSelectedNoteNames(noteNames: List<String>) {
        _selectedNoteNames.value = noteNames
    }



    fun insertCoffeeBeansNote(coffeeBeansNote: CoffeeBeansNote) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansNoteDao.insert(coffeeBeansNote)
            // 새로운 노트 추가 후 _allNotes 및 _selectedNoteNames 업데이트
            _allNotes.value = coffeeBeansNoteDao.getAll()
        }
    }

    fun updateCoffeeBeansNote(coffeeBeansNote: CoffeeBeansNote) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansNoteDao.update(coffeeBeansNote)

            // 노트 업데이트 후 _allNotes 다시 로드하여 noteListState 업데이트
            _allNotes.value = coffeeBeansNoteDao.getAll()
        }
    }

    fun deleteCoffeeBeansNote(coffeeBeansNote: CoffeeBeansNote) {
        viewModelScope.launch(Dispatchers.IO) {
            coffeeBeansNoteDao.delete(coffeeBeansNote)
            _allNotes.value = coffeeBeansNoteDao.getAll()
        }
    }

    fun updateSelectedNoteNames(noteName: String, isChecked: Boolean) {
        val currentList = _selectedNoteNames.value.toMutableList()
        if (isChecked) {
            if (!currentList.contains(noteName)) {
                currentList.add(noteName)
            }
        } else {
            currentList.remove(noteName)
        }
        _selectedNoteNames.value = currentList
        _checkedNotes.value = _allNotes.value.filter { it.isChecked }
    }

    fun handleUpdatedNoteNames(updatedNoteNames: List<String>?) {
        updatedNoteNames?.let {
            _selectedNoteNames.value = it
        }
    }

    fun filterNotesBySearchText(searchText: String) {
        _searchText.value = searchText
    }
}

// CoffeeNoteViewModelFactory 수정
class CoffeeNoteViewModelFactory(
    private val coffeeBeansNoteDao: CoffeeBeansNoteDao,
    private val coffeeBeansDao: CoffeeBeansDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoffeeNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoffeeNoteViewModel(coffeeBeansNoteDao, coffeeBeansDao) as T // CoffeeBeansDao 전달
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
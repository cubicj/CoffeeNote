package com.cubicj.coffeenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeInfoViewModel(private val recipeDao: RecipeDao) : ViewModel() {
    private val _recipe = MutableStateFlow<Recipe?>(null) // 초기값은 null로 설정
    val recipe: StateFlow<Recipe?> = _recipe

    fun loadRecipeById(recipeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipe = recipeDao.getRecipeById(recipeId)
            _recipe.value = recipe
        }
    }
}

// RecipeInfoViewModelFactory 추가
class RecipeInfoViewModelFactory(private val recipeDao: RecipeDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeInfoViewModel(recipeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
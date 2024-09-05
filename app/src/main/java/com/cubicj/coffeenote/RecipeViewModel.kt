package com.cubicj.coffeenote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeViewModel(
    private val recipeDao: RecipeDao,
    private val drinkPersonGroupDao: DrinkPersonGroupDao
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeWithDetails>>(emptyList())
    val recipes: StateFlow<List<RecipeWithDetails>> = _recipes

    var selectedBeanId: Long = -1L

    private val _drinkPersonGroups = MutableStateFlow<List<DrinkPersonGroup>>(emptyList())
    val drinkPersonGroups: StateFlow<List<DrinkPersonGroup>> = _drinkPersonGroups

    init {
        viewModelScope.launch {
            _drinkPersonGroups.value = withContext(Dispatchers.IO) { // 백그라운드 스레드에서 실행
                drinkPersonGroupDao.getAll()
            }
            _recipes.value = withContext(Dispatchers.IO) {  // 백그라운드 스레드에서 실행
                recipeDao.getRecipesWithDetailsByBeanId(selectedBeanId)
                    .sortedByDescending { it.recipe.date }
            }
        }
    }

    // 마신 사람 그룹 업데이트
    fun updateDrinkPersonGroup(group: DrinkPersonGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            drinkPersonGroupDao.update(group)
            _drinkPersonGroups.value = drinkPersonGroupDao.getAll() // 업데이트 후 다시 로드
        }
    }

    fun insertRecipe(recipe: Recipe, handDripDetails: HandDripRecipeDetails? = null, aeropressDetails: AeropressRecipeDetails? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedRecipeId = recipeDao.insert(recipe)
            recipe.id = insertedRecipeId

            when (recipe.brewMethod) {
                "handdrip" -> {
                    handDripDetails?.let {
                        it.recipeId = insertedRecipeId
                        recipeDao.insertHandDripRecipeDetails(it)
                    }
                }
                "aeropress" -> {
                    aeropressDetails?.let {
                        it.recipeId = insertedRecipeId
                        recipeDao.insertAeropressRecipeDetails(it)
                    }
                }
            }

            val newRecipeWithDetails = RecipeWithDetails(
                recipe = recipe,
                handDripDetails = if (recipe.brewMethod == "handdrip") handDripDetails else null,
                aeropressDetails = if (recipe.brewMethod == "aeropress") aeropressDetails else null
            )
            _recipes.value = recipeDao.getRecipesWithDetailsByBeanId(selectedBeanId)
                .sortedByDescending { it.recipe.date }
        }
    }

    // 레시피 업데이트 - 상세 정보 엔티티도 함께 업데이트
    fun updateRecipe(recipe: Recipe, details: Any?) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.update(recipe)

            when (details) {
                is HandDripRecipeDetails -> recipeDao.updateHandDripRecipeDetails(details)
                is AeropressRecipeDetails -> recipeDao.updateAeropressRecipeDetails(details)
            }

            _recipes.value = _recipes.value.map {
                if (it.recipe.id == recipe.id) {
                    RecipeWithDetails(
                        recipe = recipe,
                        handDripDetails = details as? HandDripRecipeDetails,
                        aeropressDetails = details as? AeropressRecipeDetails
                    )
                } else {
                    it
                }
            }
            _recipes.value = recipeDao.getRecipesWithDetailsByBeanId(selectedBeanId)
        }
    }

    // 레시피 삭제 - 상세 정보 엔티티도 함께 삭제
    fun deleteRecipe(recipeWithDetails: RecipeWithDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.delete(recipeWithDetails.recipe)

            when (recipeWithDetails.recipe.brewMethod) {
                "handdrip" -> recipeWithDetails.handDripDetails?.let { recipeDao.deleteHandDripRecipeDetails(it) }
                "aeropress" -> recipeWithDetails.aeropressDetails?.let { recipeDao.deleteAeropressRecipeDetails(it) }
            }
            _recipes.value = recipeDao.getRecipesWithDetailsByBeanId(selectedBeanId)
        }
    }

    // ID로 마신 사람 그룹 가져오기
    suspend fun getDrinkPersonGroupById(groupId: Long): DrinkPersonGroup? =
        withContext(Dispatchers.IO) {
            drinkPersonGroupDao.getById(groupId)
        }

    // 마신 사람별 레시피 필터링
    fun filterRecipesByDrinkPerson(beanId: Long, drinkPerson: String): Flow<List<RecipeWithDetails>> {
        return flow {
            val filteredRecipesWithDetails = if (drinkPerson == "전체") {
                recipeDao.getRecipesWithDetailsByBeanId(beanId)
            } else {
                recipeDao.getRecipesWithDetailsByBeanId(beanId)
                    .filter { it.recipe.drinkPerson == drinkPerson }
            }
            emit(filteredRecipesWithDetails)
        }.flowOn(Dispatchers.IO)
    }
    // 마신 사람 그룹 데이터베이스 업데이트
    fun updateDrinkPersonGroupInDb(newPersonName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentGroup = drinkPersonGroupDao.getById(1)
            if (currentGroup != null) {
                val updatedGroupNames = currentGroup.groupNames.toMutableList()
                    .apply { add(newPersonName) }
                currentGroup.groupNames = updatedGroupNames.distinct()
                drinkPersonGroupDao.update(currentGroup)
            }
        }
    }

    // 마신 사람 그룹에서 특정 사람 삭제
    fun deleteDrinkPersonFromGroup(personName: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentGroup = drinkPersonGroupDao.getById(1) // groupId는 필요에 따라 변경 가능
        if (currentGroup != null) {
            val updatedGroupNames = currentGroup.groupNames.toMutableList()
                .apply { remove(personName) }
            currentGroup.groupNames = updatedGroupNames
            drinkPersonGroupDao.update(currentGroup)

            _drinkPersonGroups.value = drinkPersonGroupDao.getAll() // 업데이트 후 다시 로드
        }
    }

    fun updateRecipeMemo(recipeId: Long, memo: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.updateMemo(recipeId, memo)
        }
    }

}

// RecipeViewModelFactory
class RecipeViewModelFactory(
    private val recipeDao: RecipeDao,
    private val drinkPersonGroupDao: DrinkPersonGroupDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(recipeDao, drinkPersonGroupDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
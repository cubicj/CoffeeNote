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
            _drinkPersonGroups.value = drinkPersonGroupDao.getAll()
        }
    }

    // 마신 사람 그룹 업데이트
    fun updateDrinkPersonGroup(group: DrinkPersonGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            drinkPersonGroupDao.update(group)
            _drinkPersonGroups.value = drinkPersonGroupDao.getAll() // 업데이트 후 다시 로드
        }
    }

    // 새로운 레시피 추가 - HandDripRecipeDetails 처리 추가
    fun insertRecipe(recipe: Recipe, handDripDetails: HandDripRecipeDetails? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedRecipeId = recipeDao.insert(recipe) // Recipe 먼저 삽입하여 id 생성
            recipe.id = insertedRecipeId

            handDripDetails?.let {
                it.recipeId = insertedRecipeId // 생성된 recipeId 설정
                recipeDao.insertHandDripRecipeDetails(it)
            }

            // RecipeWithDetails 객체 생성 및 리스트 업데이트 (handDripDetails만 사용)
            val newRecipeWithDetails = RecipeWithDetails(
                recipe = recipe,
                handDripDetails = handDripDetails,
                aeropressDetails = null
            )
            _recipes.value = _recipes.value + newRecipeWithDetails
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

            _recipes.value = _recipes.value.filter { it.recipe.id != recipeWithDetails.recipe.id }
        }
    }

    // ID로 마신 사람 그룹 가져오기
    suspend fun getDrinkPersonGroupById(groupId: Long): DrinkPersonGroup? =
        withContext(Dispatchers.IO) {
            drinkPersonGroupDao.getById(groupId)
        }

    // 마신 사람별 레시피 필터링
    fun filterRecipesByDrinkPerson(beanId: Long, drinkPerson: String): Flow<List<Recipe>> {
        return flow {
            val recipes = recipeDao.getRecipesByBeanId(beanId)
            val filteredRecipes = if (drinkPerson == "전체") {
                recipes
            } else {
                recipes.filter { it.drinkPerson == drinkPerson }
            }
            emit(filteredRecipes)
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
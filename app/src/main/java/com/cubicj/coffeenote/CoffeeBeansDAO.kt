package com.cubicj.coffeenote

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface CoffeeBeansDao {
    // CoffeeBeans 엔티티 관련 메소드
    @Query("select * from coffeeBeans")
    fun getAll(): List<CoffeeBeans>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beans: CoffeeBeans): Long

    @Delete
    suspend fun delete(beans: CoffeeBeans)

    @Update
    suspend fun update(beans: CoffeeBeans)

    @Query("SELECT * FROM coffeeBeans ORDER BY `원두_이름` ASC")
    fun getAllOrderByNameAscFlow(): Flow<List<CoffeeBeans>>

    @Query("SELECT * FROM coffeeBeans WHERE id = :id")
    suspend fun getById(id: Long): CoffeeBeans?

    @Query("SELECT * FROM coffeeBeans WHERE id = :id")
    suspend fun getCoffeeBeanById(id: Long): CoffeeBeans?
}

@Dao
interface RecipeDao {
    // Recipe 엔티티 관련 메소드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Query("SELECT * FROM recipes WHERE beanId = :beanId")
    fun getRecipesByBeanId(beanId: Long): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeById(recipeId: Long): Recipe?

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("UPDATE recipes SET 메모 = :memo WHERE id = :recipeId")
    suspend fun updateMemo(recipeId: Long, memo: String?)

    // HandDripRecipeDetails 관련 메서드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHandDripRecipeDetails(details: HandDripRecipeDetails)

    @Update
    suspend fun updateHandDripRecipeDetails(details: HandDripRecipeDetails)

    @Delete
    suspend fun deleteHandDripRecipeDetails(details: HandDripRecipeDetails)

    // AeropressRecipeDetails 관련 메서드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAeropressRecipeDetails(details: AeropressRecipeDetails)

    @Update
    suspend fun updateAeropressRecipeDetails(details: AeropressRecipeDetails)

    @Delete
    suspend fun deleteAeropressRecipeDetails(details: AeropressRecipeDetails)

    // 특정 Recipe ID에 대한 상세 정보 조회 메서드 (필요에 따라 추가)
    @Transaction
    @Query("""
        SELECT * FROM recipes 
        LEFT JOIN handdrip_recipe_details ON recipes.id = handdrip_recipe_details.recipeId
        LEFT JOIN aeropress_recipe_details ON recipes.id = aeropress_recipe_details.recipeId
        WHERE recipes.id = :recipeId
    """)
    fun getRecipeWithDetailsById(recipeId: Long): RecipeWithDetails?

    @Transaction
    @Query("SELECT * FROM recipes WHERE beanId = :beanId")
    fun getRecipesWithDetailsByBeanId(beanId: Long): List<RecipeWithDetails>
}

@Dao
interface DrinkPersonGroupDao {
    // DrinkPersonGroup 엔티티 관련 메소드
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: DrinkPersonGroup): Long

    @Query("SELECT * FROM drink_person_groups")
    suspend fun getAll(): List<DrinkPersonGroup>

    @Query("SELECT * FROM drink_person_groups WHERE id = :groupId")
    fun getById(groupId: Long): DrinkPersonGroup?

    @Update
    suspend fun update(group: DrinkPersonGroup)

    @Delete
    suspend fun delete(group: DrinkPersonGroup)
}

@Dao
interface CoffeeBeansNoteDao {
    @Query("SELECT * FROM coffeeBeansNote")
    fun getAll(): List<CoffeeBeansNote>

    @Query("SELECT * FROM coffeeBeansNote WHERE id = :id")
    fun getById(id: Long): CoffeeBeansNote?

    @Insert
    suspend fun insert(note: CoffeeBeansNote): Long

    @Update
    suspend fun update(note: CoffeeBeansNote)

    @Delete
    suspend fun delete(note: CoffeeBeansNote)

    @Query("SELECT * FROM coffeeBeansNote")
    fun getAllFlow(): Flow<List<CoffeeBeansNote>>

    @Query("SELECT * FROM coffeeBeansNote WHERE id IN (:noteIds)")
    fun getCoffeeBeansNotesByIds(noteIds: List<Long>): Flow<List<CoffeeBeansNote>>
}
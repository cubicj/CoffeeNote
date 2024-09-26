package com.cubicj.coffeenote

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    CoffeeBeans::class,
    Recipe::class,
    DrinkPersonGroup::class,
    CoffeeBeansNote::class,
    HandDripRecipeDetails::class,
    AeropressRecipeDetails::class
                     ],
    version = 4, exportSchema = false)
abstract class BeansDb: RoomDatabase() {

    abstract fun coffeeBeansDao(): CoffeeBeansDao
    abstract fun recipeDao(): RecipeDao
    abstract fun drinkPersonGroupDao(): DrinkPersonGroupDao
    abstract fun coffeeBeansNoteDao(): CoffeeBeansNoteDao

    companion object {
        @Volatile
        private var INSTANCE: BeansDb? = null

        fun getInstance(context: Context): BeansDb =
            INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(context.applicationContext, BeansDb::class.java, "coffeebeens.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
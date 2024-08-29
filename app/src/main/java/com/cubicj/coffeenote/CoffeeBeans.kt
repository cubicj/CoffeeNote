package com.cubicj.coffeenote

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Entity(tableName = "coffeeBeans")
@TypeConverters(Converters::class)
data class CoffeeBeans(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "원두_이름") var name: String = "",
    @ColumnInfo(name = "원두_정보") var content: ByteArray? = null,
    @ColumnInfo(name = "원두_노트") val noteNameColorMap: Map<String, Int> = emptyMap()
)

@Entity(
    tableName = "recipes",
    foreignKeys = [ForeignKey(
        entity = CoffeeBeans::class,
        parentColumns = ["id"],
        childColumns = ["beanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["beanId"])]
)
@TypeConverters(Converters::class)
data class Recipe(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "beanId") var beanId: Long?,
    @ColumnInfo(name = "날짜") var date: Instant? = Instant.now(),
    @ColumnInfo(name = "온도") var temp: String,
    @ColumnInfo(name = "점수") var score: String,
    @ColumnInfo(name = "점수_상대_x") var scoreRelativeX: Float? = 0f,
    @ColumnInfo(name = "점수_상대_y") var scoreRelativeY: Float? = 0f,
    @ColumnInfo(name = "마신_사람") var drinkPerson: String,
    @ColumnInfo(name = "메모") var memo: String? = null,
    @ColumnInfo(name = "추출_방식") var brewMethod: String? = null
)

// HandDripRecipeDetails 엔티티
@Entity(tableName = "handdrip_recipe_details",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recipeId"])]
)
data class HandDripRecipeDetails(
    @PrimaryKey var recipeId: Long, // Recipe 엔티티의 id와 1:1 관계
    @ColumnInfo(name = "그라인더") var selectedgrinder: String,
    @ColumnInfo(name = "분쇄도") var grindervalue: String,
    @ColumnInfo(name = "브루잉_시간") var recordedTimes: List<Int> = emptyList(),
    @ColumnInfo(name = "푸어한_양") var recordedAmounts: List<Int> = emptyList()
)

// AeropressRecipeDetails 엔티티
@Entity(tableName = "aeropress_recipe_details",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recipeId"])]
)
data class AeropressRecipeDetails(
    @PrimaryKey var recipeId: Long, // Recipe 엔티티의 id와 1:1 관계
    @ColumnInfo(name = "그라인더") var selectedgrinder: String,
    @ColumnInfo(name = "분쇄도") var grindervalue: String,
    @ColumnInfo(name = "브루잉_시간") var recordedTimes: List<Int> = emptyList(),
    @ColumnInfo(name = "푸어한_양") var recordedAmounts: List<Int> = emptyList(),
    @ColumnInfo(name = "뒤집기_여부") var inverted: Boolean = false,
    @ColumnInfo(name = "압력_유지_시간") var pressTime: String? = null,
    @ColumnInfo(name = "필터_종류") var selectedfilter: String? = null
)

data class RecipeWithDetails(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val handDripDetails: HandDripRecipeDetails?,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val aeropressDetails: AeropressRecipeDetails?
)

@Entity(tableName = "drink_person_groups")
@TypeConverters(Converters::class)
data class DrinkPersonGroup(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "그룹_이름") var groupNames: List<String>
)

@Parcelize
@Entity(tableName = "coffeeBeansNote")
data class CoffeeBeansNote(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "노트_이름") var notename: String = "",
    @ColumnInfo(name = "노트_색깔") var colorcode: Int = 0,
    @ColumnInfo(name = "체크_여부") var isChecked: Boolean = false
) : Parcelable
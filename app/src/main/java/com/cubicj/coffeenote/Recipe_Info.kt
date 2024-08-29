package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cubicj.coffeenote.databinding.ActivityRecipeInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class Recipe_Info : AppCompatActivity() {

    private var mBinding: ActivityRecipeInfoBinding? = null
    private val binding get() = mBinding!!
    private var coffeebeansDb: BeansDb? = null

    private val viewModel: RecipeInfoViewModel by viewModels {
        RecipeInfoViewModelFactory(coffeebeansDb?.recipeDao()!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityRecipeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val infoback = binding.ibRecipeInfoBack
        val selectedRecipeId = intent.getLongExtra("selectedRecipe", -1L)
        coffeebeansDb = BeansDb.getInstance(this)

        infoback.setOnClickListener {
            finish()
        }

        viewModel.loadRecipeById(selectedRecipeId)

        lifecycleScope.launch {
            viewModel.recipe.collect { recipe ->
                recipe?.let {
                    val formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.getDefault())
                        .withZone(ZoneId.systemDefault())
                    binding.tvInfoDate.text = it.date?.let { formatter.format(it) } ?: "날짜 없음"
                    binding.tvInfoTemp.text = "${it.temp}°C"
                    binding.tvInfoGrinder.text = it.grinder

                    val iconTempResource = when (it.iconTemp) {
                        "hot" -> R.drawable.hot
                        "ice" -> R.drawable.ice
                        else -> R.drawable.hot
                    }
                    binding.ivInfoHotorice.setImageResource(iconTempResource)

                    val recordedTimesString = it.recordedTimes.joinToString(" - ")
                    binding.tvInfoTime.text = "추출 시간(초): $recordedTimesString"

                    val recordedAmountsString = it.recordedAmounts.joinToString(" - ")
                    binding.tvInfoPour.text = "푸어링 양(ml): $recordedAmountsString"

                    Log.d("Recipe_Info", "Received scoreRelativeX: ${it.scoreRelativeX}, scoreRelativeY: ${it.scoreRelativeY}")
                    binding.scorePointView.scoreRelativeX = it.scoreRelativeX ?: 0f
                    binding.scorePointView.scoreRelativeY = it.scoreRelativeY ?: 0f

                    binding.scorePointView.invalidate()

                    binding.tvInfoDrinkPerson.text = it.drinkPerson
                    val memo = it.memo
                    binding.tvMemoview.text = "메모: $memo"
                }
            }
        }
    }
}


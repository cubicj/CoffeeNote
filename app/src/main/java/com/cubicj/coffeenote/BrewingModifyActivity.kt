package com.cubicj.coffeenote

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrewingModifyActivity : AppCompatActivity() {
    private var recipeId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.brewing_modify)

        recipeId = intent.getLongExtra("recipeId", -1L)
        val brewingTimeRecorderView = findViewById<BrewingTimeRecorderView>(R.id.brewingTimeRecorder)
        val brewingInsert = findViewById<Button>(R.id.btn_brewing_insert)
        val brewingCancel = findViewById<Button>(R.id.btn_brewing_cancel)
        val db = BeansDb.getInstance(applicationContext)
        val pouringAmountRecorderView = findViewById<PouringAmountRecorderView>(R.id.pouringAmountRecorder)

        brewingInsert.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val recordedTimes = brewingTimeRecorderView?.getRecordedTimes() ?: emptyList()
                val recordedAmount = pouringAmountRecorderView?.getRecordedAmounts() ?: emptyList()

                val existingRecipe = db.recipeDao().getRecipeById(recipeId)

                existingRecipe?.let {
                    it.recordedTimes = recordedTimes
                    it.recordedAmounts = recordedAmount
                    db.recipeDao().update(it)
                }
            }
            finish()
        }

        brewingCancel.setOnClickListener {
            finish()
        }
    }
}
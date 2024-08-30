package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cubicj.coffeenote.databinding.ListCoffeeRecipeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecipeAdapter(
    val context: ActivityRecipe,
    private val viewModel: RecipeViewModel
): RecyclerView.Adapter<RecipeAdapter.Holder>() {

    private var recipes: MutableList<Recipe> = mutableListOf()
    private var dateInfoAlertDialog: AlertDialog? = null
    private var tempInfoAlertDialog: AlertDialog? = null
    private var scoreInfoAlertDialog: AlertDialog? = null
    private var selectedInstant: Instant? = null
    private var initialValue: Int? = 0
    private var deleterecipeinfoAlertDialog: AlertDialog? = null
    private var drinkInfoAlertDialog: AlertDialog? = null
    private var radioInfoAlertDialog: AlertDialog? = null
    var currentSortMode = RecipeSortMode.DATE_DESC
    private var radioDeleteAlertDialog: AlertDialog? = null

    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    })

    init {
        context.lifecycleScope.launch {
            context.repeatOnLifecycle(Lifecycle.State.STARTED) {
            }
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context ?: context)
        val binding = ListCoffeeRecipeBinding.inflate(inflater, parent, false)

        return Holder(binding).apply {
            binding.root.setOnClickListener {
                val curPos: Int = adapterPosition
                // recipes 리스트가 비어있지 않은지 확인 후 접근
                if (curPos in 0 until differ.currentList.size) {
                    val recipe = differ.currentList[curPos]

                    val intent = Intent(context, Recipe_Info::class.java)
                    Log.d("RecipeAdapter", "Sending recipeId: ${recipe.id}")
                    intent.putExtra("selectedRecipe", recipe.id)
                    context.startActivity(intent)
                }
            }

            binding.root.setOnLongClickListener {
                val curPos: Int = adapterPosition
                if (curPos in 0 until differ.currentList.size) {
                    val recipe = differ.currentList[curPos]

                    val recipeinfoAlertDialog: AlertDialog?

                    val recipeInfoSelectView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.recipe_info_select, null)
                    val recipeInfoSelectBuilder = AlertDialog.Builder(parent.context)
                        .setView(recipeInfoSelectView)

                    recipeinfoAlertDialog = recipeInfoSelectBuilder.create()

                    recipeinfoAlertDialog.show()

                    val recipebasemodify =
                        recipeinfoAlertDialog.findViewById<Button>(R.id.btn_recipe_select_basemodify)
                    val recipedelete =
                        recipeinfoAlertDialog.findViewById<Button>(R.id.btn_recipe_select_delete)
                    val recipeBack =
                        recipeinfoAlertDialog.findViewById<ImageButton>(R.id.ib_recipe_info_select_back)


                    recipebasemodify?.setOnClickListener {
                        val dialogView =
                            LayoutInflater.from(context)
                                .inflate(R.layout.modify_coffee_recipe, null)
                        val modifyDateButton = dialogView.findViewById<Button>(R.id.btn_info_date)
                        val modifyTempButton = dialogView.findViewById<Button>(R.id.btn_info_temp)
                        val modifyGrinderButton =
                            dialogView.findViewById<Button>(R.id.btn_info_grinder)
                        val modifyDrinkPersonButton =
                            dialogView.findViewById<Button>(R.id.btn_drink_person)
                        val modifyScoreButton = dialogView.findViewById<Button>(R.id.btn_info_score)
                        val modifyHotoriceRadioGroup =
                            dialogView.findViewById<RadioGroup>(R.id.rg_hotorice)
                        val modifyConfirmButton =
                            dialogView.findViewById<Button>(R.id.btn_coffee_recipe_save)
                        val modifyCancelButton =
                            dialogView.findViewById<Button>(R.id.btn_coffee_recipe_cancel)
                        var updatedRelativeTouchX: Float? = null
                        var updatedRelativeTouchY: Float? = null
                        val modifyrecipeBack =
                            dialogView.findViewById<ImageButton>(R.id.ib_custom_recipe_back)
                        var drinkPerson = recipe.drinkPerson
                        modifyDrinkPersonButton.text = drinkPerson

                        initialValue = recipe.temp.toIntOrNull() ?: 0

                        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.getDefault())
                            .withZone(ZoneId.systemDefault())
                        modifyDateButton.text = formatter.format(recipe.date)
                        modifyTempButton.text = "${recipe.temp}°C"
                        modifyScoreButton.text =
                            String.format("%.2f", recipe.score.toFloatOrNull() ?: 0f)



                        val modifyDialog = AlertDialog.Builder(context)
                            .setView(dialogView)
                            .create()

                        modifyDateButton.setOnClickListener {
                            val dateInfoSelectView =
                                LayoutInflater.from(context).inflate(R.layout.info_calender, null)
                            val dateInfoSelectBuilder = AlertDialog.Builder(context)
                                .setView(dateInfoSelectView)

                            dateInfoAlertDialog = dateInfoSelectBuilder.create()

                            dateInfoAlertDialog?.show()

                            val calendarView =
                                dateInfoSelectView.findViewById<CalendarView>(R.id.cv_info_date)
                            val insertButton =
                                dateInfoSelectView.findViewById<Button>(R.id.btn_calendar_insert)
                            val cancelButton =
                                dateInfoSelectView.findViewById<Button>(R.id.btn_calendar_cancel)
                            val backButton =
                                dateInfoSelectView.findViewById<ImageButton>(R.id.ib_calendar_back)

                            selectedInstant = LocalDate.parse(modifyDateButton.text, formatter)
                                .atStartOfDay(ZoneId.systemDefault()).toInstant()

                            calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                selectedInstant =
                                    selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                            }

                            insertButton.setOnClickListener {
                                selectedInstant?.let {
                                    modifyDateButton.text = formatter.format(it)
                                }
                                dateInfoAlertDialog?.dismiss()
                            }
                            cancelButton.setOnClickListener {
                                dateInfoAlertDialog?.dismiss()
                            }
                            backButton.setOnClickListener {
                                dateInfoAlertDialog?.dismiss()
                            }
                        }
                        modifyTempButton.setOnClickListener {
                            val tempInfoSelectView =
                                LayoutInflater.from(context).inflate(R.layout.temp_select, null)
                            val tempInfoSelectBuilder = AlertDialog.Builder(context)
                                .setView(tempInfoSelectView)

                            tempInfoAlertDialog = tempInfoSelectBuilder.create()

                            tempInfoAlertDialog?.show()

                            val tempPicker =
                                tempInfoSelectView.findViewById<NumberPicker>(R.id.np_coffee_temp)
                            val confirmButton =
                                tempInfoSelectView.findViewById<Button>(R.id.btn_temp_confirm)
                            val cancelButton =
                                tempInfoSelectView.findViewById<Button>(R.id.btn_temp_calcel)
                            val backButton =
                                tempInfoSelectView.findViewById<ImageButton>(R.id.ib_temp_back)

                            tempPicker.minValue = 0
                            tempPicker.maxValue = 100
                            tempPicker.value = initialValue ?: 90 // initialValue 사용

                            confirmButton.setOnClickListener {
                                modifyTempButton.text = "${tempPicker.value}°C"
                                tempInfoAlertDialog?.dismiss()
                            }
                            cancelButton.setOnClickListener {
                                tempInfoAlertDialog?.dismiss()
                            }
                            backButton.setOnClickListener {
                                tempInfoAlertDialog?.dismiss()
                            }
                        }

                        modifyGrinderButton.setOnClickListener {
                            val grinderSelectView =
                                LayoutInflater.from(context)
                                    .inflate(R.layout.dialog_grinder_picker, null)
                            val grinderSelectBuilder = AlertDialog.Builder(context)
                                .setView(grinderSelectView)

                            val grinderAlertDialog = grinderSelectBuilder.create()
                            grinderAlertDialog.show()

                            val seekBar =
                                grinderSelectView.findViewById<SeekBar>(R.id.sb_grinder_picker)
                            val grinderValue =
                                grinderSelectView.findViewById<TextView>(R.id.tv_grinder_value)
                            val confirmButton =
                                grinderSelectView.findViewById<Button>(R.id.btn_grinder_confirm)
                            val cancelButton =
                                grinderSelectView.findViewById<Button>(R.id.btn_grinder_calcel)
                            val backButton =
                                grinderSelectView.findViewById<ImageButton>(R.id.ib_grinder_back)


                            seekBar.setOnSeekBarChangeListener(object :
                                SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(
                                    seekBar: SeekBar?,
                                    progress: Int,
                                    fromUser: Boolean
                                ) {
                                    val grind = progress + 1
                                    val group = (grind + 2) / 3
                                    val step = (grind + 2) % 3
                                    val grindValueText = "${group}-${step}"
                                    grinderValue.text = grindValueText
                                    modifyGrinderButton.text = grindValueText // "x-y" 형태로 설정

                                    if (fromUser) { // 사용자에 의해 값이 변경된경우에만 진동 발생
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator // Vibrator 객체 가져오기
                                        vibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                50,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    }
                                }

                                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                            })

                            confirmButton.setOnClickListener {
                                val updatedGrinder =
                                    "${(seekBar.progress + 3) / 3}-${(seekBar.progress + 3) % 3}"
                                modifyGrinderButton.text =
                                    updatedGrinder // 수정된 grinder 값으로버튼 텍스트 업데이트
                                grinderAlertDialog.dismiss()
                            }

                            cancelButton.setOnClickListener {
                                grinderAlertDialog.dismiss()
                            }

                            backButton.setOnClickListener {
                                grinderAlertDialog.dismiss()
                            }
                        }

                        modifyDrinkPersonButton.setOnClickListener {
                            val drinkInfoSelectView =
                                LayoutInflater.from(context).inflate(R.layout.drink_person, null)
                            val drinkInfoSelectBuilder = AlertDialog.Builder(context)
                                .setView(drinkInfoSelectView)

                            drinkInfoAlertDialog = drinkInfoSelectBuilder.create()

                            val drinkPersonPlus =
                                drinkInfoSelectView.findViewById<ImageButton>(R.id.ib_drink_plus)
                            val drinkPersonGroup =
                                drinkInfoSelectView.findViewById<RadioGroup>(R.id.rdg_drink_person)
                            val drinkPersonConfirm =
                                drinkInfoSelectView.findViewById<Button>(R.id.btn_drink_insert)
                            val drinkPersonCancel =
                                drinkInfoSelectView.findViewById<Button>(R.id.btn_drink_cancel)

                            drinkPersonGroup.setOnHierarchyChangeListener(object :
                                ViewGroup.OnHierarchyChangeListener {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onChildViewAdded(parent: View?, child: View?) {
                                    child?.setOnLongClickListener {
                                        val radioDeleteSelectView = LayoutInflater.from(context)
                                            .inflate(R.layout.delete_alert, null)
                                        val radioDeleteSelectBuilder =
                                            AlertDialog.Builder(context)
                                                .setView(radioDeleteSelectView)

                                        radioDeleteAlertDialog = radioDeleteSelectBuilder.create()

                                        radioDeleteAlertDialog?.show()

                                        val radioDeleteConfirm =
                                            radioDeleteSelectView.findViewById<Button>(R.id.btn_delete_confirm)
                                        val radioDeleteCancel =
                                            radioDeleteSelectView.findViewById<Button>(R.id.btn_delete_cancel)

                                        radioDeleteConfirm.setOnClickListener {
                                            val selectedRadioButton = (child as? RadioButton)
                                            if (selectedRadioButton != null) {
                                                context.lifecycleScope.launch {
                                                    // ViewModel의 함수를 호출하여 데이터베이스에서 삭제 및 업데이트
                                                    viewModel.deleteDrinkPersonFromGroup(selectedRadioButton.text.toString())

                                                    withContext(Dispatchers.Main) {
                                                        drinkPersonGroup.removeView(child)
                                                        radioDeleteAlertDialog?.dismiss()
                                                    }
                                                }
                                            }
                                        }
                                        radioDeleteCancel.setOnClickListener {
                                            radioDeleteAlertDialog?.dismiss()
                                        }
                                        true
                                    }
                                }


                                override fun onChildViewRemoved(p0: View?, p1: View?) {
                                }
                            })

                            drinkPersonPlus.setOnClickListener {
                                val radioInfoSelectView =
                                    LayoutInflater.from(context)
                                        .inflate(R.layout.radio_insert, null)
                                val radioInfoSelectBuilder = AlertDialog.Builder(context)
                                    .setView(radioInfoSelectView)

                                radioInfoAlertDialog = radioInfoSelectBuilder.create()
                                radioInfoAlertDialog?.show()

                                val radioEditText =
                                    radioInfoSelectView.findViewById<EditText>(R.id.et_radio_add)
                                val radioAddButton =
                                    radioInfoSelectView.findViewById<Button>(R.id.btn_add_radio)
                                val radioCancelButton =
                                    radioInfoSelectView.findViewById<Button>(R.id.btn_cancel_radio)

                                radioAddButton.setOnClickListener {
                                    val newPersonName = radioEditText.text.toString()
                                    if (newPersonName.isNotBlank()) {
                                        val radioButton = RadioButton(context)
                                        radioButton.text = newPersonName
                                        drinkPersonGroup.addView(radioButton)

                                        // 데이터베이스 업데이트 (뷰 모델 사용)
                                        viewModel.updateDrinkPersonGroupInDb(newPersonName)

                                        radioInfoAlertDialog?.dismiss()
                                    } else {
                                        Toast.makeText(context, "이름을 입력하세요.", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                                radioCancelButton.setOnClickListener {
                                    radioInfoAlertDialog?.dismiss()
                                }
                            }
                            drinkPersonConfirm.setOnClickListener {
                                val selectedRadioButtonId = drinkPersonGroup.checkedRadioButtonId
                                if (selectedRadioButtonId != -1) {
                                    val selectedRadioButton =
                                        drinkInfoSelectView.findViewById<RadioButton>(
                                            selectedRadioButtonId
                                        )
                                    modifyDrinkPersonButton!!.text = selectedRadioButton.text
                                    drinkPerson = selectedRadioButton.text.toString()
                                } else {
                                    Toast.makeText(context, "마신 사람을 선택하세요.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                drinkInfoAlertDialog?.dismiss()
                            }
                            drinkPersonCancel.setOnClickListener {
                                drinkInfoAlertDialog?.dismiss()
                            }
                            drinkInfoAlertDialog?.show()
                        }

                        modifyScoreButton.setOnClickListener {
                            val scoreInfoSelectView =
                                LayoutInflater.from(context)
                                    .inflate(R.layout.touch_point_view_layout, null)
                            val scoreInfoSelectBuilder = AlertDialog.Builder(context)
                                .setView(scoreInfoSelectView)

                            scoreInfoAlertDialog = scoreInfoSelectBuilder.create()
                            scoreInfoAlertDialog?.show()

                            val localTouchPointView =
                                scoreInfoSelectView.findViewById<TouchPointView>(R.id.touch_point_view)
                            val confirmButton =
                                scoreInfoSelectView.findViewById<Button>(R.id.btn_score_confirm)
                            val calcelButton =
                                scoreInfoSelectView.findViewById<Button>(R.id.btn_score_cancel)
                            val backButton =
                                scoreInfoSelectView.findViewById<ImageButton>(R.id.ib_score_back)

                            // 기존 터치 포인트 설정 (필요한 경우)
                            val touchPointX = recipe.scoreRelativeX ?: 0.5f
                            val touchPointY = recipe.scoreRelativeY ?: 0.5f
                            localTouchPointView.setTouchPoint(touchPointX, touchPointY)

                            confirmButton.setOnClickListener {
                                val xDistance = localTouchPointView.getXDistanceFromOrigin()
                                val yDistance = localTouchPointView.getYDistanceFromOrigin()

                                // 첫 번째 코드의 계산 방식 적용
                                val xScore =
                                    (10 - (xDistance / (localTouchPointView.width / 2f) * 10)).coerceAtLeast(
                                        0f
                                    ).toFloat()
                                val yScore =
                                    (10 - (yDistance / (localTouchPointView.height / 2f) * 10)).coerceAtLeast(
                                        0f
                                    ).toFloat()

                                val modifiedScore =
                                    ((xScore + yScore) / 2).coerceAtMost(10f).toFloat()
                                updatedRelativeTouchX = localTouchPointView.getRelativeTouchX()
                                updatedRelativeTouchY = localTouchPointView.getRelativeTouchY()

                                modifyScoreButton.text = String.format("%.2f", modifiedScore)
                                scoreInfoAlertDialog?.dismiss()
                            }

                            calcelButton.setOnClickListener {
                                scoreInfoAlertDialog?.dismiss()
                            }

                            backButton.setOnClickListener {
                                scoreInfoAlertDialog?.dismiss()
                            }
                        }


                        modifyConfirmButton.setOnClickListener {
                            context.lifecycleScope.launch(Dispatchers.IO) {
                                recipe.date = LocalDate.parse(modifyDateButton.text, formatter)
                                    .atStartOfDay(ZoneId.systemDefault()).toInstant()
                                recipe.temp = modifyTempButton.text.toString().replace("°C", "")
                                recipe.score = modifyScoreButton.text.toString()
                                recipe.scoreRelativeX =
                                    updatedRelativeTouchX ?: recipe.scoreRelativeX
                                recipe.scoreRelativeY =
                                    updatedRelativeTouchY ?: recipe.scoreRelativeY
                                recipe.drinkPerson = drinkPerson

                                Log.d(
                                    "RecipeAdapter",
                                    "Updating scoreRelativeX: ${recipe.scoreRelativeX}, scoreRelativeY: ${recipe.scoreRelativeY}"
                                )


                                withContext(Dispatchers.Main) {
                                    modifyDialog.dismiss()
                                    recipeinfoAlertDialog.dismiss()
                                }
                            }
                        }
                        modifyCancelButton.setOnClickListener {
                            modifyDialog.dismiss()
                        }
                        modifyrecipeBack.setOnClickListener {
                            modifyDialog.dismiss()
                        }
                        modifyDialog.show()
                    }




                    recipedelete?.setOnClickListener {
                        // 삭제할 레시피 가져오기 (differ 사용)
                        val recipeToDelete = differ.currentList[curPos]

                        val deleterecipeInfoSelectView =
                            LayoutInflater.from(context).inflate(R.layout.delete_alert, null)
                        val deleterecipeInfoSelectBuilder = AlertDialog.Builder(context)
                            .setView(deleterecipeInfoSelectView)

                        deleterecipeinfoAlertDialog = deleterecipeInfoSelectBuilder.create()

                        deleterecipeinfoAlertDialog?.show()

                        val deleteconfirm =
                            deleterecipeInfoSelectView.findViewById<Button>(R.id.btn_delete_confirm)
                        val deletecancel =
                            deleterecipeInfoSelectView.findViewById<Button>(R.id.btn_delete_cancel)

                        deleteconfirm.setOnClickListener {

                            deleterecipeinfoAlertDialog!!.dismiss()
                            recipeinfoAlertDialog.dismiss()
                        }

                        deletecancel.setOnClickListener {
                            deleterecipeinfoAlertDialog!!.dismiss()
                        }
                    }

                    recipeBack?.setOnClickListener {
                        recipeinfoAlertDialog.dismiss()
                    }
                }
                true
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val recipe = differ.currentList[position]
        holder.setRecipe(recipe)
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        // AsyncListDiffer를 사용하여 변경 사항 계산 및 적용
        differ.submitList(newRecipes.toList()) // toList()를 추가하여 새로운 리스트를 생성
    }


    inner class Holder(private val binding: ListCoffeeRecipeBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun setRecipe(recipe: Recipe) {
            val formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
            val roundedScore = String.format("%.2f", recipe.score.toFloatOrNull() ?: 0f)

            binding.tvRecipeDate.text = formatter.format(recipe.date) // recipe.date 사용
            binding.tvRecipeScore.text = roundedScore // roundedScore 사용
            binding.tvDrinkPerson.text = recipe.drinkPerson
        }
    }
}

package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cubicj.coffeenote.databinding.ActivityRecipeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecipeActivity:AppCompatActivity(), RecipeInsertListener {
    private var mBinding: ActivityRecipeBinding? = null
    private var coffeebeansDb: BeansDb? = null
    private var mAlertDialog: AlertDialog? = null
    private var scoreAlertDialog: AlertDialog? = null
    private lateinit var mAdapter: RecipeAdapter
    private val binding get() = mBinding!!
    private var selectedBeanId: Long = -1L
    private lateinit var vibrator: Vibrator
    private var selectDrinkPerson: Button? = null
    private var drinkPerson = "나"
    private var currentSortMode = RecipeSortMode.DATE_DESC
    var bottomSheetDialog: BottomSheetDialog? = null
    val drinkAlertDialog: AlertDialog? = null

    private val viewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(
            coffeebeansDb?.recipeDao()!!,
            coffeebeansDb?.drinkPersonGroupDao()!!
        )
    }


    override fun onRecipeInserted(recipe: Recipe) {
        // ViewModel을 통해 레시피 추가 후 자동으로 목록 갱신되도록 수정
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, "입력되었습니다.", Toast.LENGTH_SHORT).show()
            mAlertDialog!!.dismiss()

            drinkPerson = "나"
            selectDrinkPerson?.text = drinkPerson
        }
    }

    fun showCoffeeRecipeDialog(){
        val brewMethodSelectView = LayoutInflater.from(this).inflate(R.layout.select_method, null)
        val brewMethodSelectBuilder = AlertDialog.Builder(this).setView(brewMethodSelectView)
        val brewMethodAlertDialog = brewMethodSelectBuilder.create()
        brewMethodAlertDialog.show()

        val handdripButton = brewMethodSelectView.findViewById<Button>(R.id.btn_method_hand)
        val aeropressButton = brewMethodSelectView.findViewById<Button>(R.id.btn_method_aero)

        handdripButton.setOnClickListener {
            brewMethodAlertDialog.dismiss()
            val dialog = HandDripRecipeDialogFragment()
            dialog.show(supportFragmentManager, "HandDripRecipeDialogFragment")
        }

        aeropressButton.setOnClickListener {
            brewMethodAlertDialog.dismiss()
            showAeroInputDialog() // 에어로프레스 선택 시 레시피 입력 다이얼로그 표시
        }
    }

    private fun showAeroInputDialog(){

    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 다이얼로그를 닫아줍니다.
        mAlertDialog?.dismiss()
        scoreAlertDialog?.dismiss()
    }

    private fun updateRecipeList() {
        // recipes 갱신 시 UI 업데이트 (RecipeAdapter의 updateRecipes 함수 활용)
        lifecycleScope.launchWhenStarted {
            viewModel.recipes.collect { recipes ->
                val sortedRecipes = when (currentSortMode) {
                    RecipeSortMode.DATE_DESC -> recipes.sortedByDescending { it.recipe.date }
                    RecipeSortMode.SCORE_DESC -> recipes.sortedByDescending { it.recipe.score.toFloatOrNull() ?: 0f }
                }
                // RecipeWithDetails -> Recipe 변환하여 Adapter에 전달
                mAdapter.updateRecipes(sortedRecipes.map { it.recipe })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupBottomSheetRadioButtons(
        selectDrinkGroup: RadioGroup,
        drinkPersonGroups: List<DrinkPersonGroup>
    ) {
        selectDrinkGroup.removeAllViews() // 기존 라디오 버튼 모두 제거

        val bottomSheetDialog: BottomSheetDialog? = null

        // "전체" 라디오 버튼 추가
        val allRadioButton = RadioButton(this)
        allRadioButton.text = "전체"
        selectDrinkGroup.addView(allRadioButton, 0) // 맨 위에 추가

        drinkPersonGroups.forEach { group ->
            val uniqueNames = group.groupNames.toSet().toList() // 중복 제거
            uniqueNames.forEach { name ->
                val radioButton = RadioButton(this)
                radioButton.text = name
                selectDrinkGroup.addView(radioButton)
            }
        }


        // BottomSheetDialog가 처음 표시될 때 "전체" 라디오 버튼 선택
        bottomSheetDialog?.setOnShowListener {
            allRadioButton.isChecked = true
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coffeebeansDb = BeansDb.getInstance(this)
        selectedBeanId = intent.getLongExtra("selectedBeanId", -1L)
        viewModel.selectedBeanId = selectedBeanId

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.btnOrderDate.setBackgroundColor(Color.RED)
        binding.btnOrderScore.setBackgroundColor(Color.GRAY)
        binding.btnOrderDrinkPerson.setBackgroundColor(Color.GRAY)

        val bottomSheetView = layoutInflater.inflate(R.layout.select_drink_person, null)
        bottomSheetDialog = BottomSheetDialog(this@RecipeActivity)
        bottomSheetDialog?.setContentView(bottomSheetView)

        lifecycleScope.launch(Dispatchers.IO) {
            val initialGroup = coffeebeansDb?.drinkPersonGroupDao()?.getById(1)
            if (initialGroup == null) {
                val newGroup = DrinkPersonGroup(groupNames = listOf("나"))
                coffeebeansDb?.drinkPersonGroupDao()?.insert(newGroup)
            }

            withContext(Dispatchers.Main) {
                mAdapter = RecipeAdapter(this@RecipeActivity, viewModel)
                binding.rvRecipe.adapter = mAdapter
                binding.rvRecipe.layoutManager =
                    LinearLayoutManager(this@RecipeActivity, LinearLayoutManager.VERTICAL, false)
                binding.rvRecipe.setHasFixedSize(true)

                updateRecipeList()

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.drinkPersonGroups.collect { drinkPersonGroups ->
                            withContext(Dispatchers.Main) {
                                // BottomSheetDialog가 표시될 때만 라디오 버튼 설정
                                if (bottomSheetDialog?.isShowing == true) {
                                    val selectDrinkGroup =
                                        bottomSheetView.findViewById<RadioGroup>(R.id.rdg_select_person)
                                    setupBottomSheetRadioButtons(selectDrinkGroup, drinkPersonGroups)
                                }

                                // drinkAlertDialog가 표시될 때만 라디오 버튼 설정
                                if (drinkAlertDialog?.isShowing == true) {
                                    val drinkPersonGroup =
                                        drinkAlertDialog?.findViewById<RadioGroup>(R.id.rdg_drink_person)
                                    setupBottomSheetRadioButtons(drinkPersonGroup!!, drinkPersonGroups)
                                }
                            }
                        }
                    }
                }
            }
            binding.btnOrderDate.setOnClickListener {
                currentSortMode = RecipeSortMode.DATE_DESC
                updateRecipeList() // 정렬 방식 변경 후 레시피 목록 업데이트
                binding.btnOrderDate.setBackgroundColor(Color.RED)
                binding.btnOrderScore.setBackgroundColor(Color.GRAY)
            }

            binding.btnOrderScore.setOnClickListener {
                currentSortMode = RecipeSortMode.SCORE_DESC
                updateRecipeList() // 정렬 방식 변경 후 레시피 목록 업데이트
                binding.btnOrderDate.setBackgroundColor(Color.GRAY)
                binding.btnOrderScore.setBackgroundColor(Color.RED)
            }
            binding.btnOrderDrinkPerson.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        val selectDrinkGroup =
                            bottomSheetView.findViewById<RadioGroup>(R.id.rdg_select_person)
                        val selectDrinkConfirm =
                            bottomSheetView.findViewById<Button>(R.id.btn_select_person_confirm)
                        val selectDrinkCancel =
                            bottomSheetView.findViewById<Button>(R.id.btn_select_person_cancel)

                        val currentDrinkPersonGroups = viewModel.drinkPersonGroups.value ?: emptyList()
                        setupBottomSheetRadioButtons(selectDrinkGroup, currentDrinkPersonGroups)

                        // 확인 버튼 클릭 이벤트 처리
                        selectDrinkConfirm.setOnClickListener {
                            val selectedRadioButtonId = selectDrinkGroup.checkedRadioButtonId
                            val selectedText = if (selectedRadioButtonId != -1) {
                                bottomSheetView.findViewById<RadioButton>(selectedRadioButtonId).text.toString()
                            } else {
                                "" // 아무것도 선택하지 않은 경우
                            }

                            binding.btnOrderDrinkPerson.text = selectedText
                            bottomSheetDialog?.dismiss()

                            // 선택된 마신 사람에 따라 레시피 필터링
                            lifecycleScope.launchWhenStarted {
                                viewModel.filterRecipesByDrinkPerson(selectedBeanId, selectedText).collect { filteredRecipes ->
                                // 필터링된 레시피 목록 업데이트
                                }
                            }
                        }

                        // 취소 버튼 클릭 이벤트 처리
                        selectDrinkCancel.setOnClickListener {
                            bottomSheetDialog?.dismiss()
                        }
                    }
                }
                bottomSheetDialog?.show()
            }

            binding.btnRecipeBack.setOnClickListener {
                finish()
            }
            binding.ibRecipePlus.setOnClickListener {
                showCoffeeRecipeDialog()
            }

        }
    }
}

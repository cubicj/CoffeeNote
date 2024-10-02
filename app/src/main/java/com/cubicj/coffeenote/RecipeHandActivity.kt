package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.CustomHandRecipeBinding

class RecipeHandActivity : AppCompatActivity() {
    private lateinit var binding: CustomHandRecipeBinding
    private var currentTemp: Int = 90

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CustomHandRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // 뒤로 가기 버튼
        binding.ibCustomRecipeBack.setOnClickListener {
            finish()
        }

        // 날짜 선택 버튼
        binding.btnInfoDate.setOnClickListener {
            showDatePickerDialog()
        }

        // 온도 버튼 초기 텍스트 설정
        updateTempButtonText()

        // 온도 선택 버튼
        binding.btnInfoTemp.setOnClickListener {
            showTempPickerDialog()
        }

        // 분쇄도 선택 버튼
        binding.btnInfoGrinder.setOnClickListener {
            showGrinderPickerDialog()
        }

        // 점수 선택 버튼
        binding.btnInfoScore.setOnClickListener {
            // 점수 선택 다이얼로그 표시
        }

        // 마신 사람 선택 버튼
        binding.btnDrinkPerson.setOnClickListener {
            // 마신 사람 선택 다이얼로그 표시
        }

        // 시간 및 양 설정 버튼
        binding.btnTimeamountset.setOnClickListener {
            // 시간 및 양 설정 다이얼로그 표시
        }

        // 저장 버튼
        binding.btnCoffeeRecipeSave.setOnClickListener {
            saveRecipe()
        }

        // 취소 버튼
        binding.btnCoffeeRecipeCancel.setOnClickListener {
            finish()
        }

        // 라디오 그룹 리스너 설정
        binding.rgHotorice.setOnCheckedChangeListener { group, checkedId ->
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = InfoCalendarDialogFragment()
        datePickerDialog.setOnDateSelectedListener { selectedDate ->
            binding.btnInfoDate.text = selectedDate
        }
        datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
    }

    private fun showTempPickerDialog() {
        val tempPickerDialog = SelectTempDialogFragment.newInstance(currentTemp)
        tempPickerDialog.setOnTempSelectedListener { selectedTemp ->
            currentTemp = selectedTemp
            updateTempButtonText()
        }
        tempPickerDialog.show(supportFragmentManager, "TempPickerDialog")
    }

    private fun updateTempButtonText() {
        binding.btnInfoTemp.text = "${currentTemp}°C"
    }

    private fun showGrinderPickerDialog() {
        val grinderPickerDialog = SelectGrinderDialogFragment()
        grinderPickerDialog.show(supportFragmentManager, "GrinderPickerDialog")
    }

    private fun saveRecipe() {
        finish()
    }

    fun updateGrinderSelection(grinderInfo: String) {
        binding.btnInfoGrinder.text = grinderInfo
    }
}
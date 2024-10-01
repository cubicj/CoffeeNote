package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.CustomHandRecipeBinding
import com.cubicj.coffeenote.InfoCalendarDialogFragment

class RecipeHandActivity : AppCompatActivity() {
    private lateinit var binding: CustomHandRecipeBinding

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

        // 온도 선택 버튼
        binding.btnInfoTemp.setOnClickListener {
            // 온도 선택 다이얼로그 표시
        }

        // 분쇄도 선택 버튼
        binding.btnInfoGrinder.setOnClickListener {
            // 분쇄도 선택 다이얼로그 표시
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

    private fun saveRecipe() {
        finish()
    }
}
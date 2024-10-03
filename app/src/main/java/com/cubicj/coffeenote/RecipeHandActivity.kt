package com.cubicj.coffeenote

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
        setupSpinner()
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
            showDrinkPersonDialog()
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
    }

    private fun setupSpinner() {
        val spinner: Spinner = binding.spinnerCoffeeType
        ArrayAdapter.createFromResource(
            this,
            R.array.hand_coffee_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                when (selectedItem) {
                    "핫 필터커피" -> handleHotFilterCoffee()
                    "아이스 필터커피" -> handleIceFilterCoffee()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        }
    }

    private fun handleHotFilterCoffee() {
        // 핫 필터커피가 선택되었을 때의 처리
    }

    private fun handleIceFilterCoffee() {
        // 아이스 필터커피가 선택되었을 때의 처리
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

    private fun showDrinkPersonDialog() {
        val drinkPersonDialog = DrinkPersonDialogFragment()
        drinkPersonDialog.show(supportFragmentManager, DrinkPersonDialogFragment.TAG)
    }
}
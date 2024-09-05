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
    private var dateAlertDialog: AlertDialog? = null
    private var tempAlertDialog: AlertDialog? = null
    private var grinderAlertDialog: AlertDialog? = null
    private var scoreAlertDialog: AlertDialog? = null
    private lateinit var mAdapter: RecipeAdapter
    private val binding get() = mBinding!!
    private var selectedBeanId: Long = -1L
    private var selectedInstant: Instant? = Instant.now()
    private var currentTemp = 90
    private var currentGrinderStep = -1
    private var roundedScore = 0f
    private var relativeTouchX = 0f
    private var relativeTouchY = 0f
    private lateinit var vibrator: Vibrator
    private var drinkAlertDialog: AlertDialog? = null
    private var radioAlertDialog: AlertDialog? = null
    private var selectDrinkPerson: Button? = null
    private var drinkPerson = "나"
    private var radioDeleteAlertDialog: AlertDialog? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var currentSortMode = RecipeSortMode.DATE_DESC
    private var tempselectedgrinder = ""
    private var tempgrindervalue = ""
    private var coffeerecipegrinder: Button? = null

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
            showHandInputDialog() // 핸드드립 선택 시 레시피 입력 다이얼로그 표시
        }

        aeropressButton.setOnClickListener {
            brewMethodAlertDialog.dismiss()
            showAeroInputDialog() // 에어로프레스 선택 시 레시피 입력 다이얼로그 표시
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "DefaultLocale")
    private fun showHandInputDialog() {
        val mRecipeSelectView =
            LayoutInflater.from(this).inflate(R.layout.custom_hand_recipe, null)
        val mRecipeSelectBuilder = AlertDialog.Builder(this)
            .setView(mRecipeSelectView)

        mAlertDialog = mRecipeSelectBuilder.create()

        mAlertDialog?.show()

        selectedBeanId = intent.getLongExtra("selectedBeanId", -1L)
        viewModel.selectedBeanId = selectedBeanId

        if (currentGrinderStep == -1) { // 값이 없는 경우
            currentGrinderStep = 13 // 5-0에 해당하는 값 (5 * 3 - 2)
        }
        val coffeeRecipeDate = mRecipeSelectView.findViewById<Button>(R.id.btn_info_date)
        val coffeeRecipeTemp = mRecipeSelectView.findViewById<Button>(R.id.btn_info_temp)
        coffeerecipegrinder = mRecipeSelectView.findViewById(R.id.btn_info_grinder)
        val coffeerecipescore = mRecipeSelectView.findViewById<Button>(R.id.btn_info_score)
        val coffeerecipecancel =
            mRecipeSelectView?.findViewById<Button>(R.id.btn_coffee_recipe_cancel)
        val coffeerecipesave = mRecipeSelectView?.findViewById<Button>(R.id.btn_coffee_recipe_save)
        val coffeerecipeiconTemp = mRecipeSelectView?.findViewById<RadioGroup>(R.id.rg_hotorice)
        val iv_recipe_hotorice = mRecipeSelectView.findViewById<ImageView>(R.id.iv_recipe_hotorice)
        val coffeerecipeBack =
            mRecipeSelectView.findViewById<ImageButton>(R.id.ib_custom_recipe_back)
        var selectedIconTemp = true
        selectDrinkPerson = mRecipeSelectView?.findViewById(R.id.btn_drink_person)
        drinkPerson = "나"
        selectDrinkPerson?.text = drinkPerson

        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

        coffeeRecipeDate?.text = formatter.format(Instant.now())
        coffeeRecipeTemp?.text = "${currentTemp}°C"
        coffeerecipegrinder?.text = "" // 버튼 텍스트 초기값 설정
        coffeerecipescore?.text = String.format("%.2f", roundedScore)


        coffeeRecipeDate?.setOnClickListener {
            val dateSelectView =
                LayoutInflater.from(this).inflate(R.layout.info_calender, null)
            val dateSelectBuilder = AlertDialog.Builder(this)
                .setView(dateSelectView)

            dateAlertDialog = dateSelectBuilder.create()

            dateAlertDialog?.show()

            val calendarView = dateSelectView.findViewById<CalendarView>(R.id.cv_info_date)
            val insertButton = dateSelectView.findViewById<Button>(R.id.btn_calendar_insert)
            val cancelButton = dateSelectView.findViewById<Button>(R.id.btn_calendar_cancel)
            val backButton = dateSelectView.findViewById<ImageButton>(R.id.ib_calendar_back)

            calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                selectedInstant = selectedDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant() // 선택된 날짜로 selectedInstant 업데이트
            }

            insertButton?.setOnClickListener {
                coffeeRecipeDate.text =
                    formatter.format(selectedInstant) // selectedInstant가 null이 아니므로 항상 포맷팅 가능
                dateAlertDialog?.dismiss()
            }
            cancelButton?.setOnClickListener {
                dateAlertDialog?.dismiss()
            }
            backButton?.setOnClickListener {
                dateAlertDialog?.dismiss()
            }
        }

        coffeeRecipeTemp?.setOnClickListener {
            val tempSelectView =
                LayoutInflater.from(this).inflate(R.layout.temp_select, null)
            val tempSelectBuilder = AlertDialog.Builder(this)
                .setView(tempSelectView)

            tempAlertDialog = tempSelectBuilder.create()

            tempAlertDialog?.show()

            val tempPicker = tempSelectView.findViewById<NumberPicker>(R.id.np_coffee_temp)
            val confirmButton = tempSelectView.findViewById<Button>(R.id.btn_temp_confirm)
            val cancelButton = tempSelectView.findViewById<Button>(R.id.btn_temp_calcel)
            val backButton = tempSelectView.findViewById<ImageButton>(R.id.ib_temp_back)

            tempPicker?.minValue = 0
            tempPicker?.maxValue = 100
            tempPicker?.value = currentTemp
            tempPicker?.isHapticFeedbackEnabled = true

            tempPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
                vibrator.vibrate( // 외부에서 초기화된 vibrator 사용
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }

            confirmButton?.setOnClickListener {
                currentTemp = tempPicker!!.value
                coffeeRecipeTemp.text = "${currentTemp}°C"
                tempAlertDialog?.dismiss()
            }

            cancelButton?.setOnClickListener {
                tempAlertDialog?.dismiss()
            }
            backButton?.setOnClickListener {
                tempAlertDialog?.dismiss()
            }
        }


        coffeerecipeiconTemp?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_coffee_recipe_hot -> {
                    iv_recipe_hotorice?.setImageResource(R.drawable.hot)
                    selectedIconTemp = true
                }

                R.id.rbtn_coffee_recipe_ice -> {
                    iv_recipe_hotorice?.setImageResource(R.drawable.ice)
                    selectedIconTemp = false
                }
            }
        }

        coffeerecipegrinder?.setOnClickListener {
            val grinderSelectView = LayoutInflater.from(this).inflate(R.layout.select_grinder, null)
            val grinderSelectBuilder = AlertDialog.Builder(this).setView(grinderSelectView)
            grinderAlertDialog = grinderSelectBuilder.create()
            grinderAlertDialog?.show()

            val fellowOdd2Button = grinderSelectView.findViewById<Button>(R.id.btn_fellow_odd2)
            val itop03Button = grinderSelectView.findViewById<Button>(R.id.btn_itop_03)
            val timemoreC3Button = grinderSelectView.findViewById<Button>(R.id.btn_timemore_c3)
            val selectgrinderback = grinderSelectView.findViewById<ImageButton>(R.id.ib_select_grinder_back)

            fellowOdd2Button.setOnClickListener {
                showGrinderSettingDialog("Fellow Odd 2")
            }

            itop03Button.setOnClickListener {
                showGrinderSettingDialog("ITOP 03")
            }

            timemoreC3Button.setOnClickListener {
                showGrinderSettingDialog("Timemore C3 Esp Pro")
            }
            selectgrinderback.setOnClickListener {
                grinderAlertDialog?.dismiss()
            }
        }

        selectDrinkPerson?.setOnClickListener {
            val drinkSelectView = LayoutInflater.from(this).inflate(R.layout.drink_person, null)
            val drinkSelectBuilder = AlertDialog.Builder(this)
                .setView(drinkSelectView)

            drinkAlertDialog = drinkSelectBuilder.create()

            val drinkPersonPlus = drinkSelectView.findViewById<ImageButton>(R.id.ib_drink_plus)
            val drinkPersonGroup =
                drinkSelectView.findViewById<RadioGroup>(R.id.rdg_drink_person)
            val drinkPersonConfirm = drinkSelectView.findViewById<Button>(R.id.btn_drink_insert)
            val drinkPersonCancel = drinkSelectView.findViewById<Button>(R.id.btn_drink_cancel)

            val currentDrinkPersonGroups = viewModel.drinkPersonGroups.value ?: emptyList()
            setupBottomSheetRadioButtons(drinkPersonGroup, currentDrinkPersonGroups)

            drinkPersonGroup.setOnHierarchyChangeListener(object :
                ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    child?.setOnLongClickListener {
                        val radioDeleteSelectView = LayoutInflater.from(this@RecipeActivity)
                            .inflate(R.layout.delete_alert, null)
                        val radioDeleteSelectBuilder = AlertDialog.Builder(this@RecipeActivity)
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
                                lifecycleScope.launch {
                                    // ViewModel의 함수를 호출하여 데이터베이스에서 삭제 및 업데이트
                                    viewModel.deleteDrinkPersonFromGroup(selectedRadioButton.text.toString())

                                    withContext(Dispatchers.Main) {
                                        drinkPersonGroup.removeView(child) // UI에서 삭제
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
                val radioSelectView =
                    LayoutInflater.from(this).inflate(R.layout.radio_insert, null)
                val radioSelectBuilder = AlertDialog.Builder(this)
                    .setView(radioSelectView)

                radioAlertDialog = radioSelectBuilder.create()

                radioAlertDialog?.show()

                val radioEditText = radioSelectView.findViewById<EditText>(R.id.et_radio_add)
                val radioAdd = radioSelectView.findViewById<Button>(R.id.btn_add_radio)
                val radioCancel = radioSelectView.findViewById<Button>(R.id.btn_cancel_radio)

                radioAdd.setOnClickListener {
                    val newPersonName = radioEditText.text.toString()
                    if (newPersonName.isNotBlank()) {
                        val radioButton = RadioButton(this)
                        radioButton.text = newPersonName
                        drinkPersonGroup.addView(radioButton)

                        viewModel.updateDrinkPersonGroupInDb(newPersonName)
                    } else {
                        Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                    radioAlertDialog?.dismiss() // 다이얼로그 닫기
                }
                radioCancel.setOnClickListener {
                    radioAlertDialog?.dismiss()
                }
            }
            drinkPersonConfirm.setOnClickListener {
                val selectedRadioButtonId = drinkPersonGroup.checkedRadioButtonId
                if (selectedRadioButtonId != -1) {
                    val selectedRadioButton =
                        drinkSelectView.findViewById<RadioButton>(selectedRadioButtonId)
                    selectDrinkPerson!!.text = selectedRadioButton.text
                    drinkPerson = selectedRadioButton.text.toString()
                } else {
                    Toast.makeText(this, "마신 사람을 선택하세요.", Toast.LENGTH_SHORT).show()
                }
                drinkAlertDialog?.dismiss()
            }
            drinkPersonCancel.setOnClickListener {
                drinkAlertDialog?.dismiss()
            }


            lifecycleScope.launch(Dispatchers.IO) {
                val currentGroup = coffeebeansDb?.drinkPersonGroupDao()?.getById(1)
                if (currentGroup != null) {
                    withContext(Dispatchers.Main) {
                        val drinkPersonGroup =
                            drinkAlertDialog?.findViewById<RadioGroup>(R.id.rdg_drink_person)
                        drinkPersonGroup?.removeAllViews()
                        currentGroup.groupNames.forEach { name ->
                            val radioButton = RadioButton(this@RecipeActivity)
                            radioButton.text = name
                            drinkPersonGroup?.addView(radioButton)

                            // drinkPerson 값과 일치하는 라디오 버튼을 선택
                            if (name == drinkPerson) {
                                radioButton.isChecked = true
                            }
                        }
                    }
                }
            }
            drinkAlertDialog?.show()
        }

        coffeerecipescore?.setOnClickListener {
            val scoreSelectView =
                LayoutInflater.from(this).inflate(R.layout.touch_point_view_layout, null)
            val scoreSelectBuilder = AlertDialog.Builder(this)
                .setView(scoreSelectView)

            scoreAlertDialog = scoreSelectBuilder.create()

            scoreAlertDialog?.show()

            val touchPointView =
                scoreSelectView.findViewById<TouchPointView>(R.id.touch_point_view)
            val confirmButton = scoreSelectView.findViewById<Button>(R.id.btn_score_confirm)
            val cancelButton = scoreSelectView.findViewById<Button>(R.id.btn_score_cancel)
            val backButton = scoreSelectView.findViewById<ImageButton>(R.id.ib_score_back)

            confirmButton!!.setOnClickListener {
                val xDistance = touchPointView!!.getXDistanceFromOrigin()
                val yDistance = touchPointView.getYDistanceFromOrigin()

                relativeTouchX = touchPointView.getRelativeTouchX()
                relativeTouchY = touchPointView.getRelativeTouchY()

                Log.d(
                    "ActivityRecipe",
                    "Setting scoreRelativeX: $relativeTouchX, scoreRelativeY: $relativeTouchY"
                )

                val xScore =
                    (10 - (xDistance / (touchPointView.width / 2f) * 10)).coerceAtLeast(0f)
                        .toFloat()

                val yScore =
                    (10 - (yDistance / (touchPointView.height / 2f) * 10)).coerceAtLeast(0f)
                        .toFloat()

                roundedScore = ((xScore + yScore) / 2).coerceAtMost(10f).toFloat()
                relativeTouchX = touchPointView.getRelativeTouchX()
                relativeTouchY = touchPointView.getRelativeTouchY()

                coffeerecipescore.text = String.format("%.2f", roundedScore)
                scoreAlertDialog?.dismiss()
            }

            cancelButton.setOnClickListener {
                scoreAlertDialog?.dismiss()
            }

            backButton.setOnClickListener {
                scoreAlertDialog?.dismiss()
            }
        }

        coffeerecipecancel?.setOnClickListener {
            mAlertDialog?.dismiss()
        }
        coffeerecipeBack?.setOnClickListener {
            mAlertDialog?.dismiss()
        }

        coffeerecipesave?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (selectedBeanId != -1L) {
                    val newRecipe = Recipe(
                        beanId = selectedBeanId,
                        date = selectedInstant ?: Instant.now(),
                        temp = currentTemp.toString(),
                        score = roundedScore.toString(),
                        scoreRelativeX = relativeTouchX,
                        scoreRelativeY = relativeTouchY,
                        drinkPerson = drinkPerson,
                        brewMethod = "handdrip" // 추출 방식: 핸드드립
                    )

                    val handDripDetails = HandDripRecipeDetails(
                        recipeId = 0,
                        selectedgrinder = tempselectedgrinder,
                        grindervalue = tempgrindervalue,
                        recordedTimes = listOf(30, 60, 90), // 예시 값
                        recordedAmounts = listOf(50, 100, 150) // 예시 값
                    )

                    viewModel.insertRecipe(newRecipe, handDripDetails) // 뷰 모델을 통해 레시피 추가

                    withContext(Dispatchers.Main) {
                        drinkPerson = "나"
                        selectDrinkPerson?.text = drinkPerson
                        Toast.makeText(applicationContext, "입력되었습니다.", Toast.LENGTH_SHORT).show()
                        mAlertDialog?.dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "유효하지 않은 원두 ID 입니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            mAlertDialog?.setOnDismissListener {
                drinkPerson = "나"
                selectDrinkPerson?.text = drinkPerson
            }
        }
    }

    private fun showAeroInputDialog(){

    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showGrinderSettingDialog(selectedGrinder: String) {
        when (selectedGrinder) {
            "Fellow Odd 2" -> {
                val oddAlertDialog: AlertDialog?

                val oddSelectView = LayoutInflater.from(this).inflate(R.layout.dialog_grinder_picker, null)
                val oddSelectBuilder = AlertDialog.Builder(this).setView(oddSelectView)
                oddAlertDialog = oddSelectBuilder.create()
                oddAlertDialog.show()

                val seekBar = oddSelectView.findViewById<SeekBar>(R.id.sb_grinder_picker)
                val grinderValue = oddSelectView.findViewById<TextView>(R.id.tv_grinder_value)
                val oddBack = oddSelectView.findViewById<ImageButton>(R.id.ib_grinder_back)
                val oddConfirm = oddSelectView.findViewById<Button>(R.id.btn_grinder_confirm)
                val oddCancel = oddSelectView.findViewById<Button>(R.id.btn_grinder_calcel)

                seekBar.max = 30

                val initialGroup = 5
                val initialStep = 0
                grinderValue.text = "${initialGroup}-${initialStep}"
                seekBar.progress = initialGroup * 3 + initialStep - 1

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        val  grind = progress + 1
                        val group = (grind + 2) / 3
                        val step = (grind + 2) % 3
                        val grindValueText = "${group}-${step}"
                        grinderValue.text = grindValueText

                        if (fromUser) {
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }

                })

                oddConfirm.setOnClickListener {
                    val grinderString = "${(seekBar.progress + 3) / 3}-${(seekBar.progress + 3) % 3}"

                    // 선택된 그라인더 및 분쇄도 정보 저장
                    tempselectedgrinder = "Fellow Ode 2"
                    tempgrindervalue = grinderString

                    oddAlertDialog.dismiss()
                    grinderAlertDialog?.dismiss()
                    coffeerecipegrinder?.text = "$tempselectedgrinder : $tempgrindervalue"
                }
                oddCancel.setOnClickListener {
                    oddAlertDialog.dismiss()
                }
                oddBack.setOnClickListener {
                    oddAlertDialog.dismiss()
                }
            }
            "ITOP 03" -> {
                val itopAlertDialog: AlertDialog?

                val itopSelectView = LayoutInflater.from(this).inflate(R.layout.dialog_grinder_picker, null)
                val itopSelectBuilder = AlertDialog.Builder(this).setView(itopSelectView)
                itopAlertDialog = itopSelectBuilder.create()
                itopAlertDialog.show()
            }
            "Timemore C3 Esp Pro" -> {
                val timemoreAlertDialog: AlertDialog?

                val timemoreSelectView = LayoutInflater.from(this).inflate(R.layout.dialog_grinder_picker, null)
                val timemoreSelectBuilder = AlertDialog.Builder(this).setView(timemoreSelectView)
                timemoreAlertDialog = timemoreSelectBuilder.create()
                timemoreAlertDialog.show()
            }
        }
    }

    private fun setupBottomSheetRadioButtons(
        selectDrinkGroup: RadioGroup,
        drinkPersonGroups: List<DrinkPersonGroup>
    ) {
        selectDrinkGroup.removeAllViews() // 기존 라디오 버튼 모두 제거

        // "전체" 라디오 버튼 추가
        val allRadioButton = RadioButton(this@RecipeActivity)
        allRadioButton.text = "전체"
        selectDrinkGroup.addView(allRadioButton, 0) // 맨 위에 추가

        drinkPersonGroups.forEach { group ->
            val uniqueNames = group.groupNames.toSet().toList() // 중복 제거
            uniqueNames.forEach { name ->
                val radioButton = RadioButton(this@RecipeActivity)
                radioButton.text = name
                selectDrinkGroup.addView(radioButton)
            }
        }


        // BottomSheetDialog가 처음 표시될 때 "전체" 라디오 버튼 선택
        bottomSheetDialog?.setOnShowListener {
            allRadioButton.isChecked = true
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 다이얼로그를 닫아줍니다.
        drinkAlertDialog?.dismiss()
        radioAlertDialog?.dismiss()
        mAlertDialog?.dismiss()
        dateAlertDialog?.dismiss()
        tempAlertDialog?.dismiss()
        grinderAlertDialog?.dismiss()
        scoreAlertDialog?.dismiss()
    }

    private fun updateRecipeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.recipes.collect { recipes ->
                val sortedRecipes = when (currentSortMode) {
                    RecipeSortMode.DATE_DESC -> recipes.sortedByDescending { it.recipe.date }
                    RecipeSortMode.SCORE_DESC -> recipes.sortedByDescending { it.recipe.score.toFloatOrNull() ?: 0f }
                }
                mAdapter.updateRecipes(sortedRecipes) // RecipeWithDetails 리스트를 직접 전달
            }
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

package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.cubicj.coffeenote.databinding.CustomHandRecipeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HandDripRecipeDialogFragment : DialogFragment(){
    private var _binding: CustomHandRecipeBinding? = null
    private val binding get() = _binding!!

    private var listener: RecipeInsertListener? = null
    private lateinit var vibrator: Vibrator
    private var grinderAlertDialog: AlertDialog? = null
    private var tempselectedgrinder = ""
    private var tempgrindervalue = ""
    private val viewModel: RecipeViewModel by activityViewModels()
    private var coffeebeansDb: BeansDb? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is RecipeInsertListener) {
            listener = context
            coffeebeansDb = BeansDb.getInstance(context)
        } else {
            // 구현하지 않았다면 예외를 발생시킵니다.
            throw RuntimeException("$context must implement RecipeInsertListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomHandRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "UseGetLayoutInflater")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var selectedInstant: Instant? = Instant.now()
        var currentTemp = 90
        var currentGrinderStep = -1
        var roundedScore = 0f
        var relativeTouchX = 0f
        var relativeTouchY = 0f
        var drinkPerson = "나"

        var selectedIconTemp = true

        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

        binding.btnInfoDate.text = formatter.format(Instant.now())
        binding.btnInfoTemp.text = "${currentTemp}°C"
        binding.btnInfoGrinder.text = ""
        binding.btnInfoScore.text = String.format("%.2f", roundedScore)

        binding.btnInfoDate.setOnClickListener {
            val dateAlertDialog: AlertDialog?

            val dateSelectView =
                LayoutInflater.from(requireContext()).inflate(R.layout.info_calender, null)
            val dateSelectBuilder = AlertDialog.Builder(requireContext())
                .setView(dateSelectView)

            dateAlertDialog = dateSelectBuilder.create()

            dateAlertDialog.show()

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
                binding.btnInfoDate.text =
                    formatter.format(selectedInstant) // selectedInstant가 null이 아니므로 항상 포맷팅 가능
                dateAlertDialog.dismiss()
            }
            cancelButton?.setOnClickListener {
                dateAlertDialog.dismiss()
            }
            backButton?.setOnClickListener {
                dateAlertDialog.dismiss()
            }
        }

        binding.btnInfoTemp.setOnClickListener {
            val tempAlertDialog: AlertDialog?

            val tempSelectView =
                LayoutInflater.from(requireContext()).inflate(R.layout.temp_select, null)
            val tempSelectBuilder = AlertDialog.Builder(requireContext())
                .setView(tempSelectView)

            tempAlertDialog = tempSelectBuilder.create()

            tempAlertDialog.show()

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
                binding.btnInfoTemp.text = "${currentTemp}°C"
                tempAlertDialog.dismiss()
            }

            cancelButton?.setOnClickListener {
                tempAlertDialog.dismiss()
            }
            backButton?.setOnClickListener {
                tempAlertDialog.dismiss()
            }
        }
        binding.rgHotorice.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_coffee_recipe_hot -> {
                    selectedIconTemp = true
                }
                R.id.rbtn_coffee_recipe_ice -> {
                    selectedIconTemp = false
                }
            }
        }

        binding.btnInfoGrinder.setOnClickListener {
            val grinderSelectView = LayoutInflater.from(requireContext()).inflate(R.layout.select_grinder, null)
            val grinderSelectBuilder = AlertDialog.Builder(requireContext()).setView(grinderSelectView)
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

        binding.btnDrinkPerson.setOnClickListener {
            val drinkAlertDialog: AlertDialog?
            var radioDeleteAlertDialog: AlertDialog?
            var radioAlertDialog: AlertDialog?

            val drinkSelectView = LayoutInflater.from(requireContext()).inflate(R.layout.drink_person, null)
            val drinkSelectBuilder = AlertDialog.Builder(requireContext())
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
                        val radioDeleteSelectView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.delete_alert, null)
                        val radioDeleteSelectBuilder = AlertDialog.Builder(requireContext())
                            .setView(radioDeleteSelectView)

                        radioDeleteAlertDialog = radioDeleteSelectBuilder.create()

                        radioDeleteAlertDialog!!.show()

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
                                        radioDeleteAlertDialog!!.dismiss()
                                    }
                                }
                            }
                        }

                        radioDeleteCancel.setOnClickListener {
                            radioDeleteAlertDialog!!.dismiss()
                        }
                        true
                    }
                }
                override fun onChildViewRemoved(p0: View?, p1: View?) {
                }
            })

            drinkPersonPlus.setOnClickListener {
                val radioSelectView =
                    LayoutInflater.from(requireContext()).inflate(R.layout.radio_insert, null)
                val radioSelectBuilder = AlertDialog.Builder(requireContext())
                    .setView(radioSelectView)

                radioAlertDialog = radioSelectBuilder.create()

                radioAlertDialog!!.show()

                val radioEditText = radioSelectView.findViewById<EditText>(R.id.et_radio_add)
                val radioAdd = radioSelectView.findViewById<Button>(R.id.btn_add_radio)
                val radioCancel = radioSelectView.findViewById<Button>(R.id.btn_cancel_radio)

                radioAdd.setOnClickListener {
                    val newPersonName = radioEditText.text.toString()
                    if (newPersonName.isNotBlank()) {
                        val radioButton = RadioButton(requireContext())
                        radioButton.text = newPersonName
                        drinkPersonGroup.addView(radioButton)

                        viewModel.updateDrinkPersonGroupInDb(newPersonName)
                    } else {
                        Toast.makeText(requireContext(), "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
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
                    binding.btnDrinkPerson.text = selectedRadioButton.text
                    drinkPerson = selectedRadioButton.text.toString()
                } else {
                    Toast.makeText(requireContext(), "마신 사람을 선택하세요.", Toast.LENGTH_SHORT).show()
                }
                drinkAlertDialog.dismiss()
            }
            drinkPersonCancel.setOnClickListener {
                drinkAlertDialog.dismiss()
            }


            lifecycleScope.launch(Dispatchers.IO) {
                val currentGroup = coffeebeansDb?.drinkPersonGroupDao()?.getById(1)
                if (currentGroup != null) {
                    withContext(Dispatchers.Main) {
                        val drinkPersonGroup =
                            drinkAlertDialog.findViewById<RadioGroup>(R.id.rdg_drink_person)
                        drinkPersonGroup?.removeAllViews()
                        currentGroup.groupNames.forEach { name ->
                            val radioButton = RadioButton(requireContext())
                            radioButton.text = name
                            drinkPersonGroup?.addView(radioButton)

                            if (name == drinkPerson) {
                                radioButton.isChecked = true
                            }
                        }
                    }
                }
            }
            drinkAlertDialog.show()
        }

        binding.btnInfoScore.setOnClickListener {
            val scoreAlertDialog: AlertDialog?

            val scoreSelectView =
                LayoutInflater.from(requireContext()).inflate(R.layout.touch_point_view_layout, null)
            val scoreSelectBuilder = AlertDialog.Builder(requireContext())
                .setView(scoreSelectView)

            scoreAlertDialog = scoreSelectBuilder.create()

            scoreAlertDialog.show()

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

                val xScore =
                    (10 - (xDistance / (touchPointView.width / 2f) * 10)).coerceAtLeast(0f)
                        .toFloat()

                val yScore =
                    (10 - (yDistance / (touchPointView.height / 2f) * 10)).coerceAtLeast(0f)
                        .toFloat()

                roundedScore = ((xScore + yScore) / 2).coerceAtMost(10f).toFloat()
                relativeTouchX = touchPointView.getRelativeTouchX()
                relativeTouchY = touchPointView.getRelativeTouchY()

                binding.btnInfoScore.text = String.format("%.2f", roundedScore)
                scoreAlertDialog.dismiss()
            }

            cancelButton.setOnClickListener {
                scoreAlertDialog.dismiss()
            }

            backButton.setOnClickListener {
                scoreAlertDialog.dismiss()
            }
        }

        binding.btnCoffeeRecipeCancel.setOnClickListener {
            dismiss()
        }
        binding.ibCustomRecipeBack.setOnClickListener {
            dismiss()
        }

        binding.btnCoffeeRecipeSave.setOnClickListener {
            // 입력 값 검증 및 레시피 객체 생성
            val newRecipe = Recipe(
                beanId = viewModel.selectedBeanId, // ViewModel에서 selectedBeanId 가져오기
                date = selectedInstant ?: Instant.now(),
                temp = currentTemp.toString(),
                score = roundedScore.toString(),
                scoreRelativeX = relativeTouchX,
                scoreRelativeY = relativeTouchY,
                drinkPerson = drinkPerson,
                brewMethod = "handdrip"
            )

            val handDripDetails = HandDripRecipeDetails(
                recipeId = 0, // 새로운 레시피이므로 ID는 0
                selectedgrinder = tempselectedgrinder,
                grindervalue = tempgrindervalue,
                // ... (나머지 필드 설정)
            )

            listener?.onRecipeInserted(newRecipe)
            dismiss()
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "UseGetLayoutInflater")
    private fun showGrinderSettingDialog(selectedGrinder: String) {
        when (selectedGrinder) {
            "Fellow Odd 2" -> {
                val oddAlertDialog: AlertDialog?

                val oddSelectView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_grinder_picker, null)
                val oddSelectBuilder = AlertDialog.Builder(requireContext()).setView(oddSelectView)
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
                    binding.btnInfoGrinder.text = "$tempselectedgrinder : $tempgrindervalue"
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

                val itopSelectView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_grinder_picker, null)
                val itopSelectBuilder = AlertDialog.Builder(requireContext()).setView(itopSelectView)
                itopAlertDialog = itopSelectBuilder.create()
                itopAlertDialog.show()
            }
            "Timemore C3 Esp Pro" -> {
                val timemoreAlertDialog: AlertDialog?

                val timemoreSelectView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_grinder_picker, null)
                val timemoreSelectBuilder = AlertDialog.Builder(requireContext()).setView(timemoreSelectView)
                timemoreAlertDialog = timemoreSelectBuilder.create()
                timemoreAlertDialog.show()
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
        val allRadioButton = RadioButton(requireContext())
        allRadioButton.text = "전체"
        selectDrinkGroup.addView(allRadioButton, 0) // 맨 위에 추가

        drinkPersonGroups.forEach { group ->
            val uniqueNames = group.groupNames.toSet().toList() // 중복 제거
            uniqueNames.forEach { name ->
                val radioButton = RadioButton(requireContext())
                radioButton.text = name
                selectDrinkGroup.addView(radioButton)
            }
        }


        // BottomSheetDialog가 처음 표시될 때 "전체" 라디오 버튼 선택
        bottomSheetDialog?.setOnShowListener {
            allRadioButton.isChecked = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface RecipeInsertListener {
        fun onRecipeInserted(recipe: Recipe)
    }
}
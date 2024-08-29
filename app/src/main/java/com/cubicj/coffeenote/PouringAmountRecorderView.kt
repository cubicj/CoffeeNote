package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding

@SuppressLint("SetTextI18n")
class PouringAmountRecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val recordedAmountsTextView: AppCompatTextView
    val hundredsPicker: NumberPicker
    val tensPicker: NumberPicker
    val onesPicker: NumberPicker
    private val inputButton: Button
    private val recordedAmounts = mutableListOf<Int>()
    private val deleteButton: Button
    private var vibrator: Vibrator // Vibrator 추가

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        // 기록된 양을 표시할 TextView
        recordedAmountsTextView = AppCompatTextView(context).apply {
            text = "기록된 양: "
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(16)
        }

        // 삭제 버튼
        deleteButton = Button(context).apply {
            text = "삭제"
            textSize = 18f
            setOnClickListener {
                deleteLastAmount()
            }
        }

        // TextView와 삭제 버튼을 가로로 배치하기 위한 LinearLayout
        val textAndButtonLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(recordedAmountsTextView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(deleteButton)
        }
        addView(textAndButtonLayout)

        // 숫자 선택 다이얼을 담을 LinearLayout
        val pickerLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        addView(pickerLayout)

        // 백의 자리 선택 다이얼
        hundredsPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 9}
        pickerLayout.addView(hundredsPicker)

        // 십의 자리 선택 다이얼
        tensPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 9
        }
        pickerLayout.addView(tensPicker)

        // 일의 자리 선택 다이얼
        onesPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 9
        }
        pickerLayout.addView(onesPicker)

        // 입력 버튼
        inputButton = Button(context).apply {
            text = "입력"
            textSize = 18f
            setOnClickListener {
                recordAmount()
            }
        }
        addView(inputButton)

        val valueChangeListener = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
        hundredsPicker.setOnValueChangedListener(valueChangeListener)
        tensPicker.setOnValueChangedListener(valueChangeListener)
        onesPicker.setOnValueChangedListener(valueChangeListener)
    }

    private fun recordAmount() {
        val amount = hundredsPicker.value * 100 + tensPicker.value * 10 + onesPicker.value
        val previousTotalAmount = recordedAmounts.sum()
        val amountDifference = if (recordedAmounts.isEmpty()) {
            amount
        } else {
            amount - previousTotalAmount
        }

        recordedAmounts.add(amountDifference)
        updateRecordedAmountsText()
    }

    private fun updateRecordedAmountsText() {
        recordedAmountsTextView.text = "기록된 양: ${recordedAmounts.joinToString(" - ")}"
    }

    fun getRecordedAmounts(): List<Int> {
        return recordedAmounts
    }

    private fun deleteLastAmount() {
        if (recordedAmounts.isNotEmpty()) {
            recordedAmounts.removeLast()
            updateRecordedAmountsText()
        }
    }
}
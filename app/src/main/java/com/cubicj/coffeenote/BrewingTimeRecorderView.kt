package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding

@SuppressLint("SetTextI18n")
class BrewingTimeRecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private val recordedTimesTextView: TextView
    val minuteTensPicker: NumberPicker
    val minuteOnesPicker: NumberPicker
    val secondTensPicker: NumberPicker
    val secondOnesPicker: NumberPicker
    private val inputButton: Button
    private val recordedTimes = mutableListOf<Int>()
    private val deleteLastTimeButton: Button
    private var vibrator: Vibrator // Vibrator 추가

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val timeRecorderLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        addView(timeRecorderLayout)

        // 기록된 시간을 표시할 TextView
        recordedTimesTextView = AppCompatTextView(context).apply {
            text = "기록된 시간: "
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(16)
        }
        timeRecorderLayout.addView(
            recordedTimesTextView,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        ) // 가중치 1 설정

        // 직전 기록 삭제 버튼
        deleteLastTimeButton = Button(context).apply {
            text = "삭제"
            textSize = 18f
            setOnClickListener {
                deleteLastTime()
            }
        }
        timeRecorderLayout.addView(deleteLastTimeButton)

        // 숫자 선택 다이얼을 담을 LinearLayout
        val pickerLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        addView(pickerLayout)

        // 분 십의 자리 선택 다이얼
        minuteTensPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 5
        }
        pickerLayout.addView(minuteTensPicker)

        // 분 일의 자리 선택다이얼
        minuteOnesPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 9
        }
        pickerLayout.addView(
            minuteOnesPicker,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
        )

        // ':' 기호 TextView 추가
        val colonTextView = AppCompatTextView(context).apply {
            text = ":"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8)
        }
        pickerLayout.addView(
            colonTextView,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f)
        )

        // 초 십의 자리 선택 다이얼
        secondTensPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 5
        }
        pickerLayout.addView(
            secondTensPicker,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
        )

        // 초 일의 자리 선택 다이얼
        secondOnesPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 9
        }
        pickerLayout.addView(secondOnesPicker) // 가중치 1 추가

        // 입력 버튼
        inputButton = Button(context).apply {
            text = "입력"
            textSize = 18f
            setOnClickListener {
                recordTime()
            }
        }
        addView(inputButton)

        // Vibrator 초기화
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // ... (기존 코드)

        // NumberPicker에 값 변경 리스너 설정
        val valueChangeListener = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
        minuteTensPicker.setOnValueChangedListener(valueChangeListener)
        minuteOnesPicker.setOnValueChangedListener(valueChangeListener)
        secondTensPicker.setOnValueChangedListener(valueChangeListener)
        secondOnesPicker.setOnValueChangedListener(valueChangeListener)
    }

    private fun recordTime(){
        val minutes = minuteTensPicker.value * 10 + minuteOnesPicker.value
        val seconds = secondTensPicker.value * 10 + secondOnesPicker.value
        val totalSeconds = minutes * 60 + seconds

        val previousTotalSeconds = recordedTimes.sum()
        val timeDifference = if (recordedTimes.isEmpty()) {
            totalSeconds
        } else {
            totalSeconds - previousTotalSeconds
        }

        recordedTimes.add(timeDifference)
        updateRecordedTimesText()
    }

    private fun updateRecordedTimesText() {
        recordedTimesTextView.text = "기록된 시간: ${recordedTimes.joinToString(" - ")}"
    }

    fun getRecordedTimes(): List<Int> {
        return recordedTimes
    }
    private fun deleteLastTime() {
        if (recordedTimes.isNotEmpty()) {
            recordedTimes.removeLast()
            updateRecordedTimesText()
        }
    }
}
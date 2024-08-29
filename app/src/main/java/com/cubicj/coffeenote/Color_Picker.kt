package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cubicj.coffeenote.databinding.ActivityColorPickerBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener



class Color_Picker : AppCompatActivity() {

        private var nBinding: ActivityColorPickerBinding? = null

        private val binding get() = nBinding!!

        @SuppressLint("SuspiciousIndentation")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            nBinding = ActivityColorPickerBinding.inflate(layoutInflater)

            setContentView(binding.root)

            ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            val colorTextview: TextView = binding.colorTextView
            val colorView: View = binding.colorView
            val colorPickerView = binding.colorpickerview

            colorPickerView.setColorListener(ColorEnvelopeListener { envelope, fromUser ->
                colorTextview.text = envelope.hexCode
                colorView.setBackgroundColor(envelope.color)
            })

            colorPickerView.attachBrightnessSlider(binding.brigSlideBar) // findViewById 제거
            colorPickerView.attachAlphaSlider(binding.alphSlideBar)

            binding.btnColorConfirm.setOnClickListener {
                val selectedColorHex = colorTextview.text.toString()
                val colorCode = Color.parseColor("#$selectedColorHex") // 헥스 코드를 Int 색상 코드로 변환

                val intent = Intent().apply {
                    putExtra("selectedColor", colorCode) // Int 색상 코드 전달
                }
                setResult(RESULT_OK, intent)
                finish()
            }

            binding.btnColorExit.setOnClickListener { // findViewById 제거, 람다 표현식 사용
                setResult(RESULT_CANCELED)
                finish()
            }
        }
}
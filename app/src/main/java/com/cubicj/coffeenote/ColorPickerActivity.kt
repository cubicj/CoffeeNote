package com.cubicj.coffeenote

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cubicj.coffeenote.databinding.ColorPickerBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class ColorPickerActivity : AppCompatActivity() {

    private lateinit var binding: ColorPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.colorpickerview.setColorListener(ColorEnvelopeListener { envelope, fromUser ->
            val hexCode = envelope.hexCode
            binding.colorTextView.text = hexCode
            binding.colorView.setBackgroundColor(Color.parseColor("#$hexCode"))
        })

        binding.colorpickerview.attachBrightnessSlider(binding.brigSlideBar)
        binding.colorpickerview.attachAlphaSlider(binding.alphSlideBar)

        binding.btnColorConfirm.setOnClickListener {
            val selectedColorHex = binding.colorTextView.text.toString()
            val colorCode = Color.parseColor("#$selectedColorHex")

            val intent = Intent().apply {
                putExtra("selectedColor", colorCode)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.btnColorExit.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
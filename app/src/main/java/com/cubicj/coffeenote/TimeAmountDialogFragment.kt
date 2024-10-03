package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.BrewingPourCheckBinding

class TimeAmountDialogFragment : DialogFragment() {

    private var _binding: BrewingPourCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BrewingPourCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNumberPickers()
        setupButtons()
    }

    private fun setupNumberPickers() {
        // 분 설정 (0-59)
        binding.npMinutesTens.minValue = 0
        binding.npMinutesTens.maxValue = 5
        binding.npMinutesOnes.minValue = 0
        binding.npMinutesOnes.maxValue = 9

        // 초 설정 (0-59)
        binding.npSecondsTens.minValue = 0
        binding.npSecondsTens.maxValue = 5
        binding.npSecondsOnes.minValue = 0
        binding.npSecondsOnes.maxValue = 9

        // 양 설정 (0-999 ml)
        binding.npAmountHundreds.minValue = 0
        binding.npAmountHundreds.maxValue = 9
        binding.npAmountTens.minValue = 0
        binding.npAmountTens.maxValue = 9
        binding.npAmountOnes.minValue = 0
        binding.npAmountOnes.maxValue = 9
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val minutes = binding.npMinutesTens.value * 10 + binding.npMinutesOnes.value
            val seconds = binding.npSecondsTens.value * 10 + binding.npSecondsOnes.value
            val amount = binding.npAmountHundreds.value * 100 + binding.npAmountTens.value * 10 + binding.npAmountOnes.value

            // 여기에 결과를 처리하는 코드를 추가합니다.
            // 예: 리스너를 통해 액티비티에 결과 전달

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TimeAmountDialogFragment"
    }
}
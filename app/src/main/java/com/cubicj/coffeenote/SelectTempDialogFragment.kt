package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.SelectTempBinding

class SelectTempDialogFragment : DialogFragment() {
    private lateinit var binding: SelectTempBinding
    private var onTempSelectedListener: ((Int) -> Unit)? = null
    private var initialTemp: Int = 90

    companion object {
        fun newInstance(currentTemp: Int): SelectTempDialogFragment {
            return SelectTempDialogFragment().apply {
                initialTemp = currentTemp
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SelectTempBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.npCoffeeTemp.minValue = 0
        binding.npCoffeeTemp.maxValue = 100
        binding.npCoffeeTemp.value = 90  // 초기값을 90으로 설정

        binding.ibTempBack.setOnClickListener { dismiss() }
        binding.btnTempCalcel.setOnClickListener { dismiss() }
        binding.btnTempConfirm.setOnClickListener {
            onTempSelectedListener?.invoke(binding.npCoffeeTemp.value)
            dismiss()
        }
    }

    fun setOnTempSelectedListener(listener: (Int) -> Unit) {
        onTempSelectedListener = listener
    }
}
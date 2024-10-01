package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.CustomCoffeeBeansBinding

class CustomCoffeeBeansFragment : DialogFragment() {

    private var _binding: CustomCoffeeBeansBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomCoffeeBeansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.btnCustomCoffeeBeansPhotoInsert.setOnClickListener {

        }

        binding.btnCustomCoffeeBeansInsert.setOnClickListener {
            dismiss()
        }

        binding.ibCustomCoffeeBeansBack.setOnClickListener {
            dismiss()
        }

        binding.btnCustomCoffeebeansCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.CustomCoffeeNoteBinding
import android.app.Activity
import android.content.Intent
import android.graphics.Color

class CustomCoffeeNoteFragment : DialogFragment() {

    private var _binding: CustomCoffeeNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomCoffeeNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그 크기 조절
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnNoteCreateColor.setOnClickListener {
            val intent = Intent(requireContext(), ColorPickerActivity::class.java)
            startActivityForResult(intent, COLOR_PICKER_REQUEST_CODE)
        }

        binding.btnNoteCreateConfirm.setOnClickListener {
            dismiss()
        }

        binding.ibNoteCreateBack.setOnClickListener {
            dismiss()
        }

        binding.btnNoteCreateCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == COLOR_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedColor = data?.getIntExtra("selectedColor", Color.BLACK) ?: Color.BLACK
            // 여기에서 선택된 색상을 사용하는 로직을 추가하세요
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val COLOR_PICKER_REQUEST_CODE = 1001
    }
}
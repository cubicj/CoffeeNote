package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.DrinkPersonBinding

class DrinkPersonDialogFragment : DialogFragment() {

    private var _binding: DrinkPersonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DrinkPersonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDrinkCancel.setOnClickListener {
            dismiss()
        }

        binding.btnDrinkInsert.setOnClickListener {
            // 선택된 사람 처리
            dismiss()
        }

        binding.ibDrinkPlus.setOnClickListener {
            showRadioInsertDialog()
        }
    }

    private fun showRadioInsertDialog() {
        val radioInsertDialog = RadioInsertDialogFragment()
        radioInsertDialog.onRadioAdded = { newRadioText ->
            addNewRadioButton(newRadioText)
        }
        radioInsertDialog.show(childFragmentManager, RadioInsertDialogFragment.TAG)
    }

    private fun addNewRadioButton(text: String) {
        val newRadioButton = RadioButton(requireContext())
        newRadioButton.text = text
        binding.rdgDrinkPerson.addView(newRadioButton)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "DrinkPersonDialogFragment"
    }
}
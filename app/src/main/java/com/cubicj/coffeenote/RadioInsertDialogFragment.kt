package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.RadioInsertBinding

class RadioInsertDialogFragment : DialogFragment() {

    private var _binding: RadioInsertBinding? = null
    private val binding get() = _binding!!

    var onRadioAdded: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RadioInsertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelRadio.setOnClickListener {
            dismiss()
        }

        binding.btnAddRadio.setOnClickListener {
            val newRadioText = binding.etRadioAdd.text.toString()
            if (newRadioText.isNotEmpty()) {
                onRadioAdded?.invoke(newRadioText)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RadioInsertDialogFragment"
    }
}
package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.SelectGrinderBinding

class GrinderPickerDialogFragment : DialogFragment() {

    private lateinit var binding: SelectGrinderBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SelectGrinderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        binding.ibSelectGrinderBack.setOnClickListener {
            dismiss()
        }

        binding.btnFellowOdd2.setOnClickListener {
            onGrinderSelected("펠로우 오드 2")
        }

        binding.btnItop03.setOnClickListener {
            onGrinderSelected("ITOP 03")
        }

        binding.btnTimemoreC3.setOnClickListener {
            onGrinderSelected("타임모어 C3 Esp Pro")
        }
    }

    private fun onGrinderSelected(grinderName: String) {
        (activity as? RecipeHandActivity)?.updateGrinderSelection(grinderName)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
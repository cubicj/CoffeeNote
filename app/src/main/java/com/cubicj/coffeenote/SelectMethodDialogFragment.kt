package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.SelectMethodBinding
import android.view.WindowManager
import android.content.Intent

class SelectMethodDialogFragment : DialogFragment() {
    private lateinit var binding: SelectMethodBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SelectMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ibMethodBack.setOnClickListener {
            dismiss()
        }

        binding.btnMethodHand.setOnClickListener {
            // 핸드드립 선택 시 RecipeHandActivity로 이동
            val intent = Intent(requireContext(), RecipeHandActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        binding.btnMethodAero.setOnClickListener {
            val intent = Intent(requireContext(), RecipeAeroActivity::class.java)
            startActivity(intent)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
package com.cubicj.coffeenote

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.CustomHandRecipeBinding

class HandDripRecipeDialogFragment : DialogFragment(){
    private var _binding: CustomHandRecipeBinding? = null
    private val binding get() = _binding!!

    private var listener: RecipeInsertListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is RecipeInsertListener) {
            listener = context
        } else {
            // 구현하지 않았다면 예외를 발생시킵니다.
            throw RuntimeException("$context must implement RecipeInsertListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomHandRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface RecipeInsertListener {
        fun onRecipeInserted(recipe: Recipe)
    }
}
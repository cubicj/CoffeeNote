package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cubicj.coffeenote.databinding.FragmentRecipeListBinding

class RecipeListFragment : Fragment() {
    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_RECIPE_TYPE = "recipe_type"

        fun newInstance(recipeType: String): RecipeListFragment {
            return RecipeListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECIPE_TYPE, recipeType)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recipeType = arguments?.getString(ARG_RECIPE_TYPE) ?: "Unknown"
        // 여기에서 recipeType에 따라 RecyclerView 설정 및 데이터 로드
        
        binding.btnSort.setOnClickListener {
            // 정렬 버튼 클릭 시 동작 구현
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.CustomHandRecipeBinding

class RecipeHandActivity : AppCompatActivity() {
    private lateinit var binding: CustomHandRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CustomHandRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
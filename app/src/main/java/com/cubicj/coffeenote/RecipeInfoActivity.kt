package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.RecipeInfoActivityBinding

class RecipeInfoActivity : AppCompatActivity() {
    private lateinit var binding: RecipeInfoActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeInfoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
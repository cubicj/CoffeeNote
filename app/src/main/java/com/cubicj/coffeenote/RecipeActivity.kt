package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.RecipeActivityBinding

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: RecipeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibRecipePlus.setOnClickListener {
            showSelectMethodDialog()
        }

        binding.btnRecipeBack.setOnClickListener {
            finish()
        }
    }

    private fun showSelectMethodDialog() {
        val selectMethodDialog = SelectMethodDialogFragment()
        selectMethodDialog.show(supportFragmentManager, "SelectMethodDialog")
    }
}
package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.RecipeActivityBinding
import com.google.android.material.tabs.TabLayoutMediator

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: RecipeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecipeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnRecipeBack.setOnClickListener {
            finish()
        }

        binding.ibRecipePlus.setOnClickListener {
            showSelectMethodDialog()
        }

        val pagerAdapter = RecipePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "핸드드립"
                1 -> "에어로프레스"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()
    }

    private fun showSelectMethodDialog() {
        val selectMethodDialog = SelectMethodDialogFragment()
        selectMethodDialog.show(supportFragmentManager, "SelectMethodDialog")
    }
}
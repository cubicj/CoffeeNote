package com.cubicj.coffeenote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCoffeeNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            startActivity(intent)
        }

        binding.ibBeansPlus.setOnClickListener {
            openCustomCoffeeBeansFragment()
        }
    }

    private fun openCustomCoffeeBeansFragment() {
        val fragment = CustomCoffeeBeansFragment()
        fragment.show(supportFragmentManager, "CustomCoffeeBeansFragment")
    }
}
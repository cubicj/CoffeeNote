package com.cubicj.coffeenote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cubicj.coffeenote.databinding.NoteActivityBinding

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: NoteActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NoteActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibNotePlus.setOnClickListener {
            openCustomCoffeeNoteFragment()
        }

        binding.ibNoteBack.setOnClickListener {
            finish()
        }
    }

    private fun openCustomCoffeeNoteFragment() {
        val fragment = CustomCoffeeNoteFragment()
        fragment.show(supportFragmentManager, "CustomCoffeeNoteFragment")
    }
}
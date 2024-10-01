package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.InfoCalenderBinding
import java.text.SimpleDateFormat
import java.util.*

class InfoCalendarDialogFragment : DialogFragment() {
    private lateinit var binding: InfoCalenderBinding
    private var onDateSelectedListener: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = InfoCalenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ibCalendarBack.setOnClickListener {
            dismiss()
        }

        binding.btnCalendarCancel.setOnClickListener {
            dismiss()
        }

        binding.cvInfoDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            binding.btnCalendarInsert.setOnClickListener {
                onDateSelectedListener?.invoke(formattedDate)
                dismiss()
            }
        }
    }

    fun setOnDateSelectedListener(listener: (String) -> Unit) {
        onDateSelectedListener = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
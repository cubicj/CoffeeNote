package com.cubicj.coffeenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cubicj.coffeenote.databinding.SelectGrinderBinding
import android.content.DialogInterface
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog

class SelectGrinderDialogFragment : DialogFragment() {

    private lateinit var binding: SelectGrinderBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SelectGrinderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        binding.ibSelectGrinderBack.setOnClickListener {
            dismiss()
        }

        binding.btnFellowOdd2.setOnClickListener {
            showGrinderDialog(R.layout.dialog_grinder_picker, "Fellow Ode 2")
        }

        binding.btnItop03.setOnClickListener {
            showGrinderDialog(R.layout.dialog_itop03_grinder, "ITOP 03")
        }

        binding.btnTimemoreC3.setOnClickListener {
            showGrinderDialog(R.layout.dialog_timemore_grinder, "Timemore C3")
        }
    }

    private fun showGrinderDialog(layoutResId: Int, grinderName: String) {
        dismiss() // 현재 다이얼로그 닫기

        val dialogView = LayoutInflater.from(requireContext()).inflate(layoutResId, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()

        // 각 다이얼로그의 뒤로 가기 버튼 설정
        when (layoutResId) {
            R.layout.dialog_grinder_picker -> {
                dialogView.findViewById<ImageButton>(R.id.ib_grinder_back).setOnClickListener { dialog.dismiss() }
                dialogView.findViewById<Button>(R.id.btn_grinder_confirm).setOnClickListener {
                    val seekBar = dialogView.findViewById<SeekBar>(R.id.sb_grinder_picker)
                    onGrinderSelected("$grinderName: ${seekBar.progress}")
                    dialog.dismiss()
                }
                dialogView.findViewById<Button>(R.id.btn_grinder_calcel).setOnClickListener { dialog.dismiss() }
            }
            R.layout.dialog_itop03_grinder -> {
                dialogView.findViewById<ImageButton>(R.id.ib_itop_back).setOnClickListener { dialog.dismiss() }
                dialogView.findViewById<Button>(R.id.btn_itop_confirm).setOnClickListener {
                    val tensDigit = dialogView.findViewById<NumberPicker>(R.id.np_itop_tens_digit).value
                    val onesDigit = dialogView.findViewById<NumberPicker>(R.id.np_itop_ones_digit).value
                    val tenthsDigit = dialogView.findViewById<NumberPicker>(R.id.np_itop_tenths_digit).value
                    onGrinderSelected("$grinderName: $tensDigit$onesDigit.$tenthsDigit")
                    dialog.dismiss()
                }
                dialogView.findViewById<Button>(R.id.btn_itop_cancel).setOnClickListener { dialog.dismiss() }
            }
            R.layout.dialog_timemore_grinder -> {
                dialogView.findViewById<ImageButton>(R.id.imageButton).setOnClickListener { dialog.dismiss() }
                // Timemore 그라인더 다이얼로그의 확인 버튼 설정
                // 참고: dialog_timemore_grinder.xml 파일이 제공되지 않아 정확한 ID를 알 수 없습니다.
                // 적절한 ID로 변경해 주세요.
                dialogView.findViewById<Button>(R.id.btn_timemore_confirm).setOnClickListener {
                    // Timemore 그라인더 값 가져오기
                    // 예: val timemoreValue = dialogView.findViewById<TextView>(R.id.tv_timemore_value).text.toString()
                    onGrinderSelected("$grinderName: [선택된 값]")
                    dialog.dismiss()
                }
                dialogView.findViewById<Button>(R.id.btn_timemore_cancel).setOnClickListener { dialog.dismiss() }
            }
        }
    }

    private fun onGrinderSelected(grinderInfo: String) {
        (activity as? RecipeHandActivity)?.updateGrinderSelection(grinderInfo)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
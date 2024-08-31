package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cubicj.coffeenote.databinding.CoffeeBeansNoteBinding
import kotlinx.coroutines.launch

class NoteActivity: AppCompatActivity() {

    private var mBinding: CoffeeBeansNoteBinding? = null
    private lateinit var noteAdapter: NoteAdapter
    private var coffeebeansDb: BeansDb? = null
    private var selectedBeanId: Long = -1L
    private var noteAddColor: Button? = null
    private var selectedColor = Color.WHITE

    // CoffeeNoteViewModel 추가
    private val viewModel: CoffeeNoteViewModel by viewModels {
        CoffeeNoteViewModelFactory(
            coffeebeansDb?.coffeeBeansNoteDao()!!,
            coffeebeansDb?.coffeeBeansDao()!!
        )
    }


    private val binding get() = mBinding!!

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // 결과 처리
            val updatedNoteNames = data?.getStringArrayListExtra("updatedNoteNames")
            viewModel.handleUpdatedNoteNames(updatedNoteNames)

            selectedColor = data?.getIntExtra("selectedColor", 0) ?: 0 // data에서 selectedColor 가져오기
            noteAddColor?.setBackgroundColor(selectedColor)
        }
    }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = CoffeeBeansNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coffeebeansDb = BeansDb.getInstance(this)
        selectedBeanId = intent.getLongExtra("selectedBeanId", -1L)

        viewModel.setSelectedBeanId(selectedBeanId)
        var selectedNoteNames = intent.getStringArrayListExtra("selectedNoteNames") ?: emptyList<String>()
        viewModel.setSelectedNoteNames(selectedNoteNames)

        // NoteAdapter 생성 시 connectedNoteIds 전달
        noteAdapter = NoteAdapter(this@NoteActivity, viewModel)

        binding.rvCoffeeNote.adapter = noteAdapter
        binding.rvCoffeeNote.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvCoffeeNote.setHasFixedSize(true)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredNotes.collect { notes ->
                    noteAdapter.setNoteList(notes)
                }
            }
        }

        binding.rvCoffeeNote.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val view = currentFocus
                if (view is EditText) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        view.clearFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }
                }
            }
            false
        }

        binding.etNoteSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                viewModel.filterNotesBySearchText(searchText)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.ibSearchDelete.setOnClickListener {
            // 1. etNoteSearch의 포커스 해제
            binding.etNoteSearch.clearFocus()

            // 2. 키보드 숨기기 (필요한 경우)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etNoteSearch.windowToken, 0)

            // 3. 검색창 텍스트 초기화 및 필터링
            binding.etNoteSearch.text.clear()
            viewModel.filterNotesBySearchText("")
        }

        binding.ibNotePlus.setOnClickListener {

            val noteAddAlertDialog: AlertDialog?

            val noteAddSelectView = LayoutInflater.from(this).inflate(R.layout.custom_coffee_note, null)
            val noteAddSelectBuilder = AlertDialog.Builder(this)
                .setView(noteAddSelectView)

            noteAddAlertDialog = noteAddSelectBuilder.create()

            noteAddAlertDialog.show()

            val noteAddBack = noteAddSelectView.findViewById<ImageButton>(R.id.ib_note_create_back)
            val noteAddName = noteAddSelectView.findViewById<EditText>(R.id.et_note_create_name)
            noteAddColor = noteAddSelectView.findViewById(R.id.btn_note_create_color)
            val noteAddConfirm = noteAddSelectView.findViewById<Button>(R.id.btn_note_create_confirm)
            val noteAddCancel = noteAddSelectView.findViewById<Button>(R.id.btn_note_create_cancel)

            noteAddColor?.setOnClickListener {
                val intent = Intent(this@NoteActivity, Color_Picker::class.java)
                startForResult.launch(intent)
            }

            noteAddConfirm.setOnClickListener {
                val noteName = noteAddName.text.toString()
                val colorCode = selectedColor

                if (noteName.isNotBlank()) {
                    val newNote = CoffeeBeansNote(
                        notename = noteName,
                        colorcode = colorCode
                    )
                    viewModel.insertCoffeeBeansNote(newNote)
                    noteAddAlertDialog.dismiss()
                    Toast.makeText(this, "추가 되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 노트 이름이 비어있는 경우 오류 메시지 표시 등 처리
                    Toast.makeText(this, "노트 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            noteAddBack.setOnClickListener {
                noteAddAlertDialog.dismiss()
            }
            noteAddCancel.setOnClickListener {
                noteAddAlertDialog.dismiss()
            }
        }
        binding.btnNoteConfirm.setOnClickListener {
            selectedNoteNames = viewModel.selectedNoteNames.value
            // noteAdapter에서 업데이트된 checkedNotes 사용
            val checkedNotes = noteAdapter.getCheckedNotes()
            Log.d("CoffeeNote", "checkedNotes: $checkedNotes")
            val bundle = Bundle().apply {
                putParcelableArrayList("checkedNotes", ArrayList(checkedNotes))
            }
            val resultIntent = Intent().apply {
                putExtras(bundle)
                putExtra("selectedBeanId", selectedBeanId)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.btnNoteCancel.setOnClickListener {
            finish()
        }
        binding.ibNoteBack.setOnClickListener {
            finish()
        }
    }
}
package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cubicj.coffeenote.databinding.ListCoffeeNoteBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteAdapter(
    val context: NoteActivity,
    private val viewModel: CoffeeNoteViewModel
) : RecyclerView.Adapter<NoteAdapter.Holder>() {

    private var checkedNotes: List<CoffeeBeansNote> = emptyList()

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<CoffeeBeansNote>() {
        override fun areItemsTheSame(oldItem: CoffeeBeansNote, newItem: CoffeeBeansNote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoffeeBeansNote, newItem: CoffeeBeansNote): Boolean {
            return oldItem == newItem
        }
    })
    init {
        // ViewModel의 checkedNotes를 사용하여 초기화
        context.lifecycleScope.launch {
            viewModel.checkedNotes.collectLatest {
                checkedNotes = it
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context ?: context)
        val binding = ListCoffeeNoteBinding.inflate(inflater, parent, false)


        return Holder(binding).apply {

            binding.root.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val view = context.currentFocus // Get the focused view from the activity
                    if (view is EditText) {
                        val outRect = Rect()
                        view.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            view.clearFocus()
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }
                }
                false
            }



            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    val note = differ.currentList[position]
                    val newIsChecked = !note.isChecked
                    viewModel.updateSelectedNoteNames(note.notename, newIsChecked)
                }
            }

            binding.root.setOnLongClickListener{
                val position = adapterPosition // 현재 아이템의 position 가져오기
                if (position != RecyclerView.NO_POSITION) { // 유효한 position인지 확인
                    val noteToDelete = differ.currentList[position]

                    val noteDeleteAlertDialog: AlertDialog?

                    val noteDeleteSelectView =
                        LayoutInflater.from(parent.context).inflate(R.layout.delete_alert, null)
                    val noteDeleteSelectBuilder = AlertDialog.Builder(parent.context)
                        .setView(noteDeleteSelectView)

                    noteDeleteAlertDialog = noteDeleteSelectBuilder.create()

                    noteDeleteAlertDialog.show()

                    val notedeleteconfirm =
                        noteDeleteSelectView.findViewById<Button>(R.id.btn_delete_confirm)
                    val notedeletecancel =
                        noteDeleteSelectView.findViewById<Button>(R.id.btn_delete_cancel)

                    notedeleteconfirm.setOnClickListener {
                        viewModel.deleteCoffeeBeansNote(noteToDelete) // 노트 삭제
                        noteDeleteAlertDialog.dismiss()
                    }
                    notedeletecancel.setOnClickListener {
                        noteDeleteAlertDialog.dismiss()
                    }
                }
                true
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val note = differ.currentList[position]

        holder.binding.root.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val clickedNote = differ.currentList[currentPosition]
                val newIsChecked = !clickedNote.isChecked
                viewModel.updateSelectedNoteNames(clickedNote.notename, newIsChecked)
            }
        }

        holder.binding.cbNoteList.isClickable = false

        // 뷰 홀더에 데이터 바인딩
        holder.bind(note)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size // differ에서 아이템 개수 가져오기

    }

    fun getCheckedNotes(): List<CoffeeBeansNote> {
        return checkedNotes // 로컬 checkedNotes 반환
    }

    fun setNoteList(newList: List<CoffeeBeansNote>) {
        differ.submitList(newList)
    }

    inner class Holder(val binding: ListCoffeeNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: CoffeeBeansNote) {
            binding.tvNoteListName.text = note.notename
            binding.tvNoteListName.setTextColor(note.colorcode)

            binding.cbNoteList.isChecked = note.isChecked

            context.lifecycleScope.launch {
                viewModel.noteListState.flowWithLifecycle(context.lifecycle, Lifecycle.State.STARTED)
                    .collectLatest { notes ->
                        val updatedNote = notes.find { it.id == note.id }
                        updatedNote?.let {
                            binding.cbNoteList.isChecked = it.isChecked
                        }
                    }
            }

            binding.cbNoteList.setOnCheckedChangeListener { _, isChecked ->
                // _allNotes 직접 업데이트
                val updatedNotes = differ.currentList.toMutableList().also {
                    if (adapterPosition != RecyclerView.NO_POSITION) { // adapterPosition 사용
                        it[adapterPosition] = it[adapterPosition].copy(isChecked = isChecked)
                    }
                }
                viewModel._allNotes.value = updatedNotes
                viewModel.updateSelectedNoteNames(note.notename, isChecked)
            }
        }
    }
}
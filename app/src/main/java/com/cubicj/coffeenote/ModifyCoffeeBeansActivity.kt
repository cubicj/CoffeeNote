package com.cubicj.coffeenote

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cubicj.coffeenote.databinding.ModifyCoffeeBeansBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ModifyCoffeeBeansActivity : AppCompatActivity() {

    private var mBinding: ModifyCoffeeBeansBinding? = null
    private var coffeebeansDb: BeansDb? = null
    private var checkedNotes: List<CoffeeBeansNote> = emptyList()
    private var selectedBeanId: Long = -1L
    private var coffeebeanscontent: ImageView? = null
    private var selectedImageBytes: ByteArray? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data

            if (selectedImageUri != null){
                try {
                    coffeebeanscontent?.setImageURI(selectedImageUri)

                    val imageStream = contentResolver.openInputStream(selectedImageUri)
                    selectedImageBytes = imageStream?.readBytes()
                } catch (e: Exception) {
                    Log.e("ModifyCoffeeBeansActivity", "이미지 처리 오류: ${e.message}")
                    Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "이미지 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private val binding get() = mBinding!!

    private val viewModel: ModifyCoffeeBeansViewModel by viewModels {
        ModifyCoffeeBeansViewModelFactory(
            coffeebeansDb?.coffeeBeansDao()!!,
            coffeebeansDb?.coffeeBeansNoteDao()!!
        )
    }
    companion object {
        private const val REQUEST_READ_MEDIA_IMAGES = 1
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            Log.d("ModifyCoffeeBeansActivity", "data?.extras: ${data?.extras}")

            val receivedNotes: ArrayList<CoffeeBeansNote>? =
                data?.getParcelableArrayListExtra("checkedNotes", CoffeeBeansNote::class.java)

            val selectedBeanIdFromNoteActivity = data?.getLongExtra("selectedBeanId", -1L)

            if (selectedBeanIdFromNoteActivity != selectedBeanId) {
                // 오류 처리: 예상치 못한 selectedBeanId 값이 전달된 경우
                Log.e("ModifyCoffeeBeansActivity", "Unexpected selectedBeanId from CoffeeNote: $selectedBeanIdFromNoteActivity")
                return@registerForActivityResult
            }
            Log.d("ModifyCoffeeBeansActivity", "receivedNotes: $receivedNotes")
            checkedNotes = receivedNotes ?: emptyList()
            displayCheckedNotes(checkedNotes)

            // ViewModel의 coffeeBean 상태 갱신
            viewModel.coffeeBean.value?.let { coffeeBean ->
                val updatedBean = coffeeBean.copy(
                    noteNameColorMap = checkedNotes.associate { it.notename to it.colorcode }
                )
                viewModel._coffeeBean.value = updatedBean
            }
            Log.d("ModifyCoffeeBeansActivity", "receivedNotes: $receivedNotes")
            Log.d("ModifyCoffeeBeansActivity", "selectedBeanIdFromNoteActivity: $selectedBeanIdFromNoteActivity")

            binding.tvCoffeeNoteList.invalidate()
            binding.tvCoffeeNoteList.requestLayout()
        }
    }

    private fun displayCheckedNotes(checkedNotes: List<CoffeeBeansNote>) {
        val spannableString = SpannableStringBuilder()
        for (note in checkedNotes) {
            val start = spannableString.length
            spannableString.append(note.notename).append(", ")
            val end = spannableString.length
            spannableString.setSpan(ForegroundColorSpan(note.colorcode), start, end - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvCoffeeNoteList.text = spannableString
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLauncher.launch(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ModifyCoffeeBeansBinding.inflate(layoutInflater)
        val scrollView = ScrollView(this)
        scrollView.addView(binding.root)

        setContentView(scrollView)

        coffeebeansDb = BeansDb.getInstance(applicationContext)
        coffeebeanscontent = binding.ivModifyCoffeebeansPhotoview

        selectedBeanId = intent.getLongExtra("selectedBeanId", -1L)

        if (selectedBeanId != -1L) {
                viewModel.loadCoffeeBean(selectedBeanId)
        }

        lifecycleScope.launch {
            viewModel.coffeeBean.collect { coffeeBean ->
                coffeeBean?.let {
                    binding.ptCustomCoffeeBeans.setText(it.name)

                    val spannableString = SpannableStringBuilder()
                    for ((noteName, colorCode) in it.noteNameColorMap) {
                        val start = spannableString.length
                        spannableString.append(noteName).append(" ")
                        val end = spannableString.length
                        spannableString.setSpan(ForegroundColorSpan(colorCode), start, end - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    // tvCoffeeNoteList에 설정
                    binding.tvCoffeeNoteList.text = spannableString

                    it.content?.let { imageBytes ->
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        coffeebeanscontent!!.setImageBitmap(bitmap)
                    }
                }
            }
        }

        binding.btnModifyCoffeebeansPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Toast.makeText(this, "이미지를 선택하려면 갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_READ_MEDIA_IMAGES
                )
            } else {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLauncher.launch(intent)
            }
        }

        binding.btnCustomCoffeeBeansInsert.setOnClickListener {
            val updatedName = binding.ptCustomCoffeeBeans.text.toString()

            if (updatedName.isBlank()) {
                Toast.makeText(this, "원두 이름과 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.coffeeBean.value?.let { coffeeBean ->
                lifecycleScope.launch {
                    // 데이터베이스에서 선택된 노트들을 가져옴
                    val selectedNotesFromDb = withContext(Dispatchers.IO) {
                        coffeebeansDb?.coffeeBeansNoteDao()?.getCoffeeBeansNotesByIds(checkedNotes.map { it.id })?.first()
                    }

                    val updatedBean = coffeeBean.copy(
                        name = updatedName,
                        content = selectedImageBytes,
                        noteNameColorMap = selectedNotesFromDb!!.associate { it.notename to it.colorcode }
                    )
                    viewModel.updateCoffeeBean(updatedBean)

                    // CoffeeNote 액티비티로 업데이트된 노트 정보 전달 (선택 사항, 필요한 경우에만 사용)
                    val intent = Intent().apply {
                        putStringArrayListExtra(
                            "updatedNoteNames",
                            ArrayList(checkedNotes.map { it.notename })
                        )
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

        binding.btnCustomBeansNote.setOnClickListener {
            val intent = Intent(this, CoffeeNote::class.java)
            intent.putExtra("selectedBeanId", selectedBeanId)
            val selectedNoteNames = checkedNotes.map { it.notename }
            intent.putStringArrayListExtra("selectedNoteNames", ArrayList(selectedNoteNames))

            startForResult.launch(intent)
        }

        binding.ibCustomCoffeeBeansBack.setOnClickListener {
            finish()
        }
        binding.btnCustomCoffeebeansCancel.setOnClickListener {
            finish()
        }
    }
}

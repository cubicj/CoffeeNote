package com.cubicj.coffeenote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cubicj.coffeenote.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    // 전역 변수로 바인딩 객체 선언
    private var mBinding: ActivityMainBinding? = null
    private var mAlertDialog: AlertDialog? = null
    private var coffeebeansDb: BeansDb? = null
    lateinit var mAdapter: MainAdapter
    private var coffeebeanscontent: ImageView? = null
    private var selectedImageBytes: ByteArray? = null
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            coffeebeansDb?.coffeeBeansDao()!!
        )
    }
    private val binding get() = mBinding!!

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data

            if (selectedImageUri != null){
                coffeebeanscontent?.setImageURI(selectedImageUri)

                val imageStream = contentResolver.openInputStream(selectedImageUri)
                selectedImageBytes = imageStream?.readBytes()
            }
        }
    }

    //원두 추가 함수
    @SuppressLint("ResourceAsColor", "SetTextI18n", "NotifyDataSetChanged", "MissingInflatedId")
    fun showCoffeeBeansDialog() {
        if (mAlertDialog == null) {
            val mBeansSelectView =
                LayoutInflater.from(this).inflate(R.layout.custom_coffee_beans, null)
            val scrollView = ScrollView(this)
            scrollView.addView(mBeansSelectView)
            val mBeansSelectBuilder = AlertDialog.Builder(this)
                .setView(scrollView)
            mAlertDialog = mBeansSelectBuilder.create()

            val coffeebeansname = mBeansSelectView.findViewById<EditText>(R.id.pt_custom_coffeeBeans)
            val insertcoffeebeans = mBeansSelectView.findViewById<Button>(R.id.btn_custom_coffeeBeans_insert)
            val cancelcoffeebeans = mBeansSelectView.findViewById<Button>(R.id.btn_custom_coffeebeans_cancel)
            coffeebeanscontent = mBeansSelectView.findViewById(R.id.iv_beans_picture)
            val coffeebeansphotoinsert = mBeansSelectView.findViewById<Button>(R.id.btn_custom_coffeeBeans_photo_insert)
            val coffeebeansback = mBeansSelectView.findViewById<ImageButton>(R.id.ib_custom_coffee_beans_back)

            coffeebeansphotoinsert.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher.launch(intent)
                }
            }

            insertcoffeebeans.setOnClickListener {
                val newCoffeeBeansName = coffeebeansname.text.toString()

                if (newCoffeeBeansName.isBlank()) {
                    Toast.makeText(this, "원두 이름과 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // 빈칸일 경우 함수 종료
                }

                // 새로운 CoffeeBeans 객체 생성
                val newCoffeeBeans = CoffeeBeans(
                    name = newCoffeeBeansName,
                    content = selectedImageBytes
                )

                // 뷰 모델을 통해 데이터베이스에 추가
                lifecycleScope.launch {
                    viewModel.insertCoffeeBeans(newCoffeeBeans)
                }

                // 다이얼로그 닫기 및 토스트 메시지 표시 (UI 업데이트는 뷰 모델의 LiveData 관찰을 통해 처리됨)
                mAlertDialog?.dismiss()
                Toast.makeText(this@MainActivity, "원두가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }

            cancelcoffeebeans.setOnClickListener {
                mAlertDialog!!.dismiss()
            }
            coffeebeansback.setOnClickListener {
                mAlertDialog!!.dismiss()
            }
        }
        mAlertDialog?.show()
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

    // 메인 액티비티 시작할 때 수행
    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        coffeebeansDb = BeansDb.getInstance(this)


        mAdapter = MainAdapter(this@MainActivity, viewModel, this@MainActivity)

        binding.rvBeans.adapter = mAdapter
        binding.rvBeans.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvBeans.setHasFixedSize(true)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.coffeeBeans.collect { beansList ->
                    mAdapter.updateBeansList(beansList)
                }
            }
        }
        // 커피 원두 추가 팝업
        binding.ibBeansPlus.setOnClickListener{
            showCoffeeBeansDialog()
        }
    }
}
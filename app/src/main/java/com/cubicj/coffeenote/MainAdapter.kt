package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cubicj.coffeenote.databinding.ListCoffeeBeansBinding


class MainAdapter(

    val context: Context,
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner

) : RecyclerView.Adapter<MainAdapter.Holder>() {


    private val differ =
        AsyncListDiffer(this, object : DiffUtil.ItemCallback<CoffeeBeans>() { // CoffeeBeans 사용
            override fun areItemsTheSame(oldItem: CoffeeBeans, newItem: CoffeeBeans): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CoffeeBeans, newItem: CoffeeBeans): Boolean {
                return oldItem == newItem
            }
        })

    private var coffeebeansinfocontent: ImageView? = null

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context ?: context)
        val binding = ListCoffeeBeansBinding.inflate(inflater, parent, false)

        return Holder(binding).apply {
            // 일반 클릭
            binding.root.setOnClickListener {
                val curPos: Int = adapterPosition
                if (curPos != RecyclerView.NO_POSITION && differ.currentList.isNotEmpty()) {
                    val beans: CoffeeBeans = differ.currentList[curPos]
                    val intent = Intent(context, RecipeActivity::class.java)
                    intent.putExtra("selectedBeanId", beans.id)
                    context.startActivity(intent)
                }
            }
            // 롱 클릭
            binding.root.setOnLongClickListener {
                val curPos: Int = adapterPosition
                if (curPos != RecyclerView.NO_POSITION && differ.currentList.isNotEmpty()) {
                    val beans: CoffeeBeans = differ.currentList[curPos]
                    val beansInfoSelectView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.beans_info_select, null)
                    val beansInfoSelectBuilder = AlertDialog.Builder(parent.context)
                        .setView(beansInfoSelectView)

                    val beansinfoAlertDialog = beansInfoSelectBuilder.create()

                    beansinfoAlertDialog.show()

                    val beansinfo = beansinfoAlertDialog.findViewById<Button>(R.id.btn_beans_info)
                    val beansmodify =
                        beansinfoAlertDialog.findViewById<Button>(R.id.btn_beans_modify)
                    val beansdelete =
                        beansinfoAlertDialog.findViewById<Button>(R.id.btn_beans_delete)
                    val beansselectinfoback =
                        beansinfoAlertDialog.findViewById<ImageButton>(R.id.ib_beans_select_info_back)

                    beansinfo?.setOnClickListener {
                        val infoSelectView =
                            LayoutInflater.from(parent.context).inflate(R.layout.beans_info, null)
                        val scrollView = ScrollView(parent.context)
                        scrollView.addView(infoSelectView)

                        val infoSelectBuilder = AlertDialog.Builder(parent.context)
                            .setView(scrollView)

                        val infoAlertDialog: AlertDialog

                        infoAlertDialog = infoSelectBuilder.create()

                        infoAlertDialog.show()

                        val coffeebeansinfoname =
                            infoSelectView.findViewById<TextView>(R.id.tv_beans_info_name)
                        coffeebeansinfocontent = infoSelectView.findViewById(R.id.iv_coffee_beans_photo)
                        val coffeebeansinfoback =
                            infoSelectView.findViewById<ImageButton>(R.id.ib_recipe_info_back)

                        coffeebeansinfoback.setOnClickListener {
                            infoAlertDialog.dismiss()
                        }

                        coffeebeansinfoname.text = beans.name
                        beans.content?.let { imageBytes ->
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            coffeebeansinfocontent?.setImageBitmap(bitmap)
                        }
                    }

                    beansmodify?.setOnClickListener {
                        val intent = Intent(context, ModifyCoffeeBeansActivity::class.java)
                        intent.putExtra("selectedBeanId", beans.id)
                        context.startActivity(intent)
                        beansinfoAlertDialog.dismiss()
                    }

                    beansdelete?.setOnClickListener {
                        val deleteSelectView =
                            LayoutInflater.from(parent.context).inflate(R.layout.delete_alert, null)
                        val deleteSelectBuilder = AlertDialog.Builder(parent.context)
                            .setView(deleteSelectView)

                        val deleteAlertDialog: AlertDialog

                        deleteAlertDialog = deleteSelectBuilder.create()

                        deleteAlertDialog.show()

                        val deleteconfirm =
                            deleteSelectView.findViewById<Button>(R.id.btn_delete_confirm)
                        val deletecancel =
                            deleteSelectView.findViewById<Button>(R.id.btn_delete_cancel)

                        deleteconfirm.setOnClickListener {
                            viewModel.deleteCoffeeBeans(beans) // 기존에 정의된 beans 변수 사용
                            deleteAlertDialog.dismiss()
                            beansinfoAlertDialog.dismiss()
                        }

                        deletecancel.setOnClickListener {
                            deleteAlertDialog.dismiss()
                        }
                    }

                    beansselectinfoback?.setOnClickListener {
                        beansinfoAlertDialog.dismiss()
                    }
                }
                true
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val coffeeBeans = differ.currentList[position]
        holder.bind(coffeeBeans)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBeansList(newBeansList: List<CoffeeBeans>) {
        differ.submitList(newBeansList) // 효율적인 데이터 변경 처리 및 UI 업데이트
    }

    // getItemCount() 함수 수정
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class Holder(private val binding: ListCoffeeBeansBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(coffeeBeans: CoffeeBeans) = with(binding) {
            tvCoffeeBeansName.text = coffeeBeans.name

            val spannableString = SpannableStringBuilder()
            for ((name, color) in coffeeBeans.noteNameColorMap) {
                val start = spannableString.length
                spannableString.append(name).append(" ")
                val end = spannableString.length
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end - 1, // 마지막 공백 제거
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            tvCoffeebeansNote.text = spannableString
        }
    }
}

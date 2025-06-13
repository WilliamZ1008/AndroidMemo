package com.example.androidmemo

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidmemo.data.AppDatabase
import com.example.androidmemo.data.Memo
import com.example.androidmemo.databinding.ActivityMemoEditBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MemoEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoEditBinding
    private lateinit var database: AppDatabase
    private var selectedDate: Date = Date()
    private var selectedImageUri: Uri? = null
    private var memoId: Long = -1

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivSelectedImage.apply {
                    setImageURI(uri)
                    visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        memoId = intent.getLongExtra("memoId", -1)

        setupDatePicker()
        setupImagePicker()
        setupSaveButton()

        if (memoId != -1L) {
            loadMemo()
        } else {
            updateDateDisplay()
        }
    }

    private fun setupDatePicker() {
        binding.btnDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = selectedDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupImagePicker() {
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (title.isBlank()) {
                Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val memo = Memo(
                    id = if (memoId == -1L) 0 else memoId,
                    title = title,
                    content = content,
                    date = selectedDate,
                    imagePath = selectedImageUri?.toString()
                )

                if (memoId == -1L) {
                    database.memoDao().insert(memo)
                } else {
                    database.memoDao().update(memo)
                }

                Toast.makeText(this@MemoEditActivity, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadMemo() {
        lifecycleScope.launch {
            val memo = database.memoDao().getMemoById(memoId)
            memo?.let {
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
                selectedDate = it.date
                updateDateDisplay()

                it.imagePath?.let { path ->
                    selectedImageUri = Uri.parse(path)
                    binding.ivSelectedImage.apply {
                        setImageURI(selectedImageUri)
                        visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateDateDisplay() {
        binding.tvDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(selectedDate)
    }
} 
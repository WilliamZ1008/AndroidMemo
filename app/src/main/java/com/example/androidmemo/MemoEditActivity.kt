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
    private val db by lazy { AppDatabase.getInstance(this) }
    private var selectedImageUri: Uri? = null
    private var memoId: Long? = null
    private var selectedDate: Date = Date()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivImage.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memoId = intent.getLongExtra("memo_id", -1).takeIf { it != -1L }

        setupDatePicker()
        setupImagePicker()
        setupSaveButton()
        loadMemoIfExists()
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
        updateDateDisplay()
    }

    private fun setupImagePicker() {
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupSaveButton() {
        binding.fabSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (title.isBlank()) {
                Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val memo = Memo(
                id = memoId ?: 0,
                title = title,
                content = content,
                date = selectedDate,
                imagePath = selectedImageUri?.toString()
            )

            lifecycleScope.launch {
                if (memoId == null) {
                    db.memoDao().insert(memo)
                } else {
                    db.memoDao().update(memo)
                }
                finish()
            }
        }
    }

    private fun loadMemoIfExists() {
        memoId?.let { id ->
            lifecycleScope.launch {
                val memo = db.memoDao().getById(id)
                memo?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                    selectedDate = it.date
                    updateDateDisplay()

                    it.imagePath?.let { path ->
                        selectedImageUri = Uri.parse(path)
                        binding.ivImage.setImageURI(selectedImageUri)
                    }
                }
            }
        }
    }

    private fun updateDateDisplay() {
        binding.btnDate.text = dateFormat.format(selectedDate)
    }
} 
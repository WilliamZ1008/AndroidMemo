package com.example.androidmemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidmemo.adapter.MemoAdapter
import com.example.androidmemo.data.AppDatabase
import com.example.androidmemo.data.Memo
import com.example.androidmemo.databinding.ActivityMemoListBinding
import kotlinx.coroutines.launch

class MemoListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoListBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private val adapter = MemoAdapter { memo ->
        val intent = Intent(this, MemoEditActivity::class.java).apply {
            putExtra("memo_id", memo.id)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        setupAddButton()
        observeMemos()
    }

    override fun onResume() {
        super.onResume()
        observeMemos()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MemoListActivity)
            adapter = this@MemoListActivity.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchMemos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchMemos(newText)
                return true
            }
        })
    }

    private fun setupAddButton() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, MemoEditActivity::class.java))
        }
    }

    private fun observeMemos() {
        db.memoDao().getAll().observe(this) { memos ->
            adapter.submitList(memos)
        }
    }

    private fun searchMemos(query: String?) {
        if (query.isNullOrBlank()) {
            observeMemos()
        } else {
            db.memoDao().search("%$query%").observe(this) { memos ->
                adapter.submitList(memos)
            }
        }
    }

    private fun showDeleteDialog(memo: Memo) {
        AlertDialog.Builder(this)
            .setTitle("删除备忘录")
            .setMessage("确定要删除这条备忘录吗？")
            .setPositiveButton("确定") { _, _ ->
                lifecycleScope.launch {
                    db.memoDao().delete(memo)
                    Toast.makeText(this@MemoListActivity, "删除成功", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
} 
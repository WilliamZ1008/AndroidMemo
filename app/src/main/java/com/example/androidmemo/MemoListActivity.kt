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
    private lateinit var adapter: MemoAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupSearchView()
        setupFab()

        // 观察备忘录列表变化
        database.memoDao().getAllMemos().observe(this) { memos ->
            adapter.submitList(memos)
        }
    }

    private fun setupRecyclerView() {
        adapter = MemoAdapter(
            onItemClick = { memo ->
                startActivity(
                    Intent(this, MemoEditActivity::class.java).apply {
                        putExtra("memoId", memo.id)
                    }
                )
            },
            onItemLongClick = { memo ->
                showDeleteDialog(memo)
            }
        )

        binding.rvMemos.apply {
            layoutManager = LinearLayoutManager(this@MemoListActivity)
            adapter = this@MemoListActivity.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    database.memoDao().getAllMemos().observe(this@MemoListActivity) { memos ->
                        adapter.submitList(memos)
                    }
                } else {
                    database.memoDao().searchMemos(newText).observe(this@MemoListActivity) { memos ->
                        adapter.submitList(memos)
                    }
                }
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddMemo.setOnClickListener {
            startActivity(Intent(this, MemoEditActivity::class.java))
        }
    }

    private fun showDeleteDialog(memo: Memo) {
        AlertDialog.Builder(this)
            .setTitle("删除备忘录")
            .setMessage("确定要删除这条备忘录吗？")
            .setPositiveButton("确定") { _, _ ->
                lifecycleScope.launch {
                    database.memoDao().delete(memo)
                    Toast.makeText(this@MemoListActivity, "删除成功", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
} 
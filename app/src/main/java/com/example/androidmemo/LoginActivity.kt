package com.example.androidmemo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidmemo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // 检查是否已登录
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MemoListActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 这里简单演示，实际应用中应该进行真实的用户验证
            if (username == "admin" && password == "123456") {
                if (binding.cbRememberMe.isChecked) {
                    sharedPreferences.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("username", username)
                        apply()
                    }
                }
                startActivity(Intent(this, MemoListActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 
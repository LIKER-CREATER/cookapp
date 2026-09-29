package com.example.cookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.User;
import com.example.cookapp.database.UserDao;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = AppDatabase.getInstance(this).userDao();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = userDao.login(username, password);
                if (user != null) {
                    // 先登出所有用户
                    userDao.logoutAll();
                    // 设置当前用户为已登录状态
                    user.setLoggedIn(true);
                    userDao.update(user);
                    
                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", user.getId());
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
} 
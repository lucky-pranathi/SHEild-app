package com.lucky.sheild;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEt, passEt;
    private Button loginBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.email);
        passEt = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = emailEt.getText().toString().trim();
        String pwd = passEt.getText().toString();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user == null) return;

                // Check if the email matches the admin email
                if (email.equalsIgnoreCase("allurpranathi@gmail.com")) {
                    Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                } else {
                    Toast.makeText(this, "User Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DisplayActivity.class));
                }
                finish();

            } else {
                Toast.makeText(this,
                        "Login failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

package com.lucky.sheild;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    private EditText emailEt, passEt;
    private Button btnCreate, btnLogout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        emailEt = findViewById(R.id.adminEmail);
        passEt = findViewById(R.id.adminPassword);
        btnCreate = findViewById(R.id.btnCreateUser);
        btnLogout = findViewById(R.id.btnLogout);  // <-- New logout button

        auth = FirebaseAuth.getInstance();

        btnCreate.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String pwd = passEt.getText().toString();

            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            createUser(email, pwd);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevent going back
            startActivity(intent);
            finish(); // Close admin page and return to previous (like login)
        });
    }

    private void createUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User created successfully", Toast.LENGTH_LONG).show();
                        emailEt.setText(""); // Clear input fields
                        passEt.setText("");
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

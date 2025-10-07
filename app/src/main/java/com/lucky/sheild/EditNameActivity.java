package com.lucky.sheild;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditNameActivity extends AppCompatActivity {
    private TextView tvPhone;
    private EditText etNewName, etPassword;
    private Button btnConfirm;
    private String phone;
    private FirebaseAuth auth;
    private DatabaseReference contactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        tvPhone = findViewById(R.id.tvPhone);
        etNewName = findViewById(R.id.etNewName);
        etPassword = findViewById(R.id.etPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        phone = getIntent().getStringExtra("phone");
        tvPhone.setText(phone);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference("contacts").child(uid).child(phone);

        btnConfirm.setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            if (password.isEmpty()) { Toast.makeText(this,"Enter password",Toast.LENGTH_SHORT).show(); return; }

            // Re-authenticate
            String email = auth.getCurrentUser().getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // allow edit
                    String newName = etNewName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        contactRef.child("name").setValue(newName).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Toast.makeText(this,"Name updated",Toast.LENGTH_SHORT).show();
                                finish();
                            } else Toast.makeText(this,"Update failed",Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Toast.makeText(this,"Re-auth failed: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}

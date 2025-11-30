package com.lucky.sheild;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class DisplayActivity extends AppCompatActivity {
    ListView contactList;
    ArrayList<String> listData;
    ArrayAdapter<String> adapter;
    Button btnOpenMain,logoutBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        btnOpenMain=findViewById(R.id.btnOpenMain);
        logoutBtn=findViewById(R.id.logoutBtn);

        contactList = findViewById(R.id.listView);

        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        contactList.setAdapter(adapter);
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnOpenMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisplayActivity.this, MainActivity.class));
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Toast.makeText(DisplayActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DisplayActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevent going back
                startActivity(intent);
                finish();
            }
        });
        // Load contacts using UID
        loadContacts(user.getUid());
    }

    private void loadContacts(String uid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // 🔥 Correct path (matches your rules)
        DatabaseReference ref = db.getReference("users")
                .child(uid)
                .child("contacts");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listData.clear();

                for (DataSnapshot c : snapshot.getChildren()) {
                    String name = c.child("name").getValue(String.class);
                    String phone = c.child("phone").getValue(String.class);

                    listData.add("Name: " + name + "\nPhone: " + phone);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DisplayActivity.this,
                        "Error loading contacts: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

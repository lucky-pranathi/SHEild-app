package com.lucky.sheild;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplayActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnOpenMain, btnLogout;
    private DatabaseReference sentRef;
    private String uid;
    private List<String> displayList = new ArrayList<>();
    private List<String> phoneList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        listView = findViewById(R.id.listView);
        btnOpenMain = findViewById(R.id.btnOpenMain);
        btnLogout = findViewById(R.id.logout); // ✅ make sure you add this button in XML
        auth = FirebaseAuth.getInstance();

        uid = auth.getCurrentUser().getUid();
        sentRef = FirebaseDatabase.getInstance().getReference("sentLogs").child(uid);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        btnOpenMain.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        // ✅ Logout button functionality
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(DisplayActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DisplayActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevent going back
            startActivity(intent);
            finish();
        });

        sentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                displayList.clear();
                phoneList.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) s.getValue();
                    if (map == null) continue;
                    Object phonesO = map.get("phones");
                    Object namesO = map.get("names");
                    String item = "Phones: " + phonesO + ", Names: " + namesO;
                    displayList.add(item);
                    phoneList.add(phonesO != null ? phonesO.toString() : "N/A");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String phone = phoneList.get(position);
            Intent i = new Intent(DisplayActivity.this, EditNameActivity.class);
            i.putExtra("phone", phone);
            startActivity(i);
            Toast.makeText(DisplayActivity.this, "Editing: " + phone, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}

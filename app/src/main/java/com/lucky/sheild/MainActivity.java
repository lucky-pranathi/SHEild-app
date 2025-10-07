package com.lucky.sheild;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText name1, name2, name3, phone1, phone2, phone3;
    private Button btnSend;
    private BluetoothService btService;
    private boolean bound = false;
    private DatabaseReference contactsRef, sentRef;
    private String uid;

    // 🔹 Replace this with your ESP32’s Bluetooth MAC address
    private static final String ESP32_MAC = "00:4B:12:EF:2C:56";

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            btService = binder.getService();
            bound = true;

            // 🔹 Automatically connect when service is ready
            autoConnectBluetooth();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name1 = findViewById(R.id.name1);
        phone1 = findViewById(R.id.phone1);
        name2 = findViewById(R.id.name2);
        phone2 = findViewById(R.id.phone2);
        name3 = findViewById(R.id.name3);
        phone3 = findViewById(R.id.phone3);
        btnSend = findViewById(R.id.btnSend);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(uid);
        sentRef = FirebaseDatabase.getInstance().getReference("sentLogs").child(uid);

        // 🔹 Start and bind Bluetooth service automatically
        Intent i = new Intent(this, BluetoothService.class);
        startService(i);
        bindService(i, connection, BIND_AUTO_CREATE);

        // send button logic stays same
        btnSend.setOnClickListener(v -> doSend());
    }

    // 🔹 Function to automatically connect
    private void autoConnectBluetooth() {
        if (!bound || btService == null) {
            Toast.makeText(this, "Bluetooth service not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btService.isConnected()) {
            Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = btService.connectToDevice(ESP32_MAC);
        if (ok) {
            Toast.makeText(this, "Auto-connected to ESP32", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Auto-connect failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void doSend() {
        String[] phones = {
                phone1.getText().toString().trim(),
                phone2.getText().toString().trim(),
                phone3.getText().toString().trim()
        };
        String[] names = {
                name1.getText().toString().trim(),
                name2.getText().toString().trim(),
                name3.getText().toString().trim()
        };

        for (String p : phones) {
            if (p.isEmpty()) {
                Toast.makeText(this, "Enter all phone numbers", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 🔹 Format payload for ESP32
        StringBuilder payload = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            payload.append(names[i]).append(",").append(phones[i]);
            if (i < 2) payload.append(";");
        }

        // 🔹 Send via Bluetooth
        if (bound && btService.isConnected()) {
            btService.send(payload.toString());
        } else {
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
        }

        // 🔹 Save to Firebase
        Map<String, Object> contactUpdates = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> contact = new HashMap<>();
            contact.put("name", names[i]);
            contact.put("addedAt", ServerValue.TIMESTAMP);
            contactUpdates.put(phones[i], contact);
        }
        contactsRef.updateChildren(contactUpdates);

        String logId = sentRef.push().getKey();
        Map<String, Object> log = new HashMap<>();
        log.put("timestamp", ServerValue.TIMESTAMP);
        log.put("phones", phones);
        log.put("names", names);
        sentRef.child(logId).setValue(log).addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                Toast.makeText(this, "Data sent and logged", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, DisplayActivity.class));
            } else {
                Toast.makeText(this, "Logging failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}

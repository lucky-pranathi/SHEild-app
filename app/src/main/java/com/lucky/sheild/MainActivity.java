package com.lucky.sheild;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText name1, phone1, name2, phone2, name3, phone3;
    Button btnSave;
    String usernameKey;   // auto detected username/email key
    TextView noUpdateTxt;
    private static final String ESP32_MAC_ADDRESS = "80:F3:DA:64:41:F2"; // Replace with your ESP32 MAC
    private static final UUID ESP32_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GET CURRENT LOGGED-IN USER
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            connectToESP32();
        }

        // Use email or UID as username key
//        usernameKey = user.getEmail().replace(".", "_");  // Firebase-safe key
        usernameKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        name1 = findViewById(R.id.name1);
        phone1 = findViewById(R.id.phone1);
        name2 = findViewById(R.id.name2);
        phone2 = findViewById(R.id.phone2);
        name3 = findViewById(R.id.name3);
        phone3 = findViewById(R.id.phone3);
        btnSave = findViewById(R.id.btnSend);
        noUpdateTxt=findViewById(R.id.noUpdateTxt);

        btnSave.setOnClickListener(v -> saveContacts());
        noUpdateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DisplayActivity.class));
            }
        });
    }

    private void saveContacts() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        ref.child("contact1").setValue(new Contact(name1.getText().toString(), phone1.getText().toString()));
        ref.child("contact2").setValue(new Contact(name2.getText().toString(), phone2.getText().toString()));
        ref.child("contact3").setValue(new Contact(name3.getText().toString(), phone3.getText().toString()))
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Contacts Saved!", Toast.LENGTH_SHORT).show();
                    sendToESP32();

                    // Go to next page
//                    Intent i = new Intent(MainActivity.this, DisplayActivity.class);
//                    startActivity(i);
                });
    }

    private void sendToESP32() {

        // Read Phone numbers only
        String p1 = phone1.getText().toString().trim();
        String p2 = phone2.getText().toString().trim();
        String p3 = phone3.getText().toString().trim();

        // Basic validation
        if (p1.isEmpty() || p2.isEmpty() || p3.isEmpty()) {
            Toast.makeText(this, "Enter all 3 phone numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine into one string
        String finalData = p1 + "," + p2 + "," + p3;

        if (BluetoothConnectionManager.getInstance().isConnected()) {
            BluetoothConnectionManager.getInstance().sendData(finalData);
            Toast.makeText(this, "Numbers sent: " + finalData, Toast.LENGTH_LONG).show();

            // Go to display page
            startActivity(new Intent(MainActivity.this, DisplayActivity.class));
        }
        else {
            Toast.makeText(this, "ESP32 NOT connected!", Toast.LENGTH_SHORT).show();
        }
    }
    private void connectToESP32() {
        new Thread(() -> {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ESP32_MAC_ADDRESS);
                bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID);
                bluetoothSocket.connect();

                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Connected to ESP32", Toast.LENGTH_SHORT).show()
                );

                // Save socket in Singleton for other pages
                BluetoothConnectionManager.getInstance().setSocket(bluetoothSocket);

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        }).start();
    }

    // Model Class
    public static class Contact {
        public String name;
        public String phone;

        public Contact() {}

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }
}

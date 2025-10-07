package com.lucky.sheild;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private final IBinder binder = new LocalBinder();
    private BluetoothSocket socket;
    private BluetoothAdapter adapter;
    private ConnectedThread connectedThread;

    // Standard SPP UUID for ESP32
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Connect to given MAC address
    public boolean connectToDevice(String macAddress) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            showToast("Bluetooth not supported on this device");
            Log.e(TAG, "Bluetooth not supported");
            return false;
        }

        if (!adapter.isEnabled()) {
            showToast("Please enable Bluetooth first");
            Log.e(TAG, "Bluetooth not enabled");
            return false;
        }

        BluetoothDevice device = adapter.getRemoteDevice(macAddress);
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            adapter.cancelDiscovery();
            showToast("Connecting to " + macAddress + "...");
            socket.connect();

            connectedThread = new ConnectedThread(socket);
            connectedThread.start();

            showToast("✅ Connected successfully to " + device.getName());
            Log.i(TAG, "Connected to " + macAddress);
            return true;

        } catch (IOException e) {
            showToast("❌ Connection failed: " + e.getMessage());
            Log.e(TAG, "Connection failed", e);
            closeSocket();
            return false;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void send(String message) {
        if (connectedThread != null) {
            connectedThread.write(message.getBytes());
        } else {
            showToast("Not connected to any device");
            Log.e(TAG, "Not connected, cannot send");
        }
    }

    public void closeSocket() {
        try {
            if (connectedThread != null) connectedThread.cancel();
            if (socket != null) socket.close();
            showToast("Bluetooth disconnected");
        } catch (IOException e) {
            showToast("Error closing connection: " + e.getMessage());
        } finally {
            socket = null;
            connectedThread = null;
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream in;
        private final OutputStream out;

        ConnectedThread(BluetoothSocket socket) throws IOException {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = in.read(buffer);
                    if (bytes > 0) {
                        final String received = new String(buffer, 0, bytes);
                        Log.d(TAG, "Received: " + received);

                        Intent i = new Intent("BT_RECEIVED");
                        i.putExtra("data", received);
                        sendBroadcast(i);
                    }
                } catch (IOException e) {
                    showToast("⚠️ Connection lost: " + e.getMessage());
                    Log.e(TAG, "Disconnected", e);
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                out.write(bytes);
            } catch (IOException e) {
                showToast("Write failed: " + e.getMessage());
                Log.e(TAG, "Write failed", e);
            }
        }

        void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    // Helper method for Toast
    private void showToast(final String message) {
        mainHandler.post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
        );
    }
}

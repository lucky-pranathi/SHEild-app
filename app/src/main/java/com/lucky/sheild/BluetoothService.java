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

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private BluetoothAdapter adapter;
    private ConnectedThread connectedThread;

    private final IBinder binder = new LocalBinder();
    private final Handler main = new Handler(Looper.getMainLooper());

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

    // ---------------------- CONNECT -------------------------
    public void broadcastStatus(String msg) {
        Intent i = new Intent("BT_STATUS");
        i.putExtra("status", msg);
        sendBroadcast(i);
    }

    public boolean connectToDevice(String mac) {
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            broadcastStatus("Bluetooth unsupported");
            return false;
        }
        if (!adapter.isEnabled()) {
            broadcastStatus("Enable Bluetooth first");
            return false;
        }

        BluetoothDevice device = adapter.getRemoteDevice(mac);

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            adapter.cancelDiscovery();

            broadcastStatus("Connecting to ESP32…");

            socket.connect();

            connectedThread = new ConnectedThread(socket);
            connectedThread.start();

            broadcastStatus("Connected to ESP32");

            return true;

        } catch (Exception e) {
            broadcastStatus("Connection failed");
            closeSocket();
            return false;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void send(String msg) {
        if (connectedThread != null)
            connectedThread.write(msg.getBytes());
        else broadcastStatus("Not connected");
    }

    public void closeSocket() {
        try {
            if (connectedThread != null)
                connectedThread.cancel();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
        connectedThread = null;
        socket = null;
    }

    // ---------------------- DATA THREAD ----------------------
    private class ConnectedThread extends Thread {
        private final InputStream in;
        private final OutputStream out;

        ConnectedThread(BluetoothSocket s) throws IOException {
            in = s.getInputStream();
            out = s.getOutputStream();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            try {
                while (true) {
                    bytes = in.read(buffer);
                    if (bytes > 0) {
                        String data = new String(buffer, 0, bytes);
                        Log.d("BT", "Received: " + data);
                    }
                }
            } catch (IOException e) {
                broadcastStatus("Connection lost");
            }
        }

        void write(byte[] b) {
            try {
                out.write(b);
            } catch (Exception e) {
                broadcastStatus("Write failed");
            }
        }

        void cancel() {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}

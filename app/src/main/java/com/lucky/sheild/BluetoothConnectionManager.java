package com.lucky.sheild;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;
public class BluetoothConnectionManager {
    private static BluetoothConnectionManager instance;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    private BluetoothConnectionManager() {}

    public static synchronized BluetoothConnectionManager getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionManager();
        }
        return instance;
    }

    public void setSocket(BluetoothSocket socket) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
    }

    public void sendData(String message) {
        if (outputStream != null) {
            try {
                outputStream.write((message + "\n").getBytes()); // ESP expects newline
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}

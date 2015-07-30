package com.linky.mybluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by linky on 15-7-30.
 */
public class ConnectThread extends Thread {

    public static final String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private String mSocketType;

    public ConnectThread(BluetoothDevice device, UUID uuid) {
        mmDevice = device;
        BluetoothSocket tmp = null;

        try {
            device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a successful connection or an exception
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket

            Log.d(TAG, android.util.Log.getStackTraceString(e));

            try {
                mmSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close " + mSocketType + " socket during connection failure", e2);
                Log.d(TAG, android.util.Log.getStackTraceString(e2));
            }
//            connectionFailed();
            Log.d(TAG, "connect fail ");
            return;
        }

        Log.d(TAG, "connect success ");

        // Reset the ConnectThread because we're done
//        synchronized (ConnectThread.this) {
//            mConnectThread = null;
//        }

        // Start the connected thread
//        connected(mmSocket, mmDevice, mSocketType);
    }
}
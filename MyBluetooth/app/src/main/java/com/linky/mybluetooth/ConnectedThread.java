package com.linky.mybluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by linky on 15-7-30.
 */
public class ConnectedThread extends Thread {

    public static final String TAG = "ConnectedThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket, String socketType) {
        Log.d(TAG, "create ConnectedThread: " + socketType);
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
//        Log.i(TAG, "BEGIN mConnectedThread");
//        byte[] buffer = new byte[1024];
//        int bytes;
//
//        // Keep listening to the InputStream while connected
//        while (true) {
//            try {
//                // Read from the InputStream
//                bytes = mmInStream.read(buffer);
//
//                // Send the obtained bytes to the UI Activity
//                mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();
//            } catch (IOException e) {
//
//                Log.e(TAG, "disconnected", e);
//                Log.d(TAG, android.util.Log.getStackTraceString(e));
//
//                connectionLost();
//                // Start the service over to restart listening mode
//                BluetoothChatService.this.start();
//                break;
//            }
//        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);

            // Share the sent message back to the UI Activity
//            mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
//                    .sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}

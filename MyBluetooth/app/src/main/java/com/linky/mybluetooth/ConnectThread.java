package com.linky.mybluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.linky.mybluetooth.log.DebugLog;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            DebugLog.d(DebugLog.TAG, "ConnectThread:ConnectThread " + "createRfcommSocketToServiceRecord with " + uuid.toString());
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a successful connection or an exception
            DebugLog.d(DebugLog.TAG, "ConnectThread:run " + "mmSocket.connect();");
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket

            DebugLog.d(DebugLog.TAG, "ConnectThread:run " + android.util.Log.getStackTraceString(e));

            try {
                Class<?> clazz = mmSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                BluetoothSocket fallbackSocket = (BluetoothSocket) m.invoke(mmSocket.getRemoteDevice(), params);
                fallbackSocket.connect();

                DebugLog.d(DebugLog.TAG, "ConnectThread:run " + "fallbackSocket connect success");

            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.d(TAG, android.util.Log.getStackTraceString(e2));
                    DebugLog.d(DebugLog.TAG, "ConnectThread:run " + android.util.Log.getStackTraceString(e2));
                }
            }
//            connectionFailed();
            DebugLog.d(DebugLog.TAG, "ConnectThread:run " +  "connect fail ");
            return;
        }

        DebugLog.d(DebugLog.TAG, "ConnectThread:run " + "connect success ");

        // Reset the ConnectThread because we're done
//        synchronized (ConnectThread.this) {
//            mConnectThread = null;
//        }

        // Start the connected thread
//        connected(mmSocket, mmDevice, mSocketType);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
        }
    }
}
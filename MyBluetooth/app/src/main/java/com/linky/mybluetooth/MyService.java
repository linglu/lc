package com.linky.mybluetooth;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;

import com.linky.mybluetooth.log.DebugLog;

public class MyService extends Service {

    private static final int DEFAULT_INT_VALUE = -3141;
    private static final short DEFAULT_SHORT_VALUE = -31;

    private static String ACTION_ACL_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED;
    private static String ACTION_ACL_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED;
    private static String ACTION_ACL_DISCONNECT_REQUESTED = BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED;
    private static String ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
    private static String ACTION_CLASS_CHANGED = BluetoothDevice.ACTION_CLASS_CHANGED;
    private static String ACTION_FOUND = BluetoothDevice.ACTION_FOUND;
    private static String ACTION_NAME_CHANGED = BluetoothDevice.ACTION_NAME_CHANGED;
    private static String ACTION_PAIRING_REQUEST = BluetoothDevice.ACTION_PAIRING_REQUEST;
    private static String ACTION_UUID = BluetoothDevice.ACTION_UUID;

    // BluetoothHeadset
    private static final String ACTION_AUDIO_STATE_CHANGED = BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED;
    private static final String ACTION_CONNECTION_STATE_CHANGED = BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED;
    private static final String ACTION_VENDOR_SPECIFIC_HEADSET_EVENT = BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT;

    //

    public MyService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        DebugLog.d(DebugLog.TAG, "MyService:onCreate " + "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        DebugLog.d(DebugLog.TAG, "MyService:onBind " + "");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DebugLog.d(DebugLog.TAG, "MyService:onStartCommand " + "");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ACL_CONNECTED);
        intentFilter.addAction(ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(ACTION_CLASS_CHANGED);
        intentFilter.addAction(ACTION_FOUND);
        intentFilter.addAction(ACTION_NAME_CHANGED);
        intentFilter.addAction(ACTION_PAIRING_REQUEST);
        intentFilter.addAction(ACTION_UUID);
        intentFilter.addAction(ACTION_AUDIO_STATE_CHANGED);
        intentFilter.addAction(ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);

        registerReceiver(mDeviceReceiver,intentFilter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugLog.d(DebugLog.TAG, "MyService:onDestroy " + "");
    }

    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Remote device discovered.
            if(ACTION_FOUND.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive" + " ACTION_FOUND");

                BluetoothClass bc = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                showAndDisplayBluetoothClass(bc);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showAndDisplayBluetoothDevice(device);

                String extraName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraName = " + extraName);

                short extraShort = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, DEFAULT_SHORT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraShort = " + extraShort   );

                // Indicates a low level (ACL) connection has been established with a remote device.
            } else if (ACTION_ACL_CONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_CONNECTED");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showAndDisplayBluetoothDevice(device);

                // Indicates a low level (ACL) disconnection from a remote device.
            } else if (ACTION_ACL_DISCONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_DISCONNECTED");

                //  Indicates that a low level (ACL) disconnection has been requested for a remote device, and it will soon be disconnected.
            } else if (ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_DISCONNECT_REQUESTED");

                // : Indicates a change in the bond state of a remote device.
            } else if (ACTION_BOND_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_BOND_STATE_CHANGED");

                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "previousBondState = " + previousBondState );

                // Bluetooth class of a remote device has changed.
            } else if (ACTION_CLASS_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_CLASS_CHANGED");

                // Indicates the friendly name of a remote device has been retrieved for the first time, or changed since the last retrieval.
            } else if (ACTION_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_NAME_CHANGED");

                // This intent is used to broadcast PAIRING REQUEST Requires BLUETOOTH_ADMIN to receive.
            } else if (ACTION_PAIRING_REQUEST.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_PAIRING_REQUEST");

                int extraPairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, DEFAULT_INT_VALUE);
                int variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraPairingKey = " + extraPairingKey);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "variant = " + variant );

                // This intent is used to broadcast the UUID wrapped as a ParcelUuid of the remote device after it has been fetched.
            } else if (ACTION_UUID.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_UUID");

//                ParcelUuid[] pUUIDs = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
//                for(ParcelUuid uuid : pUUIDs) {
//                    DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "uuid = " + uuid.getUuid().toString());
//                }
            } else if (ACTION_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_NAME_CHANGED");
                BluetoothClass bc = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                showAndDisplayBluetoothClass(bc);
            } else if (ACTION_AUDIO_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ACTION_AUDIO_STATE_CHANGED");
//
//
//                EXTRA_PREVIOUS_STATE
//                EXTRA_DEVICE

                int extraState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, DEFAULT_INT_VALUE);
                if(extraState == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_CONNECTED");
                } else if (extraState == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_CONNECTING");
                } else if (extraState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_DISCONNECTED");
                }

                /**
                 * This intent will be broad_casted with EXTRA_STATE set to STATE_AUDIO_CONNECTING.
                 */

            } else if (ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ACTION_CONNECTION_STATE_CHANGED");
            } else if (ACTION_VENDOR_SPECIFIC_HEADSET_EVENT.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ACTION_VENDOR_SPECIFIC_HEADSET_EVENT");
            }
        }
    };

    private void showAndDisplayBluetoothClass(BluetoothClass bc) {
        int desc = bc.describeContents();
        int deviceClass = bc.getDeviceClass();
        int majorDevice = bc.getMajorDeviceClass();

        DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "desc = " + desc);
        DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "deviceClass = " + deviceClass);
        DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "majorDevice = " + majorDevice);
    }

    private void showAndDisplayBluetoothDevice(BluetoothDevice device) {
        String address = device.getAddress();
        int bondState = device.getBondState();  // 可能值：BOND_NONE == 10, BOND_BONDING == 11, BOND_BONDED == 12.

        // 可能值：
        // DEVICE_TYPE_CLASSIC == 1     # Classic - BR/EDR devices
        // DEVICE_TYPE_LE == 2          # Low Energy - LE-only
        // DEVICE_TYPE_DUAL == 3        # Dual Mode - BR/EDR/LE
        // DEVICE_TYPE_UNKNOWN == 0     #
        int type = device.getType();
        ParcelUuid[] uuids = device.getUuids();
        int desc = device.describeContents();

        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "address = " + address);
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "bondState = " + bondState);   // 12
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "type = " + type); // 1
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "desc = " + desc);

        for(ParcelUuid uuid : uuids) {
            DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "uuid = " + uuid.getUuid().toString());
        }

    }
}

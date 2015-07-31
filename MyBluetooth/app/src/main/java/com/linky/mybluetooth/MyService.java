package com.linky.mybluetooth;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;

import com.linky.mybluetooth.log.DebugLog;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MyService extends Service {

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private static final UUID SDP_HeadsetServiceClass_UUID = UUID.fromString("00001108-0000-1000-8000-00805F9B34FB");
    private static final UUID SDP_HandsfreeServiceClass_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    private static final UUID SDP_AudioSinkServiceClass_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    private static final UUID SDP_AVRemoteControlServiceClass_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");

    private static final int DEFAULT_INT_VALUE = -3141;
    private static final short DEFAULT_SHORT_VALUE = -31;

    // BluetoothA2dp
    private static final String A2DP_ACTION_CONNECTION_STATE_CHANGED  = BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED;  // EXTRA_STATE/EXTRA_PREVIOUS_STATE/EXTRA_DEVICE
    private static final String A2DP_ACTION_PLAYING_STATE_CHANGED = BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED;              // EXTRA_STATE/EXTRA_PREVIOUS_STATE/EXTRA_DEVICE

    // BluetoothAdapter
    private static final String ADAPTER_ACTION_CONNECTION_STATE_CHANGED = BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED; // EXTRA_CONNECTION_STATE/EXTRA_PREVIOUS_CONNECTION_STATE/EXTRA_DEVICE
    private static final String ADAPTER_ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
    private static final String ADAPTER_ACTION_DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
    private static final String ADAPTER_ACTION_LOCAL_NAME_CHANGED = BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED;     //  EXTRA_LOCAL_NAME
    private static final String ADAPTER_ACTION_REQUEST_DISCOVERABLE = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
    private static final String ADAPTER_ACTION_REQUEST_ENABLE = BluetoothAdapter.ACTION_REQUEST_ENABLE;
    private static final String ADAPTER_ACTION_SCAN_MODE_CHANGED = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
    private static final String ADAPTER_ACTION_STATE_CHANGED = BluetoothAdapter.ACTION_STATE_CHANGED;   //  EXTRA_STATE/EXTRA_PREVIOUS_STATE

    // BluetoothHeadset
    /**
     * EXTRA_STATE - The current state of the profile.
     * EXTRA_PREVIOUS_STATE- The previous state of the profile.
     * EXTRA_DEVICE - The remote device.
     */
    private static final String HEADSET_ACTION_AUDIO_STATE_CHANGED = BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED;

    /**
     * EXTRA_STATE - The current state of the profile.
     * EXTRA_PREVIOUS_STATE- The previous state of the profile.
     * EXTRA_DEVICE - The remote device.
     */
    private static final String HEADSET_ACTION_CONNECTION_STATE_CHANGED = BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED;

    /**
     * EXTRA_DEVICE - The remote Bluetooth Device
     * EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD - The vendor specific command
     * EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE - The AT command type which can be one of AT_CMD_TYPE_READ, AT_CMD_TYPE_TEST, or AT_CMD_TYPE_SET, AT_CMD_TYPE_BASIC,AT_CMD_TYPE_ACTION.
     * EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS - Command arguments.
     */
    private static final String HEADSET_ACTION_VENDOR_SPECIFIC_HEADSET_EVENT = BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT;

    // BluetoothDevice
    private static String DEVICE_ACTION_ACL_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED;
    private static String DEVICE_ACTION_ACL_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED;
    private static String DEVICE_ACTION_ACL_DISCONNECT_REQUESTED = BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED;
    private static String DEVICE_ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
    private static String DEVICE_ACTION_CLASS_CHANGED = BluetoothDevice.ACTION_CLASS_CHANGED;
    private static String DEVICE_ACTION_FOUND = BluetoothDevice.ACTION_FOUND;
    private static String DEVICE_ACTION_NAME_CHANGED = BluetoothDevice.ACTION_NAME_CHANGED;
    private static String DEVICE_ACTION_PAIRING_REQUEST = BluetoothDevice.ACTION_PAIRING_REQUEST;
    private static String DEVICE_ACTION_UUID = BluetoothDevice.ACTION_UUID;

    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private int mState;
    private ConnectedThread mConnectedThread;

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

        intentFilter.addAction(A2DP_ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(A2DP_ACTION_PLAYING_STATE_CHANGED);
        intentFilter.addAction(ADAPTER_ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(ADAPTER_ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(ADAPTER_ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(ADAPTER_ACTION_LOCAL_NAME_CHANGED);
        intentFilter.addAction(ADAPTER_ACTION_REQUEST_DISCOVERABLE);
        intentFilter.addAction(ADAPTER_ACTION_REQUEST_ENABLE);
        intentFilter.addAction(ADAPTER_ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(ADAPTER_ACTION_STATE_CHANGED);
        intentFilter.addAction(HEADSET_ACTION_AUDIO_STATE_CHANGED);
        intentFilter.addAction(HEADSET_ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        intentFilter.addAction(HEADSET_ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(DEVICE_ACTION_ACL_CONNECTED);
        intentFilter.addAction(DEVICE_ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(DEVICE_ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(DEVICE_ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(DEVICE_ACTION_CLASS_CHANGED);
        intentFilter.addAction(DEVICE_ACTION_FOUND);
        intentFilter.addAction(DEVICE_ACTION_NAME_CHANGED);
        intentFilter.addAction(DEVICE_ACTION_PAIRING_REQUEST);
        intentFilter.addAction(DEVICE_ACTION_UUID);

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

            if (A2DP_ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "A2DP_ACTION_CONNECTION_STATE_CHANGED");
                showState(intent);

            } else if (A2DP_ACTION_PLAYING_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "A2DP_ACTION_PLAYING_STATE_CHANGED");
                showState(intent);

            } else if (ADAPTER_ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_CONNECTION_STATE_CHANGED");

                int EXTRA_CONNECTION_STATE = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " EXTRA_CONNECTION_STATE = " + EXTRA_CONNECTION_STATE);

                int EXTRA_PREVIOUS_CONNECTION_STATE = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " EXTRA_PREVIOUS_CONNECTION_STATE = " + EXTRA_PREVIOUS_CONNECTION_STATE);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " addr = " + device.getAddress());

            } else if (ADAPTER_ACTION_DISCOVERY_FINISHED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_DISCOVERY_FINISHED");

            } else if (ADAPTER_ACTION_DISCOVERY_STARTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_DISCOVERY_STARTED");

            } else if (ADAPTER_ACTION_LOCAL_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_LOCAL_NAME_CHANGED");

                String EXTRA_LOCAL_NAME = intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "EXTRA_LOCAL_NAME = " + EXTRA_LOCAL_NAME);

            } else if (ADAPTER_ACTION_REQUEST_DISCOVERABLE.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_REQUEST_DISCOVERABLE");

            } else if (ADAPTER_ACTION_REQUEST_ENABLE.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_REQUEST_ENABLE");

            } else if (ADAPTER_ACTION_SCAN_MODE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_SCAN_MODE_CHANGED");

            } else if (ADAPTER_ACTION_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "ADAPTER_ACTION_STATE_CHANGED");

                int EXTRA_STATE = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "EXTRA_STATE = " + EXTRA_STATE);

                int EXTRA_PREVIOUS_STATE = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "EXTRA_PREVIOUS_STATE = " + EXTRA_PREVIOUS_STATE);

            } else if (HEADSET_ACTION_AUDIO_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "HEADSET_ACTION_AUDIO_STATE_CHANGED");


                int EXTRA_PREVIOUS_STATE = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "EXTRA_PREVIOUS_STATE = "+ EXTRA_PREVIOUS_STATE);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "addr = " + device.getAddress());

                int extraState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, DEFAULT_INT_VALUE);
                if (extraState == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_CONNECTED");
                } else if (extraState == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_CONNECTING");
                } else if (extraState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "extraState = " + "STATE_AUDIO_DISCONNECTED");
                }

            } else if (HEADSET_ACTION_VENDOR_SPECIFIC_HEADSET_EVENT.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "HEADSET_ACTION_VENDOR_SPECIFIC_HEADSET_EVENT");

            } else if (HEADSET_ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "HEADSET_ACTION_CONNECTION_STATE_CHANGED");

                int EXTRA_STATE = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " EXTRA_STATE = " + EXTRA_STATE);

                int EXTRA_PREVIOUS_STATE = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " EXTRA_PREVIOUS_STATE = " + EXTRA_PREVIOUS_STATE);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String addr = device.getAddress();
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " addr = " + addr);

            } else if (DEVICE_ACTION_ACL_CONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_ACL_CONNECTED");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showAndDisplayBluetoothDevice(device);
                connectToProfileService();

            } else if (DEVICE_ACTION_ACL_DISCONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_ACL_DISCONNECTED");
                disconnectToProfileService();

            } else if (DEVICE_ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_ACL_DISCONNECT_REQUESTED");

            } else if (DEVICE_ACTION_BOND_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_BOND_STATE_CHANGED");

                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "previousBondState = " + previousBondState);

            } else if (DEVICE_ACTION_CLASS_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_CLASS_CHANGED");

            } else if (DEVICE_ACTION_FOUND.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_FOUND");
                BluetoothClass bc = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                showAndDisplayBluetoothClass(bc);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showAndDisplayBluetoothDevice(device);

                String extraName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraName = " + extraName);

                short extraShort = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, DEFAULT_SHORT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraShort = " + extraShort);
            } else if (DEVICE_ACTION_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_NAME_CHANGED");

            } else if (DEVICE_ACTION_PAIRING_REQUEST.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_PAIRING_REQUEST");

                int extraPairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, DEFAULT_INT_VALUE);
                int variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraPairingKey = " + extraPairingKey);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "variant = " + variant);

            } else if (DEVICE_ACTION_UUID.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MyService:onReceive " + "DEVICE_ACTION_UUID");
                ParcelUuid pUUID = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);

//                for (ParcelUuid uuid : pUUIDs) {
                    DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "uuid = " + pUUID.getUuid().toString());
//                }
            }
        }
    };

    private void showState(Intent intent) {
        int EXTRA_STATE = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, DEFAULT_INT_VALUE);
        DebugLog.d(DebugLog.TAG, "MyService:showState " + " EXTRA_STATE = " + EXTRA_STATE);

        int EXTRA_PREVIOUS_STATE = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, DEFAULT_INT_VALUE);
        DebugLog.d(DebugLog.TAG, "MyService:showState " + " EXTRA_PREVIOUS_STATE = " + EXTRA_PREVIOUS_STATE);

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String addr = device.getAddress();
        DebugLog.d(DebugLog.TAG, "MyService:onReceive " + " addr = " + addr);
    }

    private void disconnectToProfileService() {
        if(mBluetoothHeadset != null)
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
    }

    private void connectToProfileService() {
        // Get the default adapter.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);
    }

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected ");
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
                List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();

                DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected " + "devices.size() = " + devices.size());
                for(BluetoothDevice dev : devices) {
                    String name = dev.getName();
                    DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected " + "name =" + name);
                    boolean isAudioConnected = mBluetoothHeadset.isAudioConnected(dev);
                    DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected " + "isAudioConnected = " + isAudioConnected);

                    /**
                     * Start Bluetooth voice recognition. This methods sends the voice
                     * recognition AT command to the headset and establishes the audio connection.
                     * Users can listen to ACTION_AUDIO_STATE_CHANGED. If this function returns true,
                     * this intent will be broadcasted with EXTRA_STATE set to STATE_AUDIO_CONNECTING.
                     * EXTRA_STATE will transition from STATE_AUDIO_CONNECTING to STATE_AUDIO_CONNECTED
                     * when audio connection is established and to STATE_AUDIO_DISCONNECTED in case of failure
                     * to establish the audio connection.
                     */
                    boolean started = mBluetoothHeadset.startVoiceRecognition(dev);
                    if(started) {   // 如果成功启动；
                        DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected " + "startVoiceRecognition started == true");
                    } else {
                        DebugLog.d(DebugLog.TAG, "MainActivity:onServiceConnected " + "startVoiceRecognition started == false");
                    }
                }
            }
        }
        public void onServiceDisconnected(int profile) {
            DebugLog.d(DebugLog.TAG, "MainActivity:onServiceDisconnected " + "");
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
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
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "desc = " + desc); // 0

        for(ParcelUuid uuid : uuids) {
            DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "uuid = " + uuid.getUuid().toString());
            if(SDP_HandsfreeServiceClass_UUID.equals(uuid.getUuid())) {
                mConnectThread = new ConnectThread(device, SDP_HandsfreeServiceClass_UUID);
                mConnectThread.start();

//                start(SDP_HandsfreeServiceClass_UUID);
//                mConnectThread = new ConnectThread(device, SDP_HeadsetServiceClass_UUID);
//                mConnectThread.start();
            }
        }
    }

    public synchronized void start(UUID uuid) {
        DebugLog.d(DebugLog.TAG, "MyService:start " + "");

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true, uuid);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false, uuid);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        DebugLog.d(DebugLog.TAG, "MyService:setState " + "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure, UUID uuid) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, uuid);
                } else {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, uuid);
                }
            } catch (IOException e) {
                DebugLog.d(DebugLog.TAG, "AcceptThread:AcceptThread " + "Socket Type: " + mSocketType + "listen() failed" + e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            DebugLog.d(DebugLog.TAG, "AcceptThread:run " + "Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    DebugLog.d(DebugLog.TAG, "AcceptThread:run " + "Socket Type: " + mSocketType + "accept() failed" + e.toString());
                    break;
                }

                // If a Connection Was Accepted
                if (socket != null) {
                    synchronized (MyService.this) {
                        DebugLog.d(DebugLog.TAG, "AcceptThread:run " + "socket != null");
                        connected(socket, socket.getRemoteDevice(), mSocketType);
//                        switch (mState) {
//                            case STATE_LISTEN:
//                            case STATE_CONNECTING:
//                                // Situation normal. Start the connected thread.
//
//                                break;
//                            case STATE_NONE:
//                            case STATE_CONNECTED:
//                                // Either not ready or already connected. Terminate new socket.
//                                try {
//                                    socket.close();
//                                } catch (IOException e) {
//                                    DebugLog.d(DebugLog.TAG, "AcceptThread:run " + "Could not close unwanted socket" + e.toString());
//                                }
//                                break;
//                        }
                    }
                }
            }
            DebugLog.d(DebugLog.TAG, "AcceptThread:run " + "END mAcceptThread, socket Type: " + mSocketType);
        }

        public void cancel() {
            DebugLog.d(DebugLog.TAG, "AcceptThread:cancel " + "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                DebugLog.d(DebugLog.TAG, "AcceptThread:cancel " + "Socket Type" + mSocketType + "close() of server failed" + e.toString());
            }
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        DebugLog.d(DebugLog.TAG, "MyService:connected " + "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//
//        // Cancel the accept thread because we only want to connect to one device
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

//        Send the name of the connected device back to the UI Activity
//        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }
}

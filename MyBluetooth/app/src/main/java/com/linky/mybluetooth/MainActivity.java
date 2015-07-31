package com.linky.mybluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.linky.mybluetooth.localbluetooth.LocalBluetoothProfile;
import com.linky.mybluetooth.localbluetooth.Utils;
import com.linky.mybluetooth.log.DebugLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int DEFAULT_INT_VALUE = -3141;
    private static final short DEFAULT_SHORT_VALUE = -31;

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;

    private ListAdapter mNewDevicesArrayAdapter;
    private ListAdapter mPairedDevicesArrayAdapter;

    private List<LocalBluetoothProfile> mProfiles;

    BluetoothHeadset mBluetoothHeadset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = new Intent(this, MyService.class);
        startService(in);

        // Close proxy connection after use.

        // 用于显示进度条，写在 setContentView 之前
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        setContentView(R.layout.activity_main);
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if(mBluetoothAdapter == null) {
//            finish();
//        }
//
//        if(!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
//
//        // Register for broadcasts when a device is discovered
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mDeviceReceiver, filter);
//
//        // Register for broadcasts when discovery has finished
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        this.registerReceiver(mDeviceReceiver, filter);
//
//        setupListView();
    }



    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
    }

    private void setupListView() {
        mPairedDevicesArrayAdapter = new ListAdapter(this);
        mNewDevicesArrayAdapter = new ListAdapter(this);

        // Find and set up the ListView for paired devices
        mListView = (ListView) findViewById(R.id.paired_device);
        mListView.setAdapter(mPairedDevicesArrayAdapter);
        mListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_discovery_device);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        setupDevice();
    }

    private void setupDevice() {

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if(devices.size() > 0) {
            for(BluetoothDevice device : devices) {
                mPairedDevicesArrayAdapter.addData(device);
            }
        } else {
            mPairedDevicesArrayAdapter.addData(null);
        }
    }

    private String mName;
    private BluetoothDevice mDevice;


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            mDevice = (BluetoothDevice) view.getTag();

            // Get the device MAC address, which is the last 17 chars in the View
//            String info = ((TextView) view).getText().toString();
//            String address = info.substring(info.length() - 17);
//            mName = info.substring(0, info.length() - 17);

            String address = mDevice.getAddress();
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if( device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                // 配对
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    Boolean success = (Boolean) createBondMethod.invoke(device);

                    DebugLog.d(DebugLog.TAG, "MainActivity:onItemClick" + "returnValue = " + success);

                    if (success) {
                       Toast.makeText(MainActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
                        // 配对成功之后，获得 socket，然后进行连接；
                        connect();
                    } else
                        Toast.makeText(MainActivity.this, "配对失败", Toast.LENGTH_SHORT).show();

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    DebugLog.d(DebugLog.TAG, "MainActivity:onItemClick" + Log.getStackTraceString(e));
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    DebugLog.d(DebugLog.TAG, "MainActivity:onItemClick" + Log.getStackTraceString(e));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    DebugLog.d(DebugLog.TAG, "MainActivity:onItemClick" + Log.getStackTraceString(e));
                }
            }
        }
    };

    public void connect() {
        for (LocalBluetoothProfile profile : mProfiles) {
            connectInt(profile);
        }
    }

    synchronized void connectInt(LocalBluetoothProfile profile) {

        if (profile.connect(mDevice)) {
            if (Utils.D) {
                Log.d(TAG, "MainActivity:connectInt" + "Command sent successfully:CONNECT " + describe(profile));
            }
            return;
        }
        Log.d(TAG, "MainActivity:connectInt" + "Failed to connect " + profile.toString() + " to " + mName);
    }

    /**
     * Describes the current device and profile for logging.
     *
     * @param profile Profile to describe
     * @return Description of the device and profile
     */
    private String describe(LocalBluetoothProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:").append(mDevice);
        if (profile != null) {
            sb.append(" Profile:").append(profile);
        }

        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_LONG).show();
                    setupDevice();
                    // Bluetooth is now enabled, so set up a chat session
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DebugLog.d(DebugLog.TAG, "MainActivity:onPrepareOptionsMenu" + "");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugLog.d(DebugLog.TAG, "MainActivity:onCreateOptionsMenu" + "");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        DebugLog.d(DebugLog.TAG, "MainActivity:onMenuItemSelected" + "");

        switch (item.getItemId()) {
            case R.id.begin_scan:
                mBluetoothAdapter.startDiscovery();
                setProgressBarIndeterminateVisibility(true);
                return true;
        }

        return false;
    }

    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Remote device discovered.
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
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
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_CONNECTED");

            // Indicates a low level (ACL) disconnection from a remote device.
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_DISCONNECTED");

                //  Indicates that a low level (ACL) disconnection has been requested for a remote device, and it will soon be disconnected.
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_ACL_DISCONNECT_REQUESTED");

                // : Indicates a change in the bond state of a remote device.
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_BOND_STATE_CHANGED");

                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "previousBondState = " + previousBondState );

                // Bluetooth class of a remote device has changed.
            } else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_CLASS_CHANGED");

                // Indicates the friendly name of a remote device has been retrieved for the first time, or changed since the last retrieval.
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_NAME_CHANGED");

                // This intent is used to broadcast PAIRING REQUEST Requires BLUETOOTH_ADMIN to receive.
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_PAIRING_REQUEST");

                int extraPairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, DEFAULT_INT_VALUE);
                int variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,DEFAULT_INT_VALUE);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "extraPairingKey = " + extraPairingKey);
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "variant = " + variant );

                // This intent is used to broadcast the UUID wrapped as a ParcelUuid of the remote device after it has been fetched.
            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_UUID");

//                ParcelUuid[] pUUIDs = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
//                for(ParcelUuid uuid : pUUIDs) {
//                    DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "uuid = " + uuid.getUuid().toString());
//                }
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                DebugLog.d(DebugLog.TAG, "MainActivity:onReceive " + "ACTION_NAME_CHANGED");
                BluetoothClass bc = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                showAndDisplayBluetoothClass(bc);
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
        int bondState = device.getBondState();
        int type = device.getType();
        ParcelUuid[] uuids = device.getUuids();
        int desc = device.describeContents();

        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "address = " + address);
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "bondState = " + bondState);
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "type = " + type);
        DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "desc = " + desc);

        for(ParcelUuid uuid : uuids) {
            DebugLog.d(DebugLog.TAG, "MainActivity:showAndDisplayBluetoothDevice " + "uuid = " + uuid.getUuid().toString());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }
}

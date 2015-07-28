package com.linky.mybluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;

    private ListAdapter mNewDevicesArrayAdapter;
    private ListAdapter mPairedDevicesArrayAdapter;

    private List<LocalBluetoothProfile> mProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 用于显示进度条，写在 setContentView 之前
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null) {
            finish();
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mDeviceReceiver, filter);

        setupListView();
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

//            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    mNewDevicesArrayAdapter.addData(device);
//                }
//
//            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                setProgressBarIndeterminateVisibility(false);
//                Toast.makeText(MainActivity.this, "扫描完成", Toast.LENGTH_SHORT).show();
//                if (mNewDevicesArrayAdapter.getCount() == 0) {
//                    String noDevices = "no device found".toString();
//                    Toast.makeText(MainActivity.this, noDevices, Toast.LENGTH_SHORT).show();
////                    mNewDevicesArrayAdapter.addData(null);
//                }
//            }

            // Remote device discovered.
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {



            // Indicates a low level (ACL) connection has been established with a remote device.
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

            // Indicates a low level (ACL) disconnection from a remote device.
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                //  Indicates that a low level (ACL) disconnection has been requested for a remote device, and it will soon be disconnected.
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {

                // : Indicates a change in the bond state of a remote device.
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

                // Bluetooth class of a remote device has changed.
            } else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {

                // Indicates the friendly name of a remote device has been retrieved for the first time, or changed since the last retrieval.
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {

                // This intent is used to broadcast PAIRING REQUEST Requires BLUETOOTH_ADMIN to receive.
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {

                // This intent is used to broadcast the UUID wrapped as a ParcelUuid of the remote device after it has been fetched.
            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {

            }

        }
    }    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }
}

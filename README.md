#### adb 启动 Service 命令

`am startservice -n com.linky.mybluetooth/com.linky.mybluetooth.MyService`

调试笔记：
1. 添加如下广播
```
    private static String ACTION_ACL_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED;
    private static String ACTION_ACL_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED;
    private static String ACTION_ACL_DISCONNECT_REQUESTED = BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED;
    private static String ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
    private static String ACTION_CLASS_CHANGED = BluetoothDevice.ACTION_CLASS_CHANGED;
    private static String ACTION_FOUND = BluetoothDevice.ACTION_FOUND;
    private static String ACTION_NAME_CHANGED = BluetoothDevice.ACTION_NAME_CHANGED;
    private static String ACTION_PAIRING_REQUEST = BluetoothDevice.ACTION_PAIRING_REQUEST;
    private static String ACTION_UUID = BluetoothDevice.ACTION_UUID;
```
运行，并执行连接操作，结果如下：
>* 连接上时，收到广播<br />
D/DebugLog(11323): MainActivity:onReceive ACTION_BOND_STATE_CHANGED
D/DebugLog(11323): MainActivity:onReceive previousBondState = 10
D/DebugLog(11323): MainActivity:onReceive ACTION_ACL_CONNECTED

>* 断开时，收到广播<br />
D/DebugLog( 9487): MainActivity:onReceive ACTION_ACL_DISCONNECTED
D/DebugLog( 9487): MainActivity:onReceive ACTION_BOND_STATE_CHANGED
D/DebugLog( 9487): MainActivity:onReceive previousBondState = 1


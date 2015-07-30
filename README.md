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

#### 相关知识
sco 是蓝牙基带的一个方式。蓝牙音频对称传输这些你应该从下面的解释中看到。

蓝牙基带技术支持两种连接方式：
面向连接（SCO）方式：主要用于话音传输；<br />
无连接(ACL)方式：主要用于分组数据传输。<br />
在同一微微网中，不同的主从设备可以采用不同的连接方式，在一次通信中，连接方式可以任意改变。每一连接方式可支持16种不同的分组类型，其中控制分组有4种，是SCO和ACL通用的分组，两种连接方式均采用时分双工（TDD）通信。SCO为对称连接，支持限时话音传送，主从设备无需轮询即可发送数据。SCO的分组既可以是话音又可以是数据，当发生中断时，只有数据部分需要重传。一般是单声道都有应用这个的。ACL是面向分组的连接，它支持对称和非对称两种传输流量，也支持广播信息。在ACL方式下，主设备控制链路带宽，负责从设备带宽的分配；从设备依轮询发送数据。


#### 相关连接
http://codego.net/554937/

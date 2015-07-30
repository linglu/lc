package com.linky.mybluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linky.mybluetooth.log.DebugLog;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        DebugLog.d(DebugLog.TAG, "MyReceiver:onReceive " + "");
        Intent in = new Intent(context, MyService.class);
        context.startService(in);
    }
}

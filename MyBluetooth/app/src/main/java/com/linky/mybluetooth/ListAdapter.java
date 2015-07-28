package com.linky.mybluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linky on 15-7-24.
 */
public class ListAdapter extends BaseAdapter{

    private List<BluetoothDevice> mDatas;
    private Context mContext;

    public ListAdapter(Context context) {
        mContext = context;
        mDatas = new ArrayList<>();
    }

    public void addData(BluetoothDevice data) {
        mDatas.add(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.device_name, parent, false);
        }

        BluetoothDevice device = mDatas.get(position);
        if(device != null) {
            ((TextView)convertView).setText(device.getName() + "/n" + device.getAddress());
            convertView.setTag(device);
//        } else {
//            String noDevices = "no device have been paired".toString();
//            ((TextView)convertView).setText(noDevices);
        }

        return convertView;
    }
}

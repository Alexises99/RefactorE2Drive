package com.example.refactore2drive.obd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.ToastUtils;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.SelectWearActivity;
import com.example.refactore2drive.models.Device;

import java.util.ArrayList;

public class SelectObdActivity extends AppCompatActivity {
    private SwitchCompat switchCompat;
    private DeviceListAdapter deviceListAdapter;
    private String username;
    private DatabaseHelper db;
    private BluetoothAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_obd);
        db = new DatabaseHelper(this);
        username = Helper.getUsername(this);
        myAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filter);
        if (myAdapter == null) ToastUtils.show(this, "Bluetooth no soportado");
        switchCompat = findViewById(R.id.obd_switch);
        //Alternar entre escanear o no
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            deviceListAdapter.clear();
            deviceListAdapter.notifyDataSetChanged();
            if (switchCompat.isChecked()) {
                myAdapter.startDiscovery();
            } else {
                myAdapter.cancelDiscovery();
                for (BluetoothDevice device : myAdapter.getBondedDevices()) {
                    deviceListAdapter.addDevice(device);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        });
        ListView listView = findViewById(R.id.obd_list);
        deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice device = deviceListAdapter.getDevice(position);
            db.createObd(new Device(device.getName(), device.getAddress(), username));
            myAdapter.cancelDiscovery();
            startActivity(new Intent(this, SelectWearActivity.class));
        });
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceListAdapter.addDevice(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!myAdapter.isEnabled()) myAdapter.enable();
        try {
            db.getObd(username);
            startActivity(new Intent(this, SelectWearActivity.class));
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("dispositivo no guardado", "no guardado");
        }
        for (BluetoothDevice device : myAdapter.getBondedDevices()) {
            deviceListAdapter.addDevice(device);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        deviceListAdapter.clear();
        db.closeDB();
        super.onPause();
    }

    private class DeviceListAdapter extends BaseAdapter {
        private final ArrayList<BluetoothDevice> mDevices;
        private final LayoutInflater mInflator;

        public DeviceListAdapter() {
            super();
            mDevices = new ArrayList<>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mDevices.contains(device)) {
                mDevices.add(device);
            }
        }
        public BluetoothDevice getDevice(int position) {
            return mDevices.get(position);
        }
        public void clear() {
            mDevices.clear();
        }
        @Override
        public int getCount() {
            return mDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @SuppressLint("InflateParams")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.list_item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Desconocido");
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
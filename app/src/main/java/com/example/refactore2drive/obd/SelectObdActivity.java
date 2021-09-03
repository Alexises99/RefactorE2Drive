package com.example.refactore2drive.obd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.refactore2drive.DeviceListAdapter;
import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.ToastUtils;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.SelectWearActivity;
import com.example.refactore2drive.models.Device;


public class SelectObdActivity extends AppCompatActivity {
    private SwitchCompat switchCompat;
    private ListView listView;
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

        initialize();
        listeners();

    }

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
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
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

    private void initialize() {
        switchCompat = findViewById(R.id.obd_switch);
        listView = findViewById(R.id.obd_list);
    }

    private void listeners() {
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

        deviceListAdapter = new DeviceListAdapter(this);
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice device = deviceListAdapter.getDevice(position);
            db.createObd(new Device(device.getName(), device.getAddress(), username));
            myAdapter.cancelDiscovery();
            startActivity(new Intent(this, SelectWearActivity.class));
        });
    }


}
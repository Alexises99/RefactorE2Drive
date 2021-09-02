package com.example.refactore2drive.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Device;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

public class ChangeObdActivity extends AppCompatActivity {
    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_obd);
        DatabaseHelper db = new DatabaseHelper(this);
        final String username = Helper.getUsername(this);
        ListView listView = findViewById(R.id.obd_select_list);
        deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        /*
            Representa la misma actividad para seleccionar ambos dispositivos,
            se elige según el "mode" que le llega
         */
        if (getIntent().getStringExtra("mode").equals("obd")) {
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                BluetoothDevice device = deviceListAdapter.getDevice(position);
                db.deleteObd(db.getObd(username).getId());
                db.createObd(new Device(device.getName(), device.getAddress(), username));
                finish();
            });
        }
        else if (getIntent().getStringExtra("mode").equals("wear")) {
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                BluetoothDevice device = deviceListAdapter.getDevice(position);
                db.deleteWear(db.getWear(username).getId());
                db.createWear(new Device(device.getName(), device.getAddress(), username));
                finish();
            });
        }
        db.closeDB();

        Toolbar toolbar = findViewById(R.id.obd_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Slidr.attach(this);

        BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myAdapter == null) return;
        //Se añaden los dispositivos a la lista
        for (BluetoothDevice device : myAdapter.getBondedDevices()) {
            deviceListAdapter.addDevice(device);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceListAdapter.clear();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
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
package com.example.refactore2drive.heart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Device;

import java.util.ArrayList;

public class SelectWearActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 8000;
    private SwitchCompat switchCompat;
    private boolean mScanning;
    private BluetoothAdapter myAdapter;
    private DeviceListAdapter deviceListAdapter;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_wear);
        myAdapter = BluetoothAdapter.getDefaultAdapter();
        db = new DatabaseHelper(this);
        username = Helper.getUsername(this);
        //Comprobación de permisos
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("La ubicación es necesaria para escanear dispositivos bluetooth",
                        ((dialogInterface, i) -> requestPermission()));
            } else {
                requestPermission();
            }
        }
        ListView listView = findViewById(R.id.wear_list);
        switchCompat = findViewById(R.id.wear_switch);
        //Escucha del switch para alternar entre escanear y listar
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            deviceListAdapter.clear();
            deviceListAdapter.notifyDataSetChanged();
            if (switchCompat.isChecked()) {
                scanLeDevice(true);
            } else {
                scanLeDevice(false);
                for (BluetoothDevice device : myAdapter.getBondedDevices()) {
                    deviceListAdapter.addDevice(device);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        });
        deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            BluetoothDevice device = deviceListAdapter.getDevice(i);
            db.createWear(new Device(device.getName(), device.getAddress(), username));
            //Al seleccionar un dispositivo paramos el escaneo para ahorrar recursos
            if (mScanning) {
                scanLeDevice(false);
            }
            startActivity(new Intent(this, MainActivity.class));

        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Aceptar", okListener)
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanLeDevice(true);
        try {
            startActivity(new Intent(this, MainActivity.class));
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("Error al recuperar wear", "error");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        deviceListAdapter.clear();
        db.closeDB();
        scanLeDevice(false);
    }

    /**
     * Indicamos si queremos escanear dispositivos o no
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            new Handler().postDelayed(() -> {
                switchCompat.setChecked(false);
                mScanning = false;
                myAdapter.stopLeScan(mLeScanCallback);
            }, 15000);
            if (!switchCompat.isChecked())switchCompat.setChecked(true);
            mScanning = true;
            myAdapter.startLeScan(mLeScanCallback);
        } else {
            switchCompat.setChecked(false);
            mScanning = false;
            myAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            deviceListAdapter.addDevice(device);
            deviceListAdapter.notifyDataSetChanged();
        }
    };

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
            // General ListView optimization code.
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
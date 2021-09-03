package com.example.refactore2drive.heart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import com.example.refactore2drive.DeviceListAdapter;
import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Device;

public class SelectWearActivity extends AppCompatActivity {
    private SwitchCompat switchCompat;
    private ListView listView;
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
        checkPermissions();
        deviceListAdapter = new DeviceListAdapter(this);
        initialize();
        listeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanLeDevice(true);
        try {
            db.getWear(username);
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

    private void initialize() {
        listView = findViewById(R.id.wear_list);
        switchCompat = findViewById(R.id.wear_switch);
    }

    private void listeners() {
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

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Helper.showMessageOKCancel(this,
                        "La ubicación es necesaria para escanear dispositivos bluetooth",
                        ((dialogInterface, i) -> Helper.requestPermission(this, Helper.REQUEST_LOCATION)));
            } else {
                Helper.requestPermission(this, Helper.REQUEST_LOCATION);
            }
        }
    }

    /**
     * Indicamos si queremos escanear dispositivos o no
     * @param enable habilitar o deshabilitar el escaneo
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
}
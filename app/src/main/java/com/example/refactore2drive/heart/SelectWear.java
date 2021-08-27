package com.example.refactore2drive.heart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.NavigationHost;
import com.example.refactore2drive.R;
import com.example.refactore2drive.controlpanel.InfoGridFragment;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Device;

import java.util.ArrayList;

public class SelectWear extends Fragment {
    private SwitchCompat switchCompat;
    private boolean mScanning;
    private BluetoothAdapter myAdapter;
    private DeviceListAdapter deviceListAdapter;
    private  ListView listView;
    private DatabaseHelper db;
    private String username;
    public static final int REQUEST_LOCATION = 1000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("La ubicación es necesaria para escanear dispositivos bluetooth",
                        ((dialogInterface, i) -> requestPermission()));
            } else {
                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(requireActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_wear, container, false);
        listView = view.findViewById(R.id.wear_list);
        switchCompat = view.findViewById(R.id.wear_switch);
        deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            BluetoothDevice device = deviceListAdapter.getDevice(i);
            db.createWear(new Device(device.getName(), device.getAddress(), username));
            if (mScanning) {
                myAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            ((NavigationHost) getActivity()).navigateTo(InfoGridFragment.newInstance(device.getAddress()), false);

            getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        scanLeDevice(true);
        try {
            Device device = db.getWear(username);
            ((NavigationHost) getActivity()).navigateTo(InfoGridFragment.newInstance(device.getAddress()), false);
            getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("Error al recuperar wear", "error");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        deviceListAdapter.clear();
        db.closeDB();
        scanLeDevice(false);
    }

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

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            deviceListAdapter.addDevice(device);
            deviceListAdapter.notifyDataSetChanged();
        }
    };

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mDevices;
        private LayoutInflater mInflator;

        public DeviceListAdapter() {
            super();
            mDevices = new ArrayList<>();
            mInflator = SelectWear.this.getLayoutInflater();
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
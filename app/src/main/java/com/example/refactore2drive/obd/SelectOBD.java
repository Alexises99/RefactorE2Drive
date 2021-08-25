package com.example.refactore2drive.obd;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

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
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.SelectWear;
import com.example.refactore2drive.models.Device;

import java.util.ArrayList;

public class SelectOBD extends Fragment {
    private SwitchCompat switchCompat;
    private ListView listView;
    private DeviceListAdapter deviceListAdapter;
    private String username;
    private DatabaseHelper db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_o_b_d, container, false);
        switchCompat = view.findViewById(R.id.obd_switch);
        listView = view.findViewById(R.id.obd_list);
        deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice device = deviceListAdapter.getDevice(position);
            if (!username.equals("No encontrado")) {
                db.createObd(new Device(device.getName(), device.getAddress(), username));
            }
            ((NavigationHost) getActivity()).navigateTo(new SelectWear(), false);
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            ((NavigationHost) getActivity()).navigateTo(new SelectWear(), false);
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("dispositivo no guardado", "no guardado");
        }
        if (myAdapter != null) {
            for (BluetoothDevice device : myAdapter.getBondedDevices()) {
                deviceListAdapter.addDevice(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        deviceListAdapter.clear();
        db.closeDB();
    }

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mDevices;
        private LayoutInflater mInflator;

        public DeviceListAdapter() {
            super();
            mDevices = new ArrayList<>();
            mInflator = SelectOBD.this.getLayoutInflater();
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
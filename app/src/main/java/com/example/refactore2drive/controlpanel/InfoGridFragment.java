package com.example.refactore2drive.controlpanel;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;
import com.example.refactore2drive.chart.Value;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.BluetoothLeService;
import com.example.refactore2drive.heart.SelectWear;
import com.example.refactore2drive.obd.OBDConsumer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class InfoGridFragment extends Fragment {
    private LocalBroadcastManager bm;
    private List<InfoEntry> infoEntryList;
    InfoCardRecyclerViewAdapter adapter;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private DatabaseHelper db;

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    public static InfoGridFragment newInstance(String param1) {
        InfoGridFragment fragment = new InfoGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            Log.d("PARAM", mParam1);
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        db = new DatabaseHelper(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bm.unregisterReceiver(onDataReceived);
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        bm = LocalBroadcastManager.getInstance(context);
        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter dataReceiver = new IntentFilter();
        IntentFilter heartReceiver = new IntentFilter();
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_TEMP);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_SPEED);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_FUEL);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_CONSUME);
        heartReceiver.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        heartReceiver.addAction(BluetoothLeService.EXTRA_DATA);
        bm.registerReceiver(onDataReceived, dataReceiver);
        bm.registerReceiver(mGattUpdateReceiver, heartReceiver);
    }
    //OBD
    private BroadcastReceiver onDataReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent !=  null) {
                final String action = intent.getAction();
                String data;
                switch (action) {
                    case OBDConsumer.ACTION_SEND_DATA_CONSUME:
                        data = intent.getStringExtra("dataConsume");
                        updateSingleItem(data, 3);
                        long res = 0;
                        if (data.length() == 6) res = (long) Float.parseFloat(data.substring(0,3).replace(",","."));
                        else if (data.length() == 7) res = (long) Float.parseFloat(data.substring(0,4).replace(",","."));
                        if (MainActivity.prevConsume == -1){
                            MainActivity.prevConsume = res;
                            updateSingleItem(data, 3);
                            int x = Helper.formatTime(LocalTime.now());
                            Value value = new Value((long) x,(long) res, "alex", LocalDate.now().toString());
                            db.createDataConsume(value);
                        }
                        else if (MainActivity.prevConsume != res) {
                            MainActivity.prevConsume = res;
                            int x = Helper.formatTime(LocalTime.now());
                            Value value = new Value((long) x,(long) res, "alex", LocalDate.now().toString());
                            db.createDataConsume(value);
                        }
                        break;
                    case OBDConsumer.ACTION_SEND_DATA_TEMP:
                        data = intent.getStringExtra("dataTemp");
                        updateSingleItem(data, 1);
                        break;
                    case OBDConsumer.ACTION_SEND_DATA_SPEED:
                        data = intent.getStringExtra("dataSpeed");
                        updateSingleItem(data, 0);
                        long res1 = 0;
                        if (data.length() == 7) res1 = Long.parseLong(data.substring(0,3));
                        else if (data.length() == 6) res1 = Long.parseLong(data.substring(0,2));
                        else if (data.length() == 5) res1 = Long.parseLong(data.substring(0,1));
                        if (MainActivity.prevSpeed == -1) {
                            MainActivity.prevSpeed = res1;
                            Log.d("Valor", "val"+MainActivity.prevSpeed);
                            int x1 = Helper.formatTime(LocalTime.now());
                            Value value1 = new Value((long) x1, (long) res1, "alex", LocalDate.now().toString());
                            db.createDataSpeed(value1);
                        }
                        else if (MainActivity.prevSpeed != res1) {
                            MainActivity.prevSpeed = res1;
                            int x1 = Helper.formatTime(LocalTime.now());
                            Value value1 = new Value((long) x1, (long) res1, "alex", LocalDate.now().toString());
                            Log.d("Valor", "valor: " + value1.getY());
                            db.createDataSpeed(value1);
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
    }

    private void updateSingleItem(String data, int pos) {
        InfoEntry entry= infoEntryList.get(pos);
        entry.value = data;
        adapter.notifyDataSetChanged();
    }

    //HEART
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("LE", "No se puede inicializar el servicio LE");
            }
            mBluetoothLeService.connect(mParam1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        String uuid;
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.equals("0000180d-0000-1000-8000-00805f9b34fb")) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                }
                return;
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("HEART", "conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("HEART", "desconectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d("DATA CORAZON", data);
                updateSingleItem(data, 2);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info_grid, container, false);
        setUpToolbar(view);
        if (mBluetoothLeService != null){
            final boolean result = mBluetoothLeService.connect(mParam1);
            Log.d("Result", String.valueOf(result));
        }
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        infoEntryList = InfoEntry.initList();
        adapter = new InfoCardRecyclerViewAdapter(infoEntryList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new InfoGridItemDecoration(8,4));
        return view;
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null)
            Log.d("HOla", "hola");
            activity.setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
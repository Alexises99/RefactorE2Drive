package com.example.refactore2drive.controlpanel;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;
import com.example.refactore2drive.chart.Value;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.BluetoothLeService;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.example.refactore2drive.obd.OBDConsumer;
import com.example.refactore2drive.sessions.SessionFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class InfoGridFragment extends Fragment {
    private LocalBroadcastManager bm;
    private List<InfoEntry> infoEntryList;
    InfoCardRecyclerViewAdapter adapter;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private DatabaseHelper db;
    private TextView statusText;
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private String username;
    private boolean sessionStarted;

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
        Log.d("Holis", "aki etoy");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            Log.d("PARAM", mParam1);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_grid, container, false);
        setUpToolbar(view);
        if (mBluetoothLeService != null){
            final boolean result = mBluetoothLeService.connect(mParam1);
            Log.d("Result", String.valueOf(result));
        }
        statusText = view.findViewById(R.id.status_obd);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        infoEntryList = InfoEntry.initList();
        adapter = new InfoCardRecyclerViewAdapter(infoEntryList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new InfoGridItemDecoration(8,4));
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("BAck", "estoy en el back");
        try {
            getActivity().unbindService(mServiceConnection);
        }catch (NullPointerException e) {
            Log.e("error", "error no esperado");
        } finally {
            if (!sessionStarted && !BluetoothServiceOBD.isRunning) getActivity().stopService(new Intent(getActivity(), BluetoothServiceOBD.class));
            bm.unregisterReceiver(onDataReceived);
            bm.unregisterReceiver(onStatusReceiver);
            bm.unregisterReceiver(mGattUpdateReceiver);
            db.closeDB();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("holaa", "aka andi");
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());
        bm = LocalBroadcastManager.getInstance(getActivity());
        Log.d("Conectando", "Conectandi");
        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);

        try{
            getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (NullPointerException e) {
            Log.e("error", "error inesperado");
        }

        IntentFilter dataReceiver = startFilterObd();
        IntentFilter heartReceiver = startFilterHearth();
        IntentFilter status = startFilterStatus();
        bm.registerReceiver(onStatusReceiver,status);
        bm.registerReceiver(onDataReceived, dataReceiver);
        bm.registerReceiver(mGattUpdateReceiver, heartReceiver);
        if (!BluetoothServiceOBD.isRunning) {
            Intent intent = new Intent(getActivity(), BluetoothServiceOBD.class);
            intent.putExtra("deviceAddress",db.getObd(username).getAddress());
            getActivity().startService(intent);
        }
    }

    private IntentFilter startFilterObd() {
        IntentFilter dataReceiver = new IntentFilter();
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_TEMP);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_SPEED);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_FUEL);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_CONSUME);
        return dataReceiver;
    }

    private IntentFilter startFilterHearth() {
        IntentFilter heartReceiver = new IntentFilter();
        heartReceiver.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        heartReceiver.addAction(BluetoothLeService.EXTRA_DATA);
        return heartReceiver;
    }

    private IntentFilter startFilterStatus() {
        IntentFilter status = new IntentFilter();
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_FAILED);
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_CONNECTED);
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_CONNECTING);
        status.addAction(OBDConsumer.ACTION_DISCONNECTED);
        status.addAction(SessionFragment.ACTION_SESSION_START);
        status.addAction(SessionFragment.ACTION_SESSION_END);
        return status;
    }

    //OBD
    private final BroadcastReceiver onStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                switch (action) {
                    case OBDConsumer.ACTION_DISCONNECTED:
                        statusText.setText("Desconectado");
                        getActivity().stopService(new Intent(getActivity(), BluetoothServiceOBD.class));
                        break;
                    case BluetoothServiceOBD.ACTION_CONNECTION_CONNECTED:
                        statusText.setText("Conectado");
                        break;
                    case BluetoothServiceOBD.ACTION_CONNECTION_CONNECTING:
                        statusText.setText("Conectando");
                        break;
                    case BluetoothServiceOBD.ACTION_CONNECTION_FAILED:
                        statusText.setText("Conexión Fallida");
                        break;
                    case SessionFragment.ACTION_SESSION_START:
                        sessionStarted = true;
                        break;
                    case SessionFragment.ACTION_SESSION_END:
                        sessionStarted = false;
                        break;
                }
            }
        }
    };
    private final BroadcastReceiver onDataReceived = new BroadcastReceiver() {
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

                        if (MainActivity.prevConsume == -1) createDataConsume(res);
                        else if (MainActivity.prevConsume != res) createDataConsume(res);
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

                        if (MainActivity.prevSpeed == -1) createDataSpeed(res1);
                        else if (MainActivity.prevSpeed != res1) createDataSpeed(res1);
                        break;
                }
            }
        }
    };

    private void createDataSpeed(long res1) {
        MainActivity.prevSpeed = res1;
        int x1 = Helper.formatTime(LocalTime.now());
        Value value1 = new Value((long) x1, (long) res1, username, LocalDate.now().toString());
        db.createDataSpeed(value1);
    }

    private void createDataConsume(long res) {
        MainActivity.prevConsume = res;
        int x = Helper.formatTime(LocalTime.now());
        Value value = new Value((long) x,(long) res, username, LocalDate.now().toString());
        db.createDataConsume(value);
    }

    private void updateSingleItem(String data, int pos) {
        InfoEntry entry= infoEntryList.get(pos);
        entry.value = data;
        adapter.notifyItemChanged(pos);
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



    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null)
            Log.d("HOla", "hola");
        try{
            activity.setSupportActionBar(toolbar);
        } catch (NullPointerException e) {
            Log.e("Error", "error inesperado en la barra");
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
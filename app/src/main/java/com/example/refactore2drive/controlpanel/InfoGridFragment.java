package com.example.refactore2drive.controlpanel;

import android.app.ActivityManager;
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
import androidx.annotation.Nullable;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MainActivity;
import com.example.refactore2drive.R;
import com.example.refactore2drive.activities.MoreInfoActivity;
import com.example.refactore2drive.activities.UserConfigActivity;
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

    /**
     * Para poder instanciar con un parametro
     * @param param1 valor de la direccion MAC de la pulsera
     * @return Fragmento
     */
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
        setHasOptionsMenu(true);
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
            //Conexión con la pulsera
            final boolean result = mBluetoothLeService.connect(mParam1);
            Log.d("Result", String.valueOf(result));
        }

        //Inicialización de la vista
        statusText = view.findViewById(R.id.status_obd);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        //Configuración del recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        //Inicialización de la lista
        infoEntryList = InfoEntry.initList();
        adapter = new InfoCardRecyclerViewAdapter(infoEntryList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new InfoGridItemDecoration(8,4));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        /*
        Cada vez que el fragmento no es visible liberamos recursos
         */
        Log.d("BAck", "estoy en el back");
        try {
            requireActivity().unbindService(mServiceConnection);
        }catch (NullPointerException e) {
            Log.e("error", "error no esperado");
        } finally {
            boolean isRunning = ((MainActivity) requireActivity()).isMyServiceRunning(BluetoothServiceOBD.class);
            Log.d("El estado al parar", "" + isRunning);
            Log.d("Valor sesion" , "" + MainActivity.sessionStarted);
            if (!MainActivity.sessionStarted && isRunning) requireActivity().stopService(new Intent(getActivity(), BluetoothServiceOBD.class));
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

        //Obtenemos la base de datos y el usuario
        db = new DatabaseHelper(getActivity());
        username = Helper.getUsername(getActivity());

        //Obtenemos el BroadcastManager para controlar la recepción de mensajes
        bm = LocalBroadcastManager.getInstance(requireActivity());

        //Conexión con el servicio de la pulsera
        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
        try{
            requireActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (NullPointerException e) {
            Log.e("error", "error inesperado");
        }

        //Iniciación de los filtros que manejaran los intents
        IntentFilter dataReceiver = startFilterObd();
        IntentFilter heartReceiver = startFilterHearth();
        IntentFilter status = startFilterStatus();

        //Registro de estos en el BroadcastManager
        bm.registerReceiver(onStatusReceiver,status);
        bm.registerReceiver(onDataReceived, dataReceiver);
        bm.registerReceiver(mGattUpdateReceiver, heartReceiver);

        //Comprobación de que el servicio no se este ejecutando ahora mismo y si no es así lanzarlo
        boolean isRunning = ((MainActivity) requireActivity()).isMyServiceRunning(BluetoothServiceOBD.class);
        Log.d("El servicio esta en ejecucion ", "" + isRunning);
        if (!isRunning) {
            Intent intent = new Intent(getActivity(), BluetoothServiceOBD.class);
            intent.putExtra("mode", false);
            intent.putExtra("deviceAddress",db.getObd(username).getAddress());
            requireActivity().startService(intent);
            boolean isRunning1 = ((MainActivity) requireActivity()).isMyServiceRunning(BluetoothServiceOBD.class);
            Log.d("Ahora", "" + isRunning1);
        }
    }

    /**
     * Este filtro se encarga de recibir los datos de la clase OBDConsumer y escuchar sus acciones
     * @return El filtro inicializado
     */
    private IntentFilter startFilterObd() {
        IntentFilter dataReceiver = new IntentFilter();
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_TEMP);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_SPEED);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_FUEL);
        dataReceiver.addAction(OBDConsumer.ACTION_SEND_DATA_CONSUME);
        return dataReceiver;
    }

    /**
     * Este filtro se encarga de recibir los datos del servicio que controla la pulsera y escuchar
     * sus acciones
     * @return El filtro inicializado
     */
    private IntentFilter startFilterHearth() {
        IntentFilter heartReceiver = new IntentFilter();
        heartReceiver.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        heartReceiver.addAction(BluetoothLeService.EXTRA_DATA);
        return heartReceiver;
    }

    /**
     * Este filtro se encarga de recibir las actualizaciones del estado de conexión del OBD
     * @return El filtro inicializado
     */
    private IntentFilter startFilterStatus() {
        IntentFilter status = new IntentFilter();
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_FAILED);
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_CONNECTED);
        status.addAction(BluetoothServiceOBD.ACTION_CONNECTION_CONNECTING);
        status.addAction(OBDConsumer.ACTION_DISCONNECTED);
        return status;
    }

    //OBD
    /**
     * Maneja todos los estados de conexión del obd y cuando se inicia una sesión o no
     * IMPORTANTE: Controlar aqui que se haya iniciado una sesión nos sirve para controlar que no
     * detengamos el servicio en ningún momento mientras la sesión este activa
     */
    private final BroadcastReceiver onStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                switch (action) {
                    case OBDConsumer.ACTION_DISCONNECTED:
                        statusText.setText("Desconectado");
                        requireActivity().stopService(new Intent(requireActivity(), BluetoothServiceOBD.class));
                        try {
                            Thread.sleep(1000);
                            requireActivity().startService(new Intent(requireActivity(), BluetoothServiceOBD.class)
                                    .putExtra("mode", false)
                                    .putExtra("deviceAddress", db.getObd(username).getAddress())
                            );
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        if (!BluetoothServiceOBD.isRunning) Toast.makeText(
                                requireActivity(),
                                "Error reconectando con OBD",
                                Toast.LENGTH_SHORT
                            ).show();
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
                }
            }
        }
    };

    /**
     * Maneja todas las acciones que envia el OBDConsumer
     */
    private final BroadcastReceiver onDataReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent !=  null) {
                final String action = intent.getAction();
                String data;
                switch (action) {
                    case OBDConsumer.ACTION_SEND_DATA_CONSUME:
                        //Cogemos el dato
                        data = intent.getStringExtra("dataConsume");

                        //Actualizamos el dato en la UI
                        updateSingleItem(data, 3);
                        long res = 0;

                        //Formateamos el valor para eliminar la unidad y a la vez cambiar la , por el .
                        if (data.length() == 6) res = (long) Float.parseFloat(data.substring(0,3).replace(",","."));
                        else if (data.length() == 7) res = (long) Float.parseFloat(data.substring(0,4).replace(",","."));

                        //Comprobamos que el valor no haya sido el mismo que el anterior
                        /*
                        El valor anterior esta guardado en la actividad principal ya que si lo mantenemos
                        en el Fragmento cada vez que salgamos de el se eliminara y no sera valido.
                        La actividad principal casi siempre esta en ejecución.
                         */
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

    /**
     * Creamos el valor para un número dado
     * @param res1 dato de la velocidad
     */
    private void createDataSpeed(long res1) {
        MainActivity.prevSpeed = res1;
        int x1 = Helper.formatTime(LocalTime.now());
        Value value1 = new Value((long) x1, (long) res1, username, LocalDate.now().toString());
        db.createDataSpeed(value1);
    }

    /**
     * Creamos el valor para un número dado
     * @param res dato del consumo
     */
    private void createDataConsume(long res) {
        MainActivity.prevConsume = res;
        int x = Helper.formatTime(LocalTime.now());
        Value value = new Value((long) x,(long) res, username, LocalDate.now().toString());
        db.createDataConsume(value);
    }

    /**
     * Actualiza el elemento en la UI
     * @param data datos a actualizar
     * @param pos posición en la lista
     */
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
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more_info:
                startActivity(new Intent(requireActivity(), MoreInfoActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(requireActivity(), UserConfigActivity.class));
                return true;
            default:
                return false;
        }
    }
}
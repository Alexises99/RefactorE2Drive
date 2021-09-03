package com.example.refactore2drive.sessions;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.refactore2drive.heart.BluetoothLeService;
import com.example.refactore2drive.obd.OBDConsumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TransferDataService extends Service {
    private static final String TAG = TransferDataService.class.getName();
    private Session odbSession;
    private Session heartSession;

    //Recibe los datos del pulso y obd y los escribe
    private final BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("INTENT", "accion: " + action);
            if (action.equals(OBDConsumer.ACTION_SEND_DATA_OBD_SESSION)) {
                Log.d("Extras",intent.getExtras().toString());
                ArrayList<String> list = intent.getStringArrayListExtra("lista");
                Log.d("Array", "data: " + list.toString());
                if (odbSession != null) {
                    String[] def = new String[list.size()];
                    Log.d(TAG, "La longuitud del string es: " + def.length);
                    odbSession.writeData(list.toArray(def));
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("INTENT", "accion: " + action);
            if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d("PULSO RECIBIDO", "pulso: " + data);
                String[] def = new String[2];
                def[0] = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                def[1] = data;
                if (heartSession != null) heartSession.writeData(def);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(myBroadCastReceiver,initIntent());
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            try{
                //TODO arreglar sesiones
                //Comienza las sesiones de ambos
                odbSession = new Session(intent.getStringExtra("name"), intent.getStringArrayExtra("comment"),getApplicationContext().getFilesDir().getPath(),Headers.headersUnit,intent.getStringArrayExtra("data"));
                String heart = intent.getStringExtra("name") + "Heart";
                heartSession = new Session(heart, intent.getStringArrayExtra("comment"),getApplicationContext().getFilesDir().getPath(),Headers.headersHeart,intent.getStringArrayExtra("data"));
            } catch (Session.ErrorSDException error) {
                Log.d("ERRORSD", "SD NO INSERTADA");
                close();
                this.stopSelf();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(myBroadCastReceiver);
        unregisterReceiver(broadcastReceiver);
        close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Registra las escuchas de ambos
     * @return un intentfilter con las acciones a escuchar
     */
    private IntentFilter initIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OBDConsumer.ACTION_SEND_DATA_OBD_SESSION);
        return intentFilter;
    }

    public void close() {
        if (odbSession ==  null) return;
        odbSession.close();
        heartSession.close();
        odbSession = null;
        heartSession = null;
    }
}
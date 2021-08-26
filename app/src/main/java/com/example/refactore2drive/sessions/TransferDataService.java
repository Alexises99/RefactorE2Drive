package com.example.refactore2drive.sessions;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.refactore2drive.obd.OBDConsumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TransferDataService extends Service {
    private static final String TAG = TransferDataService.class.getName();
    private Session odbSession;
    //private Session heartSession;

    private final BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("INTENT", "accion: " + action);
            if (action.equals(OBDConsumer.ACTION_SEND_DATA_OBD_SESSION)) {
                Log.d("Extras",intent.getExtras().toString());
                //TODO No funciona
                ArrayList<String> list = intent.getStringArrayListExtra("lista");
                Log.d("Array", "data: " + list.toString());
                if (odbSession != null) {
                    String[] def = new String[list.size()];
                    Log.d(TAG, "La longuitud del string es: " + def.length);
                    odbSession.writeData(list.toArray(def));
                }   //TODO esta mal la accion del elseif
            } else if (action.equals(OBDConsumer.ACTION_SEND_DATA_CONSUME)) {
                String data = intent.getStringExtra("data");
                Log.d("PULSO RECIBIDO", "pulso: " + data);
                String[] def = new String[2];
                def[0] = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                def[1] = data;
                //if (heartSession != null) heartSession.writeData(def);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(myBroadCastReceiver,initIntent());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            try{
                odbSession = new Session(intent.getStringExtra("name"), intent.getStringArrayExtra("comment"),getApplicationContext().getFilesDir().getPath(),Headers.headersUnit,intent.getStringArrayExtra("data"));
                //heartSession = new Session(intent.getStringExtra("name") + "Heart", intent.getStringArrayExtra("comment"),getApplicationContext().getFilesDir().getPath(),Headers.headersHeart,intent.getStringArrayExtra("data"));
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
        close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private IntentFilter initIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OBDConsumer.ACTION_SEND_DATA_OBD_SESSION);
        //intentFilter.addAction(MainActivity.ACTION_SEND_DATA_HEART);
        return intentFilter;
    }

    public void close() {
        if (odbSession ==  null) return;
        odbSession.close();
        //heartSession.close();
        odbSession = null;
        //heartSession = null;
    }
}
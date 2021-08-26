package com.example.refactore2drive.obd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.sessions.Headers;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class OBDConsumer implements Runnable{
    private BlockingQueue<OBDCommandJob> queue;
    private boolean isConnected;
    private Context context;
    public final static String ACTION_SEND_DATA_OBD_SESSION = "com.example_ACTION_SEND_DATA_OBD_SESSION";
    public final static String ACTION_SEND_DATA_TEMP="ACTION_OBD_DATA_TEMP";
    public final static String ACTION_SEND_DATA_SPEED="ACTION_OBD_DATA_SPEED";
    public final static String ACTION_SEND_DATA_CONSUME="ACTION_OBD_DATA_CONSUME";
    public final static String ACTION_SEND_DATA_FUEL="ACTION_OBD_DATA_FUEL";
    public final static String ACTION_DISCONNECTED="com.example_ACTION_DISCONNECTED";
    private final static String TAG = OBDConsumer.class.getName();
    private HashMap<String, String> commandResults;
    private HashMap<String, String> filter;
    public ArrayList<String> collectedData;
    private boolean mode;

    /**
     * Consumidor de comandos de la cola
     * @param queue cola donde extrae los comandos
     * @param context contexto de ejecución de la actividad
     * @param mode true si esta una sesión iniciada, false sino
     */
    public OBDConsumer(BlockingQueue<OBDCommandJob> queue,Context context, boolean mode) {
        this.queue = queue;
        isConnected = true;
        this.context = context;
        commandResults = new HashMap<>();
        filter = Helper.initFilter();
        collectedData = new ArrayList<>();
        this.mode = mode;
    }

    @Override
    public void run() {
        /*
        Mantenemos un bucle infinito, mientras no se interrumpa el hilo extraemos trabajos,
        comprobamos el estado y si es correcto mandamos a otro metodo que se encargara de actua
        lizar en la UI
         */
        while(!Thread.currentThread().isInterrupted()) {
            OBDCommandJob job = null;
            try {
                //Obtenemos el trabajo de la cola
                job = queue.take();
                //Comprobamos el estado
                if (job.getState().equals(OBDCommandJob.ObdCommandJobState.NEW)) {
                    job.setState(OBDCommandJob.ObdCommandJobState.RUNNING);
                    if (isConnected)
                        job.getCommand().run(BluetoothServiceOBD.inputStream, BluetoothServiceOBD.outputStream);
                    else {
                        job.setState(OBDCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                        Log.e(TAG, "No se puede ejecutar en un socket cerrado, " + job.getId());
                    }
                } else Log.e(TAG, "Error el comando no es nuevo, " + job.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e2) {
                if (job != null) {
                    try {
                        if (e2.getMessage().contains("Broken pipe")) {
                            job.setState(OBDCommandJob.ObdCommandJobState.BROKEN_PIPE);
                            Intent intent = new Intent(ACTION_DISCONNECTED);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            Thread.currentThread().interrupt();
                        }
                        else job.setState(OBDCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Error muy muy raro");
                    }
                }
            } catch (UnsupportedCommandException e3) {
                if (job != null) {
                    job.setState(OBDCommandJob.ObdCommandJobState.NOT_SUPPORTED);
                    Log.e(TAG, "Comando no soportado, " + job.getId());
                }
            } catch (Exception e) {
                if (job != null) job.setState(OBDCommandJob.ObdCommandJobState.EXECUTION_ERROR);
            }
            final OBDCommandJob job2 = job;
            stateUpdate(job2);
        }
    }

    /**
     * Notifica al InfoFragment de los cambios para reflejarlos en la UI
     * @param data datos a actualizar
     * @param action accion a enviar
     * @param param nombre del parametro a enviar
     */
    private void notifyFragment(final String data, final String action, final String param) {
        Intent intent = new Intent(action);
        Bundle bundle = new Bundle();
        bundle.putString(param,data);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Busca si el comando es valido entre todos los existentes
     * @param txt nombre del comano
     * @return nombre del comando en caso de que exista, si no el txt
     */
    public static String LookUpCommand(String txt) {
        for (AvailableCommandNames item : AvailableCommandNames.values()) {
            if (item.getValue().equals(txt)) {
                return item.name();
            }
        }
        return txt;
    }

    /**
     * Comprobamos que sea correcto el job y lo procesamos
     * @param job
     */
    public void stateUpdate(final OBDCommandJob job) {
        if (job == null) return;
        final String cmdName = job.getCommand().getName();
        String cmdResult = "";

        final String cmdID = LookUpCommand(cmdName);

        if (job.getState().equals(OBDCommandJob.ObdCommandJobState.EXECUTION_ERROR)) {
            cmdResult = job.getCommand().getResult();
        } else if (job.getState().equals(OBDCommandJob.ObdCommandJobState.BROKEN_PIPE)) {
            Log.e(TAG, "ERRORR BROKEN PIPE");
            Intent intent = new Intent(ACTION_DISCONNECTED);
            BluetoothServiceOBD.isRunning = false;
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Thread.currentThread().interrupt();
        } else if (job.getState().equals(OBDCommandJob.ObdCommandJobState.NOT_SUPPORTED)) {
            cmdResult = "Comando no soportado";
        } else {
            cmdResult = job.getCommand().getFormattedResult();
        }
        //Comprobamos el comando del que se trata y lo notificamos
        switch (cmdID) {
            case "AMBIENT_AIR_TEMP":
                notifyFragment(cmdResult,ACTION_SEND_DATA_TEMP,"dataTemp");
                break;
            case "FUEL_LEVEL":
                notifyFragment(cmdResult, ACTION_SEND_DATA_FUEL, "dataFuel");
                break;
            case "SPEED":
                notifyFragment(cmdResult, ACTION_SEND_DATA_SPEED, "dataSpeed");
                break;
            case "FUEL_CONSUMPTION_RATE":
                notifyFragment(cmdResult, ACTION_SEND_DATA_CONSUME, "dataConsume");
                break;
        }
        //Actualizamos el HashMap con todos los valores
        /*
        TODO optimizar commandResults
        if (mode) ´{
            commandResults.put(cmdName, cmdResult);
            if ( commandResults.size() ... ) {
                ...
            }
         }
         ¿¿Es mas eficiente ya que no llenamos con algo que sin sesion da igual??
         */
        commandResults.put(cmdName, cmdResult);
        if (commandResults.size() >= Headers.headers.length && mode) {
            //Lanzamos un hilo que se encarga de procesar los datos y enviarlos al servicio de la sesión
            (new Thread() {
                @Override
                public void run() {
                    processCollectedData();
                }
            }).start();
        }
    }

    /**
     * Procesa todos los datos
     */
    private void processCollectedData() {
        //Crea un vector con la misma longuitud que las cabeceras + 1 que sera la del instante de tiempo
        String[] results = new String[Headers.headers.length + 1];
        //Asignamos el instante de tiempo
        results[0] = LocalDateTime.now().toString();
        int i = 1;
        //Recorremos todas las cabeceras, cogemos la unidad correspondiente y el valor del dato recogido del mismo tipo
        for (String header : Headers.headers) {
            String unit = filter.get(header);
            String value = commandResults.get(header);
            try {
                //formateamos el valor
                value = value.replace(unit, "");
                value = value.replace(",", ".");
                results[i] = value;
                i++;
            } catch (NullPointerException e) {
                //En caso de que no exista se introducen ""
                results[i] = "";
                i++;
            }
        }
        //Añadimos los elementos a la lista
        Arrays.stream(results).forEach(value -> collectedData.add(value));
        if(collectedData.size() > 10) {
            //Cada vez que estemos aqui enviaremos los datos recolectados al servicio
            Log.d("Colectado", " " + collectedData.size());
            Intent intent = new Intent(ACTION_SEND_DATA_OBD_SESSION);
            //Hay que hacer una copia de collectedData porque si no, no se envia xDDD no tiene sentido
            ArrayList<String> list = new ArrayList<>();
            for (String data : collectedData) {
                list.add(data);
            }
            intent.putExtra("lista", list);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            collectedData.clear();
        }
    }

    public class Data implements Serializable{
        String data;
        public Data(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }
}

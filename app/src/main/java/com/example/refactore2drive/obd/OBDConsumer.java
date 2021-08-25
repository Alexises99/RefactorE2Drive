package com.example.refactore2drive.obd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.refactore2drive.ToastUtils;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class OBDConsumer implements Runnable{
    private BlockingQueue<OBDCommandJob> queue;
    private boolean isConnected;
    private Context context;

    public final static String ACTION_SEND_DATA_TEMP="ACTION_OBD_DATA_TEMP";
    public final static String ACTION_SEND_DATA_SPEED="ACTION_OBD_DATA_SPEED";
    public final static String ACTION_SEND_DATA_CONSUME="ACTION_OBD_DATA_CONSUME";
    public final static String ACTION_SEND_DATA_FUEL="ACTION_OBD_DATA_FUEL";
    public final static String ACTION_DISCONNECTED="com.example_ACTION_DISCONNECTED";
    private final static String TAG = OBDConsumer.class.getName();

    public OBDConsumer(BlockingQueue<OBDCommandJob> queue,Context context) {
        this.queue = queue;
        isConnected = true;
        this.context = context;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            OBDCommandJob job = null;
            try {
                job = queue.take();
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

    private void notifyFragment(final String data, final String action, final String param) {
        Intent intent = new Intent(action);
        Bundle bundle = new Bundle();
        bundle.putString(param,data);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static String LookUpCommand(String txt) {
        for (AvailableCommandNames item : AvailableCommandNames.values()) {
            if (item.getValue().equals(txt)) {
                return item.name();
            }
        }
        return txt;
    }

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
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Thread.currentThread().interrupt();
        } else if (job.getState().equals(OBDCommandJob.ObdCommandJobState.NOT_SUPPORTED)) {
            cmdResult = "Comando no soportado";
        } else {
            cmdResult = job.getCommand().getFormattedResult();
        }
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
    }
}

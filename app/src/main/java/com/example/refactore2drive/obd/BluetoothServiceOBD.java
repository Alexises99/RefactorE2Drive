package com.example.refactore2drive.obd;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.refactore2drive.Helper;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.AbsoluteLoadCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.FuelTrimCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class BluetoothServiceOBD extends Service {
    public static final String TAG = BluetoothServiceOBD.class.getName();

    private static final UUID btUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final ArrayList<ObdCommand> obdCommands = new ArrayList<>();
    static InputStream inputStream;
    static OutputStream outputStream;
    private BlockingQueue<OBDCommandJob> jobsQueue = new LinkedBlockingDeque<>(500);
    private Thread producer;
    private ConnectedThread connectedThread;
    private ConnectingThread connectingThread;
    private Thread[] consumers = new Thread[2];
    @Override
    public void onCreate() {
        super.onCreate();
        myAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectedThread != null)
            connectedThread.closeStreams();
        if (connectingThread != null)
            connectingThread.closeSocket();
    }

    private BluetoothAdapter myAdapter;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio OBD levantado");
        String deviceName = intent.getStringExtra("deviceAddress");
        checkBt(deviceName);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Comprueba que bluetooth esta encendido y que el movil tenga el adaptador
     * @param deviceAddress direccion del dispositivo a conectar
     */
    private void checkBt(final String deviceAddress) {
        if (myAdapter ==  null) {
            Helper.makeToast(getApplicationContext(),"Bluetooth no soportado");
            stopSelf();
        } else {
            if (myAdapter.isEnabled()) {
                try {
                    BluetoothDevice myDevice = myAdapter.getRemoteDevice(deviceAddress);
                    connectingThread = new ConnectingThread(myDevice);
                    connectingThread.start();
                    Log.d("Conectando", "Conectando");
                } catch (IllegalArgumentException e) {
                    Helper.makeToast(getApplicationContext(), "Dirección MAC invalida");
                    stopSelf();
                }
            } else {
                Helper.makeToast(getApplicationContext(), "Bluetooth no esta encendido");
                stopSelf();
            }
        }
    }
    //TODO Poner acciones a los intents
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mySocket;

        public ConnectingThread(BluetoothDevice device) {
            BluetoothSocket temp = null;

            try {
                temp = device.createRfcommSocketToServiceRecord(btUuid);
            } catch (IOException e) {
                Helper.makeToast(getApplicationContext(), "Conexión con OBD fallida");
                Intent intent = new Intent();
                sendBroadcast(intent);
                stopSelf();
            }
            Intent intent = new Intent();
            sendBroadcast(intent);
            mySocket = temp;
        }

        @Override
        public void run() {
            super.run();
            myAdapter.cancelDiscovery();
            try {
                mySocket.connect();
                connectedThread = new ConnectedThread(mySocket);
                Log.d("Conectado", "Conectado");
            } catch (IOException e) {
                try {
                    mySocket.close();
                    Intent intent = new Intent();
                    sendBroadcast(intent);
                    Helper.makeToast(getApplicationContext(), "Conexión fallida con OBD");
                    stopSelf();
                } catch (IOException e2) {
                    Intent intent = new Intent();
                    sendBroadcast(intent);
                    Helper.makeToast(getApplicationContext(), "Conexión con OBD cerrada");
                    stopSelf();
                }
            }
        }

        public void closeSocket() {
            try {
                mySocket.close();
            } catch (IOException e) {
                Helper.makeToast(getApplicationContext(), "Conexión con el OBD finalizada");
                stopSelf();
            } finally {
                Intent intent = new Intent();
                sendBroadcast(intent);
                stopThreads();
            }
        }
    }
    private class ConnectedThread {
        private final InputStream myInputStream;
        private final OutputStream myOutputtream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d("Hay sockets", "hay sockets");
            } catch (IOException e) {
                Intent intent = new Intent();
                sendBroadcast(intent);
                Helper.makeToast(getApplicationContext(), "Error en la transferencia con el OBD");
                stopSelf();
            }
            myInputStream = tmpIn;
            myOutputtream = tmpOut;
            outputStream = tmpOut;
            inputStream = tmpIn;
            startThreads();
            Intent intent = new Intent();
            sendBroadcast(intent);
        }

        public void closeStreams() {
            try {
                myInputStream.close();
                myOutputtream.close();
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                stopSelf();
            } finally {
                Helper.makeToast(getApplicationContext(), "Cerrando conexión con el OBD");
                Intent intent = new Intent();
                sendBroadcast(intent);
                stopThreads();
            }
        }
    }

    private void initializeList() {
        obdCommands.add(new AbsoluteLoadCommand());
        obdCommands.add(new LoadCommand());
        obdCommands.add(new MassAirFlowCommand());
        obdCommands.add(new OilTempCommand());
        obdCommands.add(new RPMCommand());
        obdCommands.add(new RuntimeCommand());
        obdCommands.add(new ThrottlePositionCommand());
        obdCommands.add(new AirFuelRatioCommand());
        obdCommands.add(new ConsumptionRateCommand());
        obdCommands.add(new FindFuelTypeCommand());
        obdCommands.add(new FuelTrimCommand());
        obdCommands.add(new FuelLevelCommand());
        obdCommands.add(new WidebandAirFuelRatioCommand());
        obdCommands.add(new BarometricPressureCommand());
        obdCommands.add(new FuelPressureCommand());
        obdCommands.add(new FuelRailPressureCommand());
        obdCommands.add(new IntakeManifoldPressureCommand());
        obdCommands.add(new AirIntakeTemperatureCommand());
        obdCommands.add(new AmbientAirTemperatureCommand());
        obdCommands.add(new EngineCoolantTemperatureCommand());
        obdCommands.add(new SpeedCommand());
    }

    private void startThreads() {
        initializeList();
        producer = new Thread(new OBDProducer(jobsQueue,obdCommands));
        producer.start();
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Thread(new OBDConsumer(jobsQueue, getApplicationContext()));
        }

        for (Thread consumer: consumers) consumer.start();
        Log.d(TAG, "THREADS CREADOS");
    }

    private void stopThreads() {
        try {
            producer.interrupt();
            for (Thread consumer : consumers) consumer.interrupt();
        } catch (NullPointerException e) {
            Log.e(TAG, "Threads no parados");
        }
        Log.d(TAG,"Threads parados");
    }
}

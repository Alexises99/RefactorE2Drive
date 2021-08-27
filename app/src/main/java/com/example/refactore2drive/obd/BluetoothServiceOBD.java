package com.example.refactore2drive.obd;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.refactore2drive.ToastUtils;
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
    public static final String ACTION_CONNECTION_FAILED = "com.example_CONNECTION_FAILED";
    public static final String ACTION_CONNECTION_CONNECTING = "com.example_CONNECTION_CONNECTING";
    public static final String ACTION_CONNECTION_CONNECTED = "com.example_CONNECTION_CONNECTED";
    private static final UUID btUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final ArrayList<ObdCommand> obdCommands = new ArrayList<>();
    static InputStream inputStream;
    static OutputStream outputStream;
    private BlockingQueue<OBDCommandJob> jobsQueue = new LinkedBlockingDeque<>(500);
    private Thread producer;
    private ConnectedThread connectedThread;
    private ConnectingThread connectingThread;
    private Thread consumer;
    public static boolean isRunning;
    private boolean mode;
    private boolean modeDev;

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
        isRunning = true;
        //Recuperamos la dirección del dispositivo del intent
        String deviceName = intent.getStringExtra("deviceAddress");
        /*
        Si mode es true significa que el servicio lo ha lanzado el SessionFragment
        Si mode es false significa que el servicio lo ha lanzado el InfoGridFragment
        Esto nos va a permitir ahorrar recursos y no estar mandando datos a un servicio que a lo
        mejor no esta activo si no hay una sesión iniciada
         */
        mode = intent.getBooleanExtra("mode", false);
        modeDev = intent.getBooleanExtra("dev", false);
        //Actualización del estado del servicio
        checkBt(deviceName);
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * Comprueba que bluetooth esta encendido y que el movil tenga el adaptador
     * Si es así lanza el hilo que maneja el intento de conexión
     * @param deviceAddress direccion del dispositivo a conectar
     */
    private void checkBt(final String deviceAddress) {
        if (myAdapter ==  null) {
            ToastUtils.show(getApplicationContext(),"Bluetooth no soportado");
            stopSelf();
        } else {
            if (myAdapter.isEnabled()) {
                try {
                    BluetoothDevice myDevice = myAdapter.getRemoteDevice(deviceAddress);
                    connectingThread = new ConnectingThread(myDevice);
                    connectingThread.start();
                    Log.d("Conectando", "Conectando");
                } catch (IllegalArgumentException e) {
                    ToastUtils.show(getApplicationContext(), "Dirección MAC invalida");
                    isRunning = false;
                    stopSelf();
                }
            } else {
                ToastUtils.show(getApplicationContext(), "Bluetooth no esta encendido");
                isRunning = false;
                stopSelf();
            }
        }
    }

    private class ConnectingThread extends Thread {
        private final BluetoothSocket mySocket;

        /**
         * Hilo que maneja el intento de conexion
         * @param device dispositivo a conectar
         */
        public ConnectingThread(BluetoothDevice device) {
            BluetoothSocket temp = null;

            try {
                //Crea la conexión
                temp = device.createRfcommSocketToServiceRecord(btUuid);
            } catch (IOException e) {
                //En caso de fallo suspendemos y actualizamos los valores
                ToastUtils.show(getApplicationContext(), "Conexión con OBD fallida");
                Intent intent = new Intent(ACTION_CONNECTION_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                isRunning = false;
                stopSelf();
            }
            //Actualizamos el estado de la conexión
            Intent intent = new Intent(ACTION_CONNECTION_CONNECTING);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            isRunning = false;
            mySocket = temp;
        }

        @Override
        public void run() {
            super.run();
            //Cancelamos el descubrimiento de nuevos dispositivos por si acaso para ahorrar recursos
            myAdapter.cancelDiscovery();
            try {
                //Nos conectamos al dispositivo y pasamos al siguiente hilo que maneja el estado de conectado
                mySocket.connect();
                connectedThread = new ConnectedThread(mySocket);
                Log.d("Conectado", "Conectado");
            } catch (IOException e) {
                //En caso de error notificamos, suspendemos y actualizamos parametros
                try {
                    mySocket.close();
                    Intent intent = new Intent(ACTION_CONNECTION_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    ToastUtils.show(getApplicationContext(), "Conexión fallida con OBD");
                    isRunning = false;
                    stopSelf();
                } catch (IOException e2) {
                    Intent intent = new Intent(ACTION_CONNECTION_FAILED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    ToastUtils.show(getApplicationContext(), "Conexión con OBD cerrada");
                    isRunning = false;
                    stopSelf();
                }
            }
        }

        /**
         * Cerrar la conexión con el socket
         */
        public void closeSocket() {
            try {
                mySocket.close();
            } catch (IOException e) {
                ToastUtils.show(getApplicationContext(), "Conexión con el OBD finalizada");
                isRunning = false;
                stopSelf();
            } finally {
                Intent intent = new Intent();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                stopThreads();
            }
        }
    }

    private class ConnectedThread {

        /**
         * Clase que maneja el estado de conectado, anteriormente era un hilo que manejaba la
         * escritura y la lectura
         * @param socket socket a conectar
         */
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn;
            OutputStream tmpOut;

            try {
                //Asigna los streams para que puedan ser usados por el hilo consumidor
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                inputStream = tmpIn;
                outputStream = tmpOut;
                Log.d("Hay sockets", "hay sockets");
            } catch (IOException e) {
                Intent intent = new Intent(ACTION_CONNECTION_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                ToastUtils.show(getApplicationContext(), "Error en la transferencia con el OBD");
                closeStreams();
                isRunning = false;
                stopSelf();
            }
            //Lanzamos lo hilos a ejecución
            startThreads();
            //Actualizamos el estado
            Intent intent = new Intent(ACTION_CONNECTION_CONNECTED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        /**
         * Cierra todos los streams abiertos para ahorrar recursos
         */
        public void closeStreams() {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                isRunning = false;
                stopSelf();
            } finally {
                ToastUtils.show(getApplicationContext(), "Cerrando conexión con el OBD");
                Intent intent = new Intent(ACTION_CONNECTION_FAILED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                stopThreads();
            }
        }
    }

    /**
     * Lista de comandos soportados por el obd
     */
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

    /**
     * Lanza los hilos productor y consumidor a ejecución
     */
    private void startThreads() {
        initializeList();
        producer = new Thread(new OBDProducer(jobsQueue,obdCommands));
        producer.start();
        consumer = new Thread(new OBDConsumer(jobsQueue, getBaseContext(), mode, modeDev));
        consumer.start();
        Log.d(TAG, "THREADS CREADOS");
    }

    /**
     * Para a ambos hilos
     */
    private void stopThreads() {
        try {
            producer.interrupt();
            consumer.interrupt();
        } catch (NullPointerException e) {
            Log.e(TAG, "Threads no parados");
        }
        Log.d(TAG,"Threads parados");
    }
}

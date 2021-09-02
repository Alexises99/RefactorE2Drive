package com.example.refactore2drive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.refactore2drive.call.CallFragment;
import com.example.refactore2drive.chart.ChartFragment;
import com.example.refactore2drive.chart.Value;
import com.example.refactore2drive.controlpanel.InfoGridFragment;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.eyes.CameraSourcePreview;
import com.example.refactore2drive.eyes.FaceTracker;
import com.example.refactore2drive.eyes.GraphicOverlay;
import com.example.refactore2drive.heart.BluetoothLeService;
import com.example.refactore2drive.login.LoginFragment;
import com.example.refactore2drive.models.SessionModel;
import com.example.refactore2drive.sessions.SessionFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationHost{
    private static final String TAG = MainActivity.class.getName();
    public static long prevSpeed = -1;
    public static long prevConsume = -1;
    public static SessionModel sessionModel;
    public static boolean sessionStarted;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private final boolean mIsFrontFacing = true;
    private static final int RC_HANDLE_GMS =  9001;
    public static BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        mPreview.setVisibility(View.INVISIBLE);
        mGraphicOverlay.setVisibility(View.INVISIBLE);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        }
        nav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.page_1) {
                navigateTo(new InfoGridFragment(), false);
                return true;
            } else if (itemId == R.id.page_2) {
                navigateTo(new CallFragment(), false);
                return true;
            } else if (itemId == R.id.page_3) {
                navigateTo(new ChartFragment(), false);
                return true;
            } else if (itemId == R.id.page_4) {
                navigateTo(new SessionFragment(), false);
                return true;
            }
            return false;
        });
        if (savedInstanceState ==  null) {
            nav.setVisibility(View.INVISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
    }

    @NonNull
    private FaceDetector createFaceDetector(Context context) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(mIsFrontFacing)
                .setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
                .build();

        Detector.Processor<Face> processor;
        if (mIsFrontFacing) {
            // For front facing mode

            Tracker<Face> tracker = new FaceTracker(mGraphicOverlay, this);
            processor = new LargestFaceFocusingProcessor.Builder(detector, tracker).build();
        } else {
            // For rear facing mode, a factory is used to create per-face tracker instances.
            MultiProcessor.Factory<Face> factory = face -> new FaceTracker(mGraphicOverlay, this);
            processor = new MultiProcessor.Builder<>(factory).build();
        }

        detector.setProcessor(processor);

        if (!detector.isOperational()) {

            // isOperational() can be used to check if the required native library is currently available.  .
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Mas storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "MAS Almacenamiento");
            }
        }
        return detector;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Servicio conectado", "El servicio esta up HEART");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("LE", "No se puede inicializar el servicio LE");
            }
            mBluetoothLeService.connect(db.getWear(Helper.getUsername(MainActivity.this)).getAddress());
            mBluetoothLeService.connect(db.getWear(Helper.getUsername(MainActivity.this)).getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Servicio desconectado", "EL servicio esta down HEART");
            mBluetoothLeService = null;
        }
    };

    /**
     * Creates the face detector and the camera.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = CameraSource.CAMERA_FACING_FRONT;

        if (!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("ACCION", action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("HEART", "conectado");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("HEART", "desconectado");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(MainActivity.mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d("DATA CORAZON", data);
                //updateSingleItem(data, 2);
                int x = Helper.formatTime(LocalTime.now());
                Value value = new Value((long) x,(long) Long.parseLong(data), Helper.getUsername(MainActivity.this), LocalDate.now().toString());
                db.createDataHeart(value);
            }
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
                        MainActivity.mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    MainActivity.mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    MainActivity.mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startCameraSource();
        Log.d("MainActivity", "me resumi");
        IntentFilter heartReceiver = new IntentFilter();
        heartReceiver.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        heartReceiver.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        heartReceiver.addAction(BluetoothLeService.EXTRA_DATA);
        registerReceiver(mGattUpdateReceiver, heartReceiver);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "me escondi");
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        super.onPause();
    }

    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            assert dlg != null;
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void notifySound() {
        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +getApplicationContext().getPackageName()+"/"+R.raw.alarm);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), sound);
        ringtone.play();
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
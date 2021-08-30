package com.example.refactore2drive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.refactore2drive.call.CallFragment;
import com.example.refactore2drive.chart.ChartFragment;
import com.example.refactore2drive.controlpanel.InfoGridFragment;
import com.example.refactore2drive.eyes.CameraSourcePreview;
import com.example.refactore2drive.eyes.FaceTracker;
import com.example.refactore2drive.eyes.GraphicOverlay;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
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
package com.example.refactore2drive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.example.refactore2drive.call.CallFragment;
import com.example.refactore2drive.chart.ChartFragment;
import com.example.refactore2drive.controlpanel.InfoGridFragment;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.heart.BluetoothLeService;
import com.example.refactore2drive.login.LoginFragment;
import com.example.refactore2drive.models.Person;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.example.refactore2drive.sessions.SessionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements NavigationHost{
    public static long prevSpeed = -1;
    public static long prevConsume = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.page_1:
                    navigateTo(new InfoGridFragment(),false);
                    return true;
                case R.id.page_2:
                    navigateTo(new CallFragment(), false);
                    return true;
                case R.id.page_3:
                    navigateTo(new ChartFragment(), false);
                    return true;
                case R.id.page_4:
                    navigateTo(new SessionFragment(), false);
                    return  true;
                default:
                    return false;
            }
        });
        if (savedInstanceState ==  null) {
            nav.setVisibility(View.INVISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Me reinicie", "Me reinicie");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DatabaseHelper db = new DatabaseHelper(this);
        startService(new Intent(this, BluetoothServiceOBD.class).putExtra("deviceAddress",db.getObd(preferences.getString("username","error")).getAddress()));
        db.closeDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, BluetoothServiceOBD.class));
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
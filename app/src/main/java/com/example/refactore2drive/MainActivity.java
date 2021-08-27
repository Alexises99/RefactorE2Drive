package com.example.refactore2drive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.example.refactore2drive.call.CallFragment;
import com.example.refactore2drive.chart.ChartFragment;
import com.example.refactore2drive.controlpanel.InfoGridFragment;
import com.example.refactore2drive.login.LoginFragment;
import com.example.refactore2drive.models.SessionModel;
import com.example.refactore2drive.sessions.SessionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements NavigationHost{
    public static long prevSpeed = -1;
    public static long prevConsume = -1;
    public static SessionModel sessionModel;
    public static boolean sessionStarted;

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

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
package com.example.refactore2drive.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MessageEvent;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.models.Device;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.example.refactore2drive.obd.OBDCommandJob;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DeveloperModeActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_mode);
        tableLayout = findViewById(R.id.table_commands_dev);
        linearLayout = findViewById(R.id.linear_dev);
        stopService(new Intent(this, BluetoothServiceOBD.class));
        DatabaseHelper db = new DatabaseHelper(this);
        String username = Helper.getUsername(this);
        String address = db.getObd(username).getAddress();
        Intent intent = new Intent(this, BluetoothServiceOBD.class);
        intent.putExtra("deviceAddress", address);
        intent.putExtra("dev", true);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        updateTable(event.name, event.id, event.result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, BluetoothServiceOBD.class));
    }

    public void updateTable(String id, String name, String res) {
        if (linearLayout.findViewWithTag(id) != null) {
            TextView existingTV = linearLayout.findViewWithTag(id);
            existingTV.setText(res);
        } else{
            addTableRow(id,name,res);
        }
    }

    private void addTableRow(String id, String key, String val) {
        TableRow tr = new TableRow(this);
        tr.setGravity(Gravity.CENTER);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 1);
        tr.setLayoutParams(params);
        TextView name = new TextView(this);
        name.setGravity(Gravity.START);
        key = key + ":";
        name.setText(key);
        TextView value = new TextView(this);
        name.setTextSize(16);
        name.setGravity(Gravity.START);
        name.setTypeface(null, Typeface.BOLD);
        value.setTextSize(24);
        value.setGravity(Gravity.END);
        value.setGravity(Gravity.END);
        value.setText(val);
        value.setTag(id);
        tr.addView(name);
        tr.addView(value);
        tableLayout.addView(tr, params);
    }
}
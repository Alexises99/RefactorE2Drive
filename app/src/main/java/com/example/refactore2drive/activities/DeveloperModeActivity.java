package com.example.refactore2drive.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.MessageEvent;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.example.refactore2drive.obd.BluetoothServiceOBD;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DeveloperModeActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_mode);
        tableLayout = findViewById(R.id.table_commands_dev);
        linearLayout = findViewById(R.id.linear_dev);
        //Paramos el servicio por si acaso
        stopService(new Intent(this, BluetoothServiceOBD.class));
        DatabaseHelper db = new DatabaseHelper(this);
        String username = Helper.getUsername(this);
        String address = db.getObd(username).getAddress();
        //Lanzamos el servicio
        Intent intent = new Intent(this, BluetoothServiceOBD.class);
        intent.putExtra("deviceAddress", address);
        //Indicamos que estamos en modo desarrollador
        intent.putExtra("dev", true);
        startService(intent);
        Toolbar toolbar = findViewById(R.id.dev_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Slidr.attach(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);

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

    /**
     * Se encarga de actualizar el tableview, si un comando es nuevo inserta una fila sino unicamente
     * la actualiza
     * @param id del comando
     * @param name del comando
     * @param res resultado del comando
     */
    public void updateTable(String id, String name, String res) {
        if (linearLayout.findViewWithTag(id) != null) {
            TextView existingTV = linearLayout.findViewWithTag(id);
            existingTV.setText(res);
        } else{
            addTableRow(id,name,res);
        }
    }

    /**
     * Añade una fila a la tavla
     * @param id
     * @param key
     * @param val
     */
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
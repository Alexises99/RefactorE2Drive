package com.example.refactore2drive.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.refactore2drive.R;
import com.r0adkll.slidr.Slidr;

public class MoreInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        Slidr.attach(this);
    }

}
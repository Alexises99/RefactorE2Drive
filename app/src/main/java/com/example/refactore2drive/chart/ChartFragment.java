package com.example.refactore2drive.chart;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;

import com.example.refactore2drive.Helper;
import com.example.refactore2drive.R;
import com.example.refactore2drive.database.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartFragment extends Fragment {
    private DatabaseHelper db;
    private HashMap<Integer, String> quarters;
    private LineChart chart;
    private EditText date;
    private String username;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = new DatabaseHelper(context);
        username = Helper.getUsername(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        setUpToolbar(view);
        chart = view.findViewById(R.id.chart);
        date = view.findViewById(R.id.chartDateEdit);
        date.setOnClickListener(view1 -> showDatePickerDialog());
        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populate();
        drawChart(LocalDate.now());
    }

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    private void populate() {
        quarters = new HashMap<>();
        for (int i = 0; i < 86400; i++) {
            quarters.put(i, LocalTime.ofSecondOfDay(i).toString());
        }
    }

    private ArrayList<Entry> process(ArrayList<Value> values) {
        ArrayList<Entry> dataList = new ArrayList<>();
        for (Value value : values) {
            Entry entry = new Entry(value.getX(), value.getY());
            dataList.add(entry);
        }
        return dataList;
    }

    private void configureAxis(XAxis xAxis, YAxis yAxisLeft, YAxis yAxisRight, ValueFormatter formatter) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(13f);
        xAxis.setTextColor(Color.rgb(92, 204, 206));
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(2f);
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawGridLines(false);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setEnabled(false);
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(false);
    }

    private void configureLine(LineDataSet lineDataSet, int red, int green, int blue) {
        lineDataSet.setColor(Color.rgb(red, green, blue));
        lineDataSet.setLineWidth(6);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColor(Color.rgb(red,green,blue));
        lineDataSet.setCircleHoleColor(Color.WHITE);
        lineDataSet.setCircleRadius(10);
        lineDataSet.setCircleHoleRadius(4);
        lineDataSet.setValueTextSize(10);
    }

    private List<Value> getDataConsume(LocalDate date) {
        return db.getDataConsumeByDate(username, date.toString());
    }

    private List<Value> getDataSpeed(LocalDate date) {
        return db.getDataSpeedByDate(username, date.toString());
    }

    private void configureLegend(Legend legend) {
        legend.setFormSize(10f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(12f);
        legend.setXEntrySpace(5f);
        legend.setYEntrySpace(5f);
    }

    private void drawChart(LocalDate date) {
        ArrayList<Value> values = new ArrayList<>(getDataConsume(date));
        ArrayList<Entry> list = process(values);
        ArrayList<Value> values1 = new ArrayList<>(getDataSpeed(date));
        Log.d("Lista1", values1.toString());
        ArrayList<Entry> list1 = process(values1);
        Log.d("process1", list1.toString());
        LineDataSet lineDataSet = new LineDataSet(list, "Consumo");
        LineDataSet lineDataSet1 = new LineDataSet(list1, "Velocidad");
        configureLine(lineDataSet, 76, 146, 177);
        configureLine(lineDataSet1, 228,142,88);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        //dataSets.add(lineDataSet);
        dataSets.add(lineDataSet1);
        LineData data = new LineData(dataSets);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return quarters.get((int) value);
            }
        };
        XAxis xAxis = chart.getXAxis();
        YAxis yAxisLeft = chart.getAxisLeft();
        YAxis yAxisRight = chart.getAxisRight();
        configureAxis(xAxis, yAxisLeft, yAxisRight, formatter);
        Legend legend = chart.getLegend();
        configureLegend(legend);
        chart.setData(data);
        chart.invalidate();
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance((datePicker, year, month, day) -> {
            final String selectedDate = year + "-" + twoDigits(month+1) + "-" + twoDigits(day);
            date.setText(selectedDate);
            Log.d("Parse", LocalDate.of(year,month,day).toString());
            Log.d("Actual", LocalDate.now().toString());
            Log.d("Igual", "Iguales: " + selectedDate.trim().equals(LocalDate.now().toString().trim()));
            drawChart(LocalDate.of(year,month+1,day));
        });
        newFragment.show(getActivity().getSupportFragmentManager(),"datePicker");
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }
}
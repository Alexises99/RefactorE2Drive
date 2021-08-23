package com.example.refactore2drive.chart;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartFragment extends Fragment {
    private DatabaseHelper db;
    private HashMap<Integer, String> quarters;

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

    private List<Value> getDataConsume() {
        return db.getDataConsume("alex");
    }

    private List<Value> getDataSpeed() {
        return db.getDataSpeed("alex");
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        db = new DatabaseHelper(activity);
        populate();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.closeDB();
    }

    private void configureLegend(Legend legend) {
        legend.setFormSize(10f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(12f);
        legend.setXEntrySpace(5f);
        legend.setYEntrySpace(5f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        LineChart chart = view.findViewById(R.id.chart);
        ArrayList<Value> values = new ArrayList<>(getDataConsume());
        ArrayList<Entry> list = process(values);
        ArrayList<Value> values1 = new ArrayList<>(getDataSpeed());
        ArrayList<Entry> list1 = process(values1);
        LineDataSet lineDataSet = new LineDataSet(list, "Consumo");
        LineDataSet lineDataSet1 = new LineDataSet(list1, "Velocidad");
        configureLine(lineDataSet, 76, 146, 177);
        configureLine(lineDataSet1, 228,142,88);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
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
        return  view;
    }
}
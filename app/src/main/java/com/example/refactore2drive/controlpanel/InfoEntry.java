package com.example.refactore2drive.controlpanel;

import com.example.refactore2drive.R;

import java.util.ArrayList;
import java.util.List;

public class InfoEntry {
    public String title;
    public String value;
    public int resourceId;

    public InfoEntry(String title, String value, int val) {
        this.title = title;
        this.value = value;
        this.resourceId = val;
    }

    public static List<InfoEntry> initList() {
        List<InfoEntry> list = new ArrayList<>();
        InfoEntry speed = new InfoEntry("Velocidad", "120 khm", R.drawable.speed);
        InfoEntry temp = new InfoEntry("Temperatura", "37 C", R.drawable.termo);
        InfoEntry heart = new InfoEntry("Pulso", "82", R.drawable.heart);
        InfoEntry consumo = new InfoEntry("Consumo", "12 kWh", R.drawable.consume);
        list.add(speed);
        list.add(temp);
        list.add(heart);
        list.add(consumo);
        return list;
    }
}

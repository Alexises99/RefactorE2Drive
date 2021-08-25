package com.example.refactore2drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Helper {

    public static void makeToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static int formatTime(LocalTime time) {
        int val;
        if (time.getSecond() <= 5) val = time.getHour()*60*60+time.getMinute()*60;
        else if (time.getSecond() > 5 && time.getSecond() <= 10) val = time.getHour()*60*60+time.getMinute()*60+5;
        else if (time.getSecond() > 10 && time.getSecond() <= 15) val =  time.getHour()*60*60+time.getMinute()*60+10;
        else if (time.getSecond() > 15 && time.getSecond() <= 20) val= time.getHour()*60*60+time.getMinute()*60+15;
        else if (time.getSecond() > 20 && time.getSecond() <= 25) val= time.getHour()*60*60+time.getMinute()*60+20;
        else if (time.getSecond() > 25 && time.getSecond() <= 30) val= time.getHour()*60*60+time.getMinute()*60+25;
        else if (time.getSecond() > 30 && time.getSecond() <= 35) val= time.getHour()*60*60+time.getMinute()*60+30;
        else if (time.getSecond() > 35 && time.getSecond() <= 40) val= time.getHour()*60*60+time.getMinute()*60+35;
        else if (time.getSecond() > 40 && time.getSecond() <= 45) val= time.getHour()*60*60+time.getMinute()*60+40;
        else if (time.getSecond() > 45 && time.getSecond() <= 50) val= time.getHour()*60*60+time.getMinute()*60+45;
        else if (time.getSecond() > 50 && time.getSecond() <= 55) val= time.getHour()*60*60+time.getMinute()*60+50;
        else if (time.getSecond() > 55 && time.getSecond() <= 59) val= time.getHour()*60*60+time.getMinute()*60+55;
        else val = (time.getHour()*60*60+time.getMinute()*60)+30;
        return val;
    }

    public static HashMap<String, String> initFilter() {
        HashMap<String, String> filter = new HashMap<>();
        filter.put("Engine RPM", "RPM");
        filter.put("Barometric Pressure", "kPa");
        filter.put("Wideband Air/Fuel Ratio", "AFR");
        filter.put("Throttle Position", "%");
        filter.put("Vehicle Speed", "km/h");
        filter.put("Mass Air Flow", "g/s");
        filter.put("Ambient Air Temperature", "C");
        filter.put("Engine oil temperature", "C");
        filter.put("Engine Runtime", "");
        filter.put("Air Intake Temperature", "C");
        filter.put("Fuel Type", "");
        filter.put("Absolute load", "%");
        filter.put("Engine Load", "%");
        filter.put("Fuel Consumption Rate", "L/h");
        filter.put("Air/Fuel Ratio", "AFR");
        filter.put("Intake Manifold Pressure", "kPa");
        filter.put("Engine Coolant Temperature", "C");
        filter.put("Fuel Level", "%");
        filter.put("Fuel Rail Pressure", "kPa");
        filter.put("Fuel Pressure", "kPa");
        filter.put("Short Term Fuel Trim Bank 1", "%");
        return initFilter();
    }

    public static String getUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString("username", "error");
        if (username.equals("error")) return "error";
        else return username;
    }


}

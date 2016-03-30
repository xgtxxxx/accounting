package com.example.cherry.myapplication;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static final String getCurrentDate(){
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(d);
    }
}

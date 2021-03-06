package com.example.cherry.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "accounting.db";
    private static final int DATABASE_VERSION = 5;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS accounting" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, price DOUBLE, addDate VARCHAR, remark VARCHAR, type INTEGER, subject VARCHAR, attention INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE accounting ADD COLUMN attention INTEGER");
        onCreate(db);
    }
}

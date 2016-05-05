package com.example.cherry.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    public static final int TYPE_ACCOUNT = 0;
    public static final int TYPE_BUDGET = 1;
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public void add(Item item) {
        db.execSQL("INSERT INTO accounting VALUES(null, ?, ?, ?, ?, ?)", new Object[]{item.getName(), item.getPrice(), item.getAddDate(), item.getRemark(), item.getType()});
    }

    public void update(Item item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("price",item.getPrice());
        cv.put("addDate",item.getAddDate());
        cv.put("remark",item.getRemark());
        cv.put("type",item.getType());
        db.update("accounting", cv, "id = ?", new String[]{String.valueOf(item.getId())});
    }

    public void delete(String id) {
        db.delete("accounting", "id = ?", new String[]{id});
    }

    public Item get(String id) {
        Cursor c = db.rawQuery("SELECT * FROM accounting where id=?", new String[]{id});
        while (c.moveToNext()) {
            Item item = new Item();
            item.setId(c.getInt(0));
            item.setName(c.getString(1));
            item.setPrice(c.getDouble(2));
            item.setAddDate(c.getString(3));
            item.setRemark(c.getString(4));
            return item;
        }
        return null;
    }

    public List<Item> query(int type) {
        List<Item> items = new ArrayList<Item>();
        Cursor c = queryTheCursor(type);
        while (c.moveToNext()) {
            Item item = new Item();
            item.setId(c.getInt(0));
            item.setName(c.getString(1));
            item.setPrice(c.getDouble(2));
            item.setAddDate(c.getString(3));
            item.setRemark(c.getString(4));
            items.add(item);
        }
        c.close();
        return items;
    }

    public Cursor queryTheCursor(int type) {
        Cursor c = db.rawQuery("SELECT * FROM accounting where type=? order by id desc", new String[]{String.valueOf(type)});
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}

package com.example.cherry.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class MyAdapter extends SimpleAdapter {
    private int[] colors = new int[]{0x30d1e6ff,0x30FFFFFF};

    private List<? extends Map<String, ?>> datas;

    public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        datas = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        LinearLayout lv = (LinearLayout)view;
        TextView tv = (TextView)lv.getChildAt(lv.getChildCount() - 1);
        int attentionFlag = getItemViewType(position);
        tv.setText("☆");
        tv.setTextSize(26);
        if(attentionFlag==1){
            tv.setTextColor(Color.parseColor("black"));
        }else{
            tv.setTextColor(Color.parseColor("lightgrey"));
        }
        int colorPos = position % colors.length;
        view.setBackgroundColor(colors[colorPos]);
//        int attentionFlag = getItemViewType(position);
//        //关注
//        if(attentionFlag==1){
//            view.setBackgroundColor(0x30eeff88);
//        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String,Object> row = (Map<String,Object>)getItem(position);
        return (Integer)row.get("attention");
    }

    public List<? extends Map<String, ?>> getDatas(){
        return this.datas;
    };
}

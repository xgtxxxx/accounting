package com.example.cherry.myapplication;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

public class SegmentView extends LinearLayout {
    private TextView textView1;
    private TextView textView2;
    private onSegmentViewClickListener listener;
    public SegmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SegmentView(Context context) {
        super(context);
        init();
    }

    public void setActive(int position){
        if(position==0){
            textView1.setSelected(true);
            textView2.setSelected(false);
        }else if(position==1){
            textView1.setSelected(false);
            textView2.setSelected(true);
        }
    }

    public int getActive(){
        int flag = 0;
        if(textView2.isSelected()){
            flag = 1;
        }
        return flag;
    }

    private void init() {
//      this.setLayoutParams(new LinearLayout.LayoutParams(dp2Px(getContext(), 60), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView1 = new TextView(getContext());
        textView2 = new TextView(getContext());
        textView1.setClickable(true);
        textView2.setClickable(true);
        textView1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));
        textView2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));
        textView1.setText("记账");
        textView2.setText("预算");
        XmlPullParser xrp = getResources().getXml(R.xml.seg_text_color_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);
            textView1.setTextColor(csl);
            textView2.setTextColor(csl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        textView1.setGravity(Gravity.CENTER);
        textView2.setGravity(Gravity.CENTER);
        textView1.setPadding(3, 6, 3, 6);
        textView2.setPadding(3, 6, 3, 6);
        setSegmentTextSize(20);
        textView1.setHeight(dp2px(40));
        textView2.setHeight(dp2px(40));
        textView1.setBackgroundResource(R.drawable.seg_left);
        textView2.setBackgroundResource(R.drawable.seg_right);
        textView1.setSelected(true);
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView1.isSelected()) {
                    return;
                }
                textView1.setSelected(true);
                textView2.setSelected(false);
                if (listener != null) {
                    listener.onSegmentViewClick(textView1, 0);
                }
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView2.isSelected()) {
                    return;
                }
                textView2.setSelected(true);
                textView1.setSelected(false);
                if (listener != null) {
                    listener.onSegmentViewClick(textView2, 1);
                }
            }
        });
        this.removeAllViews();
        this.addView(textView1);
        this.addView(textView2);
        this.invalidate();
    }
    /**
     * 设置字体大小 单位dip
     * <p>2014年7月18日</p>
     * @param dp
     * @author RANDY.ZHANG
     */
    public void setSegmentTextSize(int dp) {
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp);
    }

    private static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setOnSegmentViewClickListener(onSegmentViewClickListener listener) {
        this.listener = listener;
    }


    /**
     * 设置文字
     * <p>2014年7月18日</p>
     * @param text
     * @param position
     * @author RANDY.ZHANG
     */
    public void setSegmentText(CharSequence text,int position) {
        if (position == 0) {
            textView1.setText(text);
        }
        if (position == 1) {
            textView2.setText(text);
        }
    }

    public static interface onSegmentViewClickListener{
        /**
         *
         * @param v
         * @param position 0-左边 1-右边
         * @author RANDY.ZHANG
         */
        public void onSegmentViewClick(View v,int position);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}

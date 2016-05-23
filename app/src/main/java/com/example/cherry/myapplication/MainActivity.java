package com.example.cherry.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int TEXT_SIZE = 16;

    private DBManager dbManager;

    private ExpandableListView listView;

    private TextView total;

    private SegmentView segmentView;

    private int type;

    private Filter filter = new Filter();

    private MyExpandableListAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = new DBManager(this);
        total = (TextView) this.findViewById(R.id.totalMoney);
        listView = (ExpandableListView) this.findViewById(R.id.listView);
        segmentView = (SegmentView)this.findViewById(R.id.btn_seg);
        initAddBtnClickListener();
        initTypeFilter();
        initAttentionFilter();
        initPriceSortFilter();
        initSubjectSortFilter();
        initChildClickEvent();
        initView();
    }

    private void initAddBtnClickListener(){
        TextView addBtn = (TextView) this.findViewById(R.id.newAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddPage();
            }
        });
    }

    public void doAttention(View view){
        TextView tv = (TextView)view;
        LinearLayout ll = (LinearLayout)tv.getParent();
        TextView idView = (TextView)ll.getChildAt(0);
        int id = Integer.parseInt(idView.getText().toString());
        if(id>0){
            Item row = getItemById(id);
            if(Color.parseColor("lightgrey")==tv.getTextColors().getDefaultColor()){
                row.setAttention(1);
                tv.setTextColor(Color.parseColor("black"));
            }else{
                row.setAttention(0);
                tv.setTextColor(Color.parseColor("lightgrey"));
            }
            dbManager.update(row);
        }
    }

    private Item getItemById(int id){
        for(ExpandableItem item : myAdapter.getMap().values()){
            for(Item child : item.getChildren()){
                if(child.getId()==id){
                    return child;
                }
            }
        }
        return null;
    }

    private void initAttentionFilter(){
        TextView tv = (TextView)this.findViewById(R.id.attention);
        tv.setTextSize(24);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                ColorStateList list = tv.getTextColors();
                int c = list.getDefaultColor();
                if(c!=-1){
                    tv.setTextColor(-1);
                    filter.setIsAttention(false);
                }else{
                    tv.setTextColor(Color.parseColor("black"));
                    filter.setIsAttention(true);
                }
                filterData(getItems());
            }
        });
    }

    private void initSubjectSortFilter(){
        TextView tv = (TextView)this.findViewById(R.id.subject_btn);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if ("项目▼".equals(textView.getText().toString())) {
                    textView.setText("项目▲");
                    filter.setIsExpand(true);
                } else {
                    textView.setText("项目▼");
                    filter.setIsExpand(false);
                }
                sort(getCurrentDatas());
            }
        });
    }

    private void initPriceSortFilter(){
        TextView priceView = (TextView)this.findViewById(R.id.priceview);
        priceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                String text = tv.getText().toString();
                if ("金额↓".equals(text)) {
                    filter.setPriceSort(1);
                    tv.setText("金额↑");
                    sort(getCurrentDatas());
                } else if ("金额↑".equals(text)) {
                    filter.setPriceSort(0);
                    tv.setText("金额");
                    filterData(getItems());
                } else{
                    filter.setPriceSort(2);
                    tv.setText("金额↓");
                    sort(getCurrentDatas());
                }
            }
        });
    }

    private void reset(){
        TextView tv = (TextView)this.findViewById(R.id.attention);
        tv.setTextColor(-1);

        tv = (TextView)this.findViewById(R.id.subject_btn);
        tv.setText("项目▲");

        tv = (TextView)this.findViewById(R.id.priceview);
        tv.setText("金额");

        filter = new Filter();
    }

    private void initTypeFilter(){
        segmentView.setOnSegmentViewClickListener(new SegmentView.onSegmentViewClickListener() {
            @Override
            public void onSegmentViewClick(View v, int position) {
                reset();
                if (position == 0) {
                    filterData(getAccountItems());
                } else if (position == 1) {
                    filterData(getBudgetItems());
                }
            }
        });
    }

    private void initChildClickEvent(){
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Item item = (Item) myAdapter.getChild(groupPosition, childPosition);
                toEditPage(item);
                return false;
            }
        });
    }

    private void initView(){
        int type1 = getIntent().getIntExtra("type", 0);
        if(type1==0){
            filterData(getAccountItems());
            segmentView.setActive(0);
        }else{
            segmentView.setActive(1);
            filterData(getBudgetItems());
        }
    }

    private List<ExpandableItem> getCurrentDatas(){
        Collection<ExpandableItem> items = myAdapter.getMap().values();
        List<ExpandableItem> list = new ArrayList<ExpandableItem>();
        list.addAll(items);
        return list;
    }

    private void filterData(final List<Item> items){
        List<Item> filteredItems = new ArrayList<Item>();
        if(filter.isAttention){
            for(Item item: items){
                if(item.getAttention()==1){
                    filteredItems.add(item);
                }
            }
        }else{
            filteredItems = items;
        }

        List<ExpandableItem> expandableItems = processData(filteredItems);
        sort(expandableItems);
    }

    private void sort(List<ExpandableItem> expandableItems){
        if(filter.getPriceSort()!=0){
            Collections.sort(expandableItems, new Comparator<ExpandableItem>() {
                @Override
                public int compare(ExpandableItem lhs, ExpandableItem rhs) {
                    int flag = lhs.getParent().getPrice() > rhs.getParent().getPrice() ? 1 : -1;
                    if (filter.getPriceSort() == 1) {
                        return flag;
                    } else {
                        return 0 - flag;
                    }
                }
            });

            for(ExpandableItem item : expandableItems){
                Collections.sort(item.getChildren(), new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        int flag = lhs.getPrice() > rhs.getPrice() ? 1 : -1;
                        if (filter.getPriceSort() == 1) {
                            return flag;
                        } else {
                            return 0 - flag;
                        }
                    }
                });
            }
        }

        refreshView(expandableItems);
    }

    private List<ExpandableItem> processData(final List<Item> items){
        Map<String,ExpandableItem> expandableMap = new LinkedHashMap<String, ExpandableItem>();
        List<ExpandableItem> list = new ArrayList<ExpandableItem>();
        for(Item item: items){
            ExpandableItem p = expandableMap.get(item.getSubject());
            if(p == null){
                p = new ExpandableItem(item.getSubject());
                expandableMap.put(item.getSubject(), p);
                list.add(p);
            }
            p.addChild(item);
        }
        return list;
    }

    private void refreshView(List<ExpandableItem> expandableItems){
        Map<String,ExpandableItem> map = new LinkedHashMap<String, ExpandableItem>();
        List<String> subjects = new ArrayList<String>();
        for(ExpandableItem item: expandableItems){
            map.put(item.getSubject(),item);
            subjects.add(item.getSubject());
        }
        initTotal(map);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(map,subjects);
        this.myAdapter = adapter;
        listView.setAdapter(adapter);

        if(filter.isExpand()){
            for(int i = 0; i < adapter.getGroupCount(); i++){
                listView.expandGroup(i);
            }
        }
    }

    private void toEditPage(Item item) {
        Intent intent = new Intent(this, EditItem.class);
        intent.putExtra("store", "from activityMain");
        intent.putExtra("itemId", String.valueOf(item.getId()));
        setType(intent);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void toAddPage(){
        Intent intent = new Intent(MainActivity.this, EditItem.class);
        intent.putExtra("store", "from activityMain");
        setType(intent);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void setType(Intent intent){
        if(segmentView.getActive()==0){
            intent.putExtra("type",DBManager.TYPE_ACCOUNT);
        }else{
            intent.putExtra("type", DBManager.TYPE_BUDGET);
        }
    }

    private boolean doDelete(String id) {
        dbManager.delete(id);
        return true;
    }

    private void initTotal(final Map<String,ExpandableItem> map) {
        double m = 0;
        for (ExpandableItem row : map.values()) {
            m += row.getParent().getPrice();
        }
        total.setText("总额:￥" + String.valueOf(m));
    }

    private List<Item> getItems(){
        if(type==DBManager.TYPE_ACCOUNT){
            return getAccountItems();
        }else if(type==DBManager.TYPE_BUDGET){
            return getBudgetItems();
        }
        return null;
    }

    private List<Item> getAccountItems() {
        type = DBManager.TYPE_ACCOUNT;
        return dbManager.query(DBManager.TYPE_ACCOUNT);
    }

    private List<Item> getBudgetItems() {
        type = DBManager.TYPE_BUDGET;
        return dbManager.query(DBManager.TYPE_BUDGET);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
    }

    class Filter{
        private boolean isAttention;

        private int priceSort = 0;

        private boolean isExpand = true;

        public boolean isExpand() {
            return isExpand;
        }

        public void setIsExpand(boolean isExpand) {
            this.isExpand = isExpand;
        }

        public int getPriceSort() {
            return priceSort;
        }

        public void setPriceSort(int priceSort) {
            this.priceSort = priceSort;
        }

        public boolean isAttention() {
            return isAttention;
        }

        public void setIsAttention(boolean isAttention) {
            this.isAttention = isAttention;
        }
    }

    class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private Map<String,ExpandableItem> map;

        private List<String> subjects;

        public Map<String,ExpandableItem> getMap(){
            return map;
        }

        public MyExpandableListAdapter(Map<String,ExpandableItem> map,List<String> subjects){
            this.map = map;
            this.subjects = subjects;
        }

        //得到子item需要关联的数据
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            String subject = subjects.get(groupPosition);
            return map.get(subject).getChildren().get(childPosition);
        }

        //得到子item的ID
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //设置子item的组件
        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            try{

                String subject = subjects.get(groupPosition);
                Item item = map.get(subject).getChildren().get(childPosition);
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) MainActivity.this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.expandable_list_children, null);
                }
                TextView tv = (TextView)convertView.findViewById(R.id.rowid);
                tv.setText(String.valueOf(item.getId()));

                tv = (TextView)convertView.findViewById(R.id.name);
                tv.setText(item.getName());

                tv = (TextView)convertView.findViewById(R.id.price);
                tv.setText(String.valueOf(item.getPrice()));

                tv = (TextView)convertView.findViewById(R.id.attention_row);
                int attentionFlag = item.getAttention();
                tv.setText("☆");
                tv.setTextSize(26);
                if(attentionFlag==1){
                    tv.setTextColor(Color.parseColor("black"));
                }else{
                    tv.setTextColor(Color.parseColor("lightgrey"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return convertView;
        }

        //获取当前父item下的子item的个数
        @Override
        public int getChildrenCount(int groupPosition) {
            String subject = subjects.get(groupPosition);
            return map.get(subject).getChildren().size();
        }
        //获取当前父item的数据
        @Override
        public Object getGroup(int groupPosition) {
            String subject = subjects.get(groupPosition);
            return map.get(subject).getParent();
        }

        @Override
        public int getGroupCount() {
            return subjects.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        //设置父item组件
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.expandable_list_parent, null);
            }
            String subject = subjects.get(groupPosition);
            Item item = map.get(subject).getParent();
            TextView tv = (TextView)convertView.findViewById(R.id.p_rowid);
            tv.setText("0");

            tv = (TextView)convertView.findViewById(R.id.p_name);
            tv.setText(item.getSubject());

            tv = (TextView)convertView.findViewById(R.id.p_price);
            tv.setText(String.valueOf(item.getPrice()));

            tv = (TextView)convertView.findViewById(R.id.p_attention_row);
            int attentionFlag = item.getAttention();
            tv.setText("☆");
            tv.setTextSize(26);
            if(attentionFlag==1){
                tv.setTextColor(Color.parseColor("black"));
            }else{
                tv.setTextColor(Color.parseColor("lightgrey"));
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }
}

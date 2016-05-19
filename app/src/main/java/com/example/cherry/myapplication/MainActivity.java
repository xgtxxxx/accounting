package com.example.cherry.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int TEXT_SIZE = 16;

    private DBManager dbManager;

    private SwipeMenuListView listView;

    private TextView total;

    private SegmentView segmentView;

    private int type;

    private boolean isAttention;
    private boolean isSubject;

    private int currentSortFlag = 0;
    private String currentSortText = "金额";

    private List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

    private List<HashMap<String,Object>> currentViewData = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = new DBManager(this);
        total = (TextView) this.findViewById(R.id.totalMoney);
        listView = (SwipeMenuListView) this.findViewById(R.id.listView);
        segmentView = (SegmentView)this.findViewById(R.id.btn_seg);
        initView();
        initSwipeMenu();
        setViewClickListener();
//        ItemOnLongClick1(listView);
        TextView addBtn = (TextView) this.findViewById(R.id.newAdd);
        setSegmentViewClickListener();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddPage();
            }
        });
        initSortListener((TextView) this.findViewById(R.id.priceview));
        initAttentionClickListener();
        initSubjectOnClickListener();
    }

    public void doAttention(View view){
        TextView tv = (TextView)view;
        LinearLayout ll = (LinearLayout)tv.getParent();
        TextView idView = (TextView)ll.getChildAt(0);
        int id = Integer.parseInt(idView.getText().toString());
        if(id>0){
            HashMap<String,Object> row = getRowWithId(id);
            if(Color.parseColor("lightgrey")==tv.getTextColors().getDefaultColor()){
                row.put("attention",1);
                tv.setTextColor(Color.parseColor("black"));
            }else{
                row.put("attention",0);
                tv.setTextColor(Color.parseColor("lightgrey"));
            }
            dbManager.update(toItem(row));
        }
    }

    //未完待续
    public void showChildren(View view){
        TextView tv = (TextView)view;
        LinearLayout ll = (LinearLayout)tv.getParent();
        TextView idView = (TextView)ll.getChildAt(0);
        int id = Integer.parseInt(idView.getText().toString());
        if(id==0){
            HashMap<String,Object> row = getRowWithId(id);
        }
    }

    private HashMap<String,Object> getRowWithId(int id){
        for(HashMap<String,Object> row:data){
            if(((Integer)row.get("id")).intValue()==id){
                return row;
            }
        }
        return null;
    }

    private void filterData(){
        List<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();
        if(isAttention){
            for (HashMap<String, Object> row : data) {
                if ((Integer) row.get("attention") == 1) {
                    datas.add(row);
                }
            }
        }else{
            datas = new ArrayList<HashMap<String, Object>>(data);
        }

        if(isSubject){
            datas = mergeBySubject(datas);
        }
        datas = sort(currentSortFlag,currentSortText,datas);
        refreshView(datas);
    }

    private List<HashMap<String,Object>> mergeBySubject(final List<HashMap<String, Object>> datas){
        Map<String,SubjectData> rows = new HashMap<String, SubjectData>();
        for(HashMap<String,Object> d: datas){
            SubjectData sd = rows.get((String)d.get("subject"));
            if(sd==null){
                sd = new SubjectData((String)d.get("subject"));
            }
            sd.addData(d);
            rows.put((String)d.get("subject"),sd);
        }
        List<HashMap<String,Object>> list = new ArrayList<HashMap<String, Object>>();
        for(SubjectData sd: rows.values()){
            list.add(sd.toSubjectData());
        }
        return list;
    }

    private void initAttentionClickListener(){
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
//                        refreshView(data);
                    isAttention = false;
                }else{
                    tv.setTextColor(Color.parseColor("black"));
//                        List<HashMap<String, Object>> attentions = new ArrayList<HashMap<String, Object>>();
//                        for (HashMap<String, Object> row : data) {
//                            if ((Integer) row.get("attention") == 1) {
//                                attentions.add(row);
//                            }
//                        }
//                        refreshView(attentions);
                    isAttention = true;
                }
                filterData();
            }
        });
    }

    private void initSubjectOnClickListener(){
        TextView tv = (TextView)this.findViewById(R.id.subject_btn);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if ("项目▼".equals(textView.getText().toString())) {
                    textView.setText("项目▲");
                    isSubject = true;
                } else {
                    textView.setText("项目▼");
//                    refreshView(currentViewData);
                    isSubject = false;
                }
                filterData();
            }
        });
    }

    private void initSortListener(final TextView priceView){
        priceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                String text = tv.getText().toString();
                if ("金额".equals(text)) {
                    sort(1, "金额↑", null);
                } else if ("金额↑".equals(text)) {
                    sort(2, "金额↓", null);
                } else {
                    sort(0, "金额", null);
                }
            }
        });
    }

    private void initSwipeMenu(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
//                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
//                openItem.setWidth(dp2px(80));
//                openItem.setTitle("关注");
//                openItem.setTitleSize(TEXT_SIZE);
//                openItem.setTitleColor(Color.WHITE);
//                menu.addMenuItem(openItem);
//                //默认值0==关注
//                //==取消关注
//                if(menu.getViewType()==1){
//                    openItem.setTitle("取消关注");
//                }

                SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0xC9, 0x1E)));
                editItem.setWidth(dp2px(80));
                editItem.setTitle("编辑");
                editItem.setTitleSize(TEXT_SIZE);
                editItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(editItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(dp2px(80));
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(TEXT_SIZE);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Map row = data.get(position);
                switch (index) {
                    case 0:
                        toEditPage(row);
                        break;
                    case 1:
                        showDeleteNotice(String.valueOf(row.get("id")), position);
                        break;
//                    case 0:
//                        attention(row, menu);
//                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void attention(final Map<String,Object> row,final SwipeMenu menu){
        Integer flag = (Integer)row.get("attention");
        if(flag==0){
            row.put("attention",1);
        }else if(flag==1){
            row.put("attention",0);
        }
        dbManager.update(toItem(row));
        refreshView(data);
    }

    private Item toItem(final Map<String,Object> row){
        Item item = new Item();
        item.setId((Integer)row.get("id"));
        item.setName((String)row.get("name"));
        item.setPrice((Double)row.get("price"));
        item.setAddDate((String)row.get("addDate"));
        item.setRemark((String)row.get("remark"));
        item.setType((Integer)row.get("type"));
        item.setSubject((String) row.get("subject"));
        item.setAttention((Integer) row.get("attention"));
        return item;
    }

    private void reset(){
        TextView tv = (TextView)this.findViewById(R.id.attention);
        tv.setTextColor(-1);

        tv = (TextView)this.findViewById(R.id.subject_btn);
        tv.setText("项目▼");

        tv = (TextView)this.findViewById(R.id.priceview);
        tv.setText("金额");

        isAttention = false;
        isSubject = false;
        currentSortFlag = 0;
        currentSortText = "金额";
    }

    private void setSegmentViewClickListener(){
        segmentView.setOnSegmentViewClickListener(new SegmentView.onSegmentViewClickListener() {
            @Override
            public void onSegmentViewClick(View v, int position) {
                reset();
                if (position == 0) {
                    refreshData(getAccountItems());
                } else if (position == 1) {
                    refreshData(getBudgetItems());
                }
            }
        });
    }

    private void ItemOnLongClick1(ListView mListView) {
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "转移");
                menu.add(0, 1, 0, "删除");
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Map<String, Object> row = data.get(info.position);
        switch (item.getItemId()) {
            case 0:
                updateType(row);
                break;
            case 1:
                if(doDelete(String.valueOf(row.get("id")))){
                    data.remove(info.position);
                    initTotal(data);
                    if (!data.isEmpty()) {
                        MyAdapter adapter = (MyAdapter) listView.getAdapter();
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void initView(){
        int type1 = getIntent().getIntExtra("type", 0);
        if(type1==0){
            refreshData(getAccountItems());
            segmentView.setActive(0);
        }else{
            segmentView.setActive(1);
            refreshData(getBudgetItems());
        }
    }

    private void setViewClickListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Map<String, Object> item = (Map<String, Object>) parent.getAdapter().getItem(position);
//                toEditPage(item);

            }
        });
    }

    private List<HashMap<String,Object>> sort(final int flag,final String text, List<HashMap<String,Object>> items){
        this.currentSortFlag = flag;
        this.currentSortText = text;
        boolean refresh = true;
        if(items==null){
            items = new ArrayList<HashMap<String, Object>>(currentViewData);
        }else{
            refresh = false;
        }
        if(flag!=0){
            Collections.sort(items, new Comparator<HashMap<String, Object>>() {
                @Override
                public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
                    int i = compareObject(lhs.get("price"),rhs.get("price"));
                    return flag==1?i:0-i;
                }
            });
        }
        ((TextView)this.findViewById(R.id.priceview)).setText(text);
        if(refresh){
            refreshView(items);
        }
        return items;
    }

    private static int compareObject(Object l, Object m){
        double ll = (Double)l;
        double mm = (Double)m;
        if(ll==mm){
            return 0;
        }else{
            return ll-mm>0?1:-1;
        }
    }

    private void refreshData(final List<Item> items){
        data.clear();
        for (Item i : items) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("id", i.getId());
            item.put("name", i.getName());
            item.put("price", i.getPrice());
            item.put("addDate", i.getAddDate());
            item.put("remark", i.getRemark());
            item.put("type",i.getType());
            item.put("subject",i.getSubject());
            item.put("attention",i.getAttention());
            data.add(item);
        }
        refreshView(data);
    }

    private void refreshView(final List<HashMap<String, Object>> items){
        this.currentViewData = items;
        initTotal(items);
        SimpleAdapter adapter = new MyAdapter(this, items, R.layout.items,
                new String[]{"id", "name", "price", "addDate","attention"}, new int[]{R.id.id, R.id.name, R.id.price, R.id.addDate,R.id.attention_row});
        listView.setAdapter(adapter);
    }

    private void updateType(Map<String,Object> row){
        if(type==DBManager.TYPE_BUDGET){
            row.put("typeflag", DBManager.TYPE_ACCOUNT);
        }else{
            row.put("typeflag", DBManager.TYPE_BUDGET);
        }
        toEditPage(row);
    }

    private void toEditPage(Map<String, Object> row) {
        Intent intent = new Intent(this, EditItem.class);
        intent.putExtra("store", "from activityMain");
        intent.putExtra("itemId", String.valueOf(row.get("id")));
        if(row.get("typeflag")==null){
            setType(intent);
        }else{
            intent.putExtra("type", (Integer) row.get("typeflag"));
        }
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

    private void initTotal(List<HashMap<String,Object>> datas) {
        double m = 0;
        for (Map<String, Object> row : datas) {
            m += Double.parseDouble(String.valueOf(row.get("price")));
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

    private void showDeleteNotice(final String id,final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确定要删除该行?");
        builder.setTitle("提示");
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (doDelete(id)) {
                    data.remove(position);
                    if (!data.isEmpty()) {
                        refreshData(getItems());
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private List<HashMap<String,Object>> getChildren(String subject){
        List<HashMap<String,Object>> children = new ArrayList<HashMap<String, Object>>();
        for(HashMap<String,Object> child: data){
            if(subject.equals((String)child.get("subject"))){
                if(isAttention){
                    int f =  (Integer)child.get("attention");
                    if(f==1){
                        children.add(child);
                    }
                }else{
                    children.add(child);
                }
            }
        }
        return children;
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


    class SubjectData{
        private String subject;
        private List<HashMap<String,Object>> datas;

        public SubjectData(String subject){
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void addData(HashMap<String,Object> data){
            if(datas==null){
                datas = new ArrayList<HashMap<String, Object>>();
            }
            datas.add(data);
        }

        public HashMap<String,Object> toSubjectData(){
            double total = 0;
            for(HashMap<String,Object> row: datas){
                total+=(Double)row.get("price");
            }
            HashMap<String,Object> item = new HashMap<String, Object>();
            item.put("subject",subject);
            item.put("id", 0);
            item.put("name", subject);
            item.put("price", total);
            item.put("addDate", "");
            item.put("remark", "");
            item.put("type","");
            item.put("attention",isAttention?1:0);
            return item;
        }
    }
}

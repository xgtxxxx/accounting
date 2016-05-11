package com.example.cherry.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
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

    private List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

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
//        setViewClickListener();
//        ItemOnLongClick1(listView);
        TextView addBtn = (TextView) this.findViewById(R.id.newAdd);
        setSegmentViewClickListener();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddPage();
            }
        });
    }

    private void initSwipeMenu(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                openItem.setWidth(dp2px(60));
                openItem.setTitle("移动");
                openItem.setTitleSize(TEXT_SIZE);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

                SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0xC9, 0x1E)));
                editItem.setWidth(dp2px(60));
                editItem.setTitle("编辑");
                editItem.setTitleSize(TEXT_SIZE);
                editItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(editItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
                deleteItem.setWidth(dp2px(60));
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
                        updateType(row);
                        break;
                    case 1:
                        toEditPage(row);
                        break;
                    case 2:
                        showDeleteNotice(String.valueOf(row.get("id")),position);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void setSegmentViewClickListener(){
        segmentView.setOnSegmentViewClickListener(new SegmentView.onSegmentViewClickListener() {
            @Override
            public void onSegmentViewClick(View v, int position) {
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
                    initTotal();
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
                Map<String, Object> item = (Map<String, Object>) parent.getAdapter().getItem(position);
                toEditPage(item);
            }
        });
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
            data.add(item);
        }
        initTotal();

        SimpleAdapter adapter = new MyAdapter(this, data, R.layout.items,
                new String[]{"id", "name", "price", "addDate"}, new int[]{R.id.id, R.id.name, R.id.price, R.id.addDate});
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

    private void initTotal() {
        double m = 0;
        for (Map<String, Object> row : data) {
            m += Double.parseDouble(String.valueOf(row.get("price")));
        }
        total.setText("总额:￥"+String.valueOf(m));
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
                    initTotal();
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
}

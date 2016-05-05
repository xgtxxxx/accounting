package com.example.cherry.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private DBManager dbManager;

    private ListView listView;

    private TextView total;
    private RadioGroup group;
    private RadioButton accBtn;
    private RadioButton budgetBtn;

    private int type;

    private List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = new DBManager(this);
        total = (TextView) this.findViewById(R.id.totalMoney);
        listView = (ListView) this.findViewById(R.id.listView);
        group = (RadioGroup) this.findViewById(R.id.radioGroup);
        accBtn = (RadioButton) this.findViewById(R.id.btn_account);
        budgetBtn = (RadioButton) this.findViewById(R.id.btn_budget);
        addRadioChangeListener();
        initView();
        setViewClickListener();
        ItemOnLongClick1(listView);
        ImageButton addBtn = (ImageButton) this.findViewById(R.id.newAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddPage();
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
        }else{
            budgetBtn.setChecked(true);
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

    private void addRadioChangeListener(){
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int checkedId) {
                if (accBtn.getId() == checkedId) {
                    refreshData(getAccountItems());
                } else if (budgetBtn.getId() == checkedId) {
                    refreshData(getBudgetItems());
                }
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
            row.put("typeflag",DBManager.TYPE_BUDGET);
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
            intent.putExtra("type", (Integer)row.get("typeflag"));
        }
        startActivityForResult(intent, 1);
        finish();
    }

    private void toAddPage(){
        Intent intent = new Intent(MainActivity.this, EditItem.class);
        intent.putExtra("store", "from activityMain");
        setType(intent);
        startActivityForResult(intent, 1);
        finish();
    }

    private void setType(Intent intent){
        if(budgetBtn.getId()==group.getCheckedRadioButtonId()){
            intent.putExtra("type", DBManager.TYPE_BUDGET);
        }else {
            intent.putExtra("type",DBManager.TYPE_ACCOUNT);
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
        total.setText(String.valueOf(m));
    }

    private List<Item> getAccountItems() {
        type = DBManager.TYPE_ACCOUNT;
        return dbManager.query(DBManager.TYPE_ACCOUNT);
    }

    private List<Item> getBudgetItems() {
        type = DBManager.TYPE_BUDGET;
        return dbManager.query(DBManager.TYPE_BUDGET);
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

package com.example.cherry.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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

    private List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbManager = new DBManager(this);
        total = (TextView)this.findViewById(R.id.totalMoney);
        listView = (ListView) this.findViewById(R.id.listView);
        //获取到集合数据
        List<Item> items = getItems();
        for (Item i : items) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("id", i.getId());
            item.put("name", i.getName());
            item.put("price", i.getPrice());
            item.put("addDate", i.getAddDate());
            item.put("remark",i.getRemark());
            data.add(item);
        }
        initTotal();
        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        SimpleAdapter adapter = new MyAdapter(this, data, R.layout.items,
                new String[]{"id", "name", "price", "addDate"}, new int[]{R.id.id, R.id.name, R.id.price, R.id.addDate});
        //实现列表的显示
        listView.setAdapter(adapter);
        ItemOnLongClick1(listView);


        Button addBtn = (Button)this.findViewById(R.id.newAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,EditItem.class);
                intent.putExtra("store", "from activityMain");
                startActivityForResult(intent, 1);
                finish();
            }
        });
    }

    private void ItemOnLongClick1(ListView mListView) {
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "修改");
                menu.add(0, 1, 0, "删除");
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Map<String, Object> row = data.get(info.position);
        switch (item.getItemId()) {
            case 0:
                toEditPage(row);
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

    private void toEditPage(Map<String, Object> row){
        Intent intent = new Intent(this,EditItem.class);
        intent.putExtra("store", "from activityMain");
        intent.putExtra("itemId",String.valueOf(row.get("id")));
        startActivityForResult(intent, 1);
        finish();
    }

    private boolean doDelete(String id){
        dbManager.delete(id);
        return true;
    }

    private void initTotal(){
        double m = 0;
        for(Map<String,Object> row: data){
            m += Double.parseDouble(String.valueOf(row.get("price")));
        }
        total.setText(String.valueOf(m));
    }

    private List<Item> getItems() {
        return dbManager.query();
    }

    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            System.out.println("====================================");
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

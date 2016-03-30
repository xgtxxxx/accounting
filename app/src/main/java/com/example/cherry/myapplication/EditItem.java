package com.example.cherry.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditItem extends Activity {
    private DBManager dbManager;

    private TextView title;
    private EditText name;
    private EditText money;
    private EditText remark;
    private Button save;
    private Button cancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        dbManager = new DBManager(this);

        title = (TextView)this.findViewById(R.id.title);
        name = (EditText)this.findViewById(R.id.itemName);
        money = (EditText)this.findViewById(R.id.itemMoney);
        remark = (EditText)this.findViewById(R.id.itemRemark);
        save = (Button)this.findViewById(R.id.btnSave);
        cancel = (Button)this.findViewById(R.id.btnCancel);

        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");
        if(itemId!=null){
            initEdit(itemId);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String itemId = intent.getStringExtra("itemId");
                if(itemId==null){
                    doAdd();
                }else{
                    doEdit(itemId);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnMainView();
            }
        });
    }

    private void initEdit(String id){
        Item item = dbManager.get(id);
        if(item!=null){
            title.setText(R.string.editItem);
            name.setText(item.getName());
            money.setText(String.valueOf(item.getPrice()));
            remark.setText(item.getRemark());
        }
    }

    private void doAdd(){
        Item item = new Item();
        item.setAddDate(DateUtil.getCurrentDate());
        item.setName(name.getText().toString());
        item.setPrice(Double.parseDouble(money.getText().toString()));
        item.setRemark(remark.getText().toString());
        dbManager.add(item);
        returnMainView();
    }

    private void doEdit(String id){
        Item item = new Item();
        item.setId(Integer.parseInt(id));
        item.setAddDate(DateUtil.getCurrentDate());
        item.setName(name.getText().toString());
        item.setPrice(Double.parseDouble(money.getText().toString()));
        item.setRemark(remark.getText().toString());
        dbManager.update(item);
        returnMainView();
    }

    private void returnMainView(){
        Intent intent = new Intent(EditItem.this,MainActivity.class);
        startActivityForResult(intent, 1);
        finish();
    }
}

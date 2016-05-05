package com.example.cherry.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditItem extends Activity {
    private DBManager dbManager;

    private EditText name;
    private EditText money;
    private EditText remark;
    private ImageView save;
    private ImageView cancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        dbManager = new DBManager(this);

        name = (EditText)this.findViewById(R.id.itemName);
        money = (EditText)this.findViewById(R.id.itemMoney);
        remark = (EditText)this.findViewById(R.id.itemRemark);
        save = (ImageView)this.findViewById(R.id.btnSave);
        cancel = (ImageView)this.findViewById(R.id.btnCancel);

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
                int type = intent.getIntExtra("type", 0);
                if(itemId==null){
                    doAdd(type);
                }else{
                    doEdit(itemId,type);
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

    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            returnMainView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initEdit(String id){
        Item item = dbManager.get(id);
        if(item!=null){
            name.setText(item.getName());
            money.setText(String.valueOf(item.getPrice()));
            remark.setText(item.getRemark());
        }
    }

    private void doAdd(int type){
        Item item = new Item();
        item.setAddDate(DateUtil.getCurrentDate());
        item.setName(name.getText().toString());
        item.setPrice(Double.parseDouble(money.getText().toString()));
        item.setRemark(remark.getText().toString());
        item.setType(type);
        dbManager.add(item);
        returnMainView();
    }

    private void doEdit(String id,int type){
        Item item = new Item();
        item.setId(Integer.parseInt(id));
        item.setAddDate(DateUtil.getCurrentDate());
        item.setName(name.getText().toString());
        item.setPrice(Double.parseDouble(money.getText().toString()));
        item.setRemark(remark.getText().toString());
        item.setType(type);
        dbManager.update(item);
        returnMainView();
    }

    private void returnMainView(){
        Intent intent = new Intent(EditItem.this,MainActivity.class);
        Intent thisItement = getIntent();
        int type = thisItement.getIntExtra("type",0);
        intent.putExtra("type", type);
        startActivityForResult(intent, 1);
        finish();
    }
}

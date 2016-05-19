package com.example.cherry.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.KeyStore;

public class EditItem extends Activity {
    private DBManager dbManager;

    private EditText name;
    private EditText money;
    private EditText remark;
    private TextView save;
    private TextView cancel;
    private AutoCompleteTextView autoCompleteTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        dbManager = new DBManager(this);
        name = (EditText)this.findViewById(R.id.itemName);
        name.requestFocus();
        money = (EditText)this.findViewById(R.id.itemMoney);
        remark = (EditText)this.findViewById(R.id.itemRemark);
        save = (TextView)this.findViewById(R.id.btnSave);
        cancel = (TextView)this.findViewById(R.id.btnCancel);
        TextView title = (TextView)this.findViewById(R.id.edit_title);
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.subject);
        initSubjects();
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");
        int type = intent.getIntExtra("type", 0);
        String typename = "记账";
        if(type==1){
            typename = "预算";
        }
        if(itemId!=null){
            title.setText("修改"+typename);
            initEdit(itemId);
        }else{
            title.setText("新增"+typename);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid()){
                    Intent intent = getIntent();
                    String itemId = intent.getStringExtra("itemId");
                    int type = intent.getIntExtra("type", 0);
                    if(itemId==null){
                        doAdd(type);
                    }else{
                        doEdit(itemId,type);
                    }
                }else{
                    showErrorMassage();
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

    private void initSubjects(){
        String subjects[] = dbManager.getSubjects();
        MyArrayAdapter<String> adapter = new MyArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, subjects);
        autoCompleteTextView.setAdapter(adapter);
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

    private void showErrorMassage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditItem.this);
        builder.setMessage("项目名称,金额和类别不能为空?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void initEdit(String id){
        Item item = dbManager.get(id);
        if(item!=null){
            name.setText(item.getName());
            money.setText(String.valueOf(item.getPrice()));
            remark.setText(item.getRemark());
            autoCompleteTextView.setText(item.getSubject());
        }
    }

    private boolean isValid(){
        return !isEmpty(name.getText().toString())&&!isEmpty(money.getText().toString())&&!isEmpty(autoCompleteTextView.getText().toString());
    }

    private boolean isEmpty(final String s){
        return s==null||"".equals(s);
    }

    private void doAdd(int type){
        Item item = new Item();
        item.setAddDate(DateUtil.getCurrentDate());
        item.setName(name.getText().toString());
        item.setPrice(Double.parseDouble(money.getText().toString()));
        item.setRemark(remark.getText().toString());
        item.setType(type);
        item.setSubject(autoCompleteTextView.getText().toString());
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
        item.setSubject(autoCompleteTextView.getText().toString());
        dbManager.update(item);
        returnMainView();
    }

    private void returnMainView(){
        Intent intent = new Intent(EditItem.this,MainActivity.class);
        Intent thisItement = getIntent();
        int type = thisItement.getIntExtra("type",0);
        intent.putExtra("type", type);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }
}

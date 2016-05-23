package com.example.cherry.myapplication;

import java.util.ArrayList;
import java.util.List;

public class ExpandableItem {
    private String subject;
    private Item parent;
    private List<Item> children = new ArrayList<Item>();

    public ExpandableItem(){}

    public ExpandableItem(String subject){
        this.subject = subject;
    }

    public void addChild(Item item){
        children.add(item);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Item getParent() {
        if(parent==null){
            parent = createParent();
        }
        return parent;
    }

    private Item createParent(){
        Item item = new Item();
        item.setName(children.get(0).getSubject());
        item.setSubject(item.getName());
        item.setAttention(children.get(0).getAttention());
        for(Item child : children){
            item.setPrice(item.getPrice()+child.getPrice());
        }
        return item;
    }

    public void setParent(Item parent) {
        this.parent = parent;
    }

    public List<Item> getChildren() {
        return children;
    }

    public void setChildren(List<Item> children) {
        this.children = children;
    }
}

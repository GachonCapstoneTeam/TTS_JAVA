package com.example.myapplication.item;

public class Item {
    private String title;
    private int id;

    public Item(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
}

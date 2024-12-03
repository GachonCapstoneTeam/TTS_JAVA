package com.example.myapplication.item;

public class Item {
    private String stockName;   // 종목명
    private String stockTitle;  // 제목
    private String bank;        // 증권사
    private String script;      // 스크립트
    private int id;             // 고유 ID

    public Item(String stockName, String stockTitle, String bank, String script, int id) {
        this.stockName = stockName;
        this.stockTitle = stockTitle;
        this.bank = bank;
        this.script = script;
        this.id = id;
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockTitle() {
        return stockTitle;
    }

    public String getBank() {
        return bank;
    }

    public String getScript() {
        return script;
    }

    public int getId() {
        return id;
    }
}

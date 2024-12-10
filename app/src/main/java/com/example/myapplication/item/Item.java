package com.example.myapplication.item;
public class Item {
    private String stockName;   // 종목명
    private String stockTitle;  // 제목
    private String bank;        // 증권사
    private String script;      // 스크립트
    private int views;          // 조회수
    private String date;        // 업로드 날짜
    private String PDF_URL;     // PDF URL

    public Item(String stockName, String stockTitle, String bank, String script, int views, String date, String PDF_URL) {
        this.stockName = stockName;
        this.stockTitle = stockTitle;
        this.bank = bank;
        this.script = script;
        this.views = views;
        this.date = date;
        this.PDF_URL = PDF_URL;
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

    public int getViews() {
        return views;
    }

    public String getDate() {
        return date;
    }

    public String getPDF_URL() {
        return PDF_URL;
    }
}

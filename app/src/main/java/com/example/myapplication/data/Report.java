package com.example.myapplication.data;

public class Report {
    private String stockName;
    private String stockTitle;
    private String bank;
    private String script;

    public Report(String stockName, String stockTitle, String bank, String script) {
        this.stockName = stockName;
        this.stockTitle = stockTitle;
        this.bank = bank;
        this.script = script;
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
}

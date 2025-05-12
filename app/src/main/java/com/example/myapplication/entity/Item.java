package com.example.myapplication.entity;

import com.google.gson.annotations.SerializedName;

// Item 클래스에 getPdfUrl() 메서드 추가
public class Item {
    @SerializedName("Category")
    private String category;

    @SerializedName("Title")
    private String title;

    @SerializedName("증권사")
    private String stockName;  // company -> stockName

    @SerializedName("PDF URL")
    private String pdfUrl;  // pdfUrl 필드 추가

    @SerializedName("작성일")
    private String date;

    @SerializedName("Views")
    private String views;

    @SerializedName("Content")
    private String content;

    @SerializedName("PDF Content")
    private String pdfcontent;
    @SerializedName("Liked")
    private boolean liked;
    // 생성자
    public Item(String category, String title, String stockName, String content, String views, String date, String pdfUrl, String pdfContent) {
        this.category = category;
        this.title = title;
        this.stockName = stockName;
        this.pdfUrl = pdfUrl;
        this.date = date;
        this.views = views;
        this.content = content;
        this.pdfcontent = pdfContent;
    }

    // Getter 메서드 추가
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getStockName() { return stockName; }
    public String getPdfUrl() { return pdfUrl; }  // getPdfUrl() 메서드 추가
    public String getDate() { return date; }
    public String getViews() { return views; }
    public String getContent() { return content; }
    public String getPdfcontent() { return pdfcontent; }

    public boolean isLiked() { return liked; }

    public void setLiked(boolean liked) { this.liked = liked; }
}

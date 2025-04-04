package com.example.myapplication.view;

public class Item {
    private String category;    // 카테고리
    private String title;       // 제목
    private String bank;        // 증권사
    private String content;     // 본문 내용
    private int views;          // 조회수
    private String date;        // 작성일
    private String pdfUrl;      // PDF URL
    private String pdfcontent;  // PDF content
    private boolean liked;      // 관심 버튼

    public Item(String category, String title, String bank, String content, int views, String date, String pdfUrl, String pdfcontent) {
        this.category = category;
        this.title = title;
        this.bank = bank;
        this.content = content;
        this.views = views;
        this.date = date;
        this.pdfUrl = pdfUrl;
        this.pdfcontent = pdfcontent;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getBank() {
        return bank;
    }

    public String getContent() {
        return content;
    }

    public int getViews() {
        return views;
    }

    public String getDate() {
        return date;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public String getPdfcontent() {
        return pdfcontent;
    }

    public boolean isLiked() { return liked; }

    public void setLiked(boolean liked) { this.liked = liked; }
}

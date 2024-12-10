package com.example.myapplication.service;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/api/items")
    Call<Map<String, List<Map<String, String>>>> getContents(@Query("page") int page);
}
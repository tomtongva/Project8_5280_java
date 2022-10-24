package com.example.itis5280_project8.apicalls;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface RetrofitInterface {
    @GET("/api/getItems")
    Call<ItemResponse> getItems(@Header("region") String region);
}

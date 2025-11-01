package com.example.app2.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query; // [추가]

public interface ApiService {

    @GET("healthz")
    Call<JsonObject> healthz();

    // --- Auth ---
    @POST("auth/register-creds")
    Call<JsonObject> sendRegisterCreds(@Body JsonObject body);

    @POST("auth/register")
    Call<JsonObject> register(@Body JsonObject body);

    @POST("auth/login")
    Call<JsonObject> login(@Body JsonObject body);

    // --- Me ---
    @GET("users/me")
    Call<JsonObject> getMe();

    // --- Constitution Analyze ---
    @POST("constitution/analyze")
    Call<JsonObject> analyze(@Body JsonObject body);

    // --- [추가] 식단 기록 API (Notion 기준) ---
    @POST("diet")
    Call<JsonObject> addDietLog(@Body JsonObject body);

    // --- [추가] 운동 기록 API (Notion 기준) ---
    @POST("exercise")
    Call<JsonObject> addExerciseLog(@Body JsonObject body);

    @GET("home")
    Call<JsonObject> getHomeData();


}
package com.example.app2.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import com.example.app2.models.LoginResponse;
import com.example.app2.models.LoginRequest;
public interface ApiService {
    @GET("healthz")
    Call<JsonObject> healthz();

    @POST("auth/register-creds")
    Call<JsonObject> sendRegisterCreds(@Body JsonObject body); // username, password, email

    @POST("auth/register")
    Call<JsonObject> register(@Body JsonObject body); // email, auth_code, name, birth_year, gender

    @POST("auth/login")
    Call<JsonObject> login(@Body JsonObject body); // username, password

    @GET("users/me")
    Call<JsonObject> me(@Header("Authorization") String bearer);

    @POST("constitution/analyze")
    Call<JsonObject> analyze(@Header("Authorization") String bearer,
                             @Body JsonObject body); // userId, userInfo, answers
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("constitution/analyze")
    retrofit2.Call<com.example.app2.models.AnalyzeResponse> analyze(@Body com.example.app2.models.AnalyzeRequest body);

}

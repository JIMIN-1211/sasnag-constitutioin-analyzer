package com.example.app2.api;

import com.google.gson.JsonObject;
import com.example.app2.models.AdviceResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("users/me")
    Call<JsonObject> getMyInfo();

    @GET("me/constitution/latest")
    Call<JsonObject> getLatestConstitution();

    @POST("constitution/analyze")
    Call<JsonObject> analyze(@Body JsonObject body);

    @GET("constitution/advice")
    Call<AdviceResponse> getAdviceByType(@Query("type") String type);

    @GET("home")
    Call<JsonObject> getHomeData(@Query("date") String date);

    @POST("diet")
    Call<JsonObject> postMeal(@Body JsonObject mealRequest);

    @GET("diet")
    Call<JsonObject> getDietList(@Query("date") String date);

    @DELETE("diet/{mealId}")
    Call<JsonObject> deleteMeal(@Path("mealId") long mealId);

    @POST("exercise")
    Call<JsonObject> postExercise(@Body JsonObject exerciseRequest);

    @GET("exercise")
    Call<JsonObject> getWorkoutList(@Query("date") String date);

    @DELETE("exercise/{workoutId}")
    Call<JsonObject> deleteWorkout(@Path("workoutId") long workoutId);
}
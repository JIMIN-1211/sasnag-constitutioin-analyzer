package com.example.app2.api;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/v1/"; // emulator -> host

    private static volatile Retrofit retrofit;
    private static volatile ApiService api;

    private ApiClient() {}

    public static ApiService getApiService() {
        if (api == null) {
            synchronized (ApiClient.class) {
                if (api == null) {
                    retrofit = buildRetrofit(BASE_URL);
                    api = retrofit.create(ApiService.class);
                }
            }
        }
        return api;
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        Gson gson = new GsonBuilder().setLenient().create();

        OkHttpClient client = buildHttpClient();

        return new Retrofit.Builder()
                .baseUrl(ensureEndsWithSlash(baseUrl))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static OkHttpClient buildHttpClient() {
        // 임시: BuildConfig 없이 로깅 On/Off
        final boolean DEBUG = true;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logging.setLevel(DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        Interceptor defaultHeaders = chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder()
                    .header("Accept", "application/json");
            if (original.body() != null) {
                b.header("Content-Type", "application/json");
            }
            return chain.proceed(b.build());
        };

        return new OkHttpClient.Builder()
                .addInterceptor(defaultHeaders)
                .addInterceptor(logging)
                .connectTimeout(15, SECONDS)
                .readTimeout(15, SECONDS)
                .writeTimeout(15, SECONDS)
                .build();
    }

    private static String ensureEndsWithSlash(String url) {
        return url.endsWith("/") ? url : (url + "/");
    }
}

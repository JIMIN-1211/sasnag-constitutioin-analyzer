package com.example.app2.api;

import android.content.Context;

import com.example.app2.auth.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 클라이언트(싱글턴)
 * - BASE_URL 만 너 환경에 맞게 바꿔줘.
 * - Authorization: Bearer 자동첨부
 * - Logging 포함
 */
public final class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/v1/";

    private static volatile Retrofit retrofit;
    private static volatile ApiService api;

    private ApiClient() {}

    public static void init(Context appContext) {
        // 토큰 캐시 선로딩 (선택)
        TokenManager.init(appContext);
    }

    public static ApiService getApiService(Context context) {
        if (api == null) {
            synchronized (ApiClient.class) {
                if (api == null) {
                    retrofit = buildRetrofit(context, ensureEndsWithSlash(BASE_URL));
                    api = retrofit.create(ApiService.class);
                }
            }
        }
        return api;
    }

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit(context, ensureEndsWithSlash(BASE_URL));
                }
            }
        }
        return retrofit;
    }

    private static Retrofit buildRetrofit(Context context, String baseUrl) {
        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient client = buildHttpClient(context);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static OkHttpClient buildHttpClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Authorization 헤더 자동 첨부
        Interceptor auth = chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            String bearer = TokenManager.getBearer(context);
            if (bearer != null && !bearer.isEmpty()) {
                b.header("Authorization", bearer);
            }

            // (선택) 모든 요청에 공통 쿼리/헤더 필요 시 여기서 추가
            HttpUrl url = original.url().newBuilder().build();
            b.url(url);

            return chain.proceed(b.build());
        };

        return new OkHttpClient.Builder()
                .addInterceptor(auth)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    private static String ensureEndsWithSlash(String url) {
        return url.endsWith("/") ? url : (url + "/");
    }
}

package com.example.app2.api;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.util.Log;

import com.example.app2.auth.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    // emulator -> host
    private static final String BASE_URL = "http://10.0.2.2:3000/v1/";

    private static volatile Retrofit retrofit;
    private static volatile ApiService api;
    private static volatile Context appContext; // Authorization 인터셉터에서 사용

    private ApiClient() {}

    /** 반드시 앱 시작 후 한 번 호출 (예: LoginActivity, MainActivity 등 onCreate에서) */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            TokenManager.init(appContext); // 캐시 로드
        }
    }

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

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = buildRetrofit(BASE_URL);
                }
            }
        }
        return retrofit;
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
        final boolean DEBUG = true;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
        logging.setLevel(DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        // 공통 헤더
        Interceptor defaultHeaders = chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder()
                    .header("Accept", "application/json");
            if (original.body() != null) {
                b.header("Content-Type", "application/json");
            }
            return chain.proceed(b.build());
        };

        // Authorization 자동 첨부 (auth 엔드포인트는 제외)
        Interceptor authHeader = chain -> {
            Request original = chain.request();
            String path = original.url().encodedPath(); // 예: /v1/auth/login

            boolean isAuthEndpoint = (path.contains("/v1/auth/"));

            Request.Builder b = original.newBuilder();

            if (!isAuthEndpoint && appContext != null) {
                String raw = TokenManager.getRawToken(appContext);
                Log.d("ApiClientAuth", "Token retrieved for " + path + ": " + raw); // <-- 로그 추가
                if (raw != null && !raw.isEmpty()) {
                    String authValue = "Bearer " + raw;
                    Log.d("ApiClientAuth", "Adding header: Authorization: " + authValue); // <-- 로그 추가
                    b.header("Authorization", authValue);
                } else {
                    Log.w("ApiClientAuth", "Token is null or empty for non-auth endpoint!"); // <-- 로그 추가
                }
            }            return chain.proceed(b.build());
        };

        return new OkHttpClient.Builder()
                .addInterceptor(defaultHeaders)
                .addInterceptor(authHeader)
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

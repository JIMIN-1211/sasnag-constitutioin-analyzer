package com.example.app2.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF = "auth_pref";
    private static final String KEY_TOKEN = "jwt";      // raw JWT
    private static final String KEY_USER_ID = "user_id";

    private static volatile String cachedToken; // 메모리 캐시(인터셉터에서 즉시 접근)
    private static volatile Integer cachedUserId;

    /** 앱 시작 시(또는 ApiClient.init 전에) 한 번 불러주면 캐시 채움 */
    public static void init(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        cachedToken = sp.getString(KEY_TOKEN, null);
        if (sp.contains(KEY_USER_ID)) {
            cachedUserId = sp.getInt(KEY_USER_ID, 0);
        }
    }

    public static void saveToken(Context ctx, String rawJwt) {
        if (rawJwt != null) rawJwt = rawJwt.trim();
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY_TOKEN, rawJwt).apply();
        cachedToken = rawJwt;
    }

    public static String getRawToken(Context ctx) {
        if (cachedToken != null) return cachedToken;
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        cachedToken = sp.getString(KEY_TOKEN, null);
        return cachedToken;
    }

    /** "Bearer xxx" 형식으로 반환 (없으면 null) */
    public static String getBearer(Context ctx) {
        String raw = getRawToken(ctx);
        return (raw == null || raw.isEmpty()) ? null : ("Bearer " + raw);
    }

    public static void saveUserId(Context ctx, int userId) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putInt(KEY_USER_ID, userId).apply();
        cachedUserId = userId;
    }

    public static int getUserId(Context ctx) {
        if (cachedUserId != null) return cachedUserId;
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        int v = sp.getInt(KEY_USER_ID, 0);
        cachedUserId = v;
        return v;
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
        cachedToken = null;
        cachedUserId = null;
    }
}

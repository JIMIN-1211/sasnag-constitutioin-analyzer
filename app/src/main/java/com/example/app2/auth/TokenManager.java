package com.example.app2.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF = "auth_pref";
    private static final String KEY_TOKEN = "jwt";

    public static void saveToken(Context ctx, String token) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}

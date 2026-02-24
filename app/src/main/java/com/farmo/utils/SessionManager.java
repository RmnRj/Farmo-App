package com.farmo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FarmoSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Saves session data. If keepLogin is false, it stores empty strings
     * to ensure no sensitive data persists beyond the current session.
     */
//    public void saveSession(String userId, String userType, String token, String refreshToken, boolean keepLogin) {
//        if (keepLogin) {
//            editor.putString(KEY_USER_ID, userId);
//            editor.putString(KEY_USER_TYPE, userType);
//            editor.putString(KEY_AUTH_TOKEN, token);
//            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
//            editor.putBoolean(KEY_IS_LOGGED_IN, true);
//        } else {
//            // If keep login is not checked, we store empty/null
//            editor.putString(KEY_USER_ID, "");
//            editor.putString(KEY_USER_TYPE, "");
//            editor.putString(KEY_AUTH_TOKEN, "");
//            editor.putString(KEY_REFRESH_TOKEN, "");
//            editor.putBoolean(KEY_IS_LOGGED_IN, false);
//        }
//        editor.apply();
//    }
    public void saveSession(String userId, String userType, String token,
                            String refreshToken, boolean keepLogin) {
        // Always save the data regardless of keepLogin
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // ← Always true after login
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, "");
    }

    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, "");
    }

    public String getRefreshToken() {
        return pref.getString(KEY_REFRESH_TOKEN, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}

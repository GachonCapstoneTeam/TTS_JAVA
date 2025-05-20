package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class PreferenceUtil {

    private static final String PREF_NAME = "talkstock_prefs";
    private static SharedPreferences prefs;

    public static final String KEY_AUTO_LOGIN = "auto_login";
    public static final String KEY_LAST_USER = "last_user_email";

    // 초기화 (앱 시작 시 1회만 호출)
    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // 문자열 저장
    public static void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    // 문자열 불러오기
    public static String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    // boolean 저장 (예: 자동 로그인 여부)
    public static void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    // boolean 불러오기
    public static boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    // key 삭제
    public static void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    // 전체 삭제
    public static void clear() {
        prefs.edit().clear().apply();
    }

    public static Map<String, ?> getAll() {
        return prefs.getAll();
    }

}

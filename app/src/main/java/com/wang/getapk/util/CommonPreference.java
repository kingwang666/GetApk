package com.wang.getapk.util;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wang on 12/23/15.通用存储类
 */
public class CommonPreference {

    private static String NAME = "common_pref";


    public static void putString(Context context, String key, String value) {
        if (context == null) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        pref.edit().putString(key, value).apply();
    }

    public static void putInt(Context context, String key, int value) {
        if (context == null) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(key, value).apply();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        if (context == null) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(key, defValue);
    }

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }

    public static String getString(Context context, String key, String defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return pref.getString(key, defValue);
    }

    public static int getInt(Context context, String key) {
        return getInt(context, key, 0);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return pref.getInt(key, defaultValue);
    }

    public static boolean exist(Context context, String key) {
        if (context == null) {
            return false;
        }

        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return pref.contains(key);
    }

    public static void clear(Context context) {
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }

    public static void remove(Context context, String... keys) {
        SharedPreferences pref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        for (String key : keys){
            edit.remove(key);
        }
        edit.apply();
    }
}

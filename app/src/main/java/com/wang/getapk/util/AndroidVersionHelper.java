package com.wang.getapk.util;

import android.os.Build;

import java.util.HashMap;

/**
 * Created by wangxiaojie on 2023/8/23.
 */
public class AndroidVersionHelper {
    private static final HashMap<Integer, String> API_LEVELS = new HashMap<>();

    static {
        API_LEVELS.put(Build.VERSION_CODES.BASE, "Android 1.0");
        API_LEVELS.put(Build.VERSION_CODES.BASE_1_1, "Android 1.1");
        API_LEVELS.put(Build.VERSION_CODES.CUPCAKE, "Android 1.5 Cupcake");
        API_LEVELS.put(Build.VERSION_CODES.DONUT, "Android 1.6 Donut");
        API_LEVELS.put(Build.VERSION_CODES.ECLAIR, "Android 2.0 Eclair");
        API_LEVELS.put(Build.VERSION_CODES.ECLAIR_0_1, "Android 2.0.1 Eclair");
        API_LEVELS.put(Build.VERSION_CODES.ECLAIR_MR1, "Android 2.1 Eclair");
        API_LEVELS.put(Build.VERSION_CODES.FROYO, "Android 2.2 Froyo");
        API_LEVELS.put(Build.VERSION_CODES.GINGERBREAD, "Android 2.3 Gingerbread");
        API_LEVELS.put(Build.VERSION_CODES.GINGERBREAD_MR1, "Android 2.3.3 Gingerbread");
        API_LEVELS.put(Build.VERSION_CODES.HONEYCOMB, "Android 3.0 Honeycomb");
        API_LEVELS.put(Build.VERSION_CODES.HONEYCOMB_MR1, "Android 3.1 Honeycomb");
        API_LEVELS.put(Build.VERSION_CODES.HONEYCOMB_MR2, "Android 3.2 Honeycomb");
        API_LEVELS.put(Build.VERSION_CODES.ICE_CREAM_SANDWICH, "Android 4.0 Ice Cream Sandwich");
        API_LEVELS.put(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1, "Android 4.0.3 Ice Cream Sandwich");
        API_LEVELS.put(Build.VERSION_CODES.JELLY_BEAN, "Android 4.1 Jelly Bean");
        API_LEVELS.put(Build.VERSION_CODES.JELLY_BEAN_MR1, "Android 4.2 Jelly Bean");
        API_LEVELS.put(Build.VERSION_CODES.JELLY_BEAN_MR2, "Android 4.3 Jelly Bean");
        API_LEVELS.put(Build.VERSION_CODES.KITKAT, "Android 4.4 KitKat");
        API_LEVELS.put(Build.VERSION_CODES.KITKAT_WATCH, "Android 4.4W KitKat Watch");
        API_LEVELS.put(Build.VERSION_CODES.LOLLIPOP, "Android 5.0 Lollipop");
        API_LEVELS.put(Build.VERSION_CODES.LOLLIPOP_MR1, "Android 5.1 Lollipop");
        API_LEVELS.put(Build.VERSION_CODES.M, "Android 6.0 Marshmallow");
        API_LEVELS.put(Build.VERSION_CODES.N, "Android 7.0 Nougat");
        API_LEVELS.put(Build.VERSION_CODES.N_MR1, "Android 7.1 Nougat");
        API_LEVELS.put(Build.VERSION_CODES.O, "Android 8.0 Oreo");
        API_LEVELS.put(Build.VERSION_CODES.O_MR1, "Android 8.1 Oreo");
        API_LEVELS.put(Build.VERSION_CODES.P, "Android 9 Pie");
        API_LEVELS.put(Build.VERSION_CODES.Q, "Android 10 Quince Tart");
        API_LEVELS.put(Build.VERSION_CODES.R, "Android 11 Red Velvet Cake");
        API_LEVELS.put(Build.VERSION_CODES.S, "Android 12 Snow Cone");
        API_LEVELS.put(Build.VERSION_CODES.S_V2, "Android 12L Snow Cone");
        API_LEVELS.put(Build.VERSION_CODES.TIRAMISU, "Android 13 Tiramisu");
        // TODO: wangxiaojie 2023/8/23
        API_LEVELS.put(34, "Android 14 Upside Down Cake");
    }

    public static String getName(int apiVersion) {
        return API_LEVELS.get(apiVersion);
    }
}

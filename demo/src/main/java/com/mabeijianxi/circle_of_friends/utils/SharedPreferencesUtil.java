package com.mabeijianxi.circle_of_friends.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by mabeijianxi on 2011/1/13.
 */
public class SharedPreferencesUtil {
    private static SharedPreferences sharedPreferences;
    private static String CONFIG = "config";
    public static void saveStringData(Context context, String key, String value) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG,
                    Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(key, value).commit();
    }


    public static String getStringData(Context context, String key,
                                       String defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG,
                    Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key, defValue);
    }
}

package com.mabeijianxi.circle_of_friends.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.mabeijianxi.circle_of_friends.bean.BaseDataBean;

import org.json.JSONObject;

public class JsonUtil {

    /**
     * @param json
     * @param clazz
     * @return
     */
    public static <T> BaseDataBean<T> parseDataObject(String json,
                                                      Class<T> clazz) {
        BaseDataBean<T> baseBean = new BaseDataBean<T>();
        try {
            JSONObject jsonTree = new JSONObject(json);
//            JSONObject jsonTree = JSONObject.parseObject(json);
            try {
                baseBean.code = jsonTree.getInt("code");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                baseBean.msg = jsonTree.getString("msg");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                baseBean.now = jsonTree.getLong("now");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                baseBean.success = jsonTree.getBoolean("success");
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = jsonTree.getJSONObject("data");
            String list = jsonObject.toString();
            if (!TextUtils.isEmpty(list)) {
                Gson gson = new Gson();
                T parseObject = gson.fromJson(list, clazz);
                baseBean.data = parseObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baseBean;
    }
}
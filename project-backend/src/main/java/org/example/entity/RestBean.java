package org.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

// 响应实体类
public record RestBean<T>(int code, T data, String massage) {
    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200,data,"请求成功");
    }

    // 成功
    public static <T> RestBean<T> success(){
        return success(null);
    }


    public static <T> RestBean<T> unauthorized(String massage){
        return failure(401,massage);
    }
    // 失败
    public static <T> RestBean<T> failure(int code, String massage){
        return new RestBean<>(code,null,massage);
    }

    public static <T> RestBean<T> forbidden(String massage){
        return failure(403,massage);
    }

    // 使用JSON形式返回
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}

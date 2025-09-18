package com.sample;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class HashMapToJsonExample {
    public static void main(String[] args) {
        // HashMap 생성
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");
        user.put("age", 25);
        user.put("isMember", true);

        // HashMap 안에 또 다른 HashMap 넣기 (중첩 JSON)
        Map<String, String> address = new HashMap<>();
        address.put("city", "Seoul");
        address.put("zip", "12345");
        user.put("address", address);

        // HashMap -> JSON 변환
        Gson gson = new Gson();
        String jsonString = gson.toJson(user);

        // 출력
        System.out.println("JSON 출력:");
        System.out.println(jsonString);
    }
}

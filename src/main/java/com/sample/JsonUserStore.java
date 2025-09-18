package com.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUserStore {

    private final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    /** JSON 파일에서 사용자 맵 로드 (없으면 빈 맵 유지) */
    public void load(Path jsonPath) throws IOException {
        if (!Files.exists(jsonPath)) return;
        try (BufferedReader br = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8)) {
            Map<String, String> loaded = GSON.fromJson(br, MAP_TYPE);
            users.clear();
            if (loaded != null) users.putAll(loaded);
        }
    }

    /** 사용자 맵을 JSON 파일로 저장 (원자적 쓰기) */
    public void save(Path jsonPath) throws IOException {
        Path tmp = jsonPath.resolveSibling(jsonPath.getFileName() + ".tmp");
        Files.createDirectories(jsonPath.getParent() == null ? Paths.get(".") : jsonPath.getParent());

        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(users, bw);
        }
        Files.move(tmp, jsonPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /** 인증 */
    public boolean authenticate(String id, String pw) {
        String stored = users.get(id);
        return stored != null && stored.equals(pw);
    }

    /** 사용자 추가/업데이트 (이미 있으면 비번 갱신) */
    public void upsert(String id, String pw) {
        users.put(id, pw);
    }

    /** 사용자 삭제 */
    public boolean remove(String id) {
        return users.remove(id) != null;
    }

    /** 존재 여부 */
    public boolean exists(String id) {
        return users.containsKey(id);
    }

    /** 전체 조회 (읽기용 복사본) */
    public Map<String, String> snapshot() {
        return Map.copyOf(users);
    }
}

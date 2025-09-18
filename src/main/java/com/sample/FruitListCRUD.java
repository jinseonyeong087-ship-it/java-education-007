package com.sample;

import java.util.ArrayList;
import java.util.List;

public class FruitListCRUD {
    public static void main(String[] args) {
        // ✅ 1) 리스트 생성
        List<String> fruits = new ArrayList<>();

        // ──────── C (Create) : 추가 ────────
        fruits.add("사과");
        fruits.add("바나나");
        fruits.add("포도");
        System.out.println("추가 후: " + fruits); // [사과, 바나나, 포도]

        // ──────── R (Read) : 조회 ────────
        System.out.println("0번 인덱스: " + fruits.get(0)); // 사과
        System.out.println("포함 여부(바나나): " + fruits.contains("바나나")); // true
        System.out.println("전체 과일: " + fruits); // [사과, 바나나, 포도]

        // ──────── U (Update) : 수정 ────────
        fruits.set(1, "오렌지"); // 1번 인덱스의 바나나 → 오렌지
        System.out.println("수정 후: " + fruits); // [사과, 오렌지, 포도]

        // ──────── D (Delete) : 삭제 ────────
        fruits.remove(0); // 인덱스로 삭제 → 사과 제거
        fruits.remove("포도"); // 값으로 삭제 → 포도 제거
        System.out.println("삭제 후: " + fruits); // [오렌지]

        // 리스트 전체 비우기
        fruits.clear();
        System.out.println("전체 삭제 후 비었나? " + fruits.isEmpty()); // true
    }
}

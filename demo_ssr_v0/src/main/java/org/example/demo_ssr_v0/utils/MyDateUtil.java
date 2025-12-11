package org.example.demo_ssr_v0.utils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class MyDateUtil {

    // 정적 메서드 (기능) 시간 포맷터
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String timestamp(Timestamp timestamp) {
        // timestamp 받아서 원하는 형태로 변환
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().format(FORMATTER);

    }
}

package org.example.demo_ssr_v0._core.utils;

import java.util.Random;

public class MailUtils {
    // 정적 메서드로 랜덤 번호 6자리 생성하는 헬프 메서드
    public static String generateRandomCode() {
        Random random = new Random();
        // 0 ~ 899999 (하나의 랜덤 숫자 생성)
        // 1. 0 발생 가능
        // 2. 1 + 100_000 = 100_001
        // 3. 반드시 여섯자리 번호를 생성해야함
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code); // int -> String
    }
}

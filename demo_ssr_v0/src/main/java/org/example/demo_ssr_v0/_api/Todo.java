package org.example.demo_ssr_v0._api;

import lombok.Data;

// 응답 Todo에 대한 DTO 설계
@Data
public class Todo {
    // { "userId": 1, "id": 1, "title": "delectus aut autem", "completed": false }
    // JSON 형식은 Key값에 쌍따옴표 필수, 마지막에 , (콤마)있으면 오류남
    private Integer userId;
    private Integer id;
    private String title;
    private boolean completed;
}

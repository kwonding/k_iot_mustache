package org.example.demo_ssr_v0.user;

import lombok.Data;

public class UserRequest {

    @Data // 필드가 private 이라서 다른 곳에서 쓸 수 있게 getter, setter 걸어줌
    public static class LoginDTO {
        private String username;
        private String password;

        // 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명을 입력해주세요.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요");
            }
        }
    } // end of inner class

    @Data
    public static class JoinDTO {
        private String username;
        private String password;
        private String email;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명을 입력해주세요.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일을 입력해주세요");
            }
            if (email.contains("@") == false) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
            }
        }

        // JoinDTO를 User 타입으로 변환시키는 기능 (DB에 저장할 때는 User로 들어가니까)
        // 빌더 어노테이션 사용
        public User toEntity() {
            return User.builder() // return new User();
                    .username(this.username)
                    .password(this.password)
                    .email(this.email)
                    .build();
        }

    } // end of inner class

    @Data
    public static class UpdateDTO {
        private String password;
        // username은 제외: 변경 불가능한 고유 식별자

        public void validate() {
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요");
            }
            if (password.length() < 4) {
                throw new IllegalArgumentException("비밀번호는 4글자 이상이어야 합니다.");
            }
        }

    }

}

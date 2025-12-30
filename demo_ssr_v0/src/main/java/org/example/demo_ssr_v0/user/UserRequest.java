package org.example.demo_ssr_v0.user;

import lombok.Data;
import org.example.demo_ssr_v0._core.errors.exception.Exception400;
import org.springframework.web.multipart.MultipartFile;

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
        // MultipartFile - Spring 에서 파일 업로드를 처리하기 위한 인터페이스
        // 우리 프로젝트에서는 선택 사항이라 회원 가입시 null 또는 empty 상태가 될 수 있음
        private MultipartFile profileImage;

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
        public User toEntity(String profileImageFileName) {
            return User.builder() // return new User();
                    .username(this.username)
                    .password(this.password)
                    .email(this.email)
                    // DB에 MultipartFile을 저장할 수 없다 (파일 이름만 저장할 예정)
                    // this.profileImage는 MultipartFile 타입임
                    .profileImage(profileImageFileName)
                    .build();
        }

    } // end of inner class

    @Data
    public static class UpdateDTO {
        private String password;
        private String email;
        private MultipartFile profileImage; // mustache 파일의 form에 name과 동일해야 함
        private String profileImageFilename; // 추후 user.update 메서드에서 사용 함
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

    @Data
    public static class EmailCheckDTO {
        private String email;
        private String code;

        public void validate() {
            if (email == null || email.isEmpty()) {
                // Exception400 추후 수정
                throw new Exception400("이메일을 입력해주세요");
            }
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }
    }

    @Data
    public static class PointChargeDTO {
        private Integer amount;

        public void validate() {
            if (amount == null || amount <= 0) {
                throw new Exception400("충전할 포인트는 0보다 작을 수 없습니다.");
            }
        }
    }

}

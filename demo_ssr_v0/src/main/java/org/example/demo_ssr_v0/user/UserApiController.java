package org.example.demo_ssr_v0.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController // @Controller + @ResponseBody, json 형식으로 내려줘야하니까
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final MailService mailService;

    @PostMapping("/api/email/send")
    public ResponseEntity<?> 인증번호발송(@RequestBody UserRequest.EmailCheckDTO reqDTO) {
        // 1. 유효성 검사
        reqDTO.validate();

        // 2. 서비스 단에서 구글 메일 서버로 이메일 전송 처리
        mailService.인증번호발송(reqDTO.getEmail());

        return ResponseEntity.ok().body(Map.of("message", "인증번호가 발송되었습니다."));
    }
}

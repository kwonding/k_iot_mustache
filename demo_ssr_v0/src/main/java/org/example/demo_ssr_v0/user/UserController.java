package org.example.demo_ssr_v0.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 사용자 Controller (표현 계층)
 * 핵심 개념 :
 * - Http 요청을 받아서 처리
 * - 요청 데이터 검증 및 파라미터 바인딩 처리
 * - Service 레이어에 비즈니스 로직을 위임
 * - 응답 데이터를 View에 전달 함 or JSON 으로 던지기
 */

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    // 프로필 이미지 삭제하기
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        User updateUser = userService.프로필이미지삭제(sessionUser.getId());
        // 왜 user를 다시 받을까? - 세션 정보가 (즉 프로필이 삭제 되었기 때문에)
        // 세션 정보 갱신 처리해주기 위함
        session.setAttribute("sessionUser", updateUser); // 세션 정보 갱신

        // 일반적으로 POST 요청오면 PRG 패턴으로 설계 됨
        // POST -> Redirect 처리 --> GET 요청
        return "redirect:/user/detail";
    }

    // 마이페이지
    // http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detailForm(Model model, HttpSession session) {
        User sessionUser = (User)session.getAttribute("sessionUser");

        User user = userService.마이페이지(sessionUser.getId());
        model.addAttribute("user", user);

        return "user/detail";
    }

    // 회원 정보 수정 화면 요청
    // http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        // HttpServletRequest
        //  --> A 사용자가 요청 시
        //  --> 웹 서버
        //  --> 톰캣(WAS)이 HttpServletRequest 객체와 Response 객체를 만들어서 스프링 컨테이너에게 전달해줌

        // 1. 인증 검사 (O) - controller
        // 2. 유효성 검사 (X)
        // 인증 검사를 하려면 세션 메모리에 접근해서 사용자의 정보 유무 확인
        User sessionUser = (User)session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        // 2. 인가 처리 - service
        // 세션의 사용자 ID로 회원 정보 조회
        User user = userService.회원정보수정화면(sessionUser.getId());
        model.addAttribute("user", user);

        return "user/update-form";
    }

    // 회원 정보 수정 기능 요청 - 더티 체킹
    // http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String updateProc(UserRequest.UpdateDTO updateDTO, HttpSession session) {
        // 인증 검사
        User sessionUser = (User)session.getAttribute("sessionUser");
        // LoginInterceptor 가 알아서 처리 해줌!

        // 인가 검사 (DB 정보 조회)
        try {
            // 유효성 검사 (형식 검사)
            updateDTO.validate();
            User updateUser = userService.회원정보수정(updateDTO, sessionUser.getId());
            // 회원 정보 수정은 세션 갱신해 주어야 함
            session.setAttribute("sessionUser", updateUser);
            return "redirect:/";
        } catch (Exception e) {
            return "user/update-form";
        }

    }

    // 로그아웃 기능 요청
    // http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // 세션 무효화
        session.invalidate();

        return "redirect:/login";
    }


    // 로그인 화면 요청
    // http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // JWT 토큰 기반 인증 X -> 세션 기반 인증 처리
    // 로그인 기능 요청
    // http://localhost:8080/login
    @PostMapping("/login")
    public String loginProc(UserRequest.LoginDTO loginDTO, HttpSession session) {
        // 1. 인증검사 (X) - 필요없음 (로그인이니까)
        // 2. 유효성 검사
        // 3. DB에 사용자 이름과 비밀번호 확인
        // 4. 로그인 성공 또는 실패 처리
        // 5. 웹 서버는 바보라서 사용자의 정보를 세션 메모리에 저장 시켜야
        //      다음 번 요청이 오더라도 알 수 있음
        try {
            // 유효성 검사
            loginDTO.validate();
            User sessionUser = userService.로그인(loginDTO);
            // 세션에 저장 - 톰캣의 세션메모리
            session.setAttribute("sessionUser", sessionUser);
            return "redirect:/";

        } catch (Exception e) {
            // 로그인 실패시 다시 로그인 화면으로 처리
            return "user/login-form";
        }

    }

    // 회원가입 화면 요청
    // http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원가입 기능 요청
    // http://localhost:8080/join
    @PostMapping("/join")
    public String joinProc(UserRequest.JoinDTO joinDTO) {
        // 1. 인증검사 (X) - 필요없음 (회원가입이니까)
        // 2. 유효성 검사 (O) - 엉망인 데이터 저장할 수 없음
        // 3. 사용자 이름 중복 체크
        // 4. 저장 요청
        joinDTO.validate();
        userService.회원가입(joinDTO);

        return "redirect:/login";
    }
}

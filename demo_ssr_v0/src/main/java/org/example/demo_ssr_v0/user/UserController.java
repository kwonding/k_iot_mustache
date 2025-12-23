package org.example.demo_ssr_v0.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

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

    @Value("${oauth.kakao.client-id}") // yml 파일에 있는거 가져오려면 @Value 사용
    private String clientId;

    @Value("${tenco.key}")
    private String tencoKey;

    // todo 테스트용 코드 - 삭제 예정
//    @PostConstruct // 스프링실행시 자동실행되게 하려면 해당 어노테이션 필요
//    public void init() {
//        System.out.println("현재 적용된 카카오 클라이언트 키 확인: " + clientId);
//        System.out.println("현재 적용된 나의 시크릿 키 확인: " + tencoKey);
//    }

    // 로그인 인터셉터에서 여기 못 들어오게 막고 있음! - 인터셉터에서 제외시키기
    // [흐름] 1. 인가코드 받기 -> 2. 토큰(JWT) 발급 요청 -> 3. JWT로 사용자 정보 요청 -> 4. 우리 서버에 로그인/회원가입 처리
    @GetMapping("/user/kakao")
    public String kakaoCallback(@RequestParam(name = "code") String code, HttpSession session) {
        // 1. 인가 코드 받아서 확인
        System.out.println("1. 카카오 인가 코드 확인: " + code);

        // 2. 토큰 발급 요청 (https://kauth.kakao.com/oauth/token - POST)
        // 2-1. HTTP 헤더 커스텀
        // -> Content-Type: application/x-www-form-urlencoded;charset=utf-8

        // 2-2 server to server
        RestTemplate restTemplate = new RestTemplate();

        // 2-3
        // HTTP 메시지 헤더 구성
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 2-4 HTTP 메시지 바디 구성 (POST)
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", "a3473de44036ef36f7e90c6b70a38677"); // clientId 변수명 사용해도됨 위에서 설정했으니까 - "a3473de44036ef36f7e90c6b70a38677"
        tokenParams.add("redirect_uri", "http://localhost:8080/user/kakao");
        tokenParams.add("code", code);
        // 시크릿 키 추가
        tokenParams.add("client_secret", "nRoNjXP2WbOzztYklTGZq8cqvz96RpYx");

        // 2-5 바디 + 헤더 구성(합치기)
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

        // 2-6 요청하고 JWT 토큰 응답 받기 (카카오에서 액세스 토큰이라 부름)
        ResponseEntity<UserResponse.OAuthToken> tokenResponse = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OAuthToken.class); // 파싱 타입

        // JWT 토큰 확인 (액세스 토큰)
        System.out.println(tokenResponse.getHeaders());
        System.out.println(tokenResponse.getBody().getAccessToken());
        System.out.println(tokenResponse.getBody().getExpiresIn());

        /// ///////////////////////////////////////////
        // 3. 액세스 토큰을 받았기 때문에
        // 카카오 자원서버(User 정보 등) 사용자에 대한 정보를 요청할 수 있다.
        /// ///////////////////////////////////////////
        // GET/POST https://kapi.kakao.com/v2/user/me
        // 3-1 HTTP 클라이언트 선언
        RestTemplate profileRT = new RestTemplate();

        // 3-2 HTTP 메시지 헤더 커스텀
        HttpHeaders profileHeaders = new HttpHeaders();
        // Bearer + 공백 한칸 무조건 (안하면 오류 발생)
        profileHeaders.add("Authorization",
                "Bearer " + tokenResponse.getBody().getAccessToken());
        profileHeaders.add("Content-Type",
                "application/x-www-form-urlencoded;charset=utf-8");

        // 3-3 요청 메시지(요청 엔티티) 생성 (요청 바디 없음, 안 만들어도 됨)
        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

        ResponseEntity<UserResponse.KakaoProfile> profileResponse = profileRT.exchange(
                "https://kapi.kakao.com/v2/user/me", // url
                HttpMethod.POST, // 메서드 방식
                profileRequest, // 요청
                UserResponse.KakaoProfile.class // 응답
        );

        // 3-4 사용자 정보 수신 완료
        System.out.println(profileResponse.getBody().getId());
        System.out.println(profileResponse.getBody().getProperties().getNickname());
        System.out.println(profileResponse.getBody().getProperties().getThumbnailImage());

        /// ////////////////////////////////////////////
        // 4. 최초 사용자라면 강제 회원가입 처리 및 로그인 처리
        /// ////////////////////////////////////////////
        // DB에 회원가입 및 여부확인 -> User 엔티티 수정
        // 소셜 로그인 닉네임과 기존 회원의 닉네임 중복 될 수있음
        UserResponse.KakaoProfile kakaoProfile = profileResponse.getBody();

        // username = 권지애_4657335610
        String username = kakaoProfile.getProperties().getNickname() + "_" + kakaoProfile.getId();

        // 권지애_4657335610 (새로 생성한 username이 DB에 있다면 -> 아.. 이전에 회원가입을 했군
        // 사용자 이름 조회 쿼리 수행
        // userOrigin의 return 타입은 User 이거나 null 임
        User userOrigin = userService.사용자이름조회(username);
        if (userOrigin == null) {
            // 최초 카카오 소셜 로그인 사용자임
            System.out.println("기존 회원이 아니므로 자동 회원가입 진행");

            User newUser = User.builder()
                    .username(username)
                    .password(tencoKey) // 소셜 로그인은 임시 비밀번호로 설정함
                    .email(username + "@kakao.com") // 선택사항 (카카오 이메일 받으려면 비즈니스 앱 신청해야함 - 임시 이메일)
                    .provider(OAuthProvider.KAKAO)
                    .build();

            // 카카오 사용자 정보에 프로필 이미지가 있다면 설정
            // http://k.kakaocdn.net/dn/UCUlX/dJMcagDPgBB/YPP8LAKUSZ4DCGNM7LWSNk/img_640x640.jpg
            String profileImage = kakaoProfile.getProperties().getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                newUser.setProfileImage(profileImage); // 카카오에서 넘겨받은 URL 그대로 저장
            }

            userService.소셜회원가입(newUser);
            // 조심해야함! 반드시 필요함
            userOrigin = newUser; // 반드시 넣어 줘야 함 왜? 로그인 처리해야하니까
        } else {
            System.out.println("이미 가입된 회원입니다, 바로 로그인이 진행됩니다.");
        }
        session.setAttribute("sessionUser", userOrigin);
        return "redirect:/";
    }

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
            return "redirect:/user/detail";
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

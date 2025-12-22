package org.example.demo_ssr_v0._core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.demo_ssr_v0._core.errors.exception.Exception401;
import org.example.demo_ssr_v0._core.errors.exception.Exception403;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 컨트롤러 들어가기 전에 조회 먼저 해야 함
        // 먼저 로그인이 되어 진 후 확인을 해야 함 (로그인 인터셉터가 동작 중)
        HttpSession session = request.getSession();
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 1. 로그인 체크는 loginInterceptor가 이미 했으므로 생략 가능하지만
        // 안전상의 이유로 한번 더 체크 함
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다");
        }

        // 관리자 역할 여부 체크
        if (sessionUser.isAdmin() == false) {
            throw new Exception403("접근 권한이 없습니다.");
        }

        return true; // Controller로 들여보내짐
    }
}

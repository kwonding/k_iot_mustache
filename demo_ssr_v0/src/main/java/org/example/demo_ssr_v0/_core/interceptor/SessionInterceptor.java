package org.example.demo_ssr_v0._core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 세션 정보를 뷰 모델에 주입하는 인터셉터
 * 모든 컨트롤러가 실행된 후 (postHandle)
 * , 공통적으로 뷰에서 로그인 사용자 정보를 쓸 수 있게 모델에 주입 시킴
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 실제 뷰 랜더링 될 때만 로직을 실행 (안전성 보장)
        if (modelAndView != null) {

            // Tip. 성능 개선
            // true(기본값) 쓰면 로그인 안 한 방문자에게도 강제로 세션을 생성하여 메모리를 낭비함
            // false를 써서 "있으면 가져오고" 없으면 null 반환하게 설정함
            HttpSession session = request.getSession(false);

            if (session != null) { // 로그인한 사람
                User sessionUser = (User) session.getAttribute("sessionUser");
                // 머스태치 파일 랜더링 되기 전에 데이터를 중간에 개입해서 내려 줌
                modelAndView.addObject("sessionUser", sessionUser);
            }
        }
    }
}

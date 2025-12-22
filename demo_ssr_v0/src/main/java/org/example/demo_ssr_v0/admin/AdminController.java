package org.example.demo_ssr_v0.admin;

import jakarta.servlet.http.HttpSession;
import org.example.demo_ssr_v0._core.errors.exception.Exception401;
import org.example.demo_ssr_v0._core.errors.exception.Exception403;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        // Interceptor 에서 처리중 - 컨트롤러에서 필요없음
//        if (sessionUser == null) {
//            throw new Exception401("로그인이 필요합니다");
//        }
//
//        if (!sessionUser.isAdmin()) {
//            throw new Exception403("접근 권한이 없습니다.");
//        }

        model.addAttribute("user", sessionUser);

        return "admin/dashboard";
    }
}

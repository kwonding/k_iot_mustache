package org.example.demo_ssr_v0.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0.refund.RefundResponse;
import org.example.demo_ssr_v0.refund.RefundService;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final RefundService refundService;

    // http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        model.addAttribute("user", sessionUser);

        return "admin/dashboard";
    }

    @GetMapping("/admin/refund/list")
    public String refundManagement(Model model) {

        List<RefundResponse.AdminListDTO> refundList = refundService.관리자환불요청목록조회();
        model.addAttribute("refundList", refundList);

        return "admin/admin-refund-list";
    }

    // /admin/refund/${id}/reject
    @PostMapping("/admin/refund/{id}/reject")
    public String rejectRefund(@PathVariable Long id, @RequestParam String rejectReason) { // input 태그의 name과 맞춰야함
        refundService.환불거절(id, rejectReason);

        return "redirect:/admin/refund/list";
    }

    // /admin/refund/${id}/approve
    @PostMapping("/admin/refund/{id}/approve")
    public String approveRefund(@PathVariable Long id) {
        refundService.환불승인(id);

        return "redirect:/admin/refund/list";
    }
}

package org.example.demo_ssr_v0.refund;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.Exception401;
import org.example.demo_ssr_v0.payment.Payment;
import org.example.demo_ssr_v0.payment.PaymentResponse;
import org.example.demo_ssr_v0.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    // 환불요청화면 -- /refund/request/1
    @GetMapping("/refund/request/{paymentId}")
    public String refundRequestForm(@PathVariable Long paymentId, Model model, HttpSession session) {
        // TODO - 인증 인터셉터 아직 적용 전 - refund/** --> 추가
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 서비스단에 paymentId 관련 정보를 요청
        Payment payment = refundService.환불요청폼화면검증(paymentId, sessionUser.getId());
        // 가방담아서 데이터를 내려줄 예정
        PaymentResponse.ListDTO paymentDTO = new PaymentResponse.ListDTO(payment);
        model.addAttribute("payment", paymentDTO);

        return "refund/request-form";
    }
    // 환불요청기능
    @PostMapping("/refund/request")
    // @ResponseBody // 뷰 리졸브 안타고 데이터로 내릴거임
    public String refundRequest(RefundRequestDTO.RequestDTO reqDTO, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다");
        }

        reqDTO.validate();
        // 서비스 호출
        refundService.환불요청(sessionUser.getId(), reqDTO);

        return "redirect:/refund/list";
    }

    @GetMapping("/refund/list")
    public String refundList(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다");
        }

        List<RefundResponse.ListDTO> refundList = refundService.환불요청목록조회(sessionUser.getId());
        model.addAttribute("refundList", refundList);

        return "refund/list";
    }
}

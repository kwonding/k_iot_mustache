package org.example.demo_ssr_v0.refund;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v0._core.errors.exception.Exception400;
import org.example.demo_ssr_v0._core.errors.exception.Exception403;
import org.example.demo_ssr_v0._core.errors.exception.Exception404;
import org.example.demo_ssr_v0._core.errors.exception.Exception500;
import org.example.demo_ssr_v0.payment.Payment;
import org.example.demo_ssr_v0.payment.PaymentRepository;
import org.example.demo_ssr_v0.payment.PaymentResponse;
import org.example.demo_ssr_v0.user.User;
import org.example.demo_ssr_v0.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final UserRepository userRepository;

    @Value("${portone.imp-key}")
    private String impKey;

    @Value("${portone.imp-secret}")
    private String impSecret;

    // 0 단계 : 환불 요청 화면 진입 시 검증
    public Payment 환불요청폼화면검증(Long paymentId, Long userId) {
        // 결제 내역 정보 확인해야 함() --> 누가 결제했는지 정보가 있음
        // userId == 누가 결제 했는지 정보가 있음

        // 1. 결제 내역 조회 (with 유저정보도)
        // select 던질때 paymentId
        Payment payment = paymentRepository.findByIdWithUser(paymentId);

        // 2. 본인 확인
        if (!payment.getUser().getId().equals(userId)) {
            throw new Exception403("본인 결제 내역만 환불 요청할 수 있습니다");
        }
        // 3. 결제 완료 상태인지 확인 ("paid"일 때만 폼을 열어 줄 예정)
        if (!payment.getStatus().equals("paid")) {
            throw new Exception400("결제 완료된 상태만 환불 요청할 수 있습니다");
        }
        // 4. 이미 내가(sessionUser) 환불 요청한 상태인지 확인 (요청 중인데 다시 요청하면 안됨)
        if (refundRequestRepository.findByPaymentId(paymentId).isPresent()) {
            throw new Exception400("이미 환불 요청이 진행중입니다");
        }

        return payment;
    }
    // 1단계 : 사용자가 환불 요청 함
    @Transactional
    public void 환불요청(Long userId, RefundRequestDTO.RequestDTO reqDTO) {

        // 화면 검증 로직 재사용
        Payment payment = 환불요청폼화면검증(reqDTO.getPaymentId(), userId);

        // 사용자 조회 (세션 값으로 넘어온 id 실제 존재하는지 확인)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 환불 요청 테이블에 이력 저장 시키기
        RefundRequest refundRequest = RefundRequest
                .builder()
                .user(user)
                .payment(payment)
                .reason(reqDTO.getReason())
                .build();

        refundRequestRepository.save(refundRequest);
    }

    public List<RefundResponse.ListDTO> 환불요청목록조회(Long userId) {
        // 내 아이디로 조회 (환불 요청 내역)
        List<RefundRequest> refundList = refundRequestRepository.findAllByUserId(userId);

        return refundList.stream()
                .map(RefundResponse.ListDTO::new)
                .toList();
    }

    public List<RefundResponse.AdminListDTO> 관리자환불요청목록조회() {
        List<RefundRequest> refundRequestList = refundRequestRepository.findAllWithUserAndPayment();

        return refundRequestList.stream()
                .map(RefundResponse.AdminListDTO::new)
                .toList();
    }

    @Transactional // 업데이트 일어나야함
    public void 환불거절(Long refundRequestId, String rejectReason) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다"));

        if (!refundRequest.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 거절할 수 있습니다");
        }

        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new Exception400("거절 사유를 입력해주세요");
        }

        refundRequest.reject(rejectReason);

        // 더티체킹 - 트랜잭션이 끝나면 자동 반영
    }

    @Transactional
    public void 환불승인(Long id) {
        // 환불 테이블 - 포트원 고유 번호, 가맹점 등등
        // 1. 환불 요청 테이블 조회 (User / Payment도 같이필요함)
        // id -> PK
        RefundRequest refundRequest = refundRequestRepository.findByIdWithUserAndPayment(id)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다"));

        // 2. 환불 --> 대기 / 승인 / 거절
        if (!refundRequest.isPending()) {
            throw new Exception400("대기중인 환불 요청만 승인 할 수 있습니다");
        }

        // 3. 사용자의 포인트 잔액 검증
        Payment payment = refundRequest.getPayment();
        User user = refundRequest.getUser();
        Integer refundAmount = payment.getAmount(); // 사용자가 결제한 원 포인트 금액

        if (user.getPoint() < refundAmount) {
            // 이미 돈 사용했자나!
            throw new Exception400("사용자의 포인트 잔액이 부족하여 환불 불가");
        }

        포트원결제취소(payment.getImpUid(), payment.getAmount());
        // 포트원 액세스 토큰 발급 요청 (포트원 인증서버)
        // 포트원 자원 서버에서 update 요청 -- 즉, 결제 취소요청
        // 내 포인트 잔액 -> 환불한 금액만큼 차감처리

        user.deductPoint(refundAmount); // 포인트 차감
        payment.setStatus("cancelled"); // 결제 상태 paid -> cancelled로 변경
        refundRequest.approve();        // 환불 상태 승인으로 변경
        // 더티체킹, 트랜잭션 끝나면 자동반영됨
    }

    private void 포트원결제취소(String impUid, Integer amount) {
        // 1. 포트원 액세스 토큰 발급
        String accessToken = 포트원액세스토큰발급();
        System.out.println("accessToken : " + accessToken);

        // 2. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // 포트원 서버로부터 발급받은 액세스 토큰

        // 3. 요청 바디 설정
        Map<String, Object> body = new HashMap<>();
        body.put("imp_uid", impUid);
        body.put("amount", amount); // amount+"" 해도 자동 형변환됨 Integer + String = String
        body.put("reason", "관리자 환불 승인");

        // 4. HTTP 요청 메세지 만들기
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 5. HTTP 클라이언트 객체 --> RestTemplate 사용할 예정
        // 포트원에 요청
        RestTemplate restTemplate = new RestTemplate();
        // server to server 시 try-catch 걸어주기
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.iamport.kr/payments/cancel",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 6. 응답 처리 (body에 데이터있음)
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new Exception500("포트원 응답이 비어있습니다");
            }

            Integer code = (Integer) responseBody.get("code");
            if (code != 0) { // 응답코드가 200이라도 응답body의 code가 0이 아니면 환불에 실패했다는 의미
                            // , 실패사유는 body의 message를 통해 확인
                String message = (String) responseBody.get("message");
                throw new Exception400("환불 실패: " + message);
            }

        } catch (Exception e) {
            throw new Exception500("포트원 결제 취소 중 오류 발생");
        }

    }

    private String 포트원액세스토큰발급() {
        try {
            // https://api.iamport.kr/users/getToken
            RestTemplate restTemplate = new RestTemplate();

            // HTTP 메세지 헤더 생성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 메세지 바디 생성
            Map<String, String> body = new HashMap<>();
            // 포트원에서 발급 받았던 REST API KEY
            body.put("imp_key", impKey);
            body.put("imp_secret", impSecret);

            // 헤더 + body 결합
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            // 통신 요청
            ResponseEntity<PaymentResponse.PortOneTokenResponse> response = restTemplate.exchange(
                    "https://api.iamport.kr/users/getToken",
                    HttpMethod.POST,
                    request,
                    PaymentResponse.PortOneTokenResponse.class
            );
            // 응답 받은 액세스 토큰 리턴
            return response.getBody().getResponse().getAccessToken();
        } catch (Exception e) {
            throw new Exception400("포트원 인증실패: 관리자 설정을 확인하세요");
        }
    }
}

package org.example.demo_ssr_v0.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.example.demo_ssr_v0._core.utils.MyDateUtil;

public class PaymentResponse {

    @Data
    public static class PrepareDTO {
        private String merchantUid; // 생성된 우리서버 주문번호
        private Integer amount;
        private String impKey; // 포트원 REST API 키 (필수값)

        public PrepareDTO(String merchantUid, Integer amount, String impKey) {
            this.merchantUid = merchantUid;
            this.amount = amount;
            this.impKey = impKey;
        }
    }

    // 결제 검증 응답 DTO - JS 로 내려줄 데이터
    @Data
    public static class VerifyDTO{
        private Integer amount;
        private Integer currentPoint;

        public VerifyDTO(Integer amount, Integer currentPoint) {
            this.amount = amount;
            this.currentPoint = currentPoint;
        }
    }

    // 포트원 액세스토큰 응답 DTO 설계
    @Data
    public static class PortOneTokenResponse{
        private int code;
        private String message;
        // 중첩 객체를 설계해야함
        private ResponseData response; // "response"가 중요함 - 키값

        @Data
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ResponseData {
            // access_token --> JsonNaming 어노테이션을 통해서 accessToken과 자동 매칭됨
            private String accessToken;
            private int now;
            private int expiredAt;
        }
    }

    // 포트원 결제 조회 응답 DTO
    @Data
    public static class PortOnePaymentResponse {
        private int code;
        private String message;
        private PaymentData response;

        @Data
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PaymentData {
            private Integer amount;
            private String impUid;
            private String merchantUid;
            private String status;
            private Long paidAt;

        }

    }
    @Data
    public static class ListDTO {
        private Long id;
        private Long userId;
        private String impUid;
        private String merchantUid;
        private String paidAt;
        private String status;
        private Integer price;

        public ListDTO(Payment payment) {
            this.id = payment.getId();
            if (payment.getUser() != null) {
                this.userId = payment.getUser().getId();
            }
            this.impUid = payment.getImpUid();
            this.merchantUid = payment.getMerchantUid();
            if (payment.getTimestamp() != null) {
                this.paidAt = MyDateUtil.timestampFormat(payment.getTimestamp());
            }
            if (payment.getStatus().equals("paid")) {
                this.status = "결제완료";
            } else {
                this.status = "결제취소";
            }
            this.price = payment.getAmount();
        }
    }
}

package org.example.demo_ssr_v0.payment;

import lombok.Data;

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
}

package org.example.demo_ssr_v0.refund;

import lombok.Data;

public class RefundResponse {

    @Data
    public static class ListDTO {
        private Long id;
        private Long paymentId;
        private Integer amount;
        private String reason; // 본인이 작성한 환불 사유
        private String rejectReason; // 관리자의 환불 거절 사유 (승인 시 필요없음)
        private String statusDisplay; // 화면 표시용 (대기, 승인, 거절)

        // 상태별 플래그 변수 사용
        private boolean isPending; // 대기
        private boolean isApproved; // 승인
        private boolean isRejected; // 거절

        public ListDTO(RefundRequest refund) {
            this.id = refund.getId();
            this.paymentId = refund.getPayment().getId();
            this.amount = refund.getPayment().getAmount();
            this.reason = refund.getReason();
            this.rejectReason = refund.getRejectReason() == null ? "" : refund.getRejectReason();

            switch (refund.getStatus()) { // JDK 14버전 이후부터 사용가능
                case PENDING -> this.statusDisplay = "대기중";
                case APPROVED -> this.statusDisplay = "승인됨";
                case REJECTED -> this.statusDisplay = "거절됨";
            }

            this.isPending = (refund.getStatus() == RefundStatus.PENDING);
            this.isApproved = (refund.getStatus() == RefundStatus.APPROVED);
            this.isRejected = (refund.getStatus() == RefundStatus.REJECTED);
        }
    }
}

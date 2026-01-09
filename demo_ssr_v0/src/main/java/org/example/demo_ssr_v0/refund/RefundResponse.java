package org.example.demo_ssr_v0.refund;

import lombok.Data;
import org.example.demo_ssr_v0._core.utils.MyDateUtil;

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

    @Data
    public static class AdminListDTO {
        private Long id; // 환불 테이블의 PK
        private String username;
        private Long paymentId; // 결제 테이블의 PK
        private String impUid; // 포트원으로 승인 요청할 때
        private String merchantUid;
        private Integer amount;
        private String requestAt; // 환불 요청 일시
        private RefundStatus status; // 환불 상태
        private String statusDisplay; // 머스태치용 표시
        private String reason;
        private String rejectReason;

        public AdminListDTO(RefundRequest refundRequest) {
            // JOIN FETCH로 한번에 USER와 PAYMENT를 가져옴 - 안가져오면 ERROR 터질 수 있음
            this.id = refundRequest.getId();
            this.username = refundRequest.getUser().getUsername();
            this.paymentId = refundRequest.getPayment().getId();
            this.impUid = refundRequest.getPayment().getImpUid();
            this.merchantUid = refundRequest.getPayment().getMerchantUid();
            this.amount = refundRequest.getPayment().getAmount();
            this.status = refundRequest.getStatus();
            this.reason = refundRequest.getReason();
            this.rejectReason = refundRequest.getRejectReason();

            // TODO 대기중/승인됨/거절됨 으로 변환
            this.statusDisplay = statusDisplay;
            // 스위치 표현식 사용 - jdk 14 버전 이상
            switch (refundRequest.getStatus()) {
                case PENDING -> this.statusDisplay = "대기중";
                case APPROVED -> this.statusDisplay = "승인됨";
                case REJECTED -> this.statusDisplay = "거절됨";
            }

            // refundRequest.getCreatedAt() --> PC --> DB
            // 테스트 --> 샘플을 직접 insert 처리하면서 안넣을 때가 많음 그래서 방어적 코드 넣어주는거임
            if (refundRequest.getCreatedAt() != null) {
                this.requestAt = MyDateUtil.timestampFormat(refundRequest.getCreatedAt());
            }
        }
    }

}

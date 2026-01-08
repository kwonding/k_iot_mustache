package org.example.demo_ssr_v0.refund;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo_ssr_v0.payment.Payment;
import org.example.demo_ssr_v0.user.User;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Table(name = "refund_request_tb")
@Entity
public class RefundRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 우리 프로젝트 비즈니스 로직은 전체 환불 정책 1:1 구조
    // 추후 확장성을 위해서 부분환불을 도입한다면 1:N 으로 설계 되어야함
    // @OneToOne 대신 @ManyToOne 에 unique 제약 조건을 걸어, 1 : 1 로 구현함
    // 추후 변경이 일어난다면 unique = true 만 제거하면 됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(length = 500)
    private String rejectReason;

    @CreationTimestamp
    private Timestamp createdAt;

    @CreationTimestamp
    private Timestamp updatedAt;

    // 생성자 오버로딩
    // 사용자가 먼저 환불 요청에 의해서 row가 생성되기때문 (reason <- row 생성 시 사용자 환불 사유가들어옴)
    @Builder
    public RefundRequest(User user, Payment payment, String reason) {
        this.user = user;
        this.payment = payment;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
    }

    /////////// 편의 기능 /////////////
    // 환불 승인 처리
    public void approve() {
        this.status = RefundStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = reason;
    }

    // 대기중인 상태인지 확인
    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    // 승인된 상태인지 확인
    public boolean isApproved() {
        return this.status == RefundStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == RefundStatus.REJECTED;
    }
}

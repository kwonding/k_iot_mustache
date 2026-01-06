package org.example.demo_ssr_v0.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 기본 CRUD 만들어진 상태

    // imp_uid로 결제 내역 조회
    // 포트원 결제번호로 Payment 정보조회 쿼리 자동 생성됨
    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findByMerchantUid(String merchantUid);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.merchantUid = :merchantUid")
    boolean existsByMerchantUid(@Param("merchantUid") String merchantUid);

    @Query("""
    SELECT p FROM Payment p
    LEFT JOIN FETCH p.user u
    WHERE p.user.id = :userId
    ORDER BY p.timestamp DESC
""")
    List<Payment> findAllByUserId(@Param("userId") Long userId);
}

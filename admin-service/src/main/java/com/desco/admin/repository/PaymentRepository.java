package com.desco.admin.repository;

import com.desco.admin.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query(value = "SELECT COUNT(*) FROM payments WHERE status = CAST(:status AS payment_status)",
           nativeQuery = true)
    long countByStatus(@Param("status") String status);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE status = CAST('SUCCESS' AS payment_status)
            """, nativeQuery = true)
    BigDecimal totalRevenue();

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE status = CAST('SUCCESS' AS payment_status)
              AND bill_month = :billMonth
            """, nativeQuery = true)
    BigDecimal revenueForBillMonth(@Param("billMonth") String billMonth);

    @Query(value = """
            SELECT CAST(status AS text) AS status, COUNT(*) AS total
            FROM payments
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> countGroupedByStatus();

    @Query(value = """
            SELECT bill_month, COALESCE(SUM(amount), 0) AS revenue, COUNT(*) AS payments
            FROM payments
            WHERE status = CAST('SUCCESS' AS payment_status)
            GROUP BY bill_month
            ORDER BY bill_month DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> revenueByRecentMonths(@Param("limit") int limit);
}

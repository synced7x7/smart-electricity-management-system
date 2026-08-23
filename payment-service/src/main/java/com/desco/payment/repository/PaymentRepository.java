package com.desco.payment.repository;

import com.desco.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Native query because "status" is a PostgreSQL enum: a derived query would bind
     * the parameter as varchar and Postgres refuses `payment_status = character varying`.
     * {@code @ColumnTransformer} only rewrites INSERT/UPDATE, never a WHERE clause,
     * so the cast has to be spelled out here.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM payments
                WHERE user_id = :userId
                  AND bill_month = :billMonth
                  AND status = CAST(:status AS payment_status)
            )
            """, nativeQuery = true)
    boolean existsByUserAndBillMonthAndStatus(@Param("userId") UUID userId,
                                             @Param("billMonth") String billMonth,
                                             @Param("status") String status);
}

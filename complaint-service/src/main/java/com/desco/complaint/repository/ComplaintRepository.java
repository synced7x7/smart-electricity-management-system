package com.desco.complaint.repository;

import com.desco.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * `area` and `status` are native PostgreSQL enum types.
 *
 * <p>Derived queries (findByArea, findByStatus, ...) cannot be used on those columns:
 * Spring Data binds the parameter as varchar and Postgres refuses to compare it —
 * {@code operator does not exist: complaint_status = character varying}. The entity's
 * {@code @ColumnTransformer} only rewrites INSERT/UPDATE, never a WHERE clause.
 *
 * <p>So every enum-filtered query below is a native query with an explicit
 * {@code CAST(:param AS <type>)}, and those parameters are passed as String.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    // No enum in the WHERE clause — derived queries are fine here.
    List<Complaint> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    @Query(value = """
            SELECT * FROM complaints
            WHERE area = CAST(:area AS area_name)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Complaint> findByAreaOrderByCreatedAtDesc(@Param("area") String area);

    @Query(value = """
            SELECT * FROM complaints
            WHERE status = CAST(:status AS complaint_status)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Complaint> findByStatusOrderByCreatedAtDesc(@Param("status") String status);

    @Query(value = """
            SELECT * FROM complaints
            WHERE area = CAST(:area AS area_name)
              AND status = CAST(:status AS complaint_status)
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Complaint> findByAreaAndStatusOrderByCreatedAtDesc(@Param("area") String area,
                                                            @Param("status") String status);
}

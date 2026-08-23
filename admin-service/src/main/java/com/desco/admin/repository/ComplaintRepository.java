package com.desco.admin.repository;

import com.desco.admin.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Filtering by the native enum columns needs explicit casts. NULL parameters mean
     * "no filter", which keeps this a single query instead of four derived ones.
     */
    @Query(value = """
            SELECT * FROM complaints
            WHERE (:status IS NULL OR status = CAST(:status AS complaint_status))
              AND (:area   IS NULL OR area   = CAST(:area   AS area_name))
            ORDER BY created_at DESC
            """,
           countQuery = """
            SELECT COUNT(*) FROM complaints
            WHERE (:status IS NULL OR status = CAST(:status AS complaint_status))
              AND (:area   IS NULL OR area   = CAST(:area   AS area_name))
            """,
           nativeQuery = true)
    Page<Complaint> search(@Param("status") String status,
                           @Param("area") String area,
                           Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM complaints WHERE status = CAST(:status AS complaint_status)",
           nativeQuery = true)
    long countByStatus(@Param("status") String status);

    @Query(value = """
            SELECT CAST(status AS text) AS status, COUNT(*) AS total
            FROM complaints
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> countGroupedByStatus();
}

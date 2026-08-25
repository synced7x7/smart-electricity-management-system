package com.desco.admin.repository;

import com.desco.admin.entity.Outage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutageRepository extends JpaRepository<Outage, UUID> {

    @Query(value = """
            SELECT * FROM outages
            WHERE (:status IS NULL OR status = CAST(:status AS outage_status))
              AND (:area   IS NULL OR area   = CAST(:area   AS area_name))
            ORDER BY start_time DESC
            """,
           countQuery = """
            SELECT COUNT(*) FROM outages
            WHERE (:status IS NULL OR status = CAST(:status AS outage_status))
              AND (:area   IS NULL OR area   = CAST(:area   AS area_name))
            """,
           nativeQuery = true)
    Page<Outage> search(@Param("status") String status,
                        @Param("area") String area,
                        Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM outages WHERE status = CAST(:status AS outage_status)",
           nativeQuery = true)
    long countByStatus(@Param("status") String status);

    @Query(value = """
            SELECT CAST(status AS text) AS status, COUNT(*) AS total
            FROM outages
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> countGroupedByStatus();

    @Query(value = """
            SELECT * FROM outages
            WHERE status IN (CAST('SCHEDULED' AS outage_status), CAST('ONGOING' AS outage_status))
            ORDER BY start_time ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Outage> findUpcoming(@Param("limit") int limit);
}

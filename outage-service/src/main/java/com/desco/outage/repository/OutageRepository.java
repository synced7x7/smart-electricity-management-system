package com.desco.outage.repository;

import com.desco.outage.entity.Outage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface OutageRepository extends JpaRepository<Outage, UUID> {

    List<Outage> findAllByOrderByStartTimeDesc();

    List<Outage> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT * FROM outages
            WHERE area = CAST(:area AS area_name)
              AND CAST(status AS text) IN ('SCHEDULED', 'ONGOING')
              AND end_time >= CURRENT_TIMESTAMP
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByAreaOrderByStartTimeDesc(@Param("area") String area);

    @Query(value = """
            SELECT * FROM outages
            WHERE status = CAST(:status AS outage_status)
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByStatusOrderByStartTimeDesc(@Param("status") String status);

    @Query(value = """
            SELECT * FROM outages
            WHERE CAST(status AS text) IN (:statuses)
              AND end_time >= CURRENT_TIMESTAMP
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByStatusInOrderByStartTimeDesc(@Param("statuses") List<String> statuses);

    @Query(value = """
            SELECT * FROM outages
            WHERE area = CAST(:area AS area_name)
              AND status = CAST(:status AS outage_status)
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByAreaAndStatusOrderByStartTimeDesc(@Param("area") String area,
                                                         @Param("status") String status);
}

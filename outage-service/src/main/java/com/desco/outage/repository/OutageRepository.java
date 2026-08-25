package com.desco.outage.repository;

import com.desco.outage.entity.Outage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * `area`, `type` and `status` are native PostgreSQL enum types.
 *
 * <p>Derived queries (findByArea, findByStatus, ...) cannot be used on those columns:
 * Spring Data binds the parameter as varchar and Postgres refuses to compare it —
 * {@code operator does not exist: outage_status = character varying}. The entity's
 * {@code @ColumnTransformer} only rewrites INSERT/UPDATE, never a WHERE clause.
 *
 * <p>So every enum-filtered query below is a native query with an explicit
 * {@code CAST(:param AS <type>)}. Parameters are passed as String for that reason.
 * Note {@code CAST(x AS text)} rather than {@code x::text} — Hibernate parses {@code ::}
 * as named-parameter syntax and mangles it.
 */
@Repository
public interface OutageRepository extends JpaRepository<Outage, UUID> {

    // No enum in the WHERE clause — a derived query is fine here.
    List<Outage> findAllByOrderByStartTimeDesc();

    List<Outage> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT * FROM outages
            WHERE area = CAST(:area AS area_name)
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByAreaOrderByStartTimeDesc(@Param("area") String area);

    @Query(value = """
            SELECT * FROM outages
            WHERE status = CAST(:status AS outage_status)
            ORDER BY start_time DESC
            """, nativeQuery = true)
    List<Outage> findByStatusOrderByStartTimeDesc(@Param("status") String status);

    /**
     * IN over an enum column. The list is compared against the text form of the column
     * so a plain varchar array binds correctly.
     */
    @Query(value = """
            SELECT * FROM outages
            WHERE CAST(status AS text) IN (:statuses)
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

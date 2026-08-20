package com.desco.outageservice.repository;

import com.desco.outageservice.entity.Outage;
import com.desco.outageservice.enums.Area;
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

    @Query("SELECT o FROM Outage o WHERE o.area = :area ORDER BY CASE WHEN o.status = 'ONGOING' THEN 1 WHEN o.status = 'SCHEDULED' THEN 2 ELSE 3 END, o.startTime DESC")
    List<Outage> findByAreaActiveFirst(@Param("area") Area area);

    @Query("SELECT o FROM Outage o WHERE o.area = :area ORDER BY CASE WHEN o.status = 'ONGOING' THEN 1 WHEN o.status = 'SCHEDULED' THEN 2 ELSE 3 END, o.startTime DESC")
    Page<Outage> findByAreaActiveFirstPaged(@Param("area") Area area, Pageable pageable);

    Page<Outage> findByArea(Area area, Pageable pageable);

    @Query("SELECT o FROM Outage o WHERE o.status IN ('RESOLVED', 'CANCELLED') AND (:area IS NULL OR o.area = :area) ORDER BY o.startTime DESC")
    Page<Outage> findHistory(@Param("area") Area area, Pageable pageable);
}

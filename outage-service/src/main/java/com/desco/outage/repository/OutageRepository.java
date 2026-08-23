package com.desco.outage.repository;

import com.desco.outage.entity.Outage;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutageRepository extends JpaRepository<Outage, UUID> {

    List<Outage> findByAreaOrderByStartTimeDesc(Area area);

    List<Outage> findByStatusOrderByStartTimeDesc(OutageStatus status);

    List<Outage> findByStatusInOrderByStartTimeDesc(List<OutageStatus> statuses);

    List<Outage> findByAreaAndStatusOrderByStartTimeDesc(Area area, OutageStatus status);

    List<Outage> findAllByOrderByStartTimeDesc();

    List<Outage> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
}

package com.desco.complaint.repository;

import com.desco.complaint.entity.Complaint;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    List<Complaint> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Complaint> findByAreaOrderByCreatedAtDesc(Area area);

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    List<Complaint> findByAreaAndStatusOrderByCreatedAtDesc(Area area, ComplaintStatus status);

    List<Complaint> findAllByOrderByCreatedAtDesc();
}

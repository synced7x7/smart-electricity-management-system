package com.desco.complaintservice.repository;

import com.desco.complaintservice.entity.Complaint;
import com.desco.complaintservice.enums.Area;
import com.desco.complaintservice.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Page<Complaint> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Complaint> findByAreaOrderByCreatedAtDesc(Area area, Pageable pageable);

    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE LOWER(c.subject) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY c.createdAt DESC")
    Page<Complaint> searchComplaints(@Param("query") String query, Pageable pageable);
}

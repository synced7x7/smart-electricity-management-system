package com.desco.complaintservice.service;

import com.desco.complaintservice.dto.request.CreateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.desco.complaintservice.dto.response.ComplaintResponse;
import com.desco.complaintservice.enums.Area;
import com.desco.complaintservice.enums.ComplaintStatus;
import com.desco.complaintservice.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ComplaintService {

    ComplaintResponse createComplaint(CreateComplaintRequest request, UserPrincipal user);

    Page<ComplaintResponse> getMyComplaints(UserPrincipal user, Pageable pageable);

    ComplaintResponse getComplaintById(UUID id, UserPrincipal user);

    ComplaintResponse updateComplaint(UUID id, UpdateComplaintRequest request, UserPrincipal user);

    void deleteComplaint(UUID id, UserPrincipal user);

    Page<ComplaintResponse> getAllComplaints(Pageable pageable);

    Page<ComplaintResponse> getComplaintsByArea(Area area, Pageable pageable);

    Page<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status, Pageable pageable);

    Page<ComplaintResponse> searchComplaints(String query, Pageable pageable);

    ComplaintResponse updateComplaintStatus(UUID id, UpdateComplaintStatusRequest request);
}

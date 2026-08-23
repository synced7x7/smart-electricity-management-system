package com.desco.complaint.service;

import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintUpdateRequest;
import com.desco.complaint.dto.response.ComplaintResponse;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintStatus;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {

    ComplaintResponse submitComplaint(ComplaintRequest request);

    ComplaintResponse getComplaintById(UUID id);

    List<ComplaintResponse> getAllComplaints();

    List<ComplaintResponse> getComplaintsByUser(UUID userId);

    List<ComplaintResponse> getComplaintsByArea(Area area);

    List<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status);

    ComplaintResponse updateComplaint(UUID id, ComplaintUpdateRequest request);

    ComplaintResponse updateComplaintStatus(UUID id, ComplaintStatus status, String resolutionNotes);

    void deleteComplaint(UUID id);
}

package com.desco.admin.service;

import com.desco.admin.dto.request.CreateOutageRequest;
import com.desco.admin.dto.request.UpdateComplaintRequest;
import com.desco.admin.dto.request.UpdateOutageStatusRequest;
import com.desco.admin.dto.request.UpdateUserStatusRequest;
import com.desco.admin.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    DashboardResponse getDashboard();

    List<ServiceStatus> getServiceStatuses();

    // users 
    PageResponse<UserResponse> listUsers(Boolean isActive, Pageable pageable);

    UserResponse getUser(UUID userId);

    UserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request);

    // complaints 
    PageResponse<ComplaintResponse> listComplaints(String status, String area, Pageable pageable);

    ComplaintResponse getComplaint(UUID complaintId);

    ComplaintResponse updateComplaint(UUID complaintId, UpdateComplaintRequest request);

    // outages
    PageResponse<OutageResponse> listOutages(String status, String area, Pageable pageable);

    OutageResponse getOutage(UUID outageId);

    OutageResponse createOutage(CreateOutageRequest request, UUID createdBy);

    OutageResponse updateOutageStatus(UUID outageId, UpdateOutageStatusRequest request);

    // payments 
    PageResponse<PaymentResponse> listPayments(Pageable pageable);

    List<PaymentResponse> getUserPayments(UUID userId);
}

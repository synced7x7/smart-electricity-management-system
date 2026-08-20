package com.desco.outageservice.service;

import com.desco.outageservice.dto.request.CreateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageStatusRequest;
import com.desco.outageservice.dto.response.OutageResponse;
import com.desco.outageservice.enums.Area;
import com.desco.outageservice.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OutageService {

    OutageResponse createOutage(CreateOutageRequest request, UserPrincipal admin);

    Page<OutageResponse> getAllOutages(Pageable pageable);

    OutageResponse getOutageById(UUID id);

    Page<OutageResponse> getMyAreaOutages(UserPrincipal user, Pageable pageable);

    Page<OutageResponse> getOutagesByArea(Area area, Pageable pageable);

    Page<OutageResponse> getOutageHistory(UserPrincipal user, Pageable pageable);

    OutageResponse updateOutage(UUID id, UpdateOutageRequest request);

    OutageResponse updateOutageStatus(UUID id, UpdateOutageStatusRequest request);

    void deleteOutage(UUID id);
}

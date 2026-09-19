package com.desco.outage.service;

import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.dto.response.OutageResponse;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;

import java.util.List;
import java.util.UUID;

public interface OutageService {

    OutageResponse createOutage(OutageRequest request, UUID createdBy);

    OutageResponse getOutageById(UUID id);

    List<OutageResponse> getAllOutages();

    List<OutageResponse> getOutagesByArea(Area area);

    List<OutageResponse> getActiveOutages();

    List<OutageResponse> getOutagesByStatus(OutageStatus status);

    OutageResponse updateOutage(UUID id, OutageRequest request);

    OutageResponse updateOutageStatus(UUID id, OutageStatus status);

    void deleteOutage(UUID id);
}

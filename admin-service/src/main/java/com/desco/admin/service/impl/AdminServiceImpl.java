package com.desco.admin.service.impl;

import com.desco.admin.client.ServiceHealthClient;
import com.desco.admin.dto.request.CreateOutageRequest;
import com.desco.admin.dto.request.UpdateComplaintRequest;
import com.desco.admin.dto.request.UpdateOutageStatusRequest;
import com.desco.admin.dto.request.UpdateUserStatusRequest;
import com.desco.admin.dto.response.*;
import com.desco.admin.entity.Complaint;
import com.desco.admin.entity.Outage;
import com.desco.admin.entity.Payment;
import com.desco.admin.entity.User;
import com.desco.admin.enums.*;
import com.desco.admin.exception.ResourceNotFoundException;
import com.desco.admin.repository.ComplaintRepository;
import com.desco.admin.repository.OutageRepository;
import com.desco.admin.repository.PaymentRepository;
import com.desco.admin.repository.UserRepository;
import com.desco.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter BILL_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int RECENT_MONTHS = 6;
    private static final int UPCOMING_OUTAGES = 5;

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintRepository complaintRepository;
    private final OutageRepository outageRepository;
    private final ServiceHealthClient serviceHealthClient;


    private final ExecutorService healthCheckExecutor = Executors.newFixedThreadPool(6);

    @Value("${desco.services.auth}")         private String authUrl;
    @Value("${desco.services.user}")         private String userUrl;
    @Value("${desco.services.outage}")       private String outageUrl;
    @Value("${desco.services.notification}") private String notificationUrl;
    @Value("${desco.services.complaint}")    private String complaintUrl;
    @Value("${desco.services.payment}")      private String paymentUrl;

    //  Dashboard
    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .users(buildUserStats())
                .payments(buildPaymentStats())
                .complaints(buildComplaintStats())
                .outages(buildOutageStats())
                .services(getServiceStatuses())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private DashboardResponse.UserStats buildUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByIsActive(true);
        return DashboardResponse.UserStats.builder()
                .total(total)
                .active(active)
                .inactive(total - active)
                .admins(userRepository.countByRole(UserRole.ADMIN.name()))
                .byArea(toCountMap(userRepository.countGroupedByArea()))
                .build();
    }

    private DashboardResponse.PaymentStats buildPaymentStats() {
        Map<String, Long> byStatus = toCountMap(paymentRepository.countGroupedByStatus());
        String currentMonth = LocalDateTime.now().format(BILL_MONTH);

        List<DashboardResponse.MonthlyRevenue> recent =
                paymentRepository.revenueByRecentMonths(RECENT_MONTHS).stream()
                        .map(row -> DashboardResponse.MonthlyRevenue.builder()
                                .billMonth(asString(row[0]))
                                .revenue(asBigDecimal(row[1]))
                                .payments(asLong(row[2]))
                                .build())
                        .toList();

        return DashboardResponse.PaymentStats.builder()
                .total(paymentRepository.count())
                .successful(byStatus.getOrDefault(PaymentStatus.SUCCESS.name(), 0L))
                .pending(byStatus.getOrDefault(PaymentStatus.PENDING.name(), 0L))
                .failed(byStatus.getOrDefault(PaymentStatus.FAILED.name(), 0L))
                .totalRevenue(orZero(paymentRepository.totalRevenue()))
                .currentMonthRevenue(orZero(paymentRepository.revenueForBillMonth(currentMonth)))
                .currentBillMonth(currentMonth)
                .byStatus(byStatus)
                .recentMonths(recent)
                .build();
    }

    private DashboardResponse.ComplaintStats buildComplaintStats() {
        Map<String, Long> byStatus = toCountMap(complaintRepository.countGroupedByStatus());
        return DashboardResponse.ComplaintStats.builder()
                .total(complaintRepository.count())
                .pending(byStatus.getOrDefault(ComplaintStatus.PENDING.name(), 0L))
                .inProgress(byStatus.getOrDefault(ComplaintStatus.IN_PROGRESS.name(), 0L))
                .resolved(byStatus.getOrDefault(ComplaintStatus.RESOLVED.name(), 0L))
                .rejected(byStatus.getOrDefault(ComplaintStatus.REJECTED.name(), 0L))
                .byStatus(byStatus)
                .build();
    }

    private DashboardResponse.OutageStats buildOutageStats() {
        Map<String, Long> byStatus = toCountMap(outageRepository.countGroupedByStatus());
        return DashboardResponse.OutageStats.builder()
                .total(outageRepository.count())
                .scheduled(byStatus.getOrDefault(OutageStatus.SCHEDULED.name(), 0L))
                .ongoing(byStatus.getOrDefault(OutageStatus.ONGOING.name(), 0L))
                .resolved(byStatus.getOrDefault(OutageStatus.RESOLVED.name(), 0L))
                .cancelled(byStatus.getOrDefault(OutageStatus.CANCELLED.name(), 0L))
                .byStatus(byStatus)
                .upcoming(outageRepository.findUpcoming(UPCOMING_OUTAGES).stream()
                        .map(this::toResponse).toList())
                .build();
    }

    //Health checks of all microservices
    @Override
    public List<ServiceStatus> getServiceStatuses() {
        List<Map.Entry<String, String>> targets = List.of(
                Map.entry("auth-service", authUrl),
                Map.entry("user-service", userUrl),
                Map.entry("outage-service", outageUrl),
                Map.entry("notification-service", notificationUrl),
                Map.entry("complaint-service", complaintUrl),
                Map.entry("payment-service", paymentUrl));

        List<CompletableFuture<ServiceStatus>> futures = targets.stream()
                .map(t -> CompletableFuture.supplyAsync(
                        () -> serviceHealthClient.check(t.getKey(), t.getValue()),
                        healthCheckExecutor))
                .toList();

        return futures.stream().map(CompletableFuture::join).toList();
    }

    //  Users
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Boolean isActive, Pageable pageable) {
        Page<User> page = (isActive == null)
                ? userRepository.findAll(pageable)
                : userRepository.findByIsActive(isActive, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = findUser(userId);

        if (userRepository.countActiveByRole(UserRole.ADMIN.name()) <= 1) {
            throw new IllegalArgumentException(
                    "Cannot deactivate the only remaining active ADMIN account");
        }

        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.saveAndFlush(user);
        log.info("User {} set to isActive={}", saved.getEmail(), saved.getIsActive());
        return toResponse(saved);
    }

    //Complaints
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> listComplaints(String status, String area, Pageable pageable) {
        Page<Complaint> page = complaintRepository.search(
                normalise(status, ComplaintStatus.class, "status"),//"PENDING" → ComplaintStatus.PENDING
                normalise(area, AreaName.class, "area"),
                pageable);
        return PageResponse.from(page, this::toResponse); //converting page to DTO
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(UUID complaintId) {
        return toResponse(findComplaint(complaintId));
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(UUID complaintId, UpdateComplaintRequest request) {
        Complaint complaint = findComplaint(complaintId);
        complaint.setStatus(request.getStatus());
        if (request.getAdminRemark() != null) {
            complaint.setAdminRemark(request.getAdminRemark());
        }
        Complaint saved = complaintRepository.saveAndFlush(complaint);
        log.info("Complaint {} moved to {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    //  Outages
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutageResponse> listOutages(String status, String area, Pageable pageable) {
        Page<Outage> page = outageRepository.search(
                normalise(status, OutageStatus.class, "status"),
                normalise(area, AreaName.class, "area"),
                pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OutageResponse getOutage(UUID outageId) {
        return toResponse(findOutage(outageId));
    }

    @Override
    @Transactional
    public OutageResponse createOutage(CreateOutageRequest request, UUID createdBy) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        Outage outage = new Outage();
        outage.setTitle(request.getTitle());
        outage.setDescription(request.getDescription());
        outage.setArea(request.getArea());
        outage.setType(request.getType());
        outage.setStatus(OutageStatus.SCHEDULED);
        outage.setStartTime(request.getStartTime());
        outage.setEndTime(request.getEndTime());
        outage.setCreatedBy(createdBy);

        // saveAndFlush so @CreationTimestamp is populated before the response is mapped.
        Outage saved = outageRepository.saveAndFlush(outage);
        log.info("Outage {} created for {} ({})", saved.getId(), saved.getArea(), saved.getType());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OutageResponse updateOutageStatus(UUID outageId, UpdateOutageStatusRequest request) {
        Outage outage = findOutage(outageId);
        outage.setStatus(request.getStatus());
        Outage saved = outageRepository.saveAndFlush(outage);
        log.info("Outage {} moved to {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    //  Payments
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(Pageable pageable) {
        return PageResponse.from(paymentRepository.findAllByOrderByCreatedAtDesc(pageable),
                this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    // Utility
    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Complaint findComplaint(UUID id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }

    private Outage findOutage(UUID id) {
        return outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));
    }

    private <E extends Enum<E>> String normalise(String value, Class<E> type, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String candidate = value.trim().toUpperCase();
        try {
            Enum.valueOf(type, candidate);
            return candidate;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid %s '%s'. Allowed values: %s"
                    .formatted(field, value, java.util.Arrays.toString(type.getEnumConstants())));
        }
    }

    /** Converts native (label, count) rows into an ordered map. cause the db returns rows but mapping is efficient to handle cause we dont need to iterate the whole result set. */ 
    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(asString(row[0]), asLong(row[1]));
        }
        return map;
    }

    private String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private long asLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private BigDecimal asBigDecimal(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId()).email(u.getEmail()).role(u.getRole()).area(u.getArea())
                .isActive(u.getIsActive()).createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt())
                .build();
    }

    private ComplaintResponse toResponse(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId()).userId(c.getUserId()).subject(c.getSubject())
                .description(c.getDescription()).area(c.getArea()).status(c.getStatus())
                .adminRemark(c.getAdminRemark()).createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private OutageResponse toResponse(Outage o) {
        return OutageResponse.builder()
                .id(o.getId()).title(o.getTitle()).description(o.getDescription())
                .area(o.getArea()).type(o.getType()).status(o.getStatus())
                .startTime(o.getStartTime()).endTime(o.getEndTime()).createdBy(o.getCreatedBy())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).userId(p.getUserId()).billMonth(p.getBillMonth())
                .amount(p.getAmount()).paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId()).status(p.getStatus())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt())
                .build();
    }
}

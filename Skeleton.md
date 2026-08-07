desco-simulation/
├── docker-compose.yml
├── docker-compose.override.yml
├── .env.example
├── README.md
│
├── config-server/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/config/
│       │   └── ConfigServerApplication.java
│       └── resources/
│           ├── application.yml
│           └── configs/
│               ├── api-gateway.yml
│               ├── auth-service.yml
│               ├── user-service.yml
│               ├── outage-service.yml
│               ├── notification-service.yml
│               ├── complaint-service.yml
│               ├── payment-service.yml
│               └── admin-service.yml
│
├── api-gateway/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   ├── config/
│       │   │   ├── GatewayConfig.java
│       │   │   ├── SecurityConfig.java
│       │   │   └── CorsConfig.java
│       │   ├── filter/
│       │   │   ├── JwtAuthFilter.java
│       │   │   └── RateLimitFilter.java
│       │   └── util/
│       │       └── JwtUtil.java
│       └── resources/
│           └── application.yml
│
├── auth-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/auth/
│       │   ├── AuthServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── AuthController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── RegisterRequest.java
│       │   │   └── response/
│       │   │       ├── AuthResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── User.java
│       │   ├── repository/
│       │   │   └── UserRepository.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   └── impl/AuthServiceImpl.java
│       │   ├── security/
│       │   │   ├── JwtService.java
│       │   │   ├── JwtAuthFilter.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       ├── AuthException.java
│       │       └── ErrorResponse.java
│       └── resources/
│           └── application.yml
│
├── user-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/user/
│       │   ├── UserServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── UserController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   └── UpdateProfileRequest.java
│       │   │   └── response/
│       │   │       ├── UserResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── UserProfile.java
│       │   ├── enums/
│       │   │   ├── Area.java
│       │   │   └── Role.java
│       │   ├── repository/
│       │   │   └── UserProfileRepository.java
│       │   ├── service/
│       │   │   ├── UserService.java
│       │   │   └── impl/UserServiceImpl.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       └── ResourceNotFoundException.java
│       └── resources/
│           └── application.yml
│
├── outage-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/outage/
│       │   ├── OutageServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── OutageController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   └── OutageRequest.java
│       │   │   └── response/
│       │   │       ├── OutageResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── Outage.java
│       │   ├── enums/
│       │   │   ├── OutageType.java
│       │   │   └── OutageStatus.java
│       │   ├── repository/
│       │   │   └── OutageRepository.java
│       │   ├── service/
│       │   │   ├── OutageService.java
│       │   │   └── impl/OutageServiceImpl.java
│       │   ├── client/
│       │   │   └── NotificationClient.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       └── ResourceNotFoundException.java
│       └── resources/
│           └── application.yml
│
├── notification-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/notification/
│       │   ├── NotificationServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── NotificationController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   └── NotificationRequest.java
│       │   │   └── response/
│       │   │       ├── NotificationResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── Notification.java
│       │   ├── repository/
│       │   │   └── NotificationRepository.java
│       │   ├── service/
│       │   │   ├── NotificationService.java
│       │   │   └── impl/NotificationServiceImpl.java
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           └── application.yml
│
├── complaint-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/complaint/
│       │   ├── ComplaintServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── ComplaintController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── ComplaintRequest.java
│       │   │   │   └── ComplaintUpdateRequest.java
│       │   │   └── response/
│       │   │       ├── ComplaintResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── Complaint.java
│       │   ├── enums/
│       │   │   └── ComplaintStatus.java
│       │   ├── repository/
│       │   │   └── ComplaintRepository.java
│       │   ├── service/
│       │   │   ├── ComplaintService.java
│       │   │   └── impl/ComplaintServiceImpl.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       └── ResourceNotFoundException.java
│       └── resources/
│           └── application.yml
│
├── payment-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/payment/
│       │   ├── PaymentServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── PaymentController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   └── PaymentRequest.java
│       │   │   └── response/
│       │   │       ├── PaymentResponse.java
│       │   │       ├── ReceiptResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── entity/
│       │   │   └── Payment.java
│       │   ├── enums/
│       │   │   └── PaymentStatus.java
│       │   ├── repository/
│       │   │   └── PaymentRepository.java
│       │   ├── service/
│       │   │   ├── PaymentService.java
│       │   │   └── impl/PaymentServiceImpl.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       └── ResourceNotFoundException.java
│       └── resources/
│           └── application.yml
│
├── admin-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/desco/admin/
│       │   ├── AdminServiceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   └── AdminController.java
│       │   ├── dto/
│       │   │   └── response/
│       │   │       ├── DashboardResponse.java
│       │   │       └── ApiResponse.java
│       │   ├── client/
│       │   │   ├── UserClient.java
│       │   │   ├── OutageClient.java
│       │   │   ├── ComplaintClient.java
│       │   │   └── PaymentClient.java
│       │   ├── service/
│       │   │   ├── AdminService.java
│       │   │   └── impl/AdminServiceImpl.java
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           └── application.yml
│
└── frontend/
    ├── Dockerfile
    ├── .env.example
    ├── vite.config.js
    ├── tailwind.config.js
    ├── postcss.config.js
    ├── index.html
    ├── package.json
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── api/
        │   ├── axiosInstance.js
        │   ├── authApi.js
        │   ├── userApi.js
        │   ├── outageApi.js
        │   ├── notificationApi.js
        │   ├── complaintApi.js
        │   ├── paymentApi.js
        │   └── adminApi.js
        ├── components/
        │   ├── common/
        │   │   ├── Navbar.jsx
        │   │   ├── Sidebar.jsx
        │   │   ├── Footer.jsx
        │   │   ├── LoadingSpinner.jsx
        │   │   ├── ProtectedRoute.jsx
        │   │   └── ErrorBoundary.jsx
        │   ├── outage/
        │   │   ├── OutageCard.jsx
        │   │   └── OutageForm.jsx
        │   ├── complaint/
        │   │   ├── ComplaintCard.jsx
        │   │   └── ComplaintForm.jsx
        │   ├── payment/
        │   │   ├── PaymentForm.jsx
        │   │   └── Receipt.jsx
        │   └── notification/
        │       └── NotificationBell.jsx
        ├── pages/
        │   ├── auth/
        │   │   ├── LoginPage.jsx
        │   │   └── RegisterPage.jsx
        │   ├── user/
        │   │   ├── DashboardPage.jsx
        │   │   ├── ProfilePage.jsx
        │   │   ├── OutageHistoryPage.jsx
        │   │   ├── ComplaintsPage.jsx
        │   │   ├── PaymentsPage.jsx
        │   │   └── NotificationsPage.jsx
        │   └── admin/
        │       ├── AdminDashboardPage.jsx
        │       ├── ManageUsersPage.jsx
        │       ├── ManageOutagesPage.jsx
        │       ├── ManageComplaintsPage.jsx
        │       └── ManagePaymentsPage.jsx
        ├── store/
        │   ├── index.js
        │   ├── slices/
        │   │   ├── authSlice.js
        │   │   ├── outageSlice.js
        │   │   ├── notificationSlice.js
        │   │   └── complaintSlice.js
        └── utils/
            ├── constants.js
            ├── helpers.js
            └── validators.js
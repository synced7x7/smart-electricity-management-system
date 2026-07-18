# Smart Electricity Management System (DESCO-Simulation)

A production-inspired **Smart Electricity Management System** that modernizes communication between electricity providers and consumers. The platform enables customers to receive area-based outage notifications, submit complaints, manage their profiles, and simulate bill payments through a secure microservice architecture.

---

## Live Demo

<p align="left">
  <a href="https://www.example.com">
    <img src="https://img.shields.io/badge/Live_Website-111827?style=for-the-badge&logo=vercel&logoColor=white" alt="Live Website">
  </a>
</p>

---

## Tech Stack

### Frontend

<p>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" title="React" alt="React" width="48" height="48"/>
  <img src="https://cdn.simpleicons.org/vite/646CFF" title="Vite" alt="Vite" width="48" height="48"/>
  <img src="https://cdn.simpleicons.org/tailwindcss/06B6D4" title="Tailwind CSS" alt="Tailwind CSS" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" title="JavaScript" alt="JavaScript" width="48" height="48"/>
</p>

### Backend

<p>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" title="Java" alt="Java" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" title="Spring Boot" alt="Spring Boot" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" title="PostgreSQL" alt="PostgreSQL" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" title="Docker" alt="Docker" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" title="Apache Maven" alt="Apache Maven" width="48" height="48"/>
</p>

### Cloud & Deployment

<p>
  <img src="https://cdn.simpleicons.org/vercel/ffffff" title="Vercel" alt="Vercel" width="48" height="48"/>
  <img src="https://cdn.simpleicons.org/render/46E3B7" title="Render" alt="Render" width="48" height="48"/>
  <img src="https://cdn.simpleicons.org/supabase/3ECF8E" title="Supabase" alt="Supabase" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" title="Git" alt="Git" width="48" height="48"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg" title="GitHub" alt="GitHub" width="48" height="48"/>
</p>

### API & Security

* Spring Security
* JWT Authentication
* Spring Cloud Gateway
* Spring Cloud Config
* Spring Data JPA
* Swagger / OpenAPI

---

# Architecture

```text
                    React (Vercel)
                           │
                           ▼
                    API Gateway
                           │
      ┌────────────┬─────────────┬─────────────┐
      ▼            ▼             ▼             ▼
   Auth        User        Outage       Complaint
                                             │
                                             ▼
                                       Notification
                                             │
                                             ▼
                                         Payment

                  PostgreSQL (Supabase)
```

---

# Microservices

* API Gateway
* Config Server
* Auth Service
* User Service
* Outage Service
* Notification Service
* Complaint Service
* Payment Service
* Admin Service

---

# Features

## Customer

* JWT Authentication
* Register & Login
* Area-based service allocation
* Planned & emergency outage notifications
* Outage history
* Complaint submission & tracking
* Dummy electricity bill payment
* Payment history
* Profile management
* Responsive dashboard

## Admin

* Dashboard
* User management
* Area management
* Create/Edit/Delete outage schedules
* Broadcast notifications
* Complaint management
* Payment monitoring

---

# Area Coverage

The project simulates electricity services for selected areas of Dhaka.

* Uttara
* Gulshan
* Banani
* Dhanmondi
* Bashundhara
* Mirpur
* Banasree
* Baridhara

---

# Security

* Spring Security
* JWT Authentication
* Role-Based Access Control (RBAC)
* Password Encryption
* Protected REST APIs
* DTO Validation
* Global Exception Handling

---

# API Documentation

Swagger/OpenAPI is integrated for interactive API testing and documentation.

Example:

```text
/api/swagger-ui.html
```

---

# Project Structure

```text
smart-electricity-management/

├── frontend/
│
├── api-gateway/
│
├── config-server/
│
├── auth-service/
│
├── user-service/
│
├── outage-service/
│
├── notification-service/
│
├── complaint-service/
│
├── payment-service/
│
├── admin-service/
│
├── docker-compose.yml
│
└── README.md
```

---

# Deployment

| Service  | Platform            |
| -------- | ------------------- |
| Frontend | Vercel              |
| Backend  | Render              |
| Database | Supabase PostgreSQL |

---

# Future Improvements

* Email Notifications
* SMS Notifications
* Push Notifications
* Real Payment Gateway
* AI Complaint Categorization
* Smart Outage Prediction
* Mobile Application
* Analytics Dashboard

---

# License

This project is developed for educational and portfolio purposes.

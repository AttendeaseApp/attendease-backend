# RCians Attendease

An Automated Attendance Monitoring System using Geofencing and Facial Recognition Technologies for the College Students of Rogationist College - Silang, Cavite, Philippines

### Author

**Jake Viado** *Lead Developer & System Architect of RCians Attendease*

https://github.com/jakeeviado

## Project Overview

RCians Attendease is an automated attendance monitoring and tracking system built for the college students of Rogationist College. It automates student attendance management and event registration by leveraging mobile Geofencing and GPS to detect student presence within a defined location boundary, eliminating manual attendance taking entirely.

To ensure identity integrity during registration, Attendease integrates a dedicated Facial Recognition service that validates a student's identity before completing their registration preventing proxy attendance and unauthorized event sign-ups.

The system serves two primary user roles: **OSA (Office of Student Affairs)** staff who manage
events, monitor attendance, and generate reports, and **Students** who register and track their
own attendance through their mobile devices.

---
## Core Features

- **Geofencing & GPS-based Attendance** — automatically marks attendance when a student's device
  enters the defined campus or event boundary
- **Facial Recognition Validation** — a separate microservice verifies student identity during
  registration to prevent proxy attendance
- **Event Registration & Tracking** — students can register for events; attendance is tracked
  and monitored in real time
- **Role-based Access** — OSA and students see only what is relevant to their role

---
## System Architecture

| Layer                              | Technology            | Role & Implementation                                                                                       |
|:-----------------------------------|:----------------------|:------------------------------------------------------------------------------------------------------------|
| **Service Layer (Core)**           | LayerTechnology       | Primary Backend: Handles business logic, event orchestration, and real-time WebSocket updates.              |
| **Facial Recogntion Microservice** | Microservice          | Dedicated service for Facial Recognition processing and identity verification.                              |
| **Presentation (Web)**             | Presentation (Web)    | OSA Module: Administrative module for event, location, user, academic management and attendance monitoring. |
| **Presentation (Mobile)**          | Presentation (Mobile) | Student Module: Built with Expo for GPS tracking, Geofencing, and student check-ins.                        |
| **Database**                       | Database              | NoSQL storage for flexible student profiles.                                                                |

---

## Do I Recommend Using This Iteration?

**For full production? No. For learning? Maybe Yes**

The **RCians Attendease** served as my primary exposure to exploring system design, microservice architecture, and real-world backend patterns. 

Many architectural decisions in the early iterations were made while I was still gaining understanding on everything. While I corrected many of these as the project progressed, the final version still misses some opportunities for deep optimization and structural refinement.

### Maybe there's Educational Value?
If you are here to study how **Spring Boot, Python, MongoDB, WebSockets, and Microservices** fit together, then this is a great reference. Maybe this provides a transparent look at the development process mistakes and all making it a practical tool for learning.

---

## Bad Design Decisions (This Version)

These were identified issues in this implementation that have never been addressed during and after the final wave of development of this project

###  1. Dedicated User Account Management Inside Spring Boot
**Problem:** User registration, password hashing, session management, and role logic were
all built manually inside the Spring Boot application

**Why it was bad:**
- Too much non business code to maintain
- Security risks from custom auth implementation
- No separation of concerns

**Re-Implementation Fix:** Delegate entirely to Auth0.

---

### 2. Lack of Granular Role-Based Access Control (RBAC)
**Problem:** There was no separation between "Administrative" and "Operational" roles. The Office of Student Affairs (OSA) acted as a "Super Admin," granting them access to sensitive system configurations and user account management that were unnecessary for their daily tasks.

**Why it was bad:**
- **Violation of Least Privilege:** OSA staff could accidentally (or intentionally) modify system-level user data or security settings.
- **Security Risk:** If a single OSA staff account was compromised, the entire system—including user credentials and logs—was exposed.
- **No Audit Trail:** Because everyone had "Admin" power, it was impossible to track who made specific high-level changes.

**Future Stakeholder & User Type Definitions:**
In a production ready version, the roles should be strictly decoupled:
* **System Admin (Technical):** Manages server health, database backups, and Auth0 configurations. No access to student attendance records.
* **OSA Staff (Operational):** Manages and monitor events, manages location, and generates reports. No access to user password management or system audit logs.
* **Student (End-User):** STILL should accesses only their personal attendance history and event registration.
  
---

###  3. MongoDB for Relational Data
**Problem:** MongoDB was used as the primary database despite attendance data being
fundamentally relational (attendance records -> students -> sections -> courses -> cluster).

**Why it was bad:**
- No foreign key support — deleted records left orphaned references throughout collections
- Manual relationship management in application code
- `$lookup` aggregations for joins were slow and hard to maintain
- No cascade deletes — orphan cleanup had to be written manually

**Re-Implementation Fix:** Planning to migrate to PostgreSQL with proper foreign keys, `ON DELETE CASCADE`, and `ON DELETE RESTRICT` constraints enforced at the database level.

---

###  4. No or Poor Data Archival Strategy
**Problem:** All attendance records across all academic years lived in the same active
tables with no separation or cleanup strategy.

**Why it was bad:**
- Active tables bloated over time, slowing queries
- No clear boundary between current and historical data
- Risk of accidentally modifying historical records

**Re-Implementation Fix:** Still Not sure.

---

### 5. Lack of Automated QA and Formal Testing
**Problem:** The project was developed without automated unit, integration, or E2E tests. Verification was limited to manual smoke tests conducted by developers and stakeholders (OSA and STUDENTS) just prior to the final capstone defense.

**Why it was bad:**
- **Late-Stage Discovery:** Critical bugs were often only found during live walk throughs with users.
- **Regression Risks:** Fixing one bug often introduced two more, with no automated safety net to catch them.
- **Scaling Bottleneck:** Manual testing is not scalable. As the feature set grew, the time required to "manually verify everything" became a massive overhead.
- **Fragile Codebase:** Refactoring the early "bad" architectural decisions felt like a gamble because there was no test suite to confirm the system still functioned correctly.

**Re-Implementation Fix:** Implement this things:
1. **Unit Tests:** JUnit 5/Mockito (Backend) to test logic in isolation.
2. **Integration Tests:** Test container-based database interactions

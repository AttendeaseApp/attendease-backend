# RCians Attendease

An Automated Attendance Monitoring System using Geofencing and Facial Recognition Technologies for the College Students of Rogationist College - Silang, Cavite, Philippines

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

**for RCians**

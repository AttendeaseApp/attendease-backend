## RCians Attendease Core

Automated Attendance Monitoring System using Geofencing and Facial Recognition Technologies

### Project Overview

RCians Attendease eliminates manual attendance taking by replacing paper logs with a robust, location-aware digital system. Designed for the Office of Student Affairs (OSA), the system ensures academic integrity by verifying student presence through real-time GPS boundary detection and biometric authentication.

### Core Features

- **Geofencing & GPS-based Attendance** automatically marks attendance when a student's device
  enters the defined campus or event boundary
- **Facial Recognition Validation** a separate microservice verifies student identity during
  registration to prevent proxy attendance
- **Event Registration & Tracking** students can register for events; attendance is tracked
  and monitored in real time
- **Role-based Access** OSA and students see only what is relevant to their role

### System Architecture

The system follows a scalable **3-Tier Architecture**, ensuring a clean separation between the user interface, business logic, and data storage.

| Tier                   | Components                             | Implementation & Role                                                                                               |
| :--------------------- | :------------------------------------- | :------------------------------------------------------------------------------------------------------------------ |
| **Presentation Layer** | Admin Web Portal & Student Mobile App  | Provides role-specific interfaces for OSA staff and students, handling GPS/Geofencing triggers via Expo.            |
| **Application Layer**  | Core Service & Biometrics Microservice | Handles core business logic, event orchestration, real-time WebSocket updates, and facial recognition verification. |
| **Data Layer**         | NoSQL Database                         | Manages persistent storage for student profiles, event logs, and attendance records.                                |

---
For the College Department of Rogationist College - Silang, Cavite, Philippines

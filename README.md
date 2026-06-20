# Hotel Reservation API

Backend API for managing hotel reservations, guests, rooms, and authentication.

Built in Java using a layered Controller–Service–DAO architecture, the system focuses on maintainability, business rule enforcement, and secure access control through JWT authentication.

---

## Overview

This project was developed as a personal backend practice project to explore the design of real-world reservation systems.

The API handles the complete booking lifecycle while enforcing business constraints commonly found in hospitality systems, such as room availability validation, authorization rules, and cancellation policies.

Unlike a simple CRUD application, this project emphasizes backend architecture and domain logic.

---

## Features

- JWT authentication and authorization
- BCrypt password hashing
- Role-based access control (`ADMIN` and `GUEST`)
- Guest registration and management
- Room management
- Reservation creation and management
- Room availability validation
- Business rule enforcement
- Exception-based error handling
- RESTful API design
- Automatic synchronization between User and Guest entities

---

## Tech Stack

- Java
- MySQL
- JWT
- BCrypt
- IntelliJ IDEA

---

## Architecture

The application follows a layered architecture to separate responsibilities and improve maintainability.

```text
Controller
    ↓
Service
    ↓
DAO
    ↓
MySQL Database
```

### Layers

#### Controller

Responsible for receiving HTTP requests and returning responses.

- Request validation
- Route handling
- Authentication entry points

#### Service

Contains the business logic of the application.

Examples:

- Reservation validation
- Authorization checks
- Cancellation policies
- User and guest synchronization

#### DAO

Handles data persistence and database interactions.

Examples:

- CRUD operations
- Query execution
- Entity retrieval

---

## Core Business Rules

### Room Availability

Reservations cannot overlap for the same room.

If a room is already booked during the selected period:

```http
409 Conflict
```

```json
{
    "error": "Room unavailable"
}
```

---

### Authorization

Protected operations validate the authenticated user's role.

Attempting to access resources without sufficient permissions returns:

```http
403 Forbidden
```

```json
{
    "error": "Access denied"
}
```

---

### Cancellation Policy

Reservations cannot be cancelled within 24 hours of the check-in date.

Attempts to do so return:

```http
400 Bad Request
```

```json
{
    "error": "Cannot cancel within 24 hours"
}
```

---

## Authentication

### Login

```http
POST /api/auth/login
```

Request:

```json
{
    "email": "admin@hotel.com",
    "password": "••••••"
}
```

Response:

```json
{
    "token": "eyJhbGciOi...",
    "role": "ADMIN"
}
```

---

## Example Endpoints

### Create Reservation

```http
POST /api/bookings
```

Request:

```json
{
    "guestId": 12,
    "roomId": 5,
    "checkIn": "2026-07-10",
    "checkOut": "2026-07-13"
}
```

Successful response:

```http
201 Created
```

```json
{
    "message": "Booking created successfully"
}
```

---

### Cancel Reservation

```http
PATCH /api/bookings/{id}/cancel
```

---

### Delete Reservation

```http
DELETE /api/bookings/{id}
```

---

## Main Entities

### User

Represents authenticated users of the system.

Responsibilities:

- Authentication
- Authorization
- Role assignment

---

### Guest

Represents hotel guests associated with reservations.

Responsibilities:

- Personal information
- Reservation ownership

---

### Room

Represents hotel rooms available for booking.

Responsibilities:

- Availability management
- Capacity and room information

---

### Booking

Represents reservations made by guests.

Responsibilities:

- Check-in and check-out dates
- Validation of business rules
- Booking lifecycle management

---

## Project Structure

```text
src/
├── controller/
├── dao/
├── exception/
├── lib/
├── model/
├── service/
└── util/
```

---

## Running the Project

### Requirements

- JDK 17+
- MySQL
- IntelliJ IDEA

### Setup

1. Clone the repository.

```bash
git clone https://github.com/cburgoin-dev/hotel-reservation-api.git
```

2. Configure the database connection.

3. Create the required database schema.

4. Run the application.

5. Test the endpoints using Postman or any API client.

---

## Future Improvements

Potential improvements for future iterations include:

- Unit testing
- Integration testing
- OpenAPI / Swagger documentation
- Docker support
- CI/CD pipelines
- Automated database migrations
- Frontend client implementation

---

## Purpose

This repository is intended to showcase backend development practices, including layered architecture, authentication, authorization, and business rule implementation in Java.

It serves as a portfolio project demonstrating the design and implementation of a reservation management API beyond basic CRUD functionality.

---

## License

This project is licensed under the MIT License.

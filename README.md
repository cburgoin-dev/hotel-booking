# Hotel Reservation System (Backend) – Personal Project

Reservation system for a specific hotel.

## Description
Backend system for managing hotel room reservations, implemented in Java with MySQL. Built with a modular MVC architecture to ensure maintainable and scalable code. Supports room availability, guest management, booking creation, role-based security, and robust business rule validation.

## Project Structure
- `/src` → Java source code
  - `/controller` → Handles HTTP requests and routes them to the appropriate services
  - `/dao` → Data Access Objects for database operations
    - `/testing` → Test scripts for DAOs and database interactions
  - `/exception` → Custom exceptions and error handling
  - `/lib` → External libraries (e.g., MySQL Connector)
  - `/model` → Core entities and data models (User, Guest, Booking, Room)
  - `/service` → Business logic and service layer
  - `/util` → Utility classes and helper functions
  - `.env` → Environment variables and configuration
- `WebContent` → Static web resources (if any, for frontend integration)
- `.gitignore` → Ignored files

## Features & Highlights
- Full CRUD functionality for Users, Guests, Bookings, and Rooms, with comprehensive validation and exception handling.
- Role-based access control with JWT authentication (ADMIN and GUEST roles).
- Automatic association between User and Guest entities, synchronizing shared data (email, name, phone).
- Enforces business rules for reservation validation and dynamic pricing.
- Modular backend architecture for scalability and frontend integration, ready for API consumption.

## Main Entities
- **Room** → Hotel rooms
- **Guest** → Registered guests
- **Booking** → Made reservations
- **User** → Users

## Dependencies
- JDK 17+
- MySQL / XAMPP
- External libraries in `/lib`

## How to Run
Clone the repository:
  git clone <repository_url>
1. Open the project in IntelliJ IDEA
2. Configure the database connection in `DatabaseConnection.java`
3. Run the main class
4. Test endpoints with Postman or similar tools.

## Next Steps
- Develop a frontend to consume backend endpoints, including login and CRUD forms.
- Add unit and integration tests to improve reliability and maintainability.
- Expand features for reporting and analytics on bookings and room occupancy.

## License
[MIT](LICENSE) (optional)

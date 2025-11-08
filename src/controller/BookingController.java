package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.Booking;
import service.BookingService;
import service.RoomService;
import util.SecurityUtil;

import java.io.IOException;
import java.util.Map;

public class BookingController extends BaseController {
    private static final String BASE_PATH = "/api/bookings";
    private final BookingService bookingService;

    public BookingController() {
        this.bookingService = new BookingService();
    }

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!SecurityUtil.isAuthorized(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.info("Received request: " + method + " " + path);

        try {
            switch (method) {
                case "POST":
                    if (path.matches(BASE_PATH + "/?$")) {
                        handleCreate(exchange);
                    }
                    break;

                case "GET":
                    if (path.matches(BASE_PATH + "/\\d+")) {
                        handleGetById(exchange);
                    } else {
                        handleGetAll(exchange);
                    }
                    break;

                case "PATCH":
                    if (path.matches(BASE_PATH + "/\\d+/confirm")) {
                        handleConfirm(exchange);
                    } else if (path.matches(BASE_PATH + "/\\d+/checkin")) {
                        handleCheckIn(exchange);
                    } else if (path.matches(BASE_PATH + "/\\d+/checkout")) {
                        handleCheckOut(exchange);
                    } else if (path.matches(BASE_PATH + "/\\d+/cancel")) {
                        handleCancel(exchange);
                    } else if (path.matches(BASE_PATH + "/\\d+/status")) {
                        handleStatusUpdate(exchange);
                    }
                    break;

                case "PUT":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleUpdate(exchange);
                    }
                    break;

                case "DELETE":
                    if (path.matches(BASE_PATH + "/\\d+")) {
                        handleDelete(exchange);
                    }
                    break;

                default:
                    sendJsonResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.warning("Unexpected error in BookingController: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in GET request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            Booking booking = bookingService.getBookingById(id);
            sendJsonResponse(exchange, 200, booking);

        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            sendJsonResponse(exchange, 200, bookingService.getAllBookings());

        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            Booking booking = gson.fromJson(requestBody, Booking.class);
            bookingService.createBooking(booking);

            logger.info("Booking created successfully: ID=" + booking.getId());
            sendJsonResponse(exchange, 201, Map.of("message", "Booking created successfully", "booking", booking));

        } catch (BookingException |
                 RoomUnavailableException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PUT request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            Booking updatedBooking = gson.fromJson(requestBody, Booking.class);
            updatedBooking.setId(id);
            bookingService.updateBooking(updatedBooking);

            logger.info("Booking updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking updated successfully", "booking", updatedBooking));

        } catch (BookingException |
                 RoomUnavailableException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleConfirm(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            Booking booking = bookingService.getBookingById(id);
            bookingService.confirmBooking(booking);

            logger.info("Booking confirmed successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking confirmed successfully", "booking", booking));
        } catch (InvalidStatusException e) {
            handleInvalidStatus(exchange, e);
        } catch (BookingException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCheckIn(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            Booking booking = bookingService.getBookingById(id);
            bookingService.checkInBooking(booking);

            logger.info("Booking checked-in successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking checked-in successfully", "booking", booking));
        } catch (BookingException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCheckOut(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            Booking booking = bookingService.getBookingById(id);
            bookingService.checkOutBooking(booking);

            logger.info("Booking checked-out successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking checked-out successfully", "booking", booking));
        } catch (InvalidStatusException e) {
            handleInvalidStatus(exchange, e);
        } catch (BookingException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCancel(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            Booking booking = bookingService.getBookingById(id);
            bookingService.cancelBooking(booking);

            logger.info("Booking cancelled successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking cancelled successfully", "booking", booking));
        } catch (InvalidStatusException e) {
            handleInvalidStatus(exchange, e);
        } catch (BookingException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleStatusUpdate(HttpExchange exchange) throws IOException {
        try {
            if (!SecurityUtil.isAdmin(exchange)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                return;
            }

            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> body = gson.fromJson(requestBody, Map.class);
            String newStatus = body.get("status");

            bookingService.updateBookingStatus(id, newStatus);

            logger.info("Booking status changed: ID=" + id + " -> " + newStatus);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking status updated", "status", newStatus));
        } catch (InvalidStatusException e) {
            handleInvalidStatus(exchange, e);
        } catch (BookingException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        try {
            if (!SecurityUtil.isAdmin(exchange)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                return;
            }

            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid booking ID received in DELETE request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            bookingService.deleteBooking(id);

            logger.info("Booking permanently deleted: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Booking permanently deleted"));
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }
}

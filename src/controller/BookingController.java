package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.BookingException;
import model.Booking;
import service.BookingService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BookingController implements HttpHandler {
    private final BookingService bookingService = new BookingService();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received request: " + exchange.getRequestMethod());

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "POST":
                if (path.matches("/api/bookings/?")) {
                    handleCreateBooking(exchange);
                } else if (path.matches("/api/bookings/\\d+/confirm")) {
                    handleConfirmBooking(exchange);
                } else if (path.matches("/api/bookings/\\d+/checkin")) {
                    handleCheckInBooking(exchange);
                } else if (path.matches("/api/bookings/\\d+/checkout")) {
                    handleCheckOutBooking(exchange);
                }
                break;
            case "GET":
                if (path.matches("/api/bookings/\\d+")) {
                    handleGetBookingById(exchange);
                } else {
                    handleGetAllBookings(exchange);
                }
                break;
            case "PUT":
                handleUpdateBooking(exchange);
                break;
            case "DELETE":
                handleCancelBooking(exchange);
                break;
            default:
                sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleGetBookingById(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[parts.length - 1]);

            Booking booking = bookingService.getBookingById(id);

            if (booking != null) {
                String jsonResponse = gson.toJson(booking);
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "Booking not found");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Error getting booking: " + e.getMessage()));
        }
    }

    private void handleGetAllBookings(HttpExchange exchange) throws IOException {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            String jsonResponse = gson.toJson(bookings);
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Error getting bookings: " + e.getMessage()));
        }
    }

    private void handleCreateBooking(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Booking booking = gson.fromJson(requestBody, Booking.class);
            boolean created = bookingService.createBooking(booking);

            if (created) {
                sendResponse(exchange, 201, "Booking created successfully"); // 201: Created
            } else {
                sendResponse(exchange, 400, "Booking could not be created");
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleUpdateBooking(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            if (segments.length < 4) {
                sendResponse(exchange, 400, gson.toJson("Booking ID is missing in the URL"));
                return;
            }

            int bookingId = Integer.parseInt(segments[3]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Booking updatedBooking = gson.fromJson(requestBody, Booking.class);

            updatedBooking.setId(bookingId);

            boolean success = bookingService.updateBooking(updatedBooking);

            if (success) {
                sendResponse(exchange, 200, gson.toJson("Booking updated successfully"));
            } else {
                sendResponse(exchange, 400, gson.toJson("Booking could not be updated"));
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleConfirmBooking(HttpExchange exchange) throws IOException {
        try {
            int bookingId = extractBookingId(exchange.getRequestURI().getPath());
            Booking booking = bookingService.getBookingById(bookingId);
            boolean success = bookingService.confirmBooking(booking);

            if (success) {
                sendResponse(exchange, 200, gson.toJson("Booking confirmed successfully"));
            } else {
                sendResponse(exchange, 400, gson.toJson("Could not confirm booking"));
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleCheckInBooking(HttpExchange exchange) throws IOException {
        try {
            int bookingId = extractBookingId(exchange.getRequestURI().getPath());
            Booking booking = bookingService.getBookingById(bookingId);
            boolean success = bookingService.checkInBooking(booking);

            if (success) {
                sendResponse(exchange, 200, gson.toJson("Checked in successfully"));
            } else {
                sendResponse(exchange, 400, gson.toJson("Could not check in"));
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleCheckOutBooking(HttpExchange exchange) throws IOException {
        try {
            int bookingId = extractBookingId(exchange.getRequestURI().getPath());
            Booking booking = bookingService.getBookingById(bookingId);
            boolean success = bookingService.checkOutBooking(booking);

            if (success) {
                sendResponse(exchange, 200, gson.toJson("Checked out successfully"));
            } else {
                sendResponse(exchange, 400, gson.toJson("Could not check out"));
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleCancelBooking(HttpExchange exchange) throws IOException {
        try {
            int bookingId = extractBookingId(exchange.getRequestURI().getPath());
            Booking booking = bookingService.getBookingById(bookingId);
            boolean success = bookingService.cancelBooking(booking);

            if (success) {
                sendResponse(exchange, 200, gson.toJson("Booking cancelled successfully"));
            } else {
                sendResponse(exchange, 400, gson.toJson("Could not cancel booking"));
            }
        } catch (BookingException e) {
            handleBusinessException(exchange, e);
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson("Internal server error: " + e.getMessage()));
        }
    }

    private void handleBusinessException(HttpExchange exchange, BookingException e) throws IOException {
        String message = gson.toJson(e.getMessage());
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(400, message.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }

    private int extractBookingId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[3]); // 4th segment is the ID
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

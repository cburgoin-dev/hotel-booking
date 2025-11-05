package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

        switch (method) {
            case "GET":
                handleGetAllBookings(exchange);
                break;
            case "POST":
                handleCreateBooking(exchange);
                break;
            default:
                sendResponse(exchange, 405, "Method not allowed");
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
            System.out.println("Reading request...");
            System.out.println(requestBody);
            Booking booking = gson.fromJson(requestBody, Booking.class);
            boolean created = bookingService.createBooking(booking);

            if (created) {
                String success = gson.toJson("Booking created successfully");
                sendResponse(exchange, 201, success); // 201: Created
            } else {
                String fail = gson.toJson("Booking could not be created");
                sendResponse(exchange, 400, fail);
            }
        } catch (Exception e) {
            String error = gson.toJson("Error creating booking: " + e.getMessage());
            sendResponse(exchange, 500, error);
        }
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

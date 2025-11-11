package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.DAOException;
import exception.InvalidStatusException;
import exception.NotFoundException;
import model.User;
import service.UserService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseController implements HttpHandler {
    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public abstract void handle(HttpExchange exchange) throws IOException;

    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = (body instanceof String) ? (String) body : gson.toJson(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try(OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        sendJsonResponse(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
    }

    protected void handleNotFound(HttpExchange exchange, NotFoundException e) throws IOException {
        logger.warning("Resource not found: " + e.getMessage());
        sendJsonResponse(exchange, 404, Map.of("error", e.getMessage()));
    }

    protected void handleDAOException(HttpExchange exchange, DAOException e) throws IOException {
        logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e);
        sendJsonResponse(exchange, 500, Map.of("error", "Database error: " + e.getMessage()));
    }

    protected void handleValidationError(HttpExchange exchange, Exception e) throws IOException {
        logger.warning("Validation error: " + e.getMessage());
        sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
    }

    protected void handleInvalidStatus(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof InvalidStatusException ex) {
            logger.warning(String.format(
                    "Invalid status value received for entity '%s': '%s'", ex.getEntity(), ex.getInvalidValue()
            ));
        } else {
            logger.warning("Invalid status value received: " + e.getMessage());
        }
        sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
    }

    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {

            }
        }
        throw new IllegalArgumentException("Invalid or missing ID in URL: " + path);
    }

    protected Map<String, String> parseQueryParams(String query) {
        return Map.ofEntries(
                Arrays.stream(query.split("&"))
                        .map(s -> s.split("=", 2))
                        .map(arr -> Map.entry(arr[0], arr.length > 1 ? arr[1] : "")).toArray(Map.Entry[]::new)
        );
    }

    protected User authenticateRequest(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warning("Missing or invalid Authorization header");
            sendJsonResponse(exchange, 401, Map.of("error", "Missing or invalid token"));
            return null;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            logger.warning("Invalid or expired JWT token");
            sendJsonResponse(exchange, 401, Map.of("error", "Invalid or expired token"));
            return null;
        }

        String email = JwtUtil.extractEmail(token);
        UserService userService = new UserService();
        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (NotFoundException e) {
            logger.warning("User not found for token: " + email);
            sendJsonResponse(exchange, 401, Map.of("error", "User not found"));
            return null;
        }

        if (!user.isActive()) {
            logger.warning("User is inactive: " + email);
            sendJsonResponse(exchange, 403, Map.of("error", "User is inactive"));
            return null;
        }

        logger.info("Authenticated user: " + user.getEmail());
        return user;
    }
}

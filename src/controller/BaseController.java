package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.DAOException;
import exception.InvalidStatusException;
import exception.NotFoundException;

import java.io.IOException;
import java.io.OutputStream;
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
}

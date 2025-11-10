package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.User;
import service.UserService;
import util.JwtUtil;

import java.io.IOException;
import java.util.Map;

public class AuthController extends BaseController {
    private static final String BASE_PATH = "/api/auth";
    private final UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.info("Received request: " + method + " " + path);

        try {
            switch (method) {
                case "POST":
                    if (path.matches(BASE_PATH + "/login/?$")) {
                        handleLogin(exchange);
                    } else if (path.matches(BASE_PATH + "/register/?$")) {
                        handleRegister(exchange);
                    } else if (path.matches(BASE_PATH + "/logout/?$")) {
                        handleLogout(exchange);
                    } else {
                        logger.warning("Invalid endpoint: " + path);
                        sendJsonResponse(exchange, 404, Map.of("error", "Endpoint not found"));
                    }
                    break;
                default:
                    sendJsonResponse(exchange, 405, Map.of("error", "Method not allowed"));
            }
        } catch (Exception e) {
            logger.warning("Unexpected error in AuthController: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> data = gson.fromJson(body, Map.class);
        String email = data.get("email");
        String password = data.get("password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            logger.warning("Login failed: missing email or password");
            sendJsonResponse(exchange, 400, Map.of("error", "Email and password are required"));
            return;
        }

        try {
            User user = userService.authenticate(email, password);
            String token = JwtUtil.generateToken(user);

            logger.info("User logged in successfully: ID=" + user.getId() + ", email=" + user.getEmail());
            sendJsonResponse(exchange, 200, Map.of(
                    "message", "Login successful",
                    "token", token,
                    "user", user
            ));
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (Exception e) {
            logger.warning("Login failed for email=" + email + ": " + e.getMessage());
            sendJsonResponse(exchange, 401, Map.of("error", e.getMessage()));
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        User user = gson.fromJson(body, User.class);

        if (user.getEmail() == null || user.getPasswordHash() == null || user.getEmail().isBlank() || user.getPasswordHash().isBlank()) {
            logger.warning("Registration failed: missing email or password");
            sendJsonResponse(exchange, 400, Map.of("error", "Email and password are required"));
            return;
        }

        try {
            userService.createUser(user);
            logger.info("User registered successfully: ID=" + user.getId() + ", email=" + user.getEmail());
            sendJsonResponse(exchange, 201, Map.of("message", "User registered successfully", "user", user));
        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (Exception e) {
            logger.severe("Unexpected error during registration: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        logger.info("User logged out successfully.");
        sendJsonResponse(exchange, 200, Map.of("message", "Logout successful. Token invalidated on client side."));
    }
}

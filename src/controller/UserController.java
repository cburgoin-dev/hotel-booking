package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.Role;
import model.User;
import service.UserService;
import util.SecurityUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class UserController extends BaseController {
    private static final String BASE_PATH = "/api/users";
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        logger.info("Received request: " + method + " " + path + (query != null ? "?" + query : ""));

        try {
            if (!SecurityUtil.isAuthorized(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            boolean admin = SecurityUtil.isAdmin(exchange);

            switch (method) {
                case "POST":
                    if (!admin) {
                        sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                        return;
                    }
                    if (path.matches(BASE_PATH + "/?$")) {
                        handleCreate(exchange);
                    }
                    break;

                case "GET":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleGetById(exchange);
                    } else if (query != null) {
                        handleGetByQuery(exchange, query);
                    } else {
                        if (!admin) {
                            sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                            return;
                        }
                        handleGetAll(exchange);
                    }
                    break;

                case "PATCH":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handlePartialUpdate(exchange);
                    }
                    break;

                case "PUT":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleUpdate(exchange);
                    }
                    break;

                case "DELETE":
                    if (!admin) {
                        sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                        return;
                    }
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleDelete(exchange);
                    }
                    break;

                default:
                    sendJsonResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.warning("Unexpected error in UserController: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid user ID received in GET request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            User user = userService.getUserById(id);
            sendJsonResponse(exchange, 200, user);

        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            sendJsonResponse(exchange, 200, userService.getAllUsers());

        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetByQuery(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        try {
            if (params.containsKey("email")) {
                User user = userService.getUserByEmail(params.get("email"));
                sendJsonResponse(exchange, 200, user);
            } else {
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid query parameter"));
            }
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            User user = gson.fromJson(requestBody, User.class);
            userService.createUser(user);

            logger.info("User created successfully: ID=" + user.getId());
            sendJsonResponse(exchange, 201, Map.of("message", "User created successfully", "user", user));

        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid user ID received in PUT request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            User updatedUser = gson.fromJson(requestBody, User.class);
            updatedUser.setId(id);
            userService.updateUser(updatedUser);

            logger.info("User updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "User updated successfully", "user", updatedUser));

        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handlePartialUpdate(HttpExchange exchange) throws IOException {
        int id = extractIdFromPath(exchange.getRequestURI().getPath());
        if (id <= 0) {
            logger.warning("Invalid user ID received in PATCH request: " + id);
            sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        logger.fine("Request body: " + requestBody);

        Map<String, Object> updates = gson.fromJson(requestBody, Map.class);

        try {
            User current = userService.getUserById(id);
            if (updates.containsKey("guestId")) {
                current.setGuestId((Integer) updates.get("guestId"));
            }
            if (updates.containsKey("email")) {
                current.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("passwordHash")) {
                current.setPasswordHash((String) updates.get("passwordHash"));
            }
            if (updates.containsKey("role")) {
                current.setRole(Role.valueOf((String) updates.get("role")));
            }
            if (updates.containsKey("isActive")) {
                current.setActive(((Boolean) updates.get("isActive")));
            }

            userService.updateUser(current);

            logger.info("User updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "User updated successfully", "user", current));

        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid user ID received in DELETE request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            userService.deleteUser(id);

            logger.info("User permanently deleted: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "User permanently deleted"));
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }
}

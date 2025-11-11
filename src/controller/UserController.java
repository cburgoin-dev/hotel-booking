package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.Role;
import model.User;
import service.UserService;

import java.io.IOException;
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
        User authenticatedUser = authenticateRequest(exchange);

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        logger.info("Received request: " + method + " " + path + (query != null ? "?" + query : ""));

        try {
            switch (method) {
                case "POST":
                    if (path.matches(BASE_PATH + "/?$")) {
                        handleCreate(exchange, authenticatedUser);
                    }
                    break;

                case "GET":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleGetById(exchange, authenticatedUser);
                    } else if (query != null) {
                        handleGetByQuery(exchange, query, authenticatedUser);
                    } else {
                        handleGetAll(exchange, authenticatedUser);
                    }
                    break;

                case "PATCH":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handlePartialUpdate(exchange, authenticatedUser);
                    }
                    break;

                case "PUT":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleUpdate(exchange, authenticatedUser);
                    }
                    break;

                case "DELETE":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleDelete(exchange, authenticatedUser);
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

    private void handleGetById(HttpExchange exchange, User authenticatedUser) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid user ID received in GET request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            User user = userService.getUserById(id);
            if (!canAccessUser(user, authenticatedUser)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                return;
            }
            sendJsonResponse(exchange, 200, user);

        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetAll(HttpExchange exchange, User authenticatedUser) throws IOException {
        try {
            if (!"ADMIN".equals(authenticatedUser.getRole().name())) {
                sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                return;
            }
            sendJsonResponse(exchange, 200, userService.getAllUsers());

        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetByQuery(HttpExchange exchange, String query, User authenticatedUser) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        if (params.containsKey("name") && params.containsKey("email")) {
            sendJsonResponse(exchange, 400, Map.of("error", "Only one query parameter allowed (name or email)"));
        }
        try {
            if (params.containsKey("name")) {
                User user = userService.getUserByName(params.get("name"));
                if (!canAccessUser(user, authenticatedUser)) {
                    sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                    return;
                }
                sendJsonResponse(exchange, 200, user);
            } else if (params.containsKey("email")) {
                User user = userService.getUserByEmail(params.get("email"));
                if (!canAccessUser(user, authenticatedUser)) {
                    sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                    return;
                }
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

    private void handleCreate(HttpExchange exchange, User authenticatedUser) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            User user = gson.fromJson(requestBody, User.class);
            userService.createUser(user);

            logger.info("User created successfully: ID=" + user.getId());
            sendJsonResponse(exchange, 201, Map.of("message", "User created successfully", "user", user));

        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleUpdate(HttpExchange exchange, User authenticatedUser) throws IOException {
        try {
            if (!"ADMIN".equals(authenticatedUser.getRole().name())) {
                sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                return;
            }

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

    private void handlePartialUpdate(HttpExchange exchange, User authenticatedUser) throws IOException {
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
            if (!canAccessUser(current, authenticatedUser)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                return;
            }

            if (updates.containsKey("guestId")) {
                current.setGuestId((Integer) updates.get("guestId"));
            }
            if (updates.containsKey("firstName")) {
                current.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                current.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                current.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("passwordHash")) {
                current.setPasswordHash((String) updates.get("passwordHash"));
            }
            if (updates.containsKey("phone")) {
                current.setPhone((String) updates.get("phone"));
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

    private void handleDelete(HttpExchange exchange, User authenticatedUser) throws IOException {
        try {
            if (!"ADMIN".equals(authenticatedUser.getRole().name())) {
                sendJsonResponse(exchange, 403, Map.of("error", "Access denied"));
                return;
            }

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

    private boolean canAccessUser(User user, User authenticatedUser) {
        return "ADMIN".equals(user.getRole().name()) || user.getId().equals(authenticatedUser.getId());
    }
}

package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.Guest;
import service.GuestService;
import util.SecurityUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class GuestController extends BaseController {
    private static final String BASE_PATH = "/api/guests";
    private final GuestService guestService;

    public GuestController() {
        this.guestService = new GuestService();
    }

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!SecurityUtil.isAuthorized(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        logger.info("Received request: " + method + " " + path + (query != null ? "?" + query : ""));

        try {
            switch (method) {
                case "POST":
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
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleDelete(exchange);
                    }
                    break;

                default:
                    sendJsonResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            logger.warning("Unexpected error in GuestController: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid guest ID received in GET request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            Guest guest = guestService.getGuestById(id);
            sendJsonResponse(exchange, 200, guest);

        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            if (!SecurityUtil.isAdmin(exchange)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                return;
            }

            sendJsonResponse(exchange, 200, guestService.getAllGuests());

        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetByQuery(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        try {
            if (params.containsKey("name")) {
                Guest guest = guestService.getGuestByName(params.get("name"));
                sendJsonResponse(exchange, 200, guest);
            } else if (params.containsKey("email")) {
                Guest guest = guestService.getGuestByEmail(params.get("email"));
                sendJsonResponse(exchange, 200, guest);
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

            Guest guest = gson.fromJson(requestBody, Guest.class);
            guestService.createGuest(guest);

            logger.info("Guest created successfully: ID=" + guest.getId());
            sendJsonResponse(exchange, 201, Map.of("message", "Guest created successfully", "guest", guest));

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
                logger.warning("Invalid guest ID received in PUT request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            Guest updatedGuest = gson.fromJson(requestBody, Guest.class);
            updatedGuest.setId(id);
            guestService.updateGuest(updatedGuest);

            logger.info("Guest updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Guest updated successfully", "guest", updatedGuest));

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
            logger.warning("Invalid guest ID received in PATCH request: " + id);
            sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        logger.fine("Request body: " + requestBody);

        Map<String, Object> updates = gson.fromJson(requestBody, Map.class);

        try {
            Guest current = guestService.getGuestById(id);
            if (updates.containsKey("firstName")) {
                current.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                current.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                current.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("phone")) {
                current.setPhone((String) updates.get("phone"));
            }

            guestService.updateGuest(current);

            logger.info("Guest updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Guest updated successfully", "guest", current));

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
            if (!SecurityUtil.isAdmin(exchange)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Forbidden"));
                return;
            }

            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid guest ID received in DELETE request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            guestService.deleteGuest(id);

            logger.info("Guest permanently deleted: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Guest permanently deleted"));
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        return Map.ofEntries(
                Arrays.stream(query.split("&"))
                        .map(s -> s.split("=", 2))
                        .map(arr -> Map.entry(arr[0], arr.length > 1 ? arr[1] : "")).toArray(Map.Entry[]::new)
        );
    }
}

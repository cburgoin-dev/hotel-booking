package controller;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import model.Role;
import model.Room;
import model.RoomStatus;
import model.User;
import service.RoomService;
import util.SecurityUtil;

import java.io.IOException;
import java.util.Map;

public class RoomController extends BaseController {
    private static final String BASE_PATH = "/api/rooms";
    private final RoomService roomService;

    public RoomController() {
        this.roomService = new RoomService();
    }

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        logger.info("Received request: " + method + " " + path);

        try {
            if (!SecurityUtil.isAdmin(exchange)) {
                sendJsonResponse(exchange, 403, Map.of("error", "Admin privileges required"));
                return;
            }

            switch (method) {
                case "POST":
                    if (path.matches(BASE_PATH + "/?$")) {
                        handleCreate(exchange);
                    }
                    break;

                case "GET":
                    if (path.matches(BASE_PATH + "/\\d+$")) {
                        handleGetById(exchange);
                    } else {
                        handleGetAll(exchange);
                    }
                    break;

                case "PATCH":
                    if (path.matches(BASE_PATH + "/\\d+/status$")) {
                        handleStatusUpdate(exchange);
                    } else if (path.matches(BASE_PATH + "/\\d+$")) {
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
            logger.warning("Unexpected error in RoomController: " + e.getMessage());
            handleException(exchange, e);
        }
    }

    private void handleGetById(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid room ID received in GET request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            Room room = roomService.getRoomById(id);
            sendJsonResponse(exchange, 200, room);

        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            sendJsonResponse(exchange, 200, roomService.getAllRooms());

        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            Room room = gson.fromJson(requestBody, Room.class);
            roomService.createRoom(room);

            logger.info("Room created successfully: ID=" + room.getId());
            sendJsonResponse(exchange, 201, Map.of("message", "Room created successfully", "room", room));

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
                logger.warning("Invalid room ID received in PUT request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            logger.fine("Request body: " + requestBody);

            Room updatedRoom = gson.fromJson(requestBody, Room.class);
            updatedRoom.setId(id);
            roomService.updateRoom(updatedRoom);

            logger.info("Room updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Room updated successfully", "room", updatedRoom));

        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handleStatusUpdate(HttpExchange exchange) throws IOException {
        try {
            int id = extractIdFromPath(exchange.getRequestURI().getPath());
            if (id <= 0) {
                logger.warning("Invalid room ID received in PATCH request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> body = gson.fromJson(requestBody, Map.class);
            String newStatus = body.get("status");

            roomService.updateRoomStatus(id, newStatus);

            logger.info("Room status changed: ID=" + id + " -> " + newStatus);
            sendJsonResponse(exchange, 200, Map.of("message", "Room status updated", "status", newStatus));

        } catch (InvalidStatusException e) {
            handleInvalidStatus(exchange, e);
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }

    private void handlePartialUpdate(HttpExchange exchange) throws IOException {
        int id = extractIdFromPath(exchange.getRequestURI().getPath());
        if (id <= 0) {
            logger.warning("Invalid room ID received in PATCH request: " + id);
            sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        logger.fine("Request body: " + requestBody);

        Map<String, Object> updates = gson.fromJson(requestBody, Map.class);

        try {
            Room current = roomService.getRoomById(id);
            if (updates.containsKey("number")) {
                current.setNumber((String) updates.get("number"));
            }
            if (updates.containsKey("type")) {
                current.setType((String) updates.get("type"));
            }
            if (updates.containsKey("pricePerNight")) {
                current.setPricePerNight(((Number) updates.get("pricePerNight")).doubleValue());
            }
            if (updates.containsKey("extraGuestPricePerNight")) {
                current.setExtraGuestPricePerNight(((Number) updates.get("extraGuestPricePerNight")).doubleValue());
            }
            if (updates.containsKey("capacity")) {
                current.setCapacity(((Number) updates.get("capacity")).intValue());
            }
            if (updates.containsKey("allowedExtraGuests")) {
                current.setAllowedExtraGuests(((Number) updates.get("allowedExtraGuests")).intValue());
            }
            if (updates.containsKey("status")) {
                current.setStatus(RoomStatus.valueOf((String) updates.get("status")));
            }

            roomService.updateRoom(current);

            logger.info("Room updated successfully: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Room updated successfully", "room", current));

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
                logger.warning("Invalid room ID received in DELETE request: " + id);
                sendJsonResponse(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }
            roomService.deleteRoom(id);

            logger.info("Room permanently deleted: ID=" + id);
            sendJsonResponse(exchange, 200, Map.of("message", "Room permanently deleted"));
        } catch (NotFoundException e) {
            handleNotFound(exchange, e);
        } catch (DAOException e) {
            handleDAOException(exchange, e);
        }
    }
}

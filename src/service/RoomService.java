package service;

import dao.RoomDAO;
import exception.*;
import model.Room;
import model.RoomStatus;

import java.util.List;
import java.util.logging.Logger;

public class RoomService {
    private final static Logger logger = Logger.getLogger(RoomService.class.getName());
    private final RoomDAO roomDAO;

    public RoomService(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    public RoomService() {
        this(new RoomDAO());
    }

    public Room getRoomById(int id) throws DAOException, NotFoundException {
        return roomDAO.findById(id);
    }

    public List<Room> getAllRooms() throws DAOException {
        return roomDAO.getAll();
    }

    public void createRoom(Room room) throws DAOException, RoomInvalidException, RoomInvalidCapacityException, RoomInvalidPriceException, RoomNumberEmptyException {
        logger.info("Attempting to create room: roomId=" + room.getId());
        validateRoom(room);
        roomDAO.insert(room);
        logger.info("Room created successfully: roomId=" + room.getId());
    }

    public void updateRoom(Room room) throws DAOException, NotFoundException, RoomInvalidException, RoomInvalidCapacityException, RoomInvalidPriceException, RoomNumberEmptyException {
        logger.info("Attempting to update room: roomId=" + room.getId());
        validateRoom(room);
        roomDAO.update(room);
        logger.info("Room updated successfully: roomId=" + room.getId());
    }

    public void updateRoomStatus(int id, String status) throws DAOException, NotFoundException, InvalidStatusException {
        logger.info("Attempting to update room status: roomId=" + id + ", newStatus=" + status);
        RoomStatus newStatus = RoomStatus.fromString(status);
        roomDAO.updateStatus(id, newStatus);
        logger.info("Room status updated successfully: roomId=" + id + ", newStatus=" + newStatus);
    }

    public void deleteRoom(int id) throws DAOException, NotFoundException {
        logger.info("Attempting to delete room: roomId=" + id);
        roomDAO.delete(id);
        logger.info("Room deleted successfully: roomId=" + id);
    }

    public double getRoomPricePerNight(int id) throws DAOException, NotFoundException {
        return roomDAO.getRoomPricePerNight(id);
    }

    public double getRoomExtraGuestPricePerNight(int id) throws DAOException, NotFoundException {
        return roomDAO.getRoomExtraGuestPricePerNight(id);
    }

    public int getRoomCapacity(int id) throws DAOException, NotFoundException {
        return roomDAO.getRoomCapacity(id);
    }

    public int getAllowedExtraGuests(int id) throws DAOException, NotFoundException {
        return roomDAO.getRoomAllowedExtraGuests(id);
    }

    private void validateRoom(Room room) throws RoomInvalidException, RoomNumberEmptyException, RoomInvalidPriceException, RoomInvalidCapacityException{
        if (room == null) {
            logger.warning("Invalid room: room is null");
            throw new RoomInvalidException();
        }
        if (room.getNumber() == null || room.getNumber().isEmpty()) {
            logger.warning("Invalid room number: roomNumber is null or empty");
            throw new RoomNumberEmptyException();
        }
        if (room.getPricePerNight() <= 0) {
            logger.warning("Invalid room price per night: roomPricePerNight=" + room.getPricePerNight());
            throw new RoomInvalidPriceException();
        }
        if (room.getCapacity() <= 0) {
            logger.warning("Invalid room capacity: roomCapacity=" + room.getCapacity());
            throw new RoomInvalidCapacityException();
        }
    }
}

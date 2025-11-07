package service;

import dao.RoomDAO;
import exception.*;
import model.Room;

import java.util.List;
import java.util.logging.Logger;

public class RoomService {
    private final static Logger logger = Logger.getLogger(RoomService.class.getName());
    private final RoomDAO roomDao = new RoomDAO();

    public Room getRoomById(int id) throws DAOException, NotFoundException {
        return roomDao.findById(id);
    }

    public List<Room> getAllRooms() throws DAOException {
        return roomDao.getAll();
    }

    public void createRoom(Room room) throws DAOException, RoomInvalidException, RoomInvalidCapacityException, RoomInvalidPriceException, RoomNumberEmptyException {
        logger.info("Attempting to create room: roomId=" + room.getId());
        validateRoom(room);
        roomDao.insert(room);
        logger.info("Room created successfully: roomId=" + room.getId());
    }

    public void updateRoom(Room room) throws DAOException, NotFoundException, RoomInvalidException, RoomInvalidCapacityException, RoomInvalidPriceException, RoomNumberEmptyException {
        logger.info("Attempting to update room: roomId=" + room.getId());
        validateRoom(room);
        roomDao.update(room);
        logger.info("Room updated successfully: roomId=" + room.getId());
    }

    public void updateRoomStatus(int id, String newStatus) throws DAOException, NotFoundException {
        logger.info("Updating room status: roomId=" + id + ", newStatus=" + newStatus);
        roomDao.updateStatus(id, newStatus);
        logger.info("Room status updated successfully");
    }

    public void markRoomAsAvailable(int id) throws DAOException, NotFoundException{
        logger.info("Marking room as available: roomId=" + id);
        updateRoomStatus(id, "available");
        logger.info("Room marked as available successfully: roomId=" + id);
    }

    public void markRoomAsOccupied(int id) throws DAOException, NotFoundException {
        logger.info("Marking room as occupied: roomId=" + id);
        updateRoomStatus(id, "occupied");
        logger.info("Room marked as occupied successfully: roomId=" + id);
    }

    public void markRoomAsInMaintenance(int id) throws DAOException, NotFoundException {
        logger.info("Marking room as in maintenance: roomId=" + id);
        updateRoomStatus(id, "maintenance");
        logger.info("Room marked as in maintenance successfully: roomId=" + id);
    }

    public double getRoomPricePerNight(int id) throws DAOException, NotFoundException {
        return roomDao.getRoomPricePerNight(id);
    }

    public double getRoomExtraGuestPricePerNight(int id) throws DAOException, NotFoundException {
        return roomDao.getRoomExtraGuestPricePerNight(id);
    }

    public int getRoomCapacity(int id) throws DAOException, NotFoundException {
        return roomDao.getRoomCapacity(id);
    }

    public int getAllowedExtraGuests(int id) throws DAOException, NotFoundException {
        return roomDao.getRoomAllowedExtraGuests(id);
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

package service;

import dao.RoomDAO;

public class RoomService {
    private final RoomDAO dao = new RoomDAO();

    public void markRoomAsOccupied(int roomId) {
        dao.updateStatus(roomId, "occupied");
    }

    public void markRoomAsAvailable(int roomId) {
        dao.updateStatus(roomId, "available");
    }

    public double getRoomPricePerNight(int roomId) {
        return dao.getRoomPricePerNight(roomId);
    }

    public double getRoomExtraGuestPricePerNight(int roomId) {
        return dao.getRoomExtraGuestPricePerNight(roomId);
    }

    public int getRoomCapacity(int roomId) {
        return dao.getRoomCapacity(roomId);
    }

    public int getAllowedExtraGuests(int roomId) {
        return dao.getRoomAllowedExtraGuests(roomId);
    }
}

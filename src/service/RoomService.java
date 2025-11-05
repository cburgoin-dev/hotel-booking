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
}

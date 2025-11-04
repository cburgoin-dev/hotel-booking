package dao;

import dao.RoomDAO;
import model.Room;
import java.util.List;

public class TestRoomDAO {
    public static void main(String[] args) {
        RoomDAO dao = new RoomDAO();

        List<Room> rooms = dao.getAll();
        rooms.forEach(System.out::println);
    }
}

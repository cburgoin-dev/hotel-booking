package dao.Testing;

import dao.RoomDAO;
import model.Room;
import java.util.List;

public class TestRoomDAO {
    public static void main(String[] args) {
        RoomDAO dao = new RoomDAO();

        /*
        Room r1 = new Room("101", "Suite", 150.0, "available");
        dao.insert(r1);

         */

        List<Room> rooms = dao.getAll();
        rooms.forEach(System.out::println);
    }
}

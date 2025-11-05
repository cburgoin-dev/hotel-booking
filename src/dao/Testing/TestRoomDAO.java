package dao.Testing;

import dao.RoomDAO;
import model.Room;
import java.util.List;

public class TestRoomDAO {
    public static void main(String[] args) {
        RoomDAO dao = new RoomDAO();

        /*
        Room r1 = new Room("101", "suite", 150.0, 50.0, 2, 2, "available");
        dao.insert(r1);

         */

        /*
        Room r2 = new Room("102", "single", 120.0, 30.0, 2, 1, "available");
        dao.insert(r2);

         */

        Room r3 = new Room("103", "double", 180.0, .60, 2, 2, "available");
        dao.insert(r3);

        List<Room> rooms = dao.getAll();
        rooms.forEach(System.out::println);
    }
}

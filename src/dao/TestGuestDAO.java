package dao;

import dao.GuestDAO;
import model.Guest;
import java.util.List;

public class TestGuestDAO {
    public static void main(String[] args) {
        GuestDAO dao = new GuestDAO();

        /*
        Guest g1 = new Guest("José Franco", "Rámirez Córdoba", "josefranco@gmail.com", "0000000001");
        dao.insert(g1);
        
         */

        List<Guest> guests = dao.getAll();
        guests.forEach(System.out::println);
    }
}

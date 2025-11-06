package dao.testing;

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

        /*
        Guest g2 = new Guest("Melissa", "Cota Estrada", "melissacota@gmail.com", "0000000002");
        dao.insert(g2);

         */

        /*
        Guest g3 = new Guest("Enrique", "Lúcero Rivas", "enriquelucero@gmail.com", "0000000003");
        dao.insert(g3);

         */

        List<Guest> guests = dao.getAll();
        guests.forEach(System.out::println);
    }
}

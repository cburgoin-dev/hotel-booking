package dao;

import model.Booking;
import java.util.Date;
import java.util.List;

public class TestBookingDAO {
    public static void main(String[] args) {
        BookingDAO dao = new BookingDAO();

        /*
        Booking b1 = new Booking(1, 1, new Date(), new Date(System.currentTimeMillis() + 2L * 24 * 3600 * 1000), 3500.0, "confirmed");
        dao.insert(b1);

         */

        List<Booking> bookings = dao.getAll();
        bookings.forEach(System.out::println);
    }
}

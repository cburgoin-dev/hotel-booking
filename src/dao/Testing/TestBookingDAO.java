package dao.Testing;

import dao.BookingDAO;
import model.Booking;
import service.BookingService;

import java.util.Date;
import java.util.List;

public class TestBookingDAO {
    public static void main(String[] args) {
        BookingDAO dao = new BookingDAO();
        BookingService service = new BookingService();

        /*
        Booking b1 = new Booking(1, 1, new Date(), new Date(System.currentTimeMillis() + 2L * 24 * 3600 * 1000), 2500.0, "confirmed");
        if (service.createBooking(b1)) {
            System.out.println("Booking made successfully");
        } else {
            System.out.println("Couldn't add booking");
        }

         */

        List<Booking> bookings = dao.getAll();
        bookings.forEach(System.out::println);
    }
}

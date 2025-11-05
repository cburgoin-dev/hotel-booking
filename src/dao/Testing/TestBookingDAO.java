package dao.Testing;

import dao.BookingDAO;
import model.Booking;
import service.BookingService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestBookingDAO {
    public static void main(String[] args) throws Exception {
        BookingDAO dao = new BookingDAO();
        BookingService service = new BookingService();
        Calendar cal = Calendar.getInstance();

        cal.set(2025, Calendar.NOVEMBER, 5);
        Date checkIn = cal.getTime();

        cal.set(2025, Calendar.NOVEMBER, 7);
        Date checkOut = cal.getTime();

        Booking b1 = new Booking(2, 2, checkIn, checkOut, 0.0, "pending");
        if (service.createBooking(b1)) {
            System.out.println("Booking made successfully");
        } else {
            System.out.println("Couldn't add booking");
        }

        List<Booking> bookings = dao.getAll();
        bookings.forEach(System.out::println);
    }
}

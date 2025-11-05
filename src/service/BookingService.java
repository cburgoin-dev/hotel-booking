package service;

import dao.BookingDAO;
import model.Booking;

import java.util.Date;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomService roomService = new RoomService();

    public boolean createBooking(Booking booking) throws Exception {
        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            throw new Exception("The booking date is not valid. Please select a valid date.");
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut())) {
            throw new Exception("The booking date overlaps other booking. Please select a valid date.");
        }

        double totalPrice = calculateTotalPrice(booking);
        booking.setTotalPrice(totalPrice);

        if (bookingDAO.insert(booking)) {
            roomService.markRoomAsOccupied(booking.getRoomId());
            return true;
        }

        return false;
    }

    public boolean cancelBooking(Booking booking) {
        bookingDAO.updateStatus(booking.getId(), "cancelled");
        roomService.markRoomAsAvailable(booking.getRoomId());
        return true;
    }

    public boolean completeBooking(Booking booking) {
        bookingDAO.updateStatus(booking.getId(), "checked_out");
        roomService.markRoomAsAvailable(booking.getRoomId());
        return true;
    }

    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut) {
        List<Booking> overlaps = bookingDAO.getOverlappingBookings(roomId, checkIn, checkOut);
        return overlaps.isEmpty();
    }

    public boolean isDateValid(Date checkIn, Date checkOut) {
        Date now = new Date();
        return checkIn.before(checkOut) && checkIn.after(now);
    }

    public double calculateTotalPrice(Booking booking) throws Exception {
        double pricePerNight = roomService.getRoomPricePerNight(booking.getRoomId());

        long diffInMs = booking.getCheckOut().getTime() - booking.getCheckIn().getTime();
        long nights = diffInMs / (1000 * 60 * 60 * 24);

        if (nights <= 0) {
            throw new Exception("Invalid date range: check-in and check-out must be at least one night apart.");
        }

        return pricePerNight * nights;
    }
}
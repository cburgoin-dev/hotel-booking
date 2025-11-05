package service;

import dao.BookingDAO;
import model.Booking;

import java.util.Date;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomService roomService = new RoomService();

    public boolean createBooking(Booking booking) {
        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            return false;
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut())) {
            return false;
        }

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
}
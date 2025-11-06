package service;

import dao.BookingDAO;
import model.Booking;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomService roomService = new RoomService();

    public Booking getBookingById(int id) {
        return bookingDAO.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.getAll();
    }

    public boolean createBooking(Booking booking) throws Exception {
        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            throw new Exception("Cannot create booking: booking date is not valid. Please select a valid date.");
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut(), null)) {
            throw new Exception("Cannot create booking: booking date overlaps other booking. Please select a valid date.");
        }
        if (hasGuestActiveBooking(booking.getGuestId())) {
            throw new Exception("Cannot create booking: guest has already an active booking.");
        }

        int extraGuests = Math.max(0, booking.getNumGuests() - roomService.getRoomCapacity(booking.getRoomId()));
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            throw new Exception("The number of guests exceeds the allowed extra guests capacity");
        }

        double totalPrice = calculateTotalPrice(booking, extraGuests);
        booking.setTotalPrice(totalPrice);

        return bookingDAO.insert(booking);
    }

    public boolean updateBooking(Booking booking) throws Exception {
        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            throw new Exception("Cannot create booking: booking date is not valid. Please select a valid date.");
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut(), booking.getId())) {
            throw new Exception("Cannot create booking: booking date overlaps other booking. Please select a valid date.");
        }

        int extraGuests = Math.max(0, booking.getNumGuests() - roomService.getRoomCapacity(booking.getRoomId()));
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            throw new Exception("The number of guests exceeds the allowed extra guests capacity");
        }

        double totalPrice = calculateTotalPrice(booking, extraGuests);
        booking.setTotalPrice(totalPrice);

        return bookingDAO.update(booking);
    }

    public boolean confirmBooking(Booking booking) throws Exception {
        if (!booking.getStatus().equals("pending")) {
            throw new Exception("Only pending bookings can be confirmed.");
        }
        bookingDAO.updateStatus(booking.getId(), "confirmed");
        roomService.markRoomAsOccupied(booking.getRoomId());
        return true;
    }

    public boolean checkInBooking(Booking booking) throws Exception {
        if (!booking.getStatus().equals("confirmed")) {
            throw new Exception("Cannot check-in: booking must be confirmed first.");
        }

        Date today = new Date();
        if (today.before(booking.getCheckIn()) || today.after(booking.getCheckOut())) {
            throw new Exception("Cannot check-in: outside of valid date range.");
        }

        bookingDAO.updateStatus(booking.getId(), "checked_in");
        return true;
    }

    public boolean checkOutBooking(Booking booking) throws Exception{
        if (!booking.getStatus().equals("checked_in")) {
            throw new Exception("Cannot check-out: booking is not checked-in yet. Please check-in before checking out.");
        }
        bookingDAO.updateStatus(booking.getId(), "checked_out");
        roomService.markRoomAsAvailable(booking.getRoomId());
        return true;
    }

    public boolean cancelBooking(Booking booking) throws Exception {
        if (booking.getStatus().equals("checked_in") || booking.getStatus().equals("checked_out")) {
            throw new Exception("Cannot cancel booking after check-in");
        }
        if (!canCancel(booking)) {
            throw new Exception("Cannot cancel booking 24 hours before check-in");
        }

        bookingDAO.updateStatus(booking.getId(), "cancelled");
        roomService.markRoomAsAvailable(booking.getRoomId());
        return true;
    }

    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut, Integer bookingIdToIgnore) {
        List<Booking> overlaps = bookingDAO.getOverlappingBookings(roomId, checkIn, checkOut, bookingIdToIgnore);
        return overlaps.isEmpty();
    }

    public boolean isDateValid(Date checkIn, Date checkOut) {
        Date now = new Date();
        return checkIn.before(checkOut) && checkOut.after(now);
    }

    public double calculateTotalPrice(Booking booking, int extraGuests) throws Exception {
        double pricePerNight = roomService.getRoomPricePerNight(booking.getRoomId());

        long diffInMs = booking.getCheckOut().getTime() - booking.getCheckIn().getTime();
        long nights = diffInMs / (1000 * 60 * 60 * 24);

        if (nights <= 0) {
            throw new Exception("Invalid date range: check-in and check-out must be at least one night apart.");
        }

        double basePrice = pricePerNight * nights;
        if (extraGuests == 0) {
            return basePrice;
        }

        double extraGuestPricePerNight = roomService.getRoomExtraGuestPricePerNight(booking.getRoomId());
        double extraPrice = extraGuests * extraGuestPricePerNight * nights;

        return basePrice + extraPrice;
    }

    public boolean verifyCapacity(int extraGuests, int roomId) {
        return extraGuests <= roomService.getRoomCapacity(roomId);
    }

    public boolean hasGuestActiveBooking(int guestId) {
        List<Booking> activeBookings = bookingDAO.getBookingsByGuestAndStatus(guestId, Arrays.asList("pending", "confirmed", "checked_in"));

        return !activeBookings.isEmpty();
    }

    public boolean canCancel(Booking booking) {
        long diff = booking.getCheckIn().getTime() - System.currentTimeMillis();
        if (diff <= 0) {
            return false;
        }
        long hours = diff / (1000 * 60 * 60);
        return hours >= 24;
    }
}
package service;

import dao.BookingDAO;
import exception.*;
import model.Booking;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BookingService {
    private final static Logger logger = Logger.getLogger(BookingService.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomService roomService = new RoomService();

    public Booking getBookingById(int id) {
        return bookingDAO.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.getAll();
    }

    public boolean createBooking(Booking booking) throws Exception {
        logger.info("Attempting to create booking for guestId=" + booking.getGuestId() + ", roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());

        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            logger.warning("Invalid booking dates: checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new BookingDateInvalidException();
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut(), null)) {
            logger.warning("Room unavailable: roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new RoomUnavailableException();
        }
        if (hasGuestActiveBooking(booking.getGuestId())) {
            logger.warning("Guest has active booking: guestId=" + booking.getGuestId());
            throw new GuestHasActiveBookingException();
        }

        int extraGuests = Math.max(0, booking.getNumGuests() - roomService.getRoomCapacity(booking.getRoomId()));
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            logger.warning("Capacity exceeded: roomId=" + booking.getRoomId() + ", extraGuests=" + extraGuests);
            throw new CapacityExceededException();
        }

        double totalPrice = calculateTotalPrice(booking, extraGuests);
        booking.setTotalPrice(totalPrice);

        boolean created = bookingDAO.insert(booking);

        if (created) {
            logger.info("Booking created successfully: bookingId=" + booking.getId());
        } else {
            logger.warning("Booking creation failed for guestId=" + booking.getGuestId());
        }

        return created;
    }

    public boolean updateBooking(Booking booking) throws Exception {
        logger.info("Attempting to update booking for guestId=" + booking.getGuestId() + ", roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());

        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            logger.warning("Invalid booking dates: checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new BookingDateInvalidException();
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut(), booking.getId())) {
            logger.warning("Room unavailable: roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new RoomUnavailableException();
        }

        int extraGuests = Math.max(0, booking.getNumGuests() - roomService.getRoomCapacity(booking.getRoomId()));
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            logger.warning("Capacity exceeded: roomId=" + booking.getRoomId() + ", extraGuests=" + extraGuests);
            throw new CapacityExceededException();
        }

        double totalPrice = calculateTotalPrice(booking, extraGuests);
        booking.setTotalPrice(totalPrice);

        boolean updated = bookingDAO.update(booking);

        if (updated) {
            logger.info("Booking updated successfully: bookingId=" + booking.getId());
        } else {
            logger.warning("Booking update failed for guestId=" + booking.getGuestId());
        }

        return updated;
    }

    public boolean confirmBooking(Booking booking) throws Exception {
        logger.info("Attempting to confirm booking: bookingId=" + booking.getId());

        if (!booking.getStatus().equals("pending")) {
            logger.warning("Cannot confirm booking not pending: bookingId=" + booking.getId());
            throw new NotPendingBookingException();
        }

        bookingDAO.updateStatus(booking.getId(), "confirmed");
        roomService.markRoomAsOccupied(booking.getRoomId());

        logger.info("Booking confirmed successfully: bookingID=" + booking.getId());
        return true;
    }

    public boolean checkInBooking(Booking booking) throws Exception {
        logger.info("Attempting to check-in booking: bookingId=" + booking.getId());

        if (!booking.getStatus().equals("confirmed")) {
            logger.warning("Cannot check-in booking not confirmed: bookingId=" + booking.getId());
            throw new NotConfirmedBookingException();
        }

        Date today = new Date();
        if (today.before(booking.getCheckIn()) || today.after(booking.getCheckOut())) {
            logger.warning("Cannot check-in booking outside of a valid date range: bookingId=" + booking.getId());
            throw new InvalidCheckInDateException();
        }

        bookingDAO.updateStatus(booking.getId(), "checked_in");

        logger.info("Booking checked-in successfully: bookingID=" + booking.getId());
        return true;
    }

    public boolean checkOutBooking(Booking booking) throws Exception{
        logger.info("Attempting to check-out booking: bookingId=" + booking.getId());

        if (!booking.getStatus().equals("checked_in")) {
            logger.warning("Cannot check-out booking not checked-in: bookingId=" + booking.getId());
            throw new NotCheckedInBookingException();
        }
        bookingDAO.updateStatus(booking.getId(), "checked_out");
        roomService.markRoomAsAvailable(booking.getRoomId());

        logger.info("Booking checked-out successfully: bookingID=" + booking.getId());
        return true;
    }

    public boolean cancelBooking(Booking booking) throws Exception {
        logger.info("Attempting to cancel booking: bookingId=" + booking.getId());

        if (booking.getStatus().equals("checked_in") || booking.getStatus().equals("checked_out")) {
            logger.warning("Cannot cancel booking after check-in: bookingId=" + booking.getId());
            throw new CannotCancelBookingException(true, false);
        }
        if (!canCancel(booking)) {
            logger.warning("Cannot cancel booking 24 hours before check-in: bookingId=" + booking.getId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new CannotCancelBookingException(false, true);
        }

        bookingDAO.updateStatus(booking.getId(), "cancelled");
        roomService.markRoomAsAvailable(booking.getRoomId());

        logger.info("Booking cancelled successfully: bookingID=" + booking.getId());
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
            throw new InvalidDateRangeException();
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
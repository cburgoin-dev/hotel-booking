package service;

import dao.BookingDAO;
import exception.*;
import model.Booking;
import model.BookingStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BookingService {
    private final static Logger logger = Logger.getLogger(BookingService.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomService roomService = new RoomService();

    public Booking getBookingById(int id) throws DAOException, NotFoundException {
        return bookingDAO.findById(id);
    }

    public List<Booking> getAllBookings() throws DAOException {
        return bookingDAO.getAll();
    }

    public void createBooking(Booking booking) throws DAOException, NotFoundException, BookingDateInvalidException, RoomUnavailableException, GuestHasActiveBookingException, CapacityExceededException, InvalidDateRangeException {
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

        int extraGuests = roomService.getAllowedExtraGuests(booking.getRoomId());
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            logger.warning("Capacity exceeded: roomId=" + booking.getRoomId() + ", extraGuests=" + extraGuests);
            throw new CapacityExceededException();
        }

        booking.setTotalPrice(calculateTotalPrice(booking, extraGuests));
        booking.setStatus(BookingStatus.PENDING);
        bookingDAO.insert(booking);
        logger.info("Booking created successfully: bookingId=" + booking.getId());
    }

    public void updateBooking(Booking booking) throws DAOException, NotFoundException, BookingDateInvalidException, RoomUnavailableException, CapacityExceededException, InvalidDateRangeException {
        logger.info("Attempting to update booking for guestId=" + booking.getGuestId() + ", roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());

        if (!isDateValid(booking.getCheckIn(), booking.getCheckOut())) {
            logger.warning("Invalid booking dates: checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new BookingDateInvalidException();
        }
        if (!isRoomAvailable(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut(), booking.getId())) {
            logger.warning("Room unavailable: roomId=" + booking.getRoomId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new RoomUnavailableException();
        }

        int extraGuests = roomService.getAllowedExtraGuests(booking.getRoomId());
        if (!verifyCapacity(extraGuests, booking.getRoomId())) {
            logger.warning("Capacity exceeded: roomId=" + booking.getRoomId() + ", extraGuests=" + extraGuests);
            throw new CapacityExceededException();
        }

        booking.setTotalPrice(calculateTotalPrice(booking, extraGuests));
        bookingDAO.update(booking);
        logger.info("Booking updated successfully: bookingId=" + booking.getId());
    }

    public void confirmBooking(Booking booking) throws DAOException, NotFoundException, NotPendingBookingException {
        logger.info("Attempting to confirm booking: bookingId=" + booking.getId());

        if (booking.getStatus() != BookingStatus.PENDING) {
            logger.warning("Cannot confirm booking not pending: bookingId=" + booking.getId());
            throw new NotPendingBookingException();
        }

        bookingDAO.updateStatus(booking.getId(), BookingStatus.CONFIRMED);
        roomService.markRoomAsOccupied(booking.getRoomId());
        logger.info("Booking confirmed successfully: bookingID=" + booking.getId());
    }

    public void checkInBooking(Booking booking) throws DAOException, NotFoundException, NotConfirmedBookingException, InvalidCheckInDateException {
        logger.info("Attempting to check-in booking: bookingId=" + booking.getId());

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            logger.warning("Cannot check-in booking not confirmed: bookingId=" + booking.getId());
            throw new NotConfirmedBookingException();
        }

        Date today = new Date();
        if (today.before(booking.getCheckIn()) || today.after(booking.getCheckOut())) {
            logger.warning("Cannot check-in booking outside of a valid date range: bookingId=" + booking.getId());
            throw new InvalidCheckInDateException();
        }

        bookingDAO.updateStatus(booking.getId(), BookingStatus.CHECKED_IN);
        logger.info("Booking checked-in successfully: bookingID=" + booking.getId());
    }

    public void checkOutBooking(Booking booking) throws DAOException, NotFoundException, NotCheckedInBookingException {
        logger.info("Attempting to check-out booking: bookingId=" + booking.getId());

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            logger.warning("Cannot check-out booking not checked-in: bookingId=" + booking.getId());
            throw new NotCheckedInBookingException();
        }

        bookingDAO.updateStatus(booking.getId(), BookingStatus.CHECKED_OUT);
        roomService.markRoomAsAvailable(booking.getRoomId());
        logger.info("Booking checked-out successfully: bookingID=" + booking.getId());
    }

    public void cancelBooking(Booking booking) throws DAOException, NotFoundException, CannotCancelBookingException {
        logger.info("Attempting to cancel booking: bookingId=" + booking.getId());

        if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            logger.warning("Cannot cancel booking after check-in: bookingId=" + booking.getId());
            throw new CannotCancelBookingException(true, false);
        }
        if (!canCancel(booking)) {
            logger.warning("Cannot cancel booking 24 hours before check-in: bookingId=" + booking.getId() + ", checkIn=" + booking.getCheckIn() + ", checkOut=" + booking.getCheckOut());
            throw new CannotCancelBookingException(false, true);
        }

        bookingDAO.updateStatus(booking.getId(), BookingStatus.CANCELLED);
        roomService.markRoomAsAvailable(booking.getRoomId());
        logger.info("Booking cancelled successfully: bookingID=" + booking.getId());
    }

    public void updateBookingStatus(int id, String status) throws DAOException, NotFoundException, BookingException {
        if (!BookingStatus.isValid(status)) {
            throw new InvalidBookingStatusException(status);
        }

        BookingStatus newStatus = BookingStatus.fromString(status);
        bookingDAO.updateStatus(id, newStatus);
        logger.info("Booking status updated successfully: bookingId=" + id + ", newStatus=" + newStatus);
    }

    public void deleteBooking(int id) throws DAOException, NotFoundException {
        bookingDAO.delete(id);
    }

    public boolean isRoomAvailable(int roomId, Date checkIn, Date checkOut, Integer bookingIdToIgnore) throws DAOException {
        List<Booking> overlaps = bookingDAO.getOverlappingBookings(roomId, checkIn, checkOut, bookingIdToIgnore);
        return overlaps.isEmpty();
    }

    public boolean isDateValid(Date checkIn, Date checkOut) {
        Date now = new Date();
        return checkIn.before(checkOut) && checkOut.after(now);
    }

    public double calculateTotalPrice(Booking booking, int extraGuests) throws DAOException, NotFoundException, InvalidDateRangeException {
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

    public boolean verifyCapacity(int extraGuests, int roomId) throws DAOException, NotFoundException {
        return extraGuests <= roomService.getRoomCapacity(roomId);
    }

    public boolean hasGuestActiveBooking(int guestId) throws DAOException {
        List<BookingStatus> activeStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );
        List<Booking> activeBookings = bookingDAO.getBookingsByGuestAndStatus(guestId, activeStatuses);
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
package dao;

import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BookingDAO {
    private static final Logger logger = Logger.getLogger(BookingDAO.class.getName());

    public boolean insert(Booking booking) {
        String sql = "INSERT INTO booking (room_id, guest_id, check_in, check_out, total_price, num_guests, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getRoomId());
            stmt.setInt(2, booking.getGuestId());

            // Convert java.util.Date to java.sql.Date
            stmt.setDate(3, new java.sql.Date(booking.getCheckIn().getTime()));
            stmt.setDate(4, new java.sql.Date(booking.getCheckOut().getTime()));

            stmt.setDouble(5, booking.getTotalPrice());
            stmt.setInt(6, booking.getNumGuests());
            stmt.setString(7, booking.getStatus());

            int rows = stmt.executeUpdate();
            logger.info("Inserted booking: bookingId=" + booking.getId() + ", affectedRows=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting booking for guestId=" + booking.getGuestId(), e);
            return false;
        }
    }

    public Booking findById(int id) {
        String sql = "SELECT * FROM booking WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = new Booking(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("guest_id"),
                            rs.getDate("check_in"),
                            rs.getDate("check_out"),
                            rs.getDouble("total_price"),
                            rs.getInt("num_guests"),
                            rs.getString("status")
                    );
                    logger.fine("Found booking by ID: " + id);
                    return booking;
                } else{
                    logger.fine("No booking found with ID: " + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding booking by ID=" + id, e);
        }
        return null;
    }

    public List<Booking> getAll() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking";
        logger.fine("Fetching all bookings");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getInt("guest_id"),
                        rs.getDate("check_in"),
                        rs.getDate("check_out"),
                        rs.getDouble("total_price"),
                        rs.getInt("num_guests"),
                        rs.getString("status")
                );
                bookings.add(booking);
            }
            logger.info("Fetched all bookings, count=" + bookings.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all bookings", e);
        }

        return bookings;
    }

    public List<Booking> getOverlappingBookings(int roomId, Date checkIn, Date checkOut, Integer bookingIdToExclude) {
        List<Booking> overlappingBookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE room_id=? AND (check_in < ? AND check_out > ?)";

        if (bookingIdToExclude != null) {
            sql += " AND id != ?";
        }
        logger.fine("Fetching overlapping bookings: roomId=" + roomId + ", checkIn=" + checkIn + ", checkOut=" + checkOut + ", ignoreId=" + bookingIdToExclude);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            stmt.setDate(2, new java.sql.Date(checkOut.getTime()));
            stmt.setDate(3, new java.sql.Date(checkIn.getTime()));

            if (bookingIdToExclude != null) {
                stmt.setInt(4, bookingIdToExclude);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("guest_id"),
                            rs.getDate("check_in"),
                            rs.getDate("check_out"),
                            rs.getDouble("total_price"),
                            rs.getInt("num_guests"),
                            rs.getString("status")
                    );
                    overlappingBookings.add(booking);
                }
            }
            logger.info("Fetched overlapping bookings, count=" + overlappingBookings.size());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching overlapping bookings for roomId=" + roomId, e);
        }

        return overlappingBookings;
    }

    public List<Booking> getBookingsByGuestAndStatus(int guestId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            logger.fine("No statuses provided for guestId=" + guestId + ", returning empty list");
            return Collections.emptyList();
        }

        List<Booking> bookings = new ArrayList<>();
        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM booking WHERE guest_id=? AND status IN (" + placeholders + ")";
        logger.fine("Fetching bookings by guestId=" + guestId + ", statuses=" + statuses);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);
            for (int i = 0; i < statuses.size(); i++) {
                stmt.setString(i + 2, statuses.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getInt("guest_id"),
                        rs.getDate("check_in"),
                        rs.getDate("check_out"),
                        rs.getDouble("total_price"),
                        rs.getInt("num_guests"),
                        rs.getString("status")
                );
                bookings.add(booking);
            }
            logger.info("Fetched bookings for guestId=" + guestId + ", count=" + bookings.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching bookings for guestId=" + guestId + " with statuses=" + statuses, e);
        }

        return bookings;
    }

    public boolean update(Booking booking) {
        String sql = "UPDATE booking SET room_id=?, guest_id=?, check_in=?, check_out=?, total_price=?, num_guests=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getRoomId());
            stmt.setInt(2, booking.getGuestId());
            stmt.setDate(3, new java.sql.Date(booking.getCheckIn().getTime()));
            stmt.setDate(4, new java.sql.Date(booking.getCheckOut().getTime()));
            stmt.setDouble(5, booking.getTotalPrice());
            stmt.setInt(6, booking.getNumGuests());
            stmt.setString(7, booking.getStatus());
            stmt.setInt(8, booking.getId());

            int rows = stmt.executeUpdate();
            logger.info("Updated booking: bookingId=" + booking.getId() + ", affectedRows=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating booking: bookingId=" + booking.getId(), e);
            return false;
        }
    }

    public boolean updateStatus(int bookingId, String newStatus) {
        String sql = "UPDATE booking SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, bookingId);

            int rows = stmt.executeUpdate();
            logger.info("Updated booking status: bookingId=" + bookingId + ", newStatus=" + newStatus + ", affectedRows=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating booking status: bookingId=" + bookingId, e);
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM booking WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            logger.info("Deleting booking: bookingId=" + id + ", affectedRows=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting booking: bookingId=" + id, e);
            return false;
        }
    }
}

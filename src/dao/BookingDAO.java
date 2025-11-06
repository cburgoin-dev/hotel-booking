package dao;

import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

public class BookingDAO {

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
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting booking: " + e.getMessage());
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
                    return new Booking(
                            rs.getInt("id"),
                            rs.getInt("room_id"),
                            rs.getInt("guest_id"),
                            rs.getDate("check_in"),
                            rs.getDate("check_out"),
                            rs.getDouble("total_price"),
                            rs.getInt("num_guests"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding booking by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Booking> getAll() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking";

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
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }

        return bookings;
    }

    public List<Booking> getOverlappingBookings(int roomId, Date checkIn, Date checkOut, Integer bookingIdToExclude) {
        List<Booking> overlappingBookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE room_id=? AND (check_in < ? AND check_out > ?)";

        if (bookingIdToExclude != null) {
            sql += " AND id != ?";
        }

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
        } catch (SQLException e) {
            System.err.println("Error fetching overlapping bookings: " + e.getMessage());
        }

        return overlappingBookings;
    }

    public List<Booking> getBookingsByGuestAndStatus(int guestId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM booking WHERE guest_id=? AND status IN (" + placeholders + ")";

        List<Booking> bookings = new ArrayList<>();

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
        } catch (SQLException e) {
            e.printStackTrace();
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

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int bookingId, String newStatus) {
        String sql = "UPDATE booking SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, bookingId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM booking WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return false;
        }
    }
}

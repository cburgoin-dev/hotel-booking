package dao;

import model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public boolean insert(Room room) {
        String sql = "INSERT INTO room (number, type, price_per_night, extra_guest_price_per_night, capacity, allowed_extra_guests, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getNumber());
            stmt.setString(2, room.getType());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setDouble(4, room.getExtraGuestPricePerNight());
            stmt.setInt(5, room.getCapacity());
            stmt.setInt(6, room.getAllowedExtraGuests());
            stmt.setString(7, room.getStatus());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting room: " + e.getMessage());
            return false;
        }
    }

    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("id"),
                        rs.getString("number"),
                        rs.getString("type"),
                        rs.getDouble("price_per_night"),
                        rs.getDouble("extra_guest_price_per_night"),
                        rs.getInt("capacity"),
                        rs.getInt("allowed_extra_guests"),
                        rs.getString("status")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms: " + e.getMessage());
        }

        return rooms;
    }

    public double getRoomPricePerNight(int roomId) {
        String sql = "SELECT price_per_night FROM room WHERE id = ?";
        double price = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    price = rs.getDouble("price_per_night");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return price;
    }

    public double getRoomExtraGuestPricePerNight(int roomId) {
        String sql = "SELECT extra_guest_price_per_night FROM room WHERE id = ?";
        double price = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    price = rs.getDouble("extra_guest_price_per_night");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return price;
    }

    public int getRoomCapacity(int roomId) {
        String sql = "SELECT capacity FROM room WHERE id = ?";
        int capacity = 2;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    capacity = rs.getInt("capacity");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return capacity;
    }

    public int getRoomAllowedExtraGuests(int roomId) {
        String sql = "SELECT allowed_extra_guests FROM room WHERE id = ?";
        int allowedExtraGuests = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    allowedExtraGuests = rs.getInt("allowed_extra_guests");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allowedExtraGuests;
    }

    public boolean update(Room room) {
        String sql = "UPDATE room SET number=?, type=?, price_per_night=?, extra_guest_price_per_night=?, capacity=?, allowed_extra_guests=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getNumber());
            stmt.setString(2, room.getType());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setDouble(4, room.getExtraGuestPricePerNight());
            stmt.setInt(5, room.getCapacity());
            stmt.setInt(6, room.getAllowedExtraGuests());
            stmt.setString(7, room.getStatus());
            stmt.setInt(8, room.getId());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating room: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int roomId, String newStatus) {
        String sql = "UPDATE room SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, roomId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating room status: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM room WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
        }
    }
}

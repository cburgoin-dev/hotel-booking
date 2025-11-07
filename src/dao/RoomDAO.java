package dao;

import exception.DAOException;
import exception.NotFoundException;
import model.Booking;
import model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomDAO {
    private static final Logger logger = Logger.getLogger(RoomDAO.class.getName());

    public void insert(Room room) throws DAOException {
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

            int rows = stmt.executeUpdate();
            logger.info("Inserted room: roomId=" + room.getId() + ", affectedRows=" + rows);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting room: roomNumber=" + room.getNumber(), e);
            throw new DAOException("Failed to insert room for guestId=" + room.getNumber(), e);
        }
    }

    public Room findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM room WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
                    logger.fine("Found room by ID: " + id);
                    return room;
                } else{
                    logger.fine("No room found with ID: " + id);
                    throw new NotFoundException("Failed to find room by ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding room by ID=" + id, e);
            throw new DAOException("Failed to find room by ID=" + id, e);
        }
    }

    public List<Room> getAll() throws DAOException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";
        logger.fine("Fetching all rooms");

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
            logger.info("Fetched all rooms, count=" + rooms.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all rooms", e);
            throw new DAOException("Failed to fetch all rooms", e);
        }

        return rooms;
    }

    public double getRoomPricePerNight(int id) throws DAOException, NotFoundException {
        String sql = "SELECT price_per_night FROM room WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("price_per_night");
                    logger.info("Retrieved price_per_night for roomId=" + id + ": " + price);
                    return price;
                } else {
                    logger.warning("No room found with ID=" + id);
                    throw new NotFoundException("Room with ID=" + id + "not found");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving price_per_night for roomId" + id, e);
            throw new DAOException("Error retrieving room price for ID=" + id, e);
        }
    }

    public double getRoomExtraGuestPricePerNight(int id) throws DAOException, NotFoundException {
        String sql = "SELECT extra_guest_price_per_night FROM room WHERE id = ?";
        double price = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

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

    public int getRoomCapacity(int id) throws DAOException, NotFoundException {
        String sql = "SELECT capacity FROM room WHERE id = ?";
        int capacity = 2;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

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

    public int getRoomAllowedExtraGuests(int id) throws DAOException, NotFoundException {
        String sql = "SELECT allowed_extra_guests FROM room WHERE id = ?";
        int allowedExtraGuests = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

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

    public boolean update(Room room) throws DAOException, NotFoundException {
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

    public boolean updateStatus(int roomId, String newStatus) throws DAOException, NotFoundException {
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

    public boolean delete(int id) throws DAOException, NotFoundException {
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

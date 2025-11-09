package dao;

import exception.DAOException;
import exception.NotFoundException;
import model.Booking;
import model.BookingStatus;
import model.Room;
import model.RoomStatus;

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
            stmt.setString(7, room.getStatus().name());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Failed to insert room: no rows affected");
            }
            logger.info("Inserted room: roomId=" + room.getId() + ", affectedRows=" + rows);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting room: roomNumber=" + room.getNumber(), e);
            throw new DAOException("Failed to insert room with number=" + room.getNumber(), e);
        }
    }

    public Room findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM room WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Room room = mapResultSetToRoom(rs);
                    logger.fine("Found room by ID: " + id);
                    return room;
                } else {
                    logger.fine("Room not found with ID=" + id);
                    throw new NotFoundException("Room not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding room by ID=" + id, e);
            throw new DAOException("Error finding room by ID=" + id, e);
        }
    }

    public Room findByNumber(String number) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM room WHERE number=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                } else {
                    throw new NotFoundException("Room not found with number=" + number);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding room by number=" + number, e);
            throw new DAOException("Error finding room by number=" + number, e);
        }
    }

    public List<Room> getAll() throws DAOException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            logger.info("Fetched all rooms, count=" + rooms.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all rooms", e);
            throw new DAOException("Error fetching all rooms", e);
        }

        return rooms;
    }

    private <T> T getRoomField(int id, String fieldName, Class<T> type) throws DAOException, NotFoundException {
        String sql = "SELECT " + fieldName + " FROM room WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object value;

                    if (type == Double.class) {
                        value = rs.getDouble(fieldName);
                    } else if (type == Integer.class) {
                        value = rs.getInt(fieldName);
                    } else if (type == String.class) {
                        value = rs.getString(fieldName);
                    } else {
                        value = rs.getObject(fieldName);
                    }

                    logger.info("Retrieved field '" + fieldName + "' for roomId=" + id);
                    return type.cast(value);
                } else {
                    logger.warning("No room found with ID=" + id);
                    throw new NotFoundException("Room not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving field '" + fieldName + "' for roomId=" + id, e);
            throw new DAOException("Error retrieving field '" + fieldName + "' for room ID=" + id, e);
        }
    }

    public double getRoomPricePerNight(int id) throws DAOException, NotFoundException {
        return getRoomField(id, "price_per_night", Double.class);
    }

    public double getRoomExtraGuestPricePerNight(int id) throws DAOException, NotFoundException {
        return getRoomField(id, "extra_guest_price_per_night", Double.class);
    }

    public int getRoomCapacity(int id) throws DAOException, NotFoundException {
        return getRoomField(id, "capacity", Integer.class);
    }

    public int getRoomAllowedExtraGuests(int id) throws DAOException, NotFoundException {
        return getRoomField(id, "allowed_extra_guests", Integer.class);
    }

    public void update(Room room) throws DAOException, NotFoundException {
        String sql = "UPDATE room SET number=?, type=?, price_per_night=?, extra_guest_price_per_night=?, capacity=?, allowed_extra_guests=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getNumber());
            stmt.setString(2, room.getType());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setDouble(4, room.getExtraGuestPricePerNight());
            stmt.setInt(5, room.getCapacity());
            stmt.setInt(6, room.getAllowedExtraGuests());
            stmt.setString(7, room.getStatus().name());
            stmt.setInt(8, room.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                logger.warning("No room found to update with ID=" + room.getId());
                throw new NotFoundException("No room found to update with ID=" + room.getId());
            }
            logger.info("Updated room successfully: ID=" + room.getId());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating room ID=" + room.getId(), e);
            throw new DAOException("Error updating room ID=" + room.getId(), e);
        }
    }

    public void updateStatus(int roomId, RoomStatus newStatus) throws DAOException, NotFoundException {
        String sql = "UPDATE room SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, roomId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                logger.warning("No room found to update status with ID=" + roomId);
                throw new NotFoundException("Room not found with ID=" + roomId);
            }
            logger.info("Updated room status successfully: roomId=" + roomId + ", newStatus=" + newStatus);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating room status for ID=" + roomId, e);
            throw new DAOException("Error updating room status for ID=" + roomId, e);
        }
    }

    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM room WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                logger.warning("No room found to delete with ID=" + id);
                throw new NotFoundException("Room not found with ID=" + id);
            }
            logger.info("Deleted room successfully: ID=" + id);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting room ID=" + id, e);
            throw new DAOException("Error deleting room ID=" + id, e);
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("number"),
                rs.getString("type"),
                rs.getDouble("price_per_night"),
                rs.getDouble("extra_guest_price_per_night"),
                rs.getInt("capacity"),
                rs.getInt("allowed_extra_guests"),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }
}

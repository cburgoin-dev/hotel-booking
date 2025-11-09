package dao;

import exception.DAOException;
import exception.NotFoundException;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestDAO {
    private static final Logger logger = Logger.getLogger(dao.GuestDAO.class.getName());

    public void insert(Guest guest) throws DAOException {
        String sql = "INSERT INTO guest (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getFirstName());
            stmt.setString(2, guest.getLastName());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getPhone());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Failed to insert guest: no rows affected");
            }
            logger.info("Inserted guest: guestId=" + guest.getId() + ", affectedRows=" + rows);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting guest: guestName=" + guest.getFirstName() + " " + guest.getLastName(), e);
            throw new DAOException("Failed to insert guest with name=" + guest.getFirstName() + " " + guest.getLastName(), e);
        }
    }

    public Guest findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM guest WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Guest guest = mapResultSetToGuest(rs);
                    logger.fine("Found guest by ID: " + id);
                    return guest;
                } else {
                    logger.fine("Guest not found with ID=" + id);
                    throw new NotFoundException("Guest not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding guest by ID=" + id, e);
            throw new DAOException("Error finding guest by ID=" + id, e);
        }
    }

    public Guest findByName(String fullName) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM guest WHERE CONCAT(first_name, ' ', last_name) =?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Guest guest = mapResultSetToGuest(rs);
                    logger.fine("Found guest by NAME: " + fullName);
                    return guest;
                } else {
                    logger.fine("Guest not found with NAME=" + fullName);
                    throw new NotFoundException("Guest not found with NAME=" + fullName);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding guest by NAME=" + fullName, e);
            throw new DAOException("Error finding guest by NAME=" + fullName, e);
        }
    }

    public Guest findByEmail(String email) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM guest WHERE email=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Guest guest = mapResultSetToGuest(rs);
                    logger.fine("Found guest by EMAIL: " + email);
                    return guest;
                } else{
                    logger.fine("Guest not found with EMAIL=" + email);
                    throw new NotFoundException("Guest not found with EMAIL=" + email);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding guest by EMAIL=" + email, e);
            throw new DAOException("Error finding guest by EMAIL=" + email, e);
        }
    }

    public List<Guest> getAll() throws DAOException {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guest";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
            logger.info("Fetched all guests, count=" + guests.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all guests", e);
            throw new DAOException("Error fetching all guests", e);
        }

        return guests;
    }

    private <T> T getGuestField(int id, String fieldName, Class<T> type) throws DAOException, NotFoundException {
        String sql = "SELECT " + fieldName + " FROM guest WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object value;

                    if (type == Integer.class) {
                        value = rs.getInt(fieldName);
                    } else if (type == String.class) {
                        value = rs.getString(fieldName);
                    } else {
                        value = rs.getObject(fieldName);
                    }

                    logger.info("Retrieved field '" + fieldName + "' for guestId=" + id);
                    return type.cast(value);
                } else {
                    logger.warning("No guest found with ID=" + id);
                    throw new NotFoundException("Guest not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving field '" + fieldName + "' for guestId=" + id, e);
            throw new DAOException("Error retrieving field '" + fieldName + "' for guest ID=" + id, e);
        }
    }

    public String getGuestFirstName(int id) throws DAOException, NotFoundException {
        return getGuestField(id, "first_name", String.class);
    }

    public String getGuestLastName(int id) throws DAOException, NotFoundException {
        return getGuestField(id, "last_name", String.class);
    }

    public String getGuestEmail(int id) throws DAOException, NotFoundException {
        return getGuestField(id, "email", String.class);
    }

    public String getGuestPhone(int id) throws DAOException, NotFoundException {
        return getGuestField(id, "phone", String.class);
    }

    public boolean existsByEmail(String email, Integer ignoreId) throws DAOException {
        String sql = "SELECT id FROM guest WHERE email=?";
        if (ignoreId != null) {
            sql += " AND id<>?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            if (ignoreId != null) {
                stmt.setInt(2, ignoreId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error checking email existence: " + email, e);
            throw new DAOException("Error checking email existence for " + email, e);
        }
    }

    public void update(Guest guest) throws DAOException, NotFoundException {
        String sql = "UPDATE guest SET first_name=?, last_name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getFirstName());
            stmt.setString(2, guest.getLastName());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getPhone());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                logger.warning("No guest found to update with ID=" + guest.getId());
                throw new NotFoundException("No guest found to update with ID=" + guest.getId());
            }
            logger.info("Updated guest successfully: ID=" + guest.getId());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating guest ID=" + guest.getId(), e);
            throw new DAOException("Error updating guest ID=" + guest.getId(), e);
        }
    }

    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM guest WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                logger.warning("No guest found to delete with ID=" + id);
                throw new NotFoundException("Guest not found with ID=" + id);
            }
            logger.info("Deleted guest successfully: ID=" + id);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting guest ID=" + id, e);
            throw new DAOException("Error deleting guest ID=" + id, e);
        }
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        return new Guest(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
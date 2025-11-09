package dao;

import exception.DAOException;
import exception.NotFoundException;
import model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(dao.UserDAO.class.getName());

    public void insert(User user) throws DAOException {
        String sql = "INSERT INTO user (guest_id, email, password_hash, role, is_active, created_at, updated_at ) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getGuestId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setBoolean(5, user.isActive());
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            stmt.setTimestamp(6, now);
            stmt.setTimestamp(7, now);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DAOException("Failed to insert user: no rows affected");
            }
            logger.info("Inserted user: userId=" + user.getId() + ", affectedRows=" + rows);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting user: userEmail=" + user.getEmail(), e);
            throw new DAOException("Failed to insert user with email=" + user.getEmail(), e);
        }
    }

    public User findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM user WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    logger.fine("Found user by ID: " + id);
                    return user;
                } else {
                    logger.fine("User not found with ID=" + id);
                    throw new NotFoundException("User not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding user by ID=" + id, e);
            throw new DAOException("Error finding user by ID=" + id, e);
        }
    }

    public User findByEmail(String email) throws DAOException, NotFoundException {
        String sql = "SELECT * FROM user WHERE email=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    logger.fine("Found user by EMAIL: " + email);
                    return user;
                } else{
                    logger.fine("User not found with EMAIL=" + email);
                    throw new NotFoundException("User not found with EMAIL=" + email);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding user by EMAIL=" + email, e);
            throw new DAOException("Error finding user by EMAIL=" + email, e);
        }
    }

    public List<User> getAll() throws DAOException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            logger.info("Fetched all users, count=" + users.size());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all users", e);
            throw new DAOException("Error fetching all users", e);
        }

        return users;
    }

    private <T> T getUserField(int id, String fieldName, Class<T> type) throws DAOException, NotFoundException {
        String sql = "SELECT " + fieldName + " FROM user WHERE id=?";
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
                    } else if (type == Boolean.class) {
                        value = rs.getBoolean(fieldName);
                    } else if (type == LocalDateTime.class) {
                        Timestamp ts = rs.getTimestamp(fieldName);
                        value = ts != null ? ts.toLocalDateTime() : null;
                    } else {
                        value = rs.getObject(fieldName);
                    }

                    logger.info("Retrieved field '" + fieldName + "' for userId=" + id);
                    return type.cast(value);
                } else {
                    logger.warning("No user found with ID=" + id);
                    throw new NotFoundException("User not found with ID=" + id);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving field '" + fieldName + "' for userId=" + id, e);
            throw new DAOException("Error retrieving field '" + fieldName + "' for user ID=" + id, e);
        }
    }

    public boolean existsByEmail(String email, Integer ignoreId) throws DAOException {
        String sql = "SELECT id FROM user WHERE email=?";
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

    public void update(User user) throws DAOException, NotFoundException {
        String sql = "UPDATE user SET guest_id=?, email=?, passwordHash=?, role=?, is_active=?, created_at=?, updated_at=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getGuestId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setBoolean(5, user.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                logger.warning("No user found to update with ID=" + user.getId());
                throw new NotFoundException("No user found to update with ID=" + user.getId());
            }
            logger.info("Updated user successfully: ID=" + user.getId());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user ID=" + user.getId(), e);
            throw new DAOException("Error updating user ID=" + user.getId(), e);
        }
    }

    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM user WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                logger.warning("No user found to delete with ID=" + id);
                throw new NotFoundException("User not found with ID=" + id);
            }
            logger.info("Deleted user successfully: ID=" + id);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user ID=" + id, e);
            throw new DAOException("Error deleting user ID=" + id, e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new User(
                rs.getInt("id"),
                rs.getInt("guest_id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
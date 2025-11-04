package dao;

import model.Guest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    public boolean insert(Guest guest) {
        String sql = "INSERT INTO guest (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guest.getFirstName());
            stmt.setString(2, guest.getLastName());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getPhone());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting guest: " + e.getMessage());
            return false;
        }
    }

    public List<Guest> getAll() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guest";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guest guest = new Guest();
                guest.setId(rs.getInt("id"));
                guest.setFirstName(rs.getString("first_name"));
                guest.setLastName(rs.getString("last_name"));
                guest.setEmail(rs.getString("email"));
                guest.setPhone(rs.getString("phone"));
                guests.add(guest);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching guests: " + e.getMessage());
        }

        return guests;
    }

    public boolean update(Guest guest) {
        String sql = "UPDATE guest SET first_name=?, last_name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getFirstName());
            stmt.setString(2, guest.getLastName());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getPhone());
            stmt.setInt(5, guest.getId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating guest: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM guest WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting guest: " + e.getMessage());
            return false;
        }
    }
}

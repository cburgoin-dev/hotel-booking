package model;

import java.time.LocalDateTime;

public class User {
    private Integer id;
    private Integer guestId;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(Integer guestId, String email, String passwordHash, Role role) {
        this.guestId = guestId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(Integer id, Integer guestId, String email, String passwordHash, Role role, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.guestId = guestId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGuestId() {
        return guestId;
    }
    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User with ID " + this.id
                + ((guestId != null) ? "\nGuest ID: " + this.guestId : "")
                + "\nEmail: " + this.email
                + "\nRole: " + this.role
                + "\nActive: " + this.isActive
                + "\nCreated: " + this.createdAt
                + "\nUpdated: " + this.updatedAt
                + "\n";
    }

    public boolean checkPassword(String plainPassword) {
        return false;
    }
}

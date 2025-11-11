package model;

import util.SecurityUtil;

import java.time.LocalDateTime;

public class User {
    private Integer id;
    private Integer guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(Integer guestId, String firstName, String lastName, String email, String passwordHash, String phone, Role role, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(Integer id, Integer guestId, String firstName, String lastName, String email, String passwordHash, String phone, Role role, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
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

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
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
                + "\nFirst Name: " + this.firstName
                + "\nLast Name: " + this.lastName
                + "\nEmail: " + this.email
                + "\nPhone: " + this.phone
                + "\nRole: " + this.role
                + "\nActive: " + this.isActive
                + "\nCreated: " + this.createdAt
                + "\nUpdated: " + this.updatedAt
                + "\n";
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean checkPassword(String plainPassword) {
        return SecurityUtil.verifyPassword(plainPassword, this.passwordHash);
    }
}

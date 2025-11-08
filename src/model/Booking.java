package model;

import java.util.Date;

public class Booking {
    private int id;
    private int roomId;
    private int guestId;
    private Date checkIn;
    private Date checkOut;
    private double totalPrice;
    private int numGuests;
    private BookingStatus status;

    public Booking() {}

    public Booking(int roomId, int guestId, Date checkIn, Date checkOut, double totalPrice, int numGuests, BookingStatus status) {
        this.roomId = roomId;
        this.guestId = guestId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.numGuests = numGuests;
        this.status = status;
    }

    public Booking(int id, int roomId, int guestId, Date checkIn, Date checkOut, double totalPrice, int numGuests, BookingStatus status) {
        this.id = id;
        this.roomId = roomId;
        this.guestId = guestId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.numGuests = numGuests;
        this.status = status;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getGuestId() {
        return guestId;
    }
    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public Date getCheckIn() {
        return checkIn;
    }
    public void setCheckIn(Date checkIn) {
        this.checkIn = checkIn;
    }

    public Date getCheckOut() {
        return checkOut;
    }
    public void setCheckOut(Date checkOut) {
        this.checkOut = checkOut;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getNumGuests() {
        return numGuests;
    }
    public void setNumGuests(int numGuests) {
        this.numGuests = numGuests;
    }

    public BookingStatus getStatus() {
        return status;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking with ID " + this.id
                + "\nRoom ID: " + this.roomId
                + "\nGuest ID: " + this.guestId
                + "\nCheck In: " + this.checkIn
                + "\nCheck Out: " + this.checkOut
                + "\nTotal Price: " + this.totalPrice
                + "\nNumber of Guests: " + this.numGuests
                + "\nStatus: " + this.status
                + "\n";
    }
}

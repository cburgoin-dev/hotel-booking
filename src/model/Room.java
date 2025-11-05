package model;

public class Room {
    private int id;
    private String number;
    private String type;
    private double pricePerNight;
    private double extraGuestPricePerNight;
    private int capacity;
    private int allowedExtraGuests;
    private String status;

    public Room() {}

    public Room(String number, String type, double pricePerNight, double extraGuestPricePerNight, int capacity, int allowedExtraGuests, String status) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.extraGuestPricePerNight = extraGuestPricePerNight;
        this.capacity = capacity;
        this.allowedExtraGuests = allowedExtraGuests;
        this.status = status;
    }

    public Room(int id, String number, String type, double pricePerNight, double extraGuestPricePerNight, int capacity, int allowedExtraGuests, String status) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.extraGuestPricePerNight = extraGuestPricePerNight;
        this.capacity = capacity;
        this.allowedExtraGuests = allowedExtraGuests;
        this.status = status;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }
    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getExtraGuestPricePerNight() {
        return extraGuestPricePerNight;
    }
    public void setExtraGuestPricePerNight(double extraGuestPricePerNight) {
        this.extraGuestPricePerNight = extraGuestPricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAllowedExtraGuests() {
        return allowedExtraGuests;
    }
    public void setAllowedExtraGuests(int allowedExtraGuests) {
        this.allowedExtraGuests = allowedExtraGuests;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Room with ID " + this.id
                + "\nNumber: " + this.number
                + "\nType: " + this.type
                + "\nPrice per Night: " + this.pricePerNight
                + "\nExtra Guest Price per Night: " + this.extraGuestPricePerNight
                + "\nCapacity: " + this.capacity
                + "\nAllowed Extra Guests: " + this.allowedExtraGuests
                + "\nStatus: " + this.status
                + "\n";
    }
}
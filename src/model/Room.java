package model;

public class Room {
    private int id;
    private String number;
    private String type;
    private double pricePerNight;
    private String status;

    public Room() {}

    public Room(String number, String type, double pricePerNight, String status) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public Room(int id, String number, String type, double pricePerNight, String status) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
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
                + "\nPrice: " + this.pricePerNight
                + "\nStatus: " + this.status;
    }
}
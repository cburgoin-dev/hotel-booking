package model;

import exception.InvalidBookingStatusException;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED;

    public static boolean isValid(String value) {
        for (BookingStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static BookingStatus fromString(String value) throws InvalidBookingStatusException {
        for (BookingStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new InvalidBookingStatusException(value);
    }
}

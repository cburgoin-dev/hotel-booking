package model;

import exception.InvalidStatusException;

public enum RoomStatus {
    AVAILABLE,
    UNAVAILABLE,
    OCCUPIED,
    MAINTENANCE;

    public static boolean isValid(String value) {
        for (RoomStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static RoomStatus fromString(String value) throws InvalidStatusException {
        for (RoomStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new InvalidStatusException("Room", value);
    }
}

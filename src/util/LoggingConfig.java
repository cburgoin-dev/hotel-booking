package util;

import java.io.IOException;
import java.util.logging.*;

public class LoggingConfig {
    public static void setup() {
        Logger logger = Logger.getLogger("");
        try {
            FileHandler fileHandler = new FileHandler("booking.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

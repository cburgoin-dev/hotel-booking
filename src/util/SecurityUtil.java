package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import exception.*;
import io.github.cdimascio.dotenv.Dotenv;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtil {
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());
    private static final int BCRYPT_COST = 12;

    private SecurityUtil() { }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static Integer getUserId(String token) {
        try {
            if (!JwtUtil.validateToken(token)) {
                Logger.getLogger(SecurityUtil.class.getName()).warning("Invalid or expired JWT token");
                return null;
            }

            return JwtUtil.extractUserId(token);

        } catch (Exception e) {
            Logger.getLogger(SecurityUtil.class.getName()).warning("Failed to extract user ID from token: " + e.getMessage());
            return null;
        }
    }

    public static String getUserRole(String token) {
        try {
            if (!JwtUtil.validateToken(token)) {
                logger.warning("Invalid or expired JWT token");
                return null;
            }

            return JwtUtil.extractRole(token);

        } catch (Exception e) {
            logger.warning("Failed to extract user role from token: " + e.getMessage());
            return null;
        }
    }
}

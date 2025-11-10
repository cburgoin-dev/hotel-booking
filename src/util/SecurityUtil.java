package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import exception.*;
import io.github.cdimascio.dotenv.Dotenv;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtil {
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());
    private static final Dotenv dotenv = Dotenv.load();
    private static final String VALID_TOKEN = dotenv.get("API_KEY");
    private static final int BCRYPT_COST = 12;

    private SecurityUtil() { }

    public static boolean isAuthorized(HttpExchange exchange) throws IOException {
        List<String> headers = exchange.getRequestHeaders().get("X-API-KEY");

        if (headers == null || headers.isEmpty()) {
            logger.warning("Missing API key header");
            sendUnauthorizedResponse(exchange, "Missing API key");
            return false;
        }

        String token = headers.get(0);
        if (!VALID_TOKEN.equals(token)) {
            logger.warning("Invalid API key" + token);
            sendUnauthorizedResponse(exchange, "Invalid API key");
            return false;
        }

        return true;
    }

    public static boolean isAdmin(HttpExchange exchange) throws IOException {
        String role = exchange.getRequestHeaders().getFirst("Role");
        return "ADMIN".equalsIgnoreCase(role);
    }

    private static void sendUnauthorizedResponse(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(401, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

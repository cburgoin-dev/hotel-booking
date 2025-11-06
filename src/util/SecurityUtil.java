package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import io.github.cdimascio.dotenv.Dotenv;

public class SecurityUtil {
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());
    private static final Dotenv dotenv = Dotenv.load();
    private static final String VALID_TOKEN = dotenv.get("API_SECRET_KEY");

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

    private static void sendUnauthorizedResponse(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(401, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

package util;

import java.security.SecureRandom;
import java.util.Base64;

public class APIKeyGenerator {
    public static void main(String[] args) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        System.out.println(apiKey);
    }
}

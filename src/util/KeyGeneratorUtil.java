package util;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGeneratorUtil {

    private static final SecureRandom random = new SecureRandom();

    public static String generateBase64Key(int byteLength) {
        byte[] key = new byte[byteLength];
        random.nextBytes(key);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    }
    public static void main(String[] args) {
        System.out.println("API Key: " + generateBase64Key(32));
        System.out.println("JWT Key: " + generateBase64Key(32));
    }
}

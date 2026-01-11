package tech.sjiale.hoyo_achievement_server.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%^&*()_+-=[]{}";
    private static final String ALL_CHARS = LETTERS + DIGITS + SPECIALS;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generatePassword(int length) {
        if (length < 8 || length > 50) {
            throw new IllegalArgumentException("Password length must be between 8 and 50");
        }

        List<Character> passwordChars = new ArrayList<>(length);

        // Add at least two characters from each pool
        passwordChars.add(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        passwordChars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));

        // Get remaining characters from the all pools
        for (int i = 2; i < length; i++) {
            passwordChars.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the characters
        Collections.shuffle(passwordChars, RANDOM);

        // Convert to string
        StringBuilder sb = new StringBuilder();
        for (Character c : passwordChars) {
            sb.append(c);
        }
        return sb.toString();
    }
}

package az.matrix.api.data;

import java.util.concurrent.ThreadLocalRandom;

public final class NameFactory {

    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private NameFactory() {
    }

    public static String letters(int length) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            builder.append(LETTERS.charAt(ThreadLocalRandom.current().nextInt(LETTERS.length())));
        }
        return builder.toString();
    }

    public static String digits(int length) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }

    public static String unique(String prefix) {
        return prefix + letters(4) + digits(4);
    }

    public static String repeat(char symbol, int length) {
        return String.valueOf(symbol).repeat(length);
    }
}

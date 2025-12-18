package faang.school.urlshortenerservice.util;

public class Base62Encoder {
    private static final String BASE_62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String encode(long number) {
        if (number == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % 62);
            sb.append(BASE_62_CHARACTERS.charAt(remainder));
            number /= 62;
        }
        return sb.reverse().toString();
    }

    public static String encodePadded(long number, int length) {
        String encoded = encode(number);
        return String.format("%" + length + "s", encoded).replace(' ', '0');
    }
}
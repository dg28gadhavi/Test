package com.sec.internal.helper;

import java.util.Random;

public class StringGenerator {
    private static final int CHAR_ARRAY_SIZE = 62;
    private static final char[] charArray = new char[62];

    static {
        for (int i = 0; i < 10; i++) {
            charArray[i] = (char) (i + 48);
        }
        for (int i2 = 0; i2 < 26; i2++) {
            char[] cArr = charArray;
            cArr[i2 + 10] = (char) (i2 + 97);
            cArr[i2 + 36] = (char) (i2 + 65);
        }
    }

    private static char getChar(int i) {
        return charArray[i];
    }

    public static String generateString(int i, int i2) throws IllegalArgumentException {
        if (i <= 0 || i > i2) {
            throw new IllegalArgumentException();
        }
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        if (i2 > i) {
            i2 = random.nextInt((i2 - i) + 1) + i;
        }
        for (int i3 = 0; i3 < i2; i3++) {
            sb.append(getChar(random.nextInt(62)));
        }
        return sb.toString();
    }
}

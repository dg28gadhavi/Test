package com.sec.internal.helper.picturetool;

public class ReadScaleCalculator {
    public static int calculate(long j, long j2) throws IllegalArgumentException {
        return Math.max((int) Math.sqrt(((double) j) / ((double) j2)), 1);
    }

    public static int calculate(long j, int i, int i2, long j2, int i3, int i4) {
        return Math.max(calculate(j, j2), Math.max(Math.max(i / i3, 1), Math.max(i2 / i4, 1)));
    }
}

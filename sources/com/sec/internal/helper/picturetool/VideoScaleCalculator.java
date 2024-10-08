package com.sec.internal.helper.picturetool;

import android.util.Pair;

public class VideoScaleCalculator {
    public static Pair<Integer, Integer> calculate(int i, int i2, int i3, int i4) throws IllegalArgumentException {
        double d = (double) i;
        double d2 = (double) i2;
        double max = Math.max(Math.max(d / ((double) i3), d2 / ((double) i4)), 1.0d);
        return Pair.create(Integer.valueOf(Math.max((int) (d / max), 1)), Integer.valueOf(Math.max((int) (d2 / max), 1)));
    }
}

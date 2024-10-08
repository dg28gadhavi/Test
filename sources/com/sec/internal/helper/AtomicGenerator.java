package com.sec.internal.helper;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicGenerator {
    private static final AtomicLong mAtomicLong = new AtomicLong(0);

    public static long generateUniqueLong() {
        AtomicLong atomicLong = mAtomicLong;
        long incrementAndGet = atomicLong.incrementAndGet();
        if (incrementAndGet >= 0) {
            return incrementAndGet;
        }
        atomicLong.set(0);
        return atomicLong.incrementAndGet();
    }
}

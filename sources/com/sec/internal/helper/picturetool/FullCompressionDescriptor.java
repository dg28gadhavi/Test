package com.sec.internal.helper.picturetool;

import android.util.Log;
import android.util.Pair;

public class FullCompressionDescriptor implements ICompressionDescriptor {
    private static final int DEDICATED_IMAGE_QUALITY = 90;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "FullCompressionDescriptor";
    /* access modifiers changed from: private */
    public ICompressionDescriptor mDelegate;
    /* access modifiers changed from: private */
    public ICompressionDescriptor mFinal;
    private final ICompressionDescriptor mInitial;
    /* access modifiers changed from: private */
    public final long mMaxSize;
    /* access modifiers changed from: private */
    public final int mMinDimension;
    /* access modifiers changed from: private */
    public int mScale;
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mSecond = new ICompressionDescriptor() {
        public Pair<Integer, Integer> next(long j) {
            FullCompressionDescriptor fullCompressionDescriptor = FullCompressionDescriptor.this;
            fullCompressionDescriptor.mDelegate = fullCompressionDescriptor.mStandard;
            FullCompressionDescriptor fullCompressionDescriptor2 = FullCompressionDescriptor.this;
            fullCompressionDescriptor2.mScale = Math.max(fullCompressionDescriptor2.mScale + 1, (int) Math.sqrt((((double) j) * Math.pow((double) FullCompressionDescriptor.this.mScale, 2.0d)) / ((double) FullCompressionDescriptor.this.mMaxSize)));
            return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
        }
    };
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mStandard = new ICompressionDescriptor() {
        public Pair<Integer, Integer> next(long j) {
            int r0 = FullCompressionDescriptor.this.mScale;
            int r1 = FullCompressionDescriptor.this.mMinDimension / FullCompressionDescriptor.this.mScale;
            while (true) {
                r0++;
                int r2 = FullCompressionDescriptor.this.mMinDimension / r0;
                if (r2 != r1) {
                    if (r2 == 0) {
                        FullCompressionDescriptor.this.mFinal.next(j);
                    } else {
                        FullCompressionDescriptor.this.mScale = r0;
                        Log.d(FullCompressionDescriptor.LOG_TAG, "mStandard: Exit");
                        return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
                    }
                }
            }
        }
    };

    public FullCompressionDescriptor(long j, int i, int i2, long j2, int i3, int i4, ICompressionDescriptor iCompressionDescriptor) throws NullPointerException {
        AnonymousClass1 r0 = new ICompressionDescriptor() {
            public Pair<Integer, Integer> next(long j) {
                String r2 = FullCompressionDescriptor.LOG_TAG;
                Log.d(r2, "mInitial mScale=" + FullCompressionDescriptor.this.mScale);
                FullCompressionDescriptor fullCompressionDescriptor = FullCompressionDescriptor.this;
                fullCompressionDescriptor.mDelegate = fullCompressionDescriptor.mSecond;
                return Pair.create(90, Integer.valueOf(FullCompressionDescriptor.this.mScale));
            }
        };
        this.mInitial = r0;
        this.mDelegate = r0;
        this.mScale = 1;
        this.mFinal = iCompressionDescriptor;
        this.mMaxSize = j2;
        int min = Math.min(i, i3);
        int min2 = Math.min(i2, i4);
        int min3 = Math.min(i, i2);
        this.mMinDimension = min3;
        int max = Math.max(Math.max(i / min, i2 / min2), Math.max((int) Math.sqrt(((double) j) / ((double) j2)), 1));
        this.mScale = max;
        if (min3 / max == 0) {
            this.mDelegate = this.mFinal;
        }
    }

    public Pair<Integer, Integer> next(long j) {
        String str = LOG_TAG;
        Log.d(str, "FullCompressionDescriptor::next size=" + j);
        return this.mDelegate.next(j);
    }
}

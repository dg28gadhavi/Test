package com.sec.internal.helper.picturetool;

import android.util.Log;
import android.util.Pair;

public class PngLazyCompressionDescriptor implements ICompressionDescriptor {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "PngLazyCompressionDescriptor";
    private static final int STUB_IMAGE_QUALITY = 100;
    /* access modifiers changed from: private */
    public ICompressionDescriptor mDelegate;
    private final ICompressionDescriptor mInitial;
    /* access modifiers changed from: private */
    public final ICompressionDescriptor mPanic;
    /* access modifiers changed from: private */
    public final int mScale;

    public PngLazyCompressionDescriptor(int i, int i2, int i3, int i4, ICompressionDescriptor iCompressionDescriptor) {
        AnonymousClass1 r0 = new ICompressionDescriptor() {
            public Pair<Integer, Integer> next(long j) {
                String r0 = PngLazyCompressionDescriptor.LOG_TAG;
                Log.d(r0, "mInitial::nex" + j);
                PngLazyCompressionDescriptor pngLazyCompressionDescriptor = PngLazyCompressionDescriptor.this;
                pngLazyCompressionDescriptor.mDelegate = pngLazyCompressionDescriptor.mPanic;
                return Pair.create(100, Integer.valueOf(PngLazyCompressionDescriptor.this.mScale));
            }
        };
        this.mInitial = r0;
        this.mDelegate = r0;
        this.mScale = Math.max(getStartWidthScale(i, i3), getStartHeightScale(i2, i4));
        this.mPanic = iCompressionDescriptor;
    }

    public Pair<Integer, Integer> next(long j) {
        return this.mDelegate.next(j);
    }

    private int getStartScale(int i, int i2) {
        int i3 = 1;
        int max = Math.max(i / i2, 1);
        if (i2 >= i) {
            return max;
        }
        if (i % (i2 * max) == 0) {
            i3 = 0;
        }
        return max + i3;
    }

    private int getStartWidthScale(int i, int i2) {
        return getStartScale(i, i2);
    }

    private int getStartHeightScale(int i, int i2) {
        return getStartScale(i, i2);
    }
}

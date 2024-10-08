package com.sec.internal.helper;

import android.annotation.SuppressLint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PhoneIdKeyMap<E> {
    private final E mDefaultValue;
    @SuppressLint({"UseSparseArrays"})
    private final Map<Integer, E> mMap = new HashMap();
    private final int mSize;

    public PhoneIdKeyMap(int i, E e) {
        this.mSize = i;
        this.mDefaultValue = e;
    }

    public void put(int i, E e) {
        if (i >= 0 && i < this.mSize) {
            this.mMap.put(Integer.valueOf(i), e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x000c, code lost:
        r1 = r1.mDefaultValue;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public E get(int r2) {
        /*
            r1 = this;
            java.util.Map<java.lang.Integer, E> r0 = r1.mMap
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            java.lang.Object r2 = r0.get(r2)
            if (r2 != 0) goto L_0x0011
            E r1 = r1.mDefaultValue
            if (r1 == 0) goto L_0x0011
            r2 = r1
        L_0x0011:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.PhoneIdKeyMap.get(int):java.lang.Object");
    }

    public E remove(int i) {
        return this.mMap.remove(Integer.valueOf(i));
    }

    public int getKey(E e, int i) {
        for (Map.Entry next : this.mMap.entrySet()) {
            if (Objects.equals(next.getValue(), e)) {
                return ((Integer) next.getKey()).intValue();
            }
        }
        return i;
    }

    public void clear() {
        this.mMap.clear();
    }

    public Collection<E> values() {
        return this.mMap.values();
    }
}

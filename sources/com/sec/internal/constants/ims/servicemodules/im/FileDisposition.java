package com.sec.internal.constants.ims.servicemodules.im;

import android.util.SparseArray;

public enum FileDisposition {
    ATTACH(0),
    RENDER(1);
    
    private static final SparseArray<FileDisposition> mValueToEnum = null;
    int mCode;

    static {
        int i;
        mValueToEnum = new SparseArray<>();
        for (FileDisposition fileDisposition : values()) {
            mValueToEnum.put(fileDisposition.toInt(), fileDisposition);
        }
    }

    private FileDisposition(int i) {
        this.mCode = i;
    }

    public final int toInt() {
        return this.mCode;
    }

    public static FileDisposition valueOf(int i) {
        FileDisposition fileDisposition = mValueToEnum.get(i);
        if (fileDisposition != null) {
            return fileDisposition;
        }
        throw new IllegalArgumentException("No enum const class " + FileDisposition.class.getName() + "." + i + "!");
    }
}

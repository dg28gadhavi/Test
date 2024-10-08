package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.internal.helper.Preconditions;

public class IshFileTransfer extends IshFile {
    private long mTransmittedBytes;

    public IshFileTransfer(String str, int i, String str2) {
        Preconditions.checkNotNull(str, "path can't be NULL");
        Preconditions.checkState(i >= 0);
        Preconditions.checkNotNull(str2, "mimeType can't be NULL");
        this.mTransmittedBytes = 0;
        this.mPath = str;
        this.mSize = (long) i;
        this.mMimeType = str2;
    }

    public String getPath() {
        return this.mPath;
    }

    public long getSize() {
        return this.mSize;
    }

    public String toString() {
        return "IshFileTransfer [mTransmittedBytes=" + this.mTransmittedBytes + ", mPath=" + this.mPath + ", mSize=" + this.mSize + ", mMimeType=" + this.mMimeType + "]";
    }
}

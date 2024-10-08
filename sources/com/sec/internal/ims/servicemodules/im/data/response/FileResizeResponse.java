package com.sec.internal.ims.servicemodules.im.data.response;

import com.sec.internal.log.IMSLog;

public class FileResizeResponse {
    public final boolean isResizeSuccessful;
    public final String resizedFilePath;

    public FileResizeResponse(boolean z, String str) {
        this.isResizeSuccessful = z;
        this.resizedFilePath = str;
    }

    public String toString() {
        return "FileResizeResponse [isResizeSuccessful=" + this.isResizeSuccessful + ", resizedFilePath=" + IMSLog.checker(this.resizedFilePath) + "]";
    }
}

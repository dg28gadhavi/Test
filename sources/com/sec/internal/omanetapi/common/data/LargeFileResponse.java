package com.sec.internal.omanetapi.common.data;

public class LargeFileResponse {
    public String accessURL;
    public int partSizeMax;
    public int partSizeMin;
    public String partTag;
    public String uploadKeyId;

    public String toString() {
        return "LargeFileResponse{uploadKeyId='" + this.uploadKeyId + '\'' + ", partSizeMin=" + this.partSizeMin + ", partSizeMax=" + this.partSizeMax + ", partTag='" + this.partTag + '\'' + ", accessURL='" + this.accessURL + '\'' + '}';
    }
}

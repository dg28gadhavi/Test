package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;
import java.net.URL;

public final class FtHttpFileInfo {
    private final FileInfo mFileInfo = new FileInfo();
    private final FileInfo mThumbnailInfo = new FileInfo();

    public static class FileInfo {
        /* access modifiers changed from: private */
        public String mBrandedUrl;
        /* access modifiers changed from: private */
        public String mContentType;
        /* access modifiers changed from: private */
        public Data mData;
        /* access modifiers changed from: private */
        public FileDisposition mFileDisposition;
        /* access modifiers changed from: private */
        public String mFileName;
        /* access modifiers changed from: private */
        public long mFileSize;
        /* access modifiers changed from: private */
        public int mPlayingLength;

        public String toString() {
            return "FileInfo [mFileSize=" + this.mFileSize + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mContentType=" + this.mContentType + ", mData=" + this.mData + ", mBrandedUrl=" + IMSLog.checker(this.mBrandedUrl) + ", mFileDisposition=" + this.mFileDisposition + ", mPlayingLength=" + this.mPlayingLength + "]";
        }
    }

    public static class Data {
        /* access modifiers changed from: private */
        public final String mUntil;
        /* access modifiers changed from: private */
        public final URL mUrl;

        public Data(URL url, String str) {
            this.mUrl = url;
            this.mUntil = str;
        }

        public String toString() {
            return "Data [mUrl=" + this.mUrl + ", mUntil=" + this.mUntil + "]";
        }
    }

    public long getFileSize() {
        return this.mFileInfo.mFileSize;
    }

    public String getFileName() {
        return this.mFileInfo.mFileName;
    }

    public String getContentType() {
        return this.mFileInfo.mContentType;
    }

    public URL getDataUrl() {
        return this.mFileInfo.mData.mUrl;
    }

    public String getDataUntil() {
        return this.mFileInfo.mData.mUntil;
    }

    public String getBrandedUrl() {
        return this.mFileInfo.mBrandedUrl;
    }

    public FileDisposition getFileDisposition() {
        return this.mFileInfo.mFileDisposition;
    }

    public int getPlayingLength() {
        return this.mFileInfo.mPlayingLength;
    }

    public long getThumbnailFileSize() {
        return this.mThumbnailInfo.mFileSize;
    }

    public String getThumbnailContentType() {
        return this.mThumbnailInfo.mContentType;
    }

    public URL getThumbnailDataUrl() {
        return this.mThumbnailInfo.mData.mUrl;
    }

    public String getThumbnailDataUntil() {
        return this.mThumbnailInfo.mData.mUntil;
    }

    public void setFileSize(long j) {
        this.mFileInfo.mFileSize = j;
    }

    public void setFileName(String str) {
        this.mFileInfo.mFileName = str;
    }

    public void setContentType(String str) {
        this.mFileInfo.mContentType = str;
    }

    public void setData(Data data) {
        this.mFileInfo.mData = data;
    }

    public void setBrandedUrl(String str) {
        this.mFileInfo.mBrandedUrl = str;
    }

    public void setFileDisposition(String str) {
        if ("render".equals(str)) {
            this.mFileInfo.mFileDisposition = FileDisposition.RENDER;
        } else {
            this.mFileInfo.mFileDisposition = FileDisposition.ATTACH;
        }
    }

    public void setPlayingLength(int i) {
        this.mFileInfo.mPlayingLength = i;
    }

    public void setThumbnailFileSize(long j) {
        this.mThumbnailInfo.mFileSize = j;
    }

    public void setThumbnailContentType(String str) {
        this.mThumbnailInfo.mContentType = str;
    }

    public void setThumbnailData(Data data) {
        this.mThumbnailInfo.mData = data;
    }

    public boolean isThumbnailExist() {
        return this.mThumbnailInfo.mData != null;
    }

    public String toString() {
        return "FtHttpFileInfo [mFileInfo=" + this.mFileInfo + ", mThumbnailInfo=" + this.mThumbnailInfo + "]";
    }
}

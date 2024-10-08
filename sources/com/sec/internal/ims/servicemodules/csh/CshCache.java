package com.sec.internal.ims.servicemodules.csh;

import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IshFileTransfer;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.util.StorageEnvironment;

public class CshCache {
    private static String LOG_TAG = "CshCache";
    private static IIshServiceInterface imsServiceForIsh;
    private static IvshServiceInterface imsServiceForVsh;
    private static CshCache sInstance;
    private final SparseArray<IContentShare> mSessions = new SparseArray<>();

    private CshCache() {
    }

    public static CshCache getInstance(IIshServiceInterface iIshServiceInterface) {
        if (imsServiceForIsh == null) {
            imsServiceForIsh = iIshServiceInterface;
        }
        return getInstance();
    }

    public static CshCache getInstance(IvshServiceInterface ivshServiceInterface) {
        if (imsServiceForVsh == null) {
            imsServiceForVsh = ivshServiceInterface;
        }
        return getInstance();
    }

    public static CshCache getInstance() {
        if (sInstance == null) {
            sInstance = new CshCache();
        }
        return sInstance;
    }

    public void init() {
        this.mSessions.clear();
    }

    public IContentShare getSessionAt(int i) {
        return this.mSessions.valueAt(i);
    }

    public IContentShare getSession(int i) {
        return this.mSessions.get(i);
    }

    public IContentShare getSession(long j) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            IContentShare valueAt = this.mSessions.valueAt(i);
            if (valueAt != null && j == valueAt.getContent().shareId) {
                return valueAt;
            }
        }
        return null;
    }

    public void putSession(IContentShare iContentShare) {
        this.mSessions.append(iContentShare.getSessionId(), iContentShare);
        String str = LOG_TAG;
        Log.d(str, "Added share [" + iContentShare.getContent() + "]");
    }

    public void deleteSession(int i) {
        String str = LOG_TAG;
        Log.d(str, "Remove share sessionId " + i);
        this.mSessions.delete(i);
    }

    public int getSize() {
        return this.mSessions.size();
    }

    public ImageShare newOutgoingImageShare(ImageShareModule imageShareModule, ImsUri imsUri, String str) {
        CshInfo cshInfo = new CshInfo();
        cshInfo.shareDirection = 1;
        cshInfo.shareType = 1;
        cshInfo.shareContactUri = imsUri;
        cshInfo.dataPath = str;
        return new ImageShare(imsServiceForIsh, imageShareModule, cshInfo);
    }

    public ImageShare newIncommingImageShare(ImageShareModule imageShareModule, int i, ImsUri imsUri, IshFileTransfer ishFileTransfer) {
        CshInfo cshInfo = new CshInfo();
        cshInfo.shareDirection = 0;
        cshInfo.shareType = 1;
        cshInfo.shareContactUri = imsUri;
        cshInfo.dataPath = StorageEnvironment.generateStorePath(ishFileTransfer.getPath());
        cshInfo.dataSize = ishFileTransfer.getSize();
        cshInfo.mimeType = ishFileTransfer.getMimeType();
        ImageShare imageShare = new ImageShare(imsServiceForIsh, imageShareModule, cshInfo);
        imageShare.setSessionId(i);
        return imageShare;
    }

    public VideoShare newOutgoingVideoShare(VideoShareModule videoShareModule, ImsUri imsUri, String str) {
        CshInfo cshInfo = new CshInfo();
        cshInfo.shareDirection = 1;
        cshInfo.shareType = 2;
        cshInfo.shareContactUri = imsUri;
        cshInfo.dataPath = str;
        return new VideoShare(imsServiceForVsh, videoShareModule, cshInfo);
    }

    public VideoShare newIncommingVideoShare(VideoShareModule videoShareModule, int i, ImsUri imsUri, String str) {
        CshInfo cshInfo = new CshInfo();
        cshInfo.shareDirection = 0;
        cshInfo.shareType = 2;
        cshInfo.shareContactUri = imsUri;
        cshInfo.dataPath = str;
        VideoShare videoShare = new VideoShare(imsServiceForVsh, videoShareModule, cshInfo);
        videoShare.setSessionId(i);
        return videoShare;
    }
}

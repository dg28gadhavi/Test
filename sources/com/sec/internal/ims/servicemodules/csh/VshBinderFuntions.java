package com.sec.internal.ims.servicemodules.csh;

import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import com.sec.internal.ims.csh.IVshRemoteClient;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.VideoDisplay;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;
import com.sec.internal.ims.servicemodules.csh.event.VshViewType;

public class VshBinderFuntions extends IVshRemoteClient.Stub {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = VshBinderFuntions.class.getSimpleName();
    private final VideoShareModule mServiceModule;
    private final SparseArray<Surface> surfaceArray = new SparseArray<>();

    public VshBinderFuntions(ServiceModuleBase serviceModuleBase) {
        this.mServiceModule = (VideoShareModule) serviceModuleBase;
    }

    private int open(long j, Surface surface, int i, int i2, int i3, int i4) {
        VshViewType vshViewType;
        String str = LOG_TAG;
        Log.i(str, "Calling open in initialized state.");
        if (j < 0 || surface == null) {
            release(surface);
            return -1;
        }
        VideoShare session = this.mServiceModule.getSession(j);
        if (session == null) {
            Log.e(str, "Session is not found");
            release(surface);
            return 0;
        }
        this.surfaceArray.put(session.getSessionId(), surface);
        VideoDisplay videoDisplay = new VideoDisplay(surface, i4);
        if (session.getContent().shareDirection == 1) {
            vshViewType = VshViewType.LOCAL;
        } else {
            vshViewType = VshViewType.REMOTE;
        }
        this.mServiceModule.setVshVideoDisplay(new VshVideoDisplayParams(session.getSessionId(), vshViewType, videoDisplay, new ICshSuccessCallback() {
            public void onSuccess() {
            }

            public void onFailure() {
                Log.d(VshBinderFuntions.LOG_TAG, "setVshVideoDisplay onFailure");
            }
        }));
        return 0;
    }

    private int close(long j, Surface surface, boolean z) {
        VshViewType vshViewType;
        String str = LOG_TAG;
        Log.i(str, "Calling close in initialized state.");
        if (j < 0) {
            return -1;
        }
        VideoShare session = this.mServiceModule.getSession(j);
        if (session == null) {
            Log.e(str, "Session is not found");
            return 0;
        }
        int sessionId = session.getSessionId();
        release(this.surfaceArray.get(sessionId));
        this.surfaceArray.delete(sessionId);
        VideoDisplay videoDisplay = new VideoDisplay(surface, 0);
        if (session.getContent().shareDirection == 1) {
            vshViewType = VshViewType.LOCAL;
        } else {
            vshViewType = VshViewType.REMOTE;
        }
        this.mServiceModule.resetVshVideoDisplay(new VshVideoDisplayParams(session.getSessionId(), vshViewType, videoDisplay, new ICshSuccessCallback() {
            public void onSuccess() {
            }

            public void onFailure() {
                Log.d(VshBinderFuntions.LOG_TAG, "resetVshVideoDisplay onFailure");
            }
        }));
        return 0;
    }

    public int openVshSource(long j, Surface surface, int i, int i2, int i3, int i4) throws RemoteException {
        return open(j, surface, i, i2, i3, i4);
    }

    public int closeVshSource(long j, Surface surface, boolean z) throws RemoteException {
        int close = close(j, surface, z);
        release(surface);
        return close;
    }

    public int setOrientationListenerType(int i, int i2) throws RemoteException {
        VshOrientation vshOrientation;
        if (i2 == 1) {
            vshOrientation = VshOrientation.LANDSCAPE;
        } else if (i2 == 2) {
            vshOrientation = VshOrientation.PORTRAIT;
        } else if (i2 == 3) {
            vshOrientation = VshOrientation.FLIPPED_LANDSCAPE;
        } else if (i2 != 4) {
            vshOrientation = VshOrientation.LANDSCAPE;
        } else {
            vshOrientation = VshOrientation.REVERSE_PORTRAIT;
        }
        this.mServiceModule.setVshPhoneOrientation(vshOrientation);
        return 0;
    }

    private void release(Surface surface) {
        if (surface != null) {
            surface.release();
        }
    }
}

package com.sec.internal.ims.servicemodules.volte2;

import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.sec.internal.constants.Mno;

public class NetworkStatsOnPortHandler extends Handler {
    private static final String LOG_TAG = "NetworkStatsOnPortHandler";
    public static final int START = 1;
    public static final int STOP = 2;
    private String mIface = "";
    private int mLocalVideoRtcp = 0;
    private int mLocalVideoRtp = 0;
    Mno mMno;
    private int mPhoneId = 0;
    private int mRemoteVideoRtcp = 0;
    private int mRemoteVideoRtp = 0;
    private boolean mReportingNetworkStatsOnPort;

    public NetworkStatsOnPortHandler(int i, Mno mno, Looper looper) {
        super(looper);
        char c = Mno.MVNO_DELIMITER;
        this.mReportingNetworkStatsOnPort = false;
        this.mMno = mno;
        this.mPhoneId = i;
    }

    private void start() {
        Log.i(LOG_TAG, "NetworkStatsOnPort Start");
        Mno mno = this.mMno;
        if (mno == Mno.ROGERS || mno == Mno.DOCOMO || mno == Mno.CHATR || mno.isChn() || this.mMno.isHkMo() || this.mMno.isKor()) {
            Log.i(LOG_TAG, "skip startNetworkStatsOnPorts. (vendor req)");
        } else if (this.mReportingNetworkStatsOnPort) {
            Log.i(LOG_TAG, "startNetworkStatsOnPorts: already triggered, ignore");
        } else {
            try {
                if (this.mLocalVideoRtp != 0) {
                    if (this.mRemoteVideoRtp != 0) {
                        Log.i(LOG_TAG, "startNetworkStatsOnPorts: LocalVideoRtpPort(" + this.mLocalVideoRtp + ") RemoteVideoRtpPort(" + this.mRemoteVideoRtp + ")");
                        startNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                    }
                }
                if (!(this.mLocalVideoRtcp == 0 || this.mRemoteVideoRtcp == 0)) {
                    Log.i(LOG_TAG, "startNetworkStatsOnPorts: LocalVideoRtcpPort(" + this.mLocalVideoRtcp + ") RemoteVideoRtcpPort(" + this.mRemoteVideoRtcp + ")");
                    startNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
                }
                this.mReportingNetworkStatsOnPort = true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void startNetworkStatsOnPorts(String str, int i, int i2) {
        INetworkManagementService asInterface = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (asInterface != null) {
            try {
                asInterface.startNetworkStatsOnPorts(str, i, i2);
            } catch (RemoteException | IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopNetworkStatsOnPorts(String str, int i, int i2) {
        INetworkManagementService asInterface = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        if (asInterface != null) {
            try {
                asInterface.stopNetworkStatsOnPorts(str, i, i2);
            } catch (RemoteException | IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        Log.i(LOG_TAG, "NetworkStatsOnPort Stop");
        if (!this.mReportingNetworkStatsOnPort) {
            Log.i(LOG_TAG, "stopNetworkStatsOnPorts - startNetworkStatsOnPorts not called, ignore");
            return;
        }
        try {
            if (this.mLocalVideoRtp != 0) {
                if (this.mRemoteVideoRtp != 0) {
                    Log.i(LOG_TAG, "stopNetworkStatsOnPorts: LocalVideoRtpPort(" + this.mLocalVideoRtp + ") RemoteVideoRtpPort(" + this.mRemoteVideoRtp + ")");
                    stopNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                }
            }
            if (!(this.mLocalVideoRtcp == 0 || this.mRemoteVideoRtcp == 0)) {
                Log.i(LOG_TAG, "stopNetworkStatsOnPorts: LocalVideoRtcpPort(" + this.mLocalVideoRtcp + ") RemoteVideoRtcpPort(" + this.mRemoteVideoRtcp + ")");
                stopNetworkStatsOnPorts(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
            }
            this.mReportingNetworkStatsOnPort = false;
            setVideoPort(0, 0, 0, 0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Message message) {
        if (message != null) {
            Log.i(LOG_TAG, "handleMessage " + message.what);
            int i = message.what;
            if (i == 1) {
                start();
            } else if (i != 2) {
                Log.i(LOG_TAG, "Ignore Network Stat Event " + message.what);
            } else {
                stop();
            }
        }
    }

    public synchronized long getNetworkStatsVideoCall() {
        long j;
        long j2;
        long j3;
        INetworkManagementService asInterface = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        j = 0;
        if (asInterface != null) {
            try {
                j2 = asInterface.getNetworkStatsVideoCall(this.mIface, this.mLocalVideoRtp, this.mRemoteVideoRtp);
                try {
                    j = j2;
                    j3 = asInterface.getNetworkStatsVideoCall(this.mIface, this.mLocalVideoRtcp, this.mRemoteVideoRtcp);
                } catch (RemoteException | IllegalStateException | NullPointerException e) {
                    e = e;
                    e.printStackTrace();
                    Log.i(LOG_TAG, "getNetworkStatsVideoCall dataUsageRtp : " + j2 + ", dataUsageRtcp : " + j);
                    return j2 + j;
                }
            } catch (RemoteException | IllegalStateException | NullPointerException e2) {
                e = e2;
                j2 = 0;
                e.printStackTrace();
                Log.i(LOG_TAG, "getNetworkStatsVideoCall dataUsageRtp : " + j2 + ", dataUsageRtcp : " + j);
                return j2 + j;
            }
        } else {
            j3 = 0;
        }
        long j4 = j;
        j = j3;
        j2 = j4;
        Log.i(LOG_TAG, "getNetworkStatsVideoCall dataUsageRtp : " + j2 + ", dataUsageRtcp : " + j);
        return j2 + j;
    }

    public synchronized void setVideoPort(int i, int i2, int i3, int i4) {
        this.mLocalVideoRtp = i;
        this.mRemoteVideoRtp = i2;
        this.mLocalVideoRtcp = i3;
        this.mRemoteVideoRtcp = i4;
    }

    public synchronized void setInterface(String str) {
        if (str != null) {
            this.mIface = str;
        }
    }
}

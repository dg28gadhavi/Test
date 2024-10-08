package com.sec.internal.ims.servicemodules.volte2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;

public class SsacManager extends Handler {
    private static final String LOG_TAG = "SsacManager";
    private static final int UNAVAILABE_FACTOR = 100;
    boolean[] mIsAlwaysBarred;
    /* access modifiers changed from: private */
    public final VolteServiceModuleInternal mModule;
    private final IRegistrationManager mRegiMgr;
    SSACController mVideo;
    SSACController mVoice;
    boolean[] needReRegiAfterCall;

    public SsacManager(Looper looper, VolteServiceModuleInternal volteServiceModuleInternal, IRegistrationManager iRegistrationManager, int i) {
        super(looper);
        this.mModule = volteServiceModuleInternal;
        this.mRegiMgr = iRegistrationManager;
        boolean[] zArr = new boolean[i];
        this.needReRegiAfterCall = zArr;
        this.mIsAlwaysBarred = new boolean[i];
        Arrays.fill(zArr, false);
        Arrays.fill(this.mIsAlwaysBarred, false);
        this.mVoice = new SSACController(looper, 1, this, i);
        this.mVideo = new SSACController(looper, 2, this, i);
    }

    public boolean isCallBarred(int i, int i2) {
        if (ImsCallUtil.isE911Call(i2)) {
            return false;
        }
        if (ImsCallUtil.isVideoCall(i2)) {
            return this.mVideo.isCallBarred(i);
        }
        return this.mVoice.isCallBarred(i);
    }

    private void reRegisterBySSAC(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("reRegisterBySSAC : updateRegistrationBySSAC (");
        sb.append(!this.mIsAlwaysBarred[i]);
        sb.append(")");
        IMSLog.i(LOG_TAG, i, sb.toString());
        this.mRegiMgr.updateRegistrationBySSAC(i, !this.mIsAlwaysBarred[i]);
    }

    public void updateSSACInfo(int i, SsacInfo ssacInfo) {
        IMSLog.i(LOG_TAG, i, "updateSSACInfo : Voice(" + ssacInfo.getVoiceFactor() + ":" + ssacInfo.getVoiceTime() + ") Video(" + ssacInfo.getVideoFactor() + ":" + ssacInfo.getVideoTime() + ")");
        if (ssacInfo.getVoiceFactor() != 100 || !ssacInfo.isKnownVoiceBarringType()) {
            this.mVoice.updateSSACInfo(i, ssacInfo.getVoiceFactor(), ssacInfo.getVoiceTime());
            this.mVideo.updateSSACInfo(i, ssacInfo.getVideoFactor(), ssacInfo.getVideoTime());
            ImsRegistration imsRegistration = this.mModule.getImsRegistration(i);
            if (imsRegistration == null && ssacInfo.getVoiceFactor() == 0) {
                IMSLog.i(LOG_TAG, i, "set regiMgr.setSSACPolicy as false.");
                this.mIsAlwaysBarred[i] = true;
                this.mRegiMgr.setSSACPolicy(i, false);
                return;
            }
            Mno simMno = SimUtil.getSimMno(i);
            if (imsRegistration != null) {
                simMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            }
            if (simMno == Mno.VZW) {
                boolean isAlwaysBarred = this.mVoice.isAlwaysBarred(i);
                boolean[] zArr = this.mIsAlwaysBarred;
                if (zArr[i] != isAlwaysBarred) {
                    zArr[i] = isAlwaysBarred;
                    if (this.mRegiMgr.getTelephonyCallStatus(i) == 0) {
                        reRegisterBySSAC(i);
                        return;
                    }
                    IMSLog.i(LOG_TAG, i, "A call is exist now. update Regi after this call terminated.");
                    this.needReRegiAfterCall[i] = true;
                    return;
                }
                return;
            }
            return;
        }
        if (ssacInfo.getVideoFactor() != 100 || !ssacInfo.isKnownVideoBarringType()) {
            this.mVideo.updateSSACInfo(i, ssacInfo.getVideoFactor(), ssacInfo.getVideoTime());
            IMSLog.i(LOG_TAG, i, "Update Video SSAC Info.");
        }
        IMSLog.i(LOG_TAG, i, "Voice factor 100 with isVoiceBarred is unavailable value.");
    }

    public void handleMessage(Message message) {
        if (message.what == 0) {
            int intValue = ((Integer) message.obj).intValue();
            boolean[] zArr = this.needReRegiAfterCall;
            if (zArr[intValue]) {
                zArr[intValue] = false;
                IMSLog.i(LOG_TAG, intValue, "Call Ended. Now update Registration By SSAC.");
                reRegisterBySSAC(intValue);
            }
        }
    }

    public static class SSACController extends Handler {
        private static final int EVT_SSAC_BARRING = 1;
        private static final int MAX_BARRING_FACTOR = 100;
        public static final boolean STATE_BARRED = true;
        public static final boolean STATE_NOT_BARRED = false;
        boolean[] mBarredState;
        int mCallType;
        String mCallTypeName;
        int[] mFactor;
        SsacManager mSSACManager;
        boolean mSsacReset = false;
        int[] mTime;

        public SSACController(Looper looper, int i, SsacManager ssacManager, int i2) {
            super(looper);
            this.mCallType = i;
            if (i == 1) {
                this.mCallTypeName = "Voice Call";
            } else {
                this.mCallTypeName = "Video Call";
            }
            boolean[] zArr = new boolean[i2];
            this.mBarredState = zArr;
            this.mFactor = new int[i2];
            this.mTime = new int[i2];
            Arrays.fill(zArr, false);
            Arrays.fill(this.mFactor, 100);
            Arrays.fill(this.mTime, 0);
            this.mSSACManager = ssacManager;
        }

        public boolean isAlwaysBarred(int i) {
            return this.mFactor[i] == 0;
        }

        public boolean isCallBarred(int i) {
            if (this.mBarredState[i]) {
                return true;
            }
            double random = Math.random();
            double random2 = Math.random();
            IMSLog.i(SsacManager.LOG_TAG, i, this.mCallTypeName + ": isCallBarred:rand1:[" + random + "] rand2:[" + random2 + "]");
            if (random * 100.0d < ((double) this.mFactor[i])) {
                return false;
            }
            int i2 = (int) (((random2 * 0.6d) + 0.7d) * ((double) this.mTime[i]));
            IMSLog.i(SsacManager.LOG_TAG, i, this.mCallTypeName + ": Barred for " + i2 + " ms");
            if (i2 == 0) {
                return false;
            }
            this.mBarredState[i] = true;
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(i)), (long) i2);
            this.mSsacReset = false;
            return true;
        }

        public void updateSSACInfo(int i, int i2, int i3) {
            Mno simMno = SimUtil.getSimMno(i);
            ImsRegistration imsRegistration = this.mSSACManager.mModule.getImsRegistration(i);
            if (imsRegistration != null) {
                simMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            }
            if (i2 != 100) {
                if (i2 >= 0) {
                    this.mFactor[i] = i2;
                    this.mTime[i] = i3 * 1000;
                    this.mSsacReset = false;
                } else if ((simMno == Mno.RAKUTEN_JAPAN || simMno == Mno.KDDI || DeviceUtil.getGcfMode()) && hasMessages(1, Integer.valueOf(i)) && i2 == -1) {
                    IMSLog.i(SsacManager.LOG_TAG, i, this.mCallTypeName + ": Ignored updateSSACInfo : f[" + i2 + "], t[" + i3 + "]");
                    this.mSsacReset = true;
                    return;
                } else {
                    resetSSACInfo(i);
                }
            } else if (simMno == Mno.RAKUTEN_JAPAN || simMno == Mno.KDDI) {
                if (this.mBarredState[i]) {
                    this.mSsacReset = true;
                } else {
                    resetSSACInfo(i);
                }
            } else if (!DeviceUtil.getGcfMode()) {
                resetSSACInfo(i);
            }
            IMSLog.i(SsacManager.LOG_TAG, i, this.mCallTypeName + ": updateSSACInfo : f[" + this.mFactor[i] + "], t[" + this.mTime[i] + "]");
        }

        public void resetSSACInfo(int i) {
            this.mFactor[i] = 100;
            this.mTime[i] = 0;
            this.mBarredState[i] = false;
            removeMessages(1, Integer.valueOf(i));
        }

        public void handleMessage(Message message) {
            int intValue = ((Integer) message.obj).intValue();
            IMSLog.i(SsacManager.LOG_TAG, intValue, "handleMessage: evt " + message.what);
            if (message.what == 1) {
                this.mBarredState[intValue] = false;
                removeMessages(1, Integer.valueOf(intValue));
                if (DeviceUtil.getGcfMode() || this.mSsacReset) {
                    this.mFactor[intValue] = 100;
                    this.mTime[intValue] = 0;
                    this.mSsacReset = false;
                }
                IMSLog.i(SsacManager.LOG_TAG, intValue, this.mCallTypeName + ": Barring Timed out");
            }
        }
    }
}

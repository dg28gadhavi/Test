package com.sec.internal.imsphone;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImsCallSessionEventListener extends IImsCallSessionEventListener.Stub {
    private static final int EVENT_CALL_RETRY = 100;
    public static final String EVENT_NOTIFY_DSDA_VIDEO_CAPA = "NotifyDSDAVideoCapa";
    public static final String EVENT_NOTIFY_QUANTUM_ENCRYPTION_STATUS = "NotifyQuantumEncryptionStatus";
    private static final int EVENT_RETRY_AFTER_TIMEOUT = 101;
    public static final String EVENT_VCID_FAILURE = "VCIDGeneralFailure";
    private static final String LOG_TAG = "ImsCallSessionEventListener";
    private static int USSD_MODE_NW_ERROR = -1;
    private static int mEventCallRetryCounter;
    private static int mEventCallRetryTotalTimer;
    private static final Object mLock = new Object();
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private boolean mForceUpdateCallProfileForDtmfEvent = false;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    ImsCallSessionImpl mIcsi;
    private String mPrevErrorString = "";
    private int mPrevOnEndedError = -1;
    private int mPrevOnErrorVal = -1;
    private int mPrevRetryAfter = -1;
    private boolean mTelephonyReadyToHandleImsCallbacks = false;

    public void onEPdgUnavailable(int i) {
    }

    public void onSessionUpdateRequested(int i, byte[] bArr) {
    }

    public void onStopAlertTone() {
    }

    public void onTtyTextRequest(int i, byte[] bArr) {
    }

    public void onUssdResponse(int i) throws RemoteException {
    }

    public ImsCallSessionEventListener(ImsCallSessionImpl imsCallSessionImpl) {
        this.mIcsi = imsCallSessionImpl;
    }

    public void notifyReadyToHandleImsCallbacks() throws RemoteException {
        synchronized (mLock) {
            StringBuilder sb = new StringBuilder();
            sb.append("notifyReadyToHandleImsCallbacks() ");
            IImsCallSession iImsCallSession = this.mIcsi.mSession;
            sb.append(iImsCallSession != null ? Integer.valueOf(iImsCallSession.getSessionId()) : "null");
            sb.append(" mTelephonyReadyToHandleImsCallbacks = ");
            sb.append(this.mTelephonyReadyToHandleImsCallbacks);
            sb.append(" mPrevOnEndedError = ");
            sb.append(this.mPrevOnEndedError);
            sb.append(" mPrevOnErrorVal = ");
            sb.append(this.mPrevOnErrorVal);
            sb.append(" mPrevErrorString = ");
            sb.append(this.mPrevErrorString);
            sb.append(" mPrevRetryAfter = ");
            sb.append(this.mPrevRetryAfter);
            sb.append(" mForceUpdateCallProfileForDtmfEvent = ");
            sb.append(this.mForceUpdateCallProfileForDtmfEvent);
            Log.i(LOG_TAG, sb.toString());
            if (!this.mTelephonyReadyToHandleImsCallbacks) {
                if (this.mForceUpdateCallProfileForDtmfEvent) {
                    this.mExecutorService.submit(new ImsCallSessionEventListener$$ExternalSyntheticLambda0(this));
                }
                if (this.mPrevOnErrorVal > -1) {
                    this.mExecutorService.submit(new ImsCallSessionEventListener$$ExternalSyntheticLambda1(this));
                }
                if (this.mPrevOnEndedError > -1) {
                    this.mExecutorService.submit(new ImsCallSessionEventListener$$ExternalSyntheticLambda2(this));
                }
                this.mTelephonyReadyToHandleImsCallbacks = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyReadyToHandleImsCallbacks$0() {
        try {
            updateCallProfileForDtmfEvent();
            this.mForceUpdateCallProfileForDtmfEvent = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyReadyToHandleImsCallbacks$1() {
        try {
            Log.i(LOG_TAG, "Telephoy gets ready. Invoke onError()");
            onError(this.mPrevOnErrorVal, this.mPrevErrorString, this.mPrevRetryAfter);
            this.mPrevOnErrorVal = -1;
            this.mPrevErrorString = "";
            this.mPrevRetryAfter = -1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyReadyToHandleImsCallbacks$2() {
        try {
            Log.i(LOG_TAG, "Telephoy gets ready. Invoke onEnded()");
            onEnded(this.mPrevOnEndedError);
            this.mPrevOnEndedError = -1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onCalling() {
        try {
            IImsCallSession iImsCallSession = this.mIcsi.mSession;
            if (iImsCallSession != null && ImsCallUtil.isCmcPrimaryType(iImsCallSession.getCmcType())) {
                this.mIcsi.updateCallProfile();
                ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                if (imsCallSessionImpl.mListener != null) {
                    imsCallSessionImpl.updateRingbackToneDirection(0);
                    ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                    imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.getCallProfile());
                    ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
                    imsCallSessionImpl3.mListener.callSessionProgressing(imsCallSessionImpl3.getCallProfile().getMediaProfile());
                }
            }
        } catch (RemoteException unused) {
        }
    }

    public void onTrying() throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        if (imsCallSessionImpl.mListener != null) {
            IImsCallSession iImsCallSession = imsCallSessionImpl.mSession;
            if (iImsCallSession != null) {
                if (ImsCallUtil.isCmcSecondaryType(iImsCallSession.getCmcType())) {
                    ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                    imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.getCallProfile());
                }
                this.mIcsi.getCallProfile().getMediaProfile().mAudioQuality = 0;
                this.mIcsi.setCallProfile(3);
            }
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            imsCallSessionImpl3.mListener.callSessionInitiating(imsCallSessionImpl3.getCallProfile());
        }
    }

    public void onRingingBack() throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        imsCallSessionImpl.mState = 2;
        imsCallSessionImpl.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            notifyAlertInfo();
            this.mIcsi.updateRingbackToneDirection(0);
            ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
            imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.getCallProfile());
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            imsCallSessionImpl3.mListener.callSessionProgressing(imsCallSessionImpl3.getCallProfile().getMediaProfile());
        }
    }

    public void onSessionProgress(int i) throws RemoteException {
        if (SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId()) == Mno.CMCC) {
            this.mIcsi.updateCallProfile();
            if (this.mIcsi.mListener != null) {
                notifyAlertInfo();
                ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                imsCallSessionImpl.mListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
                return;
            }
            return;
        }
        ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
        imsCallSessionImpl2.mState = 2;
        imsCallSessionImpl2.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
        if (imsCallSessionImpl3.mListener != null) {
            if (i == -1) {
                i = 0;
            }
            imsCallSessionImpl3.updateRingbackToneDirection(i);
            ImsCallSessionImpl imsCallSessionImpl4 = this.mIcsi;
            imsCallSessionImpl4.mListener.callSessionUpdated(imsCallSessionImpl4.getCallProfile());
            ImsCallSessionImpl imsCallSessionImpl5 = this.mIcsi;
            imsCallSessionImpl5.mListener.callSessionProgressing(imsCallSessionImpl5.getCallProfile().getMediaProfile());
        }
    }

    public void onEarlyMediaStarted(int i) throws RemoteException {
        int videoCrbtSupportType = this.mIcsi.mSession.getVideoCrbtSupportType();
        CallProfile callProfile = this.mIcsi.mSession.getCallProfile();
        this.mForceUpdateCallProfileForDtmfEvent = false;
        if (!this.mTelephonyReadyToHandleImsCallbacks && callProfile.getDirection() == 1 && (videoCrbtSupportType & 4) == 4) {
            Log.i(LOG_TAG, "Telephony not ready to handle ims callbacks. Postpone mForceUpdateCallProfileForDtmfEvent");
            this.mForceUpdateCallProfileForDtmfEvent = true;
        }
        if (SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId()) != Mno.DOCOMO || i == 180) {
            this.mIcsi.mState = 2;
        }
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null && callProfile != null && !callProfile.getDelayRinging()) {
            notifyAlertInfo();
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            imsCallSessionImpl.mListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
            if (this.mIcsi.mState == 2 && callProfile.getDirection() != 1) {
                this.mIcsi.updateRingbackToneDirection(3);
                this.mIcsi.getCallProfile().getMediaProfile().mAudioQuality = 2;
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                imsCallSessionImpl2.mListener.callSessionProgressing(imsCallSessionImpl2.getCallProfile().getMediaProfile());
            }
        }
    }

    public void onEstablished(int i) throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        int i2 = imsCallSessionImpl.mState;
        imsCallSessionImpl.mState = 4;
        Mno simMno = SimUtil.getSimMno(imsCallSessionImpl.mSession.getPhoneId());
        if (this.mIcsi.isEmergencyCall() && (simMno == Mno.VZW || simMno == Mno.USCC || (simMno == Mno.SPRINT && !this.mIcsi.isWifiCall()))) {
            this.mIcsi.mIsEcbmSupport = true;
        }
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
        if (imsCallSessionImpl2.mListener != null) {
            CallProfile callProfile = imsCallSessionImpl2.mSession.getCallProfile();
            if (callProfile == null || !callProfile.isMTCall() || i2 != 4) {
                ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
                imsCallSessionImpl3.mListener.callSessionInitiated(imsCallSessionImpl3.getCallProfile());
            } else {
                ImsCallSessionImpl imsCallSessionImpl4 = this.mIcsi;
                imsCallSessionImpl4.mListener.callSessionUpdated(imsCallSessionImpl4.getCallProfile());
            }
            ImsCallSessionImpl imsCallSessionImpl5 = this.mIcsi;
            int ttyModeFromCallType = imsCallSessionImpl5.getTtyModeFromCallType(imsCallSessionImpl5.mSession.getPhoneId(), i);
            if (ttyModeFromCallType != 0) {
                this.mIcsi.mListener.callSessionTtyModeReceived(ttyModeFromCallType);
            }
        }
    }

    public void onFailure(int i) throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        if (imsCallSessionImpl.mSession != null) {
            if (imsCallSessionImpl.mListener != null) {
                if (imsCallSessionImpl.mState < 2) {
                    this.mIcsi.mListener.callSessionInitiatingFailed(new ImsReasonInfo(DataTypeConvertor.convertCallErrorReasonToFw(i), i));
                } else {
                    this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(DataTypeConvertor.convertCallErrorReasonToFw(i), i));
                }
            }
            this.mIcsi.releaseSessionListeners();
        }
    }

    public void onSwitched(int i) throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl.mListener;
        if (iImsCallSessionListener != null) {
            iImsCallSessionListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
            ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
            int ttyModeFromCallType = imsCallSessionImpl2.getTtyModeFromCallType(imsCallSessionImpl2.mSession.getPhoneId(), i);
            if (ttyModeFromCallType != 0) {
                this.mIcsi.mListener.callSessionTtyModeReceived(ttyModeFromCallType);
            }
        }
    }

    public void onHeld(boolean z, boolean z2) throws RemoteException {
        this.mIcsi.updateCallProfile();
        this.mIcsi.updateHoldToneType(z2);
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl.mListener;
        if (iImsCallSessionListener != null) {
            iImsCallSessionListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
            if (z) {
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                imsCallSessionImpl2.mListener.callSessionHeld(imsCallSessionImpl2.getCallProfile());
                return;
            }
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            imsCallSessionImpl3.mListener.callSessionHoldReceived(imsCallSessionImpl3.getCallProfile());
        }
    }

    public void onResumed(boolean z) throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl.mListener;
        if (iImsCallSessionListener != null) {
            iImsCallSessionListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
            if (z) {
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                imsCallSessionImpl2.mListener.callSessionResumed(imsCallSessionImpl2.getCallProfile());
                return;
            }
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            imsCallSessionImpl3.mListener.callSessionResumeReceived(imsCallSessionImpl3.getCallProfile());
        }
    }

    public void onForwarded() throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        if (imsCallSessionImpl.mListener != null) {
            int direction = imsCallSessionImpl.mSession.getCallProfile().getDirection();
            if (direction == 0) {
                this.mIcsi.onSuppServiceReceived(direction, 2);
            }
            ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
            imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.getCallProfile());
        }
    }

    private boolean needSkipUntilTelephonyReady(IImsCallSession iImsCallSession) throws RemoteException {
        if (iImsCallSession == null) {
            return false;
        }
        CallProfile callProfile = iImsCallSession.getCallProfile();
        int cmcType = iImsCallSession.getCmcType();
        StringBuilder sb = new StringBuilder();
        sb.append("needSkipUntilTelephonyReady() mTelephonyReadyToHandleImsCallbacks: ");
        sb.append(this.mTelephonyReadyToHandleImsCallbacks);
        sb.append(" callType: ");
        sb.append(callProfile != null ? Integer.valueOf(callProfile.getCallType()) : "null");
        sb.append(" cmcType: ");
        sb.append(cmcType);
        Log.i(LOG_TAG, sb.toString());
        if (this.mTelephonyReadyToHandleImsCallbacks || callProfile == null || callProfile.getCallType() == 12 || (cmcType != 0 && !ImsCallUtil.isCmcSecondaryType(cmcType))) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:104:0x025c, code lost:
        r9 = r9.mIcsi.mImsVideoCallProvider;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0260, code lost:
        if (r9 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0262, code lost:
        r9.setCallback((com.android.ims.internal.IImsVideoCallCallback) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onEnded(int r10) throws android.os.RemoteException {
        /*
            r9 = this;
            java.lang.Object r0 = mLock
            monitor-enter(r0)
            r1 = 0
            mEventCallRetryCounter = r1     // Catch:{ all -> 0x0266 }
            mEventCallRetryTotalTimer = r1     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r2 = r2.mSession     // Catch:{ all -> 0x0266 }
            if (r2 != 0) goto L_0x0010
            monitor-exit(r0)     // Catch:{ all -> 0x0266 }
            return
        L_0x0010:
            boolean r2 = r9.needSkipUntilTelephonyReady(r2)     // Catch:{ all -> 0x0266 }
            if (r2 == 0) goto L_0x0021
            java.lang.String r1 = "ImsCallSessionEventListener"
            java.lang.String r2 = "Telephony not ready to handle ims callbacks. Postpone onEnded()"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0266 }
            r9.mPrevOnEndedError = r10     // Catch:{ all -> 0x0266 }
            monitor-exit(r0)     // Catch:{ all -> 0x0266 }
            return
        L_0x0021:
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r2 = r2.mSession     // Catch:{ all -> 0x0266 }
            int r2 = r2.getPhoneId()     // Catch:{ all -> 0x0266 }
            com.sec.internal.constants.Mno r2 = com.sec.internal.helper.SimUtil.getSimMno(r2)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r3 = r3.mSession     // Catch:{ all -> 0x0266 }
            int r3 = r3.getVideoCrbtSupportType()     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r5 = r4.mListener     // Catch:{ all -> 0x0266 }
            r6 = 2
            r7 = 1
            if (r5 == 0) goto L_0x0052
            r5 = r3 & 1
            if (r5 == r7) goto L_0x0044
            r3 = r3 & r6
            if (r3 != r6) goto L_0x0052
        L_0x0044:
            r4.updateCallProfile()     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r4 = r3.mListener     // Catch:{ all -> 0x0266 }
            android.telephony.ims.ImsCallProfile r3 = r3.getCallProfile()     // Catch:{ all -> 0x0266 }
            r4.callSessionUpdated(r3)     // Catch:{ all -> 0x0266 }
        L_0x0052:
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r3 = r3.mSession     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.data.CallProfile r3 = r3.getCallProfile()     // Catch:{ all -> 0x0266 }
            java.lang.String r4 = "ImsCallSessionEventListener"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0266 }
            r5.<init>()     // Catch:{ all -> 0x0266 }
            java.lang.String r8 = "onEnded(), cmcType: "
            r5.append(r8)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r8 = r8.mSession     // Catch:{ all -> 0x0266 }
            int r8 = r8.getCmcType()     // Catch:{ all -> 0x0266 }
            r5.append(r8)     // Catch:{ all -> 0x0266 }
            java.lang.String r8 = ", sessionState: "
            r5.append(r8)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            int r8 = r8.mState     // Catch:{ all -> 0x0266 }
            r5.append(r8)     // Catch:{ all -> 0x0266 }
            java.lang.String r8 = ", error: "
            r5.append(r8)     // Catch:{ all -> 0x0266 }
            r5.append(r10)     // Catch:{ all -> 0x0266 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0266 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r5 = r4.mListener     // Catch:{ all -> 0x0266 }
            if (r5 == 0) goto L_0x0209
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r4 = r4.getPrevInternalState()     // Catch:{ all -> 0x0266 }
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.HeldCall     // Catch:{ all -> 0x0266 }
            if (r4 != r5) goto L_0x00a4
            r4 = 210(0xd2, float:2.94E-43)
            if (r10 != r4) goto L_0x00a4
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            r5 = 5
            r4.onSuppServiceReceived(r7, r5)     // Catch:{ all -> 0x0266 }
        L_0x00a4:
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            int r5 = r4.mState     // Catch:{ all -> 0x0266 }
            r8 = 146(0x92, float:2.05E-43)
            if (r5 >= r6) goto L_0x0184
            if (r3 == 0) goto L_0x00c3
            boolean r4 = r3.isPullCall()     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x00c3
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r4 = r4.mSession     // Catch:{ all -> 0x0266 }
            int r4 = r4.getCmcType()     // Catch:{ all -> 0x0266 }
            boolean r4 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r4)     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x00c3
            goto L_0x00c4
        L_0x00c3:
            r7 = r1
        L_0x00c4:
            java.lang.String r4 = "ImsCallSessionEventListener"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0266 }
            r5.<init>()     // Catch:{ all -> 0x0266 }
            java.lang.String r6 = "onEnded(), CallDirection: "
            r5.append(r6)     // Catch:{ all -> 0x0266 }
            if (r3 != 0) goto L_0x00d5
            java.lang.String r6 = "cp is null"
            goto L_0x00dd
        L_0x00d5:
            int r6 = r3.getDirection()     // Catch:{ all -> 0x0266 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0266 }
        L_0x00dd:
            r5.append(r6)     // Catch:{ all -> 0x0266 }
            java.lang.String r6 = ", isSdPulling: "
            r5.append(r6)     // Catch:{ all -> 0x0266 }
            r5.append(r7)     // Catch:{ all -> 0x0266 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0266 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x0266 }
            if (r3 == 0) goto L_0x011b
            boolean r4 = r3.isMTCall()     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x011b
            if (r7 != 0) goto L_0x011b
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            boolean r2 = r2.needDeclineDuringQecCall()     // Catch:{ all -> 0x0266 }
            if (r2 == 0) goto L_0x0109
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            r3 = 507(0x1fb, float:7.1E-43)
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
            goto L_0x0112
        L_0x0109:
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            int r3 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r10)     // Catch:{ all -> 0x0266 }
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
        L_0x0112:
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionTerminated(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x011b:
            if (r3 == 0) goto L_0x015c
            boolean r4 = r3.isMOCall()     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x015c
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r5 = r4.mVolteServiceModule     // Catch:{ all -> 0x0266 }
            com.sec.ims.volte2.IImsCallSession r4 = r4.mSession     // Catch:{ all -> 0x0266 }
            int r4 = r4.getPhoneId()     // Catch:{ all -> 0x0266 }
            int r6 = r3.getCallType()     // Catch:{ all -> 0x0266 }
            com.sec.ims.util.SipError r7 = new com.sec.ims.util.SipError     // Catch:{ all -> 0x0266 }
            r7.<init>(r10)     // Catch:{ all -> 0x0266 }
            boolean r4 = r5.isVolteRetryRequired(r4, r6, r7)     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x015c
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS     // Catch:{ all -> 0x0266 }
            if (r2 != r3) goto L_0x014c
            r2 = 2414(0x96e, float:3.383E-42)
            if (r10 != r2) goto L_0x014c
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            r3 = 3001(0xbb9, float:4.205E-42)
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
            goto L_0x0153
        L_0x014c:
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            r3 = 147(0x93, float:2.06E-43)
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
        L_0x0153:
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionInitiatingFailed(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x015c:
            if (r3 == 0) goto L_0x0172
            boolean r2 = r3.hasCSFBError()     // Catch:{ all -> 0x0266 }
            if (r2 == 0) goto L_0x0172
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            r2.<init>(r8, r10)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionInitiatingFailed(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x0172:
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            int r3 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r10)     // Catch:{ all -> 0x0266 }
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionInitiatingFailed(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x0184:
            com.sec.ims.volte2.IImsCallSession r4 = r4.mSession     // Catch:{ all -> 0x0266 }
            int r4 = r4.getCmcType()     // Catch:{ all -> 0x0266 }
            r5 = 4
            if (r4 <= 0) goto L_0x01a6
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            int r6 = r4.mState     // Catch:{ all -> 0x0266 }
            if (r6 != r5) goto L_0x01a6
            com.sec.ims.volte2.IImsCallSession r2 = r4.mSession     // Catch:{ all -> 0x0266 }
            int r2 = r2.getCmcType()     // Catch:{ all -> 0x0266 }
            android.telephony.ims.ImsReasonInfo r10 = r4.changeCmcErrorReason(r2, r10)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener     // Catch:{ all -> 0x0266 }
            r2.callSessionTerminated(r10)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x01a6:
            if (r3 == 0) goto L_0x01c7
            boolean r4 = r3.hasCSFBError()     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x01c7
            boolean r4 = r2.isKor()     // Catch:{ all -> 0x0266 }
            if (r4 == 0) goto L_0x01c7
            com.sec.internal.imsphone.ImsCallSessionImpl r4 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            int r4 = r4.mState     // Catch:{ all -> 0x0266 }
            if (r4 >= r5) goto L_0x01c7
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            r2.<init>(r8, r10)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionTerminated(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x01c7:
            if (r3 == 0) goto L_0x01f8
            boolean r3 = r3.hasCSFBError()     // Catch:{ all -> 0x0266 }
            if (r3 == 0) goto L_0x01f8
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS     // Catch:{ all -> 0x0266 }
            if (r2 != r3) goto L_0x01f8
            r2 = 503(0x1f7, float:7.05E-43)
            if (r10 != r2) goto L_0x01f8
            java.lang.String r2 = "ro.boot.hardware"
            java.lang.String r3 = ""
            java.lang.String r2 = android.os.SemSystemProperties.get(r2, r3)     // Catch:{ all -> 0x0266 }
            java.lang.String r3 = "qcom"
            boolean r2 = r2.contains(r3)     // Catch:{ all -> 0x0266 }
            if (r2 != 0) goto L_0x01f8
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            java.lang.String r3 = "100 Trying Timeout"
            r2.<init>(r8, r10, r3)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionInitiatingFailed(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x01f8:
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            int r3 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r10)     // Catch:{ all -> 0x0266 }
            r2.<init>(r3, r10)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r10.mListener     // Catch:{ all -> 0x0266 }
            r10.callSessionTerminated(r2)     // Catch:{ all -> 0x0266 }
            goto L_0x0231
        L_0x0209:
            com.sec.internal.imsphone.MmTelFeatureImpl r2 = r4.mMmTelFeatureImpl     // Catch:{ all -> 0x0266 }
            boolean r2 = r2.hasConferenceHost()     // Catch:{ all -> 0x0266 }
            if (r2 == 0) goto L_0x0231
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.MmTelFeatureImpl r2 = r2.mMmTelFeatureImpl     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r2.getConferenceHost()     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r3 = r2.getListener()     // Catch:{ all -> 0x0266 }
            if (r3 == 0) goto L_0x0231
            android.telephony.ims.ImsReasonInfo r3 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x0266 }
            int r4 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r10)     // Catch:{ all -> 0x0266 }
            java.lang.String r5 = ""
            r3.<init>(r4, r10, r5)     // Catch:{ all -> 0x0266 }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r2.getListener()     // Catch:{ all -> 0x0266 }
            r10.callSessionMergeFailed(r3)     // Catch:{ all -> 0x0266 }
        L_0x0231:
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            boolean r10 = r10.isMultiparty()     // Catch:{ all -> 0x0266 }
            r2 = 0
            if (r10 == 0) goto L_0x0241
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.MmTelFeatureImpl r10 = r10.mMmTelFeatureImpl     // Catch:{ all -> 0x0266 }
            r10.setConferenceHost(r2)     // Catch:{ all -> 0x0266 }
        L_0x0241:
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            boolean r3 = r10.mIsEcbmSupport     // Catch:{ all -> 0x0266 }
            if (r3 == 0) goto L_0x0250
            com.sec.internal.imsphone.MmTelFeatureImpl r10 = r10.mMmTelFeatureImpl     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsEcbmImpl r10 = r10.getImsEcbmImpl()     // Catch:{ all -> 0x0266 }
            r10.enterEmergencyCallbackMode()     // Catch:{ all -> 0x0266 }
        L_0x0250:
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r9.mIcsi     // Catch:{ all -> 0x0266 }
            r3 = 8
            r10.mState = r3     // Catch:{ all -> 0x0266 }
            r10.mIsEcbmSupport = r1     // Catch:{ all -> 0x0266 }
            r10.releaseSessionListeners()     // Catch:{ all -> 0x0266 }
            monitor-exit(r0)     // Catch:{ all -> 0x0266 }
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r9.mIcsi
            com.sec.internal.imsphone.ImsVideoCallProviderImpl r9 = r9.mImsVideoCallProvider
            if (r9 == 0) goto L_0x0265
            r9.setCallback(r2)
        L_0x0265:
            return
        L_0x0266:
            r9 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0266 }
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsCallSessionEventListener.onEnded(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x013d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onError(int r9, java.lang.String r10, int r11) throws android.os.RemoteException {
        /*
            r8 = this;
            java.lang.Object r0 = mLock
            monitor-enter(r0)
            com.sec.internal.imsphone.ImsCallSessionImpl r1 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.ims.volte2.IImsCallSession r1 = r1.mSession     // Catch:{ all -> 0x013e }
            if (r1 != 0) goto L_0x000b
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x000b:
            boolean r1 = r8.needSkipUntilTelephonyReady(r1)     // Catch:{ all -> 0x013e }
            if (r1 == 0) goto L_0x0020
            java.lang.String r1 = "ImsCallSessionEventListener"
            java.lang.String r2 = "Telephony not ready to handle ims callbacks. Postpone onError()"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x013e }
            r8.mPrevOnErrorVal = r9     // Catch:{ all -> 0x013e }
            r8.mPrevErrorString = r10     // Catch:{ all -> 0x013e }
            r8.mPrevRetryAfter = r11     // Catch:{ all -> 0x013e }
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x0020:
            com.sec.internal.imsphone.ImsCallSessionImpl r1 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.ims.volte2.IImsCallSession r1 = r1.mSession     // Catch:{ all -> 0x013e }
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.ims.volte2.IImsCallSession r2 = r2.mSession     // Catch:{ all -> 0x013e }
            int r2 = r2.getCmcType()     // Catch:{ all -> 0x013e }
            r3 = 603(0x25b, float:8.45E-43)
            r4 = 2
            if (r2 <= 0) goto L_0x00bc
            java.lang.String r5 = "ImsCallSessionEventListener"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x013e }
            r6.<init>()     // Catch:{ all -> 0x013e }
            java.lang.String r7 = "onError(), error: "
            r6.append(r7)     // Catch:{ all -> 0x013e }
            r6.append(r9)     // Catch:{ all -> 0x013e }
            java.lang.String r7 = ", sessionState: "
            r6.append(r7)     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r8.mIcsi     // Catch:{ all -> 0x013e }
            int r7 = r7.mState     // Catch:{ all -> 0x013e }
            r6.append(r7)     // Catch:{ all -> 0x013e }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x013e }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x013e }
            boolean r2 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r2)     // Catch:{ all -> 0x013e }
            if (r2 == 0) goto L_0x00aa
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            int r5 = r2.mState     // Catch:{ all -> 0x013e }
            if (r5 > r4) goto L_0x00aa
            com.sec.internal.imsphone.cmc.CmcCallSessionManager r2 = r2.getCmcCallSessionManager()     // Catch:{ all -> 0x013e }
            if (r2 == 0) goto L_0x00aa
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.cmc.CmcCallSessionManager r2 = r2.getCmcCallSessionManager()     // Catch:{ all -> 0x013e }
            int r2 = r2.getP2pSessionSize()     // Catch:{ all -> 0x013e }
            if (r2 > 0) goto L_0x0081
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.cmc.CmcCallSessionManager r2 = r2.getCmcCallSessionManager()     // Catch:{ all -> 0x013e }
            boolean r2 = r2.isExistP2pConnection()     // Catch:{ all -> 0x013e }
            if (r2 == 0) goto L_0x00aa
        L_0x0081:
            if (r9 != r3) goto L_0x0088
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            r2.mState = r4     // Catch:{ all -> 0x013e }
            goto L_0x00aa
        L_0x0088:
            java.lang.String r9 = "ImsCallSessionEventListener"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x013e }
            r10.<init>()     // Catch:{ all -> 0x013e }
            java.lang.String r11 = "onError(), ignore error of cmcCall. just return: "
            r10.append(r11)     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.cmc.CmcCallSessionManager r8 = r8.getCmcCallSessionManager()     // Catch:{ all -> 0x013e }
            int r8 = r8.getP2pSessionSize()     // Catch:{ all -> 0x013e }
            r10.append(r8)     // Catch:{ all -> 0x013e }
            java.lang.String r8 = r10.toString()     // Catch:{ all -> 0x013e }
            android.util.Log.i(r9, r8)     // Catch:{ all -> 0x013e }
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x00aa:
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            r2.updateCallProfile()     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            android.telephony.ims.aidl.IImsCallSessionListener r5 = r2.mListener     // Catch:{ all -> 0x013e }
            if (r5 == 0) goto L_0x00bc
            android.telephony.ims.ImsCallProfile r2 = r2.getCallProfile()     // Catch:{ all -> 0x013e }
            r5.callSessionUpdated(r2)     // Catch:{ all -> 0x013e }
        L_0x00bc:
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            android.telephony.ims.aidl.IImsCallSessionListener r5 = r2.mListener     // Catch:{ all -> 0x013e }
            if (r5 == 0) goto L_0x00ee
            if (r1 == 0) goto L_0x00d8
            if (r9 != r3) goto L_0x00d8
            java.lang.String r2 = "Outgoing Call Barred"
            boolean r2 = r2.equals(r10)     // Catch:{ all -> 0x013e }
            if (r2 == 0) goto L_0x00d8
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            int r3 = r1.getDirection()     // Catch:{ all -> 0x013e }
            r5 = 5
            r2.onSuppServiceReceived(r3, r5)     // Catch:{ all -> 0x013e }
        L_0x00d8:
            com.sec.internal.imsphone.ImsCallSessionImpl r2 = r8.mIcsi     // Catch:{ all -> 0x013e }
            int r2 = r2.mState     // Catch:{ all -> 0x013e }
            if (r2 >= r4) goto L_0x00e6
            boolean r9 = r8.onErrorBeforeNego(r9, r10, r11)     // Catch:{ all -> 0x013e }
            if (r9 == 0) goto L_0x012c
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x00e6:
            boolean r9 = r8.onErrorWhileNegoOrLater(r9, r10)     // Catch:{ all -> 0x013e }
            if (r9 == 0) goto L_0x012c
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x00ee:
            com.sec.internal.imsphone.MmTelFeatureImpl r11 = r2.mMmTelFeatureImpl     // Catch:{ all -> 0x013e }
            boolean r11 = r11.hasConferenceHost()     // Catch:{ all -> 0x013e }
            if (r11 == 0) goto L_0x012c
            com.sec.internal.imsphone.ImsCallSessionImpl r11 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.MmTelFeatureImpl r11 = r11.mMmTelFeatureImpl     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r11 = r11.getConferenceHost()     // Catch:{ all -> 0x013e }
            r2 = 0
            r11.mIsConferenceHost = r2     // Catch:{ all -> 0x013e }
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r11.getListener()     // Catch:{ all -> 0x013e }
            if (r2 == 0) goto L_0x012c
            android.telephony.ims.ImsReasonInfo r2 = new android.telephony.ims.ImsReasonInfo     // Catch:{ all -> 0x013e }
            int r3 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r9)     // Catch:{ all -> 0x013e }
            r2.<init>(r3, r9, r10)     // Catch:{ all -> 0x013e }
            android.telephony.ims.aidl.IImsCallSessionListener r10 = r11.getListener()     // Catch:{ all -> 0x013e }
            r10.callSessionMergeFailed(r2)     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.ImsCallSessionImpl r10 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.MmTelFeatureImpl r10 = r10.mMmTelFeatureImpl     // Catch:{ all -> 0x013e }
            boolean r10 = r10.isInitialMerge()     // Catch:{ all -> 0x013e }
            if (r10 == 0) goto L_0x012c
            r10 = 1105(0x451, float:1.548E-42)
            if (r9 != r10) goto L_0x012c
            android.telephony.ims.aidl.IImsCallSessionListener r9 = r11.getListener()     // Catch:{ all -> 0x013e }
            r9.callSessionTerminated(r2)     // Catch:{ all -> 0x013e }
        L_0x012c:
            if (r1 == 0) goto L_0x013c
            boolean r9 = r1.isConferenceCall()     // Catch:{ all -> 0x013e }
            if (r9 == 0) goto L_0x013c
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r8.mIcsi     // Catch:{ all -> 0x013e }
            com.sec.internal.imsphone.MmTelFeatureImpl r8 = r8.mMmTelFeatureImpl     // Catch:{ all -> 0x013e }
            r9 = 0
            r8.setConferenceHost(r9)     // Catch:{ all -> 0x013e }
        L_0x013c:
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            return
        L_0x013e:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x013e }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsCallSessionEventListener.onError(int, java.lang.String, int):void");
    }

    private boolean onErrorBeforeNego(int i, String str, int i2) throws RemoteException {
        ImsReasonInfo imsReasonInfo;
        ImsReasonInfo imsReasonInfo2;
        Mno simMno = SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId());
        CallProfile callProfile = this.mIcsi.mSession.getCallProfile();
        if (simMno == Mno.TMOUS && i == 503 && !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
            Log.i(LOG_TAG, "TMO E911 SERVICE_UNAVAILABLE will be handled onEnded()");
            return false;
        }
        if (callProfile == null || !callProfile.isMTCall()) {
            if (callProfile != null && callProfile.isMOCall()) {
                ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                if (imsCallSessionImpl.mVolteServiceModule.isVolteRetryRequired(imsCallSessionImpl.mSession.getPhoneId(), callProfile.getCallType(), new SipError(i, str), i2)) {
                    if (simMno == Mno.TMOUS && i == 2414) {
                        imsReasonInfo = new ImsReasonInfo(ImSessionEvent.SEND_MESSAGE, i);
                    } else {
                        imsReasonInfo = new ImsReasonInfo(147, i);
                    }
                    if (simMno == Mno.KDDI || simMno == Mno.GCF) {
                        mEventCallRetryTotalTimer += i2;
                        HandlerThread handlerThread = new HandlerThread("ImsCallSessionImpl");
                        this.mHandlerThread = handlerThread;
                        handlerThread.start();
                        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
                            public void handleMessage(Message message) {
                                if (message.what == 100) {
                                    try {
                                        ImsCallSessionEventListener.this.mIcsi.mListener.callSessionInitiatingFailed((ImsReasonInfo) message.obj);
                                    } catch (RemoteException unused) {
                                    }
                                }
                            }
                        };
                        mEventCallRetryCounter++;
                        Log.i(LOG_TAG, "mEventCallRetryCounter = " + mEventCallRetryCounter + " mEventCallRetryTotalTimer = " + mEventCallRetryTotalTimer + " retryAfter = " + i2);
                        if (i2 <= 0 || mEventCallRetryCounter >= 5 || mEventCallRetryTotalTimer >= 45) {
                            mEventCallRetryCounter = 0;
                            mEventCallRetryTotalTimer = 0;
                            this.mHandlerThread.quit();
                            imsReasonInfo = new ImsReasonInfo(DataTypeConvertor.convertCallErrorReasonToFw(i), i, str);
                        } else {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, imsReasonInfo), ((long) i2) * 1000);
                            return true;
                        }
                    }
                    this.mIcsi.mListener.callSessionInitiatingFailed(imsReasonInfo);
                }
            }
            if (ImsCallUtil.isCmcSecondaryType(this.mIcsi.mSession.getCmcType())) {
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                this.mIcsi.mListener.callSessionInitiatingFailed(imsCallSessionImpl2.changeCmcErrorReason(imsCallSessionImpl2.mSession.getCmcType(), i, str));
            } else if (callProfile != null && callProfile.hasCSFBError() && i != 381 && i != 382) {
                this.mIcsi.mListener.callSessionInitiatingFailed(new ImsReasonInfo(146, i, str));
            } else if ("LTE Retry in UAC Barred".equals(str)) {
                if (simMno == Mno.VZW) {
                    this.mIcsi.mSession.removeCallStateMachineMessage(301);
                }
                SecImsNotifier.getInstance().onTriggerEpsFallback(this.mIcsi.mSession.getPhoneId(), 2);
                return false;
            } else {
                ImsReasonInfo imsReasonInfo3 = new ImsReasonInfo(DataTypeConvertor.convertCallErrorReasonToFw(i), i, str);
                if (i == 381 || i == 382) {
                    imsReasonInfo3.mExtraCode = i;
                    int convertUrnToEccCat = DataTypeConvertor.convertUrnToEccCat(str);
                    imsReasonInfo3.mExtraMessage = String.valueOf(convertUrnToEccCat);
                    if (convertUrnToEccCat == 254) {
                        this.mIcsi.mMmTelFeatureImpl.setServiceUrn(str);
                    }
                }
                this.mIcsi.mListener.callSessionInitiatingFailed(imsReasonInfo3);
            }
        } else if (!ImsCallUtil.isCmcSecondaryType(this.mIcsi.mSession.getCmcType()) || !callProfile.isPullCall()) {
            if (this.mIcsi.needDeclineDuringQecCall()) {
                imsReasonInfo2 = new ImsReasonInfo(Id.REQUEST_IM_START_MEDIA, i);
            } else if ((i <= 5000 || i >= 6000) && (i < 6034 || i > 6127)) {
                imsReasonInfo2 = new ImsReasonInfo(DataTypeConvertor.convertCallErrorReasonToFw(i), i);
            } else {
                imsReasonInfo2 = new ImsReasonInfo(Id.REQUEST_GC_UPDATE_PARTICIPANTS, i);
            }
            this.mIcsi.mListener.callSessionTerminated(imsReasonInfo2);
        } else {
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            this.mIcsi.mListener.callSessionInitiatingFailed(imsCallSessionImpl3.changeCmcErrorReason(imsCallSessionImpl3.mSession.getCmcType(), i, str));
        }
        ImsCallSessionImpl imsCallSessionImpl4 = this.mIcsi;
        imsCallSessionImpl4.mState = 8;
        imsCallSessionImpl4.releaseSessionListeners();
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004f, code lost:
        r3 = r7.mIcsi;
     */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00dd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean onErrorWhileNegoOrLater(int r8, java.lang.String r9) throws android.os.RemoteException {
        /*
            r7 = this;
            com.sec.internal.imsphone.ImsCallSessionImpl r0 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r0 = r0.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            android.telephony.ims.ImsReasonInfo r1 = new android.telephony.ims.ImsReasonInfo
            int r2 = com.sec.internal.imsphone.DataTypeConvertor.convertCallErrorReasonToFw(r8)
            r1.<init>(r2, r8, r9)
            r9 = 1105(0x451, float:1.548E-42)
            r2 = 1
            if (r8 == r9) goto L_0x0120
            r9 = 1106(0x452, float:1.55E-42)
            if (r8 == r9) goto L_0x011f
            r9 = 1111(0x457, float:1.557E-42)
            r2 = 0
            if (r8 == r9) goto L_0x0118
            r9 = 1112(0x458, float:1.558E-42)
            if (r8 == r9) goto L_0x0110
            switch(r8) {
                case 1118: goto L_0x0108;
                case 1119: goto L_0x0100;
                case 1120: goto L_0x00f8;
                case 1121: goto L_0x00f0;
                default: goto L_0x0026;
            }
        L_0x0026:
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r9 = r9.mSession
            int r9 = r9.getPhoneId()
            com.sec.internal.constants.Mno r9 = com.sec.internal.helper.SimUtil.getSimMno(r9)
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r3 = r3.mSession
            int r3 = r3.getCmcType()
            boolean r3 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r3)
            r4 = 4
            if (r3 != 0) goto L_0x004f
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r3 = r3.mSession
            int r3 = r3.getCmcType()
            boolean r3 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r3)
            if (r3 == 0) goto L_0x0059
        L_0x004f:
            com.sec.internal.imsphone.ImsCallSessionImpl r3 = r7.mIcsi
            int r5 = r3.mState
            r6 = 3
            if (r5 == r6) goto L_0x007b
            if (r5 != r4) goto L_0x0059
            goto L_0x007b
        L_0x0059:
            if (r0 == 0) goto L_0x0085
            boolean r3 = r0.hasCSFBError()
            if (r3 == 0) goto L_0x0085
            boolean r9 = r9.isKor()
            if (r9 != 0) goto L_0x006d
            boolean r9 = r0.isECallConvertedToNormal()
            if (r9 == 0) goto L_0x0085
        L_0x006d:
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r7.mIcsi
            int r9 = r9.mState
            if (r9 >= r4) goto L_0x0085
            android.telephony.ims.ImsReasonInfo r1 = new android.telephony.ims.ImsReasonInfo
            r9 = 146(0x92, float:2.05E-43)
            r1.<init>(r9, r8)
            goto L_0x0085
        L_0x007b:
            com.sec.ims.volte2.IImsCallSession r9 = r3.mSession
            int r9 = r9.getCmcType()
            android.telephony.ims.ImsReasonInfo r1 = r3.changeCmcErrorReason(r9, r8)
        L_0x0085:
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r9 = r9.mSession
            int r9 = r9.getCmcType()
            boolean r9 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r9)
            r3 = 603(0x25b, float:8.45E-43)
            if (r9 == 0) goto L_0x00ac
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r7.mIcsi
            int r4 = r9.mState
            r5 = 2
            if (r4 != r5) goto L_0x00ac
            r4 = 200(0xc8, float:2.8E-43)
            if (r8 == r4) goto L_0x00ac
            r4 = 210(0xd2, float:2.94E-43)
            if (r8 == r4) goto L_0x00ac
            if (r8 == r3) goto L_0x00ac
            android.telephony.ims.aidl.IImsCallSessionListener r8 = r9.mListener
            r8.callSessionInitiatingFailed(r1)
            goto L_0x00d3
        L_0x00ac:
            com.sec.internal.imsphone.ImsCallSessionImpl r9 = r7.mIcsi
            com.sec.ims.volte2.IImsCallSession r9 = r9.mSession
            int r9 = r9.getCmcType()
            boolean r9 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r9)
            if (r9 == 0) goto L_0x00cc
            if (r0 == 0) goto L_0x00cc
            boolean r9 = r0.isPullCall()
            if (r9 == 0) goto L_0x00cc
            if (r8 != r3) goto L_0x00cc
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r8 = r8.mListener
            r8.callSessionInitiatingFailed(r1)
            goto L_0x00d3
        L_0x00cc:
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r8 = r8.mListener
            r8.callSessionTerminated(r1)
        L_0x00d3:
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r7.mIcsi
            r9 = 8
            r8.mState = r9
            boolean r9 = r8.mIsEcbmSupport
            if (r9 == 0) goto L_0x00ea
            com.sec.internal.imsphone.MmTelFeatureImpl r8 = r8.mMmTelFeatureImpl
            com.sec.internal.imsphone.ImsEcbmImpl r8 = r8.getImsEcbmImpl()
            r8.enterEmergencyCallbackMode()
            com.sec.internal.imsphone.ImsCallSessionImpl r8 = r7.mIcsi
            r8.mIsEcbmSupport = r2
        L_0x00ea:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            r7.releaseSessionListeners()
            goto L_0x011f
        L_0x00f0:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionCancelTransferFailed(r1)
            goto L_0x011f
        L_0x00f8:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionCancelTransferred()
            goto L_0x011f
        L_0x0100:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionTransferFailed(r1)
            goto L_0x011f
        L_0x0108:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionTransferred()
            goto L_0x011f
        L_0x0110:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionResumeFailed(r1)
            goto L_0x011f
        L_0x0118:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionHoldFailed(r1)
        L_0x011f:
            return r2
        L_0x0120:
            com.sec.internal.imsphone.ImsCallSessionImpl r7 = r7.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r7 = r7.mListener
            r7.callSessionMergeFailed(r1)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsCallSessionEventListener.onErrorWhileNegoOrLater(int, java.lang.String):boolean");
    }

    public void onProfileUpdated(MediaProfile mediaProfile, MediaProfile mediaProfile2) throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl.mListener;
        if (iImsCallSessionListener != null) {
            iImsCallSessionListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
        }
    }

    public void onConferenceEstablished() throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        imsCallSessionImpl.mState = 4;
        imsCallSessionImpl.updateCallProfile();
    }

    public void onParticipantUpdated(int i, String[] strArr, int[] iArr, int[] iArr2) throws RemoteException {
        this.mIcsi.mMmTelFeatureImpl.clearConferenceStateList();
        for (int i2 = 0; i2 < iArr.length; i2++) {
            String str = strArr[i2];
            if (str.startsWith("*23#")) {
                str = str.substring(4, str.length());
            }
            String replaceAll = str.replaceAll("[^0-9]", "");
            if (replaceAll.startsWith("010")) {
                replaceAll = replaceAll.substring(3, replaceAll.length());
            }
            if (replaceAll.length() > 8) {
                replaceAll = replaceAll.substring(0, 8);
            }
            this.mIcsi.mMmTelFeatureImpl.putConferenceStateList(Integer.parseInt(replaceAll), this.mIcsi.mSession.getCallId(), strArr[i2], Integer.toString(this.mIcsi.mSession.getCallId()), ImsCallUtil.participantStatus(iArr[i2]), iArr2[i2], this.mIcsi.getCallProfile());
        }
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        imsCallSessionImpl.mMmTelFeatureImpl.updateSecConferenceInfo(imsCallSessionImpl.mCallProfile);
        ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
        imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.mCallProfile);
        ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
        imsCallSessionImpl3.mListener.callSessionConferenceStateUpdated(imsCallSessionImpl3.mMmTelFeatureImpl.getImsConferenceState());
    }

    public void onParticipantAdded(int i) throws RemoteException {
        ImsCallSessionImpl callSession = this.mIcsi.mMmTelFeatureImpl.getCallSession(i);
        if (this.mIcsi.mMmTelFeatureImpl.hasConferenceHost() && callSession != null) {
            ImsCallSessionImpl conferenceHost = this.mIcsi.mMmTelFeatureImpl.getConferenceHost();
            IImsCallSession sessionByCallId = this.mIcsi.mVolteServiceModule.getSessionByCallId(i);
            if (sessionByCallId != null) {
                CallProfile callProfile = sessionByCallId.getCallProfile();
                String dialingNumber = callProfile.getDialingNumber();
                if (!TextUtils.isEmpty(dialingNumber)) {
                    this.mIcsi.mMmTelFeatureImpl.putConferenceState(i, dialingNumber, Integer.toString(i), "connected", this.mIcsi.getCallProfile(), callProfile.getLetteringText());
                }
                ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                if (imsCallSessionImpl.mListener != null) {
                    imsCallSessionImpl.mMmTelFeatureImpl.updateSecConferenceInfo(imsCallSessionImpl.mCallProfile);
                    ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                    imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.mCallProfile);
                    ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
                    imsCallSessionImpl3.mListener.callSessionConferenceStateUpdated(imsCallSessionImpl3.mMmTelFeatureImpl.getImsConferenceState());
                } else if (conferenceHost.getListener() != null) {
                    ImsCallSessionImpl imsCallSessionImpl4 = this.mIcsi;
                    imsCallSessionImpl4.mMmTelFeatureImpl.updateSecConferenceInfo(imsCallSessionImpl4.mCallProfile);
                    conferenceHost.getListener().callSessionUpdated(this.mIcsi.mCallProfile);
                    conferenceHost.getListener().callSessionConferenceStateUpdated(this.mIcsi.mMmTelFeatureImpl.getImsConferenceState());
                }
                callSession.mIsConferenceParticipant = true;
            }
            if (callSession.mIsConferenceHost && conferenceHost.getListener() != null) {
                conferenceHost.getListener().callSessionMergeComplete(this.mIcsi.mImpl);
                ImsCallSessionImpl imsCallSessionImpl5 = this.mIcsi;
                imsCallSessionImpl5.mMmTelFeatureImpl.setConferenceHost(imsCallSessionImpl5.mImpl);
                ImsCallSessionImpl imsCallSessionImpl6 = this.mIcsi;
                IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl6.mListener;
                if (iImsCallSessionListener == null) {
                    conferenceHost.getListener().callSessionResumed(this.mIcsi.getCallProfile());
                } else {
                    iImsCallSessionListener.callSessionResumed(imsCallSessionImpl6.getCallProfile());
                }
            }
            if (!this.mIcsi.mMmTelFeatureImpl.isInitialMerge()) {
                callSession.getListener().callSessionMergeComplete((com.android.ims.internal.IImsCallSession) null);
                if (this.mIcsi.mMmTelFeatureImpl.getConferenceHost().getInternalState() == CallConstants.STATE.HeldCall) {
                    this.mIcsi.mMmTelFeatureImpl.getConferenceHost().resume((ImsStreamMediaProfile) null);
                }
            }
        }
    }

    public void onParticipantRemoved(int i) {
        this.mIcsi.updateConferenceStatus(i, "disconnected");
        this.mIcsi.mMmTelFeatureImpl.removeConferenceState(i);
    }

    public void onConfParticipantHeld(int i, boolean z) throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        if (imsCallSessionImpl.mListener != null) {
            imsCallSessionImpl.onSuppServiceReceived(1, 32);
        }
        this.mIcsi.updateConferenceStatus(i, "on-hold");
    }

    public void onConfParticipantResumed(int i, boolean z) throws RemoteException {
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        if (imsCallSessionImpl.mListener != null) {
            imsCallSessionImpl.onSuppServiceReceived(1, 3);
        }
        this.mIcsi.updateConferenceStatus(i, "connected");
    }

    public void onUssdReceived(int i, int i2, byte[] bArr) throws RemoteException {
        String str;
        try {
            str = new String(bArr, "UTF-8");
        } catch (UnsupportedEncodingException unused) {
            str = null;
        }
        if (i == 2) {
            this.mIcsi.mListener.callSessionUssdMessageReceived(1, str);
        } else if (str == null || !str.contains("error-code")) {
            this.mIcsi.mListener.callSessionUssdMessageReceived(0, str);
        } else {
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            if (imsCallSessionImpl.mIsUssdTerminatedByUser) {
                Log.i(LOG_TAG, "Ignoring USSD error because session was terminated by user");
            } else {
                imsCallSessionImpl.mListener.callSessionUssdMessageReceived(USSD_MODE_NW_ERROR, str);
            }
        }
    }

    public void onEpdgStateChanged() throws RemoteException {
        this.mIcsi.updateCallProfile();
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        IImsCallSessionListener iImsCallSessionListener = imsCallSessionImpl.mListener;
        if (iImsCallSessionListener != null) {
            iImsCallSessionListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
        }
    }

    public void onSessionChanged(int i) throws RemoteException {
        IImsCallSession sessionByCallId = this.mIcsi.mVolteServiceModule.getSessionByCallId(i);
        if (sessionByCallId != null) {
            this.mIcsi.mSession = sessionByCallId;
        }
    }

    public void onImsGeneralEvent(String str, Bundle bundle) throws RemoteException {
        if (this.mIcsi.mListener != null) {
            Log.i(LOG_TAG, "onImsGeneralEvent:" + str);
            if (isOnlyCallProfileChanged(str)) {
                this.mIcsi.updateCallProfile();
                ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                imsCallSessionImpl.mListener.callSessionUpdated(imsCallSessionImpl.getCallProfile());
                return;
            }
            this.mIcsi.mListener.callSessionImsCallEvent(str, bundle);
        }
    }

    private boolean isOnlyCallProfileChanged(String str) {
        return "VCIDGeneralFailure".equals(str) || "NotifyQuantumEncryptionStatus".equals(str) || "NotifyDSDAVideoCapa".equals(str);
    }

    public void onRetryingVoLteOrCsCall(int i) throws RemoteException {
        if (i == 1) {
            this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(147, NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "PS Retry Required"));
        } else {
            this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(146, 1112, "CS Retry Required"));
        }
        ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
        imsCallSessionImpl.mState = 8;
        imsCallSessionImpl.releaseSessionListeners();
    }

    private void notifyAlertInfo() throws RemoteException {
        CallProfile callProfile = this.mIcsi.mSession.getCallProfile();
        String alertInfo = callProfile.getAlertInfo();
        Mno simMno = SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId());
        if ("<urn:alert:service:call-waiting>".equals(alertInfo)) {
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            if (!imsCallSessionImpl.mIsCWNotified) {
                imsCallSessionImpl.mIsCWNotified = true;
                imsCallSessionImpl.onSuppServiceReceived(0, 3);
                return;
            }
        }
        if (simMno != Mno.CMCC) {
            return;
        }
        if ("<urn:alert:service:forward>".equals(alertInfo) && callProfile.getDirection() == 0) {
            this.mIcsi.onSuppServiceReceived(callProfile.getDirection(), 2);
        } else if ("<urn:alert:service:normal>".equals(alertInfo)) {
            this.mIcsi.onSuppServiceReceived(0, 9);
        }
    }

    private void updateCallProfileForDtmfEvent() throws RemoteException {
        this.mIcsi.updateCallProfile();
        CallProfile callProfile = this.mIcsi.mSession.getCallProfile();
        if (this.mForceUpdateCallProfileForDtmfEvent) {
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            if (imsCallSessionImpl.mState == 2 && imsCallSessionImpl.mListener != null && callProfile != null && !callProfile.getDelayRinging()) {
                Log.i(LOG_TAG, "updateCallProfileForDtmfEvent");
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                imsCallSessionImpl2.mListener.callSessionUpdated(imsCallSessionImpl2.getCallProfile());
            }
        }
    }
}

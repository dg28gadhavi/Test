package com.sec.internal.ims.servicemodules.volte2;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;

public class ImsExternalCallController {
    public static final String LOG_TAG = "ImsExternalCallController";
    IVolteServiceModuleInternal mModule;
    private ImsCallSession mPullingSession = null;
    private int mTransferSessionId = 0;
    private String mTransferTarget;

    public ImsExternalCallController(IVolteServiceModuleInternal iVolteServiceModuleInternal) {
        this.mModule = iVolteServiceModuleInternal;
    }

    public void pushCall(ImsCallSession imsCallSession, String str, ImsRegistration imsRegistration) {
        boolean isSoftphoneEnabled = imsRegistration != null ? imsRegistration.getImsProfile().isSoftphoneEnabled() : false;
        if (imsCallSession.getCallState() == CallConstants.STATE.HeldCall || isSoftphoneEnabled) {
            transfer(imsCallSession.getSessionId(), str);
            return;
        }
        try {
            imsCallSession.hold(new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1));
            imsCallSession.setHoldBeforeTransfer(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mTransferSessionId = imsCallSession.getSessionId();
        this.mTransferTarget = str;
    }

    public void pushCallInternal() {
        Log.i(LOG_TAG, "pushCallInternal");
        transfer(this.mTransferSessionId, this.mTransferTarget);
        this.mTransferSessionId = 0;
        this.mTransferTarget = null;
    }

    public void consultativeTransferCall(ImsCallSession imsCallSession, ImsCallSession imsCallSession2, ImsRegistration imsRegistration) {
        Mno mno;
        boolean z;
        int phoneId = imsCallSession.getPhoneId();
        if (imsRegistration == null) {
            mno = SimUtil.getSimMno(phoneId);
            z = false;
        } else {
            Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            z = imsRegistration.getImsProfile().getEctNoHoldForActiveCall();
            mno = fromName;
        }
        if (z) {
            Log.i(LOG_TAG, "No need to hold an active call for ECT.");
            this.mTransferSessionId = imsCallSession2.getSessionId();
            this.mTransferTarget = imsCallSession.getCallProfile().getDialingNumber();
            pushCallInternal();
            return;
        }
        try {
            imsCallSession.hold(new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1));
            imsCallSession.setHoldBeforeTransfer(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mno == Mno.VODAFONE_SPAIN || mno == Mno.TELEFONICA_CZ || mno == Mno.TELENOR_DK || mno == Mno.TDC_DK || mno == Mno.H3G_DK || mno == Mno.ORANGE || mno == Mno.DLOG) {
            this.mTransferSessionId = imsCallSession.getSessionId();
            this.mTransferTarget = imsCallSession2.getCallProfile().getDialingNumber();
        } else {
            this.mTransferSessionId = imsCallSession2.getSessionId();
            this.mTransferTarget = imsCallSession.getCallProfile().getDialingNumber();
        }
        String str = LOG_TAG;
        Log.i(str, "ConsultativeTrasnfer mTransferSessionId : " + this.mTransferSessionId + ", mTransferTarget : " + this.mTransferTarget);
    }

    public void transfer(int i, String str) {
        ImsCallSession session = this.mModule.getSession(i);
        if (session != null) {
            session.pushCall(str);
        }
    }

    public void transferCall(int i, String str, String str2, DialogEvent[] dialogEventArr) throws RemoteException {
        String str3 = LOG_TAG;
        Log.i(str3, "try to transferCall from " + IMSLog.checker(str) + " to Dialog Id : " + str2);
        if (TextUtils.isEmpty(str2) || TextUtils.isEmpty(str)) {
            Log.e(str3, "ignore wrong transfer reqeuset");
            return;
        }
        Dialog dialog = null;
        for (int i2 = 0; i2 < dialogEventArr.length && dialog == null; i2++) {
            DialogEvent dialogEvent = dialogEventArr[i2];
            if (dialogEvent != null) {
                Iterator it = dialogEvent.getDialogList().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Dialog dialog2 = (Dialog) it.next();
                    if (str2.equals(dialog2.getDialogId()) && str.equals(dialogEventArr[i2].getMsisdn())) {
                        i = i2;
                        dialog = dialog2;
                        break;
                    }
                }
            }
        }
        DialogEvent dialogEvent2 = dialogEventArr[i];
        if (dialogEvent2 != null) {
            ImsRegistration regInfo = this.mModule.getRegInfo(dialogEvent2.getRegId());
            if (regInfo == null) {
                Log.e(LOG_TAG, "can't call transfer without registration");
                return;
            }
            boolean isSamsungMdmnEnabled = regInfo.getImsProfile().isSamsungMdmnEnabled();
            Iterator it2 = dialogEventArr[i].getDialogList().iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                Dialog dialog3 = (Dialog) it2.next();
                if (str2.equals(dialog3.getDialogId())) {
                    String str4 = LOG_TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("find target Dialog ");
                    sb.append(IMSLog.checker(dialog3 + ""));
                    Log.i(str4, sb.toString());
                    dialog = dialog3;
                    break;
                }
            }
            if (dialog == null || TextUtils.isEmpty(dialog.getSipCallId()) || TextUtils.isEmpty(dialog.getSipLocalTag()) || TextUtils.isEmpty(dialog.getSipRemoteTag())) {
                Log.e(LOG_TAG, "Can't find proper target dialog");
                return;
            }
            CallProfile callProfile = new CallProfile();
            MediaProfile mediaProfile = new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1);
            callProfile.setPullCall(true);
            callProfile.setCallType(dialog.getCallType());
            callProfile.setMediaProfile(mediaProfile);
            callProfile.setCLI((String) null);
            if (isSamsungMdmnEnabled) {
                dialog.setMdmnExtNumber(dialog.getSessionDescription());
            }
            ImsCallSession createSession = this.mModule.createSession(callProfile, dialogEventArr[i].getRegId());
            this.mPullingSession = createSession;
            int pulling = createSession.pulling(str, dialog);
            Log.i(LOG_TAG, "pulling Success : " + pulling);
            if (isSamsungMdmnEnabled) {
                this.mPullingSession.getCallProfile().setDialingNumber(dialog.getSessionDescription());
            }
            this.mModule.notifyOnPulling(i, this.mPullingSession.getCallId());
            return;
        }
        Log.e(LOG_TAG, "LastDialogEvent is Empty");
    }
}

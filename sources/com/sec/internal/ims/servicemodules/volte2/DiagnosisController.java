package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.log.IMSLog;

public class DiagnosisController implements IDiagnosisController {
    private String LOG_TAG = "DiagnosisController";
    private CallStateMachine mCsm;
    private ImsCallSession mSession;

    public DiagnosisController(CallStateMachine callStateMachine) {
        this.mCsm = callStateMachine;
        this.mSession = callStateMachine.mSession;
    }

    public void sendPSDailyInfo() {
        ContentValues contentValues = new ContentValues();
        String str = this.LOG_TAG;
        Log.i(str, "CallTypeHistory[" + this.mCsm.mCallTypeHistory + "]");
        if (!TextUtils.isEmpty(this.mCsm.mCallTypeHistory)) {
            String[] split = this.mCsm.mCallTypeHistory.split(",");
            if (this.mCsm.mSession.getCmcType() > 0) {
                if (!this.mCsm.mSession.getCallProfile().isPullCall() && TextUtils.isEmpty(this.mCsm.mSession.getCallProfile().getReplaceSipCallId())) {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_CMC_START_TOTAL_COUNT, 1);
                }
            } else if (this.mSession.mIsNrSaMode) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_START_TOTAL_COUNT, 1);
                if (this.mSession.mEpsFallback) {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_EPS_FALLBACK_CALL_COUNT, 1);
                }
            }
            dailyInfoCallEnd(contentValues);
            dailyInfoCallType(contentValues, split);
            contentValues.put(DiagnosisConstants.DRPT_KEY_SRVCC_COUNT, Integer.valueOf(this.mSession.getEndReason() == 8 ? 1 : 0));
            contentValues.put(DiagnosisConstants.DRPT_KEY_CSFB_COUNT, Integer.valueOf(this.mSession.getCallProfile().hasCSFBError() ? 1 : 0));
            contentValues.put(DiagnosisConstants.DRPT_KEY_FORWARDED_COUNT, Integer.valueOf(this.mSession.getForwarded() ? 1 : 0));
            contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
            String str2 = this.LOG_TAG;
            int phoneId = this.mSession.getPhoneId();
            IMSLog.i(str2, phoneId, "DRPT, storeLogToAgent[" + contentValues.toString() + "]");
            ImsLogAgentUtil.storeLogToAgent(this.mSession.getPhoneId(), this.mCsm.mContext, "DRPT", contentValues);
        }
    }

    public void sendPSCallInfo() {
        String str;
        MobileCareController mobileCareController = this.mSession.mModule.getMobileCareController();
        if (mobileCareController == null) {
            Log.i(this.LOG_TAG, "mobileCareController is null, stop make PS data");
            return;
        }
        int phoneId = this.mSession.getPhoneId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.PSCI_KEY_LTE_BAND, Integer.valueOf(mobileCareController.getLteBand()));
        contentValues.put("RSRP", Integer.valueOf(mobileCareController.getLteRsrp(phoneId)));
        contentValues.put("RSRQ", Integer.valueOf(mobileCareController.getLteRsrq(phoneId)));
        contentValues.put(DiagnosisConstants.PSCI_KEY_NETWORK_TYPE, Integer.valueOf(this.mSession.mModule.getNetwork(phoneId) != null ? this.mSession.mModule.getNetwork(phoneId).network : 0));
        if (!this.mCsm.mTelephonyManager.semIsVoNrEnabled(this.mSession.getPhoneId()) && this.mSession.mModule.getNetwork(phoneId) != null && this.mSession.mModule.getNetwork(phoneId).network == 20) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_NETWORK_TYPE, 13);
        }
        ImsCallSession imsCallSession = this.mSession;
        contentValues.put(DiagnosisConstants.PSCI_KEY_RAT_CHANGED, Integer.valueOf(imsCallSession.mModule.getRatChanged(imsCallSession.getPhoneId()) ? 1 : 0));
        this.mSession.mModule.setRatChanged(phoneId, false);
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mSIPFlowInfo.length() > 30) {
            String str2 = this.mCsm.mSIPFlowInfo;
            str = str2.substring(str2.length() - 30);
        } else {
            str = this.mCsm.mSIPFlowInfo;
        }
        callStateMachine.mSIPFlowInfo = str;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        CallStateMachine callStateMachine2 = this.mCsm;
        long j = (elapsedRealtime - callStateMachine2.mCallInitTime) / 1000;
        contentValues.put(DiagnosisConstants.PSCI_KEY_SIP_FLOW, callStateMachine2.mSIPFlowInfo);
        contentValues.put("MOMT", Integer.valueOf(this.mSession.getCallProfile().isMOCall() ? 1 : 0));
        contentValues.put("TYPE", Integer.valueOf(this.mSession.getCallProfile().getCallType()));
        if (this.mSession.getCallProfile().isConferenceCall()) {
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                contentValues.put("TYPE", 6);
            } else {
                contentValues.put("TYPE", 5);
            }
        }
        contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, Integer.valueOf(this.mSession.getPrevCallStateOrdinal()));
        if (j <= 999999) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_TIME, Integer.valueOf((int) j));
        }
        if (isCallDrop(this.mCsm.errorCode)) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_FAIL_CODE, Integer.valueOf(this.mCsm.errorCode));
        }
        if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
            CallStateMachine callStateMachine3 = this.mCsm;
            if (callStateMachine3.mVideoRTPtimeout) {
                contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 2);
            } else if (!callStateMachine3.mIsStartCameraSuccess) {
                contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 3);
            } else {
                contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 1);
            }
        }
        contentValues.put("ROAM", Integer.valueOf(this.mCsm.mTelephonyManager.isNetworkRoaming() ? 1 : 0));
        if (isEPDGWhenCallEnd(this.mCsm.errorCode)) {
            if (this.mSession.mCallProfile.isCrossSimCall()) {
                int phoneId2 = this.mSession.getPhoneId();
                int i = ImsConstants.Phone.SLOT_1;
                if (phoneId2 == i) {
                    i = ImsConstants.Phone.SLOT_2;
                }
                IPdnController pdnController = ImsRegistry.getPdnController();
                contentValues.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, Integer.valueOf(DiagnosisConstants.EPDG_STATUS.AVAILABLE_MOBILE_DATA_PHYSICAL_INTERFACE.getValue()));
                contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, Integer.valueOf((pdnController.getNetworkState(i).getMobileDataNetworkType() == 20 ? DiagnosisConstants.CALL_BEARER.NR : DiagnosisConstants.CALL_BEARER.LTE).getValue()));
            } else {
                contentValues.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, Integer.valueOf(DiagnosisConstants.EPDG_STATUS.AVAILABLE.getValue()));
                contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, Integer.valueOf(DiagnosisConstants.CALL_BEARER.WLAN.getValue()));
            }
        } else if (!this.mCsm.mTelephonyManager.semIsVoNrEnabled(this.mSession.getPhoneId()) || !isNrWhenCallEnd(this.mSession.getPhoneId())) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, Integer.valueOf(DiagnosisConstants.EPDG_STATUS.UNAVAILABLE.getValue()));
            contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, Integer.valueOf(DiagnosisConstants.CALL_BEARER.LTE.getValue()));
        } else {
            contentValues.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, Integer.valueOf(DiagnosisConstants.EPDG_STATUS.UNAVAILABLE.getValue()));
            contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, Integer.valueOf(DiagnosisConstants.CALL_BEARER.NR.getValue()));
        }
        CallStateMachine callStateMachine4 = this.mCsm;
        long j2 = callStateMachine4.mCallRingingTime - callStateMachine4.mCallInitTime;
        if (j2 > 0 && j2 <= 999999) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_SETUP_TIME, Integer.valueOf((int) j2));
        }
        CallStateMachine callStateMachine5 = this.mCsm;
        long j3 = callStateMachine5.mCallEndTime - callStateMachine5.mCallTerminateTime;
        if (j3 > 0 && j3 <= 999999) {
            contentValues.put(DiagnosisConstants.PSCI_KEY_CALL_END_TIME, Integer.valueOf((int) j3));
        }
        IMSLog.c(LogClass.VOLTE_END_CALL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + ":" + this.mCsm.mCallTypeHistory + ":" + j + "," + (isCallDrop(this.mCsm.errorCode) ? 1 : 0) + "," + this.mSession.getEndReason() + "," + this.mCsm.errorCode);
        ImsLogAgentUtil.storeLogToAgent(this.mSession.getPhoneId(), this.mCsm.mContext, DiagnosisConstants.FEATURE_PSCI, contentValues);
        String str3 = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("PSCI, storeLogToAgent[");
        sb.append(contentValues.toString());
        sb.append("]");
        Log.i(str3, sb.toString());
        ImsLogAgentUtil.requestToSendStoredLog(this.mSession.getPhoneId(), this.mCsm.mContext, DiagnosisConstants.FEATURE_PSCI);
    }

    public boolean isCallDrop(int i) {
        return (this.mSession.getCallProfile().hasCSFBError() || i == 200 || i == 210 || i == 220 || i == 230 || i == 1000 || i == 486 || i == 603 || i == 1111 || i == 3009 || i == 3010) ? false : true;
    }

    private boolean isEPDGWhenCallEnd(int i) {
        return this.mSession.isEpdgCall() || i == 2503 || this.mSession.getEndReason() == 21;
    }

    private boolean isNrWhenCallEnd(int i) {
        NetworkEvent network = this.mSession.mModule.getNetwork(i);
        return network != null && network.network == 20;
    }

    /* access modifiers changed from: protected */
    public void dailyInfoCallEnd(ContentValues contentValues) {
        if (!needToDailyInfoCallEnd()) {
            return;
        }
        if (this.mCsm.mSession.getCmcType() > 0) {
            dailyInfoCallEndForCmc(contentValues);
        } else if (this.mCsm.mTelephonyManager.semIsVoNrEnabled(this.mSession.getPhoneId()) && isNrWhenCallEnd(this.mSession.mPhoneId)) {
            dailyInfoCallEndForNR(contentValues);
        } else if (isEPDGWhenCallEnd(this.mCsm.errorCode)) {
            dailyInfoCallEndForVoWifi(contentValues);
        } else {
            dailyInfoCallEndForVolte(contentValues);
        }
    }

    private boolean needToDailyInfoCallEnd() {
        return this.mSession.getEndReason() != 8 && !this.mSession.getCallProfile().hasCSFBError();
    }

    private void dailyInfoCallEndForVoWifi(ContentValues contentValues) {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_VIDEO_COUNT, 1);
        } else if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_EMERGENCY_COUNT, 1);
        } else {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_VOICE_COUNT, 1);
        }
        if (isCallDrop(this.mCsm.errorCode)) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_FAIL_COUNT, 1);
            if (isIncomingFail()) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_INCOMING_FAIL, 1);
            } else if (isOutgoingFail()) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_OUTGOING_FAIL, 1);
            }
        }
        contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_TOTAL_COUNT, 1);
    }

    private void dailyInfoCallEndForVolte(ContentValues contentValues) {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_VIDEO_COUNT, 1);
        } else if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_EMERGENCY_COUNT, 1);
        } else {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_VOICE_COUNT, 1);
        }
        if (isCallDrop(this.mCsm.errorCode)) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_FAIL_COUNT, 1);
            if (isIncomingFail()) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_INCOMING_FAIL, 1);
            } else if (isOutgoingFail()) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_OUTGOING_FAIL, 1);
            }
        }
        contentValues.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_TOTAL_COUNT, 1);
    }

    private void dailyInfoCallEndForNR(ContentValues contentValues) {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_END_VIDEO_COUNT, 1);
        } else if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_END_EMERGENCY_COUNT, 1);
        } else {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_END_VOICE_COUNT, 1);
        }
        if (isCallDrop(this.mCsm.errorCode)) {
            contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_END_FAIL_COUNT, 1);
        }
        contentValues.put(DiagnosisConstants.DRPT_KEY_VONR_END_TOTAL_COUNT, 1);
    }

    private void dailyInfoCallEndForCmc(ContentValues contentValues) {
        if (!this.mCsm.mSession.getCallProfile().isPullCall() && TextUtils.isEmpty(this.mCsm.mSession.getCallProfile().getReplaceSipCallId())) {
            if (isCallDrop(this.mCsm.errorCode)) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_CMC_END_FAIL_COUNT, 1);
                if (isIncomingFail()) {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_CMC_INCOMING_FAIL, 1);
                } else if (isOutgoingFail()) {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_CMC_OUTGOING_FAIL, 1);
                }
            }
            contentValues.put(DiagnosisConstants.DRPT_KEY_CMC_END_TOTAL_COUNT, 1);
        }
    }

    private boolean isIncomingFail() {
        State previousState = this.mCsm.getPreviousState();
        CallStateMachine callStateMachine = this.mCsm;
        return previousState == callStateMachine.mIncomingCall || (callStateMachine.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isMTCall());
    }

    private boolean isOutgoingFail() {
        State previousState = this.mCsm.getPreviousState();
        CallStateMachine callStateMachine = this.mCsm;
        if (previousState != callStateMachine.mOutgoingCall) {
            State previousState2 = callStateMachine.getPreviousState();
            CallStateMachine callStateMachine2 = this.mCsm;
            return previousState2 == callStateMachine2.mAlertingCall || (callStateMachine2.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isMOCall());
        }
    }

    private void dailyInfoCallType(ContentValues contentValues, String[] strArr) {
        boolean[] zArr = new boolean[6];
        for (String parseInt : strArr) {
            int parseInt2 = Integer.parseInt(parseInt);
            if (parseInt2 == 1) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VOICE_COUNT, 1);
                contentValues.put(DiagnosisConstants.DRPT_KEY_DOWNGRADE_TO_VOICE_COUNT, Integer.valueOf(zArr[1] ? 1 : 0));
                zArr[0] = true;
            } else if (ImsCallUtil.isVideoCall(parseInt2)) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VIDEO_COUNT, 1);
                contentValues.put(DiagnosisConstants.DRPT_KEY_UPGRADE_TO_VIDEO_COUNT, Integer.valueOf(zArr[0] ? 1 : 0));
                zArr[1] = true;
            } else if (ImsCallUtil.isE911Call(parseInt2)) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_EMERGENCY_COUNT, 1);
                zArr[2] = true;
            } else if (ImsCallUtil.isTtyCall(parseInt2)) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_TTY_COUNT, 1);
                zArr[3] = true;
            } else if (ImsCallUtil.isRttCall(parseInt2)) {
                contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_RTT_COUNT, 1);
                zArr[4] = true;
            }
            if (this.mSession.getCallProfile().isConferenceCall()) {
                if (ImsCallUtil.isVideoCall(parseInt2)) {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VIDEO_CONFERENCE_COUNT, 1);
                } else {
                    contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_AUDIO_CONFERENCE_COUNT, 1);
                }
                zArr[5] = true;
            }
        }
        int i = 0;
        for (int i2 = 0; i2 < 6; i2++) {
            if (zArr[i2]) {
                i++;
            }
        }
        contentValues.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_TOTAL_COUNT, Integer.valueOf(i));
    }
}

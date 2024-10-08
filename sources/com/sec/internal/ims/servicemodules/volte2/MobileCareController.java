package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.telephony.SignalStrength;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.SignalStrengthWrapper;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobileCareController {
    public static final String ACTIONCALLDROP = "com.samsung.intent.action.IMS_CALL_DROP";
    public static final String ACTIONQUALITYSTATISTICS = "com.sec.android.statistics.VZW_QUALITY_STATISTICS";
    public static final String CALLTYPE = "CallType";
    public static final String ERRORREASON = "ErrorReason";
    public static final String ERRORSTRING = "ErrorString";
    public static final String EVENTNAME = "H015";
    public static final String EVENTTYPE = "event_type";
    private static final String LOG_TAG = "MobileCareController";
    public static final String NETWORKTYPE = "NetworkType";
    public static final String RSRP = "RSRP";
    public static final String RSRQ = "RSRQ";
    public static final String TIMEINFO = "TimeInfo";
    private final Context mContext;
    private Set<Integer> mErrorSet = new HashSet();
    private int mLteBand = -1;
    private int[] mLteRsrp;
    private int[] mLteRsrq;
    protected boolean mQualityStatisticsValid = false;
    private int[] mSignalLevel;

    public boolean isEnabled() {
        return true;
    }

    public MobileCareController(Context context) {
        this.mContext = context;
        initErrorList();
        int size = SimManagerFactory.getAllSimManagers().size();
        int[] iArr = new int[size];
        this.mLteRsrp = iArr;
        this.mLteRsrq = new int[size];
        this.mSignalLevel = new int[size];
        Arrays.fill(iArr, -1);
        Arrays.fill(this.mLteRsrq, -1);
        Arrays.fill(this.mSignalLevel, -1);
        if (SemCscFeature.getInstance().getBoolean(SecFeature.CSC.TAG_CSCFEATURE_COMMON_SUPPORTHUXDEVICEQUALITYSTATISTICS)) {
            this.mQualityStatisticsValid = true;
        }
    }

    private void initErrorList() {
        this.mErrorSet.add(400);
        this.mErrorSet.add(405);
        this.mErrorSet.add(Integer.valueOf(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_VOLTE_DISABLED));
        this.mErrorSet.add(408);
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE));
        this.mErrorSet.add(484);
        this.mErrorSet.add(500);
        this.mErrorSet.add(580);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_VSH_STOP_SESSION));
        this.mErrorSet.add(1108);
        this.mErrorSet.add(1114);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_SIP_DIALOG_OPEN));
        this.mErrorSet.add(1202);
        this.mErrorSet.add(1203);
        this.mErrorSet.add(1204);
        this.mErrorSet.add(Integer.valueOf(Id.REQUEST_CHATBOT_ANONYMIZE));
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS));
        this.mErrorSet.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID));
    }

    public void sendMobileCareEvent(int i, int i2, int i3, String str, boolean z) {
        if (this.mErrorSet.contains(Integer.valueOf(i3))) {
            boolean isVideoCall = ImsCallUtil.isVideoCall(i2);
            String str2 = LOG_TAG;
            Log.i(str2, "sendMobileCareEvent : isVideo [" + isVideoCall + "] isePDG [" + z + "] mRSRP [" + this.mLteRsrp[i] + "] mRSRQ [" + this.mLteRsrq[i] + "] mErrorCode [" + i3 + "] mErrorDesc [" + str + "]");
            Intent intent = new Intent();
            intent.setAction(ACTIONCALLDROP);
            intent.putExtra(CALLTYPE, isVideoCall ? 1 : 0);
            intent.putExtra(NETWORKTYPE, z ? 1 : 0);
            intent.putExtra(TIMEINFO, getCurrentTimeShort());
            intent.putExtra(ERRORREASON, i3);
            if (str == null) {
                str = VolteConstants.ErrorCode.toString(i3);
            }
            intent.putExtra(ERRORSTRING, str);
            intent.putExtra("RSRP", this.mLteRsrp[i]);
            intent.putExtra("RSRQ", this.mLteRsrq[i]);
            this.mContext.sendBroadcast(intent);
            return;
        }
        Log.i(LOG_TAG, "sendMobileCareEvent : Don't need to send event");
    }

    public void sendQualityStatisticsEvent() {
        if (this.mQualityStatisticsValid) {
            Log.i(LOG_TAG, "sendQualityStatisticsEvent");
            Intent intent = new Intent();
            intent.setAction(ACTIONQUALITYSTATISTICS);
            intent.putExtra(EVENTTYPE, EVENTNAME);
            intent.setPackage(ImsConstants.Packages.PACKAGE_QUALITY_DATALOG);
            this.mContext.sendBroadcast(intent);
        }
    }

    public void sendTelephonyNotResponding(List<ImsCallSession> list) {
        StringBuilder sb = new StringBuilder("TERMINATE,");
        sb.append(list.size());
        int i = 0;
        if (list.size() > 0) {
            for (ImsCallSession next : list) {
                if (next != null) {
                    sb.append(",");
                    sb.append(next.getSessionId());
                    sb.append(",");
                    sb.append(next.getCallState());
                    sb.append(",");
                    sb.append(next.getCallProfile() != null ? Integer.valueOf(next.getCallProfile().getCallType()) : "");
                    i = next.getPhoneId();
                }
            }
        } else {
            sb.append("REQUESTED_BY_TELEPHONY");
        }
        IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, sb.toString(), true);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.PSCI_KEY_FAIL_CODE, 1123);
        ImsLogAgentUtil.storeLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_PSCI, contentValues);
        ImsLogAgentUtil.requestToSendStoredLog(i, this.mContext, DiagnosisConstants.FEATURE_PSCI);
        Log.i(LOG_TAG, "terminate not responding session");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0023, code lost:
        r0 = r0[0];
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendMissedCallSms(android.content.Intent r5) {
        /*
            r4 = this;
            android.telephony.SmsMessage[] r0 = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(r5)
            java.lang.String r1 = "subscription"
            r2 = -1
            int r5 = r5.getIntExtra(r1, r2)
            int r5 = com.sec.internal.ims.core.sim.SimManagerFactory.getSlotId(r5)
            android.content.Context r1 = r4.mContext
            java.lang.String r1 = com.sec.internal.helper.ImsCallUtil.getPhraseByMno(r1, r5)
            if (r0 == 0) goto L_0x006b
            r2 = 0
            r3 = r0[r2]
            if (r3 == 0) goto L_0x006b
            boolean r3 = android.text.TextUtils.isEmpty(r1)
            if (r3 != 0) goto L_0x006b
            r0 = r0[r2]
            java.lang.String r2 = r0.getMessageBody()
            if (r2 == 0) goto L_0x006b
            boolean r1 = r2.contains(r1)
            if (r1 == 0) goto L_0x006b
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r5)
            java.lang.String r2 = ", "
            r1.append(r2)
            long r2 = r0.getTimestampMillis()
            r1.append(r2)
            java.lang.String r0 = r1.toString()
            r1 = 1342177288(0x50000008, float:8.5899428E9)
            com.sec.internal.log.IMSLog.c(r1, r0)
            android.content.ContentValues r0 = new android.content.ContentValues
            r0.<init>()
            r1 = 1803(0x70b, float:2.527E-42)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.String r2 = "FLCD"
            r0.put(r2, r1)
            android.content.Context r1 = r4.mContext
            java.lang.String r2 = "PSCI"
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.storeLogToAgent(r5, r1, r2, r0)
            android.content.Context r4 = r4.mContext
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.requestToSendStoredLog(r5, r4, r2)
        L_0x006b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.MobileCareController.sendMissedCallSms(android.content.Intent):void");
    }

    private String getCurrentTimeShort() {
        Calendar instance = Calendar.getInstance();
        String format = new DecimalFormat("00").format((long) instance.get(11));
        String format2 = new DecimalFormat("00").format((long) instance.get(12));
        String format3 = new DecimalFormat("00").format((long) instance.get(13));
        String format4 = new DecimalFormat(NSDSNamespaces.NSDSMigration.DEFAULT_KEY).format((long) instance.get(14));
        return format + ":" + format2 + ":" + format3 + "." + format4;
    }

    public void onSignalStrengthsChanged(int i, SignalStrength signalStrength) {
        if (signalStrength != null) {
            SignalStrengthWrapper signalStrengthWrapper = new SignalStrengthWrapper(signalStrength);
            this.mLteRsrp[i] = signalStrengthWrapper.getLteRsrp();
            this.mLteRsrq[i] = signalStrengthWrapper.getLteRsrq();
            this.mSignalLevel[i] = signalStrengthWrapper.getLevel();
            return;
        }
        Log.i(LOG_TAG, "getLteSignalStrength is null");
        this.mLteRsrp[i] = -1;
        this.mLteRsrq[i] = -1;
        this.mSignalLevel[i] = -1;
    }

    public void onLteBancChanged(String str) {
        try {
            this.mLteBand = Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            this.mLteBand = -1;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "Received LTE Band is " + str + ", mLteBand is " + this.mLteBand);
    }

    public int getLteRsrp(int i) {
        return this.mLteRsrp[i];
    }

    public int getLteRsrq(int i) {
        return this.mLteRsrq[i];
    }

    public int getLteBand() {
        return this.mLteBand;
    }

    public int getSignalLevel(int i) {
        return this.mSignalLevel[i];
    }
}

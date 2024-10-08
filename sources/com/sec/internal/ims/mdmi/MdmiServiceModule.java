package com.sec.internal.ims.mdmi;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.RemoteException;
import com.sec.ims.mdmi.IMdmiEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.log.IMSLog;

public class MdmiServiceModule extends ServiceModuleBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = MdmiServiceModule.class.getSimpleName();
    /* access modifiers changed from: private */
    public long m200OkRecvTime;
    /* access modifiers changed from: private */
    public long mInviteSendingTime;
    private final MdmiE911Listener mListener;
    public IMdmiEventListener mMdmiEventListener;
    /* access modifiers changed from: private */
    public double mMeanvalue;
    /* access modifiers changed from: private */
    public long mNumberOfCalls;
    /* access modifiers changed from: private */
    public long mNumberOfE911Calls;
    /* access modifiers changed from: private */
    public long mNumberOfE911reg;
    /* access modifiers changed from: private */
    public long mNumberOfE922Calls;
    /* access modifiers changed from: private */
    public long mNumberOfSipCancel;
    /* access modifiers changed from: private */
    public long mNumberofSipBye;
    private int mPhoneId;
    /* access modifiers changed from: private */
    public long maxTimeDiffBetweenInviteAndOk;
    /* access modifiers changed from: private */
    public long minTimeDiffBetweenInviteAndOk;

    public enum msgType {
        E911_CALL,
        E922_CALL,
        SIP_INVITE,
        SIP_INVITE_200OK,
        SIP_CANCEL,
        SIP_BYE,
        E911_REGI
    }

    public void handleIntent(Intent intent) {
    }

    public MdmiServiceModule(Looper looper, Context context) {
        super(looper);
        this.mInviteSendingTime = 0;
        this.m200OkRecvTime = 0;
        this.mMeanvalue = 0.0d;
        this.mNumberOfCalls = 0;
        this.mPhoneId = 0;
        this.mListener = new MdmiE911Listener() {
            public void notifySipMsg(msgType msgtype, long j) {
                switch (AnonymousClass2.$SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType[msgtype.ordinal()]) {
                    case 1:
                        MdmiServiceModule mdmiServiceModule = MdmiServiceModule.this;
                        mdmiServiceModule.mNumberOfE911Calls = mdmiServiceModule.mNumberOfE911Calls + 1;
                        return;
                    case 2:
                        MdmiServiceModule mdmiServiceModule2 = MdmiServiceModule.this;
                        mdmiServiceModule2.mNumberOfE922Calls = mdmiServiceModule2.mNumberOfE922Calls + 1;
                        return;
                    case 3:
                        MdmiServiceModule.this.mInviteSendingTime = j;
                        return;
                    case 4:
                        MdmiServiceModule.this.m200OkRecvTime = j;
                        long r7 = MdmiServiceModule.this.m200OkRecvTime - MdmiServiceModule.this.mInviteSendingTime;
                        MdmiServiceModule mdmiServiceModule3 = MdmiServiceModule.this;
                        mdmiServiceModule3.minTimeDiffBetweenInviteAndOk = Math.min(mdmiServiceModule3.minTimeDiffBetweenInviteAndOk, r7);
                        MdmiServiceModule mdmiServiceModule4 = MdmiServiceModule.this;
                        mdmiServiceModule4.maxTimeDiffBetweenInviteAndOk = Math.max(mdmiServiceModule4.maxTimeDiffBetweenInviteAndOk, r7);
                        double r2 = MdmiServiceModule.this.mMeanvalue;
                        MdmiServiceModule mdmiServiceModule5 = MdmiServiceModule.this;
                        mdmiServiceModule5.mMeanvalue = ((((double) mdmiServiceModule5.mNumberOfCalls) * r2) + ((double) r7)) / ((double) (MdmiServiceModule.this.mNumberOfCalls + 1));
                        MdmiServiceModule mdmiServiceModule6 = MdmiServiceModule.this;
                        mdmiServiceModule6.mNumberOfCalls = mdmiServiceModule6.mNumberOfCalls + 1;
                        return;
                    case 5:
                        MdmiServiceModule mdmiServiceModule7 = MdmiServiceModule.this;
                        mdmiServiceModule7.mNumberOfSipCancel = mdmiServiceModule7.mNumberOfSipCancel + 1;
                        return;
                    case 6:
                        MdmiServiceModule mdmiServiceModule8 = MdmiServiceModule.this;
                        mdmiServiceModule8.mNumberofSipBye = mdmiServiceModule8.mNumberofSipBye + 1;
                        return;
                    case 7:
                        MdmiServiceModule mdmiServiceModule9 = MdmiServiceModule.this;
                        mdmiServiceModule9.mNumberOfE911reg = mdmiServiceModule9.mNumberOfE911reg + 1;
                        return;
                    default:
                        return;
                }
            }

            public void onCallEnded() {
                try {
                    String r1 = MdmiServiceModule.LOG_TAG;
                    IMSLog.d(r1, "nE911reg = " + MdmiServiceModule.this.mNumberOfE911reg + " nE911Calls = " + MdmiServiceModule.this.mNumberOfE911Calls + "nE922Calls = " + MdmiServiceModule.this.mNumberOfE922Calls + " nSipCancel = " + MdmiServiceModule.this.mNumberOfSipCancel + " nSipBye = " + MdmiServiceModule.this.mNumberofSipBye + " minTimeDiffBetweenInviteAndOk = " + MdmiServiceModule.this.minTimeDiffBetweenInviteAndOk + " maxTimeDiffBetweenInviteAndOk = " + MdmiServiceModule.this.maxTimeDiffBetweenInviteAndOk + " mMeanvalue = " + MdmiServiceModule.this.mMeanvalue);
                    MdmiServiceModule mdmiServiceModule = MdmiServiceModule.this;
                    IMdmiEventListener iMdmiEventListener = mdmiServiceModule.mMdmiEventListener;
                    if (iMdmiEventListener != null) {
                        long r3 = mdmiServiceModule.mNumberOfE911reg;
                        long r5 = MdmiServiceModule.this.mNumberOfE911Calls;
                        long r7 = MdmiServiceModule.this.mNumberOfE922Calls;
                        long r9 = MdmiServiceModule.this.mNumberOfSipCancel;
                        long r11 = MdmiServiceModule.this.mNumberofSipBye;
                        double r15 = (double) MdmiServiceModule.this.minTimeDiffBetweenInviteAndOk;
                        double r17 = MdmiServiceModule.this.mMeanvalue;
                        iMdmiEventListener.onE911StatsUpdated(r3, r5, r7, r9, r11, r15, (double) MdmiServiceModule.this.maxTimeDiffBetweenInviteAndOk, r17);
                        return;
                    }
                    IMSLog.d(MdmiServiceModule.LOG_TAG, "MDMI event listener is null");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        this.mMdmiEventListener = null;
        this.mNumberOfE911reg = 0;
        this.mNumberOfE911Calls = 0;
        this.mNumberOfE922Calls = 0;
        this.mNumberOfSipCancel = 0;
        this.mNumberofSipBye = 0;
        this.minTimeDiffBetweenInviteAndOk = Long.MAX_VALUE;
        this.maxTimeDiffBetweenInviteAndOk = 0;
        this.mInviteSendingTime = 0;
        this.m200OkRecvTime = 0;
        this.mNumberOfCalls = 0;
    }

    public void init() {
        super.init();
        IMSLog.i(LOG_TAG, "init()");
    }

    /* renamed from: com.sec.internal.ims.mdmi.MdmiServiceModule$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType[] r0 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType = r0
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.E911_CALL     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.E922_CALL     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_INVITE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_INVITE_200OK     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_CANCEL     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_BYE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$mdmi$MdmiServiceModule$msgType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r1 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.E911_REGI     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.mdmi.MdmiServiceModule.AnonymousClass2.<clinit>():void");
        }
    }

    public String[] getServicesRequiring() {
        return new String[]{"mdmi"};
    }

    public MdmiE911Listener getMdmiListener() {
        return this.mListener;
    }

    public void setMdmiEventListener(IMdmiEventListener iMdmiEventListener) {
        this.mMdmiEventListener = iMdmiEventListener;
    }
}

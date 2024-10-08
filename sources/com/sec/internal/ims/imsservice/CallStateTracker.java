package com.sec.internal.ims.imsservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.servicemodules.ServiceModuleManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallStateTracker implements ISequentialInitializable {
    public static final int CALL_CONNECTED = 2;
    public static final int CALL_DISCONECTED = 1;
    public static final int CALL_RESUMED = 4;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "CallStateTracker";
    /* access modifiers changed from: private */
    public final Map<Integer, Map<String, Call>> mCallLists = new HashMap();
    private final BroadcastReceiver mCallStateReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0116  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0198 A[LOOP:0: B:30:0x0192->B:32:0x0198, LOOP_END] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r13, android.content.Intent r14) {
            /*
                r12 = this;
                java.lang.String r13 = com.sec.internal.ims.imsservice.CallStateTracker.LOG_TAG
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "Received intent: "
                r0.append(r1)
                java.lang.String r1 = r14.getAction()
                r0.append(r1)
                java.lang.String r1 = " extra: "
                r0.append(r1)
                android.os.Bundle r1 = r14.getExtras()
                r0.append(r1)
                java.lang.String r0 = r0.toString()
                android.util.Log.i(r13, r0)
                java.lang.String r13 = "EXTRA_CALL_EVENT"
                r0 = -1
                int r13 = r14.getIntExtra(r13, r0)
                java.lang.String r1 = "EXTRA_TEL_NUMBER"
                java.lang.String r1 = r14.getStringExtra(r1)
                java.lang.String r2 = "EXTRA_PHONE_ID"
                int r0 = r14.getIntExtra(r2, r0)
                java.lang.String r2 = "EXTRA_IS_INCOMING"
                r3 = 0
                java.lang.Integer r7 = java.lang.Integer.valueOf(r3)
                boolean r4 = r14.getBooleanExtra(r2, r3)
                java.lang.String r2 = "EXTRA_IS_CMC_CONNECTED"
                boolean r5 = r14.getBooleanExtra(r2, r3)
                java.lang.String r2 = "EXTRA_IS_CMC_CALL"
                boolean r6 = r14.getBooleanExtra(r2, r3)
                java.lang.String r14 = com.sec.internal.ims.imsservice.CallStateTracker.LOG_TAG
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "Received call event: "
                r2.append(r3)
                r2.append(r13)
                java.lang.String r3 = ", phoneId: "
                r2.append(r3)
                r2.append(r0)
                java.lang.String r3 = ", isCmcConnected: "
                r2.append(r3)
                r2.append(r5)
                java.lang.String r3 = ", isCmcCall: "
                r2.append(r3)
                r2.append(r6)
                java.lang.String r2 = r2.toString()
                android.util.Log.i(r14, r2)
                boolean r14 = com.sec.internal.helper.SimUtil.isValidSimSlot(r0)
                if (r14 != 0) goto L_0x0092
                java.lang.String r12 = com.sec.internal.ims.imsservice.CallStateTracker.LOG_TAG
                java.lang.String r13 = "Invalid phoneId - Ignore"
                android.util.Log.d(r12, r13)
                return
            L_0x0092:
                android.util.ArrayMap r14 = new android.util.ArrayMap
                r14.<init>()
                android.util.ArrayMap r2 = new android.util.ArrayMap
                r2.<init>()
                com.sec.internal.ims.imsservice.CallStateTracker r3 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.Map r3 = r3.mCallLists
                java.lang.Integer r8 = java.lang.Integer.valueOf(r0)
                boolean r3 = r3.containsKey(r8)
                if (r3 == 0) goto L_0x00cc
                com.sec.internal.ims.imsservice.CallStateTracker r14 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.Map r14 = r14.mCallLists
                java.lang.Integer r2 = java.lang.Integer.valueOf(r0)
                java.lang.Object r14 = r14.get(r2)
                java.util.Map r14 = (java.util.Map) r14
                com.sec.internal.ims.imsservice.CallStateTracker r2 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.Map r2 = r2.mCountLists
                java.lang.Integer r3 = java.lang.Integer.valueOf(r0)
                java.lang.Object r2 = r2.get(r3)
                java.util.Map r2 = (java.util.Map) r2
            L_0x00cc:
                r8 = r2
                r2 = 0
                if (r1 == 0) goto L_0x00f7
                java.lang.String r1 = r1.trim()
                java.lang.String r3 = com.sec.internal.ims.imsservice.CallStateTracker.LOG_TAG
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                r9.<init>()
                java.lang.String r10 = "Tel Number length "
                r9.append(r10)
                int r10 = r1.length()
                r9.append(r10)
                java.lang.String r9 = r9.toString()
                android.util.Log.i(r3, r9)
                boolean r3 = r1.isEmpty()
                if (r3 == 0) goto L_0x00f7
                r1 = r2
            L_0x00f7:
                com.sec.internal.ims.util.UriGeneratorFactory r3 = com.sec.internal.ims.util.UriGeneratorFactory.getInstance()
                com.sec.internal.ims.util.UriGenerator$URIServiceType r9 = com.sec.internal.ims.util.UriGenerator.URIServiceType.VOLTE_URI
                com.sec.internal.ims.util.UriGenerator r3 = r3.get((int) r0, (com.sec.internal.ims.util.UriGenerator.URIServiceType) r9)
                r9 = 1
                com.sec.ims.util.ImsUri r3 = r3.getNormalizedUri(r1, r9)
                if (r3 == 0) goto L_0x010d
                java.lang.String r2 = r3.getMsisdn()
                goto L_0x0113
            L_0x010d:
                if (r6 == 0) goto L_0x0113
                if (r1 == 0) goto L_0x0113
                r10 = r1
                goto L_0x0114
            L_0x0113:
                r10 = r2
            L_0x0114:
                if (r10 == 0) goto L_0x0165
                if (r13 != r9) goto L_0x0145
                com.sec.internal.ims.imsservice.CallStateTracker r13 = com.sec.internal.ims.imsservice.CallStateTracker.this
                android.content.Context r13 = r13.mContext
                com.sec.internal.helper.os.ITelephonyManager r13 = com.sec.internal.helper.os.TelephonyManagerWrapper.getInstance(r13)
                int r13 = r13.getCallState(r0)
                java.lang.Object r1 = r8.getOrDefault(r10, r7)
                java.lang.Integer r1 = (java.lang.Integer) r1
                int r1 = r1.intValue()
                int r1 = r1 - r9
                if (r1 < r9) goto L_0x013e
                if (r13 != 0) goto L_0x0136
                goto L_0x013e
            L_0x0136:
                java.lang.Integer r13 = java.lang.Integer.valueOf(r1)
                r8.put(r10, r13)
                goto L_0x0165
            L_0x013e:
                r14.remove(r10)
                r8.remove(r10)
                goto L_0x0165
            L_0x0145:
                com.sec.internal.ims.imsservice.CallStateTracker$Call r11 = new com.sec.internal.ims.imsservice.CallStateTracker$Call
                r1 = r11
                r2 = r13
                r3 = r10
                r1.<init>(r2, r3, r4, r5, r6)
                r14.put(r10, r11)
                r1 = 2
                if (r13 != r1) goto L_0x0165
                java.lang.Object r13 = r8.getOrDefault(r10, r7)
                java.lang.Integer r13 = (java.lang.Integer) r13
                int r13 = r13.intValue()
                int r13 = r13 + r9
                java.lang.Integer r13 = java.lang.Integer.valueOf(r13)
                r8.put(r10, r13)
            L_0x0165:
                com.sec.internal.ims.imsservice.CallStateTracker r13 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.Map r13 = r13.mCallLists
                java.lang.Integer r1 = java.lang.Integer.valueOf(r0)
                r13.put(r1, r14)
                com.sec.internal.ims.imsservice.CallStateTracker r13 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.Map r13 = r13.mCountLists
                java.lang.Integer r1 = java.lang.Integer.valueOf(r0)
                r13.put(r1, r8)
                java.util.ArrayList r13 = new java.util.ArrayList
                java.util.Collection r14 = r14.values()
                r13.<init>(r14)
                com.sec.internal.ims.imsservice.CallStateTracker r12 = com.sec.internal.ims.imsservice.CallStateTracker.this
                java.util.List r12 = r12.mListeners
                java.util.Iterator r12 = r12.iterator()
            L_0x0192:
                boolean r14 = r12.hasNext()
                if (r14 == 0) goto L_0x01a2
                java.lang.Object r14 = r12.next()
                com.sec.internal.ims.imsservice.CallStateTracker$Listener r14 = (com.sec.internal.ims.imsservice.CallStateTracker.Listener) r14
                r14.onCallStateChanged(r13, r0)
                goto L_0x0192
            L_0x01a2:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.CallStateTracker.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Map<Integer, Map<String, Integer>> mCountLists = new HashMap();
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public final List<Listener> mListeners = new ArrayList();
    /* access modifiers changed from: private */
    public final ServiceModuleManager mServiceModuleManager;

    public static abstract class Listener {
        /* access modifiers changed from: protected */
        public abstract void onCallStateChanged(List<ICall> list, int i);
    }

    static class Call implements ICall {
        public final boolean mIsCmcCall;
        public final boolean mIsCmcConnected;
        public final boolean mIsIncoming;
        public final String mNumber;
        public final int mState;

        Call(int i, String str, boolean z, boolean z2, boolean z3) {
            this.mState = i;
            this.mNumber = str;
            this.mIsIncoming = z;
            this.mIsCmcConnected = z2;
            this.mIsCmcCall = z3;
        }

        public String toString() {
            return "Call{mState=" + this.mState + ", mNumber='" + this.mNumber + '\'' + ", mIsIncoming=" + this.mIsIncoming + ", mIsCmcConnected=" + this.mIsCmcConnected + ", mIsCmcCall=" + this.mIsCmcCall + '}';
        }

        public boolean isConnected() {
            int i = this.mState;
            return i == 2 || i == 4;
        }

        public boolean isCmcConnected() {
            return this.mIsCmcConnected;
        }

        public boolean isCmcCall() {
            return this.mIsCmcCall;
        }

        public String getNumber() {
            return this.mNumber;
        }
    }

    public CallStateTracker(Context context, Handler handler, ServiceModuleManager serviceModuleManager) {
        this.mContext = context;
        this.mHandler = handler;
        this.mServiceModuleManager = serviceModuleManager;
    }

    public void initSequentially() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_CALL_STATE_CHANGED);
        this.mContext.registerReceiver(this.mCallStateReceiver, intentFilter, (String) null, this.mHandler);
        register(new Listener() {
            /* access modifiers changed from: protected */
            public void onCallStateChanged(List<ICall> list, int i) {
                CallStateTracker.this.mServiceModuleManager.notifyCallStateChanged(list, i);
            }
        });
    }

    public void register(Listener listener) {
        this.mListeners.add(listener);
    }

    public void unregister(Listener listener) {
        this.mListeners.remove(listener);
    }
}

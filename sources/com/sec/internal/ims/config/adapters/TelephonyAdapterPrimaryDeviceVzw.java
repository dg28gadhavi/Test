package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import com.verizon.loginclient.TokenLoginClient;
import java.nio.charset.Charset;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceVzw extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceVzw.class.getSimpleName();
    protected String mAppToken = null;
    protected TokenLoginClient mAppTokenClient = null;
    protected TokenLoginClient.ILoginClientReceiver mAppTokenClientReceiver = new TokenLoginClient.ILoginClientReceiver() {
        public void onTokenResult(TokenLoginClient.TokenQueryData tokenQueryData) {
            String r0 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r0, i, "onTokenResult: AppToken is received subId: " + tokenQueryData.subscriptionId);
            String r02 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.s(r02, i2, "onTokenResult: AppToken: " + tokenQueryData.token);
            TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw = TelephonyAdapterPrimaryDeviceVzw.this;
            telephonyAdapterPrimaryDeviceVzw.sendMessage(telephonyAdapterPrimaryDeviceVzw.obtainMessage(12, tokenQueryData.token));
        }

        public void onErrorResult(TokenLoginClient.ResultCode resultCode, Throwable th) {
            String r5 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r5, i, "onErrorResult: status: " + resultCode);
            TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw = TelephonyAdapterPrimaryDeviceVzw.this;
            telephonyAdapterPrimaryDeviceVzw.sendMessage(telephonyAdapterPrimaryDeviceVzw.obtainMessage(12, (Object) null));
        }
    };
    protected Semaphore mAppTokenSemaphore = new Semaphore(0);
    protected int mCurrentAppTokenPermits = 0;
    protected boolean mIsWaitingForAppToken = false;

    public TelephonyAdapterPrimaryDeviceVzw(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerPortSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiver();
    }

    /* access modifiers changed from: protected */
    public synchronized void registerAppTokenClient() throws IllegalStateException, IllegalArgumentException, InterruptedException {
        unregisterAppTokenClient();
        int subId = SimUtil.getSubId(this.mPhoneId);
        TokenLoginClient tokenLoginClient = new TokenLoginClient(this.mContext, this.mAppTokenClientReceiver, this.mLooper, Integer.valueOf(subId));
        this.mAppTokenClient = tokenLoginClient;
        tokenLoginClient.setTimeout(SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
        this.mAppTokenClient.setTargetSubscriptionId(Integer.valueOf(subId));
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "registerAppTokenClient: registered with current subId: " + subId);
    }

    /* access modifiers changed from: protected */
    public synchronized void unregisterAppTokenClient() {
        TokenLoginClient tokenLoginClient = this.mAppTokenClient;
        if (tokenLoginClient == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterAppTokenClient: already unregistered");
            return;
        }
        tokenLoginClient.cancelQuery();
        this.mAppTokenClient = null;
        IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterAppTokenClient: unregistered");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0035, code lost:
        r5 = r5.obj;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r5) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            int r1 = r4.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "message:"
            r2.append(r3)
            int r3 = r5.what
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            int r1 = r5.what
            if (r1 == 0) goto L_0x00a4
            r2 = 13
            r3 = 12
            if (r1 == r3) goto L_0x002b
            if (r1 == r2) goto L_0x002b
            super.handleMessage(r5)
            goto L_0x00ff
        L_0x002b:
            r4.removeMessages(r3)
            r4.removeMessages(r2)
            int r1 = r5.what
            if (r1 != r3) goto L_0x003c
            java.lang.Object r5 = r5.obj
            if (r5 == 0) goto L_0x003c
            java.lang.String r5 = (java.lang.String) r5
            goto L_0x003d
        L_0x003c:
            r5 = 0
        L_0x003d:
            r4.mAppToken = r5
            int r5 = r4.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "mAppToken: "
            r1.append(r2)
            java.lang.String r2 = r4.mAppToken
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r5, r1)
            r5 = 0
            r4.mIsWaitingForAppToken = r5
            java.util.concurrent.Semaphore r5 = r4.mAppTokenSemaphore     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r5 = r5.availablePermits()     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r5 = r5 + 1
            r4.mCurrentAppTokenPermits = r5     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r5 = r4.mPhoneId     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0086 }
            r1.<init>()     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.lang.String r2 = "release with mCurrentAppTokenPermits: "
            r1.append(r2)     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r2 = r4.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException -> 0x0086 }
            r1.append(r2)     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.lang.String r1 = r1.toString()     // Catch:{ IllegalArgumentException -> 0x0086 }
            com.sec.internal.log.IMSLog.i(r0, r5, r1)     // Catch:{ IllegalArgumentException -> 0x0086 }
            java.util.concurrent.Semaphore r5 = r4.mAppTokenSemaphore     // Catch:{ IllegalArgumentException -> 0x0086 }
            int r0 = r4.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException -> 0x0086 }
            r5.release(r0)     // Catch:{ IllegalArgumentException -> 0x0086 }
            goto L_0x00ff
        L_0x0086:
            r5 = move-exception
            java.lang.String r0 = LOG_TAG
            int r4 = r4.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "can't release with mCurrentAppTokenPermits: "
            r1.append(r2)
            java.lang.String r5 = r5.getMessage()
            r1.append(r5)
            java.lang.String r5 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r4, r5)
            goto L_0x00ff
        L_0x00a4:
            int r1 = r4.mPhoneId
            java.lang.String r2 = "receive port sms"
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            java.lang.Object r5 = r5.obj
            if (r5 == 0) goto L_0x00f8
            java.lang.String r5 = (java.lang.String) r5
            java.lang.String r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST
            boolean r5 = r5.contains(r1)
            if (r5 == 0) goto L_0x00f0
            int r5 = r4.mPhoneId
            java.lang.String r1 = "force configuration request"
            com.sec.internal.log.IMSLog.i(r0, r5, r1)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            int r0 = r4.mPhoneId
            r5.append(r0)
            java.lang.String r0 = ",REVPO"
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            r0 = 319029249(0x13040001, float:1.6660744E-27)
            com.sec.internal.log.IMSLog.c(r0, r5)
            com.sec.internal.interfaces.ims.config.IConfigModule r5 = r4.mModule
            android.os.Handler r5 = r5.getHandler()
            int r0 = r4.mPhoneId
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r1 = 21
            android.os.Message r4 = r4.obtainMessage(r1, r0)
            r5.sendMessage(r4)
            goto L_0x00ff
        L_0x00f0:
            int r4 = r4.mPhoneId
            java.lang.String r5 = "invalid port sms"
            com.sec.internal.log.IMSLog.i(r0, r4, r5)
            goto L_0x00ff
        L_0x00f8:
            int r4 = r4.mPhoneId
            java.lang.String r5 = "invalid sms configuration request"
            com.sec.internal.log.IMSLog.i(r0, r4, r5)
        L_0x00ff:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.handleMessage(android.os.Message):void");
    }

    public String getOtp() {
        return this.mState.getOtp();
    }

    public void onADSChanged() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "onADSChanged");
        if (this.mIsWaitingForAppToken) {
            IMSLog.i(str, this.mPhoneId, "onADSChanged: send apptoken timeout message");
            removeMessages(13);
            sendEmptyMessage(13);
        }
    }

    public void cleanup() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "cleanup");
        if (this.mIsWaitingForAppToken) {
            IMSLog.i(str, this.mPhoneId, "cleanup: send apptoken timeout message");
            removeMessages(13);
            sendEmptyMessage(13);
        }
        super.cleanup();
    }

    /* access modifiers changed from: protected */
    public void getState(String str) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "getState: change to " + str);
        if (TelephonyAdapterState.READY_STATE.equals(str)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(str)) {
            this.mState = new AbsentState();
        } else {
            super.getState(str);
        }
    }

    protected class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        public String getOtp() {
            return null;
        }

        protected ReadyState() {
            super();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x00b0, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            com.sec.internal.log.IMSLog.i(com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.m443$$Nest$sfgetLOG_TAG(), r5.this$0.mPhoneId, "getAppToken: cannot get apptoken");
            r6.printStackTrace();
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x00b0 A[ExcHandler: IllegalArgumentException | IllegalStateException | InterruptedException (r6v3 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x0027] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String getAppToken(boolean r6) {
            /*
                r5 = this;
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                int r1 = r1.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "getAppToken: isRetry: "
                r2.append(r3)
                r2.append(r6)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r0, r1, r2)
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                r1 = 0
                r0.mAppToken = r1
                r1 = 1
                r0.mIsWaitingForAppToken = r1
                r2 = 0
                r0.mCurrentAppTokenPermits = r2
                r0.registerAppTokenClient()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                if (r6 == 0) goto L_0x0052
                java.lang.String r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r0 = r0.mPhoneId     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r3 = "getAppToken: invalidate apptoken"
                com.sec.internal.log.IMSLog.i(r6, r0, r3)     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.verizon.loginclient.TokenLoginClient r6 = r6.mAppTokenClient     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r6.invalidateToken()     // Catch:{ SecurityException -> 0x0041, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0, IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                goto L_0x0052
            L_0x0041:
                r6 = move-exception
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r3 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r3 = r3.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r4 = "getAppToken: cannot invalidate apptoken"
                com.sec.internal.log.IMSLog.i(r0, r3, r4)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r6.printStackTrace()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
            L_0x0052:
                java.lang.String r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r0 = r0.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r3 = "getAppToken: query apptoken"
                com.sec.internal.log.IMSLog.i(r6, r0, r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.verizon.loginclient.TokenLoginClient r6 = r6.mAppTokenClient     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r6.queryTokenAsync()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r0 = 13
                r6.removeMessages(r0)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                android.os.Message r0 = r6.obtainMessage(r0)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r3 = 62000(0xf230, double:3.0632E-319)
                r6.sendMessageDelayed(r0, r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.util.concurrent.Semaphore r0 = r6.mAppTokenSemaphore     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r0 = r0.availablePermits()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r0 = r0 + r1
                r6.mCurrentAppTokenPermits = r0     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r0 = r0.mPhoneId     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r1.<init>()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r3 = "getAppToken: acquire with mCurrentAppTokenPermits: "
                r1.append(r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r3 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r3 = r3.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r1.append(r3)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.lang.String r1 = r1.toString()     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.log.IMSLog.i(r6, r0, r1)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                java.util.concurrent.Semaphore r0 = r6.mAppTokenSemaphore     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                int r6 = r6.mCurrentAppTokenPermits     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                r0.acquire(r6)     // Catch:{ IllegalArgumentException | IllegalStateException | InterruptedException -> 0x00b0 }
                goto L_0x00c1
            L_0x00ae:
                r6 = move-exception
                goto L_0x00cf
            L_0x00b0:
                r6 = move-exception
                java.lang.String r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.LOG_TAG     // Catch:{ all -> 0x00ae }
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r1 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this     // Catch:{ all -> 0x00ae }
                int r1 = r1.mPhoneId     // Catch:{ all -> 0x00ae }
                java.lang.String r3 = "getAppToken: cannot get apptoken"
                com.sec.internal.log.IMSLog.i(r0, r1, r3)     // Catch:{ all -> 0x00ae }
                r6.printStackTrace()     // Catch:{ all -> 0x00ae }
            L_0x00c1:
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                r6.unregisterAppTokenClient()
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r6 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                r6.mIsWaitingForAppToken = r2
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r5 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                java.lang.String r5 = r5.mAppToken
                return r5
            L_0x00cf:
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r0 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                r0.unregisterAppTokenClient()
                com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r5 = com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.this
                r5.mIsWaitingForAppToken = r2
                throw r6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw.ReadyState.getAppToken(boolean):java.lang.String");
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            String r0 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r0, i, "registerAutoConfigurationListener: listener: " + iAutoConfigurationListener);
            if (iAutoConfigurationListener != null) {
                synchronized (TelephonyAdapterPrimaryDeviceVzw.this.mLock) {
                    TelephonyAdapterPrimaryDeviceVzw.this.mAutoConfigurationListener.register(iAutoConfigurationListener);
                    IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "registerAutoConfigurationListener: registered");
                    if (!TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.isEmpty()) {
                        try {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "registerAutoConfigurationListener: need the postponed notification");
                            for (Integer intValue : TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.keySet()) {
                                int intValue2 = intValue.intValue();
                                notifyAutoConfigurationListener(intValue2, TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.get(Integer.valueOf(intValue2)).booleanValue());
                            }
                        } catch (NullPointerException e) {
                            String r1 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                            int i2 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                            IMSLog.i(r1, i2, "registerAutoConfigurationListener: can't notify for the postponed notification: " + e.getMessage());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            String r0 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r0, i, "unregisterAutoConfigurationListener: listener: " + iAutoConfigurationListener);
            if (iAutoConfigurationListener != null) {
                synchronized (TelephonyAdapterPrimaryDeviceVzw.this.mLock) {
                    TelephonyAdapterPrimaryDeviceVzw.this.mAutoConfigurationListener.unregister(iAutoConfigurationListener);
                    IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "unregisterAutoConfigurationListener: unregisterd");
                    TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int i, boolean z) {
            String r0 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r0, i2, "notifyAutoConfigurationListener: type: " + i + " result: " + z);
            if (i == 52) {
                synchronized (TelephonyAdapterPrimaryDeviceVzw.this.mLock) {
                    try {
                        int beginBroadcast = TelephonyAdapterPrimaryDeviceVzw.this.mAutoConfigurationListener.beginBroadcast();
                        String r2 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                        int i3 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                        IMSLog.i(r2, i3, "notifyAutoConfigurationListener: listener length: " + beginBroadcast);
                        if (beginBroadcast == 0) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "notifyAutoConfigurationListener: need to notify later for the postponed notification");
                            TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.clear();
                            TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.put(Integer.valueOf(i), Boolean.valueOf(z));
                        }
                        IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "notifyAutoConfigurationListener: onAutoConfigurationCompleted");
                        for (int i4 = 0; i4 < beginBroadcast; i4++) {
                            TelephonyAdapterPrimaryDeviceVzw.this.mAutoConfigurationListener.getBroadcastItem(i4).onAutoConfigurationCompleted(z);
                            TelephonyAdapterPrimaryDeviceVzw.this.mPostponedNotification.clear();
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String r8 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                        int i5 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                        IMSLog.i(r8, i5, "notifyAutoConfigurationListener: can't notify: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceVzw.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String r82 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                        int i6 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                        IMSLog.i(r82, i6, "notifyAutoConfigurationListener: can't finishBroadcast: " + e2.getMessage());
                    }
                }
                return;
            }
            return;
        }
    }

    protected class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        protected AbsentState() {
            super();
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "getOtp method can't run in absentState");
            return null;
        }

        public String getIdentityByPhoneId(int i) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "getIdentityByPhoneId method can't run in absentState");
            return null;
        }

        public String getDeviceId(int i) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId, "getDeviceId method can't run in absentState");
            return null;
        }
    }

    private class PortSmsReceiver extends TelephonyAdapterPrimaryDeviceBase.PortSmsReceiverBase {
        private PortSmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage smsMessage;
            Object obj;
            String stringExtra = intent.getStringExtra("format");
            String r1 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
            IMSLog.i(r1, i, "readMessageFromSMSIntent: format: " + stringExtra);
            if (com.sec.internal.constants.ims.servicemodules.sms.SmsMessage.FORMAT_3GPP2.equals(stringExtra)) {
                try {
                    Object[] objArr = (Object[]) intent.getSerializableExtra("pdus");
                    if (objArr != null && (obj = objArr[0]) != null) {
                        String str = new String((byte[]) obj, Charset.forName("UTF-8"));
                        String r8 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                        int i2 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                        IMSLog.d(r8, i2, "readMessageFromSMSIntent: message: " + str);
                        IMSLog.c(LogClass.TAPDVM_MSG, "" + TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId);
                        TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw = TelephonyAdapterPrimaryDeviceVzw.this;
                        telephonyAdapterPrimaryDeviceVzw.sendMessage(telephonyAdapterPrimaryDeviceVzw.obtainMessage(0, str));
                    }
                } catch (ClassCastException e) {
                    String r0 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                    IMSLog.i(r0, i3, "readMessageFromSMSIntent: ClassCastException: cannot get message" + e.getMessage());
                }
            } else {
                SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                    String displayMessageBody = smsMessage.getDisplayMessageBody();
                    if (displayMessageBody == null) {
                        displayMessageBody = new String(smsMessage.getUserData(), Charset.forName("UTF-8"));
                    }
                    String r82 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                    int i4 = TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId;
                    IMSLog.d(r82, i4, "readMessageFromSMSIntent: message: " + displayMessageBody);
                    IMSLog.c(LogClass.TAPDVM_MSG, "" + TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId);
                    TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw2 = TelephonyAdapterPrimaryDeviceVzw.this;
                    telephonyAdapterPrimaryDeviceVzw2.sendMessage(telephonyAdapterPrimaryDeviceVzw2.obtainMessage(0, displayMessageBody));
                }
            }
        }
    }
}

package com.sec.internal.imsphone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.ims.ImsService;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class SecImsService extends ImsService {
    public static final String LOG_TAG = "SecImsService";
    private IBinder mBinder = new LocalBinder();
    private Context mContext;
    private SimpleEventLog mEventLog;
    Map<Integer, ImsConfigImpl> mImsConfigImpls = new ConcurrentHashMap();
    Map<Integer, ImsRegistrationImpl> mImsRegistrationImpls = new ConcurrentHashMap();
    Map<Integer, MmTelFeatureImpl> mMmTelFeatureImpls = new ConcurrentHashMap();
    Map<Integer, RcsFeatureImpl> mRcsFeatureImpls = new ConcurrentHashMap();
    Map<Integer, SipTransportImpl> mSipTransports = new ConcurrentHashMap();

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public long getImsServiceCapabilities() {
        return 2;
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [android.content.Context, com.sec.internal.imsphone.SecImsService] */
    public void onCreate() {
        IMSLog.i(LOG_TAG, "onCreate");
        ImsServiceStub.getInstance();
        this.mContext = this;
        this.mEventLog = new SimpleEventLog(this, LOG_TAG, 500);
    }

    public ImsFeatureConfiguration querySupportedImsFeatures() {
        IMSLog.i(LOG_TAG, "querySupportedImsFeatures");
        return new ImsFeatureConfiguration.Builder().addFeature(0, 0).addFeature(0, 1).addFeature(0, 2).addFeature(1, 0).addFeature(1, 1).addFeature(1, 2).build();
    }

    public MmTelFeature createMmTelFeatureForSubscription(int i, int i2) {
        IMSLog.i(LOG_TAG, "createMmTelFeatureForSubscription: slotId=" + i + ",subscriptionId=" + i2);
        if (this.mMmTelFeatureImpls.containsKey(Integer.valueOf(i))) {
            return this.mMmTelFeatureImpls.get(Integer.valueOf(i));
        }
        MmTelFeatureImpl mmTelFeatureImpl = new MmTelFeatureImpl(this.mContext, i, Executors.newSingleThreadExecutor());
        this.mMmTelFeatureImpls.put(Integer.valueOf(i), mmTelFeatureImpl);
        return mmTelFeatureImpl;
    }

    public MmTelFeature createEmergencyOnlyMmTelFeature(int i) {
        IMSLog.i(LOG_TAG, "createMmTelFeatureForSubscription: slotId=" + i);
        if (this.mMmTelFeatureImpls.containsKey(Integer.valueOf(i))) {
            return this.mMmTelFeatureImpls.get(Integer.valueOf(i));
        }
        MmTelFeatureImpl mmTelFeatureImpl = new MmTelFeatureImpl(this.mContext, i, Executors.newSingleThreadExecutor());
        this.mMmTelFeatureImpls.put(Integer.valueOf(i), mmTelFeatureImpl);
        return mmTelFeatureImpl;
    }

    public MmTelFeatureImpl getMmTelFeatureImpl(int i) {
        return createMmTelFeatureForSubscription(i, -1);
    }

    public RcsFeature createRcsFeatureForSubscription(int i, int i2) {
        IMSLog.i(LOG_TAG, "createRcsFeatureForSubscription: slotId=" + i + ",subscriptionId=" + i2);
        if (this.mRcsFeatureImpls.containsKey(Integer.valueOf(i))) {
            return this.mRcsFeatureImpls.get(Integer.valueOf(i));
        }
        RcsFeatureImpl rcsFeatureImpl = new RcsFeatureImpl(i, Executors.newSingleThreadExecutor());
        this.mRcsFeatureImpls.put(Integer.valueOf(i), rcsFeatureImpl);
        return rcsFeatureImpl;
    }

    public RcsFeatureImpl getRcsFeatureImpl(int i) {
        return createRcsFeatureForSubscription(i, -1);
    }

    public ImsRegistrationImplBase getRegistrationForSubscription(int i, int i2) {
        IMSLog.i(LOG_TAG, "getRegistrationForSubscription: slotId=" + i + ",subscriptionId=" + i2);
        if (this.mImsRegistrationImpls.containsKey(Integer.valueOf(i))) {
            return this.mImsRegistrationImpls.get(Integer.valueOf(i));
        }
        ImsRegistrationImpl imsRegistrationImpl = new ImsRegistrationImpl(i, Executors.newSingleThreadExecutor());
        this.mImsRegistrationImpls.put(Integer.valueOf(i), imsRegistrationImpl);
        return imsRegistrationImpl;
    }

    public ImsRegistrationImpl getImsRegistrationImpl(int i) {
        return getRegistrationForSubscription(i, -1);
    }

    public ImsConfigImplBase getConfigForSubscription(int i, int i2) {
        IMSLog.i(LOG_TAG, "getConfigForSubscription: slotId=" + i + ",subscriptionId=" + i2);
        if (this.mImsConfigImpls.containsKey(Integer.valueOf(i))) {
            return this.mImsConfigImpls.get(Integer.valueOf(i));
        }
        ImsConfigImpl imsConfigImpl = new ImsConfigImpl(i, this.mContext);
        this.mImsConfigImpls.put(Integer.valueOf(i), imsConfigImpl);
        return imsConfigImpl;
    }

    public ImsConfigImpl getImsConfigImpl(int i) {
        return getConfigForSubscription(i, -1);
    }

    public SipTransportImpl getSipTransport(int i) {
        IMSLog.i(LOG_TAG, i, "getSipTransport");
        if (this.mSipTransports.containsKey(Integer.valueOf(i))) {
            return this.mSipTransports.get(Integer.valueOf(i));
        }
        SipTransportImpl sipTransportImpl = new SipTransportImpl(i, Executors.newSingleThreadExecutor(), this.mEventLog);
        this.mSipTransports.put(Integer.valueOf(i), sipTransportImpl);
        return sipTransportImpl;
    }

    public IBinder onBind(Intent intent) {
        ComponentName component = intent.getComponent();
        IMSLog.i(LOG_TAG, "onBind: action [" + intent.getAction() + "], component [" + component + "]");
        if (intent.getAction() == null) {
            return this.mBinder;
        }
        return SecImsService.super.onBind(intent);
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public SecImsService getService() {
            return SecImsService.this;
        }
    }
}

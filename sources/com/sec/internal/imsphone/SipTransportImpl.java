package com.sec.internal.imsphone;

import android.telephony.ims.DelegateMessageCallback;
import android.telephony.ims.DelegateRequest;
import android.telephony.ims.DelegateStateCallback;
import android.telephony.ims.FeatureTagState;
import android.telephony.ims.stub.SipDelegate;
import android.telephony.ims.stub.SipTransportImplBase;
import android.text.TextUtils;
import android.util.ArraySet;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.RegistrationTracker;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class SipTransportImpl extends SipTransportImplBase {
    private static final String LOG_TAG = "SipTransportImpl";
    final Set<String> mAllocatedTags = new ArraySet();
    final List<SipDelegateImpl> mDelegates = new ArrayList();
    private SimpleEventLog mEventLog;
    private Executor mExecutor;
    private int mPhoneId;
    RegistrationTracker mRegistrationTracker = new RegistrationTracker();

    public SipTransportImpl(int i, Executor executor, SimpleEventLog simpleEventLog) {
        super(executor);
        this.mExecutor = executor;
        this.mEventLog = simpleEventLog;
        this.mPhoneId = i;
    }

    public void createSipDelegate(int i, DelegateRequest delegateRequest, DelegateStateCallback delegateStateCallback, DelegateMessageCallback delegateMessageCallback) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog.logAndAdd(i2, "addSipDelegate: request " + delegateRequest);
        SipDelegateImpl sipDelegateImpl = new SipDelegateImpl(this.mPhoneId, this.mExecutor, delegateStateCallback, delegateMessageCallback, ImsRegistry.getRawSipSender());
        Optional.ofNullable(this.mRegistrationTracker.getRegisteredToken(allocateDelegate(delegateRequest, sipDelegateImpl))).ifPresent(new SipTransportImpl$$ExternalSyntheticLambda10(this, sipDelegateImpl));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$createSipDelegate$0(SipDelegateImpl sipDelegateImpl, RegistrationTracker.RegisteredToken registeredToken) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "Already registered. notify config and registration");
        sipDelegateImpl.notifyAlreadyRegistered(registeredToken);
    }

    private Set<String> allocateDelegate(DelegateRequest delegateRequest, SipDelegateImpl sipDelegateImpl) {
        ArraySet arraySet = new ArraySet();
        ArraySet arraySet2 = new ArraySet();
        synchronized (this.mDelegates) {
            this.mDelegates.add(sipDelegateImpl);
            delegateRequest.getFeatureTags().forEach(new SipTransportImpl$$ExternalSyntheticLambda4(this, arraySet, arraySet2));
            this.mAllocatedTags.addAll(arraySet);
            sipDelegateImpl.notifyCreated(arraySet, arraySet2);
        }
        return arraySet;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$allocateDelegate$1(Set set, Set set2, String str) {
        if (!this.mAllocatedTags.contains(str)) {
            set.add(str);
        } else {
            set2.add(new FeatureTagState(str, 1));
        }
    }

    public void destroySipDelegate(SipDelegate sipDelegate, int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog.logAndAdd(i2, "removeSipDelegate: reason code [" + i + "]");
        SipDelegateImpl sipDelegateImpl = (SipDelegateImpl) this.mDelegates.stream().filter(new SipTransportImpl$$ExternalSyntheticLambda12(sipDelegate)).findFirst().orElse((Object) null);
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "removeSipDelegate:" + sipDelegateImpl);
        if (sipDelegateImpl != null) {
            synchronized (this.mDelegates) {
                this.mAllocatedTags.removeAll(sipDelegateImpl.getFeatureTags());
                sipDelegateImpl.notifyDestroy(i);
                this.mDelegates.remove(sipDelegateImpl);
            }
        }
    }

    public boolean hasSipDelegate() {
        boolean z;
        synchronized (this.mDelegates) {
            z = !this.mDelegates.isEmpty();
        }
        return z;
    }

    public Set<String> getAllocatedFeatureTags() {
        Set<String> set;
        synchronized (this.mDelegates) {
            set = this.mAllocatedTags;
        }
        return set;
    }

    public List<String> getServiceList() {
        return (List) getAllocatedFeatureTags().stream().map(new SipTransportImpl$$ExternalSyntheticLambda9()).collect(Collectors.toList());
    }

    public void notifySipMessage(SipMsg sipMsg) {
        this.mExecutor.execute(new SipTransportImpl$$ExternalSyntheticLambda0(this, sipMsg));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifySipMessage$5(SipMsg sipMsg) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "notifySipMessage: " + sipMsg);
        if (sipMsg.isRegister()) {
            this.mRegistrationTracker.onSipRegisterTransaction(sipMsg);
        } else {
            this.mDelegates.stream().filter(new SipTransportImpl$$ExternalSyntheticLambda5(sipMsg)).forEach(new SipTransportImpl$$ExternalSyntheticLambda6(sipMsg));
        }
    }

    public void onRegistrationChanged(ImsRegistration imsRegistration, boolean z) {
        this.mExecutor.execute(new SipTransportImpl$$ExternalSyntheticLambda1(this, imsRegistration, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRegistrationChanged$8(ImsRegistration imsRegistration, boolean z) {
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onRegistrationChanged: " + imsProfile.getName() + ": registered: " + z);
        this.mRegistrationTracker.onRegistrationDone(imsRegistration, z);
        this.mDelegates.stream().filter(new SipTransportImpl$$ExternalSyntheticLambda21(imsProfile)).forEach(new SipTransportImpl$$ExternalSyntheticLambda22(this, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRegistrationChanged$7(boolean z, SipDelegateImpl sipDelegateImpl) {
        if (z) {
            Set<String> featureTags = this.mRegistrationTracker.getFeatureTags();
            IMSLog.i(LOG_TAG, "onRegistrationChanged: registered tags: " + featureTags);
            sipDelegateImpl.notifyConfigurationChanged(this.mRegistrationTracker.getRegisteredDelegateConfig());
            sipDelegateImpl.notifyRegistrationChanged(featureTags);
            return;
        }
        sipDelegateImpl.notifyRegistrationChanged(Collections.emptySet());
    }

    public void updateAdhocProfile(ImsProfile imsProfile, boolean z) {
        if (imsProfile.isSamsungMdmnEnabled()) {
            String domain = imsProfile.getDomain();
            if (TextUtils.isEmpty(domain)) {
                domain = (String) imsProfile.getImpuList().stream().filter(new SipTransportImpl$$ExternalSyntheticLambda16()).filter(new SipTransportImpl$$ExternalSyntheticLambda17()).filter(new SipTransportImpl$$ExternalSyntheticLambda18()).map(new SipTransportImpl$$ExternalSyntheticLambda19()).findFirst().orElse("");
            }
            if (TextUtils.isEmpty(domain)) {
                int i = this.mPhoneId;
                IMSLog.e(LOG_TAG, i, "updateAdhocProfile: No domain for " + imsProfile.getName());
                return;
            }
            this.mRegistrationTracker.updateAdhocDomain(z, domain);
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$updateAdhocProfile$9(String str) {
        return !str.isEmpty();
    }

    public Set<String> getRegisteredFeatureTags() {
        return this.mRegistrationTracker.getFeatureTags();
    }

    public void notifyDeRegistering(ImsRegistration imsRegistration) {
        this.mExecutor.execute(new SipTransportImpl$$ExternalSyntheticLambda15(this, imsRegistration));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyDeRegistering$15(ImsRegistration imsRegistration) {
        int phoneId = imsRegistration.getPhoneId();
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        IMSLog.i(LOG_TAG, phoneId, "notifyDeRegistering: " + imsProfile.getName());
        this.mDelegates.stream().filter(new SipTransportImpl$$ExternalSyntheticLambda13(imsProfile)).forEach(new SipTransportImpl$$ExternalSyntheticLambda14(imsRegistration));
    }

    public void onUpdateRegistrationTimeout() {
        this.mExecutor.execute(new SipTransportImpl$$ExternalSyntheticLambda3(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onUpdateRegistrationTimeout$17() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onUpdateRegistrationTimeout");
        this.mDelegates.forEach(new SipTransportImpl$$ExternalSyntheticLambda20(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onUpdateRegistrationTimeout$16(SipDelegateImpl sipDelegateImpl) {
        sipDelegateImpl.notifyRegistrationChanged(this.mRegistrationTracker.getFeatureTags());
    }

    public void onPaniUpdated(String str, String str2) {
        this.mExecutor.execute(new SipTransportImpl$$ExternalSyntheticLambda11(this, str, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPaniUpdated$21(String str, String str2) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onPaniUpdated");
        Optional.ofNullable(this.mRegistrationTracker.getRegisteredDelegateConfig()).filter(new SipTransportImpl$$ExternalSyntheticLambda7(str, str2)).ifPresent(new SipTransportImpl$$ExternalSyntheticLambda8(this, str, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPaniUpdated$20(String str, String str2, SipDelegateConfig sipDelegateConfig) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onPaniUpdated: notifySipDelegateConfig");
        SipDelegateConfig build = sipDelegateConfig.getBuilder().setSipPaniHeader(str).setSipPlaniHeader(str2).build();
        this.mRegistrationTracker.setSipDelegateConfig(build);
        this.mDelegates.forEach(new SipTransportImpl$$ExternalSyntheticLambda2(build));
    }
}

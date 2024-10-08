package com.sec.internal.imsphone;

import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Pair;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$$ExternalSyntheticLambda0;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

class RegistrationTracker {
    static final String LOG_TAG = "RegTracker";
    private List<String> mAdhocDomains = new ArrayList();
    String mCallId = "";
    State mCurrentState = State.DE_REGISTERED;
    SipDelegateConfig mDelegateConfig = new SipDelegateConfig();
    final Set<String> mFeatureTags = new ArraySet();

    enum State {
        DE_REGISTERED,
        REGISTERING,
        AUTHORIZING,
        REGISTERED,
        RE_REGISTERING,
        DE_REGISTERING
    }

    public State getState() {
        State state;
        synchronized (this) {
            state = this.mCurrentState;
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public void setCallId(String str) {
        if (!TextUtils.equals(this.mCallId, str)) {
            State state = this.mCurrentState;
            State state2 = State.DE_REGISTERED;
            if (state != state2) {
                IMSLog.i(LOG_TAG, String.format(Locale.US, "setCallId: Call-Id has changed in [%s] state! Changed to DE_REGISTERED", new Object[]{state}));
                this.mCurrentState = state2;
            }
            IMSLog.i(LOG_TAG, "setCallId: " + str.split("@")[0]);
            this.mCallId = str;
        }
    }

    public Set<String> getFeatureTags() {
        ArraySet arraySet;
        synchronized (this.mFeatureTags) {
            arraySet = new ArraySet(this.mFeatureTags);
        }
        return arraySet;
    }

    public void setSipDelegateConfig(SipDelegateConfig sipDelegateConfig) {
        this.mDelegateConfig = sipDelegateConfig;
    }

    public RegisteredToken getRegisteredToken(Set<String> set) {
        Set<String> featureTags = getFeatureTags();
        if (getState() != State.REGISTERED || !featureTags.removeAll(set)) {
            return null;
        }
        return new RegisteredToken(getRegisteredDelegateConfig(), featureTags);
    }

    public void onSipRegisterTransaction(SipMsg sipMsg) {
        String str = sipMsg.getCallIds()[0];
        if (!isIrrelevantRegister(sipMsg)) {
            setCallId(str);
            updateState(sipMsg);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isIrrelevantRegister(SipMsg sipMsg) {
        return sipMsg.isOutGoing() && (sipMsg.isContactUriHasSos() || isAdhocProfileRegister(sipMsg) || sipMsg.getFeatureTags().isEmpty());
    }

    public void updateAdhocDomain(boolean z, String str) {
        if (z) {
            this.mAdhocDomains.add(str);
        } else {
            this.mAdhocDomains.remove(str);
        }
    }

    private boolean isAdhocProfileRegister(SipMsg sipMsg) {
        String requestLineUri = sipMsg.getRequestLineUri();
        IMSLog.i(LOG_TAG, String.format("isAdhocProfileRegister: domain from StartLine is [%s]", new Object[]{requestLineUri}));
        return this.mAdhocDomains.contains(requestLineUri);
    }

    /* access modifiers changed from: package-private */
    public void updateState(boolean z) {
        if (z) {
            synchronized (this) {
                this.mCurrentState = State.REGISTERED;
            }
            return;
        }
        synchronized (this) {
            this.mCurrentState = State.DE_REGISTERED;
        }
        synchronized (this.mFeatureTags) {
            this.mFeatureTags.clear();
        }
        this.mCallId = "";
    }

    /* renamed from: com.sec.internal.imsphone.RegistrationTracker$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.imsphone.RegistrationTracker$State[] r0 = com.sec.internal.imsphone.RegistrationTracker.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State = r0
                com.sec.internal.imsphone.RegistrationTracker$State r1 = com.sec.internal.imsphone.RegistrationTracker.State.DE_REGISTERED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.imsphone.RegistrationTracker$State r1 = com.sec.internal.imsphone.RegistrationTracker.State.AUTHORIZING     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.imsphone.RegistrationTracker$State r1 = com.sec.internal.imsphone.RegistrationTracker.State.REGISTERING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.imsphone.RegistrationTracker$State r1 = com.sec.internal.imsphone.RegistrationTracker.State.RE_REGISTERING     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.imsphone.RegistrationTracker$State r1 = com.sec.internal.imsphone.RegistrationTracker.State.REGISTERED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.RegistrationTracker.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateState(SipMsg sipMsg) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$imsphone$RegistrationTracker$State[this.mCurrentState.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (!(i == 3 || i == 4)) {
                    if (i == 5 && sipMsg.isOutGoing()) {
                        this.mCurrentState = sipMsg.getExpire() == 0 ? State.DE_REGISTERING : State.RE_REGISTERING;
                        return;
                    }
                    return;
                }
            } else if (sipMsg.isOutGoing() && sipMsg.getExpire() != 0) {
                Pair<String, Integer> viaHostPort = sipMsg.getViaHostPort();
                this.mDelegateConfig.getBuilder().setHomeDomain(sipMsg.getStartLine().asRequestLine().getUri().replace("sip:", "")).setTransport(sipMsg.getViaTransport()).setLocalAddress((String) viaHostPort.first, ((Integer) viaHostPort.second).intValue()).setSipContactUserParameter(sipMsg.getContactUser()).setImei(sipMsg.getContactImei()).setSipUserAgentHeader(sipMsg.getUserAgent()).setSecurityVerifyHeader(String.join(",", sipMsg.getSecurityVerify())).setSipPaniHeader(sipMsg.getPAccessNetworkInfo()).setSipPlaniHeader(sipMsg.getPLastAccessNetworkInfo());
            }
            int code = sipMsg.getStartLine().asStatusLine().getCode();
            if (code == 401 || code == 407) {
                this.mDelegateConfig.getBuilder().setSipAuthenticationHeader(sipMsg.getAuthenticate()).setSipAuthenticationNonce(sipMsg.getAuthenticateNonce());
                this.mCurrentState = State.AUTHORIZING;
            } else if (code == 200) {
                synchronized (this.mFeatureTags) {
                    this.mFeatureTags.clear();
                    this.mFeatureTags.addAll(sipMsg.getFeatureTags());
                }
                this.mDelegateConfig.getBuilder().setSipServiceRouteHeader(String.join(",", sipMsg.getServiceRoutes())).setSipAssociatedUriHeader(String.join(",", sipMsg.getPAssociatedUris()));
            }
        } else {
            if (!sipMsg.isOutGoing()) {
                IMSLog.e(LOG_TAG, "DE_REGISTERED: updateState: Unexpected SIP [" + sipMsg + "]");
            }
            this.mCurrentState = State.REGISTERING;
        }
    }

    public SipDelegateConfig onRegistrationDone(ImsRegistration imsRegistration) {
        SipDelegateConfig build = this.mDelegateConfig.getBuilder().setPcscfAddress(imsRegistration.getPcscf()).setPublicUserIdentifier((String) Optional.ofNullable(imsRegistration.getPreferredImpu()).map(new RegistrationTracker$$ExternalSyntheticLambda0()).map(new RcsScheduler$$ExternalSyntheticLambda0()).orElse("")).setPrivateUserIdentifier(imsRegistration.getImpi()).setMaxUdpPayloadSizeBytes(imsRegistration.getImsProfile().getMssSize()).build();
        this.mDelegateConfig = build;
        return build;
    }

    public void onRegistrationDone(ImsRegistration imsRegistration, boolean z) {
        onRegistrationDone(imsRegistration);
        updateState(z);
    }

    public SipDelegateConfig getRegisteredDelegateConfig() {
        if (this.mCurrentState == State.REGISTERED) {
            return this.mDelegateConfig;
        }
        return null;
    }

    public String toString() {
        return "RegTracker(state: " + this.mCurrentState + ") callId: " + this.mCallId.split("@")[0];
    }

    public static class RegisteredToken {
        private SipDelegateConfig mDelegateConfig;
        private Set<String> mRegisteredFeatureTags;

        public RegisteredToken(SipDelegateConfig sipDelegateConfig, Set<String> set) {
            this.mDelegateConfig = sipDelegateConfig;
            this.mRegisteredFeatureTags = set;
        }

        public SipDelegateConfig getDelegateConfig() {
            return this.mDelegateConfig;
        }

        public Set<String> getRegisteredFeatureTags() {
            return this.mRegisteredFeatureTags;
        }
    }
}

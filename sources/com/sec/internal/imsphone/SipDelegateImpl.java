package com.sec.internal.imsphone;

import android.os.Message;
import android.telephony.ims.DelegateMessageCallback;
import android.telephony.ims.DelegateRegistrationState;
import android.telephony.ims.DelegateStateCallback;
import android.telephony.ims.FeatureTagState;
import android.telephony.ims.SipDelegateConfiguration;
import android.telephony.ims.SipMessage;
import android.telephony.ims.stub.SipDelegate;
import android.util.ArraySet;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.imsphone.RegistrationTracker;
import com.sec.internal.interfaces.ims.core.IRawSipSender;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

public class SipDelegateImpl implements SipDelegate {
    private static final String LOG_TAG = "SipDelegateImpl";
    private static final Map<Integer, Integer> REASON_MAP;
    Set<String> mCallIds = new ArraySet();
    private final Executor mExecutor;
    Set<String> mFeatureTags = new ArraySet();
    SipDelegateConfiguration mLatestDelegateConfig;
    private final DelegateMessageCallback mMessageCallback;
    private final int mPhoneId;
    private final IRawSipSender mRawSipSender;
    private final DelegateStateCallback mStateCallback;

    static {
        HashMap hashMap = new HashMap();
        REASON_MAP = hashMap;
        hashMap.put(23, 7);
    }

    public SipDelegateImpl(int i, Executor executor, DelegateStateCallback delegateStateCallback, DelegateMessageCallback delegateMessageCallback, IRawSipSender iRawSipSender) {
        this.mPhoneId = i;
        this.mExecutor = executor;
        this.mStateCallback = delegateStateCallback;
        this.mMessageCallback = delegateMessageCallback;
        this.mRawSipSender = iRawSipSender;
    }

    public void notifyCreated(Set<String> set, Set<FeatureTagState> set2) {
        IMSLog.i(LOG_TAG, "notifyCreated: Allowed tags " + set);
        IMSLog.i(LOG_TAG, "notifyCreated: Denied tags " + set2);
        this.mFeatureTags.addAll(set);
        this.mStateCallback.onCreated(this, set2);
    }

    public Set<String> getFeatureTags() {
        return new ArraySet(this.mFeatureTags);
    }

    public void notifyDestroy(int i) {
        IMSLog.i(LOG_TAG, "notifyDestroy: reason " + i);
        this.mStateCallback.onDestroyed(i);
    }

    public boolean isMatched(SipMsg sipMsg) {
        SipMsg.StartLine startLine = sipMsg.getStartLine();
        String str = sipMsg.getCallIds()[0];
        if (this.mCallIds.contains(str)) {
            IMSLog.i(LOG_TAG, "isMatched: Known callid [" + str + "]");
            if (sipMsg.isNonDialogMethod() && startLine != null && startLine.asStatusLine().getCode() >= 200) {
                IMSLog.d(LOG_TAG, "remove non dialog callId from mCallIds");
                this.mCallIds.remove(str);
            }
            return true;
        }
        if (!(startLine instanceof SipMsg.StatusLine) && !sipMsg.isOutGoing()) {
            if (startLine instanceof SipMsg.RequestLine) {
                String method = startLine.asRequestLine().getMethod();
                if (method.equals(HttpController.METHOD_OPTIONS) || (method.equals("NOTIFY") && sipMsg.isOneOfEvent(SipMsg.EVENT_REG, SipMsg.EVENT_PRESENCE))) {
                    return false;
                }
            }
            if (this.mFeatureTags.stream().anyMatch(new SipDelegateImpl$$ExternalSyntheticLambda9(sipMsg))) {
                if (!sipMsg.isOutGoing()) {
                    this.mCallIds.add(str);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isMatched(ImsProfile imsProfile) {
        return getFeatureTags().removeAll(SipMsg.getTagsForServices(imsProfile.getAllServiceSetFromAllNetwork()));
    }

    public void notifySipMessage(SipMsg sipMsg) {
        if (sipMsg.isOutGoing()) {
            String viaBranch = sipMsg.getViaBranch();
            IMSLog.i(LOG_TAG, "notifySipMessage: " + viaBranch);
            this.mMessageCallback.onMessageSent(viaBranch);
            return;
        }
        SipMessage telephonySipMessage = sipMsg.getTelephonySipMessage();
        IMSLog.i(LOG_TAG, "notifySipMessage: " + telephonySipMessage.getCallIdParameter());
        this.mMessageCallback.onMessageReceived(telephonySipMessage);
    }

    public void sendMessage(SipMessage sipMessage, long j) {
        if (sipMessage == null) {
            IMSLog.e(LOG_TAG, "sendMessage is null");
        } else {
            this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda7(this, sipMessage, j));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendMessage$0(SipMessage sipMessage, long j) {
        IMSLog.i(LOG_TAG, "SipDelegate.sendMessage: " + IMSLog.numberChecker(sipMessage.getStartLine(), true));
        if (isSipDelegateStale(j)) {
            IMSLog.e(LOG_TAG, "sendMessage is null");
            this.mMessageCallback.onMessageSendFailure(sipMessage.getViaBranchParameter(), 10);
            return;
        }
        this.mCallIds.add(sipMessage.getCallIdParameter());
        this.mRawSipSender.send(this.mPhoneId, new String(sipMessage.toEncodedMessage(), StandardCharsets.UTF_8), (Message) null);
    }

    private boolean isSipDelegateStale(long j) {
        long longValue = ((Long) Optional.ofNullable(this.mLatestDelegateConfig).map(new SipDelegateImpl$$ExternalSyntheticLambda6()).orElse(0L)).longValue();
        if (longValue == j) {
            return false;
        }
        IMSLog.e(LOG_TAG, "isSipDelegateStale: latest = " + longValue + ", requested = " + j);
        return true;
    }

    public void cleanupSession(String str) {
        this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda4(this, str));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$cleanupSession$1(String str) {
        IMSLog.i(LOG_TAG, "cleanupSession: " + str);
        this.mCallIds.remove(str);
    }

    public void notifyMessageReceived(String str) {
        this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda1(str));
    }

    public void notifyMessageReceiveError(String str, int i) {
        this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda2(str, i));
    }

    public void notifyAlreadyRegistered(RegistrationTracker.RegisteredToken registeredToken) {
        notifyConfigurationChanged(registeredToken.getDelegateConfig());
        notifyRegistrationChanged(registeredToken.getRegisteredFeatureTags());
    }

    public void notifyConfigurationChanged(SipDelegateConfig sipDelegateConfig) {
        this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda3(this, sipDelegateConfig));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyConfigurationChanged$4(SipDelegateConfig sipDelegateConfig) {
        this.mLatestDelegateConfig = sipDelegateConfig.convert();
        IMSLog.i(LOG_TAG, "notifyConfigurationChanged: " + this.mLatestDelegateConfig);
        this.mStateCallback.onConfigurationChanged(this.mLatestDelegateConfig);
    }

    public void notifyRegistrationChanged(Set<String> set) {
        this.mExecutor.execute(new SipDelegateImpl$$ExternalSyntheticLambda5(this, set));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyRegistrationChanged$6(Set set) {
        DelegateRegistrationState.Builder addRegisteredFeatureTags = new DelegateRegistrationState.Builder().addRegisteredFeatureTags(set);
        ArraySet arraySet = new ArraySet(this.mFeatureTags);
        arraySet.removeAll(set);
        arraySet.forEach(new SipDelegateImpl$$ExternalSyntheticLambda0(addRegisteredFeatureTags));
        this.mStateCallback.onFeatureTagRegistrationChanged(addRegisteredFeatureTags.build());
    }

    public void notifyDeRegistering(int i) {
        DelegateRegistrationState.Builder builder = new DelegateRegistrationState.Builder();
        this.mFeatureTags.forEach(new SipDelegateImpl$$ExternalSyntheticLambda8(this, builder, i));
        this.mStateCallback.onFeatureTagRegistrationChanged(builder.build());
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyDeRegistering$7(DelegateRegistrationState.Builder builder, int i, String str) {
        builder.addDeregisteringFeatureTag(str, convertReason(i));
    }

    private int convertReason(int i) {
        Integer num = REASON_MAP.get(Integer.valueOf(i));
        if (num == null) {
            return 4;
        }
        return num.intValue();
    }
}

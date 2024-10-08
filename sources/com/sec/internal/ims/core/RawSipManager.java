package com.sec.internal.ims.core;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.SipMsg$$ExternalSyntheticLambda0;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.interfaces.ims.core.IRawSipSender;
import com.sec.internal.interfaces.ims.core.handler.ISipDialogInterface;
import com.sec.internal.log.IMSLog;
import java.util.Optional;

public class RawSipManager implements IRawSipSender {
    static final int EVENT_SIP_MESSAGE_RECEIVED = 1;
    static final int EVENT_SIP_MESSAGE_SENT = 2;
    private static final String LOG_TAG = "RawSipManager";
    private final Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    protected ISipDialogInterface mRawSipInterface;

    public RawSipManager(Context context) {
        this.mContext = context;
    }

    public void init(ISipDialogInterface iSipDialogInterface) {
        this.mRawSipInterface = iSipDialogInterface;
        HandlerThread handlerThread = new HandlerThread("RawSipMgrHandler");
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        AnonymousClass1 r4 = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message message) {
                Bundle bundle = (Bundle) ((AsyncResult) message.obj).result;
                RawSipManager.this.onSipMessageEvent(message.what, bundle.getInt("phoneId"), bundle.getString("message"), bundle.getByteArray("rawContents"));
            }
        };
        this.mHandler = r4;
        this.mRawSipInterface.registerForIncomingMessages(r4, 1, (Object) null);
        this.mRawSipInterface.registerForOutgoingMessages(this.mHandler, 2, (Object) null);
    }

    /* access modifiers changed from: protected */
    public void onSipMessageEvent(int i, int i2, String str, byte[] bArr) {
        SipMsg from = SipMsg.from(str, i == 2, bArr);
        if (!from.isWellFormed()) {
            IMSLog.e(LOG_TAG, i2, "onSipMessageEvent: Wrong SIP message! SIP Message = " + ((String) Optional.ofNullable(from.getStartLine()).map(new SipMsg$$ExternalSyntheticLambda0()).orElse("Unknown!")));
        } else if (RcsUtils.isImsSingleRegiRequired(this.mContext, i2)) {
            SecImsNotifier.getInstance().notifySipMessage(i2, from);
        }
    }

    public void send(int i, String str, Message message) {
        int regId = getRegId(i);
        IMSLog.i(LOG_TAG, i, "send: regId " + regId);
        this.mRawSipInterface.sendSip(regId, str, message);
    }

    private int getRegId(int i) {
        ImsRegistration orElse = SlotBasedConfig.getInstance(i).getImsRegistrations().values().stream().filter(new RawSipManager$$ExternalSyntheticLambda2(i)).filter(new RawSipManager$$ExternalSyntheticLambda3(this)).filter(new RawSipManager$$ExternalSyntheticLambda4(this)).findFirst().orElse((Object) null);
        IMSLog.i(LOG_TAG, i, "getRegId: Found " + orElse);
        if (orElse != null) {
            return orElse.getHandle();
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getRegId$0(int i, ImsRegistration imsRegistration) {
        return imsRegistration.getPhoneId() == i;
    }

    /* access modifiers changed from: private */
    public boolean isNonEmergency(ImsRegistration imsRegistration) {
        return ((Boolean) Optional.ofNullable(imsRegistration.getImsProfile()).map(new RawSipManager$$ExternalSyntheticLambda1()).orElse(Boolean.FALSE)).booleanValue();
    }

    /* access modifiers changed from: private */
    public boolean isNonCmc(ImsRegistration imsRegistration) {
        return ((Boolean) Optional.ofNullable(imsRegistration.getImsProfile()).map(new RawSipManager$$ExternalSyntheticLambda0()).orElse(Boolean.FALSE)).booleanValue();
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Boolean lambda$isNonCmc$2(ImsProfile imsProfile) {
        return Boolean.valueOf(imsProfile.getCmcType() == 0);
    }
}

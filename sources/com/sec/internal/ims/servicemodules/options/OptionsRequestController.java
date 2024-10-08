package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class OptionsRequestController extends Handler {
    private static final int EVENT_OPTIONS_EVENT = 3;
    private static final int EVENT_PROCESS_QUEUE = 1;
    private static final int EVENT_PUSH_ERROR_RESPONSE = 7;
    private static final int EVENT_PUSH_REQUEST = 2;
    private static final int EVENT_PUSH_RESPONSE = 5;
    private static final int EVENT_SEND_CAPEX_ERROR_RESPONSE_COMPLETE = 8;
    private static final int EVENT_SEND_CAPEX_RESPONSE_COMPLETE = 6;
    private static final int EVENT_SET_OWN_CAPABILITIES = 4;
    private static final String LOG_TAG = "OptionsReqController";
    private static final int MAX_OPTIONS_REQ = 15;
    private static final int OPTIONS_PROCESS_TIMEOUT = 30;
    private Context mContext = null;
    private IOptionsEventListener mListener = null;
    private int mProcessingRequests = 0;
    private PhoneIdKeyMap<Integer> mRegistrationId = new PhoneIdKeyMap<>(SimUtil.getPhoneCount(), -1);
    final CopyOnWriteArrayList<OptionsRequest> mRequestQueue = new CopyOnWriteArrayList<>();
    IOptionsServiceInterface mService = ImsRegistry.getHandlerFactory().getOptionsHandler();

    interface IOptionsEventListener {
        void onCapabilityUpdate(OptionsEvent optionsEvent);
    }

    public OptionsRequestController(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public void init() {
        this.mService.registerForOptionsEvent(this, 3, (Object) null);
    }

    public void registerOptionsEvent(IOptionsEventListener iOptionsEventListener) {
        this.mListener = iOptionsEventListener;
    }

    public void setImsRegistration(ImsRegistration imsRegistration) {
        Optional.ofNullable(imsRegistration).ifPresent(new OptionsRequestController$$ExternalSyntheticLambda0(this, imsRegistration));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setImsRegistration$0(ImsRegistration imsRegistration, ImsRegistration imsRegistration2) {
        int phoneId = imsRegistration.getPhoneId();
        this.mRegistrationId.put(phoneId, Integer.valueOf(IRegistrationManager.getRegistrationInfoId(imsRegistration.getImsProfile().getId(), phoneId)));
        IMSLog.s(LOG_TAG, phoneId, "setImsRegistration: " + imsRegistration);
    }

    public void setImsDeRegistration(ImsRegistration imsRegistration) {
        if (imsRegistration != null) {
            int phoneId = imsRegistration.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId, "setImsDeRegistration: clearing requests queue");
            Iterator<OptionsRequest> it = this.mRequestQueue.iterator();
            while (it.hasNext()) {
                OptionsRequest next = it.next();
                if (next.getPhoneId() == phoneId) {
                    this.mRequestQueue.remove(next);
                }
            }
            this.mRegistrationId.put(phoneId, -1);
            return;
        }
        this.mRequestQueue.clear();
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mRegistrationId.put(i, -1);
        }
    }

    public void setOwnCapabilities(long j, int i) {
        sendMessage(obtainMessage(4, i, 0, Long.valueOf(j)));
    }

    public boolean requestCapabilityExchange(ImsUri imsUri, long j, int i, String str) {
        IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri: " + imsUri.toString() + ", iari: " + str);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SIG_PROPERTY_URI_NAME, imsUri);
        bundle.putLong("FEATURES", j);
        bundle.putString("EXTFEATURE", str);
        sendMessage(obtainMessage(2, i, 0, bundle));
        return true;
    }

    public boolean requestCapabilityExchange(ImsUri imsUri, Set<String> set, int i) {
        IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri: " + imsUri.toString() + ", iari: " + set);
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(set);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SIG_PROPERTY_URI_NAME, imsUri);
        bundle.putStringArrayList("MYCAPS", arrayList);
        sendMessage(obtainMessage(2, i, 0, bundle));
        return true;
    }

    public boolean sendCapexResponse(ImsUri imsUri, long j, String str, int i, int i2, String str2) {
        IMSLog.s(LOG_TAG, i2, "sendCapexResponse: uri: " + imsUri.toString());
        Bundle bundle = new Bundle();
        bundle.putString("TXID", str);
        bundle.putLong("FEATURES", j);
        bundle.putParcelable(Constants.SIG_PROPERTY_URI_NAME, imsUri);
        bundle.putInt("LASTSEEN", i);
        bundle.putString("EXTFEATURE", str2);
        sendMessage(obtainMessage(5, i2, 0, bundle));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onRequestCapabilityExchange(ImsUri imsUri, long j, String str, List<String> list, int i) {
        OptionsRequest findOptionsRequest = findOptionsRequest(imsUri, i);
        if (findOptionsRequest != null) {
            long time = new Date().getTime() - findOptionsRequest.getTimestamp().getTime();
            if (time > 30000) {
                IMSLog.i(LOG_TAG, i, "onRequestCapabilityExchange: options timeout diff = " + time + " ms, set failed");
                failedRequest(findOptionsRequest);
            } else {
                IMSLog.i(LOG_TAG, i, "onRequestCapabilityExchange: myFeatures: " + j + ", req.getMyFeatures()" + findOptionsRequest.getMyFeatures());
                if (j == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || findOptionsRequest.getMyFeatures() == j) {
                    return;
                }
            }
        }
        this.mRequestQueue.add(new OptionsRequest(imsUri, j, i, str, list));
        sendEmptyMessage(1);
    }

    public boolean sendCapexResponse(ImsUri imsUri, Set<String> set, String str, int i, int i2) {
        IMSLog.s(LOG_TAG, i2, "sendCapexResponse: uri: " + imsUri.toString());
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(set);
        Bundle bundle = new Bundle();
        bundle.putString("TXID", str);
        bundle.putStringArrayList("FEATURES", arrayList);
        bundle.putParcelable(Constants.SIG_PROPERTY_URI_NAME, imsUri);
        bundle.putInt("LASTSEEN", i);
        sendMessage(obtainMessage(5, i2, 0, bundle));
        return true;
    }

    public boolean sendCapexErrorResponse(ImsUri imsUri, String str, int i, int i2, String str2) {
        IMSLog.s(LOG_TAG, i, "sendCapexErrorResponse: uri: " + imsUri.toString());
        Bundle bundle = new Bundle();
        bundle.putString("TXID", str);
        bundle.putParcelable(Constants.SIG_PROPERTY_URI_NAME, imsUri);
        bundle.putInt("errorcode", i2);
        bundle.putString("reason", str2);
        sendMessage(obtainMessage(7, i, 0, bundle));
        return true;
    }

    private void onSendCapexResponse(ImsUri imsUri, long j, String str, int i, int i2, String str2) {
        OptionsRequest optionsRequest = new OptionsRequest(imsUri, j, i2, str2);
        optionsRequest.setIncoming(true);
        IMSLog.s(LOG_TAG, i2, "OnSendCapexResponse: txID: " + str);
        optionsRequest.setTxId(str);
        optionsRequest.setLastSeen(i);
        optionsRequest.setExtFeature(str2);
        this.mRequestQueue.add(optionsRequest);
        sendEmptyMessage(1);
    }

    private void onSendCapexResponse(ImsUri imsUri, List<String> list, String str, int i, int i2) {
        OptionsRequest optionsRequest = new OptionsRequest(imsUri, list, i2);
        optionsRequest.setIncoming(true);
        IMSLog.s(LOG_TAG, i2, "onSendCapexResponse list: txID: " + str);
        optionsRequest.setTxId(str);
        optionsRequest.setLastSeen(i);
        this.mRequestQueue.add(optionsRequest);
        sendEmptyMessage(1);
    }

    private void onSendCapexErrorResponse(ImsUri imsUri, String str, int i, int i2, String str2) {
        OptionsRequest optionsRequest = new OptionsRequest(imsUri, (List<String>) null, i2);
        optionsRequest.setIncoming(true);
        IMSLog.s(LOG_TAG, i2, "onSendCapexErrorResponse: txID: " + str);
        optionsRequest.setTxId(str);
        optionsRequest.setErrorResponseCode(i);
        optionsRequest.setReason(str2);
        this.mRequestQueue.add(optionsRequest);
        sendEmptyMessage(1);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0050  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void process() {
        /*
            r21 = this;
            r0 = r21
            java.lang.String r1 = "OptionsReqController"
            java.lang.String r2 = "process requestQueue."
            android.util.Log.i(r1, r2)
            java.util.concurrent.CopyOnWriteArrayList<com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest> r1 = r0.mRequestQueue
            int r1 = r1.size()
            if (r1 != 0) goto L_0x0013
            return
        L_0x0013:
            java.util.concurrent.CopyOnWriteArrayList<com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest> r1 = r0.mRequestQueue
            java.util.Iterator r1 = r1.iterator()
        L_0x0019:
            java.lang.Object r2 = r1.next()
            com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest r2 = (com.sec.internal.ims.servicemodules.options.OptionsRequestController.OptionsRequest) r2
            int r3 = r2.getState()
            r4 = 1
            if (r3 == r4) goto L_0x00e0
            int r3 = r2.getState()
            r5 = 2
            if (r3 != r5) goto L_0x002f
            goto L_0x00e0
        L_0x002f:
            boolean r3 = r2.isIncoming()
            if (r3 != 0) goto L_0x0050
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r5 = r0.mService
            com.sec.ims.util.ImsUri r6 = r2.getUri()
            long r7 = r2.getMyFeatures()
            int r9 = r2.getPhoneId()
            java.lang.String r10 = r2.getExtFeature()
            java.util.List r11 = r2.getMyCaps()
            r5.requestCapabilityExchange(r6, r7, r9, r10, r11)
            goto L_0x00d8
        L_0x0050:
            android.content.Context r3 = r0.mContext
            int r5 = r2.getPhoneId()
            boolean r3 = com.sec.internal.ims.rcs.util.RcsUtils.isImsSingleRegiRequired(r3, r5)
            r5 = 6
            if (r3 == 0) goto L_0x00b6
            android.content.Context r3 = r0.mContext
            int r6 = r2.getPhoneId()
            boolean r3 = com.sec.internal.ims.rcs.util.RcsUtils.isSrRcsOptionsEnabled(r3, r6)
            if (r3 == 0) goto L_0x00b6
            int r3 = r2.getErrorResponseCode()
            if (r3 == 0) goto L_0x0098
            int r3 = r2.getErrorResponseCode()
            r6 = 200(0xc8, float:2.8E-43)
            if (r3 != r6) goto L_0x0078
            goto L_0x0098
        L_0x0078:
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r7 = r0.mService
            com.sec.ims.util.ImsUri r8 = r2.getUri()
            java.lang.String r9 = r2.getTxId()
            r3 = 8
            android.os.Message r10 = r0.obtainMessage(r3)
            int r11 = r2.getPhoneId()
            int r12 = r2.getErrorResponseCode()
            java.lang.String r13 = r2.getReason()
            r7.sendCapexErrorResponse(r8, r9, r10, r11, r12, r13)
            goto L_0x00d8
        L_0x0098:
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r14 = r0.mService
            com.sec.ims.util.ImsUri r15 = r2.getUri()
            java.util.List r16 = r2.getMyCaps()
            java.lang.String r17 = r2.getTxId()
            int r18 = r2.getLastSeen()
            android.os.Message r19 = r0.obtainMessage(r5, r2)
            int r20 = r2.getPhoneId()
            r14.sendCapexResponse(r15, r16, r17, r18, r19, r20)
            goto L_0x00d8
        L_0x00b6:
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r3 = r0.mService
            com.sec.ims.util.ImsUri r6 = r2.getUri()
            long r7 = r2.getMyFeatures()
            java.lang.String r9 = r2.getTxId()
            int r10 = r2.getLastSeen()
            android.os.Message r11 = r0.obtainMessage(r5, r2)
            int r12 = r2.getPhoneId()
            java.lang.String r13 = r2.getExtFeature()
            r5 = r3
            r5.sendCapexResponse(r6, r7, r9, r10, r11, r12, r13)
        L_0x00d8:
            r2.setState(r4)
            int r2 = r0.mProcessingRequests
            int r2 = r2 + r4
            r0.mProcessingRequests = r2
        L_0x00e0:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00ec
            int r2 = r0.mProcessingRequests
            r3 = 15
            if (r2 < r3) goto L_0x0019
        L_0x00ec:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.OptionsRequestController.process():void");
    }

    /* access modifiers changed from: package-private */
    public OptionsRequest findOptionsRequest(ImsUri imsUri, int i) {
        return findRequest(imsUri, -1, i);
    }

    private OptionsRequest findRequest(ImsUri imsUri, int i, int i2) {
        Iterator<OptionsRequest> it = this.mRequestQueue.iterator();
        while (it.hasNext()) {
            OptionsRequest next = it.next();
            if (next != null && next.getPhoneId() == i2 && UriUtil.equals(next.getUri(), imsUri)) {
                if (i < 0 || next.getState() == i) {
                    return next;
                }
            }
        }
        return null;
    }

    private void failedRequest(OptionsRequest optionsRequest) {
        IMSLog.s(LOG_TAG, "failedRequest: uri: " + optionsRequest.getUri());
        this.mRequestQueue.remove(optionsRequest);
        optionsRequest.setState(3);
        this.mProcessingRequests = this.mProcessingRequests + -1;
    }

    private void completeRequest(OptionsRequest optionsRequest) {
        IMSLog.s(LOG_TAG, "completeRequest: uri: " + optionsRequest.getUri());
        this.mRequestQueue.remove(optionsRequest);
        optionsRequest.setState(2);
        this.mProcessingRequests = this.mProcessingRequests + -1;
    }

    /* access modifiers changed from: package-private */
    public void onOptionsEvent(AsyncResult asyncResult) {
        IRegistrationGovernor registrationGovernor;
        OptionsEvent optionsEvent = (OptionsEvent) asyncResult.result;
        int phoneId = optionsEvent.getPhoneId();
        if (this.mRegistrationId.get(phoneId).intValue() == -1) {
            Log.i(LOG_TAG, "onOptionsEvent: registration is null. fail.");
            return;
        }
        try {
            int handle = ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId.get(phoneId).intValue()).getHandle();
            ImsUri uri = optionsEvent.getUri();
            IMSLog.s(LOG_TAG, phoneId, "onOptionsEvent: event: " + optionsEvent);
            IMSLog.s(LOG_TAG, phoneId, "onOptionsEvent: event: mSessionId: " + optionsEvent.getSessionId() + ", featureList: " + optionsEvent.getFeatureList());
            IMSLog.s(LOG_TAG, phoneId, "onOptionsEvent: mHandle: " + handle + ", mRegistrationId: " + this.mRegistrationId);
            if (optionsEvent.isResponse()) {
                if (optionsEvent.getReason() == OptionsEvent.OptionsFailureReason.FORBIDDEN_403 && (registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(handle)) != null) {
                    Log.i(LOG_TAG, "403 forbidden response w/o warning header");
                    registrationGovernor.onSipError("options", new SipError(403, "Forbidden"));
                }
                OptionsRequest findRequest = findRequest(uri, 1, phoneId);
                if (findRequest != null) {
                    completeRequest(findRequest);
                }
                if (uri != null && uri.getUriType() == ImsUri.UriType.TEL_URI) {
                    long features = optionsEvent.getFeatures();
                    long j = Capabilities.FEATURE_CHATBOT_ROLE;
                    if ((features & j) == j) {
                        Iterator<ImsUri> it = optionsEvent.getPAssertedIdSet().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            ImsUri next = it.next();
                            if (next.getUriType() == ImsUri.UriType.SIP_URI) {
                                BotServiceIdTranslator.getInstance().register(uri.getMsisdn(), next.toString(), phoneId);
                                break;
                            }
                        }
                    }
                }
            } else {
                if (!UriUtil.hasMsisdnNumber(uri)) {
                    long features2 = optionsEvent.getFeatures();
                    long j2 = Capabilities.FEATURE_CHATBOT_ROLE;
                    if ((features2 & j2) != j2) {
                        return;
                    }
                }
                if (handle != optionsEvent.getSessionId()) {
                    IMSLog.s(LOG_TAG, phoneId, "onOptionsEvent: mHandle != event.getSessionId()");
                    return;
                }
            }
            IOptionsEventListener iOptionsEventListener = this.mListener;
            if (iOptionsEventListener != null) {
                iOptionsEventListener.onCapabilityUpdate(optionsEvent);
            }
            process();
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "onOptionsEvent: getRegistrationInfo is Null" + e.getMessage() + " mRegistrationId: " + this.mRegistrationId);
        }
    }

    private void handleSendCapexResponseComplete(AsyncResult asyncResult) {
        OptionsRequest optionsRequest = (OptionsRequest) asyncResult.userObj;
        if (optionsRequest != null) {
            Log.i(LOG_TAG, "handleSendCapexResponseComplete: txId: " + optionsRequest.getTxId() + ", state: " + optionsRequest.getState() + ", timeStamp: " + optionsRequest.getTimestamp());
            completeRequest(optionsRequest);
        }
    }

    private void handleSetOwnCapabilities(long j, int i) {
        this.mService.setOwnCapabilities(j, i);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                process();
                return;
            case 2:
                Bundle bundle = (Bundle) message.obj;
                onRequestCapabilityExchange((ImsUri) bundle.get(Constants.SIG_PROPERTY_URI_NAME), ((Long) bundle.get("FEATURES")).longValue(), (String) bundle.get("EXTFEATURE"), bundle.getStringArrayList("MYCAPS"), message.arg1);
                return;
            case 3:
                onOptionsEvent((AsyncResult) message.obj);
                return;
            case 4:
                handleSetOwnCapabilities(((Long) message.obj).longValue(), message.arg1);
                return;
            case 5:
                Bundle bundle2 = (Bundle) message.obj;
                if (!RcsUtils.isImsSingleRegiRequired(this.mContext, message.arg1) || !RcsUtils.isSrRcsOptionsEnabled(this.mContext, message.arg1)) {
                    long longValue = ((Long) bundle2.get("FEATURES")).longValue();
                    int intValue = ((Integer) bundle2.get("LASTSEEN")).intValue();
                    int i = message.arg1;
                    onSendCapexResponse((ImsUri) bundle2.get(Constants.SIG_PROPERTY_URI_NAME), longValue, (String) bundle2.get("TXID"), intValue, i, (String) bundle2.get("EXTFEATURE"));
                    return;
                }
                onSendCapexResponse((ImsUri) bundle2.get(Constants.SIG_PROPERTY_URI_NAME), (List) bundle2.get("FEATURES"), (String) bundle2.get("TXID"), ((Integer) bundle2.get("LASTSEEN")).intValue(), message.arg1);
                return;
            case 6:
                handleSendCapexResponseComplete((AsyncResult) message.obj);
                return;
            case 7:
                Bundle bundle3 = (Bundle) message.obj;
                onSendCapexErrorResponse((ImsUri) bundle3.get(Constants.SIG_PROPERTY_URI_NAME), (String) bundle3.get("TXID"), ((Integer) bundle3.get("errorcode")).intValue(), message.arg1, (String) bundle3.get("reason"));
                return;
            default:
                return;
        }
    }

    static class OptionsRequest {
        static final int DONE = 2;
        static final int FAILED = 3;
        static final int INIT = 0;
        static final int REQUESTED = 1;
        private int errorResponseCode;
        private boolean isIncoming = false;
        private int lastSeen;
        private String mExtFeature;
        private List<String> mMyCaps;
        private final long mMyFeatures;
        private int mPhoneId;
        private int mState;
        private Date mTimestamp;
        private final ImsUri mUri;
        private String reason;
        private String txId = null;

        OptionsRequest(ImsUri imsUri, long j, int i, String str) {
            this.mUri = imsUri;
            this.mMyFeatures = j;
            this.mState = 0;
            this.mTimestamp = new Date();
            this.mPhoneId = i;
            this.lastSeen = -1;
            this.mExtFeature = str;
        }

        OptionsRequest(ImsUri imsUri, long j, int i, String str, List<String> list) {
            this.mUri = imsUri;
            this.mMyFeatures = j;
            this.mState = 0;
            this.mTimestamp = new Date();
            this.mPhoneId = i;
            this.lastSeen = -1;
            this.mExtFeature = str;
            this.mMyCaps = list;
        }

        OptionsRequest(ImsUri imsUri, List<String> list, int i) {
            this.mUri = imsUri;
            this.mState = 0;
            this.mMyFeatures = 0;
            this.mTimestamp = new Date();
            this.mPhoneId = i;
            this.lastSeen = -1;
            this.mExtFeature = null;
            this.mMyCaps = list;
        }

        /* access modifiers changed from: package-private */
        public void setState(int i) {
            this.mState = i;
        }

        /* access modifiers changed from: package-private */
        public int getState() {
            return this.mState;
        }

        /* access modifiers changed from: package-private */
        public ImsUri getUri() {
            return this.mUri;
        }

        /* access modifiers changed from: package-private */
        public int getPhoneId() {
            return this.mPhoneId;
        }

        /* access modifiers changed from: package-private */
        public long getMyFeatures() {
            return this.mMyFeatures;
        }

        /* access modifiers changed from: package-private */
        public Date getTimestamp() {
            return this.mTimestamp;
        }

        /* access modifiers changed from: package-private */
        public boolean isIncoming() {
            return this.isIncoming;
        }

        /* access modifiers changed from: package-private */
        public String getExtFeature() {
            return this.mExtFeature;
        }

        /* access modifiers changed from: package-private */
        public List<String> getMyCaps() {
            return this.mMyCaps;
        }

        /* access modifiers changed from: package-private */
        public void setIncoming(boolean z) {
            this.isIncoming = z;
        }

        /* access modifiers changed from: package-private */
        public String getTxId() {
            return this.txId;
        }

        /* access modifiers changed from: package-private */
        public void setTxId(String str) {
            this.txId = str;
        }

        /* access modifiers changed from: package-private */
        public int getLastSeen() {
            return this.lastSeen;
        }

        /* access modifiers changed from: package-private */
        public void setLastSeen(int i) {
            this.lastSeen = i;
        }

        /* access modifiers changed from: package-private */
        public void setExtFeature(String str) {
            this.mExtFeature = str;
        }

        public String getReason() {
            return this.reason;
        }

        public void setReason(String str) {
            this.reason = str;
        }

        public int getErrorResponseCode() {
            return this.errorResponseCode;
        }

        public void setErrorResponseCode(int i) {
            this.errorResponseCode = i;
        }
    }
}

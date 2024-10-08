package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.ims.core.handler.OptionsHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.OptionsReceivedInfo;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResipOptionsHandler extends OptionsHandler {
    public static final int EVENT_OPTIONS_RECEIVED_NOTIFY = 102;
    public static final int EVENT_OPTIONS_REQ_RESPONSE = 101;
    private static final String LOG_TAG = "ResipOptionsHandler";
    static Map<Long, Integer> mFeatureMap;
    private Registrant mCmcRegistrant = null;
    private final IImsFramework mImsFramework;
    private Registrant mP2pRegistrant = null;
    private Registrant mRegistrant = null;
    private StackIF mStackIf;

    static {
        HashMap hashMap = new HashMap();
        mFeatureMap = hashMap;
        hashMap.put(Long.valueOf((long) Capabilities.FEATURE_CHAT_CPM), 20);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_CHAT_SIMPLE_IM), 20);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_STANDALONE_MSG), 10);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_SF_GROUP_CHAT), 21);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_INTEGRATED_MSG), 27);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_PRESENCE_DISCOVERY), 19);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_SOCIAL_PRESENCE), 28);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT), 22);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_HTTP), 25);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_STORE), 24);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_THUMBNAIL), 23);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_GEOLOCATION_PUSH), 31);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_MMTEL), 9);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_MMTEL_VIDEO), 6);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_MMTEL_CALL_COMPOSER), 55);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL), 0);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL_VIDEO), 1);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_IPCALL_VIDEO_ONLY), 2);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_ISH), 18);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_VSH), 3);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_FT_VIA_SMS), 38);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_STICKER), 37);
        mFeatureMap.put(Long.valueOf((long) Capabilities.FEATURE_GEO_VIA_SMS), 39);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_CALL_COMPOSER), 14);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_SHARED_MAP), 15);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_SHARED_SKETCH), 17);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_ENRICHED_POST_CALL), 16);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_PUBLIC_MSG), 42);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_LAST_SEEN_ACTIVE), 43);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_CHAT_SESSION), 44);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_STANDALONE_MSG), 45);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG), 46);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CHATBOT_ROLE), 53);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_CANCEL_MESSAGE), 47);
        mFeatureMap.put(Long.valueOf(Capabilities.FEATURE_PLUG_IN), 48);
    }

    public void registerForOptionsEvent(Handler handler, int i, Object obj) {
        this.mRegistrant = new Registrant(handler, i, obj);
    }

    public void registerForCmcOptionsEvent(Handler handler, int i, Object obj) {
        this.mCmcRegistrant = new Registrant(handler, i, obj);
    }

    public void registerForP2pOptionsEvent(Handler handler, int i, Object obj) {
        this.mP2pRegistrant = new Registrant(handler, i, obj);
    }

    private void notifyEvent(OptionsEvent optionsEvent) {
        Registrant registrant = this.mRegistrant;
        if (registrant != null) {
            registrant.notifyResult(optionsEvent);
        }
    }

    private void notifyCmcEvent(OptionsEvent optionsEvent) {
        Registrant registrant = this.mCmcRegistrant;
        if (registrant != null) {
            registrant.notifyResult(optionsEvent);
        }
    }

    private void notifyP2pEvent(OptionsEvent optionsEvent) {
        Registrant registrant = this.mP2pRegistrant;
        if (registrant != null) {
            registrant.notifyResult(optionsEvent);
        }
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 101) {
            Log.i(LOG_TAG, "handleMessage: EVENT_OPTIONS_REQ_RESPONSE");
        } else if (i != 102) {
            Log.i(LOG_TAG, "handleMessage: unknown event");
        } else {
            handleNotify((Notify) ((AsyncResult) message.obj).result);
            Log.i(LOG_TAG, "handleMessage: EVENT_OPTIONS_RECEIVED_NOTIFY");
        }
    }

    public ResipOptionsHandler(Looper looper, IImsFramework iImsFramework) {
        super(looper);
        this.mImsFramework = iImsFramework;
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerOptionsHandler(this, 102, (Object) null);
    }

    public void setOwnCapabilities(long j, int i) {
        IMSLog.i(LOG_TAG, i, "setOwnCapabilities: " + j);
        UserAgent ua = getUa("options", i);
        if (ua == null) {
            Log.e(LOG_TAG, "setOwnCapabilities: UserAgent not found.");
            return;
        }
        IMSLog.i(LOG_TAG, i, "setOwnCapabilities: handle = " + ua.getHandle());
        this.mStackIf.requestUpdateFeatureTag(ua.getHandle(), j);
    }

    public void requestCapabilityExchange(ImsUri imsUri, long j, int i, String str, List<String> list) {
        int i2 = i;
        StringBuilder sb = new StringBuilder();
        sb.append("requestCapabilityExchange: uri: ");
        ImsUri imsUri2 = imsUri;
        sb.append(imsUri);
        sb.append(", iari: ");
        sb.append(str);
        IMSLog.s(LOG_TAG, i2, sb.toString());
        UserAgent ua = getUa("options", i2);
        if (ua == null) {
            Log.e(LOG_TAG, "requestCapabilityExchange: UserAgent not found.");
            return;
        }
        IMSLog.i(LOG_TAG, i2, "requestCapabilityExchange: handle = " + ua.getHandle());
        this.mStackIf.requestOptionsReqCapabilityExchange(ua.getHandle(), imsUri.toString(), j, str, list);
    }

    public void requestSendCmcCheckMsg(int i, int i2, String str) {
        IMSLog.s(LOG_TAG, i, "requestSendCmcCheckMsg: regId: " + i2 + ",uri: " + str);
        UserAgent uaByRegId = getUaByRegId(i2);
        if (uaByRegId == null) {
            Log.e(LOG_TAG, "requestSendCmcCheckMsg: UserAgent not found.");
            return;
        }
        Log.i(LOG_TAG, "requestSendCmcCheckMsg: handle = " + uaByRegId.getHandle());
        this.mStackIf.requestOptionsReqSendCmcCheckMsg(uaByRegId.getHandle(), str);
    }

    public void sendCapexResponse(ImsUri imsUri, long j, String str, int i, Message message, int i2, String str2) {
        int i3 = i2;
        IMSLog.s(LOG_TAG, i3, "sendCapexResponse: uri: " + imsUri);
        UserAgent ua = getUa("options", i3);
        if (ua == null) {
            Log.e(LOG_TAG, "sendCapexResponse: UserAgent not found.");
            return;
        }
        IMSLog.i(LOG_TAG, i3, "sendCapexResponse: handle = " + ua.getHandle());
        this.mStackIf.sendCapexResponse(ua.getHandle(), imsUri.toString(), j, str, i, message, str2);
    }

    public void sendCapexResponse(ImsUri imsUri, List<String> list, String str, int i, Message message, int i2) {
        int i3 = i2;
        StringBuilder sb = new StringBuilder();
        sb.append("sendCapexResponse: uri: ");
        ImsUri imsUri2 = imsUri;
        sb.append(imsUri);
        IMSLog.s(LOG_TAG, i3, sb.toString());
        UserAgent ua = getUa("options", i3);
        if (ua == null) {
            Log.e(LOG_TAG, "sendCapexResponse: UserAgent not found.");
            return;
        }
        IMSLog.i(LOG_TAG, i3, "sendCapexResponse list : handle = " + ua.getHandle());
        this.mStackIf.sendCapexResponse(ua.getHandle(), imsUri.toString(), list, str, i, message);
    }

    public void sendCapexErrorResponse(ImsUri imsUri, String str, Message message, int i, int i2, String str2) {
        int i3 = i;
        StringBuilder sb = new StringBuilder();
        sb.append("sendCapexErrorResponse: uri: ");
        ImsUri imsUri2 = imsUri;
        sb.append(imsUri);
        IMSLog.s(LOG_TAG, i3, sb.toString());
        UserAgent ua = getUa("options", i3);
        if (ua == null) {
            Log.e(LOG_TAG, "sendCapexErrorResponse: UserAgent not found.");
            return;
        }
        IMSLog.i(LOG_TAG, i3, "sendCapexErrorResponse: handle = " + ua.getHandle());
        this.mStackIf.sendCapexErrorResponse(ua.getHandle(), imsUri.toString(), str, i2, str2, message);
    }

    public int updateCmcExtCallCount(int i, int i2) {
        Log.i(LOG_TAG, "updateCmcExtCallCount: phoneId= " + i + ", callCnt= " + i2);
        this.mStackIf.updateCmcExtCallCount(i, i2);
        return 0;
    }

    private UserAgent getUa(String str, int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(str, i);
    }

    private UserAgent getUaByRegId(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(i);
    }

    private void handleNotify(Notify notify) {
        if (notify.notifyid() != 15001) {
            Log.w(LOG_TAG, "handleNotify(): unexpected id");
            return;
        }
        Log.i(LOG_TAG, "handleNotify(), NOTIFY_OPTIONS_RECEIVED.");
        handleOptionsReceived(notify);
    }

    private void handleOptionsReceived(Notify notify) {
        boolean z;
        int i;
        OptionsEvent.OptionsFailureReason optionsFailureReason;
        OptionsEvent optionsEvent;
        boolean z2;
        Log.i(LOG_TAG, "handleOptionsReceived()");
        if (notify.notiType() != 63) {
            Log.e(LOG_TAG, "Invalid notify");
            return;
        }
        OptionsReceivedInfo optionsReceivedInfo = (OptionsReceivedInfo) notify.noti(new OptionsReceivedInfo());
        String remoteUri = optionsReceivedInfo.remoteUri();
        boolean isResponse = optionsReceivedInfo.isResponse();
        boolean success = optionsReceivedInfo.success();
        int reason = optionsReceivedInfo.reason();
        int sessionId = (int) optionsReceivedInfo.sessionId();
        String txId = optionsReceivedInfo.txId();
        String extFeature = optionsReceivedInfo.extFeature();
        boolean isChatbotParticipant = optionsReceivedInfo.isChatbotParticipant();
        boolean isCmcCheck = optionsReceivedInfo.isCmcCheck();
        Log.i(LOG_TAG, "handleOptionsReceived: isResponse: " + isResponse + ", success: " + success + ", txId: " + txId + ", extfeature: " + extFeature + ", isCmcCheck: " + isCmcCheck);
        int tagsLength = optionsReceivedInfo.tagsLength();
        StringBuilder sb = new StringBuilder();
        sb.append("handleOptionsReceived: tagLength ");
        sb.append(tagsLength);
        Log.i(LOG_TAG, sb.toString());
        long j = 0;
        if (tagsLength != 0) {
            for (int i2 = 0; i2 < tagsLength; i2++) {
                int tags = optionsReceivedInfo.tags(i2);
                for (Map.Entry next : mFeatureMap.entrySet()) {
                    int i3 = tagsLength;
                    boolean z3 = isCmcCheck;
                    if (((Integer) next.getValue()).equals(Integer.valueOf(tags))) {
                        j |= ((Long) next.getKey()).longValue();
                        Log.i(LOG_TAG, "handleOptionsReceived: key = " + next.getKey());
                    }
                    tagsLength = i3;
                    isCmcCheck = z3;
                }
                int i4 = tagsLength;
                boolean z4 = isCmcCheck;
            }
            z = isCmcCheck;
            Log.i(LOG_TAG, "handleOptionsReceived: received tags " + j);
        } else {
            z = isCmcCheck;
        }
        long j2 = j;
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        if (registrationManager.getUserAgent(sessionId) != null) {
            i = ((UserAgent) registrationManager.getUserAgent(sessionId)).getPhoneId();
        } else {
            Log.i(LOG_TAG, "handleOptionsReceived: uaHandle is invalid ");
            i = 0;
        }
        IMSLog.i(LOG_TAG, i, "handleOptionsReceived: sessionId = " + sessionId);
        UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(i, UriGenerator.URIServiceType.RCS_URI);
        if (uriGenerator == null) {
            IMSLog.e(LOG_TAG, i, "UriGenerator is null. IMS URIs won't be normalized!");
        }
        ImsUri parse = ImsUri.parse(remoteUri);
        if (uriGenerator != null) {
            parse = uriGenerator.normalize(parse);
        }
        ImsUri imsUri = parse;
        ArrayList arrayList = new ArrayList();
        String str = extFeature;
        int i5 = reason;
        for (int i6 = 0; i6 < optionsReceivedInfo.pAssertedIdLength(); i6++) {
            arrayList.add(optionsReceivedInfo.pAssertedId(i6));
        }
        HashSet hashSet = new HashSet(2, 1.0f);
        Iterator it = arrayList.iterator();
        boolean z5 = false;
        while (it.hasNext()) {
            String str2 = (String) it.next();
            Iterator it2 = it;
            ImsUri parse2 = ImsUri.parse(str2);
            if (parse2 != null) {
                if (z5 || !isChatbotParticipant || parse2.getParam("tk") == null) {
                    z2 = isChatbotParticipant;
                    z5 = z5;
                } else {
                    z2 = isChatbotParticipant;
                    z5 = parse2.getParam("tk").equals("on") ? true : z5;
                    parse2.removeParam("tk");
                }
                if (uriGenerator != null) {
                    parse2 = uriGenerator.normalize(parse2);
                }
                IMSLog.s(LOG_TAG, i, "adding " + parse2 + " to PAssertedIdSet");
                hashSet.add(parse2);
            } else {
                z2 = isChatbotParticipant;
                boolean z6 = z5;
                IMSLog.s(LOG_TAG, i, "parsing P-Asserted-Identity " + str2 + " returned null");
            }
            it = it2;
            isChatbotParticipant = z2;
        }
        boolean z7 = z;
        String str3 = str;
        OptionsEvent optionsEvent2 = r4;
        int i7 = i;
        int i8 = i5;
        boolean z8 = z5;
        OptionsEvent optionsEvent3 = new OptionsEvent(success, imsUri, j2, i, isResponse, sessionId, txId, hashSet, str3);
        if (!success) {
            if (i8 == 7) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.INVALID_DATA;
            } else if (i8 == 5) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.REQUEST_TIMED_OUT;
            } else if (i8 == 6) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.AUTOMATA_PRESENT;
            } else if (i8 == 1) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.USER_NOT_AVAILABLE;
            } else if (i8 == 2) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.DOES_NOT_EXIST_ANYWHERE;
            } else if (i8 == 4) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.USER_NOT_REACHABLE;
            } else if (i8 == 3) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.USER_NOT_REGISTERED;
            } else if (i8 == 8) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.FORBIDDEN_403;
            } else if (i8 == 0) {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.USER_AVAILABLE_OFFLINE;
            } else {
                optionsFailureReason = OptionsEvent.OptionsFailureReason.ERROR;
            }
            IMSLog.i(LOG_TAG, i7, "handleOptionsReceived: error reason: " + optionsFailureReason);
        } else {
            optionsFailureReason = null;
        }
        if (optionsReceivedInfo.capsLength() != 0) {
            ArrayList arrayList2 = new ArrayList();
            for (int i9 = 0; i9 < optionsReceivedInfo.capsLength(); i9++) {
                arrayList2.add(optionsReceivedInfo.caps(i9));
            }
            optionsEvent = optionsEvent2;
            optionsEvent.setfeatureTags(arrayList2);
        } else {
            optionsEvent = optionsEvent2;
        }
        optionsEvent.setRespCode(optionsReceivedInfo.respCode());
        optionsEvent.setReasonHdr(optionsReceivedInfo.failReason());
        optionsEvent.setReason(optionsFailureReason);
        optionsEvent.setIsTokenUsed(z8);
        Log.i(LOG_TAG, "handleOptionsReceived: lastSeen " + IMSLog.checker(Integer.valueOf(optionsReceivedInfo.lastSeen())));
        if (optionsReceivedInfo.lastSeen() >= 0) {
            optionsEvent.setLastSeen(optionsReceivedInfo.lastSeen());
        }
        List<String> featureList = optionsEvent.getFeatureList();
        for (int i10 = 0; i10 < optionsReceivedInfo.capsLength(); i10++) {
            featureList.add(optionsReceivedInfo.caps(i10));
        }
        if (z) {
            Log.i(LOG_TAG, "handleOptionsReceived: recevied OPTION response msg for CMC");
            notifyCmcEvent(optionsEvent);
            return;
        }
        if ("d2d.push".equals(str3)) {
            notifyP2pEvent(optionsEvent);
        } else {
            notifyEvent(optionsEvent);
        }
    }
}

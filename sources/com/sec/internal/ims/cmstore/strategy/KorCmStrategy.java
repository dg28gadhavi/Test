package com.sec.internal.ims.cmstore.strategy;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler;
import com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.util.HttpAuthGenerator;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;

public class KorCmStrategy extends DefaultCloudMessageStrategy {
    public static final int SYNC_MAX_BULK_OPTION = 20;
    private final String KOR_STORE_NAME = "os";
    private String LOG_TAG = KorCmStrategy.class.getSimpleName();
    private final String SKT_BASIC_AUTH_DEV_CLIENT_ID = "fa2d462e-6733-438b-9ce6-ece340219487";
    private final String SKT_BASIC_AUTH_DEV_CLIENT_SECRET = "e621e4301820d2f50ef93f4a73113aca";
    private final String SKT_BASIC_AUTH_DEV_DE_PARAM = "ZGV2X21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=";
    private final String SKT_BASIC_AUTH_PRD_CLIENT_ID = "d11108fc-dac7-4b3c-bc81-5601c789a6f6";
    private final String SKT_BASIC_AUTH_PRD_CLIENT_SECRET = "c896cf0606d7cf46b5944ebe8f71d55b";
    private final String SKT_BASIC_AUTH_PRD_DE_PARAM = "cHJkX21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=";
    private final String SKT_BASIC_AUTH_STG_CLIENT_ID = "49a34e35-7c00-469a-a93a-b518c2f2f2d9";
    private final String SKT_BASIC_AUTH_STG_CLIENT_SECRET = "f8d195801bca4fb9359fe1db56ebac59";
    private final String SKT_BASIC_AUTH_STG_DE_PARAM = "c3RnX21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=";
    private int mApiFailCount = 0;
    private IControllerCommonInterface mControllerOfLastFailedAPI = null;
    private Class<? extends IHttpAPICommonInterface> mLastFailedAPI = null;
    private Mno mMno = Mno.DEFAULT;

    public static class KorAttributeNames extends AttributeNames {
        public static String conversation_id = "Conversation-ID";
        public static String extended_rcs = "ExtendedRCS";
        public static String p_asserted_service = "P-Asserted-Service";
        public static String safety = "Safety";
    }

    public String getClientVersion() {
        return "1.0.0";
    }

    public String getOMAApiVersion() {
        return "v1";
    }

    public String getStoreName() {
        return "os";
    }

    public boolean isEncrypted() {
        return false;
    }

    public boolean isErrorCodeSupported(int i, IHttpAPICommonInterface iHttpAPICommonInterface) {
        return i == 401 || i == 429 || i == 404 || (i >= 500 && i < 600);
    }

    public boolean shouldStopSendingAPIwhenNetworklost() {
        return true;
    }

    static {
        HashMap hashMap = new HashMap();
        DefaultCloudMessageStrategy.mMessageTypeMapping = hashMap;
        hashMap.put(Integer.valueOf(McsConstants.TP_MessageType.MULTIMEDIA.getId()), Integer.valueOf(McsConstants.RCSMessageType.MULTIMEDIA.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_USER_LEFT.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_USER_LEFT.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_USER_INVITED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_USER_INVITED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_USER_JOINED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_USER_JOINED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_CONTINUE_ON_ANOTHER_DEVICE.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_CONTINUE_ON_ANOTHER_DEVICE.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.TEXT.getId()), Integer.valueOf(McsConstants.RCSMessageType.TEXT.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.LOCATION.getId()), Integer.valueOf(McsConstants.RCSMessageType.LOCATION.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_LEADER_CHANGED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_LEADER_CHANGED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_GROUP_INVITE.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_GROUP_INVITE.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_GROUP_INVITE_FAIL.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_GROUP_INVITE_FAIL.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_GROUP_REINVITE.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_GROUP_REINVITE.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_LEADER_INFORMED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_LEADER_INFORMED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_DISMISS_CHAT.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_DISMISS_CHAT.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_KICKED_OUT_BY_LEADER.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_KICKED_OUT_BY_LEADER.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_RENAME_BY_LEADER.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_RENAME_BY_LEADER.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_LEFT_CHAT.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_LEFT_CHAT.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_ALL_LEFT_CHAT.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_ALL_LEFT_CHAT.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_GROUPCHAT_CLOSED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_GROUPCHAT_CLOSED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_IS_INVITED.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_IS_INVITED.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SYSTEM_ALL_LEFT_CHAT_NO_ADD.getId()), Integer.valueOf(McsConstants.RCSMessageType.SYSTEM_ALL_LEFT_CHAT_NO_ADD.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.SINGLE.getId()), Integer.valueOf(McsConstants.RCSMessageType.SINGLE.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping.put(Integer.valueOf(McsConstants.TP_MessageType.GROUP.getId()), Integer.valueOf(McsConstants.RCSMessageType.GROUP.getId()));
        DefaultCloudMessageStrategy.mMessageTypeMapping = Collections.unmodifiableMap(DefaultCloudMessageStrategy.mMessageTypeMapping);
    }

    KorCmStrategy(MessageStoreClient messageStoreClient) {
        super(messageStoreClient);
        String str = this.LOG_TAG + "[" + messageStoreClient.getClientID() + "]";
        this.LOG_TAG = str;
        Log.d(str, "KorCmStrategy");
        this.mStrategyType = DefaultCloudMessageStrategy.CmStrategyType.KOR;
        this.mContentType = "application/json";
        this.mMno = this.mStoreClient.getSimManager().getSimMno();
        this.mProtocol = OMAGlobalVariables.HTTPS;
        this.mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        this.mMaxBulkOption = 20;
        onOmaFlowInitStart();
        initOMASuccessfulCallFlowTranslator();
        initOMAFailedCallFlowTranslator();
        onOmaFlowInitComplete();
        initStandardRetrySchedule();
        initMessageAttributeRegistration();
        initOmaRetryVariables();
    }

    private void initOMAFailedCallFlowTranslator() {
        initOmaFailureCommonFlow();
    }

    public String getBasicPassword(String str, String str2) {
        return Base64.encodeToString((str + ":" + str2).getBytes(StandardCharsets.UTF_8), 2);
    }

    public String getAuthorizationBasic() {
        String oasisAuthRoot = this.mStoreClient.getPrerenceManager().getOasisAuthRoot();
        if (oasisAuthRoot.contains("dev")) {
            return HttpAuthGenerator.generateBasicAuthHeader("fa2d462e-6733-438b-9ce6-ece340219487", getBasicPassword("fa2d462e-6733-438b-9ce6-ece340219487", "e621e4301820d2f50ef93f4a73113aca"));
        }
        if (oasisAuthRoot.contains("stg")) {
            return HttpAuthGenerator.generateBasicAuthHeader("49a34e35-7c00-469a-a93a-b518c2f2f2d9", getBasicPassword("49a34e35-7c00-469a-a93a-b518c2f2f2d9", "f8d195801bca4fb9359fe1db56ebac59"));
        }
        return oasisAuthRoot.contains(McsConstants.Auth.ROOT) ? HttpAuthGenerator.generateBasicAuthHeader("d11108fc-dac7-4b3c-bc81-5601c789a6f6", getBasicPassword("d11108fc-dac7-4b3c-bc81-5601c789a6f6", "c896cf0606d7cf46b5944ebe8f71d55b")) : "";
    }

    public String getAuthorizationBearer() {
        String mcsAccessToken = this.mStoreClient.getPrerenceManager().getMcsAccessToken();
        if (TextUtils.isEmpty(mcsAccessToken)) {
            return "";
        }
        return "Bearer " + mcsAccessToken;
    }

    private String getDecryptPasswordBasic() {
        String oasisAuthRoot = this.mStoreClient.getPrerenceManager().getOasisAuthRoot();
        if (oasisAuthRoot.contains("dev")) {
            return Util.decodeBase64("ZGV2X21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=");
        }
        if (oasisAuthRoot.contains("stg")) {
            return Util.decodeBase64("c3RnX21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=");
        }
        return oasisAuthRoot.contains(McsConstants.Auth.ROOT) ? Util.decodeBase64("cHJkX21lc3NhZ2luZ19vYXNpc19mb3JldmVyXzEyIUA=") : "";
    }

    private String getDecryptPasswordBearer(Context context) {
        try {
            String mcsClientId = CmsUtil.getMcsClientId(context);
            String decryptPasswordBasic = getDecryptPasswordBasic();
            return mcsClientId.substring(0, 6) + decryptPasswordBasic.substring(0, 26);
        } catch (NullPointerException | StringIndexOutOfBoundsException e) {
            IMSLog.e(this.LOG_TAG, e.getMessage());
            return "";
        }
    }

    public String decrypt(String str, boolean z) {
        String str2;
        try {
            String string = new JSONObject(str).getString(McsConstants.Decryption.ENCRYPTED_DATA);
            String substring = string.substring(0, string.length() - 24);
            String substring2 = string.substring(string.length() - 24);
            byte[] hexStringToBytes = StrUtil.hexStringToBytes(substring);
            byte[] hexStringToBytes2 = StrUtil.hexStringToBytes(substring2);
            if (z) {
                str2 = getDecryptPasswordBearer(this.mStoreClient.getContext());
            } else {
                str2 = getDecryptPasswordBasic();
            }
            if (TextUtils.isEmpty(str2)) {
                return "";
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(str2.getBytes(StandardCharsets.UTF_8), SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
            GCMParameterSpec gCMParameterSpec = new GCMParameterSpec(128, hexStringToBytes2);
            Cipher instance = Cipher.getInstance("AES/GCM/NoPadding");
            instance.init(2, secretKeySpec, gCMParameterSpec);
            String replace = new String(instance.doFinal(hexStringToBytes), StandardCharsets.UTF_8).replace("\\\"", CmcConstants.E_NUM_STR_QUOTE).replace("\\\\", "\\");
            return replace.substring(1, replace.length() - 1);
        } catch (Exception e) {
            IMSLog.e(this.LOG_TAG, e.getMessage());
            return "";
        }
    }

    public String getDeviceType() {
        return DeviceUtil.isTablet() ? ImConstants.MessageCreatorTag.SD : "PD";
    }

    public int getTypeUsingMessageContext(String str) {
        if (str.equals(MessageContextValues.pagerMessage)) {
            return 3;
        }
        if (str.equals("multimedia-message")) {
            return 4;
        }
        if (str.equals("chat-message") || str.equals(McsConstants.McsMessageContextValues.geolocationMessage) || str.equals(McsConstants.McsMessageContextValues.botMessage) || str.equals(McsConstants.McsMessageContextValues.responseMessage)) {
            return 11;
        }
        if (str.equals("file-message")) {
            return 12;
        }
        if (str.equals("standalone-message")) {
            return 14;
        }
        if (str.equals("imdn-message")) {
            return 13;
        }
        if (str.equals(MessageContextValues.voiceMessage)) {
            return 17;
        }
        return str.equals(McsConstants.McsMessageContextValues.conferenceMessage) ? 38 : 0;
    }

    public String getNcHost() {
        return this.mStoreClient.getPrerenceManager().getOasisServerRoot();
    }

    public String getSmsHashTagOrCorrelationTag(String str, int i, String str2) {
        return AmbsUtils.generateSmsHashCode(str, i, str2, new String[]{"::", ":", ":::"}, true);
    }

    public String getNmsHost() {
        return this.mStoreClient.getPrerenceManager().getOasisServerRoot();
    }

    public boolean shouldCareAfterPreProcess(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i) {
        int statusCode = httpResponseParams.getStatusCode();
        String str = iHttpAPICommonInterface instanceof BaseNMSRequest ? "NMS" : "NC";
        String str2 = this.LOG_TAG;
        Log.i(str2, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], res code[" + statusCode + "]");
        if (isOmaErrorRuleMatch(statusCode, iHttpAPICommonInterface, iAPICallFlowListener, obj, i)) {
            this.mStoreClient.getMcsRetryMapAdapter().remove(iHttpAPICommonInterface);
            String str3 = this.LOG_TAG;
            Log.i(str3, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], isOmaErrorRuleMatch");
            return false;
        } else if (!shouldCareAfterProcessOMACommonCase(iAPICallFlowListener, iHttpAPICommonInterface, httpResponseParams, bufferDBChangeParam)) {
            String str4 = this.LOG_TAG;
            Log.i(str4, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], match common cases");
            return false;
        } else {
            String str5 = this.LOG_TAG;
            Log.i(str5, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], [" + statusCode + "] catch call, return");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isOmaErrorRuleMatch(int i, IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, Object obj, int i2) {
        int i3;
        String str;
        OmaErrorKey omaErrorKey = new OmaErrorKey(i, iHttpAPICommonInterface.getClass().getSimpleName(), getHandlerClassName(iAPICallFlowListener));
        OmaErrorKey omaErrorKey2 = new OmaErrorKey(i, iHttpAPICommonInterface.getClass().getSimpleName(), Handler.class.getSimpleName());
        if (this.mOmaCallFlowTranslator.containsKey(omaErrorKey)) {
            i3 = this.mOmaCallFlowTranslator.get(omaErrorKey).intValue();
        } else {
            i3 = this.mOmaCallFlowTranslator.containsKey(omaErrorKey2) ? this.mOmaCallFlowTranslator.get(omaErrorKey2).intValue() : Integer.MIN_VALUE;
        }
        if (i2 == Integer.MIN_VALUE) {
            i2 = i3;
        }
        if (i2 == Integer.MIN_VALUE) {
            return false;
        }
        OMASyncEventType valueOf = OMASyncEventType.valueOf(i2);
        if (valueOf == null) {
            str = null;
        } else {
            str = valueOf.name();
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "API[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], match rule[" + str + "]");
        if (i < 200 || i >= 300) {
            iAPICallFlowListener.onFailedEvent(i2, obj);
            return true;
        }
        iAPICallFlowListener.onSuccessfulEvent(iHttpAPICommonInterface, i2, obj);
        return true;
    }

    private void initOmaRetryVariables() {
        this.mApiFailCount = this.mStoreClient.getPrerenceManager().getOmaRetryCounter();
        this.mMaxRetryCounter = 1;
        String str = this.LOG_TAG;
        Log.i(str, "OMA fail count is: " + this.mApiFailCount);
    }

    private String getHandlerClassName(IAPICallFlowListener iAPICallFlowListener) {
        String simpleName = iAPICallFlowListener.getClass().getSimpleName();
        if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
            return BaseDataChangeHandler.class.getSimpleName();
        }
        if (iAPICallFlowListener instanceof BaseDeviceDataUpdateHandler) {
            return BaseDeviceDataUpdateHandler.class.getSimpleName();
        }
        if (iAPICallFlowListener instanceof BaseSyncHandler) {
            return BaseSyncHandler.class.getSimpleName();
        }
        return ((iAPICallFlowListener instanceof OMAPollingScheduler) || (iAPICallFlowListener instanceof ChannelScheduler)) ? OMAPollingScheduler.class.getSimpleName() : simpleName;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCareAfterProcessOMACommonCase(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, BufferDBChangeParam bufferDBChangeParam) {
        int statusCode = httpResponseParams.getStatusCode();
        if ((statusCode >= 500 && statusCode < 600) || statusCode == 429) {
            return retryIfAvailable(iAPICallFlowListener, iHttpAPICommonInterface, statusCode);
        }
        if (statusCode != 401) {
            return true;
        }
        if (this.mApiFailCount >= getMaxRetryCounter()) {
            String str = this.LOG_TAG;
            Log.i(str, "OMA API failed " + this.mApiFailCount + " times before, OMA API retired more than " + getMaxRetryCounter() + " times");
            clearOmaRetryVariables();
            return true;
        }
        this.mLastFailedAPI = iHttpAPICommonInterface.getClass();
        iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
        return false;
    }

    private void initStandardRetrySchedule() {
        HashMap hashMap = new HashMap();
        this.mStandardRetrySchedule = hashMap;
        hashMap.put(0, 0);
        this.mStandardRetrySchedule.put(1, 300000);
        this.mStandardRetrySchedule.put(2, 1800000);
        this.mStandardRetrySchedule.put(3, 14400000);
        this.mStandardRetrySchedule = Collections.unmodifiableMap(this.mStandardRetrySchedule);
    }

    public void onOmaApiCredentialFailed(IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        iControllerCommonInterface.setOnApiSucceedOnceListener((OMANetAPIHandler.OnApiSucceedOnceListener) null);
        if (this.mApiFailCount >= getMaxRetryCounter()) {
            String str = this.LOG_TAG;
            Log.i(str, "OMA API failed " + this.mApiFailCount + " times before, OMA API retired more than " + getMaxRetryCounter() + " times, pop up error screen");
            clearOmaRetryVariables();
            iNetAPIEventListener.onOmaFailExceedMaxCount();
            return;
        }
        long adaptedRetrySchedule = (long) getAdaptedRetrySchedule(this.mApiFailCount);
        if (i > 0) {
            adaptedRetrySchedule = Math.max(adaptedRetrySchedule, ((long) i) * 1000);
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "OMA API failed " + this.mApiFailCount + " times beforeGo ahead fallback to SessionGen after " + (adaptedRetrySchedule / 1000) + " seconds");
        Message message = new Message();
        message.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
        message.obj = Long.valueOf(adaptedRetrySchedule);
        iControllerCommonInterface.updateMessage(message);
        increaseFailedCount(iHttpAPICommonInterface, iControllerCommonInterface);
    }

    private void increaseFailedCount(IHttpAPICommonInterface iHttpAPICommonInterface, IControllerCommonInterface iControllerCommonInterface) {
        if (iHttpAPICommonInterface.getClass().equals(this.mLastFailedAPI)) {
            this.mApiFailCount++;
            Log.i(this.LOG_TAG, "failed count increment 1, failed count: " + this.mApiFailCount);
            this.mStoreClient.getPrerenceManager().saveOmaRetryCounter(this.mApiFailCount);
            return;
        }
        this.mLastFailedAPI = iHttpAPICommonInterface.getClass();
        this.mControllerOfLastFailedAPI = iControllerCommonInterface;
        Log.i(this.LOG_TAG, "fail count keep same[" + this.mApiFailCount + "], lastFailedAPI: " + this.mLastFailedAPI.getSimpleName() + ", currentFailedAPI: " + iHttpAPICommonInterface.getClass().getSimpleName());
    }

    public void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface.getClass().equals(this.mLastFailedAPI)) {
            clearOmaRetryVariables();
        }
    }

    public IControllerCommonInterface getControllerOfLastFailedApi() {
        return this.mControllerOfLastFailedAPI;
    }

    public Class<? extends IHttpAPICommonInterface> getLastFailedApi() {
        return this.mLastFailedAPI;
    }

    private void clearOmaRetryVariables() {
        Log.i(this.LOG_TAG, "clear oma retry variables");
        this.mLastFailedAPI = null;
        this.mControllerOfLastFailedAPI = null;
        this.mApiFailCount = 0;
        this.mStoreClient.getPrerenceManager().saveOmaRetryCounter(this.mApiFailCount);
    }

    public void clearOmaRetryData() {
        clearOmaRetryVariables();
    }

    private void initMessageAttributeRegistration() {
        HashMap hashMap = new HashMap();
        this.mMessageAttributeRegistration = hashMap;
        hashMap.put(IMessageAttributeInterface.P_ASSERTED_SERVICE, KorAttributeNames.p_asserted_service);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGE_CONTEXT, AttributeNames.message_context);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DIRECTION, "Direction");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.FROM, AttributeNames.from);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TO, AttributeNames.to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DATE, "Date");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTENT_TYPE, "Content-Type");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TEXT_CONTENT, AttributeNames.textcontent);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONVERSATION_ID, KorAttributeNames.conversation_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.SUBJECT, AttributeNames.subject);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.EXTENDED_RCS, KorAttributeNames.extended_rcs);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGEBODY, AttributeNames.message_body);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CHIPLIST, AttributeNames.chip_list);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TRAFFICTYPE, "Traffic-Type");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.SAFETY, KorAttributeNames.safety);
        this.mMessageAttributeRegistration = Collections.unmodifiableMap(this.mMessageAttributeRegistration);
    }

    public static int getRCSMessageType(int i) {
        if (DefaultCloudMessageStrategy.mMessageTypeMapping.containsKey(Integer.valueOf(i))) {
            return DefaultCloudMessageStrategy.mMessageTypeMapping.get(Integer.valueOf(i)).intValue();
        }
        return -1;
    }

    private void initOMASuccessfulCallFlowTranslator() {
        initOmaSuccessCommonFlow();
    }

    public boolean handleNCCommonError(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i, int i2) {
        String str = this.LOG_TAG;
        Log.d(str, " handleNCCommonError api : " + iHttpAPICommonInterface.getClass().getSimpleName() + " statusCode: " + i + " retryAfter " + i2);
        if ((i < 500 || i >= 600) && i != 429) {
            if (i != 401) {
                return true;
            }
            iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface);
            return true;
        } else if (!this.mStoreClient.getMcsRetryMapAdapter().checkAndIncreaseRetry(iHttpAPICommonInterface, i)) {
            return false;
        } else {
            if (i == 429) {
                i2 = ImsUtil.getRandom().nextInt(ImSessionEvent.ADD_PARTICIPANTS) + 1000;
            }
            iAPICallFlowListener.onOverRequest(iHttpAPICommonInterface, String.valueOf(i), i2);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean retryIfAvailable(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        String str = this.LOG_TAG;
        IMSLog.i(str, " retryIfAvailable : request " + iHttpAPICommonInterface.getClass().getSimpleName() + "  error code " + i);
        int retryCount = this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(iHttpAPICommonInterface.getClass().getSimpleName());
        if (!this.mStoreClient.getMcsRetryMapAdapter().checkAndIncreaseRetry(iHttpAPICommonInterface, i)) {
            return true;
        }
        if (i == 429) {
            iAPICallFlowListener.onOverRequest(iHttpAPICommonInterface, String.valueOf(i), ImsUtil.getRandom().nextInt(ImSessionEvent.ADD_PARTICIPANTS) + 1000);
            return false;
        }
        int adaptedRetrySchedule = getAdaptedRetrySchedule(retryCount);
        String str2 = this.LOG_TAG;
        Log.d(str2, " retry " + iHttpAPICommonInterface.getClass().getSimpleName() + " after " + adaptedRetrySchedule);
        iAPICallFlowListener.onOverRequest(iHttpAPICommonInterface, String.valueOf(i), adaptedRetrySchedule);
        return false;
    }

    public int getAdaptedRetrySchedule(int i) {
        Map<Integer, Integer> map = this.mStandardRetrySchedule;
        if (map == null) {
            return 0;
        }
        int intValue = map.get(Integer.valueOf(i)).intValue();
        if (i == 0) {
            return intValue + ((ImsUtil.getRandom().nextInt(61) + 0) * 1000);
        }
        if (i == 1 || i == 2 || i == 3) {
            return (((int) Math.floor((double) (((float) intValue) * (ImsUtil.getRandom().nextFloat() + 1.0f)))) / 1000) * 1000;
        }
        return intValue;
    }
}

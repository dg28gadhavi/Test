package com.sec.internal.ims.cmstore.strategy;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.log.IMSLog;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TMOCmStrategy extends DefaultCloudMessageStrategy {
    private String MSTORE_API_VERSION = "v1";
    private String MSTORE_SERVERROOT = "";
    private String MSTORE_STORE_NAME = "ums";
    private String TAG = TMOCmStrategy.class.getSimpleName();
    private String WSG_URI = "";
    private boolean mPendingRequestVVMNotified = false;

    public static class TmoAttributeNames extends AttributeNames {
        public static String Content_Duration = "Content-Duration";
        public static String EmailAddress = "EmailAddress";
        public static String IMPORTANCE = "Importance";
        public static String NUT = "NUT";
        public static String SENSITIVITY = "Sensitivity";
        public static String V2T_EMAIL = "V2E_ON";
        public static String V2T_LANGUAGE = "V2t_Language";
        public static String V2T_RES = "resourceURL";
        public static String V2T_SMS = "SMSDirectLink";
        public static String VVMOn = "VVMOn";
        public static String call_disposition = "CallDisposition";
        public static String call_duration = "Call-Duration";
        public static String call_starttimestamp = "call-timestamp";
        public static String call_type = "Call-Type";
        public static String content_type = "Content-Type";
        public static String contribution_id = "Contribution-ID";
        public static String conversation_id = "Conversation-ID";
        public static String disposition_original_message_iD = "DispositionOriginalMessageID";
        public static String disposition_original_to = "DispositionOriginalTo";
        public static String disposition_status = "DispositionStatus";
        public static String disposition_type = "DispositionType";
        public static String inreplyto_contribution_Id = "InReplyTo-Contribution-ID";
        public static String is_cpm_group = "Is-CPM-Group";
        public static String message_id = "Message-Id";
        public static String mime_version = "MIME-Version";
        public static String multipartContentType = "MultipartContentType";
        public static String old_pwd = "OLD_PWD";
        public static String participating_device = "participating-device";
        public static String pwd = "PWD";
        public static String udh = "UDH";
        public static String x_cns_greeting_type = "X-CNS-Greeting-Type";
    }

    public static class TmoHttpHeaderValues {
        public static String DEVICE_ID_VALUE = "";
        public static final String GBA = "3gpp-gba";
        public static final String USER_AGENT_ID = "T-Mobile P20";
        public static String USER_AGENT_ID_VALUE = "";
    }

    public String getValidTokenByLine(String str) {
        return null;
    }

    public boolean isGbaSupported() {
        return true;
    }

    public boolean requiresMsgUploadInInitSync() {
        return false;
    }

    public boolean shouldEnableNetAPIWorking(boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return z;
    }

    TMOCmStrategy(MessageStoreClient messageStoreClient) {
        super(messageStoreClient);
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.d(str, "TMOCmStrategy");
        this.mStrategyType = DefaultCloudMessageStrategy.CmStrategyType.TMOUS;
        this.mProtocol = OMAGlobalVariables.HTTPS;
        this.mMaxSearch = 100;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mDateFormat = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        onOmaFlowInitStart();
        initSuccessfullCallFlowTranslator();
        initFailedCallFlowTranslator();
        onOmaFlowInitComplete();
        initStandardRetrySchedule();
        initMessageAttributeRegistration();
        getDeviceId();
        this.mMaxSearch = 100;
    }

    private void initSuccessfullCallFlowTranslator() {
        initOmaSuccessCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateAllObjects.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.VVM_CHANGE_SUCCEED.getId()));
        Map<OmaErrorKey, Integer> map = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey = new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType = OMASyncEventType.UPDATE_ONE_SUCCESSFUL;
        map.put(omaErrorKey, Integer.valueOf(oMASyncEventType.getId()));
        Map<OmaErrorKey, Integer> map2 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey2 = new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), VvmHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType2 = OMASyncEventType.UPLOAD_GREETING;
        map2.put(omaErrorKey2, Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType2.getId()));
    }

    private void initFailedCallFlowTranslator() {
        initOmaFailureCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(401, CloudMessageCreateAllObjects.class.getSimpleName(), VvmHandler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CREDENTIAL_EXPIRED.getId()));
    }

    private void initStandardRetrySchedule() {
        HashMap hashMap = new HashMap();
        this.mScheduledTimer = hashMap;
        hashMap.put(408, 30000);
        this.mScheduledTimer.put(Integer.valueOf(Id.REQUEST_IM_SENDMSG), 30000);
        this.mScheduledTimer.put(Integer.valueOf(OMAGlobalVariables.HTTP_NETWORK_CONNECT_TIMEOUT), 30000);
        this.mScheduledTimer.put(500, 10000);
        this.mScheduledTimer.put(501, 10000);
        this.mScheduledTimer.put(503, 10000);
        this.mScheduledTimer = Collections.unmodifiableMap(this.mScheduledTimer);
    }

    public boolean isRetryRequired(int i) {
        return this.mScheduledTimer.containsKey(Integer.valueOf(i));
    }

    public long getTimerValue(int i) {
        if (this.mScheduledTimer.containsKey(Integer.valueOf(i))) {
            return (long) this.mScheduledTimer.get(Integer.valueOf(i)).intValue();
        }
        Log.i(this.TAG, "getTimerValue not found");
        return -1;
    }

    private void initMessageAttributeRegistration() {
        HashMap hashMap = new HashMap();
        this.mMessageAttributeRegistration = hashMap;
        hashMap.put(IMessageAttributeInterface.DATE, "Date");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGE_CONTEXT, AttributeNames.message_context);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DIRECTION, "Direction");
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.FROM, AttributeNames.from);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TO, AttributeNames.to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.BCC, AttributeNames.bcc);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CC, AttributeNames.cc);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.SUBJECT, AttributeNames.subject);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_CPM_GROUP, TmoAttributeNames.is_cpm_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TEXT_CONTENT, AttributeNames.textcontent);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTRIBUTION_ID, TmoAttributeNames.contribution_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONVERSATION_ID, TmoAttributeNames.conversation_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IN_REPLY_TO_CONTRIBUTION_ID, TmoAttributeNames.inreplyto_contribution_Id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.UDH, TmoAttributeNames.udh);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DISPOSITION, TmoAttributeNames.call_disposition);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DURATION, TmoAttributeNames.call_duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_STARTTIMESTAMP, TmoAttributeNames.call_starttimestamp);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_TYPE, TmoAttributeNames.call_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.PARTICIPATING_DEVICE, TmoAttributeNames.participating_device);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_TYPE, TmoAttributeNames.disposition_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_STATUS, TmoAttributeNames.disposition_status);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID, TmoAttributeNames.disposition_original_message_iD);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO, TmoAttributeNames.disposition_original_to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MULTIPARTCONTENTTYPE, TmoAttributeNames.multipartContentType);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MESSAGE_ID, TmoAttributeNames.message_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTENT_TYPE, TmoAttributeNames.content_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MIME_VERSION, TmoAttributeNames.mime_version);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.PWD, TmoAttributeNames.pwd);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.OLD_PWD, TmoAttributeNames.old_pwd);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.X_CNS_GREETING_TYPE, TmoAttributeNames.x_cns_greeting_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTENT_DURATION, TmoAttributeNames.Content_Duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.EMAILADDRESS, TmoAttributeNames.EmailAddress);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.VVMOn, TmoAttributeNames.VVMOn);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.NUT, TmoAttributeNames.NUT);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IMPORTANCE, TmoAttributeNames.IMPORTANCE);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.SENSITIVITY, TmoAttributeNames.SENSITIVITY);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.V2T_LANGUAGE, TmoAttributeNames.V2T_LANGUAGE);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.V2T_RES, TmoAttributeNames.V2T_RES);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.V2T_SMS, TmoAttributeNames.V2T_SMS);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.V2T_EMAIL, TmoAttributeNames.V2T_EMAIL);
        this.mMessageAttributeRegistration = Collections.unmodifiableMap(this.mMessageAttributeRegistration);
    }

    public String getNmsHost() {
        return this.MSTORE_SERVERROOT;
    }

    public String getOMAApiVersion() {
        return this.MSTORE_API_VERSION;
    }

    public String getStoreName() {
        return this.MSTORE_STORE_NAME;
    }

    public int getTypeUsingMessageContext(String str) {
        String str2 = this.TAG;
        Log.d(str2, "getTypeUsingMessageContext value: " + str);
        if (str.equals(MessageContextValues.pagerMessage)) {
            return 3;
        }
        if (str.equals("multimedia-message")) {
            return 4;
        }
        if (str.equals(TMOConstants.TmoMessageContextValues.chatMessage)) {
            return 11;
        }
        if (str.equals(TMOConstants.TmoMessageContextValues.fileMessage)) {
            return 12;
        }
        if (str.equals(TMOConstants.TmoMessageContextValues.standaloneMessagePager)) {
            return 11;
        }
        if (str.equals(TMOConstants.TmoMessageContextValues.standaloneMessageLLM)) {
            return 12;
        }
        if (str.equals("imdn-message")) {
            return 13;
        }
        if (str.equals(MessageContextValues.voiceMessage)) {
            return 17;
        }
        if (str.equals(TMOConstants.TmoMessageContextValues.greetingvoice)) {
            return 18;
        }
        return (str.equals(TMOConstants.TmoMessageContextValues.gsomessage) || str.equals(TMOConstants.TmoMessageContextValues.gsosession)) ? 34 : 0;
    }

    public void setDeviceConfigUsed(Map<String, String> map) {
        String str = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL);
        Log.i(this.TAG, "setDeviceConfigUsed " + IMSLog.checker(str));
        if (!TextUtils.isEmpty(str)) {
            try {
                String substring = str.substring(new URL(str).getProtocol().length() + 3);
                this.MSTORE_SERVERROOT = substring;
                Log.i(this.TAG, substring);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        String str2 = "";
        if (map.containsKey(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI)) {
            this.WSG_URI = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI);
            str2 = str2 + " WSG_URI: " + this.WSG_URI;
        }
        String str3 = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_GREETINGS);
        if (!TextUtils.isEmpty(str3)) {
            TMOVariables.TmoMessageFolderId.mVVMailGreeting = str3;
            str2 = str2 + " mVVMailGreeting : " + str3;
        }
        String str4 = map.get(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_INBOX);
        if (!TextUtils.isEmpty(str4)) {
            TMOVariables.TmoMessageFolderId.mVVMailInbox = str4;
            str2 = str2 + " mVVMailInbox : " + str4;
        }
        Log.i(this.TAG, "TmoMessageFolderId values: " + str2);
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyDiffFromCommonRuleByCode(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        if (i != 401) {
            return false;
        }
        String str = this.TAG;
        Log.i(str, "API[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], 401, CREDENTIAL_EXPIRED");
        iAPICallFlowListener.onFailedEvent(OMASyncEventType.CREDENTIAL_EXPIRED.getId(), (Object) null);
        return true;
    }

    public boolean shouldEnableNetAPIPutFlag(String str) {
        return !CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(str);
    }

    public boolean isValidOMARequestUrl() {
        if (!TextUtils.isEmpty(this.MSTORE_API_VERSION) && !TextUtils.isEmpty(this.MSTORE_STORE_NAME) && !TextUtils.isEmpty(this.MSTORE_SERVERROOT)) {
            return true;
        }
        Log.i(this.TAG, "isValidOMARequestUrl: false");
        return false;
    }

    private void getDeviceId() {
        TmoHttpHeaderValues.DEVICE_ID_VALUE = Util.getImei(this.mStoreClient);
    }

    private void updateUserAgentIDHeader() {
        TmoHttpHeaderValues.USER_AGENT_ID_VALUE = TmoHttpHeaderValues.USER_AGENT_ID + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public void updateHTTPHeader() {
        updateUserAgentIDHeader();
    }

    public void setVVMPendingRequestCounts(boolean z) {
        Log.i(this.TAG, "setVVMPendingRequestCounts pendingRequestVVMNotified: " + this.mPendingRequestVVMNotified + ", mVVMPendingRequestCount: " + this.mVVMPendingRequestCount);
        if (z) {
            this.mVVMPendingRequestCount++;
        } else {
            this.mVVMPendingRequestCount--;
        }
        int i = this.mVVMPendingRequestCount;
        if (i == 1 && !this.mPendingRequestVVMNotified) {
            this.mPendingRequestVVMNotified = true;
            this.mStoreClient.notifyAppNetworkOperationResult(true);
        } else if (i == 0 && this.mPendingRequestVVMNotified) {
            this.mPendingRequestVVMNotified = false;
            this.mStoreClient.notifyAppNetworkOperationResult(false);
        }
    }

    public void resetVVMPendingRequestCount() {
        this.mVVMPendingRequestCount = 0;
        this.mPendingRequestVVMNotified = false;
    }

    public boolean getVVMAutoDownloadSetting() {
        return this.mAutoDownload;
    }

    public void setVVMAutoDownloadSetting(boolean z) {
        this.mAutoDownload = z;
    }
}

package com.sec.internal.ims.cmstore.strategy;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccountEligibility;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestHUIToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorMsg;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorType;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessfulCallHandling;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetActiveNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageUpdateNotificationChannelLifeTime;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ATTCmStrategy extends DefaultCloudMessageStrategy {
    private final String ATT_API_VERSION = "v1";
    private final String ATT_STORE_NAME = "base";
    private String LOG_TAG = ATTCmStrategy.class.getSimpleName();
    private int mApiFailCount = 0;
    private IControllerCommonInterface mControllerOfLastFailedAPI = null;
    private Class<? extends IHttpAPICommonInterface> mLastFailedAPI = null;

    public static class ATTAttributeNames extends AttributeNames {
        public static String call_disposition = "CallDisposition";
        public static String call_duration = "CallDuration";
        public static String contribution_id = "Contribution-ID";
        public static String conversation_id = "Conversation-ID";
        public static String disposition_original_message_iD = "DispositionOriginalMessageID";
        public static String disposition_original_to = "DispositionOriginalTo";
        public static String disposition_status = "DispositionStatus";
        public static String disposition_type = "DispositionType";
        public static String inreplyto_contribution_Id = "InReplyTo-Contribution-ID";
        public static String is_cpm_group = "Is-CPM-Group";
        public static String is_open_group = "Is-OPEN-Group";
        public static String multipartContentType = "MultipartContentType";
        public static String udh = "UDH";
    }

    public String getOMAApiVersion() {
        return "v1";
    }

    public String getStoreName() {
        return "base";
    }

    /* access modifiers changed from: protected */
    public void initOmaFailureCommonFlow() {
    }

    public boolean shouldEnableNetAPIWorking(boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return z && z2 && !z3 && z4 && !z5;
    }

    public boolean shouldStopSendingAPIwhenNetworklost() {
        return true;
    }

    ATTCmStrategy(MessageStoreClient messageStoreClient) {
        super(messageStoreClient);
        String str = this.LOG_TAG + "[" + messageStoreClient.getClientID() + "]";
        this.LOG_TAG = str;
        Log.d(str, "ATTCmStrategy");
        this.mStrategyType = DefaultCloudMessageStrategy.CmStrategyType.ATT;
        this.mProtocol = OMAGlobalVariables.HTTPS;
        this.mContentType = "application/json";
        this.mNotificationFormat = NotificationFormat.JSON;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mDateFormat = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        onOmaFlowInitStart();
        initSuccessfulCallFlowTranslator();
        initFailedCallFlowTranslator();
        onOmaFlowInitComplete();
        initStandardRetrySchedule();
        initMessageAttributeRegistration();
        initOmaRetryVariables();
    }

    public String getValidTokenByLine(String str) {
        return "Bearer PAT_" + this.mStoreClient.getPrerenceManager().getValidPAT();
    }

    public int getAdaptedRetrySchedule(int i) {
        Map<Integer, Integer> map = this.mStandardRetrySchedule;
        if (map == null) {
            return 0;
        }
        if (DebugFlag.DEBUG_RETRY_TIMELINE_FLAG) {
            return DebugFlag.getRetryTimeLine(i);
        }
        int intValue = map.get(Integer.valueOf(i)).intValue();
        if (i == 0) {
            return intValue + ((ImsUtil.getRandom().nextInt(61) + 0) * 1000);
        }
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            return (((int) Math.floor((double) (((float) intValue) * (ImsUtil.getRandom().nextFloat() + 1.0f)))) / 1000) * 1000;
        }
        return intValue;
    }

    private void initOmaRetryVariables() {
        this.mApiFailCount = this.mStoreClient.getPrerenceManager().getOmaRetryCounter();
        String str = this.LOG_TAG;
        Log.i(str, "OMA fail count is: " + this.mApiFailCount);
    }

    private void initSuccessfulCallFlowTranslator() {
        initProvisionSuccessfullCallFlowTranslator();
        initOMASuccessfulCallFlowTranslator();
    }

    private void initProvisionSuccessfullCallFlowTranslator() {
        this.mSuccessfullCallFlowTranslator = new HashMap();
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_SESSION_GEN));
        this.mSuccessfullCallFlowTranslator.put(ReqToken.class, arrayList);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY));
        EnumProvision.ProvisionEventType provisionEventType = EnumProvision.ProvisionEventType.REQ_HUI_TOKEN;
        arrayList2.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_STEADY_STATE_REQ_HUIMSTOKEN, provisionEventType));
        arrayList2.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_DELETE_ACCOUNT, EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT));
        EnumProvision.ProvisionEventType provisionEventType2 = EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT;
        arrayList2.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_CREATE_ACCOUNT, provisionEventType2));
        EnumProvision.ProvisionEventType provisionEventType3 = EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT;
        arrayList2.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_GET_SVC_ACCOUNT, provisionEventType3));
        arrayList2.add(new SuccessCallFlow(ReqConstant.HAPPY_PATH_GET_TC, EnumProvision.ProvisionEventType.REQ_GET_TC));
        this.mSuccessfullCallFlowTranslator.put(ReqSession.class, arrayList2);
        ArrayList arrayList3 = new ArrayList();
        arrayList3.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, provisionEventType3));
        this.mSuccessfullCallFlowTranslator.put(RequestAccountEligibility.class, arrayList3);
        ArrayList arrayList4 = new ArrayList();
        arrayList4.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, provisionEventType2));
        this.mSuccessfullCallFlowTranslator.put(RequestTC.class, arrayList4);
        ArrayList arrayList5 = new ArrayList();
        arrayList5.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, provisionEventType));
        this.mSuccessfullCallFlowTranslator.put(RequestCreateAccount.class, arrayList5);
        ArrayList arrayList6 = new ArrayList();
        arrayList6.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.REQ_PAT));
        this.mSuccessfullCallFlowTranslator.put(RequestHUIToken.class, arrayList6);
        ArrayList arrayList7 = new ArrayList();
        arrayList7.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.READY_PAT));
        this.mSuccessfullCallFlowTranslator.put(RequestPat.class, arrayList7);
        ArrayList arrayList8 = new ArrayList();
        arrayList8.add(new SuccessCallFlow(SuccessfulCallHandling.HAPPY_PATH_DEFAULT, EnumProvision.ProvisionEventType.DELETE_ACCOUNT_SUCCESS));
        this.mSuccessfullCallFlowTranslator.put(RequestDeleteAccount.class, arrayList8);
        this.mSuccessfullCallFlowTranslator = Collections.unmodifiableMap(this.mSuccessfullCallFlowTranslator);
    }

    private void initOMASuccessfulCallFlowTranslator() {
        initOmaSuccessCommonFlow();
        Map<OmaErrorKey, Integer> map = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey = new OmaErrorKey(200, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName());
        OMASyncEventType oMASyncEventType = OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING;
        map.put(omaErrorKey, Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateNotificationChannels.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualNotificationChannelInfo.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageUpdateNotificationChannelLifeTime.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId()));
    }

    private void initFailedCallFlowTranslator() {
        initProvisionFailedCallFlowTranslator();
        initOMAFailedCallFlowTranslator();
    }

    private void initProvisionFailedCallFlowTranslator() {
        HashMap hashMap = new HashMap();
        this.sErrorMsgsTranslator = hashMap;
        this.sErrorMsgsTranslator = Collections.unmodifiableMap(hashMap);
        this.mFailedCallFlowTranslator = new HashMap();
        ArrayList arrayList = new ArrayList();
        this.mFailedCallFlowTranslator.put(ReqZCode.class, arrayList);
        ErrorMsg errorMsg = new ErrorMsg(ErrorType.PROVISIONING, 0);
        ErrorRule.RetryAttribute retryAttribute = ErrorRule.RetryAttribute.RETRY_FORBIDDEN;
        EnumProvision.ProvisionEventType provisionEventType = EnumProvision.ProvisionEventType.ZCODE_ERROR_201;
        arrayList.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_201, retryAttribute, provisionEventType.getId(), provisionEventType.getId(), errorMsg));
        ErrorRule.RetryAttribute retryAttribute2 = ErrorRule.RetryAttribute.RETRY_ALLOW;
        int id = EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId();
        EnumProvision.ProvisionEventType provisionEventType2 = EnumProvision.ProvisionEventType.AUTH_ERR;
        arrayList.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute2, id, provisionEventType2.getId(), (ErrorMsg) null));
        ErrorRule.RetryAttribute retryAttribute3 = ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE;
        EnumProvision.ProvisionEventType provisionEventType3 = EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE;
        arrayList.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, provisionEventType3.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        ArrayList arrayList2 = new ArrayList();
        this.mFailedCallFlowTranslator.put(ReqToken.class, arrayList2);
        arrayList2.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute2, provisionEventType3.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        arrayList2.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_ATS_TOKEN.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        ArrayList arrayList3 = new ArrayList();
        this.mFailedCallFlowTranslator.put(ReqSession.class, arrayList3);
        EnumProvision.ProvisionEventType provisionEventType4 = EnumProvision.ProvisionEventType.REQ_SESSION_GEN;
        ErrorRule.RetryAttribute retryAttribute4 = retryAttribute2;
        arrayList3.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute4, provisionEventType4.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        arrayList3.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_201, retryAttribute4, provisionEventType3.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        arrayList3.add(new ErrorRule(ATTConstants.ATTErrorNames.ERROR_CODE_202, retryAttribute4, provisionEventType3.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        arrayList3.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, provisionEventType4.getId(), provisionEventType2.getId(), (ErrorMsg) null));
        ArrayList arrayList4 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestAccountEligibility.class, arrayList4);
        ErrorType errorType = ErrorType.PROVISIONING_BLOCKED;
        ErrorMsg errorMsg2 = new ErrorMsg(errorType, 0);
        int id2 = provisionEventType4.getId();
        EnumProvision.ProvisionEventType provisionEventType5 = EnumProvision.ProvisionEventType.PROVISION_ERR;
        arrayList4.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, retryAttribute2, id2, provisionEventType5.getId(), (ErrorMsg) null));
        EnumProvision.ProvisionEventType provisionEventType6 = EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN;
        ErrorRule.RetryAttribute retryAttribute5 = retryAttribute;
        ErrorType errorType2 = errorType;
        ErrorMsg errorMsg3 = errorMsg2;
        arrayList4.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, retryAttribute5, -1, provisionEventType6.getId(), errorMsg3));
        arrayList4.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_ACCOUNT_NOT_ELIGIBLE, retryAttribute5, -1, EnumProvision.ProvisionEventType.ACCOUNT_NOT_ELIGIBLE.getId(), errorMsg3));
        EnumProvision.ProvisionEventType provisionEventType7 = EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY;
        arrayList4.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, provisionEventType7.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        ArrayList arrayList5 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestAccount.class, arrayList5);
        ErrorMsg errorMsg4 = new ErrorMsg(errorType2, 0);
        ErrorRule.RetryAttribute retryAttribute6 = retryAttribute2;
        arrayList5.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, retryAttribute6, provisionEventType4.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList5.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, retryAttribute5, -1, provisionEventType6.getId(), errorMsg4));
        arrayList5.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute6, provisionEventType4.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList5.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        ArrayList arrayList6 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestHUIToken.class, arrayList6);
        int id3 = provisionEventType7.getId();
        EnumProvision.ProvisionEventType provisionEventType8 = EnumProvision.ProvisionEventType.ACCESS_ERR;
        ErrorRule.RetryAttribute retryAttribute7 = retryAttribute2;
        arrayList6.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED, retryAttribute7, id3, provisionEventType8.getId(), (ErrorMsg) null));
        arrayList6.add(new ErrorRule(ATTConstants.ATTErrorNames.LAST_RETRY_CREATE_ACCOUNT, retryAttribute7, EnumProvision.ProvisionEventType.LAST_RETRY_CREATE_ACCOUNT.getId(), provisionEventType8.getId(), (ErrorMsg) null));
        arrayList6.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_HUI_JSON, retryAttribute7, provisionEventType4.getId(), provisionEventType8.getId(), (ErrorMsg) null));
        arrayList6.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute7, provisionEventType4.getId(), provisionEventType8.getId(), (ErrorMsg) null));
        arrayList6.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId(), provisionEventType8.getId(), (ErrorMsg) null));
        ArrayList arrayList7 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestTC.class, arrayList7);
        arrayList7.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, retryAttribute2, provisionEventType4.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList7.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN, retryAttribute5, provisionEventType6.getId(), -1, (ErrorMsg) null));
        EnumProvision.ProvisionEventType provisionEventType9 = EnumProvision.ProvisionEventType.REQ_GET_TC;
        arrayList7.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, provisionEventType9.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        ArrayList arrayList8 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestCreateAccount.class, arrayList8);
        ErrorRule.RetryAttribute retryAttribute8 = retryAttribute2;
        arrayList8.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_TC_ERROR_1007, retryAttribute8, provisionEventType9.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList8.add(new ErrorRule(ATTConstants.ATTErrorNames.CPS_TC_ERROR_1008, retryAttribute8, provisionEventType9.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList8.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT, retryAttribute8, provisionEventType4.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList8.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        ArrayList arrayList9 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestDeleteAccount.class, arrayList9);
        int ordinal = provisionEventType4.ordinal();
        EnumProvision.ProvisionEventType provisionEventType10 = EnumProvision.ProvisionEventType.STOP_BACKUP_ERR;
        arrayList9.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute2, ordinal, provisionEventType10.getId(), (ErrorMsg) null));
        arrayList9.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId(), provisionEventType10.getId(), (ErrorMsg) null));
        ArrayList arrayList10 = new ArrayList();
        this.mFailedCallFlowTranslator.put(RequestPat.class, arrayList10);
        ErrorRule.RetryAttribute retryAttribute9 = retryAttribute2;
        arrayList10.add(new ErrorRule(CommonErrorName.DEFAULT_ERROR_TYPE, retryAttribute9, provisionEventType4.ordinal(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList10.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_SESSION_ID, retryAttribute9, provisionEventType4.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        arrayList10.add(new ErrorRule(ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAttribute3, EnumProvision.ProvisionEventType.REQ_PAT.getId(), provisionEventType5.getId(), (ErrorMsg) null));
        this.mFailedCallFlowTranslator = Collections.unmodifiableMap(this.mFailedCallFlowTranslator);
    }

    private void initOMAFailedCallFlowTranslator() {
        initOmaFailureCommonFlow();
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(RegistrationEvents.EVENT_UPDATE_REGI_CONFIG, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(302, CloudMessageCreateLongPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.getId()));
        Map<OmaErrorKey, Integer> map = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey = new OmaErrorKey(404, CloudMessageGetIndividualNotificationChannelInfo.class.getSimpleName(), Handler.class.getSimpleName());
        OMASyncEventType oMASyncEventType = OMASyncEventType.CREATE_NOTIFICATION_CHANNEL;
        map.put(omaErrorKey, Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetActiveNotificationChannels.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageCreateLargeDataPolling.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.DEFAULT.getId()));
    }

    private void initStandardRetrySchedule() {
        HashMap hashMap = new HashMap();
        this.mStandardRetrySchedule = hashMap;
        if (DebugFlag.DEBUG_RETRY_TIMELINE_FLAG) {
            hashMap.put(0, 0);
            this.mStandardRetrySchedule.put(1, 5000);
            this.mStandardRetrySchedule.put(2, 10001);
            this.mStandardRetrySchedule.put(3, 10002);
            this.mStandardRetrySchedule.put(4, 10003);
            this.mStandardRetrySchedule.put(5, 10004);
        } else {
            hashMap.put(0, 0);
            this.mStandardRetrySchedule.put(1, 300000);
            this.mStandardRetrySchedule.put(2, 1800000);
            this.mStandardRetrySchedule.put(3, 14400000);
            this.mStandardRetrySchedule.put(4, 43200000);
            this.mStandardRetrySchedule.put(5, 86400000);
        }
        this.mStandardRetrySchedule = Collections.unmodifiableMap(this.mStandardRetrySchedule);
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
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_CPM_GROUP, ATTAttributeNames.is_cpm_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IS_OPEN_GROUP, ATTAttributeNames.is_open_group);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.TEXT_CONTENT, AttributeNames.textcontent);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONTRIBUTION_ID, ATTAttributeNames.contribution_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CONVERSATION_ID, ATTAttributeNames.conversation_id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.IN_REPLY_TO_CONTRIBUTION_ID, ATTAttributeNames.inreplyto_contribution_Id);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.UDH, ATTAttributeNames.udh);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DISPOSITION, ATTAttributeNames.call_disposition);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.CALL_DURATION, ATTAttributeNames.call_duration);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_TYPE, ATTAttributeNames.disposition_type);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_STATUS, ATTAttributeNames.disposition_status);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID, ATTAttributeNames.disposition_original_message_iD);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO, ATTAttributeNames.disposition_original_to);
        this.mMessageAttributeRegistration.put(IMessageAttributeInterface.MULTIPARTCONTENTTYPE, ATTAttributeNames.multipartContentType);
        this.mMessageAttributeRegistration = Collections.unmodifiableMap(this.mMessageAttributeRegistration);
    }

    public String getNmsHost() {
        if (!ATTGlobalVariables.isGcmReplacePolling()) {
            return this.mStoreClient.getPrerenceManager().getNmsHost();
        }
        String nmsHost = this.mStoreClient.getPrerenceManager().getNmsHost();
        String str = this.LOG_TAG;
        Log.d(str, "use host for gcm, NMS Host value=" + nmsHost);
        if (!TextUtils.isEmpty(nmsHost)) {
            return nmsHost;
        }
        String acsNmsHost = this.mStoreClient.getPrerenceManager().getAcsNmsHost();
        return TextUtils.isEmpty(acsNmsHost) ? ATTGlobalVariables.DEFAULT_NMS_HOST : acsNmsHost;
    }

    public String getNcHost() {
        if (!ATTGlobalVariables.isGcmReplacePolling()) {
            return this.mStoreClient.getPrerenceManager().getNcHost();
        }
        String ncHost = this.mStoreClient.getPrerenceManager().getNcHost();
        if (TextUtils.isEmpty(ncHost)) {
            ncHost = ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST;
        }
        String str = this.LOG_TAG;
        Log.d(str, "NC Host value=" + ncHost);
        return ncHost;
    }

    public String getNativeLine() {
        return this.mStoreClient.getPrerenceManager().getUserTelCtn();
    }

    public int getTypeUsingMessageContext(String str) {
        if (str.equals(MessageContextValues.pagerMessage)) {
            return 3;
        }
        if (str.equals("multimedia-message")) {
            return 4;
        }
        if (str.equals("chat-message")) {
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
        return str.equals(MessageContextValues.voiceMessage) ? 17 : 0;
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

    public void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface.getClass().equals(this.mLastFailedAPI)) {
            clearOmaRetryVariables();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyBreakCommonRule(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        Log.d(this.LOG_TAG, "isCarrierStrategyBreakCommonRule()");
        if ((iHttpAPICommonInterface instanceof CloudMessageDeleteIndividualSubscription) && i != 302) {
            return true;
        }
        if ((iHttpAPICommonInterface instanceof CloudMessageDeleteIndividualChannel) && i != 302) {
            return true;
        }
        if (!(iHttpAPICommonInterface instanceof CloudMessageCreateLargeDataPolling)) {
            return false;
        }
        Log.d(this.LOG_TAG, "CloudMessageCreateLargeDataPolling, other status code");
        return true;
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
}

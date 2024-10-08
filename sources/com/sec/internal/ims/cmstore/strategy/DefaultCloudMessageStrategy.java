package com.sec.internal.ims.cmstore.strategy;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorMsg;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorType;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.OmaErrorKey;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualPayLoad;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import com.sec.internal.omanetapi.nc.BaseNCRequest;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCloudMessageStrategy implements ICloudMessageStrategy, IMessageAttributeInterface {
    public static final int MAX_RETRY_COUNTER = 4;
    protected static Map<Integer, Integer> mMessageTypeMapping;
    private String LOG_TAG = DefaultCloudMessageStrategy.class.getSimpleName();
    protected boolean mAutoDownload = true;
    protected String mContentType;
    protected SimpleDateFormat mDateFormat;
    protected Map<Class<? extends HttpRequestParams>, List<ErrorRule>> mFailedCallFlowTranslator;
    protected int mMaxBulkOption;
    protected int mMaxRetryCounter = 4;
    protected int mMaxSearch = 10;
    protected Map<String, String> mMessageAttributeRegistration;
    protected NotificationFormat mNotificationFormat;
    protected Map<OmaErrorKey, Integer> mOmaCallFlowTranslator;
    protected String mProtocol;
    protected Map<Integer, Integer> mScheduledTimer;
    protected Map<Integer, Integer> mStandardRetrySchedule;
    protected MessageStoreClient mStoreClient;
    protected CmStrategyType mStrategyType = CmStrategyType.DEFAULT;
    protected Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> mSuccessfullCallFlowTranslator;
    protected int mVVMPendingRequestCount = 0;
    protected Map<ErrorType, ErrorMsg> sErrorMsgsTranslator;

    public void clearOmaRetryData() {
    }

    public String decrypt(String str, boolean z) {
        return "";
    }

    public int getAdaptedRetrySchedule(int i) {
        return 0;
    }

    public String getAuthorizationBasic() {
        return "";
    }

    public String getAuthorizationBearer() {
        return "";
    }

    public String getClientVersion() {
        return "";
    }

    public IControllerCommonInterface getControllerOfLastFailedApi() {
        return null;
    }

    public String getDeviceType() {
        return "";
    }

    public Class<? extends IHttpAPICommonInterface> getLastFailedApi() {
        return null;
    }

    public String getNcHost() {
        return "";
    }

    public String getNmsHost() {
        return "";
    }

    public String getOMAApiVersion() {
        return "";
    }

    public String getStoreName() {
        return "";
    }

    public long getTimerValue(int i) {
        return -1;
    }

    public int getTypeUsingMessageContext(String str) {
        return 0;
    }

    public String getValidTokenByLine(String str) {
        return null;
    }

    public boolean handleNCCommonError(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i, int i2) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyBreakCommonRule(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierStrategyDiffFromCommonRuleByCode(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        return false;
    }

    public boolean isEncrypted() {
        return false;
    }

    public boolean isErrorCodeSupported(int i, IHttpAPICommonInterface iHttpAPICommonInterface) {
        return true;
    }

    public boolean isGbaSupported() {
        return false;
    }

    public final boolean isRetryEnabled() {
        return true;
    }

    public boolean isRetryRequired(int i) {
        return false;
    }

    public boolean isValidOMARequestUrl() {
        return true;
    }

    public void onOmaApiCredentialFailed(IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public boolean requiresMsgUploadInInitSync() {
        return true;
    }

    public void resetVVMPendingRequestCount() {
    }

    public void setDeviceConfigUsed(Map<String, String> map) {
    }

    public final void setProtocol(String str) {
    }

    public void setVVMAutoDownloadSetting(boolean z) {
    }

    public void setVVMPendingRequestCounts(boolean z) {
    }

    public boolean shouldEnableNetAPIPutFlag(String str) {
        return true;
    }

    public boolean shouldEnableNetAPIWorking(boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return false;
    }

    public boolean shouldStopSendingAPIwhenNetworklost() {
        return false;
    }

    public void updateHTTPHeader() {
    }

    public static class NmsNotificationType {
        private int contractType;
        private String dataType;

        public NmsNotificationType(String str, int i) {
            setDataType(str);
            setContractType(i);
        }

        public String getDataType() {
            return this.dataType;
        }

        public void setDataType(String str) {
            this.dataType = str;
        }

        public int getContractType() {
            return this.contractType;
        }

        public void setContractType(int i) {
            this.contractType = i;
        }
    }

    DefaultCloudMessageStrategy(MessageStoreClient messageStoreClient) {
        String str = this.LOG_TAG + "[" + messageStoreClient.getClientID() + "]";
        this.LOG_TAG = str;
        Log.d(str, "DefaultCloudMessageStrategy");
        this.mStoreClient = messageStoreClient;
        this.mMaxBulkOption = 100;
    }

    public String getNativeLine() {
        return this.mStoreClient.getPrerenceManager().getUserTelCtn();
    }

    public boolean shouldCareAfterPreProcess(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i) {
        int statusCode = httpResponseParams.getStatusCode();
        String str = iHttpAPICommonInterface instanceof BaseNMSRequest ? "NMS" : "NC";
        String str2 = this.LOG_TAG;
        Log.i(str2, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], res code[" + statusCode + "]");
        if (isOmaErrorRuleMatch(statusCode, iHttpAPICommonInterface, iAPICallFlowListener, obj, i)) {
            String str3 = this.LOG_TAG;
            Log.i(str3, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], isOmaErrorRuleMatch");
            return false;
        } else if (isCarrierStrategyBreakCommonRule(iHttpAPICommonInterface, statusCode)) {
            String str4 = this.LOG_TAG;
            Log.i(str4, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], [" + statusCode + "] catch call");
            return true;
        } else if (!shouldCareAfterProcessOMACommonCase(iAPICallFlowListener, iHttpAPICommonInterface, httpResponseParams, bufferDBChangeParam)) {
            String str5 = this.LOG_TAG;
            Log.i(str5, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], match common cases");
            return false;
        } else {
            String str6 = this.LOG_TAG;
            Log.i(str6, str + "[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], [" + statusCode + "] catch call, return");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public final void onOmaFlowInitStart() {
        this.mOmaCallFlowTranslator = new HashMap();
    }

    /* access modifiers changed from: protected */
    public final void onOmaFlowInitComplete() {
        this.mOmaCallFlowTranslator = Collections.unmodifiableMap(this.mOmaCallFlowTranslator);
    }

    /* access modifiers changed from: protected */
    public final void initOmaSuccessCommonFlow() {
        Log.i(this.LOG_TAG, "init OMA success common flow");
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(201, CloudMessageCreateAllObjects.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId()));
        Map<OmaErrorKey, Integer> map = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey = new OmaErrorKey(200, CloudMessageGetIndividualObject.class.getSimpleName(), BaseSyncHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType = OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED;
        map.put(omaErrorKey, Integer.valueOf(oMASyncEventType.getId()));
        Map<OmaErrorKey, Integer> map2 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey2 = new OmaErrorKey(200, CloudMessageGetIndividualObject.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType2 = OMASyncEventType.DOWNLOAD_RETRIVED;
        map2.put(omaErrorKey2, Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetAllPayloads.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetAllPayloads.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType2.getId()));
        Map<OmaErrorKey, Integer> map3 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey3 = new OmaErrorKey(201, CloudMessagePutObjectFlag.class.getSimpleName(), Handler.class.getSimpleName());
        OMASyncEventType oMASyncEventType3 = OMASyncEventType.UPDATE_ONE_SUCCESSFUL;
        map3.put(omaErrorKey3, Integer.valueOf(oMASyncEventType3.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageDeleteObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType3.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageDeleteIndividualObject.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType3.getId()));
        Map<OmaErrorKey, Integer> map4 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey4 = new OmaErrorKey(200, CloudMessageObjectsOpSearch.class.getSimpleName(), Handler.class.getSimpleName());
        OMASyncEventType oMASyncEventType4 = OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE;
        map4.put(omaErrorKey4, Integer.valueOf(oMASyncEventType4.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageObjectsOpSearch.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType4.getId()));
        Map<OmaErrorKey, Integer> map5 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey5 = new OmaErrorKey(200, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType5 = OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL;
        map5.put(omaErrorKey5, Integer.valueOf(oMASyncEventType5.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType5.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(200, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType5.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(204, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType5.getId()));
    }

    /* access modifiers changed from: protected */
    public void initOmaFailureCommonFlow() {
        Log.i(this.LOG_TAG, "init OMA failure common flow");
        Map<OmaErrorKey, Integer> map = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey = new OmaErrorKey(404, CloudMessageGetIndividualObject.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType = OMASyncEventType.DOWNLOAD_RETRIVED;
        map.put(omaErrorKey, Integer.valueOf(oMASyncEventType.getId()));
        Map<OmaErrorKey, Integer> map2 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey2 = new OmaErrorKey(404, CloudMessageGetIndividualObject.class.getSimpleName(), BaseSyncHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType2 = OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED;
        map2.put(omaErrorKey2, Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetAllPayloads.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetAllPayloads.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseSyncHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType2.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageGetIndividualPayLoad.class.getSimpleName(), BaseDataChangeHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType.getId()));
        Map<OmaErrorKey, Integer> map3 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey3 = new OmaErrorKey(404, CloudMessagePutObjectFlag.class.getSimpleName(), Handler.class.getSimpleName());
        OMASyncEventType oMASyncEventType3 = OMASyncEventType.UPDATE_ONE_SUCCESSFUL;
        map3.put(omaErrorKey3, Integer.valueOf(oMASyncEventType3.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageDeleteObjectFlag.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType3.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(404, CloudMessageDeleteIndividualObject.class.getSimpleName(), Handler.class.getSimpleName()), Integer.valueOf(oMASyncEventType3.getId()));
        Map<OmaErrorKey, Integer> map4 = this.mOmaCallFlowTranslator;
        OmaErrorKey omaErrorKey4 = new OmaErrorKey(403, CloudMessageBulkUpdate.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName());
        OMASyncEventType oMASyncEventType4 = OMASyncEventType.FALLBACK_ONE_UPDATE_OR_DELETE;
        map4.put(omaErrorKey4, Integer.valueOf(oMASyncEventType4.getId()));
        this.mOmaCallFlowTranslator.put(new OmaErrorKey(403, CloudMessageBulkDeletion.class.getSimpleName(), BaseDeviceDataUpdateHandler.class.getSimpleName()), Integer.valueOf(oMASyncEventType4.getId()));
    }

    /* access modifiers changed from: protected */
    public boolean isFailedForOmaRetryAfter(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, BufferDBChangeParam bufferDBChangeParam) {
        List list = httpResponseParams.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER);
        if (list == null || list.size() <= 0) {
            return true;
        }
        String str = (String) list.get(0);
        String str2 = this.LOG_TAG;
        Log.i(str2, "retryAfterHeader: " + list.toString() + "API[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], retry after " + str + " seconds");
        try {
            int parseInt = Integer.parseInt(str);
            if (parseInt > 0) {
                iAPICallFlowListener.onOverRequest(iHttpAPICommonInterface, CommonErrorName.RETRY_HEADER, parseInt);
            } else if (bufferDBChangeParam != null) {
                iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
            }
        } catch (NumberFormatException e) {
            Log.e(this.LOG_TAG, e.getMessage());
            iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isFailedForLocationRedirect(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams) {
        List list = httpResponseParams.getHeaders().get("Location");
        String str = (list == null || list.size() <= 0) ? null : (String) list.get(0);
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        try {
            URL url = new URL(str);
            if (ATTGlobalVariables.isGcmReplacePolling()) {
                this.mStoreClient.getPrerenceManager().saveNmsHost(url.getHost());
                iHttpAPICommonInterface.updateServerRoot(getNmsHost());
            } else if (iHttpAPICommonInterface instanceof BaseNMSRequest) {
                this.mStoreClient.getPrerenceManager().saveNmsHost(url.getHost());
                iHttpAPICommonInterface.updateServerRoot(getNmsHost());
            } else if (iHttpAPICommonInterface instanceof BaseNCRequest) {
                this.mStoreClient.getPrerenceManager().saveNcHost(url.getHost());
                iHttpAPICommonInterface.updateServerRoot(getNcHost());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        iAPICallFlowListener.onFailedEvent(OMASyncEventType.MSTORE_REDIRECT.getId(), iHttpAPICommonInterface);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCareAfterProcessOMACommonCase(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, BufferDBChangeParam bufferDBChangeParam) {
        int statusCode = httpResponseParams.getStatusCode();
        if (statusCode >= 500 && statusCode != 503) {
            iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
            return false;
        } else if (isCarrierStrategyDiffFromCommonRuleByCode(iAPICallFlowListener, iHttpAPICommonInterface, statusCode)) {
            return false;
        } else {
            if (statusCode == 302) {
                String str = this.LOG_TAG;
                Log.i(str, "API[" + iHttpAPICommonInterface.getClass().getSimpleName() + "], 302");
                if (isFailedForLocationRedirect(iAPICallFlowListener, iHttpAPICommonInterface, httpResponseParams)) {
                    iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
                }
                return false;
            } else if (statusCode == 401) {
                iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
                return false;
            } else if (statusCode == 408) {
                iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
                return false;
            } else if (statusCode != 429 && statusCode != 503) {
                return true;
            } else {
                if (isFailedForOmaRetryAfter(iAPICallFlowListener, iHttpAPICommonInterface, httpResponseParams, bufferDBChangeParam)) {
                    iAPICallFlowListener.onFailedCall(iHttpAPICommonInterface, bufferDBChangeParam);
                }
                return false;
            }
        }
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

    protected enum CmStrategyType {
        DEFAULT,
        ATT,
        TMOUS,
        KOR;

        /* access modifiers changed from: protected */
        public boolean isOneOf(CmStrategyType... cmStrategyTypeArr) {
            for (CmStrategyType cmStrategyType : cmStrategyTypeArr) {
                if (this == cmStrategyType) {
                    return true;
                }
            }
            return false;
        }
    }

    public final Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator() {
        return this.mSuccessfullCallFlowTranslator;
    }

    public final Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator() {
        return this.mFailedCallFlowTranslator;
    }

    public final int getMaxRetryCounter() {
        return this.mMaxRetryCounter;
    }

    public final Map<String, String> getMessageAttributeRegistration() {
        return this.mMessageAttributeRegistration;
    }

    public final String getProtocol() {
        return this.mProtocol;
    }

    public final NotificationFormat getNotificaitonFormat() {
        return this.mNotificationFormat;
    }

    public final String getContentType() {
        return this.mContentType;
    }

    public final boolean isProvisionRequired() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isMultiLineSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isPollingAllowed() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isNmsEventHasMessageDetail() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isCaptivePortalCheckSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isGoForwardSyncSupported() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public String getSmsHashTagOrCorrelationTag(String str, int i, String str2) {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT, CmStrategyType.TMOUS)) {
            return AmbsUtils.generateSmsHashCode(str, i, str2, new String[]{":::", "::", ":::::"}, false);
        }
        return null;
    }

    public final boolean isEnableFolderIdInSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean getIsInitSyncIndicatorRequired() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isTokenRequestedFromProvision() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean requiresInterworkingCrossSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT, CmStrategyType.TMOUS);
    }

    public final boolean isBulkUpdateEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return this.mStrategyType.isOneOf(CmStrategyType.KOR);
    }

    public final boolean isBulkDeleteEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return true;
    }

    public final boolean isBulkCreationEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isPostMethodForBulkDelete() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT) || this.mStrategyType.isOneOf(CmStrategyType.KOR);
    }

    public int getMaxBulkOptionEntry() {
        return this.mMaxBulkOption;
    }

    public final int getMaxSearchEntry() {
        return this.mMaxSearch;
    }

    public final boolean isDeviceConfigUsed() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isUIButtonUsed() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isEnableATTHeader() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isEnableTMOHeader() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isNotifyAppOnUpdateCloudFail() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isAirplaneModeChangeHandled() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT, CmStrategyType.TMOUS);
    }

    public final boolean shouldSkipMessage(ParamOMAObject paramOMAObject) {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            String str = paramOMAObject.TEXT_CONTENT;
            String str2 = paramOMAObject.FROM;
            if (!(str == null || str2 == null)) {
                return ReqZCode.isSmsZCode(str, str2.replaceAll("[^0-9]+", ""));
            }
        }
        return false;
    }

    public final boolean shouldCorrectShortCode() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isAppTriggerMessageSearch() {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean needToHandleSimSwap() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean bulkOpTreatSuccessIndividualResponse(int i) {
        if (this.mStrategyType.isOneOf(CmStrategyType.TMOUS)) {
            return i == 403 || i == 404;
        }
        return false;
    }

    public final boolean bulkOpTreatSuccessRequestResponse(int i) {
        return this.mStrategyType.isOneOf(CmStrategyType.TMOUS) && i == 404;
    }

    public final boolean shouldSkipCmasSMS(String str) {
        return str == null || str.contains("#CMAS#");
    }

    public final boolean shouldPersistImsRegNum() {
        return !this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean isStoreImdnEnabled() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return this.mStrategyType.isOneOf(CmStrategyType.KOR);
    }

    public final boolean shouldClearCursorUponInitSyncDone() {
        return !this.mStrategyType.isOneOf(CmStrategyType.TMOUS);
    }

    public final boolean isNeedCheckBlockedNumberBeforeCopyRcsDb() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public final boolean shouldStopInitSyncUponLowMemory() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean shouldCareGroupChatAttribute() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return false;
    }

    public final boolean isSupportExpiredRule() {
        if (this.mStrategyType.isOneOf(CmStrategyType.ATT)) {
            return ATTGlobalVariables.isAmbsPhaseIV();
        }
        return this.mStrategyType.isOneOf(CmStrategyType.KOR);
    }

    public final boolean isTrIdCorrelationId() {
        return this.mStrategyType.isOneOf(CmStrategyType.ATT);
    }

    public SimpleDateFormat getDateFormat() {
        return this.mDateFormat;
    }

    public boolean getVVMAutoDownloadSetting() {
        return this.mAutoDownload;
    }
}

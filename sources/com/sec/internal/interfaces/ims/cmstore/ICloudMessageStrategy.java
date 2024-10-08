package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessCallFlow;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.omanetapi.common.data.NotificationFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public interface ICloudMessageStrategy {
    boolean bulkOpTreatSuccessIndividualResponse(int i);

    boolean bulkOpTreatSuccessRequestResponse(int i);

    void clearOmaRetryData();

    String decrypt(String str, boolean z);

    int getAdaptedRetrySchedule(int i);

    String getAuthorizationBasic();

    String getAuthorizationBearer();

    String getClientVersion();

    String getContentType();

    IControllerCommonInterface getControllerOfLastFailedApi();

    SimpleDateFormat getDateFormat();

    String getDeviceType();

    Map<Class<? extends HttpRequestParams>, List<ErrorRule>> getFailedCallFlowTranslator();

    boolean getIsInitSyncIndicatorRequired();

    Class<? extends IHttpAPICommonInterface> getLastFailedApi();

    int getMaxBulkOptionEntry();

    int getMaxRetryCounter();

    int getMaxSearchEntry();

    Map<String, String> getMessageAttributeRegistration();

    String getNativeLine();

    String getNcHost();

    String getNmsHost();

    NotificationFormat getNotificaitonFormat();

    String getOMAApiVersion();

    String getProtocol();

    String getSmsHashTagOrCorrelationTag(String str, int i, String str2);

    String getStoreName();

    Map<Class<? extends HttpRequestParams>, List<SuccessCallFlow>> getSuccessfullCallFlowTranslator();

    long getTimerValue(int i);

    int getTypeUsingMessageContext(String str);

    boolean getVVMAutoDownloadSetting();

    String getValidTokenByLine(String str);

    boolean handleNCCommonError(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i, int i2);

    boolean isAirplaneModeChangeHandled();

    boolean isAppTriggerMessageSearch();

    boolean isBulkCreationEnabled();

    boolean isBulkDeleteEnabled();

    boolean isBulkUpdateEnabled();

    boolean isCaptivePortalCheckSupported();

    boolean isDeviceConfigUsed();

    boolean isEnableATTHeader();

    boolean isEnableFolderIdInSearch();

    boolean isEnableTMOHeader();

    boolean isEncrypted();

    boolean isErrorCodeSupported(int i, IHttpAPICommonInterface iHttpAPICommonInterface);

    boolean isGbaSupported();

    boolean isGoForwardSyncSupported();

    boolean isMultiLineSupported();

    boolean isNeedCheckBlockedNumberBeforeCopyRcsDb();

    boolean isNmsEventHasMessageDetail();

    boolean isNotifyAppOnUpdateCloudFail();

    boolean isPollingAllowed();

    boolean isPostMethodForBulkDelete();

    boolean isProvisionRequired();

    boolean isRetryEnabled();

    boolean isRetryRequired(int i);

    boolean isStoreImdnEnabled();

    boolean isSupportExpiredRule();

    boolean isTokenRequestedFromProvision();

    boolean isTrIdCorrelationId();

    boolean isUIButtonUsed();

    boolean isValidOMARequestUrl();

    boolean needToHandleSimSwap();

    void onOmaApiCredentialFailed(IControllerCommonInterface iControllerCommonInterface, INetAPIEventListener iNetAPIEventListener, IHttpAPICommonInterface iHttpAPICommonInterface, int i);

    void onOmaSuccess(IHttpAPICommonInterface iHttpAPICommonInterface);

    boolean requiresInterworkingCrossSearch();

    boolean requiresMsgUploadInInitSync();

    void resetVVMPendingRequestCount();

    void setDeviceConfigUsed(Map<String, String> map);

    void setProtocol(String str);

    void setVVMAutoDownloadSetting(boolean z);

    void setVVMPendingRequestCounts(boolean z);

    boolean shouldCareAfterPreProcess(IAPICallFlowListener iAPICallFlowListener, IHttpAPICommonInterface iHttpAPICommonInterface, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i);

    boolean shouldCareGroupChatAttribute();

    boolean shouldClearCursorUponInitSyncDone();

    boolean shouldCorrectShortCode();

    boolean shouldEnableNetAPIPutFlag(String str);

    boolean shouldEnableNetAPIWorking(boolean z, boolean z2, boolean z3, boolean z4, boolean z5);

    boolean shouldPersistImsRegNum();

    boolean shouldSkipCmasSMS(String str);

    boolean shouldSkipMessage(ParamOMAObject paramOMAObject);

    boolean shouldStopInitSyncUponLowMemory();

    boolean shouldStopSendingAPIwhenNetworklost();

    void updateHTTPHeader();
}

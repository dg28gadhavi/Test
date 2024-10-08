package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.data.SortOrderEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.ObjectsOpSearch;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriteria;
import com.sec.internal.omanetapi.nms.data.SortCriterion;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CloudMessageGreetingSearch extends ObjectsOpSearch {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageGreetingSearch.class.getSimpleName();
    private final SimpleDateFormat mFormatOfName;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessageGreetingSearch(IAPICallFlowListener iAPICallFlowListener, String str, String str2, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), str2, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mFormatOfName = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.mIAPICallFlowListener = iAPICallFlowListener;
        SelectionCriteria selectionCriteria = new SelectionCriteria();
        constructSearchParam(str2, SyncMsgType.VM_GREETINGS, selectionCriteria);
        if (!TextUtils.isEmpty(str)) {
            selectionCriteria.fromCursor = str;
        }
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(str2));
        initPostRequest(selectionCriteria, true);
        final BufferDBChangeParam bufferDBChangeParam2 = bufferDBChangeParam;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        final String str3 = str2;
        final String str4 = str;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                List list;
                String str;
                String dataString = httpResponseParams.getDataString();
                String r1 = CloudMessageGreetingSearch.this.TAG;
                Log.d(r1, "Result code = " + httpResponseParams.getStatusCode());
                if (httpResponseParams.getStatusCode() == 302) {
                    Log.d(CloudMessageGreetingSearch.this.TAG, "302 redirect");
                    List list2 = httpResponseParams.getHeaders().get("Location");
                    if (list2 == null || list2.size() <= 0) {
                        str = null;
                    } else {
                        Log.i(CloudMessageGreetingSearch.this.TAG, list2.toString());
                        str = (String) list2.get(0);
                    }
                    if (!TextUtils.isEmpty(str)) {
                        try {
                            CloudMessageGreetingSearch.this.mStoreClient.getPrerenceManager().saveNcHost(new URL(str).getHost());
                        } catch (MalformedURLException e) {
                            String r0 = CloudMessageGreetingSearch.this.TAG;
                            Log.d(r0, "" + e.getMessage());
                            e.printStackTrace();
                        }
                        CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFailedCall(this, String.valueOf(302));
                        return;
                    }
                    CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFailedCall(this, bufferDBChangeParam2);
                } else if (httpResponseParams.getStatusCode() == 401) {
                    if (!CloudMessageGreetingSearch.this.handleUnAuthorized(httpResponseParams)) {
                        ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam2).setLine(CloudMessageGreetingSearch.this.getBoxId());
                        Message message = new Message();
                        message.obj = line.build();
                        message.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
                        iAPICallFlowListener2.onFixedFlowWithMessage(message);
                        iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                    }
                } else if (CloudMessageGreetingSearch.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(httpResponseParams.getStatusCode())) {
                    iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2, SyncMsgType.VM_GREETINGS, httpResponseParams.getStatusCode());
                } else if (httpResponseParams.getStatusCode() == 429 && (list = httpResponseParams.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER)) != null && list.size() > 0) {
                    Log.i(CloudMessageGreetingSearch.this.TAG, list.toString());
                    String str2 = (String) list.get(0);
                    String r02 = CloudMessageGreetingSearch.this.TAG;
                    Log.d(r02, "retryAfter is " + str2 + "seconds");
                    try {
                        int parseInt = Integer.parseInt(str2);
                        if (parseInt > 0) {
                            iAPICallFlowListener2.onOverRequest(this, CommonErrorName.RETRY_HEADER, parseInt);
                        } else {
                            iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                        }
                    } catch (NumberFormatException e2) {
                        Log.e(CloudMessageGreetingSearch.this.TAG, e2.getMessage());
                        iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                    }
                } else if (httpResponseParams.getStatusCode() == 204) {
                    Message message2 = new Message();
                    ParamOMAresponseforBufDB.Builder actionType = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE);
                    OMASyncEventType oMASyncEventType = OMASyncEventType.DELETE_GREETING;
                    message2.obj = actionType.setOMASyncEventType(oMASyncEventType).setMStoreClient(CloudMessageGreetingSearch.this.mStoreClient).setLine(str3).setSyncType(SyncMsgType.VM_GREETINGS).setCursor("").setBufferDBChangeParam(bufferDBChangeParam2).build();
                    message2.what = oMASyncEventType.getId();
                    CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFixedFlowWithMessage(message2);
                } else if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 206) {
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(dataString, OMAApiResponseParam.class);
                        if (oMAApiResponseParam != null) {
                            ObjectList objectList = oMAApiResponseParam.objectList;
                            Message message3 = new Message();
                            if (objectList != null) {
                                String str3 = objectList.cursor;
                                if (TextUtils.isEmpty(str3) || str3.equals(str4)) {
                                    ParamOMAresponseforBufDB.Builder actionType2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE);
                                    OMASyncEventType oMASyncEventType2 = OMASyncEventType.DELETE_GREETING;
                                    message3.obj = actionType2.setOMASyncEventType(oMASyncEventType2).setMStoreClient(CloudMessageGreetingSearch.this.mStoreClient).setObjectList(objectList).setLine(str3).setSyncType(SyncMsgType.VM_GREETINGS).setCursor(str3).setBufferDBChangeParam(bufferDBChangeParam2).build();
                                    message3.what = oMASyncEventType2.getId();
                                    CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFixedFlowWithMessage(message3);
                                    return;
                                }
                                ParamOMAresponseforBufDB.Builder objectList2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_PARTIAL_SYNC_SUMMARY).setObjectList(objectList);
                                OMASyncEventType oMASyncEventType3 = OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH;
                                message3.obj = objectList2.setOMASyncEventType(oMASyncEventType3).setMStoreClient(CloudMessageGreetingSearch.this.mStoreClient).setLine(str3).setSyncType(SyncMsgType.VM_GREETINGS).setCursor(str3).setBufferDBChangeParam(bufferDBChangeParam2).build();
                                message3.what = oMASyncEventType3.getId();
                                CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFixedFlowWithMessage(message3);
                                return;
                            }
                            message3.obj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE).setObjectList(objectList).setSyncType(SyncMsgType.VM_GREETINGS).setCursor("").setLine(str3).setBufferDBChangeParam(bufferDBChangeParam2).build();
                            message3.what = OMASyncEventType.DELETE_GREETING.getId();
                            CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFixedFlowWithMessage(message3);
                        }
                    } catch (Exception e3) {
                        String r5 = CloudMessageGreetingSearch.this.TAG;
                        Log.e(r5, e3 + " ");
                        e3.printStackTrace();
                    }
                } else {
                    CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFailedCall(this, bufferDBChangeParam2);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageGreetingSearch.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageGreetingSearch.this.mIAPICallFlowListener.onFailedCall(this, bufferDBChangeParam2);
            }
        });
    }

    private void constructSearchParam(String str, SyncMsgType syncMsgType, SelectionCriteria selectionCriteria) {
        Reference reference;
        selectionCriteria.maxEntries = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMaxSearchEntry();
        if (!SyncMsgType.VM_GREETINGS.equals(syncMsgType)) {
            Log.e(this.TAG, "illegal type " + syncMsgType);
            return;
        }
        SortCriteria sortCriteria = new SortCriteria();
        SortCriterion[] sortCriterionArr = {new SortCriterion()};
        SortCriterion sortCriterion = sortCriterionArr[0];
        sortCriterion.type = "Date";
        sortCriterion.order = SortOrderEnum.Date;
        sortCriteria.criterion = sortCriterionArr;
        String str2 = TMOVariables.TmoMessageFolderId.mVVMailGreeting;
        String protocol = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol).encodedAuthority(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost()).appendPath("nms").appendPath(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion()).appendPath(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName()).appendPath(str).appendPath("folders").appendPath(str2);
        try {
            reference = new Reference();
            try {
                reference.resourceURL = new URL(builder.build().toString());
            } catch (MalformedURLException e) {
                e = e;
            }
        } catch (MalformedURLException e2) {
            e = e2;
            reference = null;
            Log.e(this.TAG, e.getMessage() + "");
            reference.resourceURL = null;
            selectionCriteria.searchScope = reference;
            selectionCriteria.sortCriteria = sortCriteria;
        }
        selectionCriteria.searchScope = reference;
        selectionCriteria.sortCriteria = sortCriteria;
    }
}

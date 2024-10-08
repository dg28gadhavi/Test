package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannelLifetime;
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;

public class IndividualNotificationChannel extends BaseNCRequest {
    protected static final String Channels = "channels";
    private static final long serialVersionUID = 7321524394607040641L;
    private String TAG = IndividualNotificationChannel.class.getSimpleName();
    protected final String mChannelId;

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public IndividualNotificationChannel(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mChannelId = str4;
        buildAPISpecificURLFromBase();
    }

    public IndividualNotificationChannel(String str, String str2, String str3, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mChannelId = "";
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath(Channels);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        IMSLog.i(this.TAG, IMSLog.checker(uri));
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    public void initDeleteRequest(String str) {
        initDeleteRequest();
        this.mNCRequestHeaderMap.put("x-mcs-reason", str);
        setHeaders(this.mNCRequestHeaderMap);
    }

    public void initPutRequest(NotificationChannelLifetime notificationChannelLifetime, boolean z) {
        HttpPostBody httpPostBody;
        super.initCommonPutRequest();
        if (z) {
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            OMAApiRequestParam.NotificationChannelLifetimeRequest notificationChannelLifetimeRequest = new OMAApiRequestParam.NotificationChannelLifetimeRequest();
            notificationChannelLifetimeRequest.notificationChannelLifetime = notificationChannelLifetime;
            httpPostBody = new HttpPostBody(new Gson().toJson(notificationChannelLifetimeRequest));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    public void initPutRequest(McsNotificationChannelLifetime mcsNotificationChannelLifetime, boolean z) {
        HttpPostBody httpPostBody;
        super.initCommonPutRequest();
        if (z) {
            updateContactSyncHeader();
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            OMAApiRequestParam.McsNotificationChannelLifetimeRequest mcsNotificationChannelLifetimeRequest = new OMAApiRequestParam.McsNotificationChannelLifetimeRequest();
            mcsNotificationChannelLifetimeRequest.notificationChannelLifetime = mcsNotificationChannelLifetime;
            httpPostBody = new HttpPostBody(new Gson().toJson(mcsNotificationChannelLifetimeRequest));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath(Channels).appendPath(this.mChannelId);
        this.mBaseUrl = buildUpon.build().toString();
        String str = this.TAG;
        IMSLog.i(str, "buildAPISpecificURLFromBase: mBaseUrl: " + IMSLog.checker(this.mBaseUrl));
    }
}

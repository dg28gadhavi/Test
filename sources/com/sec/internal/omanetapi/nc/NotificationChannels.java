package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nc.data.ChannelData;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannel;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;

public class NotificationChannels extends BaseNCRequest {
    private static final long serialVersionUID = 2784778118212806724L;
    private final String TAG = NotificationChannels.class.getSimpleName();

    public NotificationChannels(String str, String str2, String str3, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, messageStoreClient);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(NotificationChannel notificationChannel, boolean z) {
        HttpPostBody httpPostBody;
        OMAApiRequestParam.NotificationChannels notificationChannels = new OMAApiRequestParam.NotificationChannels();
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Class<ChannelData> cls = ChannelData.class;
            gsonBuilder.registerTypeAdapter(cls, new GsonInterfaceAdapter(cls));
            Gson create = gsonBuilder.disableHtmlEscaping().create();
            notificationChannels.notificationChannel = notificationChannel;
            httpPostBody = new HttpPostBody(create.toJson(notificationChannels));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    public void initPostRequest(McsNotificationChannel mcsNotificationChannel, boolean z) {
        HttpPostBody httpPostBody;
        OMAApiRequestParam.McsNotificationChannels mcsNotificationChannels = new OMAApiRequestParam.McsNotificationChannels();
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            updateContactSyncHeader();
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            Gson create = new GsonBuilder().disableHtmlEscaping().create();
            mcsNotificationChannels.notificationChannel = mcsNotificationChannel;
            httpPostBody = new HttpPostBody(create.toJson(mcsNotificationChannels));
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
        buildUpon.appendPath("channels");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }
}

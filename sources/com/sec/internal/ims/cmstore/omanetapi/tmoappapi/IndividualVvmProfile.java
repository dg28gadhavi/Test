package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;

public class IndividualVvmProfile extends BaseNMSRequest {
    private static final long serialVersionUID = -6892711250370417577L;
    public String TAG = IndividualVvmProfile.class.getSimpleName();

    public IndividualVvmProfile(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initPostRequest(VvmServiceProfile vvmServiceProfile, boolean z) {
        HttpPostBody httpPostBody;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        OMAApiRequestParam.VvmServiceProfileRequest vvmServiceProfileRequest = new OMAApiRequestParam.VvmServiceProfileRequest();
        vvmServiceProfileRequest.vvmserviceProfile = vvmServiceProfile;
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            httpPostBody = new HttpPostBody(new Gson().toJson(vvmServiceProfileRequest));
        } else {
            Log.e(this.TAG, "VvmServiceProfile is not JSON");
            httpPostBody = null;
        }
        setPostBody(httpPostBody);
    }

    public void initPutRequest(VvmServiceProfile vvmServiceProfile, boolean z) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
        setHeaders(this.mNMSRequestHeaderMap);
        if (z) {
            OMAApiRequestParam.VvmServiceProfileRequest vvmServiceProfileRequest = new OMAApiRequestParam.VvmServiceProfileRequest();
            vvmServiceProfileRequest.vvmserviceProfile = vvmServiceProfile;
            setPostBody(new HttpPostBody(new Gson().toJson(vvmServiceProfileRequest)));
            return;
        }
        Log.e(this.TAG, "VvmServiceProfile is not JSON");
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("vvmserviceProfile");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.numberChecker(uri));
    }
}

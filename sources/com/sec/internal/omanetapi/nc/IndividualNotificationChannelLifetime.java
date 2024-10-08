package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class IndividualNotificationChannelLifetime extends IndividualNotificationChannel {
    private static final String ChannelLifetime = "channelLifetime";
    private static final String TAG = IndividualNotificationChannelLifetime.class.getSimpleName();

    public IndividualNotificationChannelLifetime(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("channels").appendPath(this.mChannelId).appendPath(ChannelLifetime);
        this.mBaseUrl = buildUpon.build().toString();
        String str = TAG;
        IMSLog.i(str, "buildAPISpecificURLFromBase: mBaseUrl: " + IMSLog.checker(this.mBaseUrl));
    }
}

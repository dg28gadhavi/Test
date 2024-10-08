package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class IndividualFolder extends BaseNMSRequest {
    private static final String TAG = IndividualFolder.class.getSimpleName();
    private final String mAttrFilter;
    private final String mFolderId;
    private final String mFromCursor;
    private final String mListFilter;
    private final int mMaxEntries;
    private final String mPath;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IndividualFolder(String str, String str2, String str3, String str4, String str5, String str6, int i, String str7, String str8, String str9, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mFolderId = str5;
        this.mFromCursor = str6;
        this.mMaxEntries = i;
        this.mListFilter = str7;
        this.mPath = str8;
        this.mAttrFilter = str9;
        buildAPISpecificURLFromBase();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("folders");
        buildUpon.appendPath(this.mFolderId);
        String str = this.mFromCursor;
        if (str != null) {
            buildUpon.appendQueryParameter("fromCursor", str);
        }
        int i = this.mMaxEntries;
        if (i > 0) {
            buildUpon.appendQueryParameter("maxEntries", String.valueOf(i));
        }
        String str2 = this.mListFilter;
        if (str2 != null) {
            buildUpon.appendQueryParameter("listFilter", str2);
        }
        String str3 = this.mPath;
        if (str3 != null) {
            buildUpon.appendQueryParameter("path", str3);
        }
        String str4 = this.mAttrFilter;
        if (str4 != null) {
            buildUpon.appendQueryParameter("attrFilter", str4);
        }
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}

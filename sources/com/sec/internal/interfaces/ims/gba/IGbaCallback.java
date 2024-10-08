package com.sec.internal.interfaces.ims.gba;

import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.gba.GbaException;

public interface IGbaCallback {
    void onComplete(int i, String str, String str2, boolean z, HttpResponseParams httpResponseParams);

    void onFail(int i, GbaException gbaException);
}

package com.sec.internal.interfaces.ims.gba;

import android.telephony.gba.GbaAuthRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.gba.GbaValue;
import com.sec.internal.ims.gba.params.GbaData;

public interface IGbaServiceModule {
    int getBtidAndGbaKey(GbaAuthRequest gbaAuthRequest, IGbaCallback iGbaCallback);

    int getBtidAndGbaKey(HttpRequestParams httpRequestParams, String str, HttpResponseParams httpResponseParams, IGbaCallback iGbaCallback);

    IGbaCallback getGbaCallback(int i);

    GbaValue getGbaValue(int i, String str);

    String getImei(int i);

    String getImpi(int i);

    GbaData getPassword(String str, boolean z, int i);

    boolean initGbaAccessibleObj();

    boolean isGbaUiccSupported(int i);

    void removeGbaCallback(int i);

    void resetGbaKey(String str, int i);

    void storeGbaBootstrapParams(int i, byte[] bArr, String str, String str2);

    String storeGbaDataAndGenerateKey(String str, String str2, String str3, byte[] bArr, byte[] bArr2, byte[] bArr3, GbaData gbaData, boolean z, int i);
}

package com.sec.internal.ims.translate;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendEucResponseResponse;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.util.ImsUtil;

public class EucResponseStatusTranslator implements TypeTranslator<SendEucResponseResponse, EucSendResponseStatus> {
    private static final String LOG_TAG = "EucResponseStatusTranslator";

    public EucSendResponseStatus translate(SendEucResponseResponse sendEucResponseResponse) {
        EucSendResponseStatus.Status status;
        EucType eucType;
        String id = sendEucResponseResponse.id();
        if (id != null) {
            int handle = ImsUtil.getHandle(sendEucResponseResponse.handle());
            int status2 = sendEucResponseResponse.status();
            if (status2 == 0) {
                status = EucSendResponseStatus.Status.SUCCESS;
            } else if (status2 == 1) {
                status = EucSendResponseStatus.Status.FAILURE_INTERNAL;
            } else if (status2 == 2) {
                status = EucSendResponseStatus.Status.FAILURE_NETWORK;
            } else {
                throw new TranslationException(Integer.valueOf(status2));
            }
            EucSendResponseStatus.Status status3 = status;
            int type = sendEucResponseResponse.type();
            if (type == 0) {
                eucType = EucType.PERSISTENT;
            } else if (type == 1) {
                eucType = EucType.VOLATILE;
            } else {
                Log.e(LOG_TAG, "Unknown or unsupported type of the original EUCR message.");
                throw new TranslationException(Integer.valueOf(type));
            }
            return new EucSendResponseStatus(id, eucType, ImsUri.parse(sendEucResponseResponse.remoteUri()), EucTranslatorUtil.getOwnIdentity(handle), status3);
        }
        throw new TranslationException("ID of EUC related to response is null!");
    }
}

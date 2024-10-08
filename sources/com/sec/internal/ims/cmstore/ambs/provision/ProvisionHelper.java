package com.sec.internal.ims.cmstore.ambs.provision;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.log.IMSLog;

public class ProvisionHelper {
    public static final String TAG = "ProvisionHelper";

    static void readAndSaveSimInformation(MessageStoreClient messageStoreClient) {
        String convertPhoneNumberToUserAct = AmbsUtils.convertPhoneNumberToUserAct(messageStoreClient.getSimManager().getMsisdn());
        String imsi = messageStoreClient.getSimManager().getImsi();
        String str = TAG;
        Log.i(str, "Phone number == " + IMSLog.checker(convertPhoneNumberToUserAct));
        messageStoreClient.getPrerenceManager().saveSimImsi(imsi);
        if (TextUtils.isEmpty(convertPhoneNumberToUserAct)) {
            Log.d(str, "empty CTN");
        }
        messageStoreClient.getPrerenceManager().saveUserCtn(convertPhoneNumberToUserAct, false);
    }

    static boolean isOOBE(MessageStoreClient messageStoreClient) {
        return messageStoreClient.getPrerenceManager().isEmptyPref();
    }
}

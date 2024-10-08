package com.sec.internal.helper.os;

import android.content.Context;
import android.os.UserHandle;
import com.android.internal.telephony.util.TelephonyUtils;

public class TelephonyUtilsWrapper {
    public static UserHandle getSubscriptionUserHandle(Context context, int i) {
        return TelephonyUtils.getSubscriptionUserHandle(context, i);
    }
}

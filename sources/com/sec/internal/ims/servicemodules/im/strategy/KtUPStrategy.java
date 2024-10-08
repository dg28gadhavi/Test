package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.os.UserManager;
import android.provider.Settings;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class KtUPStrategy extends SecUPStrategy {
    private static final String TAG = "KtUPStrategy";

    public KtUPStrategy(Context context, int i) {
        super(context, i);
    }

    public boolean isBMode(boolean z) {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "two_register", 0);
        IMSLog.i(TAG, this.mPhoneId, "isKtTwoPhoneServiceRegistered: " + i);
        if (i != 1) {
            return false;
        }
        if (i == 1 && z) {
            return true;
        }
        List users = Extensions.UserManagerRef.getUsers((UserManager) this.mContext.getSystemService("user"));
        if (users != null) {
            int i2 = 0;
            while (i2 < users.size()) {
                Object obj = users.get(i2);
                if (!Extensions.UserInfo.isBMode(obj) || Extensions.UserInfo.getUserId(obj) != Extensions.ActivityManager.getCurrentUser()) {
                    i2++;
                } else {
                    IMSLog.i(TAG, this.mPhoneId, "Current user set BMode.");
                    return true;
                }
            }
        }
        return false;
    }
}

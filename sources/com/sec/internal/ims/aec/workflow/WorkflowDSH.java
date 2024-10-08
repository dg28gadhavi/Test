package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.sec.internal.ims.aec.util.URLExtractor;

public class WorkflowDSH extends WorkflowTS43 {
    WorkflowDSH(Context context, Looper looper, Handler handler, String str) {
        super(context, looper, handler, str);
    }

    /* access modifiers changed from: protected */
    public void requestEntitlement(int i) {
        String url = URLExtractor.getUrl(this.mContext, this.mPhoneId, this.mAECJar.getEntitlementDomain(), this.mAECJar.getEntitlementPort(), this.mAECJar.getEntitlementPath(), this.mAECJar.getDomainFromImpi());
        if (i < 0 || TextUtils.isEmpty(url) || this.mAECJar.getAppId().isEmpty()) {
            sendMessage(obtainMessage(1002, this.mPhoneId, this.mAECJar.getHttpResponse()));
            return;
        }
        this.mPowerCtrl.lock(90000);
        if (TextUtils.isEmpty(this.mHttpJar.getHttpUrl())) {
            this.mHttpJar.setUserAgent(this.mAECJar.getEntitlementVersion());
            this.mHttpJar.setHostName(URLExtractor.getHostName(url));
            this.mHttpJar.setHttpUrl(url);
        }
        doWorkflow();
        this.mPowerCtrl.release();
    }
}

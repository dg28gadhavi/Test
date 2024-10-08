package com.sec.internal.ims.aec.receiver.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.ims.aec.workflow.WorkflowFactory;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.log.AECLog;

public class FcmIntentService extends IntentService {
    private static final String LOG_TAG = FcmIntentService.class.getSimpleName();

    public FcmIntentService() {
        super(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void onHandleIntent(Intent intent) {
        String str = LOG_TAG;
        synchronized (str) {
            int intExtra = intent.getIntExtra("phoneId", 0);
            String stringExtra = intent.getStringExtra(AECNamespace.NotifExtras.SENDER_ID);
            try {
                if (TextUtils.isEmpty(stringExtra)) {
                    updateFcmToken(intExtra, (String) null, "fcm senderId not ready");
                } else {
                    String token = FirebaseInstanceId.getInstance().getToken(stringExtra, "FCM");
                    if (TextUtils.isEmpty(token)) {
                        updateFcmToken(intExtra, (String) null, "fcm token not ready");
                    } else {
                        AECLog.s(str, stringExtra + ", " + token, intExtra);
                        updateFcmToken(intExtra, token, "fcm token ready");
                    }
                }
            } catch (Exception e) {
                updateFcmToken(intExtra, (String) null, e.getMessage());
            }
        }
    }

    private void updateFcmToken(int i, String str, String str2) {
        IWorkflowImpl workflow = WorkflowFactory.getInstance().getWorkflow(i);
        if (workflow != null) {
            workflow.updateFcmToken(str, str2);
        }
    }
}

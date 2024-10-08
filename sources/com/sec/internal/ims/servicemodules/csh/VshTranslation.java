package com.sec.internal.ims.servicemodules.csh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;

public class VshTranslation {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "VshTranslation";
    private static final String mCranePackage = "com.samsung.crane";
    private static final IntentFilter sIntentFilter;
    private Context mContext;
    private BroadcastReceiver mIntentReceiver;
    private VideoShareModule mServiceModule;

    static {
        IntentFilter intentFilter = new IntentFilter();
        sIntentFilter = intentFilter;
        intentFilter.addAction(VshIntents.VshIntent.ACTION_SHARE_CONTENT);
        intentFilter.addAction(VshIntents.VshIntent.ACTION_SHARE_ACCEPT);
        intentFilter.addAction(VshIntents.VshIntent.ACTION_SHARE_CANCEL);
        intentFilter.addAction(VshIntents.VshIntent.ACTION_TOGGLE_CAMERA);
        intentFilter.addAction(VshIntents.VshIntent.ACTION_CHANGE_SURFACE_ORIENTATION);
    }

    public VshTranslation(Context context, VideoShareModule videoShareModule) {
        AnonymousClass1 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String r0 = VshTranslation.LOG_TAG;
                Log.i(r0, "Received intent: " + action);
                if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_CONTENT)) {
                    VshTranslation.this.requestNewShare(intent);
                } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_ACCEPT)) {
                    VshTranslation.this.requestAcceptShare(intent);
                } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_CANCEL)) {
                    VshTranslation.this.requestCancelShare(intent);
                } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_TOGGLE_CAMERA)) {
                    VshTranslation.this.requestToggleCamera(intent);
                } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_CHANGE_SURFACE_ORIENTATION)) {
                    VshTranslation.this.requestChangeSurfaceOrientation(intent);
                }
            }
        };
        this.mIntentReceiver = r0;
        this.mContext = context;
        this.mServiceModule = videoShareModule;
        context.registerReceiver(r0, sIntentFilter);
    }

    /* access modifiers changed from: private */
    public void requestNewShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestNewShare: extras " + extras);
        this.mServiceModule.createShare(ImsUri.parse(((Uri) extras.getParcelable(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI)).toString()), extras.getString(ICshConstants.ExtraInformation.EXTRA_FILE_PATH, VshIntents.LIVE_VIDEO_CONTENTPATH));
    }

    /* access modifiers changed from: private */
    public void requestAcceptShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestAcceptShare: extras " + extras);
        this.mServiceModule.acceptShare(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1));
    }

    /* access modifiers changed from: private */
    public void requestCancelShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestCancelShare: extras " + extras);
        this.mServiceModule.cancelShare(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1));
    }

    /* access modifiers changed from: private */
    public void requestToggleCamera(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestToggleCamera: extras " + extras);
        this.mServiceModule.toggleCamera(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1));
    }

    /* access modifiers changed from: private */
    public void requestChangeSurfaceOrientation(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestChangeSurfaceOrientation: extras " + extras);
        this.mServiceModule.changeSurfaceOrientation(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1), extras.getInt(ICshConstants.ExtraInformation.EXTRA_SURFACE_ORIENTATION, -1));
    }

    public void broadcastIncomming(long j, ImsUri imsUri, String str) {
        Intent intent = new Intent();
        intent.setClassName(mCranePackage, "com.samsung.crane.callcomposer.receiver.SessionInvitationReceiver");
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_INCOMING);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, j);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_TYPE, 2);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(imsUri.toString()));
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_FILE_PATH, str);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastCanceled(long j, ImsUri imsUri, int i, int i2) {
        Intent intent = new Intent();
        intent.setClassName(mCranePackage, "com.samsung.crane.callcomposer.receiver.SessionInvitationReceiver");
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_CANCELED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, j);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(imsUri.toString()));
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_REASON, i2);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_DIRECTION, i);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastConnected(long j, ImsUri imsUri) {
        Intent intent = new Intent();
        intent.setClassName(mCranePackage, "com.samsung.crane.listener.RcsCallActionListener");
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_CONNECTED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, j);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(imsUri.toString()));
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastCommunicationError() {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_COMMUNICATION_ERROR);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastServiceReady() {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_SERVICE_READY);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastServiceNotReady() {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_SHARE_SERVICE_NOT_READY);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastCshCamError() {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_CAM_ERROR);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastCshServiceNotReady() {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    public void broadcastApproachingVsMaxDuration(long j, int i) {
        Intent intent = new Intent();
        intent.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(VshIntents.VshNotificationIntent.NOTIFICATION_APPROCHING_VS_MAX_DURATION);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, j);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_REMAINING_TIME, i);
        broadcastIntent(intent, this.mServiceModule.mActiveCallPhoneId);
    }

    private void broadcastIntent(Intent intent) throws NullPointerException {
        String str = LOG_TAG;
        Log.d(str, intent.toString() + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void broadcastIntent(Intent intent, int i) {
        String str = LOG_TAG;
        Log.d(str, intent.toString() + intent.getExtras());
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(i));
        if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle);
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        Log.i(str, "Received intent: " + action);
        if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_CONTENT)) {
            requestNewShare(intent);
        } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_ACCEPT)) {
            requestAcceptShare(intent);
        } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_SHARE_CANCEL)) {
            requestCancelShare(intent);
        } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_TOGGLE_CAMERA)) {
            requestToggleCamera(intent);
        } else if (TextUtils.equals(action, VshIntents.VshIntent.ACTION_CHANGE_SURFACE_ORIENTATION)) {
            requestChangeSurfaceOrientation(intent);
        }
    }
}

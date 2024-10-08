package com.sec.internal.ims.config.adapters;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.sec.ims.settings.ImsProfile;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.helper.userconsent.HyperlinkUtils;
import com.sec.internal.helper.userconsent.IHyperlinkOnClickListener;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.log.IMSLog;
import java.util.concurrent.Semaphore;

public class DialogAdapter extends Handler implements IDialogAdapter {
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String CANCEL_TC_NOTIFICATION = "com.samsung.rcs.framework.dialogadapter.action.CANCEL_TC_NOTIFICATION";
    static final int HANDLE_CREATE_SHOW_ACCEPT_REJECT = 0;
    static final int HANDLE_CREATE_SHOW_AUTOCONFIG = 5;
    static final int HANDLE_CREATE_SHOW_MSISDN = 2;
    static final int HANDLE_SIM_STATE_ABSENT = 6;
    static final String KEY_PHONE_ID = "phone_id";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = DialogAdapter.class.getSimpleName();
    static final int RCS_MSISDN_PROMPT_NOTIFICATION = 56846849;
    static final int RCS_TC_NOTIFICATION = 11012013;
    public static final String SHOW_MSISDN_POPUP = "com.samsung.rcs.framework.dialogadapter.action.SHOW_MSISDN_POPUP";
    public static final String SHOW_TC_POPUP = "com.samsung.rcs.framework.dialogadapter.action.SHOW_TC_POPUP";
    static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    /* access modifiers changed from: private */
    public String mAccept;
    /* access modifiers changed from: private */
    public boolean mAcceptReject;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mCountryCode;
    /* access modifiers changed from: private */
    public AlertDialog mDialog;
    protected DialogNotiReceiver mDialogNotiReceiver;
    /* access modifiers changed from: private */
    public String mMessage;
    /* access modifiers changed from: private */
    public String mMsisdn;
    /* access modifiers changed from: private */
    public boolean mNextCancel;
    /* access modifiers changed from: private */
    public NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public int mPhoneId;
    /* access modifiers changed from: private */
    public boolean mRcsTcNotification;
    private Receiver mReceiver;
    private ReceiverForTcPopup mReceiverForTcPopup;
    /* access modifiers changed from: private */
    public String mReject;
    /* access modifiers changed from: private */
    public final Semaphore mSemaphore;
    /* access modifiers changed from: private */
    public boolean mSkip;
    /* access modifiers changed from: private */
    public boolean mSupportNotiBar;
    /* access modifiers changed from: private */
    public boolean mTcPopupFlag;
    /* access modifiers changed from: private */
    public ITelephonyManager mTelephony;
    /* access modifiers changed from: private */
    public String mTitle;
    /* access modifiers changed from: private */
    public boolean mYesNo;

    public DialogAdapter(Context context, IConfigModule iConfigModule, int i) {
        this(context, iConfigModule);
        this.mPhoneId = i;
    }

    public DialogAdapter(Context context, IConfigModule iConfigModule) {
        super(iConfigModule.getHandler().getLooper());
        this.mTcPopupFlag = false;
        this.mSemaphore = new Semaphore(0);
        this.mDialog = null;
        this.mTitle = null;
        this.mMessage = null;
        this.mAccept = null;
        this.mReject = null;
        this.mPhoneId = 0;
        this.mCountryCode = null;
        this.mAcceptReject = false;
        this.mYesNo = false;
        this.mNextCancel = false;
        this.mSkip = false;
        this.mMsisdn = "";
        this.mSupportNotiBar = true;
        this.mRcsTcNotification = false;
        this.mTelephony = null;
        this.mDialogNotiReceiver = new DialogNotiReceiver();
        this.mReceiverForTcPopup = new ReceiverForTcPopup();
        this.mReceiver = new Receiver();
        IMSLog.i(LOG_TAG, this.mPhoneId, "Init DialogAdapter");
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mTelephony = TelephonyManagerWrapper.getInstance(context);
        registerReceivers();
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SHOW_TC_POPUP);
        intentFilter.addAction(CANCEL_TC_NOTIFICATION);
        intentFilter.addAction(SHOW_MSISDN_POPUP);
        this.mContext.registerReceiver(this.mDialogNotiReceiver, intentFilter);
        Context context = this.mContext;
        Receiver receiver = this.mReceiver;
        context.registerReceiver(receiver, receiver.getIntentFilter());
    }

    protected class DialogNotiReceiver extends BroadcastReceiver {
        protected DialogNotiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String r1 = DialogAdapter.LOG_TAG;
            int r2 = DialogAdapter.this.mPhoneId;
            IMSLog.i(r1, r2, "DialogNotiReceiver: " + action);
            if (context.getContentResolver() != null && intent.getExtras() != null && intent.getExtras().getInt(DialogAdapter.KEY_PHONE_ID, -1) == DialogAdapter.this.mPhoneId) {
                if (DialogAdapter.SHOW_TC_POPUP.equals(action) && !DialogAdapter.this.mTcPopupFlag) {
                    DialogAdapter dialogAdapter = DialogAdapter.this;
                    if (dialogAdapter.isStringValid(dialogAdapter.mTitle)) {
                        DialogAdapter dialogAdapter2 = DialogAdapter.this;
                        if (!dialogAdapter2.isStringValid(dialogAdapter2.mMessage)) {
                            return;
                        }
                        if (DialogAdapter.shouldShowButton(DialogAdapter.this.mAccept) || DialogAdapter.shouldShowButton(DialogAdapter.this.mReject)) {
                            DialogAdapter.this.sendEmptyMessage(0);
                        }
                    }
                } else if (DialogAdapter.CANCEL_TC_NOTIFICATION.equals(action)) {
                    DialogAdapter.this.mTcPopupFlag = false;
                    DialogAdapter.this.mRcsTcNotification = false;
                    DialogAdapter.this.mNotificationManager.cancel(DialogAdapter.this.mPhoneId + DialogAdapter.RCS_TC_NOTIFICATION);
                } else if (DialogAdapter.SHOW_MSISDN_POPUP.equals(action)) {
                    DialogAdapter.this.mNotificationManager.cancel(DialogAdapter.this.mPhoneId + DialogAdapter.RCS_MSISDN_PROMPT_NOTIFICATION);
                    DialogAdapter.this.sendEmptyMessage(2);
                }
            }
        }
    }

    private class ReceiverForTcPopup extends BroadcastReceiver {
        private ReceiverForTcPopup() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent.getExtras() == null) {
                String r6 = DialogAdapter.LOG_TAG;
                int r4 = DialogAdapter.this.mPhoneId;
                IMSLog.i(r6, r4, "ReceiverForTcPopup: " + action + " , intent getExtras returning null");
                return;
            }
            String string = intent.getExtras().getString("reason");
            if (string != null && !TextUtils.isEmpty(string)) {
                String r0 = DialogAdapter.LOG_TAG;
                int r2 = DialogAdapter.this.mPhoneId;
                IMSLog.i(r0, r2, "ReceiverForTcPopup: " + action + ", reason: " + string);
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) && DialogAdapter.this.mDialog != null) {
                    if (string.equals(DialogAdapter.SYSTEM_DIALOG_REASON_RECENT_APPS) || string.equals(DialogAdapter.SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        DialogAdapter.this.mTcPopupFlag = false;
                        DialogAdapter.this.mDialog.dismiss();
                        DialogAdapter.this.unregisterReceiverForTcPopup();
                    }
                }
            }
        }
    }

    private void registerReceiverForTcPopup() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiver(this.mReceiverForTcPopup, intentFilter);
    }

    /* access modifiers changed from: private */
    public void unregisterReceiverForTcPopup() {
        try {
            this.mContext.unregisterReceiver(this.mReceiverForTcPopup);
        } catch (IllegalArgumentException unused) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "unregisterReceiverForTcPopup: Receiver not registered!");
        }
    }

    private class Receiver extends BroadcastReceiver {
        private IntentFilter mIntentFilter;

        public Receiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED") && 1 == DialogAdapter.this.mTelephony.getSimState() && DialogAdapter.this.mDialog != null) {
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "onReceive: HANDLE_SIM_STATE_ABSENT");
                DialogAdapter dialogAdapter = DialogAdapter.this;
                dialogAdapter.sendMessage(dialogAdapter.obtainMessage(6, dialogAdapter.mDialog));
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    private void showRcsNotification(int i, String str, String str2) {
        PendingIntent pendingIntent;
        String str3 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str3, i2, "showRcsNotification: type: " + i);
        String string = this.mContext.getResources().getString(R.string.app_name);
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(string, string, 2));
        Notification.Builder builder = new Notification.Builder(this.mContext, string);
        builder.setSmallIcon(R.drawable.stat_notify_rcs_service_avaliable);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        if (i == RCS_TC_NOTIFICATION) {
            this.mRcsTcNotification = true;
            Intent intent = new Intent(SHOW_TC_POPUP);
            intent.putExtra(KEY_PHONE_ID, this.mPhoneId);
            intent.setPackage(this.mContext.getPackageName());
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, this.mPhoneId + i, intent, 33554432);
            String obj = Html.fromHtml(str2, 0).toString();
            builder.setContentTitle(str);
            builder.setContentText(obj);
            pendingIntent = broadcast;
        } else if (i != RCS_MSISDN_PROMPT_NOTIFICATION) {
            IMSLog.i(str3, this.mPhoneId, "showRcsNotification: unsupported type!");
            return;
        } else {
            Intent intent2 = new Intent(SHOW_MSISDN_POPUP);
            intent2.putExtra(KEY_PHONE_ID, this.mPhoneId);
            intent2.setPackage(this.mContext.getPackageName());
            pendingIntent = PendingIntent.getBroadcast(this.mContext, this.mPhoneId + i, intent2, 33554432);
            builder.setContentTitle(this.mContext.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title) + " [SIM" + (this.mPhoneId + 1) + "]");
            builder.setContentText(Html.fromHtml(this.mContext.getResources().getString(R.string.dialog_text_rcs_config_msisdn_text), 0));
        }
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        this.mNotificationManager.notify(i + this.mPhoneId, builder.build());
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            IMSLog.i(str, this.mPhoneId, "accept, reject dialog create & show");
            this.mSupportNotiBar = true;
            Object obj = message.obj;
            if (obj != null) {
                this.mSupportNotiBar = ((Boolean) obj).booleanValue();
            }
            int i3 = this.mPhoneId;
            IMSLog.i(str, i3, "support_notification_for_TnC : " + this.mSupportNotiBar);
            if (this.mSupportNotiBar) {
                registerReceiverForTcPopup();
            }
            AlertDialog create = createAcceptRejectBuilder(this.mContext, this.mTitle, this.mMessage, this.mAccept, this.mReject).create();
            this.mDialog = create;
            create.getWindow().addFlags(65792);
            this.mDialog.getWindow().setType(2038);
            this.mDialog.setCancelable(this.mSupportNotiBar);
            this.mTcPopupFlag = true;
            this.mDialog.show();
        } else if (i2 == 2) {
            AlertDialog create2 = createMsisdnBuilder(this.mContext).create();
            this.mDialog = create2;
            create2.getWindow().setSoftInputMode(32);
            if (!"2017A".equals(SemSystemProperties.get("ro.build.scafe.version"))) {
                this.mDialog.getWindow().addFlags(65536);
            }
            this.mDialog.getWindow().setType(2038);
            this.mDialog.setCancelable(false);
            this.mDialog.show();
            this.mDialog.getButton(-1).setEnabled(false);
        } else if (i2 == 5) {
            this.mDialog = createAutoconfigBuilder(this.mContext).create();
            if (SemSystemProperties.get("ro.build.scafe.cream").contains("white")) {
                this.mDialog.getWindow().setType(2038);
            } else {
                this.mDialog.getWindow().addFlags(65792);
                this.mDialog.getWindow().setType(2038);
            }
            this.mDialog.setCancelable(false);
            this.mDialog.show();
        } else if (i2 != 6) {
            IMSLog.i(str, this.mPhoneId, "unknown message!!");
        } else {
            AlertDialog alertDialog = (AlertDialog) message.obj;
            if (this.mRcsTcNotification) {
                Intent intent = new Intent(CANCEL_TC_NOTIFICATION);
                intent.putExtra(KEY_PHONE_ID, this.mPhoneId);
                this.mContext.sendBroadcast(intent);
            }
            if (alertDialog != null) {
                alertDialog.dismiss();
                this.mSemaphore.release();
                IMSLog.i(str, this.mPhoneId, "dismiss Dialog");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isStringValid(String str) {
        return str != null && !str.isEmpty();
    }

    /* access modifiers changed from: private */
    public static boolean shouldShowButton(String str) {
        return "1".equals(str);
    }

    public boolean getAcceptReject(String str, String str2, String str3, String str4) {
        return getAcceptReject(str, str2, str3, str4, this.mPhoneId);
    }

    public boolean getAcceptReject(String str, String str2, String str3, String str4, int i) {
        this.mTitle = str;
        this.mMessage = str2;
        this.mAccept = str3;
        this.mReject = str4;
        this.mPhoneId = i;
        String str5 = LOG_TAG;
        IMSLog.i(str5, i, "getAcceptReject");
        if (!isStringValid(this.mTitle) || !isStringValid(this.mMessage) || (!shouldShowButton(this.mAccept) && !shouldShowButton(this.mReject))) {
            int i2 = this.mPhoneId;
            IMSLog.i(str5, i2, "popup dialog cancelled mTitle: " + this.mTitle + ", mMessage: " + this.mMessage + ", mAccept: " + this.mAccept + ", mReject: " + this.mReject);
            return true;
        }
        showRcsNotification(RCS_TC_NOTIFICATION, str, str2);
        boolean z = Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
        if (!this.mTcPopupFlag && z) {
            sendMessage(obtainMessage(0, Boolean.valueOf(ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.SUPPORT_NOTIFICATION_FOR_TNC, true))));
        }
        IMSLog.i(str5, this.mPhoneId, "getAcceptReject: wait yes or no");
        if (shallRcsRegisterByDefault(str4)) {
            this.mAcceptReject = true;
        } else {
            try {
                this.mSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String str6 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str6, i3, "getAcceptReject: receive yes or no:" + this.mAcceptReject);
        this.mTcPopupFlag = false;
        return this.mAcceptReject;
    }

    public String getMsisdn(String str, String str2) {
        this.mMsisdn = str2;
        String str3 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str3, i, "getMsisdn: old msisdn: " + IMSLog.checker(str2) + " entered earlier by user");
        return getMsisdn(str);
    }

    public String getMsisdn(String str) {
        this.mCountryCode = str;
        boolean z = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1) {
            z = true;
        }
        if (z) {
            sendEmptyMessage(2);
        } else {
            showRcsNotification(RCS_MSISDN_PROMPT_NOTIFICATION, (String) null, (String) null);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "getMsisdn: wait MSISDN");
        try {
            this.mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, this.mPhoneId, "getMsisdn: mYesNo: " + this.mYesNo + ", mSkip: " + this.mSkip);
        if (this.mYesNo) {
            IMSLog.d(str2, this.mPhoneId, "getMsisdn: receive MSISDN:" + IMSLog.checker(this.mMsisdn));
        } else if (this.mSkip) {
            this.mMsisdn = "skip";
        }
        return this.mMsisdn;
    }

    public boolean getNextCancel() {
        sendEmptyMessage(5);
        IMSLog.i(LOG_TAG, this.mPhoneId, "getNextCancel: wait Next or Cancel");
        try {
            this.mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getNextCancel: " + this.mNextCancel);
        return this.mNextCancel;
    }

    private int checkNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32 ? 16974545 : 16974546;
    }

    private AlertDialog.Builder createAcceptRejectBuilder(Context context, String str, String str2, String str3, final String str4) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, checkNightMode(context));
        ScrollView scrollView = (ScrollView) LayoutInflater.from(this.mContext).inflate(R.layout.notification_dialog, (ViewGroup) null);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.notification_dialog);
        builder.setView(scrollView);
        if (str != null) {
            builder.setTitle(str);
        }
        TextView textView = (TextView) linearLayout.findViewById(R.id.messagebox);
        if (str2 != null) {
            HyperlinkUtils.processLinks(textView, str2, new IHyperlinkOnClickListener() {
                public void onClick(View view, Uri uri) {
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.setData(uri);
                    intent.setFlags(LogClass.SIM_EVENT);
                    try {
                        DialogAdapter.this.mContext.startActivity(intent);
                        if (DialogAdapter.this.mDialog != null) {
                            DialogAdapter.this.mDialog.cancel();
                        }
                    } catch (ActivityNotFoundException e) {
                        IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, e.getMessage());
                        Toast.makeText(DialogAdapter.this.mContext, R.string.hyperlink_format_not_supported_exception, 0).show();
                    }
                }
            });
        }
        if ("1".equals(str3)) {
            String string = context.getResources().getString(R.string.dialog_text_rcs_config_ok);
            if ("1".equals(str4)) {
                string = context.getResources().getString(R.string.dialog_text_rcs_config_accept);
            }
            builder.setPositiveButton(string, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(DialogAdapter.CANCEL_TC_NOTIFICATION);
                    intent.putExtra(DialogAdapter.KEY_PHONE_ID, DialogAdapter.this.mPhoneId);
                    DialogAdapter.this.mContext.sendBroadcast(intent);
                    dialogInterface.dismiss();
                    if (DialogAdapter.this.mSupportNotiBar) {
                        DialogAdapter.this.unregisterReceiverForTcPopup();
                    }
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                    DialogAdapter.this.mAcceptReject = true;
                    if (!DialogAdapter.this.shallRcsRegisterByDefault(str4)) {
                        DialogAdapter.this.mSemaphore.release();
                    }
                }
            });
        }
        if ("1".equals(str4)) {
            builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_reject), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(DialogAdapter.CANCEL_TC_NOTIFICATION);
                    intent.putExtra(DialogAdapter.KEY_PHONE_ID, DialogAdapter.this.mPhoneId);
                    DialogAdapter.this.mContext.sendBroadcast(intent);
                    dialogInterface.dismiss();
                    if (DialogAdapter.this.mSupportNotiBar) {
                        DialogAdapter.this.unregisterReceiverForTcPopup();
                    }
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                    DialogAdapter.this.mAcceptReject = false;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "onCancel");
                DialogAdapter.this.mTcPopupFlag = false;
                dialogInterface.dismiss();
                if (DialogAdapter.this.mSupportNotiBar) {
                    DialogAdapter.this.unregisterReceiverForTcPopup();
                }
            }
        });
        return builder;
    }

    private AlertDialog.Builder createMsisdnBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, checkNightMode(context));
        if (RcsUtils.DualRcs.isDualRcsReg()) {
            builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title) + "[SIM" + (this.mPhoneId + 1) + "]");
        } else {
            builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title));
        }
        LayoutInflater from = LayoutInflater.from(this.mContext);
        ScrollView scrollView = (ScrollView) from.inflate(R.layout.notification_dialog, (ViewGroup) null);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.notification_dialog);
        View inflate = from.inflate(R.layout.notification_inputbox, (ViewGroup) null);
        ((TextView) linearLayout.findViewById(R.id.messagebox)).setText(Html.fromHtml(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_text), 0));
        final EditText editText = (EditText) inflate.findViewById(R.id.input);
        linearLayout.addView(inflate);
        if (!"".equals(this.mMsisdn) && !"skip".equals(this.mMsisdn)) {
            editText.setText(this.mMsisdn);
        }
        editText.setInputType(3);
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                String r0 = DialogAdapter.LOG_TAG;
                int r1 = DialogAdapter.this.mPhoneId;
                IMSLog.i(r0, r1, "input:" + editable.toString());
                if (ImsCallUtil.validatePhoneNumber(editable.toString(), DialogAdapter.this.mCountryCode).length() != 0) {
                    DialogAdapter.this.mDialog.getButton(-1).setEnabled(true);
                } else {
                    DialogAdapter.this.mDialog.getButton(-1).setEnabled(false);
                }
            }
        });
        builder.setView(scrollView);
        builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_config_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                DialogAdapter.this.mMsisdn = ImsCallUtil.validatePhoneNumber(editText.getText().toString(), DialogAdapter.this.mCountryCode);
                DialogAdapter.this.mYesNo = true;
                DialogAdapter.this.mSkip = false;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if (!isEnableUPInImsprofile() || !simMno.isVodafone()) {
            builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                    DialogAdapter.this.mMsisdn = "";
                    DialogAdapter.this.mYesNo = false;
                    DialogAdapter.this.mSkip = false;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        if (isEnableUPInImsprofile()) {
            builder.setNeutralButton(context.getResources().getString(R.string.dialog_text_rcs_config_skip), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NeutralButton");
                    DialogAdapter.this.mMsisdn = "";
                    DialogAdapter.this.mYesNo = false;
                    DialogAdapter.this.mSkip = true;
                    DialogAdapter.this.mSemaphore.release();
                }
            });
        }
        return builder;
    }

    private AlertDialog.Builder createAutoconfigBuilder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, checkNightMode(context));
        builder.setTitle(context.getResources().getString(R.string.dialog_text_rcs_config_msisdn_title));
        builder.setMessage(Html.fromHtml(context.getResources().getString(R.string.dialog_text_rcs_config_autoconfig_text), 0));
        builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_config_next), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "PositiveButton");
                DialogAdapter.this.mNextCancel = true;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_config_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                IMSLog.i(DialogAdapter.LOG_TAG, DialogAdapter.this.mPhoneId, "NegativeButton");
                DialogAdapter.this.mNextCancel = false;
                DialogAdapter.this.mSemaphore.release();
            }
        });
        return builder;
    }

    private boolean isEnableUPInImsprofile() {
        return ImsProfile.isRcsUpProfile(ImsRegistry.getRcsProfileType(this.mPhoneId));
    }

    /* access modifiers changed from: private */
    public boolean shallRcsRegisterByDefault(String str) {
        return SimUtil.getSimMno(this.mPhoneId).isOneOf(Mno.TELEFONICA_GERMANY, Mno.TELEFONICA_SPAIN, Mno.TELEFONICA_UK) && !"1".equals(str);
    }

    public void cleanup() {
        DialogNotiReceiver dialogNotiReceiver = this.mDialogNotiReceiver;
        if (dialogNotiReceiver != null) {
            this.mContext.unregisterReceiver(dialogNotiReceiver);
        }
        Receiver receiver = this.mReceiver;
        if (receiver != null) {
            this.mContext.unregisterReceiver(receiver);
        }
    }
}

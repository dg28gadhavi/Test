package com.sec.internal.ims.entitlement.softphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AccessTokenResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AddAddressResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AddressValidationResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.CallWaitingResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.ImsNetworkIdentifiersResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.SoftphoneResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.TermsAndConditionsResponse;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.State;
import com.sec.internal.helper.entitlement.softphone.SoftphoneResponseUtils;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.log.IMSLog;
import com.sec.vsim.attsoftphone.data.GeneralNotify;

public class SoftphoneStateHandler extends VSimClient {
    private final IntentFilter INTENT_FILTER_AKA_CHALLENGE;
    private final IntentFilter INTENT_FILTER_LOCATION_SERVICE;
    private final IntentFilter INTENT_FILTER_SHUTDOWN_SERVICE;
    private final IntentFilter INTENT_FILTER_SOFTPHONE_ALARM;
    private final IntentFilter INTENT_FILTER_SOFTPHONE_REGISTRATION;
    public final String LOG_TAG;
    /* access modifiers changed from: private */
    public final String mAccountId;
    protected final State mActivatedState;
    protected final State mActivatingState;
    /* access modifiers changed from: private */
    public final State mAirplaneState;
    final BroadcastReceiver mAkaEventReceiver;
    protected SoftphoneClient mClient;
    Context mContext;
    protected final State mDeactivatingState;
    public SimpleEventLog mEventLog;
    protected final State mInitialState;
    protected final State mReadyState;
    /* access modifiers changed from: private */
    public final State mRefreshState;
    protected final State mRegisteredState;
    protected final State mReleasingState;
    /* access modifiers changed from: private */
    public final State mReloginState;
    protected final State mServiceOutState;
    BroadcastReceiver mSoftphoneAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("Receive Alarm Intent, action: " + action);
            if ("refresh_token".equalsIgnoreCase(action)) {
                Message obtainMessage = SoftphoneStateHandler.this.obtainMessage(15, 0, (int) SoftphoneNamespaces.mTimeoutType4[0], (Object) null);
                int intExtra = intent.getIntExtra(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3);
                Bundle bundle = new Bundle();
                bundle.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, intExtra);
                obtainMessage.setData(bundle);
                SoftphoneStateHandler.this.sendMessage(obtainMessage);
            } else if (SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS.equalsIgnoreCase(action)) {
                SoftphoneStateHandler.this.removeMessages(1020);
                SoftphoneStateHandler.this.sendMessage(16, 0);
                SoftphoneStateHandler.this.mClient.scheduleSmsAlarm();
            } else if (SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY.equalsIgnoreCase(action)) {
                SoftphoneStateHandler.this.sendMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN, 0, 0);
            }
        }
    };
    final BroadcastReceiver mSoftphoneRegistrationReceiver;
    final BroadcastReceiver mSoftphoneShutdownReceiver;
    protected final State mUserSwitchState;

    public SoftphoneStateHandler(Looper looper, Context context, String str, SoftphoneClient softphoneClient) {
        super(looper);
        AnonymousClass2 r5 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SoftphoneStateHandler.this.mEventLog.logAndAdd("Receive SoftphoneRegistrationFailure Intent");
                SoftphoneStateHandler.this.mClient.isTarget(intent.getStringExtra(SoftphoneContract.SoftphoneRegistrationFailure.EXTRA_IMPI));
            }
        };
        this.mSoftphoneRegistrationReceiver = r5;
        AnonymousClass3 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SoftphoneStateHandler.this.mEventLog.logAndAdd("Receive Shutdown Intent");
                SoftphoneStateHandler.this.sendMessage(1024);
            }
        };
        this.mSoftphoneShutdownReceiver = r0;
        AnonymousClass4 r1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String str = SoftphoneStateHandler.this.LOG_TAG;
                IMSLog.i(str, "Intent received : " + intent);
                if ("com.sec.imsservice.REQUEST_AKA_CHALLENGE".equals(intent.getAction())) {
                    String stringExtra = intent.getStringExtra(WwwAuthenticateHeader.HEADER_PARAM_NONCE);
                    String stringExtra2 = intent.getStringExtra("impi");
                    int intExtra = intent.getIntExtra("id", 0);
                    String str2 = SoftphoneStateHandler.this.LOG_TAG;
                    IMSLog.s(str2, "AKA challenge for id: " + intExtra + ", mProfileId: " + SoftphoneStateHandler.this.mClient.getProfileId() + ", impi: " + stringExtra2);
                    if (SoftphoneStateHandler.this.mClient.getProfileId() == intExtra) {
                        SoftphoneStateHandler.this.mClient.onRequestAkaChallenge(stringExtra, 0);
                    }
                }
            }
        };
        this.mAkaEventReceiver = r1;
        this.mInitialState = new InitialState();
        this.mActivatingState = new ActivatingState();
        this.mReadyState = new ReadyState();
        this.mActivatedState = new ActivatedState();
        this.mRegisteredState = new RegisteredState();
        this.mRefreshState = new RefreshState();
        this.mAirplaneState = new AirplaneState();
        this.mServiceOutState = new ServiceOut();
        this.mReleasingState = new ReleasingState();
        this.mUserSwitchState = new UserSwitchState();
        this.mDeactivatingState = new DeactivatingState();
        this.mReloginState = new ReloginState();
        this.mContext = context;
        this.mClient = softphoneClient;
        this.mAccountId = str;
        String str2 = SoftphoneStateHandler.class.getSimpleName() + '-' + str;
        this.LOG_TAG = str2;
        this.mEventLog = new SimpleEventLog(context, str2, 200);
        IntentFilter intentFilter = new IntentFilter();
        this.INTENT_FILTER_SOFTPHONE_ALARM = intentFilter;
        intentFilter.addAction("refresh_token");
        intentFilter.addAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        intentFilter.addAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY);
        this.mContext.registerReceiver(this.mSoftphoneAlarmReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        this.INTENT_FILTER_SOFTPHONE_REGISTRATION = intentFilter2;
        intentFilter2.addAction(SoftphoneContract.SoftphoneRegistrationFailure.ACTION_TRY_REGISTER);
        this.mContext.registerReceiver(r5, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        this.INTENT_FILTER_LOCATION_SERVICE = intentFilter3;
        intentFilter3.addAction("android.location.PROVIDERS_CHANGED");
        IntentFilter intentFilter4 = new IntentFilter();
        this.INTENT_FILTER_SHUTDOWN_SERVICE = intentFilter4;
        intentFilter4.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(r0, intentFilter4);
        IntentFilter intentFilter5 = new IntentFilter();
        this.INTENT_FILTER_AKA_CHALLENGE = intentFilter5;
        intentFilter5.addAction("com.sec.imsservice.REQUEST_AKA_CHALLENGE");
        this.mContext.registerReceiver(r1, intentFilter5);
        initState();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mEventLog.logAndAdd("finalize()");
        super.finalize();
    }

    public int getAccountStatus() {
        Cursor query = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mClient.getUserId()), (String[]) null, (String) null, (String[]) null, (String) null);
        int i = -1;
        if (query != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("getAccountStatus found " + query.getCount() + " records");
            if (query.moveToFirst()) {
                i = query.getInt(query.getColumnIndex("status"));
            }
            query.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getAccountStatus status: " + i);
        return i;
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mReadyState, this.mDefaultState);
        addState(this.mActivatedState, this.mReadyState);
        addState(this.mRegisteredState, this.mActivatedState);
        addState(this.mRefreshState, this.mReadyState);
        addState(this.mAirplaneState, this.mReadyState);
        addState(this.mServiceOutState, this.mReadyState);
        addState(this.mReleasingState, this.mDefaultState);
        addState(this.mUserSwitchState, this.mReleasingState);
        addState(this.mDeactivatingState, this.mReleasingState);
        addState(this.mReloginState, this.mReleasingState);
        setInitialState(this.mInitialState);
        start();
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler.this.mClient.startInitstate();
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 0) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(100, message.arg1, message.arg2));
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mActivatingState);
            } else if (i != 15) {
                String str = SoftphoneStateHandler.this.LOG_TAG;
                IMSLog.e(str, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                return false;
            } else {
                SoftphoneStateHandler.this.mClient.refreshToken(message.arg1, (long) message.arg2, message.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3));
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReadyState);
            }
            return true;
        }
    }

    class ActivatingState extends State {
        ActivatingState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            SoftphoneStateHandler.this.mEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            boolean z = false;
            if (i == 100) {
                AccessTokenResponse accessTokenResponse = (AccessTokenResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, AccessTokenResponse.class, 200);
                SoftphoneClient softphoneClient = SoftphoneStateHandler.this.mClient;
                int i2 = message.arg1;
                if (message.arg2 == 1) {
                    z = true;
                }
                softphoneClient.processExchangeForAccessTokenResponse(accessTokenResponse, i2, z);
            } else if (i == 1018) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mDeactivatingState);
            } else if (i == 1027) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(100, message.arg1, message.arg2));
            } else if (i == 1035) {
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReadyState);
            } else if (i != 1036) {
                IMSLog.e(SoftphoneStateHandler.this.LOG_TAG, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                return false;
            } else {
                SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                softphoneStateHandler3.transitionTo(softphoneStateHandler3.mInitialState);
            }
            return true;
        }
    }

    class ReadyState extends State {
        ReadyState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 1) {
                Message obtainMessage = SoftphoneStateHandler.this.obtainMessage(101, message.arg1, message.arg2);
                obtainMessage.setData(message.getData());
                ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage);
                return true;
            } else if (i == 2) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(102, message.arg1, message.arg2));
                return true;
            } else if (i == 3) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(103, message.arg1, message.arg2));
                return true;
            } else if (i == 4) {
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(true, false, 0, SoftphoneNamespaces.mTimeoutType1[0], message.arg1);
                return true;
            } else if (i != 6) {
                if (i != 7) {
                    if (i == 18) {
                        SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IDENTITY_RELEASED, (String) null);
                        return true;
                    } else if (i != 19) {
                        Class cls = SoftphoneResponse.class;
                        if (i == 1010) {
                            SoftphoneStateHandler.this.mClient.processSetCallWaitingInfoResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls, 200), message.arg1, message.getData().getParcelable("communication-waiting"));
                            return true;
                        } else if (i == 1011) {
                            SoftphoneStateHandler.this.mClient.processSetCallForwardingInfoResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls, 200), message.arg1, message.getData().getParcelable("communication-diversion"));
                            return true;
                        } else if (i == 1017) {
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) message.obj);
                            return true;
                        } else if (i == 1018) {
                            SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                            softphoneStateHandler.transitionTo(softphoneStateHandler.mDeactivatingState);
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            return true;
                        } else if (i == 1024) {
                            SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                            softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReleasingState);
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                            return true;
                        } else if (i == 1025) {
                            SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                            softphoneStateHandler3.transitionTo(softphoneStateHandler3.mUserSwitchState);
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                            return true;
                        } else if (i == 1030) {
                            SoftphoneStateHandler softphoneStateHandler4 = SoftphoneStateHandler.this;
                            softphoneStateHandler4.transitionTo(softphoneStateHandler4.mReloginState);
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            return true;
                        } else if (i == 1031) {
                            SoftphoneStateHandler softphoneStateHandler5 = SoftphoneStateHandler.this;
                            softphoneStateHandler5.transitionTo(softphoneStateHandler5.mAirplaneState);
                            SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                            SoftphoneStateHandler.this.mClient.resetCurrentAddresses();
                            return true;
                        } else if (i == 1033) {
                            SoftphoneStateHandler softphoneStateHandler6 = SoftphoneStateHandler.this;
                            softphoneStateHandler6.transitionTo(softphoneStateHandler6.mServiceOutState);
                            return true;
                        } else if (i != 1034) {
                            switch (i) {
                                case 7:
                                    break;
                                case 8:
                                    ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(108, message.arg1, message.arg2));
                                    return true;
                                case 9:
                                    ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(109, message.arg1, message.arg2));
                                    return true;
                                case 10:
                                    Message obtainMessage2 = SoftphoneStateHandler.this.obtainMessage(1010, message.arg1, message.arg2);
                                    obtainMessage2.setData(message.getData());
                                    ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage2);
                                    return true;
                                case 11:
                                    Message obtainMessage3 = SoftphoneStateHandler.this.obtainMessage(1011, message.arg1, message.arg2);
                                    obtainMessage3.setData(message.getData());
                                    ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage3);
                                    return true;
                                case 15:
                                    SoftphoneStateHandler.this.mClient.refreshToken(message.arg1, (long) message.arg2, message.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 3));
                                    return true;
                                case 1015:
                                    HttpResponseParams httpResponseParams = (HttpResponseParams) message.obj;
                                    SoftphoneStateHandler.this.mClient.processRefreshTokenResponse((AccessTokenResponse) SoftphoneResponseUtils.parseJsonResponse(httpResponseParams, AccessTokenResponse.class, 200), httpResponseParams != null ? httpResponseParams.getStatusCode() : -1, message.arg1, message.arg2);
                                    return true;
                                case 1020:
                                    SoftphoneStateHandler.this.deferMessage(message);
                                    return true;
                                case 1022:
                                    SoftphoneStateHandler.this.deferMessage(message);
                                    return true;
                                case SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REFRESHSTATE:
                                    SoftphoneStateHandler softphoneStateHandler7 = SoftphoneStateHandler.this;
                                    softphoneStateHandler7.transitionTo(softphoneStateHandler7.mRefreshState);
                                    return true;
                                case SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_ACTIVATEDSTATE:
                                    SoftphoneStateHandler softphoneStateHandler8 = SoftphoneStateHandler.this;
                                    softphoneStateHandler8.transitionTo(softphoneStateHandler8.mActivatedState);
                                    return true;
                                default:
                                    Class cls2 = ImsNetworkIdentifiersResponse.class;
                                    switch (i) {
                                        case 101:
                                            SoftphoneStateHandler.this.mClient.processImsNetworkIdentifiersResponse((ImsNetworkIdentifiersResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls2, 200), false, message.arg1, message.arg2 == 1, message.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 6));
                                            return true;
                                        case 102:
                                            SoftphoneStateHandler.this.mClient.processTermsAndConditionsResponse((TermsAndConditionsResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, TermsAndConditionsResponse.class, 200), message.arg1);
                                            return true;
                                        case 103:
                                            SoftphoneStateHandler.this.mClient.processProvisionAccountResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls, 204), message.arg1);
                                            return true;
                                        case 104:
                                            SoftphoneStateHandler.this.mClient.processImsNetworkIdentifiersResponse((ImsNetworkIdentifiersResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls2, 200), true, message.arg1, message.arg2 == 1, message.getData().getInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 6));
                                            return true;
                                        default:
                                            switch (i) {
                                                case 106:
                                                    AddressValidationResponse addressValidationResponse = (AddressValidationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, AddressValidationResponse.class, 201);
                                                    addressValidationResponse.mTransactionId = message.arg1;
                                                    addressValidationResponse.mAddressId = message.arg2;
                                                    Bundle data = message.getData();
                                                    addressValidationResponse.mConfirmed = data.getBoolean(SoftphoneNamespaces.SoftphoneSettings.CONFIRMED, false);
                                                    SoftphoneStateHandler.this.mClient.processValidateE911AddressResponse(addressValidationResponse, data.getInt("retry_count", 3));
                                                    return true;
                                                case 107:
                                                    SoftphoneStateHandler.this.mClient.processAddE911AddressResponse((AddAddressResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, AddAddressResponse.class, 200), message.arg1, message.arg2);
                                                    return true;
                                                case 108:
                                                    SoftphoneStateHandler.this.mClient.processGetCallWaitingInfoResponse((CallWaitingResponse) SoftphoneResponseUtils.parseXmlResponse((HttpResponseParams) message.obj, CallWaitingResponse.class, 200, false), message.arg1);
                                                    return true;
                                                case 109:
                                                    HttpResponseParams httpResponseParams2 = (HttpResponseParams) message.obj;
                                                    if (httpResponseParams2 != null) {
                                                        String dataString = httpResponseParams2.getDataString();
                                                        if (dataString != null) {
                                                            dataString = dataString.replace("<cp:conditions></cp:conditions>", "<cp:conditions><ss:unconditional/></cp:conditions>");
                                                        }
                                                        httpResponseParams2.setDataString(dataString);
                                                    }
                                                    SoftphoneStateHandler.this.mClient.processGetCallForwardingInfoResponse((CallForwardingResponse) SoftphoneResponseUtils.parseXmlResponse(httpResponseParams2, CallForwardingResponse.class, 200, false), message.arg1);
                                                    return true;
                                                default:
                                                    String str = SoftphoneStateHandler.this.LOG_TAG;
                                                    IMSLog.e(str, "Unexpected event : current status is " + SoftphoneStateHandler.this.getAccountStatus());
                                                    return false;
                                            }
                                    }
                            }
                        } else {
                            SoftphoneStateHandler.this.mClient.processAkaChallengeResponse((AkaAuthenticationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, AkaAuthenticationResponse.class, 200), message.arg1, message.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE));
                            return true;
                        }
                    } else {
                        Message obtainMessage4 = SoftphoneStateHandler.this.obtainMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_REQUEST_AKA_CHALLENGE_DONE, message.arg1, message.arg2);
                        obtainMessage4.setData(message.getData());
                        ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage4);
                        return true;
                    }
                }
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(107, message.arg1, message.arg2));
                return true;
            } else {
                Message obtainMessage5 = SoftphoneStateHandler.this.obtainMessage(106, message.arg1, message.arg2);
                obtainMessage5.setData(message.getData());
                ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage5);
                return true;
            }
        }
    }

    class ActivatedState extends State {
        ActivatedState() {
        }

        public boolean processMessage(Message message) {
            SoftphoneStateHandler.this.mEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i != 14) {
                boolean z = false;
                if (i == 1014) {
                    SoftphoneStateHandler.this.mClient.notifyRegisterStatus(false, "AKA failed.");
                    SoftphoneStateHandler.this.mClient.getAutoRetryComSet(false, true);
                    SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                    SoftphoneStateHandler.this.sendMessage(1018);
                    return true;
                } else if (i == 16) {
                    SoftphoneStateHandler.this.mClient.obtainPdCookies(message.arg1);
                    return true;
                } else if (i == 17) {
                    SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                    return true;
                } else if (i == 1016) {
                    SoftphoneStateHandler.this.mClient.broadcastExplicitIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_REGISTERED, (String) message.obj);
                    SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                    softphoneStateHandler.transitionTo(softphoneStateHandler.mRegisteredState);
                    return true;
                } else if (i != 1017) {
                    if (i == 1028) {
                        SoftphoneClient softphoneClient = SoftphoneStateHandler.this.mClient;
                        int i2 = message.arg1;
                        if (message.arg2 == 1) {
                            z = true;
                        }
                        softphoneClient.reLogin(i2, z);
                        return true;
                    } else if (i != 1029) {
                        return false;
                    } else {
                        SoftphoneStateHandler.this.deferMessage(message);
                        return true;
                    }
                } else if (message.arg1 == 1408) {
                    return true;
                } else {
                    SoftphoneStateHandler.this.mClient.notifyRegisterStatus(false, (String) null);
                    return true;
                }
            } else {
                SoftphoneStateHandler.this.mClient.handleTryRegisterRequest();
                return true;
            }
        }
    }

    class RegisteredState extends State {
        RegisteredState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler.this.mClient.notifyRegisterStatus(true, (String) null);
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i != 14) {
                if (i == 1025) {
                    SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                    softphoneStateHandler.mClient.updateAccountStatus(softphoneStateHandler.mAccountId, 4);
                } else if (i == 1029) {
                    SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
                    SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                    softphoneStateHandler2.transitionTo(softphoneStateHandler2.mRefreshState);
                    return true;
                } else if (i != 1030) {
                    switch (i) {
                        case 1017:
                            SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) message.obj);
                            SoftphoneStateHandler softphoneStateHandler3 = SoftphoneStateHandler.this;
                            softphoneStateHandler3.transitionTo(softphoneStateHandler3.mActivatedState);
                            return true;
                        case 1018:
                            SoftphoneStateHandler softphoneStateHandler4 = SoftphoneStateHandler.this;
                            softphoneStateHandler4.transitionTo(softphoneStateHandler4.mDeactivatingState);
                            return true;
                        case 1019:
                            SoftphoneStateHandler.this.mClient.handleLabelUpdated();
                            return true;
                        case 1020:
                            ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(1021, message.arg1, message.arg2));
                            return true;
                        case 1021:
                            SoftphoneStateHandler.this.mClient.processObtainPdCookiesResponse((HttpResponseParams) message.obj, message.arg1);
                            return true;
                        case 1022:
                            ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(1023, message.arg1, message.arg2));
                            return true;
                        case 1023:
                            SoftphoneStateHandler.this.mClient.processSendSMSResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, SoftphoneResponse.class, 200), message.arg1);
                            return true;
                    }
                } else {
                    SoftphoneStateHandler softphoneStateHandler5 = SoftphoneStateHandler.this;
                    softphoneStateHandler5.transitionTo(softphoneStateHandler5.mReloginState);
                    return true;
                }
                return false;
            }
            SoftphoneStateHandler.this.mClient.notifyRegisterStatus(true, (String) null);
            return true;
        }
    }

    class RefreshState extends State {
        RefreshState() {
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 5) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, message.arg1, message.arg2));
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mReadyState);
            } else if (i != 105) {
                return false;
            } else {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, SoftphoneResponse.class, 204), message.arg1);
            }
            return true;
        }
    }

    private class AirplaneState extends State {
        private AirplaneState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
            SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
            softphoneStateHandler.mClient.updateAccountStatus(softphoneStateHandler.mAccountId, 4);
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 5) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, message.arg1, message.arg2));
            } else if (i == 101) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mReadyState);
                return false;
            } else if (i == 105) {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, SoftphoneResponse.class, 204), message.arg1);
            } else if (i != 1017) {
                if (i != 1032) {
                    return false;
                }
                SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
            }
            return true;
        }
    }

    class ServiceOut extends State {
        ServiceOut() {
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            if (message.what != 1032) {
                return false;
            }
            SoftphoneStateHandler.this.mClient.handleDeRegisterRequest();
            SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
            softphoneStateHandler.transitionTo(softphoneStateHandler.mRefreshState);
            return true;
        }
    }

    class ReleasingState extends State {
        ReleasingState() {
        }

        public void enter() {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd(SoftphoneStateHandler.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 0) {
                SoftphoneStateHandler.this.mClient.notifyProgress(new GeneralNotify(0, false, "Logout is in progress. Please try again later."));
            } else if (i == 5) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(105, message.arg1, message.arg2));
            } else if (i == 105) {
                SoftphoneStateHandler.this.mClient.processReleaseImsNetworkIdentitiesResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, SoftphoneResponse.class, 204), message.arg1);
            } else if (i == 1017) {
                SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
                SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_DEREGISTERED, (String) message.obj);
            } else if (i == 1034) {
                String string = message.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE);
                SoftphoneStateHandler.this.mClient.processAkaChallengeResponse((AkaAuthenticationResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, AkaAuthenticationResponse.class, 200), message.arg1, string);
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IDENTITY_RELEASED, (String) null);
            } else if (i != 19) {
                return false;
            } else {
                Message obtainMessage = SoftphoneStateHandler.this.obtainMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_REQUEST_AKA_CHALLENGE_DONE, message.arg1, message.arg2);
                obtainMessage.setData(message.getData());
                ((SoftphoneHttpTransaction) message.obj).commit(obtainMessage);
            }
            return true;
        }
    }

    class UserSwitchState extends State {
        UserSwitchState() {
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i != 1017) {
                if (i == 1026) {
                    SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                    softphoneStateHandler.mClient.updateAccountStatus(softphoneStateHandler.mAccountId, 2);
                } else if (i != 1032) {
                    return false;
                } else {
                    SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                    softphoneStateHandler2.transitionTo(softphoneStateHandler2.mReadyState);
                    SoftphoneStateHandler.this.mClient.getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
                }
            }
            return true;
        }
    }

    class DeactivatingState extends State {
        DeactivatingState() {
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 12) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(1012, message.arg1, message.arg2));
            } else if (i == 13) {
                ((SoftphoneHttpTransaction) message.obj).commit(SoftphoneStateHandler.this.obtainMessage(1013, message.arg1, message.arg2));
            } else if (i == 18) {
                SoftphoneStateHandler.this.mClient.revokeAccessToken();
            } else if (i == 1017) {
                SoftphoneStateHandler.this.mClient.releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
            } else if (i != 1036) {
                Class cls = SoftphoneResponse.class;
                if (i == 1012) {
                    SoftphoneStateHandler.this.mClient.processRevokeAccessTokenResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls, 200));
                } else if (i != 1013) {
                    return false;
                } else {
                    SoftphoneStateHandler.this.mClient.processRevokeRefreshTokenResponse((SoftphoneResponse) SoftphoneResponseUtils.parseJsonResponse((HttpResponseParams) message.obj, cls, 200));
                }
            } else {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mInitialState);
            }
            return true;
        }
    }

    class ReloginState extends State {
        ReloginState() {
        }

        public boolean processMessage(Message message) {
            SimpleEventLog simpleEventLog = SoftphoneStateHandler.this.mEventLog;
            simpleEventLog.logAndAdd("state: " + SoftphoneStateHandler.this.getCurrentState().getName() + ", msg: " + message.what);
            int i = message.what;
            if (i == 18) {
                SoftphoneStateHandler softphoneStateHandler = SoftphoneStateHandler.this;
                softphoneStateHandler.transitionTo(softphoneStateHandler.mInitialState);
            } else if (i != 1018) {
                return false;
            } else {
                SoftphoneStateHandler softphoneStateHandler2 = SoftphoneStateHandler.this;
                softphoneStateHandler2.transitionTo(softphoneStateHandler2.mDeactivatingState);
            }
            return true;
        }
    }
}

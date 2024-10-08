package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.RcsService;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaMessagingSession;
import com.gsma.services.rcs.extension.MultimediaSession;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class MultimediaMessagingSessionImpl extends IMultimediaMessagingSession.Stub {
    private static final String ENRICHED_CALL_PREFIX = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.";
    static final String LOG_TAG = "MMessagingSessionImpl";
    private final ImSession mSession;
    private final ISessionModule mSessionModule;

    public void flushMessages() throws RemoteException {
    }

    public MultimediaMessagingSessionImpl(ISessionModule iSessionModule, ImSession imSession) {
        this.mSessionModule = iSessionModule;
        this.mSession = imSession;
    }

    public void abortSession() throws ServerApiException {
        Log.d(LOG_TAG, "mypid: " + Process.myPid() + " calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid() + " called abortSession");
        this.mSessionModule.abortSession(this.mSession.getChatId());
    }

    public void acceptInvitation() throws ServerApiException {
        Log.d(LOG_TAG, "mypid: " + Process.myPid() + " calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid() + " called acceptInvitation");
        this.mSession.acceptSession(false);
    }

    public RcsService.Direction getDirection() {
        if (this.mSession.getDirection() == ImDirection.OUTGOING) {
            return RcsService.Direction.OUTGOING;
        }
        return RcsService.Direction.INCOMING;
    }

    public MultimediaSession.ReasonCode getReasonCode() {
        MultimediaSession.ReasonCode reasonCode = MultimediaSession.ReasonCode.UNSPECIFIED;
        ImSessionClosedEvent imSessionClosedEvent = this.mSession.getImSessionClosedEvent();
        if (imSessionClosedEvent == null) {
            return reasonCode;
        }
        MultimediaSession.ReasonCode translateErrorCode = translateErrorCode(imSessionClosedEvent.mResult.getImError());
        Log.d(LOG_TAG, "getReasonCode, event.mReason=" + imSessionClosedEvent.mResult.getImError() + " reasonCode=" + translateErrorCode);
        return translateErrorCode;
    }

    public ContactId getRemoteContact() {
        ImsUri remoteUri = this.mSession.getRemoteUri();
        if (remoteUri != null) {
            return new ContactId(remoteUri.getMsisdn());
        }
        return null;
    }

    public String getServiceId() {
        return translateServiceId(this.mSession.getServiceId());
    }

    private static String translateServiceId(String str) {
        Preconditions.checkNotNull(str);
        return str.startsWith(ENRICHED_CALL_PREFIX) ? str.substring(str.lastIndexOf("gsma")) : str;
    }

    public String getSessionId() {
        return this.mSession.getChatId();
    }

    public MultimediaSession.State getState() {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[this.mSession.getDetailedState().ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    return MultimediaSession.State.STARTED;
                }
                if (i == 4) {
                    return MultimediaSession.State.ABORTED;
                }
                if (i != 5) {
                    return MultimediaSession.State.FAILED;
                }
                return MultimediaSession.State.ABORTED;
            } else if (this.mSession.getDirection() == ImDirection.OUTGOING) {
                return MultimediaSession.State.INITIATING;
            } else {
                return MultimediaSession.State.ACCEPTING;
            }
        } else if (this.mSession.getDirection() == ImDirection.OUTGOING) {
            return MultimediaSession.State.INITIATING;
        } else {
            return MultimediaSession.State.INVITED;
        }
    }

    public void rejectInvitation() throws ServerApiException {
        Log.d(LOG_TAG, "mypid: " + Process.myPid() + " calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid() + " called rejectInvitation");
        this.mSession.rejectSession();
    }

    public void rejectInvitation2(MultimediaSession.ReasonCode reasonCode) throws ServerApiException {
        Log.d(LOG_TAG, "mypid: " + Process.myPid() + " calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid() + " called rejectInvitation2 " + reasonCode);
        this.mSession.rejectSession();
    }

    public void sendMessage(byte[] bArr, String str) throws ServerApiException {
        Log.d(LOG_TAG, "contentType: " + str);
        MultimediaSession.State state = getState();
        if (state == MultimediaSession.State.INITIATING || state == MultimediaSession.State.ACCEPTING || state == MultimediaSession.State.STARTED) {
            this.mSessionModule.sendMultimediaMessage(this.mSession.getChatId(), bArr, str);
            return;
        }
        throw new ServerApiException("Session not started");
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaMessagingSessionImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState;

        /* JADX WARNING: Can't wrap try/catch for region: R(37:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|(2:33|34)|35|37|38|39|40|41|42|43|44|(3:45|46|48)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(39:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|37|38|39|40|41|42|43|44|(3:45|46|48)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(42:0|1|2|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|37|38|39|40|41|42|43|44|45|46|48) */
        /* JADX WARNING: Can't wrap try/catch for region: R(43:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|37|38|39|40|41|42|43|44|45|46|48) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00ad */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00b7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00c1 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00cb */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.BUSY_HERE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r3 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_PARTY_DECLINED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r4 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r5 = com.sec.internal.constants.ims.servicemodules.im.ImError.INVALID_REQUEST     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.SERVICE_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r7 = 6
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_NO_WARNING_HEADER     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r7 = 7
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r7 = 8
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_ACCEPTABLE_HERE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r7 = 9
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.REQUEST_PENDING     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r7 = 10
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r7 = 11
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_IMPLEMENTED     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r7 = 12
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.SERVER_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x009c }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r7 = 13
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState[] r5 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.values()
                int r5 = r5.length
                int[] r5 = new int[r5]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState = r5
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r6 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.INITIAL     // Catch:{ NoSuchFieldError -> 0x00ad }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ad }
                r5[r6] = r1     // Catch:{ NoSuchFieldError -> 0x00ad }
            L_0x00ad:
                int[] r1 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x00b7 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r5 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.STARTING     // Catch:{ NoSuchFieldError -> 0x00b7 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b7 }
                r1[r5] = r0     // Catch:{ NoSuchFieldError -> 0x00b7 }
            L_0x00b7:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x00c1 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r1 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x00c1 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c1 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c1 }
            L_0x00c1:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x00cb }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r1 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSING     // Catch:{ NoSuchFieldError -> 0x00cb }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cb }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x00cb }
            L_0x00cb:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x00d5 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r1 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSED     // Catch:{ NoSuchFieldError -> 0x00d5 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d5 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x00d5 }
            L_0x00d5:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaMessagingSessionImpl.AnonymousClass1.<clinit>():void");
        }
    }

    private static MultimediaSession.ReasonCode translateErrorCode(ImError imError) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()]) {
            case 1:
                return MultimediaSession.ReasonCode.REJECT_REASON_BUSY;
            case 2:
                return MultimediaSession.ReasonCode.REJECT_REASON_DECLINE;
            case 3:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_UNAVAILABLE;
            case 4:
                return MultimediaSession.ReasonCode.REJECT_REASON_BAD_REQUEST;
            case 5:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQ_TERMINATED;
            case 6:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVICE_UNAVAILABLE;
            case 7:
                return MultimediaSession.ReasonCode.REJECT_REASON_USER_CALL_BLOCK;
            case 8:
                return MultimediaSession.ReasonCode.REJECTED_BY_TIMEOUT;
            case 9:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_NOT_ACCEPTABLE;
            case 10:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQUEST_PENDING;
            case 11:
                return MultimediaSession.ReasonCode.REJECT_REASON_REMOTE_USER_INVALID;
            case 12:
                return MultimediaSession.ReasonCode.REJECT_REASON_NOT_IMPLEMENTED;
            case 13:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVER_TIMEOUT;
            default:
                return MultimediaSession.ReasonCode.UNSPECIFIED;
        }
    }
}

package com.sec.internal.ims.servicemodules.im.translator;

import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;

public class TranslatorCollection {
    private static final String LOG_TAG = "TranslatorCollection";

    private TranslatorCollection() {
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.translator.TranslatorCollection$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus = r0
                com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.CONNECTED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.PENDING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.translator.TranslatorCollection.AnonymousClass1.<clinit>():void");
        }
    }

    public static ImParticipant.Status translateEngineParticipantInfo(ImConferenceParticipantInfo imConferenceParticipantInfo, ImParticipant imParticipant) {
        ImConferenceParticipantInfo.ImConferenceParticipantStatus imConferenceParticipantStatus;
        if (!(imConferenceParticipantInfo == null || (imConferenceParticipantStatus = imConferenceParticipantInfo.mParticipantStatus) == null)) {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus[imConferenceParticipantStatus.ordinal()];
            if (i == 1) {
                return ImParticipant.Status.ACCEPTED;
            }
            if (i == 2) {
                ImConferenceParticipantInfo.ImConferenceDisconnectionReason imConferenceDisconnectionReason = imConferenceParticipantInfo.mDisconnectionReason;
                if (imConferenceDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.DEPARTED || imConferenceParticipantInfo.mUserElemState == ImConferenceParticipantInfo.ImConferenceUserElemState.DELETED) {
                    return ImParticipant.Status.DECLINED;
                }
                if (imConferenceDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.FAILED) {
                    if (imConferenceParticipantInfo.mDisconnectionCause == ImError.REMOTE_PARTY_DECLINED) {
                        return ImParticipant.Status.DECLINED;
                    }
                    return ImParticipant.Status.FAILED;
                } else if (imConferenceDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.BOOTED) {
                    return ImParticipant.Status.GONE;
                } else {
                    return ImParticipant.Status.FAILED;
                }
            } else if (i == 3) {
                return (imParticipant == null || imParticipant.getStatus() != ImParticipant.Status.ACCEPTED) ? ImParticipant.Status.TO_INVITE : ImParticipant.Status.PENDING;
            } else {
                Log.i(LOG_TAG, "No translation for the following Engine's participant Status: " + imConferenceParticipantInfo.mParticipantStatus);
            }
        }
        return null;
    }
}

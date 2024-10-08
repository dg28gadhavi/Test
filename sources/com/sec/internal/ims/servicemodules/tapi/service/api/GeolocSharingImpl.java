package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharing;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;

public class GeolocSharingImpl extends IGeolocSharing.Stub {
    private static final String LOG_TAG = GeolocSharingImpl.class.getSimpleName();
    private final FtMessage mGeoMsg;
    private final IGlsModule mGlsModule;

    public GeolocSharingImpl(FtMessage ftMessage, IGlsModule iGlsModule) {
        this.mGeoMsg = ftMessage;
        this.mGlsModule = iGlsModule;
    }

    public String getSharingId() throws ServerApiException {
        Log.i(LOG_TAG, "getSharingId()");
        return String.valueOf(this.mGeoMsg.getId());
    }

    public Geoloc getGeoloc() throws ServerApiException {
        Log.i(LOG_TAG, "getGeoloc()");
        FtMessage ftMessage = this.mGeoMsg;
        if (ftMessage == null || !MIMEContentType.LOCATION_PUSH.equals(ftMessage.getContentType())) {
            return null;
        }
        return getGeoInfo(this.mGeoMsg.getExtInfo());
    }

    public ContactId getRemoteContact() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "getRemoteContact=" + this.mGeoMsg.getRemoteUri());
        if (this.mGeoMsg.getRemoteUri() != null) {
            return new ContactId(this.mGeoMsg.getRemoteUri().toString());
        }
        return null;
    }

    public GeolocSharing.State getState() throws ServerApiException {
        GeolocSharing.State state = GeolocSharing.State.INVITED;
        ImDirection direction = this.mGeoMsg.getDirection();
        int stateId = this.mGeoMsg.getStateId();
        if (!(stateId == 0 || stateId == 1)) {
            if (stateId == 2) {
                return GeolocSharing.State.STARTED;
            }
            if (stateId == 3) {
                return GeolocSharing.State.RINGING;
            }
            if (stateId != 4) {
                if (stateId != 6) {
                    if (stateId != 7) {
                        return GeolocSharing.State.INVITED;
                    }
                }
            }
            if (ImDirection.INCOMING == direction) {
                return GeolocSharing.State.REJECTED;
            }
            if (ImDirection.OUTGOING == direction) {
                return GeolocSharing.State.ABORTED;
            }
            return state;
        }
        if (ImDirection.INCOMING == direction) {
            return GeolocSharing.State.INVITED;
        }
        return ImDirection.OUTGOING == direction ? GeolocSharing.State.INITIATING : state;
    }

    public GeolocSharing.ReasonCode getReasonCode() throws ServerApiException {
        GeolocSharing.ReasonCode reasonCode = GeolocSharing.ReasonCode.UNSPECIFIED;
        CancelReason cancelReason = this.mGeoMsg.getCancelReason();
        FtRejectReason rejectReason = this.mGeoMsg.getRejectReason();
        if (rejectReason != null) {
            return translatorRejectReason(rejectReason);
        }
        return GeolocSharingServiceImpl.translateToReasonCode(cancelReason);
    }

    public String getMaapTrafficType() throws ServerApiException {
        String maapTrafficType = this.mGeoMsg.getMaapTrafficType();
        String str = LOG_TAG;
        Log.i(str, "getMaapTrafficType, maapTrafficType = [" + maapTrafficType + "]");
        return maapTrafficType;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason;

        static {
            int[] iArr = new int[FtRejectReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason = iArr;
            try {
                iArr[FtRejectReason.DECLINE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    private GeolocSharing.ReasonCode translatorRejectReason(FtRejectReason ftRejectReason) {
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason[ftRejectReason.ordinal()] != 1) {
            return GeolocSharing.ReasonCode.UNSPECIFIED;
        }
        return GeolocSharing.ReasonCode.FAILED_INITIATION;
    }

    public int getDirection() throws ServerApiException {
        return this.mGeoMsg.getDirection().getId();
    }

    public void acceptInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Accept session invitation,id=" + this.mGeoMsg.getId());
        this.mGlsModule.acceptLocationShare(this.mGeoMsg.getImdnId(), this.mGeoMsg.getChatId(), this.mGeoMsg.getContentUri());
    }

    public void rejectInvitation() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Reject session invitation,id=" + this.mGeoMsg.getId());
        this.mGlsModule.rejectLocationShare(this.mGeoMsg.getImdnId(), this.mGeoMsg.getChatId());
    }

    public void abortSharing() throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Abort session invitation,id=" + this.mGeoMsg.getId());
        if (this.mGeoMsg.getStateId() != 3) {
            this.mGlsModule.cancelLocationShare(this.mGeoMsg.getImdnId(), this.mGeoMsg.getDirection(), this.mGeoMsg.getChatId());
        }
    }

    private Geoloc getGeoInfo(String str) {
        String str2;
        if (str == null) {
            Log.d(LOG_TAG, "geolocation extinfo is null");
            return null;
        }
        String[] split = str.split(",");
        double doubleValue = Double.valueOf(split[0]).doubleValue();
        double doubleValue2 = Double.valueOf(split[1]).doubleValue();
        float floatValue = Float.valueOf(split[2]).floatValue();
        long longValue = Long.valueOf(split[3]).longValue();
        if (split.length != 5) {
            str2 = "";
        } else {
            str2 = split[4];
        }
        return new Geoloc(str2, doubleValue, doubleValue2, longValue, floatValue);
    }

    public long getTimeStamp() throws RemoteException {
        return this.mGeoMsg.getInsertedTimestamp();
    }
}

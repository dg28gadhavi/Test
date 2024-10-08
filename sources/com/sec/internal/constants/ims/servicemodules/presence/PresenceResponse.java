package com.sec.internal.constants.ims.servicemodules.presence;

import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.servicemodules.ss.UtConstant;
import java.util.Locale;

public class PresenceResponse {
    private PresenceFailureReason mError;
    private String mErrorPhrase;
    private int mPhoneId;
    private int mRetryTime;
    private int mSipErrorCode;
    private String mSubscribeId;
    private String mSubscribeTerminatedReason;
    private boolean mSuccess;

    public enum PresenceStatusCode {
        NONE,
        PRESENCE_AT_BAD_EVENT,
        PRESENCE_AT_NOT_PROVISIONED,
        PRESENCE_AT_NOT_REGISTERED,
        PRESENCE_RE_REGISTRATION,
        PRESENCE_RETRY_EXP_BACKOFF,
        PRESENCE_REQUEST_TIMEOUT,
        PRESENCE_REQUIRE_FULL_PUBLISH,
        PRESENCE_NO_SUBSCRIBE,
        PRESENCE_NOT_FOUND,
        PRESENCE_FORBIDDEN,
        PRESENCE_DISABLE_MODE,
        PRESENCE_NO_RESPONSE,
        PRESENCE_REQUIRE_RETRY_PUBLISH,
        PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER,
        PRESENCE_INTERVAL_TOO_SHORT
    }

    public PresenceResponse(boolean z, int i, String str, int i2, int i3) {
        this.mSuccess = z;
        this.mSipErrorCode = i;
        this.mErrorPhrase = str;
        if (!z) {
            this.mError = translateSipError(i, str);
        }
        this.mRetryTime = i2;
        this.mPhoneId = i3;
    }

    public PresenceResponse(boolean z, String str, String str2, int i) {
        this.mSubscribeId = str2;
        this.mSuccess = z;
        this.mPhoneId = i;
        this.mSubscribeTerminatedReason = str;
    }

    public boolean isSuccess() {
        return this.mSuccess;
    }

    public PresenceFailureReason getReason() {
        return this.mError;
    }

    public int getSipError() {
        return this.mSipErrorCode;
    }

    public String getErrorDescription() {
        return this.mErrorPhrase;
    }

    public int getRetryTime() {
        return this.mRetryTime;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getSubscribeId() {
        return this.mSubscribeId;
    }

    public String getSubscribeTerminatedReason() {
        return this.mSubscribeTerminatedReason;
    }

    private PresenceFailureReason translateSipError(int i, String str) {
        PresenceFailureReason presenceFailureReason = PresenceFailureReason.UNSPECIFIED_ERROR;
        String lowerCase = str != null ? str.toLowerCase(Locale.US) : "";
        if (i >= 900 && i < 1000) {
            i = 999;
        }
        switch (i) {
            case 400:
                return PresenceFailureReason.INVALID_REQUEST;
            case 403:
                return checkReasonPhrase(i, lowerCase);
            case 404:
                return PresenceFailureReason.USER_NOT_FOUND;
            case 405:
                return PresenceFailureReason.METHOD_NOT_ALLOWED;
            case 408:
                return checkReasonPhrase(i, lowerCase);
            case UtConstant.UtError.HTTP_412_PRECONDITION_FAILED /*412*/:
                return PresenceFailureReason.CONDITIONAL_REQUEST_FAILED;
            case 413:
                return PresenceFailureReason.ENTITY_TOO_LARGE;
            case AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE:
                return PresenceFailureReason.UNSUPPORTED_MEDIA_TYPE;
            case 423:
                return PresenceFailureReason.INTERVAL_TOO_SHORT;
            case NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE:
                return PresenceFailureReason.TEMPORARILY_UNAVAILABLE;
            case NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE:
                return PresenceFailureReason.BUSY_HERE;
            case 489:
                return PresenceFailureReason.BAD_EVENT;
            case 500:
                return PresenceFailureReason.SERVER_INTERNAL_ERROR;
            case 503:
                return PresenceFailureReason.SERVICE_UNAVAILABLE;
            case 600:
                return PresenceFailureReason.BUSY_EVERYWHERE;
            case Id.REQUEST_UPDATE_TIME_IN_PLANI /*603*/:
                return PresenceFailureReason.DECLINE;
            case 604:
                return PresenceFailureReason.DOES_NOT_EXIST_ANYWHERE;
            case 708:
            case 999:
                return PresenceFailureReason.NO_RESPONSE;
            default:
                return presenceFailureReason;
        }
    }

    private PresenceFailureReason checkReasonPhrase(int i, String str) {
        PresenceFailureReason presenceFailureReason = PresenceFailureReason.UNSPECIFIED_ERROR;
        if (i != 403) {
            if (i != 408) {
                return presenceFailureReason;
            }
            if (str.contains("transaction timeout")) {
                return PresenceFailureReason.REQUEST_TIMEOUT_RETRY;
            }
            return PresenceFailureReason.REQUEST_TIMEOUT;
        } else if (str.contains("not authorized for presence")) {
            return PresenceFailureReason.USER_NOT_PROVISIONED;
        } else {
            if (str.contains("user not registered")) {
                return PresenceFailureReason.USER_NOT_REGISTERED;
            }
            return PresenceFailureReason.FORBIDDEN;
        }
    }

    public enum PresenceFailureReason {
        INVALID_REQUEST,
        USER_NOT_REGISTERED,
        USER_NOT_PROVISIONED,
        FORBIDDEN,
        USER_NOT_FOUND,
        METHOD_NOT_ALLOWED,
        REQUEST_TIMEOUT,
        REQUEST_TIMEOUT_RETRY,
        CONDITIONAL_REQUEST_FAILED,
        ENTITY_TOO_LARGE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERVAL_TOO_SHORT,
        TEMPORARILY_UNAVAILABLE,
        BUSY_HERE,
        BAD_EVENT,
        SERVER_INTERNAL_ERROR,
        SERVICE_UNAVAILABLE,
        BUSY_EVERYWHERE,
        DECLINE,
        DOES_NOT_EXIST_ANYWHERE,
        UNSPECIFIED_ERROR,
        NO_RESPONSE;

        public boolean isOneOf(PresenceFailureReason... presenceFailureReasonArr) {
            for (PresenceFailureReason presenceFailureReason : presenceFailureReasonArr) {
                if (this == presenceFailureReason) {
                    return true;
                }
            }
            return false;
        }
    }

    public String toString() {
        return "PresenceResponse [mSuccess=" + this.mSuccess + ", mSipErrorCode=" + this.mSipErrorCode + ", mError=" + this.mError + ", mErrorPhrase=" + this.mErrorPhrase + ", mRetryTime=" + this.mRetryTime + ", phoneId=" + this.mPhoneId + "]";
    }
}

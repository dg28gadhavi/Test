package com.sec.sve.generalevent;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.ImsConstants;

public class BuiltVcidEvent extends VcidEvent {
    private final Bundle mBundle;

    public BuiltVcidEvent(Builder builder) {
        this.mEvent = builder.mEvent;
        this.mBundle = builder.mBundle;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public static class Builder {
        private String mAction;
        /* access modifiers changed from: private */
        public final Bundle mBundle = new Bundle();
        /* access modifiers changed from: private */
        public final String mEvent = "VCID";
        private String mFileUrl;
        private String mServiceType;
        private int mSessionId = -1;
        private int mSubId = -1;

        public BuiltVcidEvent build() {
            if (!TextUtils.isEmpty(this.mAction)) {
                this.mBundle.putString("action", this.mAction);
            }
            if (!TextUtils.isEmpty(this.mFileUrl)) {
                this.mBundle.putString("fileURL", this.mFileUrl);
            }
            if (!TextUtils.isEmpty(this.mServiceType)) {
                this.mBundle.putString("serviceType", this.mServiceType);
            }
            int i = this.mSessionId;
            if (i != -1) {
                this.mBundle.putInt("sessionId", i);
            }
            int i2 = this.mSubId;
            if (i2 != -1) {
                this.mBundle.putInt(ImsConstants.Intents.EXTRA_RESET_NETWORK_SUBID, i2);
            }
            return new BuiltVcidEvent(this);
        }

        public Builder setAction(String str) {
            this.mAction = str;
            return this;
        }

        public Builder setFileUrl(String str) {
            this.mFileUrl = str;
            return this;
        }

        public Builder setServiceType(String str) {
            this.mServiceType = str;
            return this;
        }

        public Builder setSessionId(int i) {
            this.mSessionId = i;
            return this;
        }

        public Builder setSubId(int i) {
            this.mSubId = i;
            return this;
        }
    }

    public String toString() {
        return "BuiltVcidEvent [mEvent= " + this.mEvent + ", mBundle=" + this.mBundle + "]";
    }
}

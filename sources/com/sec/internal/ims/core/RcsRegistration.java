package com.sec.internal.ims.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RcsRegistration {
    private Map<String, String> mStringParams;

    public String getString(String str) {
        return (String) Optional.ofNullable(this.mStringParams.get(str)).orElse("");
    }

    public RcsRegistration(Builder builder) {
        AnonymousClass1 r0 = new HashMap<String, String>() {
            {
                put("UserPwd", "");
            }
        };
        this.mStringParams = r0;
        r0.put("UserPwd", builder.mPassword);
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public String mPassword;

        public Builder setPassword(String str) {
            this.mPassword = str;
            return this;
        }

        public RcsRegistration build() {
            return new RcsRegistration(this);
        }
    }
}

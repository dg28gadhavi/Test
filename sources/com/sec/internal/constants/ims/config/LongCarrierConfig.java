package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.interfaces.ims.config.ICarrierConfig;
import java.util.Optional;

public enum LongCarrierConfig implements ICarrierConfig {
    RCS_REQUEST_RETRY_INTERVAL("rcs_request_retry_interval_millis_long", "ims.rcs_request_retry_interval_millis_long");
    
    private final String mCarrierConfigName;
    private final String mGlobalSettingsName;

    private LongCarrierConfig(String str, String str2) {
        this.mGlobalSettingsName = str;
        this.mCarrierConfigName = str2;
    }

    public String getGlobalSettingsName() {
        return this.mGlobalSettingsName;
    }

    public void putOverrideConfig(PersistableBundle persistableBundle, JsonObject jsonObject) {
        Optional.ofNullable(jsonObject.get(this.mGlobalSettingsName)).ifPresent(new LongCarrierConfig$$ExternalSyntheticLambda0(this, persistableBundle));
    }
}

package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.interfaces.ims.config.ICarrierConfig;
import java.util.Optional;

public enum IntCarrierConfig implements ICarrierConfig {
    WIFI_OFF_DEFERRING_TIME("wifi_off_deferring_time_millis_int", "ims.wifi_off_deferring_time_millis_int"),
    NON_RCS_CAPABILITIES_CACHE_EXPIRATION("non_rcs_capabilities_cache_expiration_sec_int", "ims.non_rcs_capabilities_cache_expiration_sec_int");
    
    private final String mCarrierConfigName;
    private final String mGlobalSettingsName;

    private IntCarrierConfig(String str, String str2) {
        this.mGlobalSettingsName = str;
        this.mCarrierConfigName = str2;
    }

    public String getGlobalSettingsName() {
        return this.mGlobalSettingsName;
    }

    public void putOverrideConfig(PersistableBundle persistableBundle, JsonObject jsonObject) {
        Optional.ofNullable(jsonObject.get(this.mGlobalSettingsName)).ifPresent(new IntCarrierConfig$$ExternalSyntheticLambda0(this, persistableBundle));
    }
}

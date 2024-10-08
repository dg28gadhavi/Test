package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.interfaces.ims.config.ICarrierConfig;
import java.util.Optional;

public enum StringArrayCarrierConfig implements ICarrierConfig {
    PUBLISH_SERVICE_DESC_FEATURE_TAG_MAP_OVERRIDE("publish_service_desc_feature_tag_map_override_string_array", "ims.publish_service_desc_feature_tag_map_override_string_array"),
    RCS_FEATURE_TAG_ALLOWED("rcs_feature_tag_allowed_string_array", "ims.rcs_feature_tag_allowed_string_array");
    
    private static final String LOG_TAG = null;
    private final String mCarrierConfigName;
    private final String mGlobalSettingsName;

    static {
        LOG_TAG = StringArrayCarrierConfig.class.getSimpleName();
    }

    private StringArrayCarrierConfig(String str, String str2) {
        this.mGlobalSettingsName = str;
        this.mCarrierConfigName = str2;
    }

    public String getGlobalSettingsName() {
        return this.mGlobalSettingsName;
    }

    public void putOverrideConfig(PersistableBundle persistableBundle, JsonObject jsonObject) {
        Optional.ofNullable(jsonObject.get(this.mGlobalSettingsName)).ifPresent(new StringArrayCarrierConfig$$ExternalSyntheticLambda2(this, persistableBundle));
    }
}

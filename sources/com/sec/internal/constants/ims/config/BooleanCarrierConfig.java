package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.interfaces.ims.config.ICarrierConfig;
import java.util.Optional;

public enum BooleanCarrierConfig implements ICarrierConfig {
    IMS_SINGLE_REGISTRATION_REQUIRED("ims_single_registration_required_bool", "ims.ims_single_registration_required_bool"),
    ENABLE_PRESENCE_PUBLISH("enable_presence_publish_bool", "ims.enable_presence_publish_bool"),
    ENABLE_PRESENCE_CAPABILITY_EXCHANGE("enable_presence_capability_exchange_bool", "ims.enable_presence_capability_exchange_bool"),
    RCS_BULK_CAPABILITY_EXCHANGE("rcs_bulk_capability_exchange_bool", "ims.rcs_bulk_capability_exchange_bool"),
    ENABLE_PRESENCE_GROUP_SUBSCRIBE("enable_presence_group_subscribe_bool", "ims.enable_presence_group_subscribe_bool"),
    RCS_REQUEST_FORBIDDEN_BY_SIP_489("rcs_request_forbidden_by_sip_489_bool", "ims.rcs_request_forbidden_by_sip_489_bool"),
    USE_ACS_FOR_RCS("use_acs_for_rcs_bool", "use_acs_for_rcs_bool");
    
    private final String mCarrierConfigName;
    private final String mGlobalSettingsName;

    private BooleanCarrierConfig(String str, String str2) {
        this.mGlobalSettingsName = str;
        this.mCarrierConfigName = str2;
    }

    public String getGlobalSettingsName() {
        return this.mGlobalSettingsName;
    }

    public void putOverrideConfig(PersistableBundle persistableBundle, JsonObject jsonObject) {
        Optional.ofNullable(jsonObject.get(this.mGlobalSettingsName)).ifPresent(new BooleanCarrierConfig$$ExternalSyntheticLambda0(this, persistableBundle));
    }
}

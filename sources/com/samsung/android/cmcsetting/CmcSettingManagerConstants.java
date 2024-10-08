package com.samsung.android.cmcsetting;

public class CmcSettingManagerConstants {
    public static int KEY_NOT_EXIST = -2;
    public static String SETTINGS_KEY_CMC_IS_DUAL_SIM_SUPPORTED = "cmc_is_dual_sim_supported";
    public static String SETTINGS_KEY_CMC_IS_DUAL_SIM_SUPPORTED_ON_PD = "cmc_is_dual_sim_supported_on_pd";
    public static String SETTINGS_KEY_CMC_SELECTED_SIMS_ON_PD = "cmc_selected_sims_on_pd";
    public static int SUPPORTED = 1;

    public enum DeviceCategory {
        DEVICE_CATEGORY_PHONE,
        DEVICE_CATEGORY_TABLET,
        DEVICE_CATEGORY_BT_WATCH,
        DEVICE_CATEGORY_SPEAKER,
        DEVICE_CATEGORY_PC,
        DEVICE_CATEGORY_TV,
        DEVICE_CATEGORY_LAPTOP,
        DEVICE_CATEGORY_VST,
        DEVICE_CATEGORY_UNDEFINED
    }

    public enum DeviceType {
        DEVICE_TYPE_PD,
        DEVICE_TYPE_SD,
        DEVICE_TYPE_UNDEFINED
    }

    public enum NetworkMode {
        NETWORK_MODE_USE_MOBILE_NETWORK,
        NETWORK_MODE_WIFI_ONLY,
        NETWORK_MODE_UNDEFINED
    }
}

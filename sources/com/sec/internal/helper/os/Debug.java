package com.sec.internal.helper.os;

import android.os.Build;
import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;

public class Debug {
    public static final boolean ALLOW_DIAGNOSTICS = (Build.IS_ENG || Build.IS_DEBUGGABLE || !isProductShip());
    private static final String PRODUCT_SHIP_PROP = "ro.product_ship";

    public static boolean isProductShip() {
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(SemSystemProperties.get(PRODUCT_SHIP_PROP, CloudMessageProviderContract.JsonData.TRUE));
    }
}

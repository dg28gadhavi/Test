package com.sec.internal.interfaces.ims.config;

import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.ims.DiagnosisConstants;
import java.util.Map;

public interface IWorkflow {
    public static final int ACTIVE_AUTOCONFIG_VERSION = 1;
    public static final int DEFAULT_ERROR_CODE = 987;
    public static final int DISABLE_AUTOCONFIG_VERSION = -2;
    public static final int DISABLE_PERMANENTLY_AUTOCONFIG_VERSION = -1;
    public static final int DISABLE_TEMPORARY_AUTOCONFIG_VERSION = 0;
    public static final int DORMANT_AUTOCONFIG_VERSION = -3;

    void changeOpMode(boolean z) {
    }

    boolean checkNetworkConnectivity() {
        return false;
    }

    void cleanup();

    void clearAutoConfigStorage(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
    }

    void clearToken(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
    }

    void closeStorage();

    void dump() {
    }

    void forceAutoConfig(boolean z);

    void forceAutoConfigNeedResetConfig(boolean z);

    int getLastErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    IStorageAdapter getStorage();

    void handleMSISDNDialog();

    void init();

    boolean isConfigOngoing() {
        return false;
    }

    void onADSChanged() {
    }

    void onBootCompleted() {
    }

    void onDefaultSmsPackageChanged();

    void reInitIfNeeded() {
    }

    Map<String, String> read(String str);

    void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
    }

    void removeValidToken() {
    }

    void sendIidToken(String str) {
    }

    void sendMsisdnNumber(String str) {
    }

    void sendVerificationCode(String str) {
    }

    void setEnableRcsByMigration() {
    }

    void setRcsClientConfiguration(String str, String str2, String str3, String str4, String str5) {
    }

    void startAutoConfig(boolean z);

    void startAutoConfigDualsim(boolean z);

    void startCurConfig() {
    }

    void stopWorkFlow() {
    }

    void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
    }
}

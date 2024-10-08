package com.sec.internal.helper.os;

import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SystemUtil {
    private static final String LOG_TAG = "SystemUtil";

    public static boolean checkUltraPowerSavingMode(SemEmergencyManager semEmergencyManager) {
        return semEmergencyManager.checkModeType(512) || semEmergencyManager.checkModeType(1024);
    }

    public static boolean verifyCerts(X509Certificate[] x509CertificateArr) {
        try {
            TrustManagerFactory instance = TrustManagerFactory.getInstance("X509");
            instance.init((KeyStore) null);
            ((X509TrustManager) instance.getTrustManagers()[0]).checkServerTrusted(x509CertificateArr, "RSA");
            return true;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            Log.e(LOG_TAG, "Verification failed", e);
            return false;
        }
    }
}

package com.sec.internal.ims.servicemodules.quantumencryption;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.voltecrypt.service.SXICTQMVoLTECallBack;
import com.voltecrypt.service.SXICTQMVoLTECryptService;

public class QuantumEncryptionService extends SXICTQMVoLTECryptService.Stub {
    private static final String ALLOWED_PKG = "com.ctq.simkey.pivot";
    private static final String LOG_TAG = QuantumEncryptionService.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.QUANTUM_ENCRYPTION";
    private static final Signature QSS_SIGNATURE = new Signature("3082035f30820247a00302010202042adbb04c300d06092a864886f70d01010b05003060310e300c060355040613054368696e61310e300c06035504081305416e487569310e300c060355040713054865466569310e300c060355040a13055a44584c5a310e300c060355040b13055a44584c5a310e300c060355040313055a44584c5a301e170d3231313131303035343530335a170d3436313130343035343530335a3060310e300c060355040613054368696e61310e300c06035504081305416e487569310e300c060355040713054865466569310e300c060355040a13055a44584c5a310e300c060355040b13055a44584c5a310e300c060355040313055a44584c5a30820122300d06092a864886f70d01010105000382010f003082010a0282010100adad9c252bde1e99d43f782290b398b48e275ee18518ee57d08d911fc8c01dc0a98bef655d7cc865e47a0e461cbf0fddcd35ec2af30b78078d263071f3f55f4fa4bd353cf9ee8b71b80655a850cafd30216992206f05e22ebc44f3c3c5e4b540bd073bec3a2ed6ab06b7e003bb65630aa1bfa02b5a304f0871bd11929715a754e3af33b70b9d1b116f0b5030ea975e1add9e69b0a292e199a10bb604e67e89f9355c556c095ddd1c07503cc40992641c614081b32b8971abe32baba5950c7ef27d490bfdc948c97201ae29508608436fb80d42e467139a803ea05035304a7c47f7021e51b0303e40f9c4eebe498d9e33eb0e50b06f9ab6ba3a62ac8e99f434f90203010001a321301f301d0603551d0e04160414c7d20a5bdd87e10530f23cb00a5406db7d195b38300d06092a864886f70d01010b0500038201010008a45d1fe3b6e3d86f83f6724abe8b889e03a6368cdc66e984a256542efa9b147ccc0d1a463b7aa6fb1bd08835ec7851a4597b03ca2eb26de8144961848257b139341e262cfbd48a0c0fbeaa8cc0813e147206441e45d717244fb24058a976d576a7ecadb9a72be77184a794530e8a28687446326cf017a865a5be756d4ed96b955f8707844c8d90fb28179b52a3d71ae789cbc9b8977fc92b33f22ae39c3023365a95ae9e929336cba78a8feed5cadd9053fe35d37e18582eff4b0c3d5ece4e3a3106653b8a0397ff865aaffc47a4b355caaafe201ec406456f74c6243ef7891b84caa1ab0449160b450e448d539b55d5cf5aecddb85764f2a51c28b3354651");
    protected Context mContext = null;
    protected QuantumEncryptionServiceModule mServiceModule = null;

    public QuantumEncryptionService(ServiceModuleBase serviceModuleBase) {
        QuantumEncryptionServiceModule quantumEncryptionServiceModule = (QuantumEncryptionServiceModule) serviceModuleBase;
        this.mServiceModule = quantumEncryptionServiceModule;
        this.mContext = quantumEncryptionServiceModule.getContext();
    }

    public int registerVoLTECallback(SXICTQMVoLTECallBack sXICTQMVoLTECallBack) throws RemoteException {
        String str = LOG_TAG;
        Log.i(str, "registerVoLTECallback");
        if (isAllowed().booleanValue()) {
            return this.mServiceModule.registerVoLTECallback(sXICTQMVoLTECallBack);
        }
        Log.d(str, "registerVoLTECallback is not allowed");
        return -1;
    }

    public int notifyAuthenticationStatus(int i, String str, String str2) throws RemoteException {
        String str3 = LOG_TAG;
        Log.i(str3, "notifyAuthenticationStatus");
        if (isAllowed().booleanValue()) {
            return this.mServiceModule.notifyAuthenticationStatus(i, str, str2);
        }
        Log.d(str3, "notifyAuthenticationStatus is not allowed");
        return -1;
    }

    public int notifyPeerProfileStatus(int i, String str, String str2, String str3) throws RemoteException {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyPeerProfileStatus");
        if (isAllowed().booleanValue()) {
            return this.mServiceModule.notifyPeerProfileStatus(i, str, str2, str3);
        }
        Log.d(str4, "notifyPeerProfileStatus is not allowed");
        return -1;
    }

    public int notifyQMKeyStatus(int i, String str, String str2, byte[] bArr, String str3) throws RemoteException {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyQMKeyStatus");
        if (isAllowed().booleanValue()) {
            return this.mServiceModule.notifyQMKeyStatus(i, str, str2, bArr, str3);
        }
        Log.d(str4, "notifyQMKeyStatus is not allowed");
        return -1;
    }

    public int notifyVoLTEStatus(int i, String str) throws RemoteException {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyVoLTEStatus");
        if (!isAllowed().booleanValue()) {
            Log.d(str2, "notifyVoLTEStatus is not allowed");
            return -1;
        }
        this.mContext.enforceCallingOrSelfPermission("com.sec.imsservice.QUANTUM_ENCRYPTION", str2);
        return this.mServiceModule.notifyVoLTEStatus(i, str);
    }

    public void checkNeedGoClientApp(int i) throws RemoteException {
        String str = LOG_TAG;
        Log.i(str, "checkNeedGoClientApp");
        if (!isAllowed().booleanValue()) {
            Log.d(str, "checkNeedGoClientApp is not allowed");
        }
    }

    public void notifyLoginResult(int i, String str) throws RemoteException {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyLoginResult");
        if (!isAllowed().booleanValue()) {
            Log.d(str2, "notifyLoginResult is not allowed");
        } else {
            this.mServiceModule.notifyLoginResult(i, str);
        }
    }

    private Boolean isAllowed() {
        PackageManager packageManager = this.mContext.getPackageManager();
        for (String str : packageManager.getPackagesForUid(Binder.getCallingUid())) {
            if (ALLOWED_PKG.equals(str)) {
                try {
                    Signature[] apkContentsSigners = packageManager.getPackageInfo(str, 134217728).signingInfo.getApkContentsSigners();
                    if (apkContentsSigners != null && apkContentsSigners.length > 0) {
                        for (Signature equals : apkContentsSigners) {
                            if (QSS_SIGNATURE.equals(equals)) {
                                return Boolean.TRUE;
                            }
                        }
                        continue;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return Boolean.FALSE;
    }
}

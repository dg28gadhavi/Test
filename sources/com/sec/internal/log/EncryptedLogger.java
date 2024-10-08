package com.sec.internal.log;

import android.os.FileObserver;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class EncryptedLogger {
    private static final String B64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4HnSCdRy3WviYMvfRDtEcLAQU3Mi3et4f9W0ivmrMc1B+5LUEoWbrb6Rb5IKf7BI7qRflHKOfn1a9R1pYEBaBnrNrQHuIOhG4b3zYkAU+i093wKtE/dLvpa+NOEAfn/HMO0qVdRjdVs9FaJWYbjRNeiZC3PIX8bLFwqgOLwe70HOi9V7vcrrUyhJTMfXz77Zm1bbCMtU2R7UJUnI0b2fQyKdIhYgZiKChmfHH395939x2yQd8ZFYPGbmB+Zq4mCivEZSSaNZ6h9r6YYdoFSmgLVM1upBvt3kEpOE91TWbtIS4nLBWvLIfZTW4MA77BltW7mtkO61ZepLqkdj0eFoXQIDAQAB";
    private static int ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD = ((NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE + 1) * 2);
    private static int ENCRYPTED_LOGGER_LINE_MAX_PAYLOAD = 1600;
    private static final String KEY_POSTFIX = "⁝❜";
    private static final String KEY_PREFIX = "❛⁝";
    private static final String LOG_MIDFIX = "══";
    private static final String LOG_POSTFIX = "═╝";
    private static final String LOG_PREFIX = "╔═";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "EncryptedLogger";
    private static final String PublicKeyId = "R001";
    private static EncryptedLogger singleInstance;
    private Cipher cipher = null;
    private byte[] iv = null;
    private SecretKey secretKey = null;
    /* access modifiers changed from: private */
    public SilentLogWatcher silentLogWatcher;

    public static synchronized EncryptedLogger getInstance() {
        EncryptedLogger encryptedLogger;
        synchronized (EncryptedLogger.class) {
            synchronized (EncryptedLogger.class) {
                if (singleInstance == null) {
                    singleInstance = new EncryptedLogger();
                }
            }
            encryptedLogger = singleInstance;
        }
        return encryptedLogger;
    }

    private EncryptedLogger() {
        initCipher();
    }

    private void initCipher() {
        try {
            Log.d(LOG_TAG, "initCipher");
            KeyGenerator instance = KeyGenerator.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
            instance.init(256, ImsUtil.getRandom());
            this.secretKey = instance.generateKey();
            Cipher instance2 = Cipher.getInstance(ConfigUtil.TRANSFORMATION);
            this.cipher = instance2;
            instance2.init(1, this.secretKey);
            this.iv = this.cipher.getIV();
            writeSecretKeyToLogcat();
            startSilentLogWatcher();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public String getBase64EncodedSecretKey() {
        SecretKey secretKey2 = this.secretKey;
        if (secretKey2 == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(concatBytes(encryptRSA(concatBytes(secretKey2.getEncoded(), this.iv)), PublicKeyId.getBytes()));
    }

    public String getBase64EncodedSecretKeyWithDelimiter() {
        if (this.secretKey == null) {
            return null;
        }
        return KEY_PREFIX + getBase64EncodedSecretKey() + KEY_POSTFIX;
    }

    public String doLog(String str, String str2, int i) {
        long nanoTime = System.nanoTime();
        String encodeToString = Base64.getMimeEncoder(ENCRYPTED_LOGGER_LINE_MAX_PAYLOAD, "\n".getBytes()).encodeToString(encryptAES(str2));
        long nanoTime2 = System.nanoTime() - nanoTime;
        int length = encodeToString.length();
        if (length > ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD) {
            int i2 = 0;
            while (i2 < length) {
                int min = Math.min(ENCRYPTED_LOGGER_ENTRY_MAX_PAYLOAD, encodeToString.length() - i2) + i2;
                writeLog(str, makeEncryptMessagePackage(encodeToString.substring(i2, min), min < length, nanoTime2), i);
                i2 = min;
            }
            return makeEncryptMessagePackage(encodeToString, false, nanoTime2);
        }
        String makeEncryptMessagePackage = makeEncryptMessagePackage(encodeToString, false, nanoTime2);
        writeLog(str, makeEncryptMessagePackage, i);
        return makeEncryptMessagePackage;
    }

    private void writeLog(String str, String str2, int i) {
        if (i == 2) {
            Log.v(str, str2);
        } else if (i == 3) {
            Log.d(str, str2);
        } else if (i == 4) {
            Log.i(str, str2);
        } else if (i == 5) {
            Log.w(str, str2);
        } else if (i == 6) {
            Log.e(str, str2);
        }
    }

    private String makeEncryptMessagePackage(String str, boolean z, long j) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("╔═ " + j);
        String str2 = "\n";
        stringBuffer.append(str2);
        stringBuffer.append(str);
        if (z) {
            str2 = "";
        }
        stringBuffer.append(str2);
        stringBuffer.append(z ? LOG_MIDFIX : LOG_POSTFIX);
        return stringBuffer.toString();
    }

    private byte[] encryptAES(String str) {
        try {
            return this.cipher.doFinal(str.getBytes("UTF-8"));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return new byte[]{0};
        }
    }

    private byte[] encryptRSA(byte[] bArr) {
        try {
            PublicKey transformPublicKey = transformPublicKey(B64PublicKey);
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            instance.init(1, transformPublicKey);
            return instance.doFinal(bArr);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return new byte[]{0};
        }
    }

    private PublicKey transformPublicKey(String str) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(str.getBytes())));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return null;
        }
    }

    private byte[] concatBytes(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[(bArr.length + bArr2.length)];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }

    /* access modifiers changed from: private */
    public void writeSecretKeyToLogcat() {
        Log.d(LOG_TAG, getBase64EncodedSecretKeyWithDelimiter());
    }

    public String _debug_GetSecretKeyInfo() {
        return "  " + Base64.getEncoder().encodeToString(this.secretKey.getEncoded()) + "\n  " + Base64.getEncoder().encodeToString(this.iv) + "\n";
    }

    public void startSilentLogWatcher() {
        new Thread() {
            public void run() {
                int i = 10;
                while (true) {
                    int i2 = i - 1;
                    if (i > 0) {
                        try {
                            if (Files.exists(Paths.get("/sdcard", new String[0]), new LinkOption[0])) {
                                EncryptedLogger encryptedLogger = EncryptedLogger.this;
                                encryptedLogger.silentLogWatcher = new SilentLogWatcher(encryptedLogger);
                                EncryptedLogger.this.silentLogWatcher.startWatch();
                                i = 0;
                            } else {
                                Log.d(EncryptedLogger.LOG_TAG, "/sdcard is not mounted yet");
                                Thread.sleep(RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
                                i = i2;
                            }
                        } catch (Exception e) {
                            Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }.start();
    }

    private class SilentLogWatcher {
        int[] EVENT = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4095};
        String[] NAME = {"ACCESS", "MODIFY", "ATTRIB", "CLOSE_WRITE", "CLOSE_NOWRITE", "OPEN", "MOVED_FROM", "MOVED_TO", "CREATE", HttpController.METHOD_DELETE, "DELETE_SELF", "MOVE_SELF", "ALL_EVENTS"};
        /* access modifiers changed from: private */
        public final Path SILENT_LOG_HOME;
        FileObserver[] fileObservers;
        final /* synthetic */ EncryptedLogger this$0;

        public SilentLogWatcher(EncryptedLogger encryptedLogger) {
            final EncryptedLogger encryptedLogger2 = encryptedLogger;
            this.this$0 = encryptedLogger2;
            Path path = Paths.get("/sdcard/log/ap_silentlog", new String[0]);
            this.SILENT_LOG_HOME = path;
            this.fileObservers = new FileObserver[path.getNameCount()];
            final int i = 0;
            while (i < this.SILENT_LOG_HOME.getNameCount() - 1) {
                int i2 = i + 1;
                this.fileObservers[i] = new FileObserver(this.SILENT_LOG_HOME.getRoot().resolve(this.SILENT_LOG_HOME.subpath(0, i2)).toFile()) {
                    public void onEvent(int i, String str) {
                        String path = SilentLogWatcher.this.SILENT_LOG_HOME.getName(i + 1).toString();
                        if (str != null && path.equals(str)) {
                            if ((i & 256) == 256) {
                                SilentLogWatcher.this.fileObservers[i + 1].startWatching();
                            } else if ((i & 512) == 512) {
                                SilentLogWatcher.this.fileObservers[i + 1].stopWatching();
                            }
                        }
                    }

                    public void startWatching() {
                        super.startWatching();
                        if (Files.exists(SilentLogWatcher.this.SILENT_LOG_HOME.getRoot().resolve(SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, i + 2)), new LinkOption[0])) {
                            SilentLogWatcher.this.fileObservers[i + 1].startWatching();
                        }
                    }

                    public void stopWatching() {
                        super.stopWatching();
                        int i = i;
                        while (true) {
                            i++;
                            FileObserver[] fileObserverArr = SilentLogWatcher.this.fileObservers;
                            if (i < fileObserverArr.length) {
                                fileObserverArr[i].stopWatching();
                            } else {
                                return;
                            }
                        }
                    }
                };
                i = i2;
            }
            this.fileObservers[i] = new SilentLogObserver(this.SILENT_LOG_HOME);
        }

        public void startWatch() {
            this.fileObservers[0].startWatching();
        }

        private class SilentLogObserver extends FileObserver {
            public static final String CHILD_PATH_REGEX = "20\\d{6}_\\d{6}";
            /* access modifiers changed from: private */
            public long lastWriteTime;
            /* access modifiers changed from: private */
            public Path mPath;
            /* access modifiers changed from: private */
            public Timer timer = new Timer();

            public SilentLogObserver(Path path) {
                super(path.toFile());
                this.mPath = path;
            }

            public void startWatching() {
                super.startWatching();
                String r0 = EncryptedLogger.LOG_TAG;
                Log.d(r0, "startWatching : " + SilentLogWatcher.this.SILENT_LOG_HOME.getRoot() + SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, SilentLogWatcher.this.SILENT_LOG_HOME.getNameCount()));
                try {
                    this.timer.schedule(new TimerTask() {
                        public void run() {
                            try {
                                Optional<Path> reduce = Files.list(SilentLogObserver.this.mPath).reduce(new EncryptedLogger$SilentLogWatcher$SilentLogObserver$1$$ExternalSyntheticLambda0());
                                if (reduce.isPresent()) {
                                    Path path = reduce.get();
                                    if (Files.getLastModifiedTime(path, new LinkOption[0]).toMillis() + 10000 >= System.currentTimeMillis() && path.getFileName().toString().matches(SilentLogObserver.CHILD_PATH_REGEX)) {
                                        SilentLogObserver.this.timer.schedule(new KeyTimerTask(path), 0);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                            }
                        }

                        /* access modifiers changed from: private */
                        public static /* synthetic */ Path lambda$run$0(Path path, Path path2) {
                            try {
                                return Files.getLastModifiedTime(path, new LinkOption[0]).compareTo(Files.getLastModifiedTime(path2, new LinkOption[0])) > 0 ? path : path2;
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                    }, 1000);
                } catch (Exception e) {
                    Log.e(EncryptedLogger.LOG_TAG, e.getMessage(), e);
                }
            }

            public void stopWatching() {
                super.stopWatching();
                String r0 = EncryptedLogger.LOG_TAG;
                Log.d(r0, "stopWatching : " + SilentLogWatcher.this.SILENT_LOG_HOME.getRoot() + SilentLogWatcher.this.SILENT_LOG_HOME.subpath(0, SilentLogWatcher.this.SILENT_LOG_HOME.getNameCount()));
            }

            public void onEvent(int i, String str) {
                if ((i & 256) == 256 && str != null) {
                    try {
                        if (str.matches(CHILD_PATH_REGEX)) {
                            this.timer.schedule(new KeyTimerTask(this.mPath.resolve(str)), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                        }
                    } catch (Exception e) {
                        Log.e(EncryptedLogger.LOG_TAG, e.getMessage());
                    }
                }
            }

            private class KeyTimerTask extends TimerTask {
                private Path mPath;

                public KeyTimerTask(Path path) {
                    this.mPath = path;
                }

                public void run() {
                    try {
                        if (Files.list(this.mPath).anyMatch(new EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$$ExternalSyntheticLambda0()) && SilentLogObserver.this.lastWriteTime + 10000 < System.currentTimeMillis()) {
                            SilentLogObserver.this.lastWriteTime = System.currentTimeMillis();
                            SilentLogWatcher.this.this$0.writeSecretKeyToLogcat();
                        }
                    } catch (Exception e) {
                        Log.e(EncryptedLogger.LOG_TAG, e.getMessage());
                    }
                }
            }
        }
    }
}

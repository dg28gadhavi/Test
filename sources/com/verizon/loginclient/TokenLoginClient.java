package com.verizon.loginclient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

public class TokenLoginClient {
    private ILoginClientReceiver mActiveEventReceiver;
    private boolean mAlwaysReturnSubscriptionId = true;
    private final ILoginClientReceiver mAsyncEventReceiver;
    private boolean mBypassDeviceFeatureCheck = false;
    private boolean mBypassEnginePackageCheck = false;
    private final Context mContext;
    private final ILoginClientReceiver mDirectEventReceiver;
    private boolean mInteractiveQueryMode = false;
    /* access modifiers changed from: private */
    public final Object mLockObj = new Object();
    /* access modifiers changed from: private */
    public final TokenMsgHandler mMsgHandler;
    private boolean mObserveOnNullResult = true;
    private ContentObserver mObserver;
    private final InternalCallbackReceiver mSyncEventReceiver;
    private Integer mTargetSubscriptionId;
    private long mTimeoutMs = SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF;
    private boolean mTokenTypeAuth = false;

    public interface ILoginClientReceiver {
        void onErrorResult(ResultCode resultCode, Throwable th);

        void onTokenResult(TokenQueryData tokenQueryData);
    }

    public enum ResultCode {
        success,
        failure,
        waitingOnObserver,
        timeout,
        deviceNotCapable,
        engineNotInstalled,
        rogueEngineInstalled,
        securityException
    }

    public TokenLoginClient(Context context, ILoginClientReceiver iLoginClientReceiver, Looper looper, Integer num) {
        AsyncEventReceiver asyncEventReceiver;
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.mTargetSubscriptionId = num;
            this.mSyncEventReceiver = new InternalCallbackReceiver();
            this.mDirectEventReceiver = iLoginClientReceiver;
            if (iLoginClientReceiver == null) {
                asyncEventReceiver = null;
            } else {
                asyncEventReceiver = new AsyncEventReceiver(iLoginClientReceiver);
            }
            this.mAsyncEventReceiver = asyncEventReceiver;
            this.mMsgHandler = new TokenMsgHandler(looper == null ? context.getMainLooper() : looper, this);
            return;
        }
        throw new IllegalArgumentException("context cannot be null");
    }

    public void queryTokenAsync() {
        ILoginClientReceiver iLoginClientReceiver = this.mAsyncEventReceiver;
        if (iLoginClientReceiver != null) {
            this.mActiveEventReceiver = iLoginClientReceiver;
            new Thread(new Runnable() {
                public void run() {
                    TokenQueryResult access$200 = TokenLoginClient.this.doTokenQuery();
                    if (access$200.getResultCode() != ResultCode.waitingOnObserver) {
                        TokenLoginClient.this.callbackWithResult(access$200);
                    }
                }
            }).start();
            return;
        }
        throw new IllegalStateException("cannot perform async query with null callback receiver (constructor)");
    }

    public void invalidateToken() throws SecurityException {
        doTokenDelete(false);
    }

    public void cancelQuery() {
        unregisterContentObserver();
        stopTimeoutWait();
        this.mActiveEventReceiver = null;
        synchronized (this.mLockObj) {
            this.mLockObj.notifyAll();
        }
    }

    public void setTargetSubscriptionId(Integer num) {
        this.mTargetSubscriptionId = num;
    }

    public void setTimeout(long j) {
        this.mTimeoutMs = j;
    }

    private void doTokenDelete(boolean z) throws SecurityException {
        Uri loginClientUri = getLoginClientUri();
        SelectParameters buildDeleteParams = buildDeleteParams(z);
        if (loginClientUri != null) {
            try {
                this.mContext.getContentResolver().delete(loginClientUri, buildDeleteParams.getSelectString(), buildDeleteParams.getSelectParams());
            } catch (IllegalArgumentException | IllegalStateException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    public TokenQueryResult doTokenQuery() {
        if (!isDeviceCapable()) {
            return new TokenQueryResult(ResultCode.deviceNotCapable, (TokenQueryData) null, (Throwable) null);
        }
        Uri loginClientUri = getLoginClientUri();
        if (loginClientUri == null) {
            return new TokenQueryResult(ResultCode.engineNotInstalled, (TokenQueryData) null, (Throwable) null);
        }
        return queryContentProvider(loginClientUri, buildQueryParams(), this.mObserveOnNullResult);
    }

    /* access modifiers changed from: private */
    public TokenQueryResult queryContentProvider(Uri uri, SelectParameters selectParameters, boolean z) {
        try {
            Cursor query = this.mContext.getContentResolver().query(uri, (String[]) null, selectParameters.getSelectString(), selectParameters.getSelectParams(), (String) null);
            if (query == null) {
                return new TokenQueryResult(ResultCode.engineNotInstalled, (TokenQueryData) null, (Throwable) null);
            }
            if (!query.moveToFirst()) {
                query.close();
                return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            }
            String string = query.getString(query.getColumnIndex("token"));
            int columnIndex = query.getColumnIndex("subscriptionId");
            int i = columnIndex > 0 ? query.getInt(columnIndex) : -1;
            query.close();
            if (string != null) {
                return new TokenQueryResult(ResultCode.success, new TokenQueryData(Base64.encodeToString(string.getBytes(), 2), i), (Throwable) null);
            } else if (!z || this.mTimeoutMs <= 0) {
                return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            } else {
                registerContentObserver(uri);
                startTimeoutWait();
                return new TokenQueryResult(ResultCode.waitingOnObserver, (TokenQueryData) null, (Throwable) null);
            }
        } catch (SecurityException e) {
            return new TokenQueryResult(ResultCode.securityException, (TokenQueryData) null, e);
        } catch (IllegalArgumentException | IllegalStateException e2) {
            return new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, e2);
        }
    }

    /* access modifiers changed from: private */
    public SelectParameters buildQueryParams() {
        SelectParameters.Builder subscriptionId = new SelectParameters.Builder().setSubscriptionId(this.mTargetSubscriptionId);
        if (this.mAlwaysReturnSubscriptionId) {
            subscriptionId.setAlwaysSendSubscriberId(Boolean.TRUE);
        }
        return subscriptionId.build();
    }

    private SelectParameters buildDeleteParams(boolean z) {
        SelectParameters.Builder builder = new SelectParameters.Builder();
        if (z) {
            builder.setDeleteAllTokens(Boolean.TRUE);
        } else {
            builder.setSubscriptionId(this.mTargetSubscriptionId);
        }
        return builder.build();
    }

    private synchronized void registerContentObserver(Uri uri) {
        unregisterContentObserver();
        this.mObserver = new TokenContentObserver(this.mMsgHandler, uri);
        this.mContext.getContentResolver().registerContentObserver(uri, false, this.mObserver);
    }

    /* access modifiers changed from: private */
    public synchronized void unregisterContentObserver() {
        if (this.mObserver != null) {
            try {
                this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            } catch (IllegalStateException unused) {
            }
            this.mObserver = null;
        }
    }

    private boolean isDeviceCapable() {
        if (this.mBypassDeviceFeatureCheck) {
            return true;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        for (String hasSystemFeature : SSOContentProviderConstants.LTE_FEATURES) {
            if (packageManager.hasSystemFeature(hasSystemFeature)) {
                return true;
            }
        }
        return false;
    }

    private Uri getLoginClientUri() {
        PackageManager packageManager = this.mContext.getPackageManager();
        for (String str : SSOContentProviderConstants.AUTHORITIES) {
            ProviderInfo resolveContentProvider = packageManager.resolveContentProvider(str, 0);
            if (resolveContentProvider != null) {
                for (String equals : SSOContentProviderConstants.OFFICIAL_TOKEN_PACKAGES) {
                    if (equals.equals(resolveContentProvider.packageName) && isAuthorizedSignature(resolveContentProvider.packageName)) {
                        return buildQueryUri(str);
                    }
                }
                if (this.mBypassEnginePackageCheck) {
                    return buildQueryUri(str);
                }
            }
        }
        return null;
    }

    private Uri buildQueryUri(String str) {
        if (str == null) {
            return null;
        }
        return Uri.parse(String.format(Locale.ENGLISH, "content://%s/%s%s", new Object[]{str, this.mTokenTypeAuth ? "authtoken" : "token", this.mInteractiveQueryMode ? "" : "/silent"}));
    }

    private void startTimeoutWait() {
        this.mMsgHandler.sendMessageDelayed(this.mMsgHandler.obtainMessage(2), this.mTimeoutMs);
    }

    /* access modifiers changed from: private */
    public void stopTimeoutWait() {
        this.mMsgHandler.removeMessages(2);
    }

    /* access modifiers changed from: private */
    public void callbackWithResult(TokenQueryResult tokenQueryResult) {
        ILoginClientReceiver iLoginClientReceiver = this.mActiveEventReceiver;
        if (iLoginClientReceiver != null) {
            if (tokenQueryResult == null) {
                tokenQueryResult = new TokenQueryResult(ResultCode.failure, (TokenQueryData) null, (Throwable) null);
            }
            if (tokenQueryResult.getResultCode() == ResultCode.success) {
                iLoginClientReceiver.onTokenResult(tokenQueryResult.getTokenData());
            } else {
                iLoginClientReceiver.onErrorResult(tokenQueryResult.getResultCode(), tokenQueryResult.getException());
            }
        }
    }

    private static class TokenMsgHandler extends Handler {
        private final WeakReference<TokenLoginClient> mParent;

        private TokenMsgHandler(Looper looper, TokenLoginClient tokenLoginClient) {
            super(looper);
            this.mParent = new WeakReference<>(tokenLoginClient);
        }

        public void handleMessage(Message message) {
            TokenLoginClient tokenLoginClient = (TokenLoginClient) this.mParent.get();
            if (tokenLoginClient != null) {
                if (message.what == 1) {
                    tokenLoginClient.stopTimeoutWait();
                    tokenLoginClient.callbackWithResult(tokenLoginClient.queryContentProvider((Uri) message.obj, tokenLoginClient.buildQueryParams(), false));
                }
                if (message.what == 2) {
                    tokenLoginClient.unregisterContentObserver();
                    tokenLoginClient.callbackWithResult(new TokenQueryResult(ResultCode.timeout, (TokenQueryData) null, (Throwable) null));
                }
            }
        }
    }

    class TokenContentObserver extends ContentObserver {
        private final Handler mHandler;
        private final Uri mQueryUri;

        TokenContentObserver(Handler handler, Uri uri) {
            super(handler);
            this.mHandler = handler;
            this.mQueryUri = uri;
        }

        public void onChange(boolean z) {
            TokenLoginClient.this.unregisterContentObserver();
            Handler handler = this.mHandler;
            if (handler != null) {
                Message obtainMessage = handler.obtainMessage(1);
                obtainMessage.obj = this.mQueryUri;
                this.mHandler.sendMessage(obtainMessage);
            }
        }
    }

    @SuppressLint({"PackageManagerGetSignatures"})
    private boolean isAuthorizedSignature(String str) {
        boolean z = this.mBypassEnginePackageCheck;
        if (z) {
            return true;
        }
        if (str == null) {
            return z;
        }
        try {
            for (Signature certFingerprint : this.mContext.getPackageManager().getPackageInfo(str, 64).signatures) {
                String certFingerprint2 = getCertFingerprint(certFingerprint);
                for (String equals : SSOContentProviderConstants.OFFICIAL_SIGNING_CERT_SHA1) {
                    if (equals.equals(certFingerprint2)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return this.mBypassEnginePackageCheck;
    }

    private String getCertFingerprint(Signature signature) {
        ByteArrayInputStream byteArrayInputStream;
        if (signature == null) {
            return null;
        }
        try {
            byteArrayInputStream = new ByteArrayInputStream(signature.toByteArray());
            byte[] digest = MessageDigest.getInstance("SHA1").digest(((X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream)).getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                if (sb.length() > 0) {
                    sb.append(":");
                }
                sb.append(String.format(Locale.ENGLISH, "%02X", new Object[]{Byte.valueOf(b)}));
            }
            String sb2 = sb.toString();
            byteArrayInputStream.close();
            return sb2;
        } catch (IOException | NoSuchAlgorithmException | CertificateException unused) {
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static class TokenQueryData {
        public final int subscriptionId;
        public final String token;

        public TokenQueryData(String str, int i) {
            this.subscriptionId = i;
            this.token = str;
        }
    }

    public static class TokenQueryResult {
        private final Throwable mException;
        private final ResultCode mResultCode;
        private final TokenQueryData mTokenData;

        private TokenQueryResult(ResultCode resultCode, TokenQueryData tokenQueryData, Throwable th) {
            this.mResultCode = resultCode;
            this.mTokenData = tokenQueryData;
            this.mException = th;
        }

        public ResultCode getResultCode() {
            return this.mResultCode;
        }

        public TokenQueryData getTokenData() {
            return this.mTokenData;
        }

        public Throwable getException() {
            return this.mException;
        }
    }

    private static class SelectParameters {
        private final String[] mSelectParams;
        private final String mSelectString;

        private SelectParameters(String str, String[] strArr) {
            this.mSelectString = str;
            this.mSelectParams = strArr;
        }

        public String getSelectString() {
            return this.mSelectString;
        }

        public String[] getSelectParams() {
            return this.mSelectParams;
        }

        static class Builder {
            private Boolean mAlwaysSendSubscriberId = null;
            private Boolean mDeleteAllTokens = null;
            private Integer mTargetSubscriptionId = null;

            Builder() {
            }

            public SelectParameters build() {
                int i;
                int paramCount = getParamCount();
                if (paramCount == 0) {
                    return new SelectParameters((String) null, (String[]) null);
                }
                String[] strArr = new String[paramCount];
                String str = "";
                Integer num = this.mTargetSubscriptionId;
                if (num != null) {
                    i = 1;
                    str = addParam(str, strArr, "subscriptionId", Integer.toString(num.intValue()), 0);
                } else {
                    i = 0;
                }
                Boolean bool = this.mAlwaysSendSubscriberId;
                if (bool != null) {
                    str = addParam(str, strArr, "alwaysReturnSubscriptionId", bool.toString(), i);
                    i++;
                }
                Boolean bool2 = this.mDeleteAllTokens;
                if (bool2 != null) {
                    str = addParam(str, strArr, "deleteAll", bool2.toString(), i);
                }
                return new SelectParameters(str, strArr);
            }

            public Builder setAlwaysSendSubscriberId(Boolean bool) {
                this.mAlwaysSendSubscriberId = bool;
                return this;
            }

            public Builder setSubscriptionId(Integer num) {
                this.mTargetSubscriptionId = num;
                return this;
            }

            public Builder setDeleteAllTokens(Boolean bool) {
                this.mDeleteAllTokens = bool;
                return this;
            }

            private int getParamCount() {
                int i = this.mAlwaysSendSubscriberId != null ? 1 : 0;
                if (this.mDeleteAllTokens != null) {
                    i++;
                }
                return this.mTargetSubscriptionId != null ? i + 1 : i;
            }

            private String addParam(String str, String[] strArr, String str2, String str3, int i) {
                if (str.length() > 0) {
                    str = str + " AND ";
                }
                String str4 = str + str2 + " = ?";
                strArr[i] = str3;
                return str4;
            }
        }
    }

    private class InternalCallbackReceiver implements ILoginClientReceiver {
        private TokenQueryResult result;

        private InternalCallbackReceiver() {
            this.result = null;
        }

        public void onTokenResult(TokenQueryData tokenQueryData) {
            handleEvent(ResultCode.success, tokenQueryData, (Throwable) null);
        }

        public void onErrorResult(ResultCode resultCode, Throwable th) {
            handleEvent(resultCode, (TokenQueryData) null, th);
        }

        private void handleEvent(ResultCode resultCode, TokenQueryData tokenQueryData, Throwable th) {
            synchronized (TokenLoginClient.this.mLockObj) {
                this.result = new TokenQueryResult(resultCode, tokenQueryData, th);
                TokenLoginClient.this.mLockObj.notifyAll();
            }
        }
    }

    private class AsyncEventReceiver implements ILoginClientReceiver {
        /* access modifiers changed from: private */
        public final ILoginClientReceiver mClientReceiver;

        AsyncEventReceiver(ILoginClientReceiver iLoginClientReceiver) {
            if (iLoginClientReceiver != null) {
                this.mClientReceiver = iLoginClientReceiver;
                return;
            }
            throw new IllegalArgumentException("client receiver cannot be null");
        }

        public void onTokenResult(final TokenQueryData tokenQueryData) {
            TokenLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onTokenResult(tokenQueryData);
                }
            });
        }

        public void onErrorResult(final ResultCode resultCode, final Throwable th) {
            TokenLoginClient.this.mMsgHandler.post(new Runnable() {
                public void run() {
                    AsyncEventReceiver.this.mClientReceiver.onErrorResult(resultCode, th);
                }
            });
        }
    }
}

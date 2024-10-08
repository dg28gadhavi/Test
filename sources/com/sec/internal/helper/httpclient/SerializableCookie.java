package com.sec.internal.helper.httpclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import okhttp3.Cookie;

public class SerializableCookie implements Serializable {
    public static final String TAG = SerializableCookie.class.getSimpleName();
    private static final long serialVersionUID = 1;
    private transient Cookie cookie;

    /* JADX WARNING: Removed duplicated region for block: B:18:0x005a A[SYNTHETIC, Splitter:B:18:0x005a] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x007c A[SYNTHETIC, Splitter:B:26:0x007c] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String encodeCookie(okhttp3.Cookie r6) {
        /*
            r5 = this;
            java.lang.String r0 = "error2: "
            r5.cookie = r6
            java.io.ByteArrayOutputStream r6 = new java.io.ByteArrayOutputStream
            r6.<init>()
            r1 = 0
            java.io.ObjectOutputStream r2 = new java.io.ObjectOutputStream     // Catch:{ IOException -> 0x003c, all -> 0x003a }
            r2.<init>(r6)     // Catch:{ IOException -> 0x003c, all -> 0x003a }
            r2.writeObject(r5)     // Catch:{ IOException -> 0x0038 }
            r2.close()     // Catch:{ IOException -> 0x0016 }
            goto L_0x002f
        L_0x0016:
            r1 = move-exception
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            java.lang.String r0 = r1.toString()
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            com.sec.internal.log.IMSLog.e(r2, r0)
        L_0x002f:
            byte[] r6 = r6.toByteArray()
            java.lang.String r5 = r5.byteArrayToHexString(r6)
            return r5
        L_0x0038:
            r5 = move-exception
            goto L_0x003e
        L_0x003a:
            r5 = move-exception
            goto L_0x007a
        L_0x003c:
            r5 = move-exception
            r2 = r1
        L_0x003e:
            java.lang.String r6 = TAG     // Catch:{ all -> 0x0078 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0078 }
            r3.<init>()     // Catch:{ all -> 0x0078 }
            java.lang.String r4 = "error1: "
            r3.append(r4)     // Catch:{ all -> 0x0078 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0078 }
            r3.append(r5)     // Catch:{ all -> 0x0078 }
            java.lang.String r5 = r3.toString()     // Catch:{ all -> 0x0078 }
            com.sec.internal.log.IMSLog.e(r6, r5)     // Catch:{ all -> 0x0078 }
            if (r2 == 0) goto L_0x0077
            r2.close()     // Catch:{ IOException -> 0x005e }
            goto L_0x0077
        L_0x005e:
            r5 = move-exception
            java.lang.String r6 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            java.lang.String r5 = r5.toString()
            r2.append(r5)
            java.lang.String r5 = r2.toString()
            com.sec.internal.log.IMSLog.e(r6, r5)
        L_0x0077:
            return r1
        L_0x0078:
            r5 = move-exception
            r1 = r2
        L_0x007a:
            if (r1 == 0) goto L_0x0099
            r1.close()     // Catch:{ IOException -> 0x0080 }
            goto L_0x0099
        L_0x0080:
            r6 = move-exception
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            java.lang.String r6 = r6.toString()
            r2.append(r6)
            java.lang.String r6 = r2.toString()
            com.sec.internal.log.IMSLog.e(r1, r6)
        L_0x0099:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.SerializableCookie.encodeCookie(okhttp3.Cookie):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059 A[SYNTHETIC, Splitter:B:20:0x0059] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x006a A[SYNTHETIC, Splitter:B:27:0x006a] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public okhttp3.Cookie decodeCookie(java.lang.String r6) {
        /*
            r5 = this;
            java.lang.String r0 = "decodeCookie error2: "
            byte[] r5 = r5.hexStringToByteArray(r6)
            java.io.ByteArrayInputStream r6 = new java.io.ByteArrayInputStream
            r6.<init>(r5)
            r5 = 0
            java.io.ObjectInputStream r1 = new java.io.ObjectInputStream     // Catch:{ IOException | ClassNotFoundException -> 0x003f, all -> 0x003b }
            r1.<init>(r6)     // Catch:{ IOException | ClassNotFoundException -> 0x003f, all -> 0x003b }
            java.lang.Object r6 = r1.readObject()     // Catch:{ IOException | ClassNotFoundException -> 0x0039 }
            if (r6 == 0) goto L_0x001f
            java.lang.Object r6 = r1.readObject()     // Catch:{ IOException | ClassNotFoundException -> 0x0039 }
            com.sec.internal.helper.httpclient.SerializableCookie r6 = (com.sec.internal.helper.httpclient.SerializableCookie) r6     // Catch:{ IOException | ClassNotFoundException -> 0x0039 }
            okhttp3.Cookie r5 = r6.cookie     // Catch:{ IOException | ClassNotFoundException -> 0x0039 }
        L_0x001f:
            r1.close()     // Catch:{ IOException -> 0x0023 }
            goto L_0x0066
        L_0x0023:
            r6 = move-exception
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
        L_0x002b:
            r2.append(r0)
            r2.append(r6)
            java.lang.String r6 = r2.toString()
            com.sec.internal.log.IMSLog.e(r1, r6)
            goto L_0x0066
        L_0x0039:
            r6 = move-exception
            goto L_0x0041
        L_0x003b:
            r6 = move-exception
            r1 = r5
            r5 = r6
            goto L_0x0068
        L_0x003f:
            r6 = move-exception
            r1 = r5
        L_0x0041:
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0067 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0067 }
            r3.<init>()     // Catch:{ all -> 0x0067 }
            java.lang.String r4 = "decodeCookie error1:"
            r3.append(r4)     // Catch:{ all -> 0x0067 }
            r3.append(r6)     // Catch:{ all -> 0x0067 }
            java.lang.String r6 = r3.toString()     // Catch:{ all -> 0x0067 }
            com.sec.internal.log.IMSLog.e(r2, r6)     // Catch:{ all -> 0x0067 }
            if (r1 == 0) goto L_0x0066
            r1.close()     // Catch:{ IOException -> 0x005d }
            goto L_0x0066
        L_0x005d:
            r6 = move-exception
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            goto L_0x002b
        L_0x0066:
            return r5
        L_0x0067:
            r5 = move-exception
        L_0x0068:
            if (r1 == 0) goto L_0x0083
            r1.close()     // Catch:{ IOException -> 0x006e }
            goto L_0x0083
        L_0x006e:
            r6 = move-exception
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r0)
            r2.append(r6)
            java.lang.String r6 = r2.toString()
            com.sec.internal.log.IMSLog.e(r1, r6)
        L_0x0083:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.SerializableCookie.decodeCookie(java.lang.String):okhttp3.Cookie");
    }

    /* access modifiers changed from: protected */
    public String byteArrayToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            byte b2 = b & 255;
            if (b2 < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b2));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /* access modifiers changed from: protected */
    public byte[] hexStringToByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(this.cookie.name());
        objectOutputStream.writeObject(this.cookie.value());
        objectOutputStream.writeObject(Long.valueOf(this.cookie.persistent() ? this.cookie.expiresAt() : -1));
        objectOutputStream.writeObject(this.cookie.domain());
        objectOutputStream.writeObject(this.cookie.path());
        objectOutputStream.writeObject(Boolean.valueOf(this.cookie.secure()));
        objectOutputStream.writeObject(Boolean.valueOf(this.cookie.httpOnly()));
        objectOutputStream.writeObject(Boolean.valueOf(this.cookie.hostOnly()));
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Cookie.Builder builder = new Cookie.Builder();
        builder.name((String) objectInputStream.readObject());
        builder.value((String) objectInputStream.readObject());
        long longValue = ((Long) objectInputStream.readObject()).longValue();
        if (longValue != -1) {
            builder.expiresAt(longValue);
        }
        String str = (String) objectInputStream.readObject();
        builder.domain(str);
        builder.path((String) objectInputStream.readObject());
        if (((Boolean) objectInputStream.readObject()).booleanValue()) {
            builder.secure();
        }
        if (((Boolean) objectInputStream.readObject()).booleanValue()) {
            builder.httpOnly();
        }
        if (((Boolean) objectInputStream.readObject()).booleanValue()) {
            builder.hostOnlyDomain(str);
        }
        this.cookie = builder.build();
    }
}

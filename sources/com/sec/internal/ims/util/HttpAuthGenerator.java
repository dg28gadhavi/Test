package com.sec.internal.ims.util;

import android.net.Uri;
import android.telephony.IBootstrapAuthenticationCallback;
import android.telephony.gba.GbaAuthRequest;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.header.AuthorizationHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.GbaException;
import com.sec.internal.ims.gba.GbaUtility;
import com.sec.internal.ims.gba.GbaValue;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpAuthGenerator {
    private static final String AKAV1_MD5 = "AKAv1-MD5";
    private static final String AKAV2_MD5 = "AKAv2-MD5";
    private static final String LOG_TAG = "HttpAuthGenerator";

    public static String generate(String str, String str2, String str3, String str4, String str5) {
        String str6 = LOG_TAG;
        IMSLog.s(str6, "generateAuthHeader: challenge= " + str + " uri=" + str2 + " method=" + str3);
        StringBuilder sb = new StringBuilder();
        sb.append("generateAuthHeader: user=");
        sb.append(str4);
        sb.append(" password=");
        sb.append(str5);
        IMSLog.s(str6, sb.toString());
        String[] split = str.split(" ");
        if (split.length < 2) {
            throw new IllegalArgumentException("challenge is not WWW-Authenticate");
        } else if (WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME.equalsIgnoreCase(split[0])) {
            return generateDigestAuthHeader(str, str2, str3, str4, str5);
        } else {
            if (WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME.equalsIgnoreCase(split[0])) {
                return generateBasicAuthHeader(str4, str5);
            }
            return null;
        }
    }

    public static String getEAPAkaChallengeResponse(int i, String str) {
        String str2 = null;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("eap-relay-packet")) {
                String string = jSONObject.getString("eap-relay-packet");
                ISimManager simManager = SimManagerFactory.getSimManager();
                if (simManager == null) {
                    return null;
                }
                String bytesToHexString = StrUtil.bytesToHexString(Base64.decode(string.getBytes(StandardCharsets.UTF_8), 2));
                String generateChallengeResponse = AKAEapAuthHelper.generateChallengeResponse(bytesToHexString, simManager.getIsimAuthentication(AKAEapAuthHelper.getNonce(bytesToHexString)), AKAEapAuthHelper.composeRootNai(i));
                if (!TextUtils.isEmpty(generateChallengeResponse)) {
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("eap-relay-packet", generateChallengeResponse);
                    str2 = jSONObject2.toString();
                }
                String str3 = LOG_TAG;
                IMSLog.s(str3, "handleEapAkaChallenge akaResp: " + str2);
            }
        } catch (IllegalArgumentException | JSONException e) {
            String str4 = LOG_TAG;
            IMSLog.e(str4, "getEAPAkaChallengeResponse error: " + e.getMessage());
        }
        return str2;
    }

    public static String getAuthorizationHeader(int i, String str, String str2, String str3) {
        return getAuthorizationHeader(i, str, str2, str3, (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ac A[SYNTHETIC, Splitter:B:31:0x00ac] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getAuthorizationHeader(int r8, java.lang.String r9, java.lang.String r10, java.lang.String r11, java.lang.String r12) {
        /*
            java.lang.String r0 = "/"
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = getRcsStrategy(r8)
            com.sec.internal.ims.servicemodules.im.ImConfig r2 = getImConfig(r8)
            com.sec.internal.helper.parser.WwwAuthHeaderParser r3 = new com.sec.internal.helper.parser.WwwAuthHeaderParser
            r3.<init>()
            if (r1 == 0) goto L_0x001b
            java.lang.String r4 = "ft_with_gba"
            boolean r1 = r1.boolSetting(r4)
            if (r1 == 0) goto L_0x001b
            r1 = 1
            goto L_0x001c
        L_0x001b:
            r1 = 0
        L_0x001c:
            boolean r4 = android.text.TextUtils.isEmpty(r9)
            if (r4 == 0) goto L_0x0032
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "getAuthorizationHeader: requestUrl is empty. get url from imConfig"
            com.sec.internal.log.IMSLog.i(r4, r5)
            android.net.Uri r4 = r2.getFtHttpCsUri()
            java.lang.String r4 = r4.toString()
            goto L_0x0033
        L_0x0032:
            r4 = r9
        L_0x0033:
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "url = "
            r6.append(r7)
            r6.append(r4)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.s(r5, r6)
            java.net.URI r5 = new java.net.URI     // Catch:{ URISyntaxException -> 0x0074 }
            r5.<init>(r4)     // Catch:{ URISyntaxException -> 0x0074 }
            java.lang.String r4 = r5.getPath()     // Catch:{ URISyntaxException -> 0x0074 }
            java.lang.String r6 = r5.getQuery()     // Catch:{ URISyntaxException -> 0x0072 }
            if (r6 == 0) goto L_0x0079
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ URISyntaxException -> 0x0072 }
            r6.<init>()     // Catch:{ URISyntaxException -> 0x0072 }
            r6.append(r4)     // Catch:{ URISyntaxException -> 0x0072 }
            java.lang.String r7 = "?"
            r6.append(r7)     // Catch:{ URISyntaxException -> 0x0072 }
            java.lang.String r5 = r5.getQuery()     // Catch:{ URISyntaxException -> 0x0072 }
            r6.append(r5)     // Catch:{ URISyntaxException -> 0x0072 }
            java.lang.String r4 = r6.toString()     // Catch:{ URISyntaxException -> 0x0072 }
            goto L_0x0079
        L_0x0072:
            r5 = move-exception
            goto L_0x0076
        L_0x0074:
            r5 = move-exception
            r4 = r0
        L_0x0076:
            r5.printStackTrace()
        L_0x0079:
            boolean r5 = android.text.TextUtils.isEmpty(r4)
            if (r5 == 0) goto L_0x0080
            goto L_0x0081
        L_0x0080:
            r0 = r4
        L_0x0081:
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "path = "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.s(r4, r5)
            java.lang.String r5 = ""
            if (r1 == 0) goto L_0x00ac
            java.lang.String r0 = "FT with GBA is on"
            com.sec.internal.log.IMSLog.s(r4, r0)
            java.lang.String r5 = getGbaResponse(r8, r9, r10, r11, r12)     // Catch:{ UnsupportedEncodingException -> 0x00a6 }
            goto L_0x013f
        L_0x00a6:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x013f
        L_0x00ac:
            com.sec.internal.helper.header.WwwAuthenticateHeader r9 = r3.parseHeaderValue(r10)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r12 = r2.getFtHttpCsPwd()     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r1 = r9.getAlgorithm()     // Catch:{ IllegalArgumentException -> 0x0121 }
            if (r1 == 0) goto L_0x0103
            java.lang.String r3 = r1.toLowerCase()     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r6 = "aka"
            boolean r3 = r3.startsWith(r6)     // Catch:{ IllegalArgumentException -> 0x0121 }
            if (r3 == 0) goto L_0x0103
            java.lang.String r9 = r9.getNonce()     // Catch:{ IllegalArgumentException -> 0x0121 }
            com.sec.internal.ims.util.AkaAuth$AkaAuthenticationResponse r8 = com.sec.internal.ims.util.AkaAuth.getAkaResponse(r8, r9)     // Catch:{ IllegalArgumentException -> 0x0121 }
            if (r8 == 0) goto L_0x0103
            java.lang.String r9 = "AKAv1-MD5"
            boolean r9 = r1.equalsIgnoreCase(r9)     // Catch:{ IllegalArgumentException -> 0x0121 }
            if (r9 == 0) goto L_0x00dd
            java.lang.String r12 = r8.getRes()     // Catch:{ IllegalArgumentException -> 0x0121 }
            goto L_0x0103
        L_0x00dd:
            java.lang.String r9 = "AKAv2-MD5"
            boolean r9 = r1.equalsIgnoreCase(r9)     // Catch:{ IllegalArgumentException -> 0x0121 }
            if (r9 == 0) goto L_0x0103
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0121 }
            r9.<init>()     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r12 = r8.getRes()     // Catch:{ IllegalArgumentException -> 0x0121 }
            r9.append(r12)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r12 = r8.getAuthKey()     // Catch:{ IllegalArgumentException -> 0x0121 }
            r9.append(r12)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r8 = r8.getEncrKey()     // Catch:{ IllegalArgumentException -> 0x0121 }
            r9.append(r8)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r12 = r9.toString()     // Catch:{ IllegalArgumentException -> 0x0121 }
        L_0x0103:
            java.lang.String r8 = r2.getFtHttpCsUser()     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r5 = generate(r10, r0, r11, r8, r12)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x0121 }
            r8.<init>()     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r9 = "response: "
            r8.append(r9)     // Catch:{ IllegalArgumentException -> 0x0121 }
            r8.append(r5)     // Catch:{ IllegalArgumentException -> 0x0121 }
            java.lang.String r8 = r8.toString()     // Catch:{ IllegalArgumentException -> 0x0121 }
            com.sec.internal.log.IMSLog.s(r4, r8)     // Catch:{ IllegalArgumentException -> 0x0121 }
            goto L_0x013f
        L_0x0121:
            r8 = move-exception
            java.lang.String r9 = LOG_TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "getAuthorizationHeader: unable to parse wwwAuthHeader: "
            r10.append(r11)
            java.lang.String r11 = r8.getMessage()
            r10.append(r11)
            java.lang.String r10 = r10.toString()
            com.sec.internal.log.IMSLog.e(r9, r10)
            r8.printStackTrace()
        L_0x013f:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.HttpAuthGenerator.getAuthorizationHeader(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String):java.lang.String");
    }

    private static IMnoStrategy getRcsStrategy(int i) {
        return RcsPolicyManager.getRcsStrategy(i);
    }

    private static ImConfig getImConfig(int i) {
        return ImConfig.getInstance(i);
    }

    private static String generateDigestAuthHeader(String str, String str2, String str3, String str4, String str5) {
        try {
            WwwAuthenticateHeader parseHeaderValue = new WwwAuthHeaderParser().parseHeaderValue(str);
            return AuthorizationHeader.getAuthorizationHeader(str4, str5, parseHeaderValue.getRealm(), str3, str2, parseHeaderValue);
        } catch (IllegalArgumentException e) {
            String str6 = LOG_TAG;
            IMSLog.e(str6, "generateDigestAuthHeader: unable to parse wwwAuthHeader : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String generateBasicAuthHeader(String str, String str2) {
        return "Basic " + Base64.encodeToString((str + ":" + str2).getBytes(StandardCharsets.UTF_8), 2);
    }

    public static synchronized String getGbaResponse(int i, String str, String str2, String str3, String str4) throws UnsupportedEncodingException {
        synchronized (HttpAuthGenerator.class) {
            IGbaServiceModule gbaServiceModule = ImsRegistry.getServiceModuleManager().getGbaServiceModule();
            Mno simMno = SimUtil.getSimMno(i);
            GbaAuthRequest gbaAuthRequest = new GbaAuthRequest(SimUtil.getSubId(i), 0, Uri.parse(str), GbaUtility.convertCipherSuite(str4, simMno.isOneOf(Mno.TMOUS, Mno.DISH)), false, (IBootstrapAuthenticationCallback) null);
            String[] strArr = {""};
            CountDownLatch countDownLatch = new CountDownLatch(1);
            GbaValue gbaValue = gbaServiceModule.getGbaValue(i, GbaUtility.getNafUrl(str));
            if (gbaValue != null) {
                String encodeToString = Base64.encodeToString(gbaValue.getValue(), 2);
                String generate = generate(str2, GbaUtility.getNafPath(str), str3, gbaValue.getBtid(), encodeToString);
                return generate;
            }
            String str5 = str2;
            String str6 = str3;
            final String[] strArr2 = strArr;
            final String str7 = str2;
            final String str8 = str;
            final String str9 = str3;
            final CountDownLatch countDownLatch2 = countDownLatch;
            gbaServiceModule.getBtidAndGbaKey(gbaAuthRequest, new IGbaCallback() {
                public void onComplete(int i, String str, String str2, boolean z, HttpResponseParams httpResponseParams) {
                    if (!(str == null || str2 == null)) {
                        strArr2[0] = HttpAuthGenerator.generate(str7, GbaUtility.getNafPath(str8), str9, str, str2);
                    }
                    countDownLatch2.countDown();
                }

                public void onFail(int i, GbaException gbaException) {
                    countDownLatch2.countDown();
                }
            });
            try {
                countDownLatch.await(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String str10 = strArr[0];
            return str10;
        }
    }
}

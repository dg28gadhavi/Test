package com.sec.internal.ims.config.util;

import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Locale;

public class AKAEapAuthHelper {
    private static final String AT_AUTN_ATTR = "02";
    private static final String AT_AUTS_HEADER = "0404";
    private static final String AT_MAC_HEADER = "0B050000";
    private static final int AT_MAC_LENGTH = 20;
    private static final String AT_MAC_WITH_ZEROED_VALUE = "00000000000000000000000000000000";
    private static final int AT_PARAM_HEADER_LENGTH = 8;
    private static final int AT_PARAM_LENGTH = 32;
    private static final String AT_RAND_ATTR = "01";
    private static final int AT_RES = 3;
    private static final int AT_RES_LENGTH_INDEX = 1;
    private static final int AT_RES_RESLENGTH_INDEX = 3;
    private static final String EAP_FRAME_HEADER_CHALLENGE = "0201002817010000";
    private static final int EAP_FRAME_HEADER_LENGTH = 16;
    private static final String EAP_FRAME_HEADER_SYNC_FAILURE = "0201002817040000";
    private static final int EAP_FRAME_IDENTIFIER_INDEX = 1;
    private static final int EAP_FRAME_LEN_INDEX = 3;
    public static final String EAP_JSON_TYPE = "application/vnd.gsma.eap-relay.v1.0+json";
    public static final String EAP_RELAY_PACKET = "eap-relay-packet";
    private static final String LOG_TAG = "AKAEapAuthHelper";
    private static final String NONCE_SEPARATOR = "10";
    public static final String PARAM_EAP_ID = "EAP_ID";

    public static String decodeChallenge(String str) {
        return StrUtil.bytesToHexString(Base64.decode(str.getBytes(), 2)).toUpperCase(Locale.US);
    }

    public static String generateChallengeResponse(String str, String str2, String str3) {
        String str4;
        String str5 = LOG_TAG;
        IMSLog.d(str5, "generateChallengeResponse: _org " + str);
        try {
            String[] atParams = getAtParams(str);
            IMSLog.d(str5, "generateChallengeResponse: _all " + (NONCE_SEPARATOR + atParams[0] + NONCE_SEPARATOR + atParams[1]));
            IMSLog.d(str5, "generateChallengeResponse: rand " + atParams[0]);
            IMSLog.d(str5, "generateChallengeResponse: autn " + atParams[1]);
        } catch (IllegalArgumentException e) {
            IMSLog.e(LOG_TAG, e.getMessage() + " : " + str);
        }
        try {
            str4 = buildFinalEapFrame(str2, str3, StrUtil.hexStringToBytes(str)[1]);
        } catch (IndexOutOfBoundsException | NullPointerException e2) {
            IMSLog.e(LOG_TAG, e2.getMessage() + " : " + str);
            str4 = null;
        }
        if (str2 != null && str4 != null) {
            return Base64.encodeToString(StrUtil.hexStringToBytes(str4), 2);
        }
        IMSLog.e(LOG_TAG, "generateChallengeResponse: ISIM/USIM Auth failed");
        return null;
    }

    private static String[] getAtParams(String str) {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalArgumentException("the akaChallenge argument cannot be null");
        } else if (str.length() > 16) {
            String str2 = "";
            String str3 = str2;
            for (String substring = str.substring(16); substring.length() >= 40; substring = substring.substring(40)) {
                if (substring.startsWith(AT_RAND_ATTR)) {
                    str2 = substring.substring(8, 40);
                } else if (substring.startsWith(AT_AUTN_ATTR)) {
                    str3 = substring.substring(8, 40);
                }
                if (!TextUtils.isEmpty(str2) && !TextUtils.isEmpty(str3)) {
                    break;
                }
            }
            if (!TextUtils.isEmpty(str2) && !TextUtils.isEmpty(str3)) {
                return new String[]{str2, str3};
            }
            String str4 = TextUtils.isEmpty(str3) ? !TextUtils.isEmpty(str2) ? "autn" : "rand, autn" : "rand";
            throw new IllegalArgumentException("it is not possible to obtain " + str4 + " from the akaChallenge argument");
        } else {
            throw new IllegalArgumentException("length of the akaChallenge argument is not enough");
        }
    }

    public static String getNonce(String str) {
        if (TextUtils.isEmpty(str)) {
            IMSLog.e(LOG_TAG, "akaChallenge is null. Cannot authenticate.");
            return null;
        }
        try {
            String[] atParams = getAtParams(str);
            return NONCE_SEPARATOR + atParams[0] + NONCE_SEPARATOR + atParams[1];
        } catch (IllegalArgumentException e) {
            String str2 = LOG_TAG;
            IMSLog.e(str2, e.getMessage() + " : " + str);
            return null;
        }
    }

    private static String getResFrameHeader(byte[] bArr, byte b) {
        byte[] hexStringToBytes = StrUtil.hexStringToBytes(EAP_FRAME_HEADER_CHALLENGE);
        if (hexStringToBytes == null) {
            return null;
        }
        int length = hexStringToBytes.length;
        int i = 20;
        if (bArr != null) {
            i = 20 + bArr.length;
        }
        hexStringToBytes[3] = (byte) (length + i);
        hexStringToBytes[1] = b;
        return StrUtil.bytesToHexString(hexStringToBytes);
    }

    private static String getAutSFrameHeader(byte[] bArr, byte b) {
        byte[] hexStringToBytes = StrUtil.hexStringToBytes(EAP_FRAME_HEADER_SYNC_FAILURE);
        if (hexStringToBytes == null) {
            return null;
        }
        hexStringToBytes[3] = (byte) (hexStringToBytes.length + (bArr != null ? bArr.length : 0));
        hexStringToBytes[1] = b;
        return StrUtil.bytesToHexString(hexStringToBytes);
    }

    private static String buildFinalEapFrame(String str, String str2, byte b) {
        if (str == null) {
            IMSLog.e(LOG_TAG, "buildFinalEapFrame: cannot build final frame");
            return null;
        }
        String buildAtResFrame = buildAtResFrame(str);
        if (buildAtResFrame == null) {
            IMSLog.e(LOG_TAG, "buildFinalEapFrame: cannot build final frame, atResFrame is" + buildAtResFrame);
            return null;
        } else if (StrUtil.hexStringToBytes(buildAtResFrame) == null || StrUtil.hexStringToBytes(buildAtResFrame)[0] != 3) {
            String autSFrameHeader = getAutSFrameHeader(StrUtil.hexStringToBytes(buildAtResFrame), b);
            IMSLog.d(LOG_TAG, "buildFinalEapFrame calling for ISIM/USIM: EAP finalFrame " + autSFrameHeader + buildAtResFrame);
            return autSFrameHeader + buildAtResFrame;
        } else {
            String buildK_AutnForAtMac = buildK_AutnForAtMac(str, str2);
            String str3 = LOG_TAG;
            IMSLog.d(str3, "buildFinalEapFrame: K_AUT " + buildK_AutnForAtMac);
            if (buildK_AutnForAtMac == null) {
                IMSLog.e(str3, "buildFinalEapFrame: K_AUT is null. Can not calculate final EAP frame");
                return null;
            }
            String resFrameHeader = getResFrameHeader(StrUtil.hexStringToBytes(buildAtResFrame), b);
            String str4 = resFrameHeader + buildAtResFrame + "0B05000000000000000000000000000000000000";
            IMSLog.d(str3, "buildFinalEapFrame: resultWithZeroedMac " + str4);
            String str5 = "";
            try {
                str5 = HmacSha1Signature.calculateRFC2104HMAC(StrUtil.hexStringToBytes(str4), StrUtil.hexStringToBytes(buildK_AutnForAtMac));
                IMSLog.d(str3, "buildFinalEapFrame calling for ISIM/USIM: AT_MAC " + str5);
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                e.printStackTrace();
            }
            String str6 = resFrameHeader + buildAtResFrame + AT_MAC_HEADER + str5;
            IMSLog.d(LOG_TAG, "buildFinalEapFrame calling for ISIM/USIM: EAP finalFrame " + str6);
            return str6;
        }
    }

    private static String generateAtResHeader(int i) {
        byte[] bArr = {3, 0, 0, 0};
        bArr[1] = (byte) ((4 + i) / 4);
        bArr[3] = (byte) (i * 8);
        return StrUtil.bytesToHexString(bArr);
    }

    private static String buildAtResFrame(String str) {
        AkaResponse buildAkaResponse = TelephonySupport.buildAkaResponse(str);
        if (buildAkaResponse == null) {
            IMSLog.e(LOG_TAG, "buildAtResFrame: failed ISimAuthentication");
            return null;
        }
        byte[] res = buildAkaResponse.getRes();
        if (res != null) {
            String str2 = generateAtResHeader(res.length) + StrUtil.bytesToHexString(res);
            IMSLog.d(LOG_TAG, "buildAtResFrame: AT_RES Frame" + str2);
            return str2;
        }
        return AT_AUTS_HEADER + StrUtil.bytesToHexString(buildAkaResponse.getAuts());
    }

    private static String buildK_AutnForAtMac(String str, String str2) {
        byte[] buildMainKey = TelephonySupport.buildMainKey(str2, str);
        if (buildMainKey == null) {
            IMSLog.d(LOG_TAG, "buildK_AutnForAtMac: key null, vail");
            return null;
        }
        SHA1 sha1 = new SHA1();
        byte[] bArr = new byte[20];
        sha1.update(buildMainKey);
        sha1.digest(bArr);
        String str3 = LOG_TAG;
        IMSLog.d(str3, "Main Key:" + StrUtil.bytesToHexString(bArr));
        byte[] bArr2 = new byte[MNO.UMOBILE];
        Fips186_2.fips186_2_prf2(bArr, bArr2);
        IMSLog.d(str3, "PRF OUTPUT with main key:" + StrUtil.bytesToHexString(bArr2));
        return StrUtil.bytesToHexString(bArr2).substring(32, 64);
    }

    public static String composeRootNai(int i) {
        String str;
        String str2;
        StringBuilder sb = new StringBuilder();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            return "";
        }
        String imsi = simManagerFromSimSlot.getImsi();
        String simOperator = simManagerFromSimSlot.getSimOperator();
        if (TextUtils.isEmpty(simOperator)) {
            IMSLog.d(LOG_TAG, "composeRootNai, operator empty");
            return "";
        }
        if (simOperator.length() == 5) {
            str2 = simOperator.substring(0, 3);
            str = "0" + simOperator.substring(3, 5);
        } else if (simOperator.length() == 6) {
            str2 = simOperator.substring(0, 3);
            str = simOperator.substring(3, 6);
        } else {
            IMSLog.d(LOG_TAG, "composeRootNai, wrong operator");
            return "";
        }
        if (imsi != null) {
            sb.append(imsi);
            sb.append("@nai.epc.mnc");
            sb.append(str);
            sb.append(".mcc");
            sb.append(str2);
            sb.append(UtUtils.DOMAIN_NAME);
        }
        return sb.toString();
    }
}

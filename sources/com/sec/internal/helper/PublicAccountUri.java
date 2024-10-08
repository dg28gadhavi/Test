package com.sec.internal.helper;

import com.sec.ims.util.ImsUri;

public class PublicAccountUri {
    private static String mCountryCode;
    private static String publicAccountDomain;

    public static void setPublicAccountDomain(String str) {
        publicAccountDomain = str;
    }

    public static void setCountryCode(String str) {
        mCountryCode = str;
    }

    public static ImsUri convertToPublicAccountUri(String str) {
        if (str == null) {
            return null;
        }
        String str2 = publicAccountDomain;
        String[] split = str.split(":");
        if (split.length <= 1) {
            return null;
        }
        String[] split2 = split[1].split("@");
        if (split2.length > 1) {
            if (split2[0].startsWith("+86")) {
                return ImsUri.parse(split[0] + ":" + split2[0].substring(3) + "@" + str2);
            }
            return ImsUri.parse(split[0] + ":" + split2[0] + "@" + str2);
        } else if (split[1].startsWith("+86")) {
            return ImsUri.parse("sip:" + split[1].substring(3) + "@" + str2);
        } else {
            return ImsUri.parse("sip:" + split[1] + "@" + str2);
        }
    }

    public static boolean isPublicAccountUri(ImsUri imsUri) {
        String str;
        if (imsUri.toString().startsWith("sip:+8612520")) {
            return true;
        }
        if (!imsUri.toString().startsWith("sip:12520") || (str = mCountryCode) == null || !"cn".equalsIgnoreCase(str)) {
            return false;
        }
        return true;
    }
}

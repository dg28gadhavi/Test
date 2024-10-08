package com.sec.internal.ims.servicemodules.gls;

public class GlsGeoSmsComposer {
    public String compose(GlsData glsData, int i) throws NullPointerException {
        if (glsData != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("geo:");
            sb.append(glsData.getLocation().getLatitude());
            sb.append(",");
            sb.append(glsData.getLocation().getLongitude());
            sb.append(";");
            sb.append("crs=gcj02;");
            sb.append("u=");
            sb.append(glsData.getLocation().getAccuracy());
            sb.append(";");
            sb.append("rcs-l=");
            sb.append(glsData.getLabel());
            if (i > 0) {
                return subStr(sb.toString(), i);
            }
            return sb.toString();
        }
        throw new NullPointerException("GlsData is null");
    }

    private static String subStr(String str, int i) {
        if (str == null || str.length() == 0 || i <= 0) {
            return "";
        }
        if (str.getBytes().length <= i) {
            return str;
        }
        int length = str.length();
        StringBuilder sb = new StringBuilder(i);
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            String valueOf = String.valueOf(str.charAt(i3));
            i2 += valueOf.getBytes().length;
            if (i2 > i) {
                break;
            }
            sb.append(valueOf);
        }
        return sb.toString();
    }
}

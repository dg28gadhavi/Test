package com.sec.internal.ims.entitlement.util;

import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.data.DeviceConfiguration;
import com.sec.internal.log.IMSLog;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

public final class DeviceConfigParser {
    private static final String LOG_TAG = "DeviceConfigParser";
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        /* JADX WARNING: Code restructure failed: missing block: B:4:?, code lost:
            r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser();
            r2.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0013, code lost:
            return r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
            throw new java.lang.RuntimeException(r2);
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0005 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public org.xmlpull.v1.XmlPullParser createParser() {
            /*
                r2 = this;
                org.xmlpull.v1.XmlPullParser r2 = android.util.Xml.newPullParser()     // Catch:{ Exception -> 0x0005 }
                return r2
            L_0x0005:
                org.xmlpull.v1.XmlPullParserFactory r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance()     // Catch:{ Exception -> 0x0014 }
                org.xmlpull.v1.XmlPullParser r2 = r2.newPullParser()     // Catch:{ Exception -> 0x0014 }
                java.lang.String r0 = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
                r1 = 1
                r2.setFeature(r0, r1)     // Catch:{ Exception -> 0x0014 }
                return r2
            L_0x0014:
                r2 = move-exception
                java.lang.RuntimeException r0 = new java.lang.RuntimeException
                r0.<init>(r2)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.util.DeviceConfigParser.AnonymousClass1.createParser():org.xmlpull.v1.XmlPullParser");
        }
    };

    private DeviceConfigParser() {
    }

    static GsonXml createGsonXml(boolean z) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(z).setSameNameLists(true).create();
    }

    public static DeviceConfiguration parseDeviceConfig(String str) {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "deviceConfigXml: " + str);
        if (str == null) {
            return null;
        }
        try {
            return (DeviceConfiguration) createGsonXml(false).fromXml(str, DeviceConfiguration.class);
        } catch (JsonSyntaxException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "parseDeviceConfig: malformed device config xml" + e.getMessage());
            return null;
        }
    }
}

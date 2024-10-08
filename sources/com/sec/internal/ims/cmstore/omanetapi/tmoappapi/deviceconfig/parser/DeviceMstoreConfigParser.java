package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.parser;

import android.util.Log;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.DeviceConfig;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class DeviceMstoreConfigParser {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "DeviceMstoreConfigParser";
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        public XmlPullParser createParser() {
            try {
                XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
                newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
                return newPullParser;
            } catch (Exception e) {
                String r0 = DeviceMstoreConfigParser.LOG_TAG;
                Log.d(r0, "parserCreator(): " + e.getMessage());
                Log.d(DeviceMstoreConfigParser.LOG_TAG, "createParser failed");
                return null;
            }
        }
    };

    private DeviceMstoreConfigParser() {
    }

    static GsonXml createGsonXml(boolean z) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(z).setSameNameLists(true).create();
    }

    public static DeviceConfig parseDeviceConfig(String str) {
        if (str == null) {
            return null;
        }
        try {
            return (DeviceConfig) createGsonXml(false).fromXml(str, DeviceConfig.class);
        } catch (JsonSyntaxException unused) {
            Log.e(LOG_TAG, "parseDeviceConfig: malformed device config xml");
            return null;
        }
    }
}

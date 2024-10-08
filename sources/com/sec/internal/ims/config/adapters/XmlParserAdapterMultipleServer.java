package com.sec.internal.ims.config.adapters;

import android.util.Log;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class XmlParserAdapterMultipleServer extends XmlParserAdapter {
    private static final String ATTR_VALUE_ACCESS_CONTROL_APPID = "app-id";
    private static final String LOG_TAG = "XmlParserAdapterMultipleServer";

    public XmlParserAdapterMultipleServer() {
        Log.i(LOG_TAG, "Init XmlParserAdapterMultipleServer");
        this.mListTagName.put("server", (Object) null);
        this.mListTagName.put(ATTR_VALUE_ACCESS_CONTROL_APPID, (Object) null);
    }

    public void parseEndTag(XmlPullParser xmlPullParser, List<String> list, List<String> list2, Map<String, Integer> map) {
        super.parseEndTag(xmlPullParser, list, list2, map);
        map.remove(ATTR_VALUE_ACCESS_CONTROL_APPID);
    }
}

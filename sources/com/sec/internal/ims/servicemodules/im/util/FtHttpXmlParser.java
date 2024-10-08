package com.sec.internal.ims.servicemodules.im.util;

import android.util.Log;
import android.util.Xml;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FtHttpXmlParser {
    private static final String LOG_TAG = "FtHttpXmlParser";
    private static final String NS_FTHTTPEXT = "urn:gsma:params:xml:ns:rcs:rcs:up:fthttpext";
    private static final String NS_RRAM = "urn:gsma:params:xml:ns:rcs:rcs:rram";
    private static final String ns = null;
    private FtHttpFileInfo mFtHttpFileInfo;
    private FtHttpResumeInfo mFtHttpResumeInfo;

    public static FtHttpFileInfo parse(String str) throws XmlPullParserException, IOException {
        return new FtHttpXmlParser().parseFromString(str);
    }

    public static FtHttpResumeInfo parseResume(String str) throws XmlPullParserException, IOException {
        return new FtHttpXmlParser().parseResumeFromString(str);
    }

    private FtHttpFileInfo parseFromString(String str) throws XmlPullParserException, IOException {
        this.mFtHttpFileInfo = new FtHttpFileInfo();
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
        newPullParser.setInput(new StringReader(str));
        try {
            newPullParser.nextTag();
            readFile(newPullParser);
            String str2 = LOG_TAG;
            Log.i(str2, "Parsing result: " + this.mFtHttpFileInfo);
        } catch (RuntimeException e) {
            String str3 = LOG_TAG;
            IMSLog.e(str3, "Parsing failed: " + e);
            this.mFtHttpFileInfo = null;
        }
        return this.mFtHttpFileInfo;
    }

    private FtHttpResumeInfo parseResumeFromString(String str) throws XmlPullParserException, IOException {
        String str2 = LOG_TAG;
        Log.i(str2, "Parse: " + str);
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
        newPullParser.setInput(new StringReader(str));
        try {
            newPullParser.nextTag();
            this.mFtHttpResumeInfo = readFileResumeInfo(newPullParser);
            Log.i(str2, "Parsing result: " + this.mFtHttpResumeInfo);
        } catch (RuntimeException e) {
            String str3 = LOG_TAG;
            IMSLog.e(str3, "Parsing failed: " + e);
            this.mFtHttpResumeInfo = null;
        }
        return this.mFtHttpResumeInfo;
    }

    private void readFile(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, ns, "file");
        while (xmlPullParser.next() != 3 && xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 2) {
                if ("file-info".equals(xmlPullParser.getName())) {
                    String attributeValue = xmlPullParser.getAttributeValue(ns, "type");
                    if ("thumbnail".equals(attributeValue)) {
                        readThumbnailInfo(xmlPullParser);
                    } else if ("file".equals(attributeValue)) {
                        readFileInfo(xmlPullParser);
                    }
                } else {
                    skip(xmlPullParser);
                }
            }
        }
    }

    private void readThumbnailInfo(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, ns, "file-info");
        while (xmlPullParser.next() != 3 && xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if ("file-size".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailFileSize(readFileSize(xmlPullParser));
                } else if ("content-type".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailContentType(readContentType(xmlPullParser));
                } else if ("data".equals(name)) {
                    this.mFtHttpFileInfo.setThumbnailData(readData(xmlPullParser));
                } else {
                    skip(xmlPullParser);
                }
            }
        }
    }

    private void readFileInfo(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        String str = ns;
        xmlPullParser.require(2, str, "file-info");
        this.mFtHttpFileInfo.setFileDisposition(xmlPullParser.getAttributeValue(str, "file-disposition"));
        while (xmlPullParser.next() != 3 && xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if ("file-size".equals(name)) {
                    this.mFtHttpFileInfo.setFileSize(readFileSize(xmlPullParser));
                } else if ("file-name".equals(name)) {
                    this.mFtHttpFileInfo.setFileName(readFileName(xmlPullParser));
                } else if ("content-type".equals(name)) {
                    this.mFtHttpFileInfo.setContentType(readContentType(xmlPullParser));
                } else if ("data".equals(name)) {
                    this.mFtHttpFileInfo.setData(readData(xmlPullParser));
                } else if ("branded-url".equals(name)) {
                    this.mFtHttpFileInfo.setBrandedUrl(readBrandedUrl(xmlPullParser));
                } else if ("playing-length".equals(name)) {
                    this.mFtHttpFileInfo.setPlayingLength(readPlayingLength(xmlPullParser));
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        xmlPullParser.require(3, ns, "file-info");
    }

    private long readFileSize(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        String str = ns;
        xmlPullParser.require(2, str, "file-size");
        long longValue = Long.valueOf(readText(xmlPullParser)).longValue();
        xmlPullParser.require(3, str, "file-size");
        return longValue;
    }

    private String readFileName(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        String str = ns;
        xmlPullParser.require(2, str, "file-name");
        String readText = readText(xmlPullParser);
        xmlPullParser.require(3, str, "file-name");
        return readText;
    }

    private String readContentType(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        String str = ns;
        xmlPullParser.require(2, str, "content-type");
        String readText = readText(xmlPullParser);
        xmlPullParser.require(3, str, "content-type");
        return readText;
    }

    private FtHttpFileInfo.Data readData(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        String str;
        String str2;
        String str3 = ns;
        xmlPullParser.require(2, str3, "data");
        if ("data".equals(xmlPullParser.getName())) {
            str2 = xmlPullParser.getAttributeValue((String) null, ImsConstants.FtDlParams.FT_DL_URL);
            str = xmlPullParser.getAttributeValue((String) null, "until");
            xmlPullParser.nextTag();
        } else {
            str2 = "";
            str = str2;
        }
        xmlPullParser.require(3, str3, "data");
        return new FtHttpFileInfo.Data(new URL(str2), str);
    }

    private String readBrandedUrl(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, NS_FTHTTPEXT, "branded-url");
        String readText = readText(xmlPullParser);
        xmlPullParser.require(3, NS_FTHTTPEXT, "branded-url");
        return readText;
    }

    private FtHttpResumeInfo readFileResumeInfo(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, ns, "file-resume-info");
        String str = "";
        long j = 0;
        long j2 = 0;
        while (xmlPullParser.next() != 3 && xmlPullParser.getEventType() != 1) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if ("file-range".equals(name)) {
                    String str2 = ns;
                    String attributeValue = xmlPullParser.getAttributeValue(str2, VcidEvent.BUNDLE_VALUE_ACTION_START);
                    if (attributeValue != null) {
                        j2 = Long.valueOf(attributeValue).longValue();
                    }
                    String attributeValue2 = xmlPullParser.getAttributeValue(str2, "end");
                    if (attributeValue2 != null) {
                        j = Long.valueOf(attributeValue2).longValue();
                    }
                    xmlPullParser.nextTag();
                } else if ("data".equals(name)) {
                    String attributeValue3 = xmlPullParser.getAttributeValue(ns, ImsConstants.FtDlParams.FT_DL_URL);
                    if (attributeValue3 != null) {
                        str = attributeValue3;
                    }
                    xmlPullParser.nextTag();
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        xmlPullParser.require(3, ns, "file-resume-info");
        return new FtHttpResumeInfo(j2, j, new URL(str));
    }

    private String readText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        if (xmlPullParser.next() != 4) {
            return "";
        }
        String text = xmlPullParser.getText();
        xmlPullParser.nextTag();
        return text;
    }

    private int readPlayingLength(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, NS_RRAM, "playing-length");
        int parseInt = Integer.parseInt(readText(xmlPullParser));
        xmlPullParser.require(3, NS_RRAM, "playing-length");
        return parseInt;
    }

    private void skip(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser.getEventType() == 2) {
            int i = 1;
            while (i != 0 && xmlPullParser.next() != 1) {
                int eventType = xmlPullParser.getEventType();
                if (eventType == 2) {
                    i++;
                } else if (eventType == 3) {
                    i--;
                }
            }
            return;
        }
        throw new IllegalStateException();
    }
}

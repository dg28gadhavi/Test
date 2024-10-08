package com.sec.internal.helper;

import android.text.TextUtils;
import java.io.IOException;
import java.util.StringTokenizer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlUtils {
    /* JADX WARNING: Code restructure failed: missing block: B:4:?, code lost:
        r0 = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser();
        r0.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0013, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        throw new java.lang.RuntimeException(r0);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0005 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static org.xmlpull.v1.XmlPullParser newPullParser() {
        /*
            org.xmlpull.v1.XmlPullParser r0 = android.util.Xml.newPullParser()     // Catch:{ Exception -> 0x0005 }
            return r0
        L_0x0005:
            org.xmlpull.v1.XmlPullParserFactory r0 = org.xmlpull.v1.XmlPullParserFactory.newInstance()     // Catch:{ Exception -> 0x0014 }
            org.xmlpull.v1.XmlPullParser r0 = r0.newPullParser()     // Catch:{ Exception -> 0x0014 }
            java.lang.String r1 = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
            r2 = 1
            r0.setFeature(r1, r2)     // Catch:{ Exception -> 0x0014 }
            return r0
        L_0x0014:
            r0 = move-exception
            java.lang.RuntimeException r1 = new java.lang.RuntimeException
            r1.<init>(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.XmlUtils.newPullParser():org.xmlpull.v1.XmlPullParser");
    }

    public static void skipCurrentTag(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if (next == 1) {
                return;
            }
            if (next == 3 && xmlPullParser.getDepth() <= depth) {
                return;
            }
        }
    }

    /*  JADX ERROR: StackOverflow in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public static final void beginDocument(org.xmlpull.v1.XmlPullParser r3, java.lang.String r4) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        L_0x0000:
            int r0 = r3.next()
            r1 = 2
            if (r0 == r1) goto L_0x000b
            r2 = 1
            if (r0 == r2) goto L_0x000b
            goto L_0x0000
        L_0x000b:
            if (r0 != r1) goto L_0x003b
            java.lang.String r0 = r3.getName()
            boolean r0 = r0.equals(r4)
            if (r0 == 0) goto L_0x0018
            return
        L_0x0018:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Unexpected start tag: found "
            r1.append(r2)
            java.lang.String r3 = r3.getName()
            r1.append(r3)
            java.lang.String r3 = ", expected "
            r1.append(r3)
            r1.append(r4)
            java.lang.String r3 = r1.toString()
            r0.<init>(r3)
            throw r0
        L_0x003b:
            org.xmlpull.v1.XmlPullParserException r3 = new org.xmlpull.v1.XmlPullParserException
            java.lang.String r4 = "No start tag found"
            r3.<init>(r4)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.XmlUtils.beginDocument(org.xmlpull.v1.XmlPullParser, java.lang.String):void");
    }

    public static boolean search(XmlPullParser xmlPullParser, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, ".");
        if (!stringTokenizer.hasMoreTokens()) {
            return false;
        }
        try {
            beginDocument(xmlPullParser, stringTokenizer.nextToken());
            xmlPullParser.nextTag();
            while (stringTokenizer.hasMoreTokens() && searchTag(xmlPullParser, stringTokenizer.nextToken())) {
                if (!stringTokenizer.hasMoreTokens()) {
                    return true;
                }
                xmlPullParser.nextTag();
            }
        } catch (XmlPullParserException unused) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean searchTag(XmlPullParser xmlPullParser, String str) {
        while (true) {
            try {
                int eventType = xmlPullParser.getEventType();
                if (eventType == 1) {
                    return false;
                }
                if (eventType != 2) {
                    xmlPullParser.nextTag();
                } else if (str.equalsIgnoreCase(xmlPullParser.getName())) {
                    return true;
                } else {
                    skipCurrentTag(xmlPullParser);
                }
            } catch (XmlPullParserException unused) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}

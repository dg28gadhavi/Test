package com.sec.internal.ims.servicemodules.volte2.util;

import android.util.Log;
import com.sec.ims.DialogEvent;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.text.ParseException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class DialogXmlParser {
    private static final int CMC_TYPE_NONE = 0;
    private static final int CMC_TYPE_PRIMARY = 1;
    private static final int CMC_TYPE_SECONDARY = 2;
    private static final int CMC_WIFI_HS_TYPE_PRIMARY = 5;
    private static final int CMC_WIFI_HS_TYPE_SECONDARY = 6;
    private static final int CMC_WIFI_P2P_TYPE_PRIMARY = 7;
    private static final int CMC_WIFI_P2P_TYPE_SECONDARY = 8;
    private static final int CMC_WIFI_TYPE_PRIMARY = 3;
    private static final int CMC_WIFI_TYPE_SECONDARY = 4;
    private static final String LOG_TAG = "DialogXmlParser";
    private static DialogXmlParser sInstance;
    private XPath mXPath;
    private XPathExpression mXPathCallId;
    private XPathExpression mXPathCallSlot;
    private XPathExpression mXPathCallType;
    private XPathExpression mXPathCode;
    private XPathExpression mXPathDialog;
    private XPathExpression mXPathDialogInfo;
    private XPathExpression mXPathDirection;
    private XPathExpression mXPathEntity;
    private XPathExpression mXPathEvent;
    private XPathExpression mXPathExclusive;
    private XPathExpression mXPathId;
    private XPathExpression mXPathLocalDisplay;
    private XPathExpression mXPathLocalDisplayName;
    private XPathExpression mXPathLocalIdentity;
    private XPathExpression mXPathLocalTag;
    private XPathExpression mXPathLocalTarget;
    private XPathExpression mXPathLocalUri;
    private XPathExpression mXPathMediaAttributes;
    private XPathExpression mXPathMediaDirection;
    private XPathExpression mXPathMediaPortZero;
    private XPathExpression mXPathMediaType;
    private XPathExpression mXPathRemoteDisplay;
    private XPathExpression mXPathRemoteDisplayName;
    private XPathExpression mXPathRemoteIdentity;
    private XPathExpression mXPathRemoteTag;
    private XPathExpression mXPathSessionDesc;
    private XPathExpression mXPathSipInstance;
    private XPathExpression mXPathSipRendering;
    private XPathExpression mXPathState;

    public static DialogXmlParser getInstance() {
        if (sInstance == null) {
            sInstance = new DialogXmlParser();
        }
        return sInstance;
    }

    private void init() {
        XPath newXPath = XPathFactory.newInstance().newXPath();
        this.mXPath = newXPath;
        newXPath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String str) {
                if ("dins".equals(str)) {
                    return "urn:ietf:params:xml:ns:dialog-info";
                }
                return "sa".equals(str) ? "urn:ietf:params:xml:ns:sa-dialog-info" : "";
            }

            public String getPrefix(String str) {
                throw new UnsupportedOperationException();
            }

            public Iterator getPrefixes(String str) {
                throw new UnsupportedOperationException();
            }
        });
        try {
            this.mXPathDialogInfo = this.mXPath.compile("/dins:dialog-info");
            this.mXPathEntity = this.mXPath.compile("@entity");
            this.mXPathDialog = this.mXPath.compile("dins:dialog");
            this.mXPathId = this.mXPath.compile("@id");
            this.mXPathCallId = this.mXPath.compile("@call-id");
            this.mXPathLocalTag = this.mXPath.compile("@local-tag");
            this.mXPathRemoteTag = this.mXPath.compile("@remote-tag");
            this.mXPathDirection = this.mXPath.compile("@direction");
            this.mXPathExclusive = this.mXPath.compile("sa:exclusive");
            this.mXPathState = this.mXPath.compile("dins:state");
            this.mXPathEvent = this.mXPath.compile("dins:state/@event");
            this.mXPathCode = this.mXPath.compile("dins:state/@code");
            this.mXPathLocalIdentity = this.mXPath.compile("dins:local/dins:identity");
            this.mXPathLocalDisplayName = this.mXPath.compile("dins:local/dins:identity/@display-name");
            this.mXPathLocalDisplay = this.mXPath.compile("dins:local/dins:identity/@display");
            this.mXPathLocalUri = this.mXPath.compile("dins:local/dins:target/@uri");
            this.mXPathLocalTarget = this.mXPath.compile("dins:local/dins:target");
            this.mXPathSessionDesc = this.mXPath.compile("dins:session-description");
            this.mXPathCallType = this.mXPath.compile("dins:calltype");
            this.mXPathCallSlot = this.mXPath.compile("dins:callslot");
            this.mXPathSipInstance = this.mXPath.compile("dins:local/dins:target/dins:param[@pname='+sip.instance']/@pval");
            this.mXPathSipRendering = this.mXPath.compile("dins:local/dins:target/dins:param[@pname='+sip.rendering']/@pval");
            this.mXPathMediaAttributes = this.mXPath.compile("dins:local/dins:mediaAttributes");
            this.mXPathMediaType = this.mXPath.compile("dins:mediaType");
            this.mXPathMediaDirection = this.mXPath.compile("dins:mediaDirection");
            this.mXPathMediaPortZero = this.mXPath.compile("dins:port0");
            this.mXPathRemoteIdentity = this.mXPath.compile("dins:remote/dins:identity");
            this.mXPathRemoteDisplayName = this.mXPath.compile("dins:remote/dins:identity/@display-name");
            this.mXPathRemoteDisplay = this.mXPath.compile("dins:remote/dins:identity/@display");
        } catch (XPathExpressionException e) {
            Log.e(LOG_TAG, "XPath compile failed!", e);
        }
    }

    private DialogXmlParser() {
        init();
    }

    private int convertDialogDirection(String str) throws ParseException {
        if ("initiator".equals(str)) {
            return 0;
        }
        if (CloudMessageProviderContract.VVMMessageColumns.RECIPIENT.equals(str)) {
            return 1;
        }
        throw new ParseException("invalid direction: " + str, 0);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int convertDialogState(int r6, java.lang.String r7, java.lang.String r8, java.lang.String r9) throws java.text.ParseException {
        /*
            r5 = this;
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "convertDialogState(): "
            r0.append(r1)
            r0.append(r7)
            java.lang.String r1 = " / "
            r0.append(r1)
            r0.append(r8)
            r0.append(r1)
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r5, r0)
            r5 = 0
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ NumberFormatException -> 0x002a }
            goto L_0x0041
        L_0x002a:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "convertDialogState(): ignoring invalid code "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r9 = r1.toString()
            android.util.Log.e(r0, r9)
            r9 = r5
        L_0x0041:
            r7.hashCode()
            int r0 = r7.hashCode()
            r1 = 3
            r2 = 2
            r3 = -1
            r4 = 1
            switch(r0) {
                case -1308815837: goto L_0x0073;
                case -864995257: goto L_0x0067;
                case -804109473: goto L_0x005c;
                case 96278371: goto L_0x0051;
                default: goto L_0x004f;
            }
        L_0x004f:
            r7 = r3
            goto L_0x007e
        L_0x0051:
            java.lang.String r0 = "early"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x005a
            goto L_0x004f
        L_0x005a:
            r7 = r1
            goto L_0x007e
        L_0x005c:
            java.lang.String r0 = "confirmed"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0065
            goto L_0x004f
        L_0x0065:
            r7 = r2
            goto L_0x007e
        L_0x0067:
            java.lang.String r0 = "trying"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0071
            goto L_0x004f
        L_0x0071:
            r7 = r4
            goto L_0x007e
        L_0x0073:
            java.lang.String r0 = "terminated"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x007d
            goto L_0x004f
        L_0x007d:
            r7 = r5
        L_0x007e:
            switch(r7) {
                case 0: goto L_0x0096;
                case 1: goto L_0x008a;
                case 2: goto L_0x0089;
                case 3: goto L_0x0082;
                default: goto L_0x0081;
            }
        L_0x0081:
            goto L_0x008e
        L_0x0082:
            if (r6 != r4) goto L_0x008e
            r6 = 180(0xb4, float:2.52E-43)
            if (r9 != r6) goto L_0x008e
            return r5
        L_0x0089:
            return r4
        L_0x008a:
            if (r6 != 0) goto L_0x008e
            r5 = 4
            return r5
        L_0x008e:
            java.lang.String r5 = LOG_TAG
            java.lang.String r6 = "convertDialogState(): ignoring"
            android.util.Log.i(r5, r6)
            return r3
        L_0x0096:
            java.lang.String r5 = "rejected"
            boolean r5 = r5.equals(r8)
            if (r5 == 0) goto L_0x00a4
            r5 = 486(0x1e6, float:6.81E-43)
            if (r9 != r5) goto L_0x00a4
            return r1
        L_0x00a4:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser.convertDialogState(int, java.lang.String, java.lang.String, java.lang.String):int");
    }

    private String getDeviceIdFromSipInstanceId(String str) throws ParseException {
        Matcher matcher = Pattern.compile("urn:gsma:imei:([0-9-]+)").matcher(str);
        if (matcher.matches()) {
            return matcher.group(1).replaceAll("[^0-9]", "");
        }
        throw new ParseException("invalid instance id: " + str, 0);
    }

    private int convertDialogCallType(String str) throws ParseException {
        return SipMsg.FEATURE_TAG_MMTEL_VIDEO.equalsIgnoreCase(str) ? 2 : 1;
    }

    private int convertDialogMediaDirection(String str) throws ParseException {
        if ("sendrecv".equalsIgnoreCase(str)) {
            return 4;
        }
        if ("recvonly".equalsIgnoreCase(str)) {
            return 3;
        }
        if ("sendonly".equalsIgnoreCase(str)) {
            return 2;
        }
        if ("inactive".equalsIgnoreCase(str)) {
            return 1;
        }
        return 0;
    }

    private int convertDialogCallState(String str) throws ParseException {
        return "no".equalsIgnoreCase(str) ? 2 : 1;
    }

    public DialogEvent parseDialogInfoXml(String str) throws XPathExpressionException {
        return parseDialogInfoXml(str, 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r38v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r37v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r36v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r34v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r32v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r31v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r30v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r32v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r33v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r36v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v23, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v16, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v24, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v26, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v27, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v29, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v32, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v30, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v37, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v40, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v21, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v36, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v33, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v45, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v18, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v38, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v22, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v25, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v46, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v28, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v47, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v19, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r26v20, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v20, resolved type: java.lang.String} */
    /* JADX WARNING: Can't wrap try/catch for region: R(3:146|147|148) */
    /* JADX WARNING: Code restructure failed: missing block: B:147:?, code lost:
        android.util.Log.e(LOG_TAG, "[CMC] ignoring invalid callSlot");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0285, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:136:0x0247 */
    /* JADX WARNING: Missing exception handler attribute for start block: B:146:0x027e */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01fe A[Catch:{ ParseException -> 0x0308 }] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0272 A[SYNTHETIC, Splitter:B:143:0x0272] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02a6  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x02ca A[SYNTHETIC, Splitter:B:167:0x02ca] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02df  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.DialogEvent parseDialogInfoXml(java.lang.String r50, int r51) throws javax.xml.xpath.XPathExpressionException {
        /*
            r49 = this;
            r1 = r49
            r2 = r51
            org.xml.sax.InputSource r0 = new org.xml.sax.InputSource
            java.io.StringReader r3 = new java.io.StringReader
            r4 = r50
            r3.<init>(r4)
            r0.<init>(r3)
            javax.xml.xpath.XPathExpression r3 = r1.mXPathDialogInfo
            javax.xml.namespace.QName r4 = javax.xml.xpath.XPathConstants.NODE
            java.lang.Object r0 = r3.evaluate(r0, r4)
            org.w3c.dom.Node r0 = (org.w3c.dom.Node) r0
            javax.xml.xpath.XPathExpression r3 = r1.mXPathEntity
            java.lang.String r3 = r3.evaluate(r0)
            com.sec.ims.util.ImsUri r3 = com.sec.ims.util.ImsUri.parse(r3)
            if (r3 == 0) goto L_0x048a
            java.lang.String r4 = r3.getMsisdn()
            if (r4 != 0) goto L_0x0030
            java.lang.String r4 = r3.getUser()
        L_0x0030:
            javax.xml.xpath.XPathExpression r3 = r1.mXPathDialog
            javax.xml.namespace.QName r5 = javax.xml.xpath.XPathConstants.NODESET
            java.lang.Object r0 = r3.evaluate(r0, r5)
            r3 = r0
            org.w3c.dom.NodeList r3 = (org.w3c.dom.NodeList) r3
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r7 = 0
        L_0x0041:
            int r0 = r3.getLength()
            java.lang.String r8 = ""
            if (r7 >= r0) goto L_0x045b
            org.w3c.dom.Node r0 = r3.item(r7)
            javax.xml.xpath.XPathExpression r9 = r1.mXPathDirection     // Catch:{ ParseException -> 0x03ea }
            java.lang.String r9 = r9.evaluate(r0)     // Catch:{ ParseException -> 0x03ea }
            int r9 = r1.convertDialogDirection(r9)     // Catch:{ ParseException -> 0x03ea }
            javax.xml.xpath.XPathExpression r10 = r1.mXPathState     // Catch:{ ParseException -> 0x03cc }
            java.lang.String r10 = r10.evaluate(r0)     // Catch:{ ParseException -> 0x03cc }
            javax.xml.xpath.XPathExpression r11 = r1.mXPathEvent     // Catch:{ ParseException -> 0x03cc }
            java.lang.String r11 = r11.evaluate(r0)     // Catch:{ ParseException -> 0x03cc }
            javax.xml.xpath.XPathExpression r12 = r1.mXPathCode     // Catch:{ ParseException -> 0x03cc }
            java.lang.String r12 = r12.evaluate(r0)     // Catch:{ ParseException -> 0x03cc }
            int r10 = r1.convertDialogState(r9, r10, r11, r12)     // Catch:{ ParseException -> 0x03cc }
            if (r10 >= 0) goto L_0x0073
            r20 = r3
            goto L_0x0451
        L_0x0073:
            javax.xml.xpath.XPathExpression r11 = r1.mXPathMediaAttributes     // Catch:{ ParseException -> 0x03b4 }
            javax.xml.namespace.QName r12 = javax.xml.xpath.XPathConstants.NODESET     // Catch:{ ParseException -> 0x03b4 }
            java.lang.Object r11 = r11.evaluate(r0, r12)     // Catch:{ ParseException -> 0x03b4 }
            org.w3c.dom.NodeList r11 = (org.w3c.dom.NodeList) r11     // Catch:{ ParseException -> 0x03b4 }
            int r12 = r11.getLength()     // Catch:{ ParseException -> 0x03b4 }
            r13 = 2
            if (r12 <= 0) goto L_0x0109
            r12 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r18 = 0
        L_0x008c:
            int r6 = r11.getLength()     // Catch:{ ParseException -> 0x00ee }
            if (r12 >= r6) goto L_0x00eb
            org.w3c.dom.Node r6 = r11.item(r12)     // Catch:{ ParseException -> 0x00ee }
            javax.xml.xpath.XPathExpression r14 = r1.mXPathMediaType     // Catch:{ ParseException -> 0x00ee }
            java.lang.String r14 = r14.evaluate(r6)     // Catch:{ ParseException -> 0x00ee }
            int r14 = r1.convertDialogCallType(r14)     // Catch:{ ParseException -> 0x00ee }
            if (r15 != 0) goto L_0x00a3
            goto L_0x00a7
        L_0x00a3:
            if (r15 == r13) goto L_0x00a8
            if (r14 != r13) goto L_0x00a8
        L_0x00a7:
            r15 = r14
        L_0x00a8:
            r20 = r3
            r3 = 1
            if (r14 == r3) goto L_0x00da
            if (r14 == r13) goto L_0x00ba
            javax.xml.xpath.XPathExpression r3 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x00e9 }
            java.lang.String r3 = r3.evaluate(r6)     // Catch:{ ParseException -> 0x00e9 }
            int r16 = r1.convertDialogMediaDirection(r3)     // Catch:{ ParseException -> 0x00e9 }
            goto L_0x00e4
        L_0x00ba:
            javax.xml.xpath.XPathExpression r3 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x00e9 }
            java.lang.String r3 = r3.evaluate(r6)     // Catch:{ ParseException -> 0x00e9 }
            int r17 = r1.convertDialogMediaDirection(r3)     // Catch:{ ParseException -> 0x00e9 }
            javax.xml.xpath.XPathExpression r3 = r1.mXPathMediaPortZero     // Catch:{ ParseException -> 0x00e9 }
            javax.xml.namespace.QName r14 = javax.xml.xpath.XPathConstants.NODESET     // Catch:{ ParseException -> 0x00e9 }
            java.lang.Object r3 = r3.evaluate(r6, r14)     // Catch:{ ParseException -> 0x00e9 }
            org.w3c.dom.NodeList r3 = (org.w3c.dom.NodeList) r3     // Catch:{ ParseException -> 0x00e9 }
            int r3 = r3.getLength()     // Catch:{ ParseException -> 0x00e9 }
            if (r3 <= 0) goto L_0x00d7
            r18 = 1
            goto L_0x00e4
        L_0x00d7:
            r18 = 0
            goto L_0x00e4
        L_0x00da:
            javax.xml.xpath.XPathExpression r3 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x00e9 }
            java.lang.String r3 = r3.evaluate(r6)     // Catch:{ ParseException -> 0x00e9 }
            int r16 = r1.convertDialogMediaDirection(r3)     // Catch:{ ParseException -> 0x00e9 }
        L_0x00e4:
            int r12 = r12 + 1
            r3 = r20
            goto L_0x008c
        L_0x00e9:
            r0 = move-exception
            goto L_0x00f1
        L_0x00eb:
            r20 = r3
            goto L_0x0112
        L_0x00ee:
            r0 = move-exception
            r20 = r3
        L_0x00f1:
            r11 = r8
            r12 = r11
            r13 = r12
            r14 = r13
            r23 = r14
            r26 = r23
            r27 = r26
            r24 = r9
            r3 = r16
            r2 = 0
            r6 = 0
            r19 = 0
            r9 = r27
        L_0x0105:
            r16 = r9
            goto L_0x0407
        L_0x0109:
            r20 = r3
            r15 = 0
            r16 = 0
            r17 = 0
            r18 = 0
        L_0x0112:
            javax.xml.xpath.XPathExpression r3 = r1.mXPathId     // Catch:{ ParseException -> 0x039a }
            java.lang.String r3 = r3.evaluate(r0)     // Catch:{ ParseException -> 0x039a }
            r6 = 8
            r11 = 4
            if (r2 == r13) goto L_0x0121
            if (r2 == r11) goto L_0x0121
            if (r2 != r6) goto L_0x014e
        L_0x0121:
            int r12 = r3.length()     // Catch:{ ParseException -> 0x0382 }
            if (r12 <= 0) goto L_0x014e
            int r12 = r3.hashCode()     // Catch:{ ParseException -> 0x0137 }
            r14 = 2147483647(0x7fffffff, float:NaN)
            r12 = r12 & r14
            r14 = 31
            int r14 = r14 + r12
            java.lang.String r3 = java.lang.Integer.toString(r14)     // Catch:{ ParseException -> 0x0137 }
            goto L_0x014e
        L_0x0137:
            r0 = move-exception
            r11 = r8
            r12 = r11
            r13 = r12
            r14 = r13
            r23 = r14
            r26 = r23
            r27 = r26
            r24 = r9
            r2 = 0
            r6 = 0
            r19 = 0
            r8 = r3
            r9 = r27
            r3 = r16
            goto L_0x0105
        L_0x014e:
            javax.xml.xpath.XPathExpression r12 = r1.mXPathCallId     // Catch:{ ParseException -> 0x0382 }
            java.lang.String r12 = r12.evaluate(r0)     // Catch:{ ParseException -> 0x0382 }
            javax.xml.xpath.XPathExpression r14 = r1.mXPathLocalTag     // Catch:{ ParseException -> 0x0379 }
            java.lang.String r14 = r14.evaluate(r0)     // Catch:{ ParseException -> 0x0379 }
            javax.xml.xpath.XPathExpression r6 = r1.mXPathRemoteTag     // Catch:{ ParseException -> 0x036e }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x036e }
            javax.xml.xpath.XPathExpression r11 = r1.mXPathLocalIdentity     // Catch:{ ParseException -> 0x0361 }
            java.lang.String r11 = r11.evaluate(r0)     // Catch:{ ParseException -> 0x0361 }
            javax.xml.xpath.XPathExpression r13 = r1.mXPathRemoteIdentity     // Catch:{ ParseException -> 0x0357 }
            java.lang.String r13 = r13.evaluate(r0)     // Catch:{ ParseException -> 0x0357 }
            r23 = r6
            javax.xml.xpath.XPathExpression r6 = r1.mXPathLocalDisplay     // Catch:{ ParseException -> 0x034e }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x034e }
            r24 = r9
            javax.xml.xpath.XPathExpression r9 = r1.mXPathSessionDesc     // Catch:{ ParseException -> 0x0344 }
            java.lang.String r9 = r9.evaluate(r0)     // Catch:{ ParseException -> 0x0344 }
            r25 = r10
            boolean r10 = android.text.TextUtils.isEmpty(r6)     // Catch:{ ParseException -> 0x033b }
            r26 = r6
            r6 = 1
            if (r10 != r6) goto L_0x0191
            javax.xml.xpath.XPathExpression r6 = r1.mXPathLocalDisplayName     // Catch:{ ParseException -> 0x018e }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x018e }
            goto L_0x0193
        L_0x018e:
            r0 = move-exception
            goto L_0x033e
        L_0x0191:
            r6 = r26
        L_0x0193:
            javax.xml.xpath.XPathExpression r10 = r1.mXPathRemoteDisplay     // Catch:{ ParseException -> 0x033b }
            java.lang.String r10 = r10.evaluate(r0)     // Catch:{ ParseException -> 0x033b }
            r26 = r6
            boolean r6 = android.text.TextUtils.isEmpty(r10)     // Catch:{ ParseException -> 0x0329 }
            r27 = r10
            r10 = 1
            if (r6 != r10) goto L_0x01ae
            javax.xml.xpath.XPathExpression r6 = r1.mXPathRemoteDisplayName     // Catch:{ ParseException -> 0x01ab }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x01ab }
            goto L_0x01b0
        L_0x01ab:
            r0 = move-exception
            goto L_0x032c
        L_0x01ae:
            r6 = r27
        L_0x01b0:
            javax.xml.xpath.XPathExpression r10 = r1.mXPathSipRendering     // Catch:{ ParseException -> 0x0325 }
            java.lang.String r10 = r10.evaluate(r0)     // Catch:{ ParseException -> 0x0325 }
            int r10 = r1.convertDialogCallState(r10)     // Catch:{ ParseException -> 0x0325 }
            r27 = r6
            javax.xml.xpath.XPathExpression r6 = r1.mXPathExclusive     // Catch:{ ParseException -> 0x031d }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x031d }
            boolean r6 = java.lang.Boolean.parseBoolean(r6)     // Catch:{ ParseException -> 0x031d }
            r28 = r6
            javax.xml.xpath.XPathExpression r6 = r1.mXPathSipInstance     // Catch:{ ParseException -> 0x0314 }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x0314 }
            boolean r29 = android.text.TextUtils.isEmpty(r6)     // Catch:{ ParseException -> 0x0314 }
            if (r29 != 0) goto L_0x01e0
            java.lang.String r6 = r1.getDeviceIdFromSipInstanceId(r6)     // Catch:{ ParseException -> 0x0314 }
        L_0x01d8:
            r8 = 2
        L_0x01d9:
            r48 = r16
            r16 = r6
            r6 = r48
            goto L_0x01ef
        L_0x01e0:
            javax.xml.xpath.XPathExpression r6 = r1.mXPathLocalTarget     // Catch:{ ParseException -> 0x0314 }
            java.lang.String r6 = r6.evaluate(r0)     // Catch:{ ParseException -> 0x0314 }
            if (r16 != 0) goto L_0x01ea
            r16 = 4
        L_0x01ea:
            if (r15 != 0) goto L_0x01d8
            r8 = 2
            r15 = 1
            goto L_0x01d9
        L_0x01ef:
            if (r2 == r8) goto L_0x01f8
            r8 = 4
            if (r2 == r8) goto L_0x01f8
            r8 = 8
            if (r2 != r8) goto L_0x02c2
        L_0x01f8:
            int r8 = r3.length()     // Catch:{ ParseException -> 0x0308 }
            if (r8 <= 0) goto L_0x02c2
            java.lang.String r8 = "*31#"
            boolean r8 = r9.startsWith(r8)     // Catch:{ ParseException -> 0x0308 }
            if (r8 != 0) goto L_0x0211
            java.lang.String r8 = "#31#"
            boolean r8 = r9.startsWith(r8)     // Catch:{ ParseException -> 0x0308 }
            if (r8 == 0) goto L_0x020f
            goto L_0x0211
        L_0x020f:
            r8 = r9
            goto L_0x021e
        L_0x0211:
            r8 = 4
            java.lang.String r9 = r9.substring(r8)     // Catch:{ ParseException -> 0x0308 }
            java.lang.String r8 = LOG_TAG     // Catch:{ ParseException -> 0x0308 }
            java.lang.String r2 = "Remove CLIR prefix"
            android.util.Log.i(r8, r2)     // Catch:{ ParseException -> 0x0308 }
            goto L_0x020f
        L_0x021e:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x02ba }
            r2.<init>()     // Catch:{ ParseException -> 0x02ba }
            java.lang.String r9 = "sip:"
            r2.append(r9)     // Catch:{ ParseException -> 0x02ba }
            r2.append(r8)     // Catch:{ ParseException -> 0x02ba }
            java.lang.String r2 = r2.toString()     // Catch:{ ParseException -> 0x02ba }
            java.lang.String r9 = LOG_TAG     // Catch:{ ParseException -> 0x02ac }
            java.lang.String r13 = "[CMC] Displayname on pulling UI : use session-description value."
            android.util.Log.i(r9, r13)     // Catch:{ ParseException -> 0x02ac }
            javax.xml.xpath.XPathExpression r9 = r1.mXPathCallType     // Catch:{ NumberFormatException -> 0x0247, ParseException -> 0x0243 }
            java.lang.String r9 = r9.evaluate(r0)     // Catch:{ NumberFormatException -> 0x0247, ParseException -> 0x0243 }
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ NumberFormatException -> 0x0247, ParseException -> 0x0243 }
            r15 = r9
            goto L_0x024e
        L_0x0243:
            r0 = move-exception
            r13 = r2
            goto L_0x02bb
        L_0x0247:
            java.lang.String r9 = LOG_TAG     // Catch:{ ParseException -> 0x02ac }
            java.lang.String r13 = "[CMC] ignoring invalid callType"
            android.util.Log.e(r9, r13)     // Catch:{ ParseException -> 0x02ac }
        L_0x024e:
            java.lang.String r9 = LOG_TAG     // Catch:{ ParseException -> 0x02ac }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x02ac }
            r13.<init>()     // Catch:{ ParseException -> 0x02ac }
            r19 = r2
            java.lang.String r2 = "[CMC] calltype: "
            r13.append(r2)     // Catch:{ ParseException -> 0x02aa }
            r13.append(r15)     // Catch:{ ParseException -> 0x02aa }
            java.lang.String r2 = r13.toString()     // Catch:{ ParseException -> 0x02aa }
            android.util.Log.i(r9, r2)     // Catch:{ ParseException -> 0x02aa }
            javax.xml.xpath.XPathExpression r2 = r1.mXPathCallSlot     // Catch:{ ParseException -> 0x02aa }
            java.lang.String r2 = r2.evaluate(r0)     // Catch:{ ParseException -> 0x02aa }
            boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ ParseException -> 0x02aa }
            if (r2 != 0) goto L_0x02a6
            javax.xml.xpath.XPathExpression r2 = r1.mXPathCallSlot     // Catch:{ NumberFormatException -> 0x027e }
            java.lang.String r0 = r2.evaluate(r0)     // Catch:{ NumberFormatException -> 0x027e }
            int r0 = java.lang.Integer.parseInt(r0)     // Catch:{ NumberFormatException -> 0x027e }
            r2 = r0
            goto L_0x0286
        L_0x027e:
            java.lang.String r0 = LOG_TAG     // Catch:{ ParseException -> 0x02aa }
            java.lang.String r2 = "[CMC] ignoring invalid callSlot"
            android.util.Log.e(r0, r2)     // Catch:{ ParseException -> 0x02aa }
            r2 = 0
        L_0x0286:
            java.lang.String r0 = LOG_TAG     // Catch:{ ParseException -> 0x02a1 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x02a1 }
            r9.<init>()     // Catch:{ ParseException -> 0x02a1 }
            java.lang.String r13 = "callSlot : "
            r9.append(r13)     // Catch:{ ParseException -> 0x02a1 }
            r9.append(r2)     // Catch:{ ParseException -> 0x02a1 }
            java.lang.String r9 = r9.toString()     // Catch:{ ParseException -> 0x02a1 }
            android.util.Log.i(r0, r9)     // Catch:{ ParseException -> 0x02a1 }
            r9 = r8
            r8 = r19
            r0 = 2
            goto L_0x02c5
        L_0x02a1:
            r0 = move-exception
            r9 = r8
            r13 = r19
            goto L_0x02b3
        L_0x02a6:
            r9 = r8
            r8 = r19
            goto L_0x02c3
        L_0x02aa:
            r0 = move-exception
            goto L_0x02af
        L_0x02ac:
            r0 = move-exception
            r19 = r2
        L_0x02af:
            r9 = r8
            r13 = r19
            r2 = 0
        L_0x02b3:
            r8 = r3
            r3 = r6
            r19 = r10
            r10 = r25
            goto L_0x02db
        L_0x02ba:
            r0 = move-exception
        L_0x02bb:
            r9 = r8
            r19 = r10
            r10 = r25
            r2 = 0
            goto L_0x02d9
        L_0x02c2:
            r8 = r13
        L_0x02c3:
            r0 = 2
            r2 = 0
        L_0x02c5:
            if (r10 == r0) goto L_0x02df
            r13 = 4
            if (r6 == r13) goto L_0x02df
            java.lang.String r13 = LOG_TAG     // Catch:{ ParseException -> 0x02d3 }
            java.lang.String r0 = "HELD call check by Audio Direction"
            android.util.Log.i(r13, r0)     // Catch:{ ParseException -> 0x02d3 }
            r13 = 2
            goto L_0x02e0
        L_0x02d3:
            r0 = move-exception
            r13 = r8
            r19 = r10
            r10 = r25
        L_0x02d9:
            r8 = r3
            r3 = r6
        L_0x02db:
            r6 = r28
            goto L_0x0407
        L_0x02df:
            r13 = r10
        L_0x02e0:
            r43 = r2
            r29 = r3
            r44 = r6
            r35 = r8
            r38 = r9
            r34 = r11
            r31 = r12
            r42 = r13
            r32 = r14
            r41 = r15
            r30 = r16
            r45 = r17
            r47 = r18
            r33 = r23
            r40 = r24
            r39 = r25
            r36 = r26
            r37 = r27
            r46 = r28
            goto L_0x0447
        L_0x0308:
            r0 = move-exception
            r8 = r3
            r3 = r6
            r19 = r10
            r10 = r25
            r6 = r28
            r2 = 0
            goto L_0x0407
        L_0x0314:
            r0 = move-exception
            r19 = r10
            r10 = r25
            r6 = r28
            r2 = 0
            goto L_0x0332
        L_0x031d:
            r0 = move-exception
            r19 = r10
            r10 = r25
            r2 = 0
            r6 = 0
            goto L_0x0332
        L_0x0325:
            r0 = move-exception
            r27 = r6
            goto L_0x032c
        L_0x0329:
            r0 = move-exception
            r27 = r10
        L_0x032c:
            r10 = r25
            r2 = 0
            r6 = 0
            r19 = 0
        L_0x0332:
            r48 = r8
            r8 = r3
            r3 = r16
            r16 = r48
            goto L_0x0407
        L_0x033b:
            r0 = move-exception
            r26 = r6
        L_0x033e:
            r27 = r8
            r10 = r25
            goto L_0x0392
        L_0x0344:
            r0 = move-exception
            r26 = r6
            r25 = r10
            r9 = r8
            r27 = r9
            goto L_0x0392
        L_0x034e:
            r0 = move-exception
            r24 = r9
            r25 = r10
            r9 = r8
            r26 = r9
            goto L_0x0390
        L_0x0357:
            r0 = move-exception
            r23 = r6
            r24 = r9
            r25 = r10
            r9 = r8
            r13 = r9
            goto L_0x036b
        L_0x0361:
            r0 = move-exception
            r23 = r6
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r13 = r11
        L_0x036b:
            r26 = r13
            goto L_0x0390
        L_0x036e:
            r0 = move-exception
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r13 = r11
            r23 = r13
            goto L_0x038e
        L_0x0379:
            r0 = move-exception
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r13 = r11
            goto L_0x038b
        L_0x0382:
            r0 = move-exception
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r12 = r11
            r13 = r12
        L_0x038b:
            r14 = r13
            r23 = r14
        L_0x038e:
            r26 = r23
        L_0x0390:
            r27 = r26
        L_0x0392:
            r2 = 0
            r6 = 0
            r19 = 0
            r8 = r3
            r3 = r16
            goto L_0x03b0
        L_0x039a:
            r0 = move-exception
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r12 = r11
            r13 = r12
            r14 = r13
            r23 = r14
            r26 = r23
            r27 = r26
            r3 = r16
            r2 = 0
            r6 = 0
            r19 = 0
        L_0x03b0:
            r16 = r27
            goto L_0x0407
        L_0x03b4:
            r0 = move-exception
            r20 = r3
            r24 = r9
            r25 = r10
            r9 = r8
            r11 = r9
            r12 = r11
            r13 = r12
            r14 = r13
            r16 = r14
            r23 = r16
            r26 = r23
            r27 = r26
            r2 = 0
            r3 = 0
            r6 = 0
            goto L_0x03e2
        L_0x03cc:
            r0 = move-exception
            r20 = r3
            r24 = r9
            r9 = r8
            r11 = r9
            r12 = r11
            r13 = r12
            r14 = r13
            r16 = r14
            r23 = r16
            r26 = r23
            r27 = r26
            r2 = 0
            r3 = 0
            r6 = 0
            r10 = 0
        L_0x03e2:
            r15 = 0
            r17 = 0
            r18 = 0
            r19 = 0
            goto L_0x0407
        L_0x03ea:
            r0 = move-exception
            r20 = r3
            r9 = r8
            r11 = r9
            r12 = r11
            r13 = r12
            r14 = r13
            r16 = r14
            r23 = r16
            r26 = r23
            r27 = r26
            r2 = 0
            r3 = 0
            r6 = 0
            r10 = 0
            r15 = 0
            r17 = 0
            r18 = 0
            r19 = 0
            r24 = 0
        L_0x0407:
            java.lang.String r1 = LOG_TAG
            r21 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r22 = r3
            java.lang.String r3 = "Parse error for dialog id "
            r2.append(r3)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2, r0)
            r46 = r6
            r29 = r8
            r38 = r9
            r39 = r10
            r34 = r11
            r31 = r12
            r35 = r13
            r32 = r14
            r41 = r15
            r30 = r16
            r45 = r17
            r47 = r18
            r42 = r19
            r43 = r21
            r44 = r22
            r33 = r23
            r40 = r24
            r36 = r26
            r37 = r27
        L_0x0447:
            com.sec.ims.Dialog r0 = new com.sec.ims.Dialog
            r28 = r0
            r28.<init>(r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47)
            r5.add(r0)
        L_0x0451:
            int r7 = r7 + 1
            r1 = r49
            r2 = r51
            r3 = r20
            goto L_0x0041
        L_0x045b:
            com.sec.ims.DialogEvent r0 = new com.sec.ims.DialogEvent
            r0.<init>(r4, r5)
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "parsed dialog xml: "
            r2.append(r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            r3.append(r8)
            java.lang.String r3 = r3.toString()
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r3)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            return r0
        L_0x048a:
            javax.xml.xpath.XPathExpressionException r0 = new javax.xml.xpath.XPathExpressionException
            java.lang.String r1 = "invalid entity"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser.parseDialogInfoXml(java.lang.String, int):com.sec.ims.DialogEvent");
    }
}

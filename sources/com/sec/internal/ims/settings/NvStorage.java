package com.sec.internal.ims.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NvStorage {
    private static final String DEFAULT_NAME = "DEFAULT";
    public static final String ID_OMADM = "omadm";
    private static final String IMS_NV_STORAGE_XML = "/efs/sec_efs/ims_nv_";
    private static final String LOG_TAG = "NvStorage";
    private static final String OMADM_PREFIX = "omadm/./3GPP_IMS/";
    protected static final String ROOT_ELEMENT = "NV_STORAGE";
    private static final String SILENT_REDIAL_PATH = "/efs/sec_efs/silent_redial";
    private Context mContext;
    protected Document mDoc = null;
    private SimpleEventLog mEventLog;
    private final Object mLock = new Object();
    private String mName;
    protected File mNvFile;
    private int mPhoneId;

    public NvStorage(Context context, String str, int i) {
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 50);
        this.mContext = context;
        this.mPhoneId = i;
        StringBuilder sb = new StringBuilder();
        sb.append(TextUtils.isEmpty(str) ? DEFAULT_NAME : str);
        sb.append("_");
        sb.append(i);
        this.mName = sb.toString();
        this.mDoc = null;
        this.mNvFile = new File(IMS_NV_STORAGE_XML + this.mName + ".xml");
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "loading new nv file: " + this.mNvFile);
        initNvStorage(false);
        initDoc();
        initElements();
        try {
            Os.chmod(this.mNvFile.getAbsolutePath(), 432);
            Os.chmod(SILENT_REDIAL_PATH, 432);
        } catch (ErrnoException e) {
            String str2 = LOG_TAG;
            IMSLog.e(str2, i, "chmod error!! : " + e);
        }
    }

    public void close() {
        synchronized (this.mLock) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mPhoneId;
            simpleEventLog.logAndAdd(i, "Close : " + this.mNvFile);
            this.mNvFile = null;
            this.mDoc = null;
        }
    }

    private synchronized void initElements() {
        initDoc();
        Document document = this.mDoc;
        if (document == null) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str, i, this.mNvFile.getName() + " open failed");
            return;
        }
        NodeList elementsByTagName = document.getElementsByTagName(ID_OMADM);
        if (elementsByTagName == null || elementsByTagName.getLength() == 0) {
            this.mEventLog.logAndAdd(this.mPhoneId, "initElements: create omadm");
            NodeList elementsByTagName2 = this.mDoc.getElementsByTagName(ROOT_ELEMENT);
            if (elementsByTagName2 != null) {
                if (elementsByTagName2.getLength() != 0) {
                    Element createElement = this.mDoc.createElement(ID_OMADM);
                    migrateFromOldFile(createElement);
                    elementsByTagName2.item(0).appendChild(createElement);
                    try {
                        Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
                        newTransformer.setOutputProperty("indent", "yes");
                        newTransformer.transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
                    } catch (TransformerException e) {
                        String str2 = LOG_TAG;
                        int i2 = this.mPhoneId;
                        IMSLog.e(str2, i2, "create() TransformerException exception" + e);
                    }
                }
            }
            IMSLog.e(LOG_TAG, this.mPhoneId, "root is empty");
            return;
        }
        return;
        return;
    }

    private void migrateFromOldFile(Element element) {
        Document document = null;
        try {
            File file = new File(IMS_NV_STORAGE_XML + this.mName.replaceFirst("_\\d", "") + ".xml");
            if (file.exists()) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "migrateFromOldFile: Copy from old file: " + file);
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        Optional.ofNullable(document).map(new NvStorage$$ExternalSyntheticLambda2()).map(new NvStorage$$ExternalSyntheticLambda3()).map(new NvStorage$$ExternalSyntheticLambda4()).ifPresent(new NvStorage$$ExternalSyntheticLambda5(this, element));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$migrateFromOldFile$2(Element element, NamedNodeMap namedNodeMap) {
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node item = namedNodeMap.item(i);
            String nodeName = item.getNodeName();
            String nodeValue = item.getNodeValue();
            String str = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "migrateFromOldFile: " + nodeName + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + IMSLog.checker(nodeValue));
            element.setAttribute(nodeName, nodeValue);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initDoc() {
        /*
            r6 = this;
            monitor-enter(r6)
            r0 = 1
            javax.xml.parsers.DocumentBuilderFactory r1 = javax.xml.parsers.DocumentBuilderFactory.newInstance()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            javax.xml.parsers.DocumentBuilder r1 = r1.newDocumentBuilder()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            org.w3c.dom.Document r2 = r6.mDoc     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            if (r2 != 0) goto L_0x001a
            java.io.File r2 = r6.mNvFile     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            org.w3c.dom.Document r1 = r1.parse(r2)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            r6.mDoc = r1     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            if (r1 != 0) goto L_0x001a
            monitor-exit(r6)
            return
        L_0x001a:
            org.w3c.dom.Document r1 = r6.mDoc     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            org.w3c.dom.Element r1 = r1.getDocumentElement()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            if (r1 != 0) goto L_0x002f
            java.lang.String r1 = LOG_TAG     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            int r2 = r6.mPhoneId     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            java.lang.String r3 = "initDoc: getDocumentElement(): null"
            com.sec.internal.log.IMSLog.e(r1, r2, r3)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            r6.initNvStorage(r0)     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            goto L_0x005b
        L_0x002f:
            org.w3c.dom.Document r1 = r6.mDoc     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            org.w3c.dom.Element r1 = r1.getDocumentElement()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            r1.normalize()     // Catch:{ IOException | ParserConfigurationException | SAXException -> 0x003b }
            goto L_0x005b
        L_0x0039:
            r0 = move-exception
            goto L_0x005d
        L_0x003b:
            r1 = move-exception
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0039 }
            int r3 = r6.mPhoneId     // Catch:{ all -> 0x0039 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0039 }
            r4.<init>()     // Catch:{ all -> 0x0039 }
            java.lang.String r5 = "initDoc: Exception occurred! "
            r4.append(r5)     // Catch:{ all -> 0x0039 }
            r4.append(r1)     // Catch:{ all -> 0x0039 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0039 }
            com.sec.internal.log.IMSLog.e(r2, r3, r4)     // Catch:{ all -> 0x0039 }
            boolean r1 = r1 instanceof org.xml.sax.SAXException     // Catch:{ all -> 0x0039 }
            if (r1 == 0) goto L_0x005b
            r6.initNvStorage(r0)     // Catch:{ all -> 0x0039 }
        L_0x005b:
            monitor-exit(r6)
            return
        L_0x005d:
            monitor-exit(r6)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.NvStorage.initDoc():void");
    }

    private synchronized void initNvStorage(boolean z) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "initNvStorage(): isForce: " + z);
        File file = this.mNvFile;
        if (z || !file.exists()) {
            try {
                Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                newDocument.appendChild(newDocument.createElement(ROOT_ELEMENT));
                Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
                newTransformer.setOutputProperty("indent", "yes");
                newTransformer.transform(new DOMSource(newDocument), new StreamResult(file));
            } catch (ParserConfigurationException | TransformerException e) {
                String str = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.e(str, i2, "initNvStorage: Exception occurred! " + e);
            }
        }
        return;
    }

    public void insert(String str, ContentValues contentValues) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "insert: " + IMSLog.checker(contentValues));
        synchronized (this.mLock) {
            save(str, contentValues);
        }
    }

    public Cursor query(String str, String[] strArr) {
        MatrixCursor matrixCursor;
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "query: " + str + "," + Arrays.toString(strArr));
        synchronized (this.mLock) {
            Map<String, Object> readFromStorage = readFromStorage(str, strArr);
            if ("VZW".equalsIgnoreCase(this.mName) && ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) && readFromStorage != null && readFromStorage.containsKey("SMS_FORMAT")) {
                readFromStorage.put("SMS_FORMAT", "3GPP");
                IMSLog.i(str2, this.mPhoneId, "VZW CDMA-less case! Return fake SMS_FORAMT(3GPP) by force");
            }
            if (readFromStorage == null || readFromStorage.size() <= 0) {
                matrixCursor = null;
            } else {
                String[] strArr2 = new String[2];
                matrixCursor = new MatrixCursor(new String[]{"PATH", "VALUE"});
                for (Map.Entry next : readFromStorage.entrySet()) {
                    strArr2[0] = "omadm/./3GPP_IMS/" + ((String) next.getKey());
                    strArr2[1] = (String) next.getValue();
                    matrixCursor.addRow(strArr2);
                }
            }
        }
        return matrixCursor;
    }

    private Map<String, Object> readFromStorage(String str, String[] strArr) {
        HashMap hashMap = new HashMap();
        initDoc();
        Document document = this.mDoc;
        HashSet hashSet = null;
        if (document == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, this.mNvFile.getName() + " open failed");
            return null;
        }
        NodeList elementsByTagName = document.getElementsByTagName(str);
        if (elementsByTagName != null) {
            Node item = elementsByTagName.item(0);
            if (item == null) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "query(" + str + "): nNode is null");
                initElements();
                return null;
            }
            NamedNodeMap attributes = item.getAttributes();
            if (strArr != null) {
                for (int i = 0; i < strArr.length; i++) {
                    strArr[i] = strArr[i].replace("omadm/./3GPP_IMS/", "");
                }
                hashSet = new HashSet(Arrays.asList(strArr));
            }
            for (int i2 = 0; i2 < attributes.getLength(); i2++) {
                Node item2 = attributes.item(i2);
                if (hashSet == null || hashSet.contains(item2.getNodeName())) {
                    hashMap.put(item2.getNodeName(), item2.getNodeValue());
                }
            }
        }
        return hashMap;
    }

    public int delete(String str) {
        int i;
        this.mEventLog.logAndAdd(this.mPhoneId, "delete: table " + str);
        synchronized (this.mLock) {
            initDoc();
            Document document = this.mDoc;
            i = 0;
            if (document == null) {
                IMSLog.e(LOG_TAG, this.mPhoneId, this.mNvFile.getName() + " open failed");
                return 0;
            }
            NodeList elementsByTagName = document.getElementsByTagName(str);
            if (elementsByTagName == null) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "delete(" + str + "): targetChild is null");
                initElements();
                return 0;
            }
            Element element = (Element) elementsByTagName.item(0);
            NamedNodeMap attributes = element.getAttributes();
            int length = attributes.getLength();
            while (length > 0) {
                length--;
                element.removeAttribute(attributes.item(length).getNodeName());
                i++;
            }
            try {
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
            } catch (TransformerException e) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "delete: Exception occurred! " + e);
            }
        }
        return i;
    }

    private void save(String str, ContentValues contentValues) {
        initDoc();
        Document document = this.mDoc;
        if (document == null) {
            String str2 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str2, i, this.mNvFile.getName() + " open failed");
            return;
        }
        Element element = (Element) document.getElementsByTagName(str).item(0);
        if (element == null) {
            String str3 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.e(str3, i2, "save(" + str + "): targetElement is null");
            initElements();
            return;
        }
        for (Map.Entry next : contentValues.valueSet()) {
            String replace = ((String) next.getKey()).replace("omadm/./3GPP_IMS/", "");
            String obj = next.getValue().toString();
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i3 = this.mPhoneId;
            simpleEventLog.logAndAdd(i3, "save: " + replace + " [" + IMSLog.checker(obj) + "]");
            element.setAttribute(replace, obj);
            if ("silent_redial".equalsIgnoreCase(replace)) {
                writeSilentRedial(obj);
            }
        }
        try {
            Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
            newTransformer.setOutputProperty("indent", "yes");
            newTransformer.transform(new DOMSource(this.mDoc), new StreamResult(this.mNvFile));
        } catch (TransformerException e) {
            String str4 = LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.e(str4, i4, "reset() TransformerException exception" + e);
        }
        for (Map.Entry<String, Object> key : contentValues.valueSet()) {
            String replace2 = ((String) key.getKey()).replace("omadm/./3GPP_IMS/", "");
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.notifyChange(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/" + replace2, this.mPhoneId), (ContentObserver) null);
        }
    }

    private synchronized void writeSilentRedial(String str) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(SILENT_REDIAL_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            printWriter = null;
        }
        if (printWriter != null) {
            printWriter.print(str);
            printWriter.close();
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, this.mPhoneId, "Dump of NvStorage:");
        int i = this.mPhoneId;
        IMSLog.dump(str, i, "NV File: " + this.mNvFile.toString());
        this.mEventLog.dump();
        Optional.ofNullable(readFromStorage(ID_OMADM, (String[]) null)).ifPresent(new NvStorage$$ExternalSyntheticLambda1(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$dump$4(Map map) {
        String str = LOG_TAG;
        IMSLog.increaseIndent(str);
        IMSLog.dump(str, this.mPhoneId, "Last value of NV OMADM:");
        IMSLog.increaseIndent(str);
        map.forEach(new NvStorage$$ExternalSyntheticLambda0(this));
        IMSLog.decreaseIndent(str);
        IMSLog.decreaseIndent(str);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$dump$3(String str, Object obj) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.dump(str2, i, str + ": " + obj);
    }
}

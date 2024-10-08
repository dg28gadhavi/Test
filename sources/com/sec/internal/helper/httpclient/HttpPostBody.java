package com.sec.internal.helper.httpclient;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class HttpPostBody {
    public static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
    public static final String CONTENT_DISPOSITION_FORM_DATA = "form-data";
    public static final String CONTENT_DISPOSITION_ICON = "icon";
    public static final String CONTENT_TRANSFER_ENCODING_BASE64 = "base64";
    public static final String CONTENT_TRANSFER_ENCODING_BINARY = "binary";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_DEFAULT = "application/octet-stream";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/";
    public static final String CONTENT_TYPE_MULTIPART_FORMDATA = "multipart/form-data";
    public static final String CONTENT_TYPE_MULTIPART_MIXED = "multipart/mixed";
    public static final String CONTENT_TYPE_MULTIPART_RELATED = "multipart/related";
    public static final String CONTENT_TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private String mBody;
    private long mBodySize;
    private String mContentDisposition;
    private String mContentId;
    private String mContentTransferEncoding;
    private String mContentType;
    private byte[] mData;
    private File mFile;
    private String mFileIcon;
    private JSONObject mJSONBody;
    private List<HttpPostBody> mMultiparts;

    public HttpPostBody(String str) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mBody = str;
        this.mBodySize = (long) getFieldSize(str);
    }

    public HttpPostBody(JSONObject jSONObject) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mBodySize = 0;
        this.mJSONBody = jSONObject;
        if (jSONObject != null) {
            this.mBody = jSONObject.toString();
        }
        this.mBodySize = (long) getFieldSize(this.mBody);
    }

    public HttpPostBody(File file) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mFile = file;
        if (file != null) {
            this.mBodySize = file.length();
        }
    }

    public HttpPostBody(byte[] bArr) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mData = bArr;
        if (bArr != null) {
            this.mBodySize = (long) bArr.length;
        }
    }

    public HttpPostBody(List<HttpPostBody> list) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mMultiparts = list;
        if (list != null) {
            for (HttpPostBody bodySize : list) {
                this.mBodySize += bodySize.getBodySize();
            }
        }
    }

    public HttpPostBody(Map<String, String> map) {
        this.mContentDisposition = null;
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        String convertPrams = convertPrams(map);
        this.mBody = convertPrams;
        this.mBodySize = (long) getFieldSize(convertPrams);
    }

    public HttpPostBody(String str, String str2, String str3) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mBody = str3;
        this.mBodySize = fieldSize2 + ((long) getFieldSize(str3));
    }

    public HttpPostBody(String str, String str2, String str3, String str4) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mBody = str3;
        long fieldSize3 = fieldSize2 + ((long) getFieldSize(str3));
        this.mBodySize = fieldSize3;
        this.mContentId = str4;
        this.mBodySize = fieldSize3 + ((long) getFieldSize(str4));
    }

    public HttpPostBody(String str, String str2, byte[] bArr) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mData = bArr;
        if (bArr != null) {
            this.mBodySize = fieldSize2 + ((long) bArr.length);
        }
    }

    public HttpPostBody(String str, String str2, byte[] bArr, String str3) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mData = bArr;
        if (bArr != null) {
            this.mBodySize = fieldSize2 + ((long) bArr.length);
        }
        this.mContentId = str3;
        this.mBodySize += (long) getFieldSize(str3);
    }

    public HttpPostBody(String str, String str2, byte[] bArr, String str3, String str4) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mData = bArr;
        if (bArr != null) {
            this.mBodySize = fieldSize2 + ((long) bArr.length);
        }
        this.mFileIcon = str3;
        long fieldSize3 = this.mBodySize + ((long) getFieldSize(str3));
        this.mBodySize = fieldSize3;
        this.mContentId = str4;
        this.mBodySize = fieldSize3 + ((long) getFieldSize(str4));
    }

    public HttpPostBody(String str, String str2, File file) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        long fieldSize2 = fieldSize + ((long) getFieldSize(str2));
        this.mBodySize = fieldSize2;
        this.mFile = file;
        if (file != null) {
            this.mBodySize = fieldSize2 + file.length();
        }
    }

    public HttpPostBody(String str, String str2, List<HttpPostBody> list) {
        this.mContentType = null;
        this.mContentTransferEncoding = null;
        this.mFileIcon = null;
        this.mContentId = null;
        this.mBody = null;
        this.mFile = null;
        this.mData = null;
        this.mMultiparts = null;
        this.mJSONBody = null;
        this.mBodySize = 0;
        this.mContentDisposition = str;
        long fieldSize = (long) getFieldSize(str);
        this.mBodySize = fieldSize;
        this.mContentType = str2;
        this.mBodySize = fieldSize + ((long) getFieldSize(str2));
        this.mMultiparts = list;
        for (HttpPostBody bodySize : list) {
            this.mBodySize += bodySize.getBodySize();
        }
    }

    public long getBodySize() {
        return this.mBodySize;
    }

    public String getContentDisposition() {
        return this.mContentDisposition;
    }

    public String getContentTransferEncoding() {
        return this.mContentTransferEncoding;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getFileIcon() {
        return this.mFileIcon;
    }

    public String getContentId() {
        return this.mContentId;
    }

    public String getBody() {
        return this.mBody;
    }

    public JSONObject getJSONBody() {
        return this.mJSONBody;
    }

    public File getFile() {
        return this.mFile;
    }

    public byte[] getData() {
        return this.mData;
    }

    public List<HttpPostBody> getMultiparts() {
        return this.mMultiparts;
    }

    public void setContentTransferEncoding(String str) {
        this.mContentTransferEncoding = str;
    }

    private String convertPrams(Map<String, String> map) {
        try {
            return convertPrams(map, Charset.defaultCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertPrams(Map<String, String> map, Charset charset) throws UnsupportedEncodingException {
        StringBuffer stringBuffer = new StringBuffer();
        boolean z = true;
        for (Map.Entry next : map.entrySet()) {
            if (z) {
                z = false;
            } else {
                stringBuffer.append("&");
            }
            stringBuffer.append(URLEncoder.encode((String) next.getKey(), charset.name()));
            stringBuffer.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            stringBuffer.append(URLEncoder.encode((String) next.getValue(), charset.name()));
        }
        return stringBuffer.toString();
    }

    public String toString() {
        try {
            return toString(0);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return "";
        }
    }

    /* JADX WARNING: type inference failed for: r7v2, types: [java.lang.String] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String toString(int r14) {
        /*
            r13 = this;
            java.lang.String r0 = r13.makeIndent(r14)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r2 = "    "
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.StringBuffer r2 = new java.lang.StringBuffer
            r2.<init>()
            java.util.List<com.sec.internal.helper.httpclient.HttpPostBody> r3 = r13.mMultiparts
            if (r3 != 0) goto L_0x0024
            java.lang.String r3 = "null"
            r2.append(r3)
            goto L_0x003e
        L_0x0024:
            java.util.Iterator r3 = r3.iterator()
        L_0x0028:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x003e
            java.lang.Object r4 = r3.next()
            com.sec.internal.helper.httpclient.HttpPostBody r4 = (com.sec.internal.helper.httpclient.HttpPostBody) r4
            int r5 = r14 + 1
            java.lang.String r4 = r4.toString(r5)
            r2.append(r4)
            goto L_0x0028
        L_0x003e:
            java.lang.String r3 = r13.mBody
            java.lang.String r4 = "]"
            java.lang.String r5 = "mMultiparts: "
            java.lang.String r6 = "mFile: "
            java.lang.String r7 = "mContentTransferEncoding: "
            java.lang.String r8 = "mContentType: "
            java.lang.String r9 = "mContentDisposition: "
            java.lang.String r10 = ")[\r\n"
            java.lang.String r11 = "HttpPostBody(depth"
            java.lang.String r12 = "\r\n"
            if (r3 == 0) goto L_0x00c7
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r12)
            r3.append(r0)
            r3.append(r11)
            r3.append(r14)
            r3.append(r10)
            r3.append(r1)
            r3.append(r9)
            java.lang.String r14 = r13.mContentDisposition
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r7)
            java.lang.String r14 = r13.mContentTransferEncoding
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r8)
            java.lang.String r14 = r13.mContentType
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            java.lang.String r14 = "mBody: "
            r3.append(r14)
            java.lang.String r14 = r13.mBody
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r6)
            java.io.File r13 = r13.mFile
            r3.append(r13)
            r3.append(r12)
            r3.append(r1)
            r3.append(r5)
            r3.append(r2)
            r3.append(r12)
            r3.append(r0)
            r3.append(r4)
            java.lang.String r13 = r3.toString()
            return r13
        L_0x00c7:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r12)
            r3.append(r0)
            r3.append(r11)
            r3.append(r14)
            r3.append(r10)
            r3.append(r1)
            r3.append(r9)
            java.lang.String r14 = r13.mContentDisposition
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r8)
            java.lang.String r14 = r13.mContentType
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            java.lang.String r14 = "mFileIcon: "
            r3.append(r14)
            java.lang.String r14 = r13.mFileIcon
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            java.lang.String r14 = "mContentId: "
            r3.append(r14)
            java.lang.String r14 = r13.mContentId
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r7)
            java.lang.String r14 = r13.mContentTransferEncoding
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            java.lang.String r14 = "mData: length is "
            r3.append(r14)
            byte[] r14 = r13.mData
            if (r14 == 0) goto L_0x0133
            int r14 = r14.length
            goto L_0x0134
        L_0x0133:
            r14 = 0
        L_0x0134:
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            java.lang.String r14 = "mData: "
            r3.append(r14)
            byte[] r14 = r13.mData
            if (r14 == 0) goto L_0x0151
            int r7 = r14.length
            r8 = 8192(0x2000, float:1.14794E-41)
            if (r7 >= r8) goto L_0x0151
            java.lang.String r7 = new java.lang.String
            r7.<init>(r14)
            r14 = r7
        L_0x0151:
            r3.append(r14)
            r3.append(r12)
            r3.append(r1)
            r3.append(r6)
            java.io.File r13 = r13.mFile
            r3.append(r13)
            r3.append(r12)
            r3.append(r1)
            r3.append(r5)
            r3.append(r2)
            r3.append(r12)
            r3.append(r0)
            r3.append(r4)
            java.lang.String r13 = r3.toString()
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.HttpPostBody.toString(int):java.lang.String");
    }

    private String makeIndent(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < i * 2; i2++) {
            stringBuffer.append("    ");
        }
        return stringBuffer.toString();
    }

    private int getFieldSize(String str) {
        if (str != null) {
            return str.length();
        }
        return 0;
    }
}

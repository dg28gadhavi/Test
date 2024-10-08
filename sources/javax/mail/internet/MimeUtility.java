package javax.mail.internet;

import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.QPDecoderStream;
import com.sun.mail.util.QPEncoderStream;
import com.sun.mail.util.UUDecoderStream;
import com.sun.mail.util.UUEncoderStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.mail.MessagingException;

public class MimeUtility {
    private static boolean decodeStrict = true;
    private static boolean encodeEolStrict = false;
    private static boolean foldEncodedWords = false;
    private static boolean foldText = true;
    private static Hashtable java2mime = new Hashtable(40);
    private static Hashtable mime2java = new Hashtable(10);

    private MimeUtility() {
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(5:42|45|46|47|48) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x008a */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0022 A[SYNTHETIC, Splitter:B:13:0x0022] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x003b A[Catch:{ SecurityException -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x003d A[Catch:{ SecurityException -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e A[Catch:{ SecurityException -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004f A[Catch:{ SecurityException -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x006e A[SYNTHETIC, Splitter:B:36:0x006e] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01b1  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A[RETURN, SYNTHETIC] */
    static {
        /*
            java.lang.String r0 = "mail.mime.decodetext.strict"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0052 }
            java.lang.String r1 = "false"
            r2 = 0
            r3 = 1
            if (r0 == 0) goto L_0x0014
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x0014
            r0 = r2
            goto L_0x0015
        L_0x0014:
            r0 = r3
        L_0x0015:
            decodeStrict = r0     // Catch:{ SecurityException -> 0x0052 }
            java.lang.String r0 = "mail.mime.encodeeol.strict"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0052 }
            java.lang.String r4 = "true"
            if (r0 == 0) goto L_0x002a
            boolean r0 = r0.equalsIgnoreCase(r4)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x002a
            r0 = r3
            goto L_0x002b
        L_0x002a:
            r0 = r2
        L_0x002b:
            encodeEolStrict = r0     // Catch:{ SecurityException -> 0x0052 }
            java.lang.String r0 = "mail.mime.foldencodedwords"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x003d
            boolean r0 = r0.equalsIgnoreCase(r4)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x003d
            r0 = r3
            goto L_0x003e
        L_0x003d:
            r0 = r2
        L_0x003e:
            foldEncodedWords = r0     // Catch:{ SecurityException -> 0x0052 }
            java.lang.String r0 = "mail.mime.foldtext"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x004f
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ SecurityException -> 0x0052 }
            if (r0 == 0) goto L_0x004f
            goto L_0x0050
        L_0x004f:
            r2 = r3
        L_0x0050:
            foldText = r2     // Catch:{ SecurityException -> 0x0052 }
        L_0x0052:
            java.util.Hashtable r0 = new java.util.Hashtable
            r1 = 40
            r0.<init>(r1)
            java2mime = r0
            java.util.Hashtable r0 = new java.util.Hashtable
            r1 = 10
            r0.<init>(r1)
            mime2java = r0
            java.lang.Class<javax.mail.internet.MimeUtility> r0 = javax.mail.internet.MimeUtility.class
            java.lang.String r1 = "/META-INF/javamail.charset.map"
            java.io.InputStream r0 = r0.getResourceAsStream(r1)     // Catch:{ Exception -> 0x008b }
            if (r0 == 0) goto L_0x008b
            com.sun.mail.util.LineInputStream r1 = new com.sun.mail.util.LineInputStream     // Catch:{ all -> 0x0083 }
            r1.<init>(r0)     // Catch:{ all -> 0x0083 }
            java.util.Hashtable r0 = java2mime     // Catch:{ all -> 0x0081 }
            loadMappings(r1, r0)     // Catch:{ all -> 0x0081 }
            java.util.Hashtable r0 = mime2java     // Catch:{ all -> 0x0081 }
            loadMappings(r1, r0)     // Catch:{ all -> 0x0081 }
            r1.close()     // Catch:{ Exception -> 0x008b }
            goto L_0x008b
        L_0x0081:
            r0 = move-exception
            goto L_0x0087
        L_0x0083:
            r1 = move-exception
            r5 = r1
            r1 = r0
            r0 = r5
        L_0x0087:
            r1.close()     // Catch:{ Exception -> 0x008a }
        L_0x008a:
            throw r0     // Catch:{ Exception -> 0x008b }
        L_0x008b:
            java.util.Hashtable r0 = java2mime
            boolean r0 = r0.isEmpty()
            java.lang.String r1 = "euc-kr"
            java.lang.String r2 = "ISO-8859-1"
            if (r0 == 0) goto L_0x01a9
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-1"
            r0.put(r3, r2)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_2"
            java.lang.String r4 = "ISO-8859-2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-2"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_3"
            java.lang.String r4 = "ISO-8859-3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-3"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_4"
            java.lang.String r4 = "ISO-8859-4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-4"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_5"
            java.lang.String r4 = "ISO-8859-5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-5"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_6"
            java.lang.String r4 = "ISO-8859-6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-6"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_7"
            java.lang.String r4 = "ISO-8859-7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-7"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_8"
            java.lang.String r4 = "ISO-8859-8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "8859_9"
            java.lang.String r4 = "ISO-8859-9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859_9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso8859-9"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "sjis"
            java.lang.String r4 = "Shift_JIS"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "jis"
            java.lang.String r4 = "ISO-2022-JP"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "iso2022jp"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_jp"
            java.lang.String r4 = "euc-jp"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "koi8_r"
            java.lang.String r4 = "koi8-r"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_cn"
            java.lang.String r4 = "euc-cn"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_tw"
            java.lang.String r4 = "euc-tw"
            r0.put(r3, r4)
            java.util.Hashtable r0 = java2mime
            java.lang.String r3 = "euc_kr"
            r0.put(r3, r1)
        L_0x01a9:
            java.util.Hashtable r0 = mime2java
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x0205
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "iso-2022-cn"
            java.lang.String r4 = "ISO2022CN"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "iso-2022-kr"
            java.lang.String r4 = "ISO2022KR"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "utf-8"
            java.lang.String r4 = "UTF8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "utf8"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "ja_jp.iso2022-7"
            java.lang.String r4 = "ISO2022JP"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "ja_jp.eucjp"
            java.lang.String r4 = "EUCJIS"
            r0.put(r3, r4)
            java.util.Hashtable r0 = mime2java
            java.lang.String r3 = "KSC5601"
            r0.put(r1, r3)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "euckr"
            r0.put(r1, r3)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "us-ascii"
            r0.put(r1, r2)
            java.util.Hashtable r0 = mime2java
            java.lang.String r1 = "x-us-ascii"
            r0.put(r1, r2)
        L_0x0205:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.<clinit>():void");
    }

    public static InputStream decode(InputStream inputStream, String str) throws MessagingException {
        if (str.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return new BASE64DecoderStream(inputStream);
        }
        if (str.equalsIgnoreCase("quoted-printable")) {
            return new QPDecoderStream(inputStream);
        }
        if (str.equalsIgnoreCase("uuencode") || str.equalsIgnoreCase("x-uuencode") || str.equalsIgnoreCase("x-uue")) {
            return new UUDecoderStream(inputStream);
        }
        if (str.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BINARY) || str.equalsIgnoreCase("7bit") || str.equalsIgnoreCase("8bit")) {
            return inputStream;
        }
        throw new MessagingException("Unknown encoding: " + str);
    }

    public static OutputStream encode(OutputStream outputStream, String str) throws MessagingException {
        if (str == null) {
            return outputStream;
        }
        if (str.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return new BASE64EncoderStream(outputStream);
        }
        if (str.equalsIgnoreCase("quoted-printable")) {
            return new QPEncoderStream(outputStream);
        }
        if (str.equalsIgnoreCase("uuencode") || str.equalsIgnoreCase("x-uuencode") || str.equalsIgnoreCase("x-uue")) {
            return new UUEncoderStream(outputStream);
        }
        if (str.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BINARY) || str.equalsIgnoreCase("7bit") || str.equalsIgnoreCase("8bit")) {
            return outputStream;
        }
        throw new MessagingException("Unknown encoding: " + str);
    }

    public static String quote(String str, String str2) {
        int length = str.length();
        char c = 0;
        int i = 0;
        boolean z = false;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '\"' || charAt == '\\' || charAt == 13 || charAt == 10) {
                StringBuffer stringBuffer = new StringBuffer(length + 3);
                stringBuffer.append('\"');
                stringBuffer.append(str.substring(0, i));
                while (i < length) {
                    char charAt2 = str.charAt(i);
                    if ((charAt2 == '\"' || charAt2 == '\\' || charAt2 == 13 || charAt2 == 10) && !(charAt2 == 10 && c == 13)) {
                        stringBuffer.append('\\');
                    }
                    stringBuffer.append(charAt2);
                    i++;
                    c = charAt2;
                }
                stringBuffer.append('\"');
                return stringBuffer.toString();
            }
            if (charAt < ' ' || charAt >= 127 || str2.indexOf(charAt) >= 0) {
                z = true;
            }
            i++;
        }
        if (!z) {
            return str;
        }
        StringBuffer stringBuffer2 = new StringBuffer(length + 2);
        stringBuffer2.append('\"');
        stringBuffer2.append(str);
        stringBuffer2.append('\"');
        return stringBuffer2.toString();
    }

    public static String fold(int i, String str) {
        if (!foldText) {
            return str;
        }
        int length = str.length() - 1;
        while (length >= 0 && ((r4 = str.charAt(length)) == ' ' || r4 == 9 || r4 == 13 || r4 == 10)) {
            length--;
        }
        if (length != str.length() - 1) {
            str = str.substring(0, length + 1);
        }
        if (str.length() + i <= 76) {
            return str;
        }
        StringBuffer stringBuffer = new StringBuffer(str.length() + 4);
        int i2 = i;
        String str2 = str;
        char c = 0;
        while (true) {
            if (str2.length() + i2 <= 76) {
                break;
            }
            int i3 = 0;
            int i4 = -1;
            while (i3 < str2.length() && (i4 == -1 || i2 + i3 <= 76)) {
                char charAt = str2.charAt(i3);
                if (!((charAt != ' ' && charAt != 9) || c == ' ' || c == 9)) {
                    i4 = i3;
                }
                i3++;
                c = charAt;
            }
            if (i4 == -1) {
                stringBuffer.append(str2);
                str2 = "";
                break;
            }
            stringBuffer.append(str2.substring(0, i4));
            stringBuffer.append("\r\n");
            c = str2.charAt(i4);
            stringBuffer.append(c);
            str2 = str2.substring(i4 + 1);
            i2 = 1;
        }
        stringBuffer.append(str2);
        return stringBuffer.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0007, code lost:
        r0 = (java.lang.String) r0.get(r2.toLowerCase(java.util.Locale.ENGLISH));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String javaCharset(java.lang.String r2) {
        /*
            java.util.Hashtable r0 = mime2java
            if (r0 == 0) goto L_0x0017
            if (r2 != 0) goto L_0x0007
            goto L_0x0017
        L_0x0007:
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.String r1 = r2.toLowerCase(r1)
            java.lang.Object r0 = r0.get(r1)
            java.lang.String r0 = (java.lang.String) r0
            if (r0 != 0) goto L_0x0016
            goto L_0x0017
        L_0x0016:
            r2 = r0
        L_0x0017:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.javaCharset(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:0:0x0000 */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP:0: B:0:0x0000->B:15:0x0000, LOOP_START, MTH_ENTER_BLOCK, SYNTHETIC, Splitter:B:0:0x0000] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void loadMappings(com.sun.mail.util.LineInputStream r3, java.util.Hashtable r4) {
        /*
        L_0x0000:
            java.lang.String r0 = r3.readLine()     // Catch:{ IOException -> 0x0042 }
            if (r0 != 0) goto L_0x0007
            goto L_0x0042
        L_0x0007:
            java.lang.String r1 = "--"
            boolean r2 = r0.startsWith(r1)
            if (r2 == 0) goto L_0x0016
            boolean r1 = r0.endsWith(r1)
            if (r1 == 0) goto L_0x0016
            goto L_0x0042
        L_0x0016:
            java.lang.String r1 = r0.trim()
            int r1 = r1.length()
            if (r1 == 0) goto L_0x0000
            java.lang.String r1 = "#"
            boolean r1 = r0.startsWith(r1)
            if (r1 == 0) goto L_0x0029
            goto L_0x0000
        L_0x0029:
            java.util.StringTokenizer r1 = new java.util.StringTokenizer
            java.lang.String r2 = " \t"
            r1.<init>(r0, r2)
            java.lang.String r0 = r1.nextToken()     // Catch:{ NoSuchElementException -> 0x0000 }
            java.lang.String r1 = r1.nextToken()     // Catch:{ NoSuchElementException -> 0x0000 }
            java.util.Locale r2 = java.util.Locale.ENGLISH     // Catch:{ NoSuchElementException -> 0x0000 }
            java.lang.String r0 = r0.toLowerCase(r2)     // Catch:{ NoSuchElementException -> 0x0000 }
            r4.put(r0, r1)     // Catch:{ NoSuchElementException -> 0x0000 }
            goto L_0x0000
        L_0x0042:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeUtility.loadMappings(com.sun.mail.util.LineInputStream, java.util.Hashtable):void");
    }
}

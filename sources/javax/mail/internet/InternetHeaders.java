package javax.mail.internet;

import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.helper.httpclient.HttpController;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders {
    protected List headers;

    protected static final class InternetHeader extends Header {
        String line;

        public InternetHeader(String str) {
            super("", "");
            int indexOf = str.indexOf(58);
            if (indexOf < 0) {
                this.name = str.trim();
            } else {
                this.name = str.substring(0, indexOf).trim();
            }
            this.line = str;
        }

        public InternetHeader(String str, String str2) {
            super(str, "");
            if (str2 != null) {
                this.line = String.valueOf(str) + ": " + str2;
                return;
            }
            this.line = null;
        }

        public String getValue() {
            char charAt;
            int indexOf = this.line.indexOf(58);
            if (indexOf < 0) {
                return this.line;
            }
            while (true) {
                indexOf++;
                if (indexOf < this.line.length() && ((charAt = this.line.charAt(indexOf)) == ' ' || charAt == 9 || charAt == 13 || charAt == 10)) {
                }
            }
            return this.line.substring(indexOf);
        }
    }

    static class matchEnum implements Enumeration {
        private Iterator e;
        private boolean match;
        private String[] names;
        private InternetHeader next_header = null;
        private boolean want_line;

        matchEnum(List list, String[] strArr, boolean z, boolean z2) {
            this.e = list.iterator();
            this.names = strArr;
            this.match = z;
            this.want_line = z2;
        }

        public boolean hasMoreElements() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            return this.next_header != null;
        }

        public Object nextElement() {
            if (this.next_header == null) {
                this.next_header = nextMatch();
            }
            InternetHeader internetHeader = this.next_header;
            if (internetHeader != null) {
                this.next_header = null;
                if (this.want_line) {
                    return internetHeader.line;
                }
                return new Header(internetHeader.getName(), internetHeader.getValue());
            }
            throw new NoSuchElementException("No more headers");
        }

        private InternetHeader nextMatch() {
            while (this.e.hasNext()) {
                InternetHeader internetHeader = (InternetHeader) this.e.next();
                if (internetHeader.line != null) {
                    if (this.names != null) {
                        int i = 0;
                        while (true) {
                            String[] strArr = this.names;
                            if (i >= strArr.length) {
                                if (!this.match) {
                                    return internetHeader;
                                }
                            } else if (!strArr[i].equalsIgnoreCase(internetHeader.getName())) {
                                i++;
                            } else if (this.match) {
                                return internetHeader;
                            }
                        }
                    } else if (this.match) {
                        return null;
                    } else {
                        return internetHeader;
                    }
                }
            }
            return null;
        }
    }

    public InternetHeaders() {
        ArrayList arrayList = new ArrayList(40);
        this.headers = arrayList;
        arrayList.add(new InternetHeader("Return-Path", (String) null));
        this.headers.add(new InternetHeader("Received", (String) null));
        this.headers.add(new InternetHeader("Resent-Date", (String) null));
        this.headers.add(new InternetHeader("Resent-From", (String) null));
        this.headers.add(new InternetHeader("Resent-Sender", (String) null));
        this.headers.add(new InternetHeader("Resent-To", (String) null));
        this.headers.add(new InternetHeader("Resent-Cc", (String) null));
        this.headers.add(new InternetHeader("Resent-Bcc", (String) null));
        this.headers.add(new InternetHeader("Resent-Message-Id", (String) null));
        this.headers.add(new InternetHeader("Date", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.from, (String) null));
        this.headers.add(new InternetHeader("Sender", (String) null));
        this.headers.add(new InternetHeader("Reply-To", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.to, (String) null));
        this.headers.add(new InternetHeader(AttributeNames.cc, (String) null));
        this.headers.add(new InternetHeader(AttributeNames.bcc, (String) null));
        this.headers.add(new InternetHeader("Message-Id", (String) null));
        this.headers.add(new InternetHeader("In-Reply-To", (String) null));
        this.headers.add(new InternetHeader("References", (String) null));
        this.headers.add(new InternetHeader(AttributeNames.subject, (String) null));
        this.headers.add(new InternetHeader("Comments", (String) null));
        this.headers.add(new InternetHeader("Keywords", (String) null));
        this.headers.add(new InternetHeader("Errors-To", (String) null));
        this.headers.add(new InternetHeader("MIME-Version", (String) null));
        this.headers.add(new InternetHeader("Content-Type", (String) null));
        this.headers.add(new InternetHeader(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, (String) null));
        this.headers.add(new InternetHeader("Content-MD5", (String) null));
        this.headers.add(new InternetHeader(":", (String) null));
        this.headers.add(new InternetHeader("Content-Length", (String) null));
        this.headers.add(new InternetHeader("Status", (String) null));
    }

    public InternetHeaders(InputStream inputStream) throws MessagingException {
        this.headers = new ArrayList(40);
        load(inputStream);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0031 A[Catch:{ IOException -> 0x0052 }] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0022 A[Catch:{ IOException -> 0x0052 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(java.io.InputStream r6) throws javax.mail.MessagingException {
        /*
            r5 = this;
            com.sun.mail.util.LineInputStream r0 = new com.sun.mail.util.LineInputStream
            r0.<init>(r6)
            java.lang.StringBuffer r6 = new java.lang.StringBuffer
            r6.<init>()
            r1 = 0
            r2 = r1
        L_0x000c:
            java.lang.String r3 = r0.readLine()     // Catch:{ IOException -> 0x0052 }
            if (r3 == 0) goto L_0x0031
            java.lang.String r4 = " "
            boolean r4 = r3.startsWith(r4)     // Catch:{ IOException -> 0x0052 }
            if (r4 != 0) goto L_0x0022
            java.lang.String r4 = "\t"
            boolean r4 = r3.startsWith(r4)     // Catch:{ IOException -> 0x0052 }
            if (r4 == 0) goto L_0x0031
        L_0x0022:
            if (r2 == 0) goto L_0x0028
            r6.append(r2)     // Catch:{ IOException -> 0x0052 }
            r2 = r1
        L_0x0028:
            java.lang.String r4 = "\r\n"
            r6.append(r4)     // Catch:{ IOException -> 0x0052 }
            r6.append(r3)     // Catch:{ IOException -> 0x0052 }
            goto L_0x0049
        L_0x0031:
            if (r2 == 0) goto L_0x0037
            r5.addHeaderLine(r2)     // Catch:{ IOException -> 0x0052 }
            goto L_0x0048
        L_0x0037:
            int r2 = r6.length()     // Catch:{ IOException -> 0x0052 }
            if (r2 <= 0) goto L_0x0048
            java.lang.String r2 = r6.toString()     // Catch:{ IOException -> 0x0052 }
            r5.addHeaderLine(r2)     // Catch:{ IOException -> 0x0052 }
            r2 = 0
            r6.setLength(r2)     // Catch:{ IOException -> 0x0052 }
        L_0x0048:
            r2 = r3
        L_0x0049:
            if (r3 == 0) goto L_0x0051
            int r3 = r3.length()     // Catch:{ IOException -> 0x0052 }
            if (r3 > 0) goto L_0x000c
        L_0x0051:
            return
        L_0x0052:
            r5 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException
            java.lang.String r0 = "Error in input stream"
            r6.<init>(r0, r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetHeaders.load(java.io.InputStream):void");
    }

    public String[] getHeader(String str) {
        ArrayList arrayList = new ArrayList();
        for (InternetHeader internetHeader : this.headers) {
            if (str.equalsIgnoreCase(internetHeader.getName()) && internetHeader.line != null) {
                arrayList.add(internetHeader.getValue());
            }
        }
        if (arrayList.size() == 0) {
            return null;
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    public String getHeader(String str, String str2) {
        String[] header = getHeader(str);
        if (header == null) {
            return null;
        }
        if (header.length == 1 || str2 == null) {
            return header[0];
        }
        StringBuffer stringBuffer = new StringBuffer(header[0]);
        for (int i = 1; i < header.length; i++) {
            stringBuffer.append(str2);
            stringBuffer.append(header[i]);
        }
        return stringBuffer.toString();
    }

    public void addHeaderLine(String str) {
        try {
            char charAt = str.charAt(0);
            if (charAt != ' ') {
                if (charAt != 9) {
                    this.headers.add(new InternetHeader(str));
                    return;
                }
            }
            List list = this.headers;
            InternetHeader internetHeader = (InternetHeader) list.get(list.size() - 1);
            internetHeader.line = String.valueOf(internetHeader.line) + "\r\n" + str;
        } catch (StringIndexOutOfBoundsException | NoSuchElementException unused) {
        }
    }

    public Enumeration getNonMatchingHeaderLines(String[] strArr) {
        return new matchEnum(this.headers, strArr, false, true);
    }
}

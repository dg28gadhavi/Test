package javax.mail.internet;

import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.HeaderTokenizer;

public class MimeBodyPart extends BodyPart implements MimePart {
    static boolean cacheMultipart = true;
    private static boolean decodeFileName = false;
    private static boolean encodeFileName = false;
    private static boolean setContentTypeFileName = true;
    private static boolean setDefaultTextCharset = true;
    protected byte[] content;
    protected InputStream contentStream;
    protected DataHandler dh;
    protected InternetHeaders headers;

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0025 A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027 A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0038 A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003a A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004b A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x004d A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005e A[Catch:{ SecurityException -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x005f A[Catch:{ SecurityException -> 0x0062 }] */
    static {
        /*
            java.lang.String r0 = "mail.mime.setdefaulttextcharset"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0062 }
            r1 = 0
            java.lang.String r2 = "false"
            r3 = 1
            if (r0 == 0) goto L_0x0014
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x0014
            r0 = r1
            goto L_0x0015
        L_0x0014:
            r0 = r3
        L_0x0015:
            setDefaultTextCharset = r0     // Catch:{ SecurityException -> 0x0062 }
            java.lang.String r0 = "mail.mime.setcontenttypefilename"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x0027
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x0027
            r0 = r1
            goto L_0x0028
        L_0x0027:
            r0 = r3
        L_0x0028:
            setContentTypeFileName = r0     // Catch:{ SecurityException -> 0x0062 }
            java.lang.String r0 = "mail.mime.encodefilename"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x003a
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 != 0) goto L_0x003a
            r0 = r3
            goto L_0x003b
        L_0x003a:
            r0 = r1
        L_0x003b:
            encodeFileName = r0     // Catch:{ SecurityException -> 0x0062 }
            java.lang.String r0 = "mail.mime.decodefilename"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x004d
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 != 0) goto L_0x004d
            r0 = r3
            goto L_0x004e
        L_0x004d:
            r0 = r1
        L_0x004e:
            decodeFileName = r0     // Catch:{ SecurityException -> 0x0062 }
            java.lang.String r0 = "mail.mime.cachemultipart"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x005f
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0062 }
            if (r0 == 0) goto L_0x005f
            goto L_0x0060
        L_0x005f:
            r1 = r3
        L_0x0060:
            cacheMultipart = r1     // Catch:{ SecurityException -> 0x0062 }
        L_0x0062:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeBodyPart.<clinit>():void");
    }

    public MimeBodyPart() {
        this.headers = new InternetHeaders();
    }

    public MimeBodyPart(InputStream inputStream) throws MessagingException {
        if (!(inputStream instanceof ByteArrayInputStream) && !(inputStream instanceof BufferedInputStream) && !(inputStream instanceof SharedInputStream)) {
            inputStream = new BufferedInputStream(inputStream);
        }
        this.headers = new InternetHeaders(inputStream);
        if (inputStream instanceof SharedInputStream) {
            SharedInputStream sharedInputStream = (SharedInputStream) inputStream;
            this.contentStream = sharedInputStream.newStream(sharedInputStream.getPosition(), -1);
            return;
        }
        try {
            this.content = ASCIIUtility.getBytes(inputStream);
        } catch (IOException e) {
            throw new MessagingException("Error reading input stream", e);
        }
    }

    public MimeBodyPart(InternetHeaders internetHeaders, byte[] bArr) throws MessagingException {
        this.headers = internetHeaders;
        this.content = bArr;
    }

    public int getSize() throws MessagingException {
        byte[] bArr = this.content;
        if (bArr != null) {
            return bArr.length;
        }
        InputStream inputStream = this.contentStream;
        if (inputStream == null) {
            return -1;
        }
        try {
            int available = inputStream.available();
            if (available > 0) {
                return available;
            }
            return -1;
        } catch (IOException unused) {
            return -1;
        }
    }

    public String getContentType() throws MessagingException {
        String header = getHeader("Content-Type", (String) null);
        return header == null ? MIMEContentType.PLAIN_TEXT : header;
    }

    public String getEncoding() throws MessagingException {
        return getEncoding(this);
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return getDataHandler().getInputStream();
    }

    /* access modifiers changed from: protected */
    public InputStream getContentStream() throws MessagingException {
        InputStream inputStream = this.contentStream;
        if (inputStream != null) {
            return ((SharedInputStream) inputStream).newStream(0, -1);
        }
        if (this.content != null) {
            return new ByteArrayInputStream(this.content);
        }
        throw new MessagingException("No content");
    }

    public DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            this.dh = new DataHandler(new MimePartDataSource(this));
        }
        return this.dh;
    }

    public void writeTo(OutputStream outputStream) throws IOException, MessagingException {
        writeTo(this, outputStream, (String[]) null);
    }

    public String[] getHeader(String str) throws MessagingException {
        return this.headers.getHeader(str);
    }

    public String getHeader(String str, String str2) throws MessagingException {
        return this.headers.getHeader(str, str2);
    }

    public Enumeration getNonMatchingHeaderLines(String[] strArr) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(strArr);
    }

    static String getEncoding(MimePart mimePart) throws MessagingException {
        HeaderTokenizer.Token next;
        int type;
        String header = mimePart.getHeader(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, (String) null);
        if (header == null) {
            return null;
        }
        String trim = header.trim();
        if (trim.equalsIgnoreCase("7bit") || trim.equalsIgnoreCase("8bit") || trim.equalsIgnoreCase("quoted-printable") || trim.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BINARY) || trim.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) {
            return trim;
        }
        HeaderTokenizer headerTokenizer = new HeaderTokenizer(trim, "()<>@,;:\\\"\t []/?=");
        do {
            next = headerTokenizer.next();
            type = next.getType();
            if (type == -4) {
                return trim;
            }
        } while (type != -1);
        return next.getValue();
    }

    static void writeTo(MimePart mimePart, OutputStream outputStream, String[] strArr) throws IOException, MessagingException {
        LineOutputStream lineOutputStream;
        if (outputStream instanceof LineOutputStream) {
            lineOutputStream = (LineOutputStream) outputStream;
        } else {
            lineOutputStream = new LineOutputStream(outputStream);
        }
        Enumeration nonMatchingHeaderLines = mimePart.getNonMatchingHeaderLines(strArr);
        while (nonMatchingHeaderLines.hasMoreElements()) {
            lineOutputStream.writeln((String) nonMatchingHeaderLines.nextElement());
        }
        lineOutputStream.writeln();
        OutputStream encode = MimeUtility.encode(outputStream, mimePart.getEncoding());
        mimePart.getDataHandler().writeTo(encode);
        encode.flush();
    }
}

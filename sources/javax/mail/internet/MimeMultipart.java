package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

public class MimeMultipart extends Multipart {
    private static boolean bmparse = true;
    private static boolean ignoreMissingBoundaryParameter = true;
    private static boolean ignoreMissingEndBoundary = true;
    private boolean complete;
    protected DataSource ds;
    protected boolean parsed;
    private String preamble;

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0025 A[Catch:{ SecurityException -> 0x003c }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027 A[Catch:{ SecurityException -> 0x003c }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0038 A[Catch:{ SecurityException -> 0x003c }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0039 A[Catch:{ SecurityException -> 0x003c }] */
    static {
        /*
            java.lang.String r0 = "mail.mime.multipart.ignoremissingendboundary"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x003c }
            r1 = 0
            java.lang.String r2 = "false"
            r3 = 1
            if (r0 == 0) goto L_0x0014
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x003c }
            if (r0 == 0) goto L_0x0014
            r0 = r1
            goto L_0x0015
        L_0x0014:
            r0 = r3
        L_0x0015:
            ignoreMissingEndBoundary = r0     // Catch:{ SecurityException -> 0x003c }
            java.lang.String r0 = "mail.mime.multipart.ignoremissingboundaryparameter"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x003c }
            if (r0 == 0) goto L_0x0027
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x003c }
            if (r0 == 0) goto L_0x0027
            r0 = r1
            goto L_0x0028
        L_0x0027:
            r0 = r3
        L_0x0028:
            ignoreMissingBoundaryParameter = r0     // Catch:{ SecurityException -> 0x003c }
            java.lang.String r0 = "mail.mime.multipart.bmparse"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x003c }
            if (r0 == 0) goto L_0x0039
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x003c }
            if (r0 == 0) goto L_0x0039
            goto L_0x003a
        L_0x0039:
            r1 = r3
        L_0x003a:
            bmparse = r1     // Catch:{ SecurityException -> 0x003c }
        L_0x003c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.<clinit>():void");
    }

    public MimeMultipart() {
        this("mixed");
    }

    public MimeMultipart(String str) {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        String uniqueBoundaryValue = UniqueValue.getUniqueBoundaryValue();
        ContentType contentType = new ContentType("multipart", str, (ParameterList) null);
        contentType.setParameter("boundary", uniqueBoundaryValue);
        this.contentType = contentType.toString();
    }

    public MimeMultipart(DataSource dataSource) throws MessagingException {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        if (dataSource instanceof MessageAware) {
            setParent(((MessageAware) dataSource).getMessageContext().getPart());
        }
        if (dataSource instanceof MultipartDataSource) {
            setMultipartDataSource((MultipartDataSource) dataSource);
            return;
        }
        this.parsed = false;
        this.ds = dataSource;
        this.contentType = dataSource.getContentType();
    }

    public synchronized int getCount() throws MessagingException {
        parse();
        return super.getCount();
    }

    public synchronized BodyPart getBodyPart(int i) throws MessagingException {
        parse();
        return super.getBodyPart(i);
    }

    public synchronized void addBodyPart(BodyPart bodyPart) throws MessagingException {
        parse();
        super.addBodyPart(bodyPart);
    }

    public synchronized void writeTo(OutputStream outputStream) throws IOException, MessagingException {
        parse();
        String str = "--" + new ContentType(this.contentType).getParameter("boundary");
        LineOutputStream lineOutputStream = new LineOutputStream(outputStream);
        String str2 = this.preamble;
        if (str2 != null) {
            byte[] bytes = ASCIIUtility.getBytes(str2);
            lineOutputStream.write(bytes);
            if (!(bytes.length <= 0 || bytes[bytes.length - 1] == 13 || bytes[bytes.length - 1] == 10)) {
                lineOutputStream.writeln();
            }
        }
        for (int i = 0; i < this.parts.size(); i++) {
            lineOutputStream.writeln(str);
            ((MimeBodyPart) this.parts.elementAt(i)).writeTo(outputStream);
            lineOutputStream.writeln();
        }
        lineOutputStream.writeln(String.valueOf(str) + "--");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0140, code lost:
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00bf, code lost:
        if (ignoreMissingEndBoundary == false) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00c1, code lost:
        r1.complete = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00d1, code lost:
        throw new javax.mail.MessagingException("missing multipart end boundary");
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:177:0x023f */
    /* JADX WARNING: Missing exception handler attribute for start block: B:70:0x00c6 */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x01ad A[Catch:{ Exception -> 0x0248, IOException -> 0x0233, all -> 0x0231 }] */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x00bd A[EDGE_INSN: B:199:0x00bd->B:65:0x00bd ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x017f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00d2 A[Catch:{ Exception -> 0x0248, IOException -> 0x0233, all -> 0x0231 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void parse() throws javax.mail.MessagingException {
        /*
            r23 = this;
            r1 = r23
            monitor-enter(r23)
            boolean r0 = r1.parsed     // Catch:{ all -> 0x0251 }
            if (r0 == 0) goto L_0x0009
            monitor-exit(r23)
            return
        L_0x0009:
            boolean r0 = bmparse     // Catch:{ all -> 0x0251 }
            if (r0 == 0) goto L_0x0012
            r23.parsebm()     // Catch:{ all -> 0x0251 }
            monitor-exit(r23)
            return
        L_0x0012:
            javax.activation.DataSource r0 = r1.ds     // Catch:{ Exception -> 0x0248 }
            java.io.InputStream r0 = r0.getInputStream()     // Catch:{ Exception -> 0x0248 }
            boolean r2 = r0 instanceof java.io.ByteArrayInputStream     // Catch:{ Exception -> 0x0248 }
            if (r2 != 0) goto L_0x002a
            boolean r2 = r0 instanceof java.io.BufferedInputStream     // Catch:{ Exception -> 0x0248 }
            if (r2 != 0) goto L_0x002a
            boolean r2 = r0 instanceof javax.mail.internet.SharedInputStream     // Catch:{ Exception -> 0x0248 }
            if (r2 != 0) goto L_0x002a
            java.io.BufferedInputStream r2 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x0248 }
            r2.<init>(r0)     // Catch:{ Exception -> 0x0248 }
            goto L_0x002b
        L_0x002a:
            r2 = r0
        L_0x002b:
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ all -> 0x0251 }
            if (r0 == 0) goto L_0x0033
            r0 = r2
            javax.mail.internet.SharedInputStream r0 = (javax.mail.internet.SharedInputStream) r0     // Catch:{ all -> 0x0251 }
            goto L_0x0034
        L_0x0033:
            r0 = 0
        L_0x0034:
            javax.mail.internet.ContentType r4 = new javax.mail.internet.ContentType     // Catch:{ all -> 0x0251 }
            java.lang.String r5 = r1.contentType     // Catch:{ all -> 0x0251 }
            r4.<init>(r5)     // Catch:{ all -> 0x0251 }
            java.lang.String r5 = "boundary"
            java.lang.String r4 = r4.getParameter(r5)     // Catch:{ all -> 0x0251 }
            if (r4 == 0) goto L_0x0052
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0251 }
            java.lang.String r6 = "--"
            r5.<init>(r6)     // Catch:{ all -> 0x0251 }
            r5.append(r4)     // Catch:{ all -> 0x0251 }
            java.lang.String r4 = r5.toString()     // Catch:{ all -> 0x0251 }
            goto L_0x0057
        L_0x0052:
            boolean r4 = ignoreMissingBoundaryParameter     // Catch:{ all -> 0x0251 }
            if (r4 == 0) goto L_0x0240
            r4 = 0
        L_0x0057:
            com.sun.mail.util.LineInputStream r5 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x0233 }
            r5.<init>(r2)     // Catch:{ IOException -> 0x0233 }
            r6 = 0
            r7 = 0
        L_0x005e:
            java.lang.String r8 = r5.readLine()     // Catch:{ IOException -> 0x0233 }
            r9 = 9
            r10 = 32
            r11 = 0
            r12 = 1
            if (r8 != 0) goto L_0x006b
            goto L_0x0093
        L_0x006b:
            int r13 = r8.length()     // Catch:{ IOException -> 0x0233 }
            int r13 = r13 - r12
        L_0x0070:
            if (r13 >= 0) goto L_0x0073
            goto L_0x007b
        L_0x0073:
            char r14 = r8.charAt(r13)     // Catch:{ IOException -> 0x0233 }
            if (r14 == r10) goto L_0x0227
            if (r14 == r9) goto L_0x0227
        L_0x007b:
            int r13 = r13 + 1
            java.lang.String r8 = r8.substring(r11, r13)     // Catch:{ IOException -> 0x0233 }
            if (r4 == 0) goto L_0x008a
            boolean r13 = r8.equals(r4)     // Catch:{ IOException -> 0x0233 }
            if (r13 == 0) goto L_0x01fd
            goto L_0x0093
        L_0x008a:
            java.lang.String r13 = "--"
            boolean r13 = r8.startsWith(r13)     // Catch:{ IOException -> 0x0233 }
            if (r13 == 0) goto L_0x01fd
            r4 = r8
        L_0x0093:
            if (r8 == 0) goto L_0x01f5
            if (r6 == 0) goto L_0x009d
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x0233 }
            r1.preamble = r6     // Catch:{ IOException -> 0x0233 }
        L_0x009d:
            byte[] r4 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r4)     // Catch:{ IOException -> 0x0233 }
            int r6 = r4.length     // Catch:{ IOException -> 0x0233 }
            r7 = 0
            r13 = r7
            r15 = r11
        L_0x00a6:
            if (r15 == 0) goto L_0x00a9
            goto L_0x00c3
        L_0x00a9:
            if (r0 == 0) goto L_0x00d4
            long r7 = r0.getPosition()     // Catch:{ IOException -> 0x0233 }
        L_0x00af:
            java.lang.String r16 = r5.readLine()     // Catch:{ IOException -> 0x0233 }
            if (r16 == 0) goto L_0x00bb
            int r17 = r16.length()     // Catch:{ IOException -> 0x0233 }
            if (r17 > 0) goto L_0x00af
        L_0x00bb:
            if (r16 != 0) goto L_0x00d2
            boolean r0 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0233 }
            if (r0 == 0) goto L_0x00ca
            r1.complete = r11     // Catch:{ IOException -> 0x0233 }
        L_0x00c3:
            r2.close()     // Catch:{ IOException -> 0x00c6 }
        L_0x00c6:
            r1.parsed = r12     // Catch:{ all -> 0x0251 }
            monitor-exit(r23)
            return
        L_0x00ca:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0233 }
            java.lang.String r3 = "missing multipart end boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x0233 }
            throw r0     // Catch:{ IOException -> 0x0233 }
        L_0x00d2:
            r3 = 0
            goto L_0x00da
        L_0x00d4:
            javax.mail.internet.InternetHeaders r16 = r1.createInternetHeaders(r2)     // Catch:{ IOException -> 0x0233 }
            r3 = r16
        L_0x00da:
            boolean r17 = r2.markSupported()     // Catch:{ IOException -> 0x0233 }
            if (r17 == 0) goto L_0x01ed
            if (r0 != 0) goto L_0x00ea
            java.io.ByteArrayOutputStream r17 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0233 }
            r17.<init>()     // Catch:{ IOException -> 0x0233 }
            r11 = r17
            goto L_0x00ef
        L_0x00ea:
            long r13 = r0.getPosition()     // Catch:{ IOException -> 0x0233 }
            r11 = 0
        L_0x00ef:
            r18 = r12
            r19 = -1
            r20 = -1
        L_0x00f5:
            if (r18 == 0) goto L_0x0170
            int r9 = r6 + 4
            int r9 = r9 + 1000
            r2.mark(r9)     // Catch:{ IOException -> 0x0233 }
            r9 = 0
        L_0x00ff:
            if (r9 < r6) goto L_0x0102
            goto L_0x010c
        L_0x0102:
            int r10 = r2.read()     // Catch:{ IOException -> 0x0233 }
            byte r12 = r4[r9]     // Catch:{ IOException -> 0x0233 }
            r12 = r12 & 255(0xff, float:3.57E-43)
            if (r10 == r12) goto L_0x0163
        L_0x010c:
            if (r9 != r6) goto L_0x0147
            int r9 = r2.read()     // Catch:{ IOException -> 0x0233 }
            r10 = 45
            if (r9 != r10) goto L_0x0122
            int r12 = r2.read()     // Catch:{ IOException -> 0x0233 }
            if (r12 != r10) goto L_0x0122
            r10 = 1
            r1.complete = r10     // Catch:{ IOException -> 0x0233 }
            r10 = 0
            goto L_0x0186
        L_0x0122:
            r10 = 32
            if (r9 == r10) goto L_0x0142
            r12 = 9
            if (r9 == r12) goto L_0x0142
            r10 = 10
            if (r9 != r10) goto L_0x012f
            goto L_0x0140
        L_0x012f:
            r12 = 13
            if (r9 != r12) goto L_0x0147
            r9 = 1
            r2.mark(r9)     // Catch:{ IOException -> 0x0233 }
            int r9 = r2.read()     // Catch:{ IOException -> 0x0233 }
            if (r9 == r10) goto L_0x0140
            r2.reset()     // Catch:{ IOException -> 0x0233 }
        L_0x0140:
            r10 = 0
            goto L_0x0187
        L_0x0142:
            int r9 = r2.read()     // Catch:{ IOException -> 0x0233 }
            goto L_0x0122
        L_0x0147:
            r2.reset()     // Catch:{ IOException -> 0x0233 }
            r10 = r19
            r12 = -1
            if (r11 == 0) goto L_0x0160
            if (r10 == r12) goto L_0x0160
            r11.write(r10)     // Catch:{ IOException -> 0x0233 }
            r9 = r20
            if (r9 == r12) goto L_0x015b
            r11.write(r9)     // Catch:{ IOException -> 0x0233 }
        L_0x015b:
            r19 = r12
            r20 = r19
            goto L_0x0179
        L_0x0160:
            r19 = r20
            goto L_0x0175
        L_0x0163:
            r10 = r19
            r19 = r20
            r12 = -1
            int r9 = r9 + 1
            r12 = 1
            r19 = r10
            r10 = 32
            goto L_0x00ff
        L_0x0170:
            r10 = r19
            r19 = r20
            r12 = -1
        L_0x0175:
            r20 = r19
            r19 = r10
        L_0x0179:
            int r9 = r2.read()     // Catch:{ IOException -> 0x0233 }
            if (r9 >= 0) goto L_0x01ad
            boolean r9 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0233 }
            if (r9 == 0) goto L_0x01a5
            r10 = 0
            r1.complete = r10     // Catch:{ IOException -> 0x0233 }
        L_0x0186:
            r15 = 1
        L_0x0187:
            if (r0 == 0) goto L_0x0192
            java.io.InputStream r3 = r0.newStream(r7, r13)     // Catch:{ IOException -> 0x0233 }
            javax.mail.internet.MimeBodyPart r3 = r1.createMimeBodyPart(r3)     // Catch:{ IOException -> 0x0233 }
            goto L_0x019a
        L_0x0192:
            byte[] r9 = r11.toByteArray()     // Catch:{ IOException -> 0x0233 }
            javax.mail.internet.MimeBodyPart r3 = r1.createMimeBodyPart(r3, r9)     // Catch:{ IOException -> 0x0233 }
        L_0x019a:
            super.addBodyPart(r3)     // Catch:{ IOException -> 0x0233 }
            r11 = r10
            r9 = 9
            r10 = 32
            r12 = 1
            goto L_0x00a6
        L_0x01a5:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0233 }
            java.lang.String r3 = "missing multipart end boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x0233 }
            throw r0     // Catch:{ IOException -> 0x0233 }
        L_0x01ad:
            r10 = 13
            if (r9 == r10) goto L_0x01c2
            r10 = 10
            if (r9 != r10) goto L_0x01b6
            goto L_0x01c2
        L_0x01b6:
            if (r11 == 0) goto L_0x01bb
            r11.write(r9)     // Catch:{ IOException -> 0x0233 }
        L_0x01bb:
            r10 = 32
            r12 = 1
            r18 = 0
            goto L_0x00f5
        L_0x01c2:
            if (r0 == 0) goto L_0x01cc
            long r13 = r0.getPosition()     // Catch:{ IOException -> 0x0233 }
            r21 = 1
            long r13 = r13 - r21
        L_0x01cc:
            r10 = 13
            if (r9 != r10) goto L_0x01e4
            r10 = 1
            r2.mark(r10)     // Catch:{ IOException -> 0x0233 }
            int r10 = r2.read()     // Catch:{ IOException -> 0x0233 }
            r12 = 10
            if (r10 != r12) goto L_0x01e1
            r19 = r9
            r20 = r10
            goto L_0x01e6
        L_0x01e1:
            r2.reset()     // Catch:{ IOException -> 0x0233 }
        L_0x01e4:
            r19 = r9
        L_0x01e6:
            r10 = 32
            r12 = 1
            r18 = 1
            goto L_0x00f5
        L_0x01ed:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0233 }
            java.lang.String r3 = "Stream doesn't support mark"
            r0.<init>(r3)     // Catch:{ IOException -> 0x0233 }
            throw r0     // Catch:{ IOException -> 0x0233 }
        L_0x01f5:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0233 }
            java.lang.String r3 = "Missing start boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x0233 }
            throw r0     // Catch:{ IOException -> 0x0233 }
        L_0x01fd:
            int r3 = r8.length()     // Catch:{ IOException -> 0x0233 }
            if (r3 <= 0) goto L_0x005e
            if (r7 != 0) goto L_0x0211
            java.lang.String r3 = "line.separator"
            java.lang.String r7 = "\n"
            java.lang.String r3 = java.lang.System.getProperty(r3, r7)     // Catch:{ SecurityException -> 0x020e }
            goto L_0x0210
        L_0x020e:
            java.lang.String r3 = "\n"
        L_0x0210:
            r7 = r3
        L_0x0211:
            if (r6 != 0) goto L_0x021f
            java.lang.StringBuffer r3 = new java.lang.StringBuffer     // Catch:{ IOException -> 0x0233 }
            int r6 = r8.length()     // Catch:{ IOException -> 0x0233 }
            int r6 = r6 + 2
            r3.<init>(r6)     // Catch:{ IOException -> 0x0233 }
            r6 = r3
        L_0x021f:
            r6.append(r8)     // Catch:{ IOException -> 0x0233 }
            r6.append(r7)     // Catch:{ IOException -> 0x0233 }
            goto L_0x005e
        L_0x0227:
            int r13 = r13 + -1
            r9 = 9
            r10 = 32
            r11 = 0
            r12 = 1
            goto L_0x0070
        L_0x0231:
            r0 = move-exception
            goto L_0x023c
        L_0x0233:
            r0 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x0231 }
            java.lang.String r4 = "IO Error"
            r3.<init>(r4, r0)     // Catch:{ all -> 0x0231 }
            throw r3     // Catch:{ all -> 0x0231 }
        L_0x023c:
            r2.close()     // Catch:{ IOException -> 0x023f }
        L_0x023f:
            throw r0     // Catch:{ all -> 0x0251 }
        L_0x0240:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ all -> 0x0251 }
            java.lang.String r2 = "Missing boundary parameter"
            r0.<init>(r2)     // Catch:{ all -> 0x0251 }
            throw r0     // Catch:{ all -> 0x0251 }
        L_0x0248:
            r0 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0251 }
            java.lang.String r3 = "No inputstream from datasource"
            r2.<init>(r3, r0)     // Catch:{ all -> 0x0251 }
            throw r2     // Catch:{ all -> 0x0251 }
        L_0x0251:
            r0 = move-exception
            monitor-exit(r23)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.parse():void");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: int[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v5, resolved type: int[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v4, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v7, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v2, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v3, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v4, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r29v5, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v14, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v15, resolved type: int[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v19, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v20, resolved type: boolean} */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00cc, code lost:
        if (ignoreMissingEndBoundary == false) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00ce, code lost:
        r1.complete = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00de, code lost:
        throw new javax.mail.MessagingException("missing multipart end boundary");
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:211:0x033b */
    /* JADX WARNING: Missing exception handler attribute for start block: B:69:0x00d3 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x016b A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0177 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0185 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0190 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x01a2 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x01b2 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x0214 A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }, LOOP:8: B:129:0x01a4->B:155:0x0214, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x00ca A[EDGE_INSN: B:236:0x00ca->B:64:0x00ca ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x01b0 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x01ac A[EDGE_INSN: B:251:0x01ac->B:133:0x01ac ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x00df A[Catch:{ Exception -> 0x0344, IOException -> 0x032f, all -> 0x032d }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void parsebm() throws javax.mail.MessagingException {
        /*
            r31 = this;
            r1 = r31
            monitor-enter(r31)
            boolean r0 = r1.parsed     // Catch:{ all -> 0x034d }
            if (r0 == 0) goto L_0x0009
            monitor-exit(r31)
            return
        L_0x0009:
            javax.activation.DataSource r0 = r1.ds     // Catch:{ Exception -> 0x0344 }
            java.io.InputStream r0 = r0.getInputStream()     // Catch:{ Exception -> 0x0344 }
            boolean r2 = r0 instanceof java.io.ByteArrayInputStream     // Catch:{ Exception -> 0x0344 }
            if (r2 != 0) goto L_0x0021
            boolean r2 = r0 instanceof java.io.BufferedInputStream     // Catch:{ Exception -> 0x0344 }
            if (r2 != 0) goto L_0x0021
            boolean r2 = r0 instanceof javax.mail.internet.SharedInputStream     // Catch:{ Exception -> 0x0344 }
            if (r2 != 0) goto L_0x0021
            java.io.BufferedInputStream r2 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x0344 }
            r2.<init>(r0)     // Catch:{ Exception -> 0x0344 }
            goto L_0x0022
        L_0x0021:
            r2 = r0
        L_0x0022:
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ all -> 0x034d }
            if (r0 == 0) goto L_0x002a
            r0 = r2
            javax.mail.internet.SharedInputStream r0 = (javax.mail.internet.SharedInputStream) r0     // Catch:{ all -> 0x034d }
            goto L_0x002b
        L_0x002a:
            r0 = 0
        L_0x002b:
            javax.mail.internet.ContentType r4 = new javax.mail.internet.ContentType     // Catch:{ all -> 0x034d }
            java.lang.String r5 = r1.contentType     // Catch:{ all -> 0x034d }
            r4.<init>(r5)     // Catch:{ all -> 0x034d }
            java.lang.String r5 = "boundary"
            java.lang.String r4 = r4.getParameter(r5)     // Catch:{ all -> 0x034d }
            if (r4 == 0) goto L_0x0049
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x034d }
            java.lang.String r6 = "--"
            r5.<init>(r6)     // Catch:{ all -> 0x034d }
            r5.append(r4)     // Catch:{ all -> 0x034d }
            java.lang.String r4 = r5.toString()     // Catch:{ all -> 0x034d }
            goto L_0x004e
        L_0x0049:
            boolean r4 = ignoreMissingBoundaryParameter     // Catch:{ all -> 0x034d }
            if (r4 == 0) goto L_0x033c
            r4 = 0
        L_0x004e:
            com.sun.mail.util.LineInputStream r5 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x032f }
            r5.<init>(r2)     // Catch:{ IOException -> 0x032f }
            r6 = 0
            r7 = 0
        L_0x0055:
            java.lang.String r8 = r5.readLine()     // Catch:{ IOException -> 0x032f }
            r9 = 9
            r10 = 32
            r12 = 0
            r13 = 1
            if (r8 != 0) goto L_0x0062
            goto L_0x008e
        L_0x0062:
            int r14 = r8.length()     // Catch:{ IOException -> 0x032f }
            int r14 = r14 - r13
        L_0x0067:
            if (r14 >= 0) goto L_0x006a
            goto L_0x0072
        L_0x006a:
            char r15 = r8.charAt(r14)     // Catch:{ IOException -> 0x032f }
            if (r15 == r10) goto L_0x031c
            if (r15 == r9) goto L_0x031c
        L_0x0072:
            int r14 = r14 + 1
            java.lang.String r8 = r8.substring(r12, r14)     // Catch:{ IOException -> 0x032f }
            if (r4 == 0) goto L_0x0085
            boolean r14 = r8.equals(r4)     // Catch:{ IOException -> 0x032f }
            if (r14 == 0) goto L_0x0081
            goto L_0x008e
        L_0x0081:
            r26 = r5
            goto L_0x02f0
        L_0x0085:
            java.lang.String r14 = "--"
            boolean r14 = r8.startsWith(r14)     // Catch:{ IOException -> 0x032f }
            if (r14 == 0) goto L_0x0081
            r4 = r8
        L_0x008e:
            if (r8 == 0) goto L_0x02e8
            if (r6 == 0) goto L_0x0098
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x032f }
            r1.preamble = r6     // Catch:{ IOException -> 0x032f }
        L_0x0098:
            byte[] r4 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r4)     // Catch:{ IOException -> 0x032f }
            int r6 = r4.length     // Catch:{ IOException -> 0x032f }
            r7 = 256(0x100, float:3.59E-43)
            int[] r7 = new int[r7]     // Catch:{ IOException -> 0x032f }
            r8 = r12
        L_0x00a2:
            if (r8 < r6) goto L_0x02cf
            int[] r8 = new int[r6]     // Catch:{ IOException -> 0x032f }
            r14 = r6
        L_0x00a7:
            if (r14 > 0) goto L_0x029b
            int r14 = r6 + -1
            r8[r14] = r13     // Catch:{ IOException -> 0x032f }
            r15 = 0
            r19 = r12
            r17 = r15
        L_0x00b3:
            if (r19 == 0) goto L_0x00b6
            goto L_0x00d0
        L_0x00b6:
            if (r0 == 0) goto L_0x00e3
            long r15 = r0.getPosition()     // Catch:{ IOException -> 0x032f }
        L_0x00bc:
            java.lang.String r20 = r5.readLine()     // Catch:{ IOException -> 0x032f }
            if (r20 == 0) goto L_0x00c8
            int r21 = r20.length()     // Catch:{ IOException -> 0x032f }
            if (r21 > 0) goto L_0x00bc
        L_0x00c8:
            if (r20 != 0) goto L_0x00df
            boolean r0 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x032f }
            if (r0 == 0) goto L_0x00d7
            r1.complete = r12     // Catch:{ IOException -> 0x032f }
        L_0x00d0:
            r2.close()     // Catch:{ IOException -> 0x00d3 }
        L_0x00d3:
            r1.parsed = r13     // Catch:{ all -> 0x034d }
            monitor-exit(r31)
            return
        L_0x00d7:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x032f }
            java.lang.String r3 = "missing multipart end boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x032f }
            throw r0     // Catch:{ IOException -> 0x032f }
        L_0x00df:
            r22 = r15
            r15 = 0
            goto L_0x00eb
        L_0x00e3:
            javax.mail.internet.InternetHeaders r20 = r1.createInternetHeaders(r2)     // Catch:{ IOException -> 0x032f }
            r22 = r15
            r15 = r20
        L_0x00eb:
            boolean r16 = r2.markSupported()     // Catch:{ IOException -> 0x032f }
            if (r16 == 0) goto L_0x0293
            if (r0 != 0) goto L_0x00fb
            java.io.ByteArrayOutputStream r16 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x032f }
            r16.<init>()     // Catch:{ IOException -> 0x032f }
            r3 = r16
            goto L_0x0100
        L_0x00fb:
            long r17 = r0.getPosition()     // Catch:{ IOException -> 0x032f }
            r3 = 0
        L_0x0100:
            byte[] r9 = new byte[r6]     // Catch:{ IOException -> 0x032f }
            byte[] r10 = new byte[r6]     // Catch:{ IOException -> 0x032f }
            r24 = r13
            r13 = r12
        L_0x0107:
            int r11 = r6 + 4
            int r11 = r11 + 1000
            r2.mark(r11)     // Catch:{ IOException -> 0x032f }
            int r11 = readFully(r2, r9, r12, r6)     // Catch:{ IOException -> 0x032f }
            if (r11 >= r6) goto L_0x0135
            boolean r19 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x032f }
            if (r19 == 0) goto L_0x012d
            if (r0 == 0) goto L_0x011e
            long r17 = r0.getPosition()     // Catch:{ IOException -> 0x032f }
        L_0x011e:
            r1.complete = r12     // Catch:{ IOException -> 0x032f }
            r26 = r5
            r28 = r8
            r29 = r12
            r19 = 1
            r12 = r7
        L_0x0129:
            r7 = r17
            goto L_0x01c5
        L_0x012d:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x032f }
            java.lang.String r3 = "missing multipart end boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x032f }
            throw r0     // Catch:{ IOException -> 0x032f }
        L_0x0135:
            r25 = r14
        L_0x0137:
            if (r25 >= 0) goto L_0x013c
            r26 = r5
            goto L_0x0144
        L_0x013c:
            byte r12 = r9[r25]     // Catch:{ IOException -> 0x032f }
            r26 = r5
            byte r5 = r4[r25]     // Catch:{ IOException -> 0x032f }
            if (r12 == r5) goto L_0x0286
        L_0x0144:
            if (r25 >= 0) goto L_0x0221
            r5 = 13
            if (r24 != 0) goto L_0x0168
            int r25 = r13 + -1
            byte r12 = r10[r25]     // Catch:{ IOException -> 0x032f }
            if (r12 == r5) goto L_0x0155
            r5 = 10
            if (r12 != r5) goto L_0x0168
            goto L_0x0157
        L_0x0155:
            r5 = 10
        L_0x0157:
            if (r12 != r5) goto L_0x0166
            r5 = 2
            if (r13 < r5) goto L_0x0166
            int r5 = r13 + -2
            byte r5 = r10[r5]     // Catch:{ IOException -> 0x032f }
            r12 = 13
            if (r5 != r12) goto L_0x0166
            r5 = 2
            goto L_0x0169
        L_0x0166:
            r5 = 1
            goto L_0x0169
        L_0x0168:
            r5 = 0
        L_0x0169:
            if (r24 != 0) goto L_0x0175
            if (r5 <= 0) goto L_0x016e
            goto L_0x0175
        L_0x016e:
            r27 = r4
            r12 = r7
            r28 = r8
            goto L_0x0211
        L_0x0175:
            if (r0 == 0) goto L_0x0185
            long r17 = r0.getPosition()     // Catch:{ IOException -> 0x032f }
            r12 = r7
            r28 = r8
            long r7 = (long) r6     // Catch:{ IOException -> 0x032f }
            long r17 = r17 - r7
            long r7 = (long) r5     // Catch:{ IOException -> 0x032f }
            long r17 = r17 - r7
            goto L_0x0188
        L_0x0185:
            r12 = r7
            r28 = r8
        L_0x0188:
            int r7 = r2.read()     // Catch:{ IOException -> 0x032f }
            r8 = 45
            if (r7 != r8) goto L_0x01a2
            int r8 = r2.read()     // Catch:{ IOException -> 0x032f }
            r29 = r5
            r5 = 45
            if (r8 != r5) goto L_0x01a4
            r5 = 1
            r1.complete = r5     // Catch:{ IOException -> 0x032f }
            r7 = r17
            r19 = 1
            goto L_0x01c5
        L_0x01a2:
            r29 = r5
        L_0x01a4:
            r5 = 32
            if (r7 == r5) goto L_0x0214
            r8 = 9
            if (r7 == r8) goto L_0x0214
            r5 = 10
            if (r7 != r5) goto L_0x01b2
            goto L_0x0129
        L_0x01b2:
            r8 = 13
            if (r7 != r8) goto L_0x020f
            r7 = 1
            r2.mark(r7)     // Catch:{ IOException -> 0x032f }
            int r7 = r2.read()     // Catch:{ IOException -> 0x032f }
            if (r7 == r5) goto L_0x0129
            r2.reset()     // Catch:{ IOException -> 0x032f }
            goto L_0x0129
        L_0x01c5:
            if (r0 == 0) goto L_0x01d6
            r27 = r4
            r4 = r22
            java.io.InputStream r3 = r0.newStream(r4, r7)     // Catch:{ IOException -> 0x032f }
            javax.mail.internet.MimeBodyPart r3 = r1.createMimeBodyPart(r3)     // Catch:{ IOException -> 0x032f }
            r22 = r4
            goto L_0x01f9
        L_0x01d6:
            r27 = r4
            r4 = r22
            int r13 = r13 - r29
            if (r13 <= 0) goto L_0x01e5
            r22 = r4
            r4 = 0
            r3.write(r10, r4, r13)     // Catch:{ IOException -> 0x032f }
            goto L_0x01e7
        L_0x01e5:
            r22 = r4
        L_0x01e7:
            boolean r4 = r1.complete     // Catch:{ IOException -> 0x032f }
            if (r4 != 0) goto L_0x01f1
            if (r11 <= 0) goto L_0x01f1
            r4 = 0
            r3.write(r9, r4, r11)     // Catch:{ IOException -> 0x032f }
        L_0x01f1:
            byte[] r3 = r3.toByteArray()     // Catch:{ IOException -> 0x032f }
            javax.mail.internet.MimeBodyPart r3 = r1.createMimeBodyPart(r15, r3)     // Catch:{ IOException -> 0x032f }
        L_0x01f9:
            super.addBodyPart(r3)     // Catch:{ IOException -> 0x032f }
            r17 = r7
            r7 = r12
            r15 = r22
            r5 = r26
            r4 = r27
            r8 = r28
            r9 = 9
            r10 = 32
            r12 = 0
            r13 = 1
            goto L_0x00b3
        L_0x020f:
            r27 = r4
        L_0x0211:
            r25 = 0
            goto L_0x0226
        L_0x0214:
            r27 = r4
            r4 = 10
            r8 = 13
            int r7 = r2.read()     // Catch:{ IOException -> 0x032f }
            r4 = r27
            goto L_0x01a4
        L_0x0221:
            r27 = r4
            r12 = r7
            r28 = r8
        L_0x0226:
            int r4 = r25 + 1
            byte r5 = r9[r25]     // Catch:{ IOException -> 0x032f }
            r5 = r5 & 127(0x7f, float:1.78E-43)
            r5 = r12[r5]     // Catch:{ IOException -> 0x032f }
            int r4 = r4 - r5
            r5 = r28[r25]     // Catch:{ IOException -> 0x032f }
            int r4 = java.lang.Math.max(r4, r5)     // Catch:{ IOException -> 0x032f }
            r5 = 2
            if (r4 >= r5) goto L_0x0262
            if (r0 != 0) goto L_0x0243
            r4 = 1
            if (r13 <= r4) goto L_0x0243
            int r4 = r13 + -1
            r5 = 0
            r3.write(r10, r5, r4)     // Catch:{ IOException -> 0x032f }
        L_0x0243:
            r2.reset()     // Catch:{ IOException -> 0x032f }
            r4 = 1
            r1.skipFully(r2, r4)     // Catch:{ IOException -> 0x032f }
            r5 = 1
            if (r13 < r5) goto L_0x025b
            int r13 = r13 + -1
            byte r4 = r10[r13]     // Catch:{ IOException -> 0x032f }
            r7 = 0
            r10[r7] = r4     // Catch:{ IOException -> 0x032f }
            byte r4 = r9[r7]     // Catch:{ IOException -> 0x032f }
            r10[r5] = r4     // Catch:{ IOException -> 0x032f }
            r13 = 2
            goto L_0x027a
        L_0x025b:
            r4 = 0
            byte r7 = r9[r4]     // Catch:{ IOException -> 0x032f }
            r10[r4] = r7     // Catch:{ IOException -> 0x032f }
            r13 = r5
            goto L_0x027a
        L_0x0262:
            r5 = 1
            if (r13 <= 0) goto L_0x026c
            if (r0 != 0) goto L_0x026c
            r7 = 0
            r3.write(r10, r7, r13)     // Catch:{ IOException -> 0x032f }
            goto L_0x026d
        L_0x026c:
            r7 = 0
        L_0x026d:
            r2.reset()     // Catch:{ IOException -> 0x032f }
            long r7 = (long) r4     // Catch:{ IOException -> 0x032f }
            r1.skipFully(r2, r7)     // Catch:{ IOException -> 0x032f }
            r13 = r4
            r30 = r10
            r10 = r9
            r9 = r30
        L_0x027a:
            r7 = r12
            r5 = r26
            r4 = r27
            r8 = r28
            r12 = 0
            r24 = 0
            goto L_0x0107
        L_0x0286:
            r27 = r4
            r12 = r7
            r28 = r8
            r5 = 1
            int r25 = r25 + -1
            r5 = r26
            r12 = 0
            goto L_0x0137
        L_0x0293:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x032f }
            java.lang.String r3 = "Stream doesn't support mark"
            r0.<init>(r3)     // Catch:{ IOException -> 0x032f }
            throw r0     // Catch:{ IOException -> 0x032f }
        L_0x029b:
            r27 = r4
            r26 = r5
            r12 = r7
            r28 = r8
            r5 = r13
            int r3 = r6 + -1
        L_0x02a5:
            if (r3 >= r14) goto L_0x02af
        L_0x02a7:
            if (r3 > 0) goto L_0x02aa
            goto L_0x02be
        L_0x02aa:
            int r3 = r3 + -1
            r28[r3] = r14     // Catch:{ IOException -> 0x032f }
            goto L_0x02a7
        L_0x02af:
            byte r4 = r27[r3]     // Catch:{ IOException -> 0x032f }
            int r7 = r3 - r14
            byte r7 = r27[r7]     // Catch:{ IOException -> 0x032f }
            if (r4 != r7) goto L_0x02be
            int r4 = r3 + -1
            r28[r4] = r14     // Catch:{ IOException -> 0x032f }
            int r3 = r3 + -1
            goto L_0x02a5
        L_0x02be:
            int r14 = r14 + -1
            r13 = r5
            r7 = r12
            r5 = r26
            r4 = r27
            r8 = r28
            r9 = 9
            r10 = 32
            r12 = 0
            goto L_0x00a7
        L_0x02cf:
            r27 = r4
            r26 = r5
            r12 = r7
            r3 = r9
            r5 = r13
            byte r4 = r27[r8]     // Catch:{ IOException -> 0x032f }
            int r8 = r8 + 1
            r12[r4] = r8     // Catch:{ IOException -> 0x032f }
            r9 = r3
            r13 = r5
            r7 = r12
            r5 = r26
            r4 = r27
            r10 = 32
            r12 = 0
            goto L_0x00a2
        L_0x02e8:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x032f }
            java.lang.String r3 = "Missing start boundary"
            r0.<init>(r3)     // Catch:{ IOException -> 0x032f }
            throw r0     // Catch:{ IOException -> 0x032f }
        L_0x02f0:
            int r3 = r8.length()     // Catch:{ IOException -> 0x032f }
            if (r3 <= 0) goto L_0x0318
            if (r7 != 0) goto L_0x0304
            java.lang.String r3 = "line.separator"
            java.lang.String r5 = "\n"
            java.lang.String r3 = java.lang.System.getProperty(r3, r5)     // Catch:{ SecurityException -> 0x0301 }
            goto L_0x0303
        L_0x0301:
            java.lang.String r3 = "\n"
        L_0x0303:
            r7 = r3
        L_0x0304:
            if (r6 != 0) goto L_0x0312
            java.lang.StringBuffer r3 = new java.lang.StringBuffer     // Catch:{ IOException -> 0x032f }
            int r5 = r8.length()     // Catch:{ IOException -> 0x032f }
            r9 = 2
            int r5 = r5 + r9
            r3.<init>(r5)     // Catch:{ IOException -> 0x032f }
            r6 = r3
        L_0x0312:
            r6.append(r8)     // Catch:{ IOException -> 0x032f }
            r6.append(r7)     // Catch:{ IOException -> 0x032f }
        L_0x0318:
            r5 = r26
            goto L_0x0055
        L_0x031c:
            r26 = r5
            r3 = r9
            r10 = r12
            r5 = r13
            r9 = 2
            int r14 = r14 + -1
            r9 = r3
            r13 = r5
            r12 = r10
            r5 = r26
            r10 = 32
            goto L_0x0067
        L_0x032d:
            r0 = move-exception
            goto L_0x0338
        L_0x032f:
            r0 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x032d }
            java.lang.String r4 = "IO Error"
            r3.<init>(r4, r0)     // Catch:{ all -> 0x032d }
            throw r3     // Catch:{ all -> 0x032d }
        L_0x0338:
            r2.close()     // Catch:{ IOException -> 0x033b }
        L_0x033b:
            throw r0     // Catch:{ all -> 0x034d }
        L_0x033c:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ all -> 0x034d }
            java.lang.String r2 = "Missing boundary parameter"
            r0.<init>(r2)     // Catch:{ all -> 0x034d }
            throw r0     // Catch:{ all -> 0x034d }
        L_0x0344:
            r0 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x034d }
            java.lang.String r3 = "No inputstream from datasource"
            r2.<init>(r3, r0)     // Catch:{ all -> 0x034d }
            throw r2     // Catch:{ all -> 0x034d }
        L_0x034d:
            r0 = move-exception
            monitor-exit(r31)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.parsebm():void");
    }

    private static int readFully(InputStream inputStream, byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        if (i2 == 0) {
            return 0;
        }
        while (i2 > 0) {
            int read = inputStream.read(bArr, i, i2);
            if (read <= 0) {
                break;
            }
            i += read;
            i3 += read;
            i2 -= read;
        }
        if (i3 > 0) {
            return i3;
        }
        return -1;
    }

    private void skipFully(InputStream inputStream, long j) throws IOException {
        while (j > 0) {
            long skip = inputStream.skip(j);
            if (skip > 0) {
                j -= skip;
            } else {
                throw new EOFException("can't skip");
            }
        }
    }

    /* access modifiers changed from: protected */
    public InternetHeaders createInternetHeaders(InputStream inputStream) throws MessagingException {
        return new InternetHeaders(inputStream);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InternetHeaders internetHeaders, byte[] bArr) throws MessagingException {
        return new MimeBodyPart(internetHeaders, bArr);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InputStream inputStream) throws MessagingException {
        return new MimeBodyPart(inputStream);
    }
}

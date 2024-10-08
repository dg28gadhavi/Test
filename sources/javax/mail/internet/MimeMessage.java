package javax.mail.internet;

import com.sec.internal.constants.ims.MIMEContentType;
import java.io.InputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.util.SharedByteArrayInputStream;

public class MimeMessage extends Message implements MimePart {
    private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);
    private static MailDateFormat mailDateFormat = new MailDateFormat();
    protected byte[] content;
    protected InputStream contentStream;
    protected DataHandler dh;
    protected InternetHeaders headers;

    public String getContentType() throws MessagingException {
        String header = getHeader("Content-Type", (String) null);
        return header == null ? MIMEContentType.PLAIN_TEXT : header;
    }

    public String getEncoding() throws MessagingException {
        return MimeBodyPart.getEncoding(this);
    }

    /* access modifiers changed from: protected */
    public InputStream getContentStream() throws MessagingException {
        InputStream inputStream = this.contentStream;
        if (inputStream != null) {
            return ((SharedInputStream) inputStream).newStream(0, -1);
        }
        if (this.content != null) {
            return new SharedByteArrayInputStream(this.content);
        }
        throw new MessagingException("No content");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            this.dh = new DataHandler(new MimePartDataSource(this));
        }
        return this.dh;
    }

    public String getHeader(String str, String str2) throws MessagingException {
        return this.headers.getHeader(str, str2);
    }

    public Enumeration getNonMatchingHeaderLines(String[] strArr) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(strArr);
    }
}

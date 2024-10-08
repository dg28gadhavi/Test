package javax.mail.internet;

import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.httpclient.HttpPostBody;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

public class MimePartDataSource implements DataSource, MessageAware {
    private static boolean ignoreMultipartEncoding = true;
    private MessageContext context;
    protected MimePart part;

    static {
        try {
            String property = System.getProperty("mail.mime.ignoremultipartencoding");
            ignoreMultipartEncoding = property == null || !property.equalsIgnoreCase(ConfigConstants.VALUE.INFO_COMPLETED);
        } catch (SecurityException unused) {
        }
    }

    public MimePartDataSource(MimePart mimePart) {
        this.part = mimePart;
    }

    public InputStream getInputStream() throws IOException {
        InputStream inputStream;
        try {
            MimePart mimePart = this.part;
            if (mimePart instanceof MimeBodyPart) {
                inputStream = ((MimeBodyPart) mimePart).getContentStream();
            } else if (mimePart instanceof MimeMessage) {
                inputStream = ((MimeMessage) mimePart).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            String restrictEncoding = restrictEncoding(this.part.getEncoding(), this.part);
            return restrictEncoding != null ? MimeUtility.decode(inputStream, restrictEncoding) : inputStream;
        } catch (MessagingException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static String restrictEncoding(String str, MimePart mimePart) throws MessagingException {
        String contentType;
        if (!ignoreMultipartEncoding || str == null || str.equalsIgnoreCase("7bit") || str.equalsIgnoreCase("8bit") || str.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BINARY) || (contentType = mimePart.getContentType()) == null) {
            return str;
        }
        try {
            ContentType contentType2 = new ContentType(contentType);
            if (contentType2.match("multipart/*") || contentType2.match("message/*")) {
                return null;
            }
            return str;
        } catch (ParseException unused) {
            return str;
        }
    }

    public String getContentType() {
        try {
            return this.part.getContentType();
        } catch (MessagingException unused) {
            return HttpPostBody.CONTENT_TYPE_DEFAULT;
        }
    }

    public synchronized MessageContext getMessageContext() {
        if (this.context == null) {
            this.context = new MessageContext(this.part);
        }
        return this.context;
    }
}

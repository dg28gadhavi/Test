package javax.mail.internet;

import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Part;

public interface MimePart extends Part {
    String getEncoding() throws MessagingException;

    String getHeader(String str, String str2) throws MessagingException;

    Enumeration getNonMatchingHeaderLines(String[] strArr) throws MessagingException;
}

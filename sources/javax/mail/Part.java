package javax.mail;

import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;

public interface Part {
    String getContentType() throws MessagingException;

    DataHandler getDataHandler() throws MessagingException;

    String[] getHeader(String str) throws MessagingException;

    InputStream getInputStream() throws IOException, MessagingException;

    int getSize() throws MessagingException;
}

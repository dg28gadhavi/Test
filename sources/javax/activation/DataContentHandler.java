package javax.activation;

import java.io.IOException;
import java.io.OutputStream;

public interface DataContentHandler {
    void writeTo(Object obj, String str, OutputStream outputStream) throws IOException;
}

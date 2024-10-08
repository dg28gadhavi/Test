package javax.activation;

import java.io.IOException;
import java.io.InputStream;

public interface DataSource {
    String getContentType();

    InputStream getInputStream() throws IOException;
}

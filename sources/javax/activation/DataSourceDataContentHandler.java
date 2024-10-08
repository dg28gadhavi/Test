package javax.activation;

import java.io.IOException;
import java.io.OutputStream;
import myjava.awt.datatransfer.DataFlavor;

/* compiled from: DataHandler */
class DataSourceDataContentHandler implements DataContentHandler {
    private DataContentHandler dch;
    private DataSource ds;
    private DataFlavor[] transferFlavors = null;

    public DataSourceDataContentHandler(DataContentHandler dataContentHandler, DataSource dataSource) {
        this.ds = dataSource;
        this.dch = dataContentHandler;
    }

    public void writeTo(Object obj, String str, OutputStream outputStream) throws IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            dataContentHandler.writeTo(obj, str, outputStream);
            return;
        }
        throw new UnsupportedDataTypeException("no DCH for content type " + this.ds.getContentType());
    }
}

package javax.activation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import myjava.awt.datatransfer.DataFlavor;

/* compiled from: DataHandler */
class ObjectDataContentHandler implements DataContentHandler {
    private DataContentHandler dch;
    private String mimeType;
    private Object obj;
    private DataFlavor[] transferFlavors = null;

    public ObjectDataContentHandler(DataContentHandler dataContentHandler, Object obj2, String str) {
        this.obj = obj2;
        this.mimeType = str;
        this.dch = dataContentHandler;
    }

    public DataContentHandler getDCH() {
        return this.dch;
    }

    public void writeTo(Object obj2, String str, OutputStream outputStream) throws IOException {
        DataContentHandler dataContentHandler = this.dch;
        if (dataContentHandler != null) {
            dataContentHandler.writeTo(obj2, str, outputStream);
        } else if (obj2 instanceof byte[]) {
            outputStream.write((byte[]) obj2);
        } else if (obj2 instanceof String) {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write((String) obj2);
            outputStreamWriter.flush();
        } else {
            throw new UnsupportedDataTypeException("no object DCH for MIME type " + this.mimeType);
        }
    }
}

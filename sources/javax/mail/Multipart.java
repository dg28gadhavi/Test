package javax.mail;

import java.util.Vector;

public abstract class Multipart {
    protected String contentType = "multipart/mixed";
    protected Part parent;
    protected Vector parts = new Vector();

    protected Multipart() {
    }

    /* access modifiers changed from: protected */
    public synchronized void setMultipartDataSource(MultipartDataSource multipartDataSource) throws MessagingException {
        this.contentType = multipartDataSource.getContentType();
        int count = multipartDataSource.getCount();
        for (int i = 0; i < count; i++) {
            addBodyPart(multipartDataSource.getBodyPart(i));
        }
    }

    public synchronized int getCount() throws MessagingException {
        Vector vector = this.parts;
        if (vector == null) {
            return 0;
        }
        return vector.size();
    }

    public synchronized BodyPart getBodyPart(int i) throws MessagingException {
        Vector vector;
        vector = this.parts;
        if (vector != null) {
        } else {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        return (BodyPart) vector.elementAt(i);
    }

    public synchronized void addBodyPart(BodyPart bodyPart) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        this.parts.addElement(bodyPart);
        bodyPart.setParent(this);
    }

    public synchronized void setParent(Part part) {
        this.parent = part;
    }
}

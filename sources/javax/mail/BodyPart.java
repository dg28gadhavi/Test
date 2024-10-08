package javax.mail;

public abstract class BodyPart implements Part {
    protected Multipart parent;

    /* access modifiers changed from: package-private */
    public void setParent(Multipart multipart) {
        this.parent = multipart;
    }
}

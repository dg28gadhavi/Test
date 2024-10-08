package javax.mail;

public class MessagingException extends Exception {
    private static final long serialVersionUID = -7569192289819959253L;
    private Exception next;

    public MessagingException() {
        initCause((Throwable) null);
    }

    public MessagingException(String str) {
        super(str);
        initCause((Throwable) null);
    }

    public MessagingException(String str, Exception exc) {
        super(str);
        this.next = exc;
        initCause((Throwable) null);
    }

    public synchronized Throwable getCause() {
        return this.next;
    }

    public synchronized String toString() {
        String exc = super.toString();
        Exception exc2 = this.next;
        if (exc2 == null) {
            return exc;
        }
        if (exc == null) {
            exc = "";
        }
        StringBuffer stringBuffer = new StringBuffer(exc);
        while (exc2 != null) {
            stringBuffer.append(";\n  nested exception is:\n\t");
            if (exc2 instanceof MessagingException) {
                MessagingException messagingException = (MessagingException) exc2;
                stringBuffer.append(messagingException.superToString());
                exc2 = messagingException.next;
            } else {
                stringBuffer.append(exc2.toString());
                exc2 = null;
            }
        }
        return stringBuffer.toString();
    }

    private final String superToString() {
        return super.toString();
    }
}

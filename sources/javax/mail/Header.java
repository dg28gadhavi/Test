package javax.mail;

public class Header {
    protected String name;
    protected String value;

    public Header(String str, String str2) {
        this.name = str;
        this.value = str2;
    }

    public String getName() {
        return this.name;
    }
}

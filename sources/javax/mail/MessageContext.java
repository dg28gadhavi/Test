package javax.mail;

public class MessageContext {
    private Part part;

    public MessageContext(Part part2) {
        this.part = part2;
    }

    public Part getPart() {
        return this.part;
    }
}

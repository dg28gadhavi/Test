package javax.activation;

public abstract class CommandMap {
    private static CommandMap defaultCommandMap;

    public abstract DataContentHandler createDataContentHandler(String str);

    public static CommandMap getDefaultCommandMap() {
        if (defaultCommandMap == null) {
            defaultCommandMap = new MailcapCommandMap();
        }
        return defaultCommandMap;
    }

    public DataContentHandler createDataContentHandler(String str, DataSource dataSource) {
        return createDataContentHandler(str);
    }
}

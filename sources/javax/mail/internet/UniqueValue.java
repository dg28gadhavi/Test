package javax.mail.internet;

class UniqueValue {
    private static int id;

    public static String getUniqueBoundaryValue() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("----=_Part_");
        stringBuffer.append(getUniqueId());
        stringBuffer.append("_");
        stringBuffer.append(stringBuffer.hashCode());
        stringBuffer.append('.');
        stringBuffer.append(System.currentTimeMillis());
        return stringBuffer.toString();
    }

    private static synchronized int getUniqueId() {
        int i;
        synchronized (UniqueValue.class) {
            i = id;
            id = i + 1;
        }
        return i;
    }
}

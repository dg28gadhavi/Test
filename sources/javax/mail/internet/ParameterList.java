package javax.mail.internet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ParameterList {
    private static boolean applehack = false;
    private static boolean decodeParameters = false;
    private static boolean decodeParametersStrict = false;
    private static boolean encodeParameters = false;
    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private String lastName;
    private Map list;
    private Set multisegmentNames;
    private Map slist;

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0026 A[Catch:{ SecurityException -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0028 A[Catch:{ SecurityException -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0039 A[Catch:{ SecurityException -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003b A[Catch:{ SecurityException -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004c A[Catch:{ SecurityException -> 0x0050 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x004d A[Catch:{ SecurityException -> 0x0050 }] */
    static {
        /*
            java.lang.String r0 = "mail.mime.encodeparameters"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0050 }
            r1 = 1
            java.lang.String r2 = "true"
            r3 = 0
            if (r0 == 0) goto L_0x0015
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x0015
            r0 = r1
            goto L_0x0016
        L_0x0015:
            r0 = r3
        L_0x0016:
            encodeParameters = r0     // Catch:{ SecurityException -> 0x0050 }
            java.lang.String r0 = "mail.mime.decodeparameters"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x0028
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x0028
            r0 = r1
            goto L_0x0029
        L_0x0028:
            r0 = r3
        L_0x0029:
            decodeParameters = r0     // Catch:{ SecurityException -> 0x0050 }
            java.lang.String r0 = "mail.mime.decodeparameters.strict"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x003b
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x003b
            r0 = r1
            goto L_0x003c
        L_0x003b:
            r0 = r3
        L_0x003c:
            decodeParametersStrict = r0     // Catch:{ SecurityException -> 0x0050 }
            java.lang.String r0 = "mail.mime.applefilenames"
            java.lang.String r0 = java.lang.System.getProperty(r0)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x004d
            boolean r0 = r0.equalsIgnoreCase(r2)     // Catch:{ SecurityException -> 0x0050 }
            if (r0 == 0) goto L_0x004d
            goto L_0x004e
        L_0x004d:
            r1 = r3
        L_0x004e:
            applehack = r1     // Catch:{ SecurityException -> 0x0050 }
        L_0x0050:
            r0 = 16
            char[] r0 = new char[r0]
            r0 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70} // fill-array
            hex = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<clinit>():void");
    }

    private static class Value {
        String charset;
        String encodedValue;
        String value;

        private Value() {
        }

        /* synthetic */ Value(Value value2) {
            this();
        }
    }

    private static class MultiValue extends ArrayList {
        String value;

        private MultiValue() {
        }

        /* synthetic */ MultiValue(MultiValue multiValue) {
            this();
        }
    }

    public ParameterList() {
        this.list = new LinkedHashMap();
        this.lastName = null;
        if (decodeParameters) {
            this.multisegmentNames = new HashSet();
            this.slist = new HashMap();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0124, code lost:
        throw new javax.mail.internet.ParseException("Expected ';', got \"" + r8.getValue() + com.sec.internal.ims.core.cmc.CmcConstants.E_NUM_STR_QUOTE);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParameterList(java.lang.String r8) throws javax.mail.internet.ParseException {
        /*
            r7 = this;
            r7.<init>()
            javax.mail.internet.HeaderTokenizer r0 = new javax.mail.internet.HeaderTokenizer
            java.lang.String r1 = "()<>@,;:\\\"\t []/?="
            r0.<init>(r8, r1)
        L_0x000a:
            javax.mail.internet.HeaderTokenizer$Token r8 = r0.next()
            int r1 = r8.getType()
            r2 = -4
            if (r1 != r2) goto L_0x0016
            goto L_0x0028
        L_0x0016:
            char r3 = (char) r1
            r4 = 59
            r5 = -1
            java.lang.String r6 = "\""
            if (r3 != r4) goto L_0x00c2
            javax.mail.internet.HeaderTokenizer$Token r8 = r0.next()
            int r1 = r8.getType()
            if (r1 != r2) goto L_0x0031
        L_0x0028:
            boolean r8 = decodeParameters
            if (r8 == 0) goto L_0x0030
            r8 = 0
            r7.combineMultisegmentNames(r8)
        L_0x0030:
            return
        L_0x0031:
            int r1 = r8.getType()
            if (r1 != r5) goto L_0x00a7
            java.lang.String r8 = r8.getValue()
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.String r8 = r8.toLowerCase(r1)
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r2 = r1.getType()
            char r2 = (char) r2
            r3 = 61
            if (r2 != r3) goto L_0x008c
            javax.mail.internet.HeaderTokenizer$Token r1 = r0.next()
            int r2 = r1.getType()
            if (r2 == r5) goto L_0x0077
            r3 = -2
            if (r2 != r3) goto L_0x005c
            goto L_0x0077
        L_0x005c:
            javax.mail.internet.ParseException r7 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            java.lang.String r0 = "Expected parameter value, got \""
            r8.<init>(r0)
            java.lang.String r0 = r1.getValue()
            r8.append(r0)
            r8.append(r6)
            java.lang.String r8 = r8.toString()
            r7.<init>(r8)
            throw r7
        L_0x0077:
            java.lang.String r1 = r1.getValue()
            r7.lastName = r8
            boolean r2 = decodeParameters
            if (r2 == 0) goto L_0x0085
            r7.putEncodedName(r8, r1)
            goto L_0x000a
        L_0x0085:
            java.util.Map r2 = r7.list
            r2.put(r8, r1)
            goto L_0x000a
        L_0x008c:
            javax.mail.internet.ParseException r7 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            java.lang.String r0 = "Expected '=', got \""
            r8.<init>(r0)
            java.lang.String r0 = r1.getValue()
            r8.append(r0)
            r8.append(r6)
            java.lang.String r8 = r8.toString()
            r7.<init>(r8)
            throw r7
        L_0x00a7:
            javax.mail.internet.ParseException r7 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "Expected parameter name, got \""
            r0.<init>(r1)
            java.lang.String r8 = r8.getValue()
            r0.append(r8)
            r0.append(r6)
            java.lang.String r8 = r0.toString()
            r7.<init>(r8)
            throw r7
        L_0x00c2:
            boolean r2 = applehack
            if (r2 == 0) goto L_0x010a
            if (r1 != r5) goto L_0x010a
            java.lang.String r1 = r7.lastName
            if (r1 == 0) goto L_0x010a
            java.lang.String r2 = "name"
            boolean r1 = r1.equals(r2)
            if (r1 != 0) goto L_0x00de
            java.lang.String r1 = r7.lastName
            java.lang.String r2 = "filename"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x010a
        L_0x00de:
            java.util.Map r1 = r7.list
            java.lang.String r2 = r7.lastName
            java.lang.Object r1 = r1.get(r2)
            java.lang.String r1 = (java.lang.String) r1
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r1 = java.lang.String.valueOf(r1)
            r2.<init>(r1)
            java.lang.String r1 = " "
            r2.append(r1)
            java.lang.String r8 = r8.getValue()
            r2.append(r8)
            java.lang.String r8 = r2.toString()
            java.util.Map r1 = r7.list
            java.lang.String r2 = r7.lastName
            r1.put(r2, r8)
            goto L_0x000a
        L_0x010a:
            javax.mail.internet.ParseException r7 = new javax.mail.internet.ParseException
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "Expected ';', got \""
            r0.<init>(r1)
            java.lang.String r8 = r8.getValue()
            r0.append(r8)
            r0.append(r6)
            java.lang.String r8 = r0.toString()
            r7.<init>(r8)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.<init>(java.lang.String):void");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: javax.mail.internet.ParameterList$Value} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v4, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void putEncodedName(java.lang.String r5, java.lang.String r6) throws javax.mail.internet.ParseException {
        /*
            r4 = this;
            r0 = 42
            int r0 = r5.indexOf(r0)
            if (r0 >= 0) goto L_0x000e
            java.util.Map r4 = r4.list
            r4.put(r5, r6)
            goto L_0x0057
        L_0x000e:
            int r1 = r5.length()
            int r1 = r1 + -1
            r2 = 0
            if (r0 != r1) goto L_0x0025
            java.lang.String r5 = r5.substring(r2, r0)
            java.util.Map r4 = r4.list
            javax.mail.internet.ParameterList$Value r6 = decodeValue(r6)
            r4.put(r5, r6)
            goto L_0x0057
        L_0x0025:
            java.lang.String r0 = r5.substring(r2, r0)
            java.util.Set r1 = r4.multisegmentNames
            r1.add(r0)
            java.util.Map r1 = r4.list
            java.lang.String r3 = ""
            r1.put(r0, r3)
            java.lang.String r0 = "*"
            boolean r0 = r5.endsWith(r0)
            if (r0 == 0) goto L_0x0052
            javax.mail.internet.ParameterList$Value r0 = new javax.mail.internet.ParameterList$Value
            r1 = 0
            r0.<init>(r1)
            r0.encodedValue = r6
            r0.value = r6
            int r6 = r5.length()
            int r6 = r6 + -1
            java.lang.String r5 = r5.substring(r2, r6)
            r6 = r0
        L_0x0052:
            java.util.Map r4 = r4.slist
            r4.put(r5, r6)
        L_0x0057:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.putEncodedName(java.lang.String, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d7 A[Catch:{ NumberFormatException -> 0x00f5, UnsupportedEncodingException -> 0x00e2, StringIndexOutOfBoundsException -> 0x00cf, all -> 0x011a }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ea A[Catch:{ NumberFormatException -> 0x00f5, UnsupportedEncodingException -> 0x00e2, StringIndexOutOfBoundsException -> 0x00cf, all -> 0x011a }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x00d8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x00eb A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void combineMultisegmentNames(boolean r13) throws javax.mail.internet.ParseException {
        /*
            r12 = this;
            java.util.Set r0 = r12.multisegmentNames     // Catch:{ all -> 0x011a }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x011a }
        L_0x0006:
            boolean r1 = r0.hasNext()     // Catch:{ all -> 0x011a }
            if (r1 != 0) goto L_0x0050
            java.util.Map r13 = r12.slist
            int r13 = r13.size()
            if (r13 <= 0) goto L_0x0045
            java.util.Map r13 = r12.slist
            java.util.Collection r13 = r13.values()
            java.util.Iterator r13 = r13.iterator()
        L_0x001e:
            boolean r0 = r13.hasNext()
            if (r0 != 0) goto L_0x002c
            java.util.Map r13 = r12.list
            java.util.Map r0 = r12.slist
            r13.putAll(r0)
            goto L_0x0045
        L_0x002c:
            java.lang.Object r0 = r13.next()
            boolean r1 = r0 instanceof javax.mail.internet.ParameterList.Value
            if (r1 == 0) goto L_0x001e
            javax.mail.internet.ParameterList$Value r0 = (javax.mail.internet.ParameterList.Value) r0
            java.lang.String r1 = r0.encodedValue
            javax.mail.internet.ParameterList$Value r1 = decodeValue(r1)
            java.lang.String r2 = r1.charset
            r0.charset = r2
            java.lang.String r1 = r1.value
            r0.value = r1
            goto L_0x001e
        L_0x0045:
            java.util.Set r13 = r12.multisegmentNames
            r13.clear()
            java.util.Map r12 = r12.slist
            r12.clear()
            return
        L_0x0050:
            java.lang.Object r1 = r0.next()     // Catch:{ all -> 0x011a }
            java.lang.String r1 = (java.lang.String) r1     // Catch:{ all -> 0x011a }
            java.lang.StringBuffer r2 = new java.lang.StringBuffer     // Catch:{ all -> 0x011a }
            r2.<init>()     // Catch:{ all -> 0x011a }
            javax.mail.internet.ParameterList$MultiValue r3 = new javax.mail.internet.ParameterList$MultiValue     // Catch:{ all -> 0x011a }
            r4 = 0
            r3.<init>(r4)     // Catch:{ all -> 0x011a }
            r5 = 0
            r6 = r4
        L_0x0063:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x011a }
            java.lang.String r8 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x011a }
            r7.<init>(r8)     // Catch:{ all -> 0x011a }
            java.lang.String r8 = "*"
            r7.append(r8)     // Catch:{ all -> 0x011a }
            r7.append(r5)     // Catch:{ all -> 0x011a }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x011a }
            java.util.Map r8 = r12.slist     // Catch:{ all -> 0x011a }
            java.lang.Object r8 = r8.get(r7)     // Catch:{ all -> 0x011a }
            if (r8 != 0) goto L_0x0081
            goto L_0x00b0
        L_0x0081:
            r3.add(r8)     // Catch:{ all -> 0x011a }
            boolean r9 = r8 instanceof javax.mail.internet.ParameterList.Value     // Catch:{ all -> 0x011a }
            if (r9 == 0) goto L_0x0109
            javax.mail.internet.ParameterList$Value r8 = (javax.mail.internet.ParameterList.Value) r8     // Catch:{ NumberFormatException -> 0x00f5, UnsupportedEncodingException -> 0x00e2, StringIndexOutOfBoundsException -> 0x00cf }
            java.lang.String r9 = r8.encodedValue     // Catch:{ NumberFormatException -> 0x00f5, UnsupportedEncodingException -> 0x00e2, StringIndexOutOfBoundsException -> 0x00cf }
            if (r5 != 0) goto L_0x00a9
            javax.mail.internet.ParameterList$Value r10 = decodeValue(r9)     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            java.lang.String r11 = r10.charset     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            r8.charset = r11     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            java.lang.String r6 = r10.value     // Catch:{ NumberFormatException -> 0x00a0, UnsupportedEncodingException -> 0x009e, StringIndexOutOfBoundsException -> 0x009c }
            r8.value = r6     // Catch:{ NumberFormatException -> 0x00a0, UnsupportedEncodingException -> 0x009e, StringIndexOutOfBoundsException -> 0x009c }
            goto L_0x010d
        L_0x009c:
            r6 = move-exception
            goto L_0x00d3
        L_0x009e:
            r6 = move-exception
            goto L_0x00e6
        L_0x00a0:
            r6 = move-exception
            goto L_0x00f9
        L_0x00a3:
            r8 = move-exception
            goto L_0x00d1
        L_0x00a5:
            r8 = move-exception
            goto L_0x00e4
        L_0x00a7:
            r8 = move-exception
            goto L_0x00f7
        L_0x00a9:
            if (r6 != 0) goto L_0x00c6
            java.util.Set r8 = r12.multisegmentNames     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            r8.remove(r1)     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
        L_0x00b0:
            if (r5 != 0) goto L_0x00b9
            java.util.Map r2 = r12.list     // Catch:{ all -> 0x011a }
            r2.remove(r1)     // Catch:{ all -> 0x011a }
            goto L_0x0006
        L_0x00b9:
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x011a }
            r3.value = r2     // Catch:{ all -> 0x011a }
            java.util.Map r2 = r12.list     // Catch:{ all -> 0x011a }
            r2.put(r1, r3)     // Catch:{ all -> 0x011a }
            goto L_0x0006
        L_0x00c6:
            java.lang.String r10 = decodeBytes(r9, r6)     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            r8.value = r10     // Catch:{ NumberFormatException -> 0x00a7, UnsupportedEncodingException -> 0x00a5, StringIndexOutOfBoundsException -> 0x00a3 }
            r11 = r6
            r6 = r10
            goto L_0x010d
        L_0x00cf:
            r8 = move-exception
            r9 = r4
        L_0x00d1:
            r11 = r6
            r6 = r8
        L_0x00d3:
            boolean r8 = decodeParametersStrict     // Catch:{ all -> 0x011a }
            if (r8 != 0) goto L_0x00d8
            goto L_0x00fd
        L_0x00d8:
            javax.mail.internet.ParseException r0 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x011a }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x011a }
            r0.<init>(r1)     // Catch:{ all -> 0x011a }
            throw r0     // Catch:{ all -> 0x011a }
        L_0x00e2:
            r8 = move-exception
            r9 = r4
        L_0x00e4:
            r11 = r6
            r6 = r8
        L_0x00e6:
            boolean r8 = decodeParametersStrict     // Catch:{ all -> 0x011a }
            if (r8 != 0) goto L_0x00eb
            goto L_0x00fd
        L_0x00eb:
            javax.mail.internet.ParseException r0 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x011a }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x011a }
            r0.<init>(r1)     // Catch:{ all -> 0x011a }
            throw r0     // Catch:{ all -> 0x011a }
        L_0x00f5:
            r8 = move-exception
            r9 = r4
        L_0x00f7:
            r11 = r6
            r6 = r8
        L_0x00f9:
            boolean r8 = decodeParametersStrict     // Catch:{ all -> 0x011a }
            if (r8 != 0) goto L_0x00ff
        L_0x00fd:
            r6 = r9
            goto L_0x010d
        L_0x00ff:
            javax.mail.internet.ParseException r0 = new javax.mail.internet.ParseException     // Catch:{ all -> 0x011a }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x011a }
            r0.<init>(r1)     // Catch:{ all -> 0x011a }
            throw r0     // Catch:{ all -> 0x011a }
        L_0x0109:
            java.lang.String r8 = (java.lang.String) r8     // Catch:{ all -> 0x011a }
            r11 = r6
            r6 = r8
        L_0x010d:
            r2.append(r6)     // Catch:{ all -> 0x011a }
            java.util.Map r6 = r12.slist     // Catch:{ all -> 0x011a }
            r6.remove(r7)     // Catch:{ all -> 0x011a }
            int r5 = r5 + 1
            r6 = r11
            goto L_0x0063
        L_0x011a:
            r0 = move-exception
            if (r13 == 0) goto L_0x015f
            java.util.Map r13 = r12.slist
            int r13 = r13.size()
            if (r13 <= 0) goto L_0x0155
            java.util.Map r13 = r12.slist
            java.util.Collection r13 = r13.values()
            java.util.Iterator r13 = r13.iterator()
        L_0x012f:
            boolean r1 = r13.hasNext()
            if (r1 == 0) goto L_0x014e
            java.lang.Object r1 = r13.next()
            boolean r2 = r1 instanceof javax.mail.internet.ParameterList.Value
            if (r2 == 0) goto L_0x012f
            javax.mail.internet.ParameterList$Value r1 = (javax.mail.internet.ParameterList.Value) r1
            java.lang.String r2 = r1.encodedValue
            javax.mail.internet.ParameterList$Value r2 = decodeValue(r2)
            java.lang.String r3 = r2.charset
            r1.charset = r3
            java.lang.String r2 = r2.value
            r1.value = r2
            goto L_0x012f
        L_0x014e:
            java.util.Map r13 = r12.list
            java.util.Map r1 = r12.slist
            r13.putAll(r1)
        L_0x0155:
            java.util.Set r13 = r12.multisegmentNames
            r13.clear()
            java.util.Map r12 = r12.slist
            r12.clear()
        L_0x015f:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.ParameterList.combineMultisegmentNames(boolean):void");
    }

    public String get(String str) {
        Object obj = this.list.get(str.trim().toLowerCase(Locale.ENGLISH));
        if (obj instanceof MultiValue) {
            return ((MultiValue) obj).value;
        }
        if (obj instanceof Value) {
            return ((Value) obj).value;
        }
        return (String) obj;
    }

    public void set(String str, String str2) {
        if (str != null || str2 == null || !str2.equals("DONE")) {
            String lowerCase = str.trim().toLowerCase(Locale.ENGLISH);
            if (decodeParameters) {
                try {
                    putEncodedName(lowerCase, str2);
                } catch (ParseException unused) {
                    this.list.put(lowerCase, str2);
                }
            } else {
                this.list.put(lowerCase, str2);
            }
        } else if (decodeParameters && this.multisegmentNames.size() > 0) {
            try {
                combineMultisegmentNames(true);
            } catch (ParseException unused2) {
            }
        }
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int i) {
        ToStringBuffer toStringBuffer = new ToStringBuffer(i);
        for (String str : this.list.keySet()) {
            Object obj = this.list.get(str);
            if (obj instanceof MultiValue) {
                MultiValue multiValue = (MultiValue) obj;
                String str2 = String.valueOf(str) + "*";
                for (int i2 = 0; i2 < multiValue.size(); i2++) {
                    Object obj2 = multiValue.get(i2);
                    if (obj2 instanceof Value) {
                        toStringBuffer.addNV(String.valueOf(str2) + i2 + "*", ((Value) obj2).encodedValue);
                    } else {
                        toStringBuffer.addNV(String.valueOf(str2) + i2, (String) obj2);
                    }
                }
            } else if (obj instanceof Value) {
                toStringBuffer.addNV(String.valueOf(str) + "*", ((Value) obj).encodedValue);
            } else {
                toStringBuffer.addNV(str, (String) obj);
            }
        }
        return toStringBuffer.toString();
    }

    private static class ToStringBuffer {
        private StringBuffer sb = new StringBuffer();
        private int used;

        public ToStringBuffer(int i) {
            this.used = i;
        }

        public void addNV(String str, String str2) {
            String access$0 = ParameterList.quote(str2);
            this.sb.append("; ");
            this.used += 2;
            if (this.used + str.length() + access$0.length() + 1 > 76) {
                this.sb.append("\r\n\t");
                this.used = 8;
            }
            StringBuffer stringBuffer = this.sb;
            stringBuffer.append(str);
            stringBuffer.append('=');
            int length = this.used + str.length() + 1;
            this.used = length;
            if (length + access$0.length() > 76) {
                String fold = MimeUtility.fold(this.used, access$0);
                this.sb.append(fold);
                int lastIndexOf = fold.lastIndexOf(10);
                if (lastIndexOf >= 0) {
                    this.used += (fold.length() - lastIndexOf) - 1;
                } else {
                    this.used += fold.length();
                }
            } else {
                this.sb.append(access$0);
                this.used += access$0.length();
            }
        }

        public String toString() {
            return this.sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static String quote(String str) {
        return MimeUtility.quote(str, "()<>@,;:\\\"\t []/?=");
    }

    private static Value decodeValue(String str) throws ParseException {
        Value value = new Value((Value) null);
        value.encodedValue = str;
        value.value = str;
        try {
            int indexOf = str.indexOf(39);
            if (indexOf > 0) {
                String substring = str.substring(0, indexOf);
                int i = indexOf + 1;
                int indexOf2 = str.indexOf(39, i);
                if (indexOf2 >= 0) {
                    str.substring(i, indexOf2);
                    String substring2 = str.substring(indexOf2 + 1);
                    value.charset = substring;
                    value.value = decodeBytes(substring2, substring);
                    return value;
                } else if (!decodeParametersStrict) {
                    return value;
                } else {
                    throw new ParseException("Missing language in encoded value: " + str);
                }
            } else if (!decodeParametersStrict) {
                return value;
            } else {
                throw new ParseException("Missing charset in encoded value: " + str);
            }
        } catch (NumberFormatException e) {
            if (decodeParametersStrict) {
                throw new ParseException(e.toString());
            }
        } catch (UnsupportedEncodingException e2) {
            if (decodeParametersStrict) {
                throw new ParseException(e2.toString());
            }
        } catch (StringIndexOutOfBoundsException e3) {
            if (decodeParametersStrict) {
                throw new ParseException(e3.toString());
            }
        }
    }

    private static String decodeBytes(String str, String str2) throws UnsupportedEncodingException {
        byte[] bArr = new byte[str.length()];
        int i = 0;
        int i2 = 0;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt == '%') {
                charAt = (char) Integer.parseInt(str.substring(i + 1, i + 3), 16);
                i += 2;
            }
            bArr[i2] = (byte) charAt;
            i++;
            i2++;
        }
        return new String(bArr, 0, i2, MimeUtility.javaCharset(str2));
    }
}

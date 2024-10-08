package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MailcapFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MailcapCommandMap extends CommandMap {
    private static MailcapFile defDB;
    private MailcapFile[] DB;

    public MailcapCommandMap() {
        ArrayList arrayList = new ArrayList(5);
        arrayList.add((Object) null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            String property = System.getProperty("user.home");
            if (property != null) {
                MailcapFile loadFile = loadFile(property + File.separator + ".mailcap");
                if (loadFile != null) {
                    arrayList.add(loadFile);
                }
            }
        } catch (SecurityException unused) {
        }
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            StringBuilder sb = new StringBuilder(String.valueOf(System.getProperty("java.home")));
            String str = File.separator;
            sb.append(str);
            sb.append("lib");
            sb.append(str);
            sb.append("mailcap");
            MailcapFile loadFile2 = loadFile(sb.toString());
            if (loadFile2 != null) {
                arrayList.add(loadFile2);
            }
        } catch (SecurityException unused2) {
        }
        LogSupport.log("MailcapCommandMap: load JAR");
        loadAllResources(arrayList, "mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        synchronized (MailcapCommandMap.class) {
            if (defDB == null) {
                defDB = loadResource("mailcap.default");
            }
        }
        MailcapFile mailcapFile = defDB;
        if (mailcapFile != null) {
            arrayList.add(mailcapFile);
        }
        MailcapFile[] mailcapFileArr = new MailcapFile[arrayList.size()];
        this.DB = mailcapFileArr;
        this.DB = (MailcapFile[]) arrayList.toArray(mailcapFileArr);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: java.io.InputStream} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.io.InputStream} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: javax.activation.MailcapCommandMap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: java.io.InputStream} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: javax.activation.MailcapCommandMap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v9, resolved type: javax.activation.MailcapCommandMap} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: java.io.InputStream} */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        if (r5 != null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0081, code lost:
        if (r5 == null) goto L_0x0084;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0058 A[Catch:{ IOException -> 0x006a, SecurityException -> 0x0050, all -> 0x004e, all -> 0x0085 }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0069 A[Catch:{ IOException -> 0x006a, SecurityException -> 0x0050, all -> 0x004e, all -> 0x0085 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0072 A[Catch:{ IOException -> 0x006a, SecurityException -> 0x0050, all -> 0x004e, all -> 0x0085 }] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0089 A[SYNTHETIC, Splitter:B:41:0x0089] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sun.activation.registries.MailcapFile loadResource(java.lang.String r6) {
        /*
            r5 = this;
            java.lang.String r0 = "MailcapCommandMap: can't load "
            r1 = 0
            java.lang.Class r5 = r5.getClass()     // Catch:{ IOException -> 0x006a, SecurityException -> 0x0050, all -> 0x004e }
            java.io.InputStream r5 = javax.activation.SecuritySupport.getResourceAsStream(r5, r6)     // Catch:{ IOException -> 0x006a, SecurityException -> 0x0050, all -> 0x004e }
            if (r5 == 0) goto L_0x002d
            com.sun.activation.registries.MailcapFile r2 = new com.sun.activation.registries.MailcapFile     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            r2.<init>((java.io.InputStream) r5)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            boolean r3 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            if (r3 == 0) goto L_0x0029
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            java.lang.String r4 = "MailcapCommandMap: successfully loaded mailcap file: "
            r3.<init>(r4)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            r3.append(r6)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            com.sun.activation.registries.LogSupport.log(r3)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
        L_0x0029:
            r5.close()     // Catch:{ IOException -> 0x002c }
        L_0x002c:
            return r2
        L_0x002d:
            boolean r2 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            if (r2 == 0) goto L_0x0044
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            java.lang.String r3 = "MailcapCommandMap: not loading mailcap file: "
            r2.<init>(r3)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            r2.append(r6)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
            com.sun.activation.registries.LogSupport.log(r2)     // Catch:{ IOException -> 0x004c, SecurityException -> 0x004a }
        L_0x0044:
            if (r5 == 0) goto L_0x0084
        L_0x0046:
            r5.close()     // Catch:{ IOException -> 0x0084 }
            goto L_0x0084
        L_0x004a:
            r2 = move-exception
            goto L_0x0052
        L_0x004c:
            r2 = move-exception
            goto L_0x006c
        L_0x004e:
            r6 = move-exception
            goto L_0x0087
        L_0x0050:
            r2 = move-exception
            r5 = r1
        L_0x0052:
            boolean r3 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x0085 }
            if (r3 == 0) goto L_0x0067
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0085 }
            r3.<init>(r0)     // Catch:{ all -> 0x0085 }
            r3.append(r6)     // Catch:{ all -> 0x0085 }
            java.lang.String r6 = r3.toString()     // Catch:{ all -> 0x0085 }
            com.sun.activation.registries.LogSupport.log(r6, r2)     // Catch:{ all -> 0x0085 }
        L_0x0067:
            if (r5 == 0) goto L_0x0084
            goto L_0x0046
        L_0x006a:
            r2 = move-exception
            r5 = r1
        L_0x006c:
            boolean r3 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x0085 }
            if (r3 == 0) goto L_0x0081
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0085 }
            r3.<init>(r0)     // Catch:{ all -> 0x0085 }
            r3.append(r6)     // Catch:{ all -> 0x0085 }
            java.lang.String r6 = r3.toString()     // Catch:{ all -> 0x0085 }
            com.sun.activation.registries.LogSupport.log(r6, r2)     // Catch:{ all -> 0x0085 }
        L_0x0081:
            if (r5 == 0) goto L_0x0084
            goto L_0x0046
        L_0x0084:
            return r1
        L_0x0085:
            r6 = move-exception
            r1 = r5
        L_0x0087:
            if (r1 == 0) goto L_0x008c
            r1.close()     // Catch:{ IOException -> 0x008c }
        L_0x008c:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.loadResource(java.lang.String):com.sun.activation.registries.MailcapFile");
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x00cb */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:72:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadAllResources(java.util.List r9, java.lang.String r10) {
        /*
            r8 = this;
            java.lang.String r0 = "MailcapCommandMap: can't load "
            r1 = 0
            java.lang.ClassLoader r2 = javax.activation.SecuritySupport.getContextClassLoader()     // Catch:{ Exception -> 0x00cf }
            if (r2 != 0) goto L_0x0011
            java.lang.Class r2 = r8.getClass()     // Catch:{ Exception -> 0x00cf }
            java.lang.ClassLoader r2 = r2.getClassLoader()     // Catch:{ Exception -> 0x00cf }
        L_0x0011:
            if (r2 == 0) goto L_0x0018
            java.net.URL[] r2 = javax.activation.SecuritySupport.getResources(r2, r10)     // Catch:{ Exception -> 0x00cf }
            goto L_0x001c
        L_0x0018:
            java.net.URL[] r2 = javax.activation.SecuritySupport.getSystemResources(r10)     // Catch:{ Exception -> 0x00cf }
        L_0x001c:
            if (r2 == 0) goto L_0x00e5
            boolean r3 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ Exception -> 0x00cf }
            if (r3 == 0) goto L_0x0029
            java.lang.String r3 = "MailcapCommandMap: getResources"
            com.sun.activation.registries.LogSupport.log(r3)     // Catch:{ Exception -> 0x00cf }
        L_0x0029:
            r3 = r1
        L_0x002a:
            int r4 = r2.length     // Catch:{ Exception -> 0x00cc }
            if (r1 < r4) goto L_0x0030
            r1 = r3
            goto L_0x00e5
        L_0x0030:
            r4 = r2[r1]     // Catch:{ Exception -> 0x00cc }
            boolean r5 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0049
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00cc }
            java.lang.String r6 = "MailcapCommandMap: URL "
            r5.<init>(r6)     // Catch:{ Exception -> 0x00cc }
            r5.append(r4)     // Catch:{ Exception -> 0x00cc }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x00cc }
            com.sun.activation.registries.LogSupport.log(r5)     // Catch:{ Exception -> 0x00cc }
        L_0x0049:
            r5 = 0
            java.io.InputStream r5 = javax.activation.SecuritySupport.openStream(r4)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            if (r5 == 0) goto L_0x0071
            com.sun.activation.registries.MailcapFile r6 = new com.sun.activation.registries.MailcapFile     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            r6.<init>((java.io.InputStream) r5)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            r9.add(r6)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            r3 = 1
            boolean r6 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            if (r6 == 0) goto L_0x0088
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            java.lang.String r7 = "MailcapCommandMap: successfully loaded mailcap file from URL: "
            r6.<init>(r7)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            r6.append(r4)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            com.sun.activation.registries.LogSupport.log(r6)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            goto L_0x0088
        L_0x0071:
            boolean r6 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            if (r6 == 0) goto L_0x0088
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            java.lang.String r7 = "MailcapCommandMap: not loading mailcap file from URL: "
            r6.<init>(r7)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            r6.append(r4)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
            com.sun.activation.registries.LogSupport.log(r6)     // Catch:{ IOException -> 0x00a9, SecurityException -> 0x0090 }
        L_0x0088:
            if (r5 == 0) goto L_0x00c2
        L_0x008a:
            r5.close()     // Catch:{ IOException -> 0x00c2 }
            goto L_0x00c2
        L_0x008e:
            r1 = move-exception
            goto L_0x00c6
        L_0x0090:
            r6 = move-exception
            boolean r7 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x008e }
            if (r7 == 0) goto L_0x00a6
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x008e }
            r7.<init>(r0)     // Catch:{ all -> 0x008e }
            r7.append(r4)     // Catch:{ all -> 0x008e }
            java.lang.String r4 = r7.toString()     // Catch:{ all -> 0x008e }
            com.sun.activation.registries.LogSupport.log(r4, r6)     // Catch:{ all -> 0x008e }
        L_0x00a6:
            if (r5 == 0) goto L_0x00c2
            goto L_0x008a
        L_0x00a9:
            r6 = move-exception
            boolean r7 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x008e }
            if (r7 == 0) goto L_0x00bf
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x008e }
            r7.<init>(r0)     // Catch:{ all -> 0x008e }
            r7.append(r4)     // Catch:{ all -> 0x008e }
            java.lang.String r4 = r7.toString()     // Catch:{ all -> 0x008e }
            com.sun.activation.registries.LogSupport.log(r4, r6)     // Catch:{ all -> 0x008e }
        L_0x00bf:
            if (r5 == 0) goto L_0x00c2
            goto L_0x008a
        L_0x00c2:
            int r1 = r1 + 1
            goto L_0x002a
        L_0x00c6:
            if (r5 == 0) goto L_0x00cb
            r5.close()     // Catch:{ IOException -> 0x00cb }
        L_0x00cb:
            throw r1     // Catch:{ Exception -> 0x00cc }
        L_0x00cc:
            r2 = move-exception
            r1 = r3
            goto L_0x00d0
        L_0x00cf:
            r2 = move-exception
        L_0x00d0:
            boolean r3 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r3 == 0) goto L_0x00e5
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>(r0)
            r3.append(r10)
            java.lang.String r0 = r3.toString()
            com.sun.activation.registries.LogSupport.log(r0, r2)
        L_0x00e5:
            if (r1 != 0) goto L_0x0109
            boolean r0 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r0 == 0) goto L_0x00f2
            java.lang.String r0 = "MailcapCommandMap: !anyLoaded"
            com.sun.activation.registries.LogSupport.log(r0)
        L_0x00f2:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "/"
            r0.<init>(r1)
            r0.append(r10)
            java.lang.String r10 = r0.toString()
            com.sun.activation.registries.MailcapFile r8 = r8.loadResource(r10)
            if (r8 == 0) goto L_0x0109
            r9.add(r8)
        L_0x0109:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.loadAllResources(java.util.List, java.lang.String):void");
    }

    private MailcapFile loadFile(String str) {
        try {
            return new MailcapFile(str);
        } catch (IOException unused) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0027, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        r2 = r4.DB;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002b, code lost:
        if (r1 < r2.length) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        if (r2[r1] != null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        if (com.sun.activation.registries.LogSupport.isLoggable() == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003b, code lost:
        com.sun.activation.registries.LogSupport.log("  search fallback DB #" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        r2 = r4.DB[r1].getMailcapFallbackList(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0054, code lost:
        if (r2 == null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        r2 = (java.util.List) r2.get("content-handler");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005e, code lost:
        if (r2 == null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0060, code lost:
        r2 = getDataContentHandler((java.lang.String) r2.get(0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006a, code lost:
        if (r2 == null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006d, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006e, code lost:
        r1 = r1 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.activation.DataContentHandler createDataContentHandler(java.lang.String r5) {
        /*
            r4 = this;
            monitor-enter(r4)
            boolean r0 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00b3 }
            if (r0 == 0) goto L_0x0018
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            java.lang.String r1 = "MailcapCommandMap: createDataContentHandler for "
            r0.<init>(r1)     // Catch:{ all -> 0x00b3 }
            r0.append(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x00b3 }
            com.sun.activation.registries.LogSupport.log(r0)     // Catch:{ all -> 0x00b3 }
        L_0x0018:
            if (r5 == 0) goto L_0x0020
            java.util.Locale r0 = java.util.Locale.ENGLISH     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = r5.toLowerCase(r0)     // Catch:{ all -> 0x00b3 }
        L_0x0020:
            r0 = 0
            r1 = r0
        L_0x0022:
            com.sun.activation.registries.MailcapFile[] r2 = r4.DB     // Catch:{ all -> 0x00b3 }
            int r3 = r2.length     // Catch:{ all -> 0x00b3 }
            if (r1 < r3) goto L_0x0071
            r1 = r0
        L_0x0028:
            com.sun.activation.registries.MailcapFile[] r2 = r4.DB     // Catch:{ all -> 0x00b3 }
            int r3 = r2.length     // Catch:{ all -> 0x00b3 }
            if (r1 < r3) goto L_0x0030
            monitor-exit(r4)
            r4 = 0
            return r4
        L_0x0030:
            r2 = r2[r1]     // Catch:{ all -> 0x00b3 }
            if (r2 != 0) goto L_0x0035
            goto L_0x006e
        L_0x0035:
            boolean r2 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x004c
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = "  search fallback DB #"
            r2.<init>(r3)     // Catch:{ all -> 0x00b3 }
            r2.append(r1)     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00b3 }
            com.sun.activation.registries.LogSupport.log(r2)     // Catch:{ all -> 0x00b3 }
        L_0x004c:
            com.sun.activation.registries.MailcapFile[] r2 = r4.DB     // Catch:{ all -> 0x00b3 }
            r2 = r2[r1]     // Catch:{ all -> 0x00b3 }
            java.util.Map r2 = r2.getMailcapFallbackList(r5)     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x006e
            java.lang.String r3 = "content-handler"
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x00b3 }
            java.util.List r2 = (java.util.List) r2     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x006e
            java.lang.Object r2 = r2.get(r0)     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = (java.lang.String) r2     // Catch:{ all -> 0x00b3 }
            javax.activation.DataContentHandler r2 = r4.getDataContentHandler(r2)     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x006e
            monitor-exit(r4)
            return r2
        L_0x006e:
            int r1 = r1 + 1
            goto L_0x0028
        L_0x0071:
            r2 = r2[r1]     // Catch:{ all -> 0x00b3 }
            if (r2 != 0) goto L_0x0076
            goto L_0x00af
        L_0x0076:
            boolean r2 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x008d
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            java.lang.String r3 = "  search DB #"
            r2.<init>(r3)     // Catch:{ all -> 0x00b3 }
            r2.append(r1)     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00b3 }
            com.sun.activation.registries.LogSupport.log(r2)     // Catch:{ all -> 0x00b3 }
        L_0x008d:
            com.sun.activation.registries.MailcapFile[] r2 = r4.DB     // Catch:{ all -> 0x00b3 }
            r2 = r2[r1]     // Catch:{ all -> 0x00b3 }
            java.util.Map r2 = r2.getMailcapList(r5)     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x00af
            java.lang.String r3 = "content-handler"
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x00b3 }
            java.util.List r2 = (java.util.List) r2     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x00af
            java.lang.Object r2 = r2.get(r0)     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = (java.lang.String) r2     // Catch:{ all -> 0x00b3 }
            javax.activation.DataContentHandler r2 = r4.getDataContentHandler(r2)     // Catch:{ all -> 0x00b3 }
            if (r2 == 0) goto L_0x00af
            monitor-exit(r4)
            return r2
        L_0x00af:
            int r1 = r1 + 1
            goto L_0x0022
        L_0x00b3:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.createDataContentHandler(java.lang.String):javax.activation.DataContentHandler");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r3 = java.lang.Class.forName(r4);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:12:0x0037 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private javax.activation.DataContentHandler getDataContentHandler(java.lang.String r4) {
        /*
            r3 = this;
            java.lang.String r0 = "Can't load DCH "
            boolean r1 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r1 == 0) goto L_0x000d
            java.lang.String r1 = "    got content-handler"
            com.sun.activation.registries.LogSupport.log(r1)
        L_0x000d:
            boolean r1 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r1 == 0) goto L_0x0024
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            java.lang.String r2 = "      class "
            r1.<init>(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            com.sun.activation.registries.LogSupport.log(r1)
        L_0x0024:
            java.lang.ClassLoader r1 = javax.activation.SecuritySupport.getContextClassLoader()     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
            if (r1 != 0) goto L_0x0032
            java.lang.Class r3 = r3.getClass()     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
            java.lang.ClassLoader r1 = r3.getClassLoader()     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
        L_0x0032:
            java.lang.Class r3 = r1.loadClass(r4)     // Catch:{ Exception -> 0x0037 }
            goto L_0x003b
        L_0x0037:
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
        L_0x003b:
            if (r3 == 0) goto L_0x0088
            java.lang.Object r3 = r3.newInstance()     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
            javax.activation.DataContentHandler r3 = (javax.activation.DataContentHandler) r3     // Catch:{ IllegalAccessException -> 0x0072, ClassNotFoundException -> 0x005b, InstantiationException -> 0x0044 }
            return r3
        L_0x0044:
            r3 = move-exception
            boolean r1 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r1 == 0) goto L_0x0088
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>(r0)
            r1.append(r4)
            java.lang.String r4 = r1.toString()
            com.sun.activation.registries.LogSupport.log(r4, r3)
            goto L_0x0088
        L_0x005b:
            r3 = move-exception
            boolean r1 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r1 == 0) goto L_0x0088
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>(r0)
            r1.append(r4)
            java.lang.String r4 = r1.toString()
            com.sun.activation.registries.LogSupport.log(r4, r3)
            goto L_0x0088
        L_0x0072:
            r3 = move-exception
            boolean r1 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r1 == 0) goto L_0x0088
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>(r0)
            r1.append(r4)
            java.lang.String r4 = r1.toString()
            com.sun.activation.registries.LogSupport.log(r4, r3)
        L_0x0088:
            r3 = 0
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.getDataContentHandler(java.lang.String):javax.activation.DataContentHandler");
    }
}

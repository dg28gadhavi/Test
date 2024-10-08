package org.apache.harmony.awt.datatransfer;

import org.apache.harmony.awt.internal.nls.Messages;
import org.apache.harmony.misc.SystemUtils;

public abstract class DTK {
    protected final DataTransferThread dataTransferThread;

    public String getDefaultCharset() {
        return "unicode";
    }

    public abstract void initDragAndDrop();

    public abstract void runEventLoop();

    protected DTK() {
        DataTransferThread dataTransferThread2 = new DataTransferThread(this);
        this.dataTransferThread = dataTransferThread2;
        dataTransferThread2.start();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static org.apache.harmony.awt.datatransfer.DTK getDTK() {
        /*
            java.lang.Object r0 = org.apache.harmony.awt.ContextStorage.getContextLock()
            monitor-enter(r0)
            boolean r1 = org.apache.harmony.awt.ContextStorage.shutdownPending()     // Catch:{ all -> 0x001d }
            if (r1 == 0) goto L_0x000e
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            r0 = 0
            return r0
        L_0x000e:
            org.apache.harmony.awt.datatransfer.DTK r1 = org.apache.harmony.awt.ContextStorage.getDTK()     // Catch:{ all -> 0x001d }
            if (r1 != 0) goto L_0x001b
            org.apache.harmony.awt.datatransfer.DTK r1 = createDTK()     // Catch:{ all -> 0x001d }
            org.apache.harmony.awt.ContextStorage.setDTK(r1)     // Catch:{ all -> 0x001d }
        L_0x001b:
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            return r1
        L_0x001d:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.awt.datatransfer.DTK.getDTK():org.apache.harmony.awt.datatransfer.DTK");
    }

    private static DTK createDTK() {
        String str;
        int os = SystemUtils.getOS();
        if (os == 1) {
            str = "org.apache.harmony.awt.datatransfer.windows.WinDTK";
        } else if (os == 2) {
            str = "org.apache.harmony.awt.datatransfer.linux.LinuxDTK";
        } else {
            throw new RuntimeException(Messages.getString("awt.4E"));
        }
        try {
            return (DTK) Class.forName(str).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

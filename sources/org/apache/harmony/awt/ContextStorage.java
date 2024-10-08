package org.apache.harmony.awt;

import org.apache.harmony.awt.datatransfer.DTK;

public final class ContextStorage {
    private static final ContextStorage globalContext = new ContextStorage();
    private final Object contextLock = new ContextLock(this, (ContextLock) null);
    private DTK dtk;
    private volatile boolean shutdownPending = false;

    private class ContextLock {
        private ContextLock() {
        }

        /* synthetic */ ContextLock(ContextStorage contextStorage, ContextLock contextLock) {
            this();
        }
    }

    public static void setDTK(DTK dtk2) {
        getCurrentContext().dtk = dtk2;
    }

    public static DTK getDTK() {
        return getCurrentContext().dtk;
    }

    public static Object getContextLock() {
        return getCurrentContext().contextLock;
    }

    private static ContextStorage getCurrentContext() {
        return globalContext;
    }

    public static boolean shutdownPending() {
        return getCurrentContext().shutdownPending;
    }
}

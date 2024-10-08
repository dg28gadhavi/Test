package android.support.v4.content;

import android.content.Context;
import java.io.File;

public class ContextCompat {
    private static final Object sLock = new Object();

    public static File[] getExternalFilesDirs(Context context, String str) {
        return context.getExternalFilesDirs(str);
    }

    public static File[] getExternalCacheDirs(Context context) {
        return context.getExternalCacheDirs();
    }

    public static final int getColor(Context context, int i) {
        return context.getColor(i);
    }

    public static boolean isDeviceProtectedStorage(Context context) {
        return context.isDeviceProtectedStorage();
    }
}

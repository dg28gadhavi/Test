package com.sec.internal.ims.settings;

import android.content.Context;

public class GlobalSettingsRepoUsa extends GlobalSettingsRepoBase {
    /* access modifiers changed from: protected */
    public boolean needResetVolteAsDefault(int i, int i2, String str, String str2) {
        return i != i2;
    }

    public GlobalSettingsRepoUsa(Context context, int i) {
        super(context, i);
    }
}

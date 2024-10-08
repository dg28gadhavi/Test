package com.sec.internal.ims.core.sim;

import android.content.ContentValues;
import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SimManager$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ ContentValues f$0;
    public final /* synthetic */ SharedPreferences f$1;

    public /* synthetic */ SimManager$$ExternalSyntheticLambda0(ContentValues contentValues, SharedPreferences sharedPreferences) {
        this.f$0 = contentValues;
        this.f$1 = sharedPreferences;
    }

    public final void accept(Object obj) {
        this.f$0.put((String) obj, Boolean.valueOf(this.f$1.getBoolean((String) obj, false)));
    }
}

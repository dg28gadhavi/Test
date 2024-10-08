package com.sec.internal.ims.settings;

import android.util.SparseArray;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class DeviceConfigManager$$ExternalSyntheticLambda2 implements Consumer {
    public final /* synthetic */ DeviceConfigManager f$0;
    public final /* synthetic */ Integer f$1;
    public final /* synthetic */ SparseArray f$2;

    public /* synthetic */ DeviceConfigManager$$ExternalSyntheticLambda2(DeviceConfigManager deviceConfigManager, Integer num, SparseArray sparseArray) {
        this.f$0 = deviceConfigManager;
        this.f$1 = num;
        this.f$2 = sparseArray;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$initStorage$0(this.f$1, this.f$2, (String) obj);
    }
}

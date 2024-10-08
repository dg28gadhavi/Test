package com.sec.internal.ims.settings;

import java.util.function.Consumer;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class NvStorage$$ExternalSyntheticLambda5 implements Consumer {
    public final /* synthetic */ NvStorage f$0;
    public final /* synthetic */ Element f$1;

    public /* synthetic */ NvStorage$$ExternalSyntheticLambda5(NvStorage nvStorage, Element element) {
        this.f$0 = nvStorage;
        this.f$1 = element;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$migrateFromOldFile$2(this.f$1, (NamedNodeMap) obj);
    }
}

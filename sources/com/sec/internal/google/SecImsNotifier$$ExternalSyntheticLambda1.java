package com.sec.internal.google;

import android.net.Uri;
import com.sec.ims.util.NameAddr;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SecImsNotifier$$ExternalSyntheticLambda1 implements Function {
    public final Object apply(Object obj) {
        return Uri.parse(((NameAddr) obj).getUri().toString());
    }
}

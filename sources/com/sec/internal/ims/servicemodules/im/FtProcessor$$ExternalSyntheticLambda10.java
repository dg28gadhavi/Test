package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import java.util.Set;
import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class FtProcessor$$ExternalSyntheticLambda10 implements Callable {
    public final /* synthetic */ FtProcessor f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ boolean f$10;
    public final /* synthetic */ boolean f$11;
    public final /* synthetic */ FileDisposition f$12;
    public final /* synthetic */ boolean f$13;
    public final /* synthetic */ boolean f$14;
    public final /* synthetic */ String f$15;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ Uri f$3;
    public final /* synthetic */ ImsUri f$4;
    public final /* synthetic */ Set f$5;
    public final /* synthetic */ String f$6;
    public final /* synthetic */ String f$7;
    public final /* synthetic */ boolean f$8;
    public final /* synthetic */ boolean f$9;

    public /* synthetic */ FtProcessor$$ExternalSyntheticLambda10(FtProcessor ftProcessor, int i, String str, Uri uri, ImsUri imsUri, Set set, String str2, String str3, boolean z, boolean z2, boolean z3, boolean z4, FileDisposition fileDisposition, boolean z5, boolean z6, String str4) {
        this.f$0 = ftProcessor;
        this.f$1 = i;
        this.f$2 = str;
        this.f$3 = uri;
        this.f$4 = imsUri;
        this.f$5 = set;
        this.f$6 = str2;
        this.f$7 = str3;
        this.f$8 = z;
        this.f$9 = z2;
        this.f$10 = z3;
        this.f$11 = z4;
        this.f$12 = fileDisposition;
        this.f$13 = z5;
        this.f$14 = z6;
        this.f$15 = str4;
    }

    public final Object call() {
        return this.f$0.lambda$attachFileToSingleChat$3(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15);
    }
}

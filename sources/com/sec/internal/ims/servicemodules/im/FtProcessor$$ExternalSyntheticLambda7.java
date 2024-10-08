package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import java.util.Set;
import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class FtProcessor$$ExternalSyntheticLambda7 implements Callable {
    public final /* synthetic */ FtProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ FileDisposition f$10;
    public final /* synthetic */ boolean f$11;
    public final /* synthetic */ String f$12;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ Uri f$3;
    public final /* synthetic */ Set f$4;
    public final /* synthetic */ String f$5;
    public final /* synthetic */ boolean f$6;
    public final /* synthetic */ String f$7;
    public final /* synthetic */ boolean f$8;
    public final /* synthetic */ boolean f$9;

    public /* synthetic */ FtProcessor$$ExternalSyntheticLambda7(FtProcessor ftProcessor, String str, String str2, Uri uri, Set set, String str3, boolean z, String str4, boolean z2, boolean z3, FileDisposition fileDisposition, boolean z4, String str5) {
        this.f$0 = ftProcessor;
        this.f$1 = str;
        this.f$2 = str2;
        this.f$3 = uri;
        this.f$4 = set;
        this.f$5 = str3;
        this.f$6 = z;
        this.f$7 = str4;
        this.f$8 = z2;
        this.f$9 = z3;
        this.f$10 = fileDisposition;
        this.f$11 = z4;
        this.f$12 = str5;
    }

    public final Object call() {
        return this.f$0.lambda$attachFileToGroupChat$4(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12);
    }
}

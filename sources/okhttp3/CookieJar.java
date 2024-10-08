package okhttp3;

import com.sec.internal.constants.ims.ImsConstants;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CookieJar.kt */
public interface CookieJar {
    @NotNull
    public static final Companion Companion = Companion.$$INSTANCE;
    @NotNull
    public static final CookieJar NO_COOKIES = new Companion.NoCookies();

    @NotNull
    List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl);

    void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list);

    /* compiled from: CookieJar.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        private Companion() {
        }

        /* compiled from: CookieJar.kt */
        private static final class NoCookies implements CookieJar {
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
                Intrinsics.checkNotNullParameter(list, "cookies");
            }

            @NotNull
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
                return CollectionsKt__CollectionsKt.emptyList();
            }
        }
    }
}

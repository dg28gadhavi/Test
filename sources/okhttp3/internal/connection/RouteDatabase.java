package okhttp3.internal.connection;

import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Route;
import org.jetbrains.annotations.NotNull;

/* compiled from: RouteDatabase.kt */
public final class RouteDatabase {
    @NotNull
    private final Set<Route> failedRoutes = new LinkedHashSet();

    public final synchronized void failed(@NotNull Route route) {
        Intrinsics.checkNotNullParameter(route, "failedRoute");
        this.failedRoutes.add(route);
    }

    public final synchronized void connected(@NotNull Route route) {
        Intrinsics.checkNotNullParameter(route, "route");
        this.failedRoutes.remove(route);
    }

    public final synchronized boolean shouldPostpone(@NotNull Route route) {
        Intrinsics.checkNotNullParameter(route, "route");
        return this.failedRoutes.contains(route);
    }
}

package kotlin.random;

import org.jetbrains.annotations.NotNull;

/* compiled from: PlatformRandom.kt */
public final class FallbackThreadLocalRandom extends AbstractPlatformRandom {
    @NotNull
    private final FallbackThreadLocalRandom$implStorage$1 implStorage = new FallbackThreadLocalRandom$implStorage$1();
}

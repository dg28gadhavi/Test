package com.sec.internal.ims.servicemodules.csh.event;

import android.view.Surface;

public class VideoDisplay implements IVideoDisplay {
    private final int mColor;
    private final Surface mWindowHandle;

    public VideoDisplay(Surface surface, int i) {
        this.mWindowHandle = surface;
        this.mColor = i;
    }

    public Surface getWindowHandle() {
        return this.mWindowHandle;
    }

    public int getColor() {
        return this.mColor;
    }
}

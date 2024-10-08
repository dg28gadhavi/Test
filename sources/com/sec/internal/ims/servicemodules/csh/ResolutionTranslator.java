package com.sec.internal.ims.servicemodules.csh;

import com.sec.internal.ims.servicemodules.csh.event.VshResolution;
import java.util.HashMap;

public class ResolutionTranslator {
    private static final int CIF_HEIGHT = 288;
    private static final int CIF_WIDTH = 352;
    private static final int QCIF_HEIGHT = 144;
    private static final int QCIF_WIDTH = 176;
    private static final int QVGA_HEIGHT = 240;
    private static final int QVGA_WIDTH = 320;
    private static final int VGA_HEIGHT = 480;
    private static final int VGA_WIDTH = 640;
    private static final HashMap<VshResolution, Integer[]> translate;

    static {
        HashMap<VshResolution, Integer[]> hashMap = new HashMap<>();
        translate = hashMap;
        VshResolution vshResolution = VshResolution.CIF;
        Integer valueOf = Integer.valueOf(CIF_WIDTH);
        hashMap.put(vshResolution, new Integer[]{valueOf, 288});
        hashMap.put(VshResolution.CIF_PORTRAIT, new Integer[]{288, valueOf});
        hashMap.put(VshResolution.QCIF, new Integer[]{176, 144});
        hashMap.put(VshResolution.QCIF_PORTRAIT, new Integer[]{144, 176});
        VshResolution vshResolution2 = VshResolution.VGA;
        Integer valueOf2 = Integer.valueOf(VGA_WIDTH);
        hashMap.put(vshResolution2, new Integer[]{valueOf2, 480});
        hashMap.put(VshResolution.VGA_PORTRAIT, new Integer[]{480, valueOf2});
        VshResolution vshResolution3 = VshResolution.QVGA;
        Integer valueOf3 = Integer.valueOf(QVGA_WIDTH);
        hashMap.put(vshResolution3, new Integer[]{valueOf3, 240});
        hashMap.put(VshResolution.QVGA_PORTRAIT, new Integer[]{240, valueOf3});
        hashMap.put(VshResolution.NONE, new Integer[]{0, 0});
    }

    public static int getWidth(VshResolution vshResolution) {
        return translate.get(vshResolution)[0].intValue();
    }

    public static int getHeight(VshResolution vshResolution) {
        return translate.get(vshResolution)[1].intValue();
    }
}

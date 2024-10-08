package com.sec.internal.ims.core.handler.secims;

import android.util.LongSparseArray;
import android.util.Pair;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.ims.XmlElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XmlDataStructureWrapper {
    private static final String CAP_AVAILABLE = "true";
    private static final String CAP_SUPPORTED = "supported";
    private static final String CAP_UNAVAILABLE = "false";
    private static final String DEVICE_CAP_MOBILITY = "mobility";
    private static final String LANGUAGE = "lang";
    private static final String LOG_TAG = "XDM-WRAPPER";
    private static final String MEDIA_CAP_AUDIO = "audio";
    private static final String MEDIA_CAP_DUPLEX = "duplex";
    private static final String MEDIA_CAP_FULL_DUPLEX = "full";
    private static final String MEDIA_CAP_VIDEO = "video";
    private static final String XML_NS = "xml";
    private static LongSparseArray<List<XmlElement>> mMediaCapabilities;

    private XmlDataStructureWrapper() {
    }

    public static List<XmlElement> getTextElements(String str, List<Pair<String, String>> list) {
        ArrayList arrayList = new ArrayList();
        for (Pair next : list) {
            if (next.first != null) {
                arrayList.add(new XmlElement(str, (String) next.second).addAttribute(LANGUAGE, (String) next.first, "xml"));
            } else {
                arrayList.add(new XmlElement(str, (String) next.second));
            }
        }
        return arrayList;
    }

    public static List<XmlElement> getMediaCapabilityElements(long j) {
        long j2 = (long) ((int) j);
        return mMediaCapabilities.get(j2) == null ? new ArrayList() : mMediaCapabilities.get(j2);
    }

    static {
        LongSparseArray<List<XmlElement>> longSparseArray = new LongSparseArray<>();
        mMediaCapabilities = longSparseArray;
        longSparseArray.put((long) Capabilities.FEATURE_MMTEL, Arrays.asList(new XmlElement[]{new XmlElement(MEDIA_CAP_AUDIO, "true"), new XmlElement("video", "false"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_MMTEL_VIDEO, Arrays.asList(new XmlElement[]{new XmlElement(MEDIA_CAP_AUDIO, "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL, Arrays.asList(new XmlElement[]{new XmlElement(MEDIA_CAP_AUDIO, "true"), new XmlElement("video", "false"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL_VIDEO, Arrays.asList(new XmlElement[]{new XmlElement(MEDIA_CAP_AUDIO, "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
        mMediaCapabilities.put((long) Capabilities.FEATURE_IPCALL_VIDEO_ONLY, Arrays.asList(new XmlElement[]{new XmlElement(MEDIA_CAP_AUDIO, "true"), new XmlElement("video", "true"), new XmlElement(MEDIA_CAP_FULL_DUPLEX).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(MEDIA_CAP_DUPLEX))}));
    }

    public static List<XmlElement> getDeviceCapabilityElements(List<String> list) {
        ArrayList arrayList = new ArrayList();
        for (String xmlElement : list) {
            arrayList.add(new XmlElement(xmlElement).setParent(new XmlElement(CAP_SUPPORTED)).setParent(new XmlElement(DEVICE_CAP_MOBILITY)));
        }
        return arrayList;
    }
}

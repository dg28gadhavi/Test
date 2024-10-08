package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.helper.Iso8601;

public class GlsXmlComposer {
    public String compose(GlsData glsData) throws NullPointerException {
        if (glsData != null) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<rcsenvelope xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:geolocation\"" + " xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + " xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"" + " xmlns:gml=\"http://www.opengis.net/gml\"" + " xmlns:gs=\"http://www.opengis.net/pidflo/1.0\"" + " entity=\"" + glsData.getSender().toString() + "\">" + addRcsPushLocation(glsData) + addValidityDate(glsData) + "<gp:geopriv>" + "<gp:location-info>" + "<gs:Circle srsName=\"urn:ogc:def:crs:EPSG::4326\">" + "<gml:pos>" + addPosition(glsData.getLocation()) + "</gml:pos>" + "<gs:radius uom=\"urn:ogc:def:uom:EPSG::9001\">" + glsData.getLocation().getAccuracy() + "</gs:radius>" + "</gs:Circle>" + "</gp:location-info>" + addValidityDateUsageRules(glsData) + "</gp:geopriv>" + "<timestamp>" + Iso8601.format(glsData.getDate()) + "</timestamp>" + "</rcspushlocation>" + "</rcsenvelope>";
        }
        throw new NullPointerException("GlsData is null");
    }

    private static String addPosition(Location location) {
        return location.getLatitude() + " " + location.getLongitude();
    }

    private static String addValidityDate(GlsData glsData) {
        if (glsData.getValidityDate() == null) {
            return "";
        }
        return "<rpid:time-offset rpid:until=\"" + Iso8601.format(glsData.getValidityDate().getValidityDate()) + "\">" + glsData.getValidityDate().getTimeZone() + "</rpid:time-offset>";
    }

    private static String addValidityDateUsageRules(GlsData glsData) {
        if (glsData.getValidityDate() == null) {
            return "";
        }
        return "<gp:usage-rules>" + "<gp:retention-expiry>" + Iso8601.format(glsData.getValidityDate().getValidityDate()) + "</gp:retention-expiry>" + "</gp:usage-rules>";
    }

    private static String addRcsPushLocation(GlsData glsData) {
        StringBuilder sb = new StringBuilder();
        sb.append("<rcspushlocation id=\"");
        sb.append(glsData.getId());
        if (glsData.getLocationType().equals(LocationType.OWN_LOCATION)) {
            sb.append("\">");
        } else if (glsData.getLabel() != null) {
            sb.append("\" label=\"");
            sb.append(glsData.getLabel());
            sb.append("\">");
        } else {
            sb.append("\" label=\"\">");
        }
        return sb.toString();
    }
}

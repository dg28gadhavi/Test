package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.GeoLocation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class GlsXmlParser {
    private static final String LOG_TAG = "GlsXmlParser";
    private XPathExpression mDatePath;
    private DocumentBuilder mDocumentBuilder;
    private XPathExpression mEntityPath;
    private XPathExpression mIdPath;
    private XPathExpression mLabelPath;
    private XPathExpression mLocationPath;
    private XPathExpression mPointLocationPath;
    private XPathExpression mRadiusPath;
    private XPathExpression mValidityDatePath;
    private XPathExpression mValidityTimezonePath;
    private XPath mXpath;

    public GlsXmlParser() {
        try {
            this.mDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mXpath = XPathFactory.newInstance().newXPath();
        this.mEntityPath = createXpathLazy("rcsenvelope/@entity");
        this.mIdPath = createXpathLazy("rcsenvelope/rcspushlocation/@id");
        this.mLabelPath = createXpathLazy("rcsenvelope/rcspushlocation/@label");
        this.mLocationPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Circle/pos");
        this.mRadiusPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Circle/radius");
        this.mPointLocationPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Point/pos");
        this.mDatePath = createXpathLazy("rcsenvelope/rcspushlocation/timestamp");
        this.mValidityDatePath = createXpathLazy("rcsenvelope/rcspushlocation/time-offset/@until");
        this.mValidityTimezonePath = createXpathLazy("rcsenvelope/rcspushlocation/time-offset");
    }

    public GlsData parse(String str) throws Exception {
        LocationType locationType;
        String[] strArr;
        GlsValidityTime glsValidityTime;
        int i;
        Document parse = this.mDocumentBuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8")));
        String extractString = extractString(this.mEntityPath, parse);
        verifyNotEmpty(extractString, "entity");
        ImsUri parse2 = ImsUri.parse(extractString);
        String extractString2 = extractString(this.mIdPath, parse);
        verifyNotEmpty(extractString2, "id");
        String extractString3 = extractString(this.mLabelPath, parse);
        if (extractString3 != null) {
            locationType = LocationType.OTHER_LOCATION;
        } else {
            locationType = LocationType.OWN_LOCATION;
        }
        LocationType locationType2 = locationType;
        String extractString4 = extractString(this.mPointLocationPath, parse);
        String extractString5 = extractString(this.mLocationPath, parse);
        Date date = null;
        double d = 0.0d;
        if (extractString4 != null && !extractString4.isEmpty()) {
            strArr = extractString4.split(" ");
        } else if (extractString5 == null || extractString5.isEmpty()) {
            Log.i(LOG_TAG, "Other type location, error!");
            strArr = null;
        } else {
            strArr = extractString5.split(" ");
            String extractString6 = extractString(this.mRadiusPath, parse);
            verifyNotEmpty(extractString6, "radiusStr");
            d = Double.valueOf(extractString6).doubleValue();
        }
        if (strArr == null || strArr.length != 2) {
            throw new Exception("Could not parse location string: " + extractString5);
        }
        double doubleValue = Double.valueOf(strArr[0]).doubleValue();
        double doubleValue2 = Double.valueOf(strArr[1]).doubleValue();
        Location location = new Location("passive");
        location.setLatitude(doubleValue);
        location.setLongitude(doubleValue2);
        location.setAccuracy((float) d);
        String extractString7 = extractString(this.mDatePath, parse);
        verifyNotEmpty(extractString7, "dateString");
        Date parse3 = Iso8601.parse(extractString7);
        String extractString8 = extractString(this.mValidityDatePath, parse);
        String extractString9 = extractString(this.mValidityTimezonePath, parse);
        if (extractString8 == null) {
            glsValidityTime = null;
        } else {
            try {
                date = Iso8601.parse(extractString8);
                i = Integer.valueOf(extractString9).intValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                i = 0;
            } catch (IOException e2) {
                throw e2;
            } catch (SAXException e3) {
                throw e3;
            } catch (XPathExpressionException e4) {
                throw e4;
            } catch (URISyntaxException e5) {
                throw e5;
            } catch (ParseException e6) {
                throw e6;
            } catch (Exception e7) {
                throw e7;
            }
            glsValidityTime = new GlsValidityTime(date, i);
        }
        return new GlsData(extractString2, parse2, location, locationType2, parse3, extractString3, glsValidityTime);
    }

    public String getGeolocString(String str) throws Exception {
        long j;
        GlsData parse = parse(str);
        GlsValidityTime validityDate = parse.getValidityDate();
        Location location = parse.getLocation();
        String label = parse.getLabel();
        if (validityDate == null || validityDate.getValidityDate() == null) {
            j = parse.getDate().getTime();
        } else {
            j = validityDate.getValidityDate().getTime();
        }
        return location.getLatitude() + CmcConstants.E_NUM_SLOT_SPLIT + location.getLongitude() + CmcConstants.E_NUM_SLOT_SPLIT + location.getAccuracy() + CmcConstants.E_NUM_SLOT_SPLIT + j + CmcConstants.E_NUM_SLOT_SPLIT + label;
    }

    public String getGlsExtInfo(String str) {
        long j;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "body=" + str);
        try {
            GlsData parse = parse(str);
            GlsValidityTime validityDate = parse.getValidityDate();
            Location location = parse.getLocation();
            LocationType locationType = parse.getLocationType();
            String label = locationType == LocationType.OWN_LOCATION ? "" : parse.getLabel();
            if (validityDate != null) {
                if (validityDate.getValidityDate() != null) {
                    j = validityDate.getValidityDate().getTime();
                    return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + j + "," + label + "," + locationType.toString();
                }
            }
            j = parse.getDate().getTime();
            return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + j + "," + label + "," + locationType.toString();
        } catch (Exception e) {
            IMSLog.s(LOG_TAG, e.toString());
            return null;
        }
    }

    private XPathExpression createXpathLazy(String str) {
        try {
            return this.mXpath.compile(str);
        } catch (XPathExpressionException e) {
            throw new Error(e);
        }
    }

    private static String extractString(XPathExpression xPathExpression, Document document) throws XPathExpressionException {
        Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }

    private static void verifyNotEmpty(String str, String str2) throws Exception {
        if (str == null || str.isEmpty()) {
            throw new Exception(str2 + " is empty!");
        }
    }

    public String getGeoJson(String str) throws Exception {
        long j;
        int i;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "body = " + str);
        GlsData parse = parse(str);
        Location location = parse.getLocation();
        Date date = parse.getDate();
        GlsValidityTime validityDate = parse.getValidityDate();
        if (validityDate == null || validityDate.getValidityDate() == null) {
            j = date.getTime();
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            i = (-(gregorianCalendar.get(15) + gregorianCalendar.get(16))) / ScheduleConstant.UPDATE_SUBSCRIPTION_DELAY_TIME;
        } else {
            j = validityDate.getValidityDate().getTime();
            i = validityDate.getTimeZone();
        }
        return new Gson().toJson(new GeoLocation(parse.getLabel(), String.valueOf(date), String.valueOf(j), String.valueOf(i), new GeoLocation.Circle(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()), location.getAccuracy())));
    }
}

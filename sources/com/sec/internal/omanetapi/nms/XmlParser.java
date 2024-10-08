package com.sec.internal.omanetapi.nms;

import android.util.Log;
import com.sec.internal.omanetapi.nms.data.GroupState;
import com.sec.internal.omanetapi.nms.data.Part;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlParser {
    public static final String LOG_TAG = "XmlParser";
    public static final String TAG_GROUP_STATE = "groupstate";
    public static final String TAG_GROUP_STATE_ATTR_CONTRIBUTIONID = "contributionid";
    public static final String TAG_GROUP_STATE_ATTR_GROUP_TYPE = "group-type";
    public static final String TAG_GROUP_STATE_ATTR_LASTFOCUSSESSIONID = "lastfocussessionid";
    public static final String TAG_GROUP_STATE_ATTR_TIMESTAMP = "timestamp";
    public static final String TAG_PARTICIPANT = "participant";
    public static final String TAG_PARTICIPANT_ATTR_COMMADDR = "comm-addr";
    public static final String TAG_PARTICIPANT_ATTR_NAME = "name";
    public static final String TAG_PARTICIPANT_ATTR_ROLE = "role";

    public static GroupState parseGroupState(String str) {
        GroupState groupState = new GroupState();
        try {
            XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setInput(new StringReader(str));
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                if (eventType == 0) {
                    Log.i(LOG_TAG, "Start document");
                } else if (eventType == 2) {
                    String lowerCase = newPullParser.getName().toLowerCase(Locale.US);
                    String str2 = LOG_TAG;
                    Log.i(str2, "start tagName:" + lowerCase);
                    int attributeCount = newPullParser.getAttributeCount();
                    int i = 0;
                    if (TAG_GROUP_STATE.equals(lowerCase)) {
                        while (i < attributeCount) {
                            String attributeName = newPullParser.getAttributeName(i);
                            Locale locale = Locale.US;
                            String lowerCase2 = attributeName.toLowerCase(locale);
                            String lowerCase3 = newPullParser.getAttributeValue(i).toLowerCase(locale);
                            if ("timestamp".equals(lowerCase2)) {
                                groupState.timestamp = lowerCase3;
                            } else if (TAG_GROUP_STATE_ATTR_LASTFOCUSSESSIONID.equals(lowerCase2)) {
                                groupState.lastfocussessionid = lowerCase3;
                            } else if (TAG_GROUP_STATE_ATTR_GROUP_TYPE.equals(lowerCase2)) {
                                groupState.group_type = lowerCase3;
                            } else if (TAG_GROUP_STATE_ATTR_CONTRIBUTIONID.equals(lowerCase2)) {
                                groupState.contributionid = lowerCase3;
                            } else {
                                String str3 = LOG_TAG;
                                Log.e(str3, "Unknown attrName:" + lowerCase2);
                            }
                            i++;
                        }
                    } else if ("participant".equals(lowerCase)) {
                        Part part = new Part();
                        while (i < attributeCount) {
                            String attributeName2 = newPullParser.getAttributeName(i);
                            Locale locale2 = Locale.US;
                            String lowerCase4 = attributeName2.toLowerCase(locale2);
                            String lowerCase5 = newPullParser.getAttributeValue(i).toLowerCase(locale2);
                            PrintStream printStream = System.out;
                            printStream.println("attrname>" + lowerCase4 + ";attrValue>" + lowerCase5);
                            if ("name".equals(lowerCase4)) {
                                part.name = lowerCase5;
                            } else if (TAG_PARTICIPANT_ATTR_COMMADDR.equals(lowerCase4)) {
                                part.comm_addr = lowerCase5;
                            } else if (TAG_PARTICIPANT_ATTR_ROLE.equals(lowerCase4)) {
                                part.role = lowerCase5;
                            } else {
                                String str4 = LOG_TAG;
                                Log.e(str4, "Unknown attrName:" + lowerCase4);
                            }
                            i++;
                        }
                        groupState.participantList.add(part);
                    } else {
                        Log.e(str2, "Unknown tagName:" + lowerCase);
                    }
                } else if (eventType == 3) {
                    String lowerCase6 = newPullParser.getName().toLowerCase(Locale.US);
                    String str5 = LOG_TAG;
                    Log.i(str5, "end tagName:" + lowerCase6);
                } else if (eventType == 4) {
                    Log.e(LOG_TAG, "unhandled element");
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return groupState;
    }
}

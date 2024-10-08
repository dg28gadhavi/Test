package com.sec.internal.ims.core;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ConcurrentUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@SuppressLint({"NewApi"})
public class GeolocationController extends Handler implements IGeolocationController {
    protected static final int EVENT_EPDG_AVAILABLE = 5;
    protected static final int EVENT_SERVICE_STATE_CHANGED = 4;
    protected static final int EVENT_START_LOCATION_UPDATE = 1;
    protected static final int EVENT_START_PERIODIC_LOCATION_UPDATE = 3;
    protected static final int EVENT_STOP_LOCATION_UPDATE = 2;
    private static final String INTENT_EPDG_SSID_CHANGED = "com.sec.epdg.EPDG_SSID_CHANGED";
    private static final String INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD = "com.sec.internal.ims.imsservice.periodic_lu";
    private static final String INTENT_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
    private static final int LOCATION_REQUEST_TIMEOUT = 45000;
    private static final String LOG_TAG = "GeolocationCon";
    private static final int PERIODIC_LOCATION_TIME = 1800000;
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    protected String mCountryIso = "";
    private int[] mDataRegState;
    ContentObserver mDtLocUserConsentObserver = new ContentObserver(this) {
        public void onChange(boolean z, Uri uri) {
            int i = ImsSharedPrefHelper.getInt(-1, GeolocationController.this.mContext, "dtlocuserconsent", "dtlocation", -1);
            Log.i(GeolocationController.LOG_TAG, "onChange- dtlocuserconsent : " + i);
            for (int i2 = 0; i2 < GeolocationController.this.mTelephonyManager.getPhoneCount(); i2++) {
                if (GeolocationController.this.mIsLocationUserConsent[i2] != i) {
                    GeolocationController.this.mIsLocationUserConsent[i2] = i;
                    Mno simMno = SimUtil.getSimMno(i2);
                    if (simMno == Mno.TMOBILE || simMno == Mno.TMOBILE_NED) {
                        GeolocationController geolocationController = GeolocationController.this;
                        if (geolocationController.mIsEpdgAvaialble[i2]) {
                            geolocationController.mIsForceEpdgAvailUpdate[i2] = true;
                            SimpleEventLog simpleEventLog = GeolocationController.this.mEventLog;
                            simpleEventLog.add("DTLocUserConsent onChange(" + i2 + ") :mIsLocationUserConsent[" + GeolocationController.this.mIsLocationUserConsent[i2] + "]");
                            GeolocationController geolocationController2 = GeolocationController.this;
                            geolocationController2.sendMessage(geolocationController2.obtainMessage(5, i2, 1));
                        }
                    }
                }
            }
        }
    };
    SimpleEventLog mEventLog;
    LocationInfo mGeolocation = null;
    private GeolocationListener mGeolocationListener = null;
    private boolean mHasToRestoreLocationSetting = false;
    private final IntentListener mIntentListener;
    protected boolean[] mIsEpdgAvaialble;
    /* access modifiers changed from: private */
    public boolean[] mIsForceEpdgAvailUpdate;
    /* access modifiers changed from: private */
    public boolean mIsLocationEnabled = false;
    /* access modifiers changed from: private */
    public boolean mIsLocationEnabledToRestore = false;
    /* access modifiers changed from: private */
    public int[] mIsLocationUserConsent;
    private boolean mIsRequested = false;
    LocationManager mLocationManager;
    private Handler mLocationUpdateHandler;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private final RegistrationManagerBase mRegistrationManager;
    /* access modifiers changed from: private */
    public final ITelephonyManager mTelephonyManager;
    protected int[] mVoiceRegState;

    public GeolocationController(Context context, Looper looper, RegistrationManagerBase registrationManagerBase) {
        super(looper);
        this.mContext = context;
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(context);
        this.mTelephonyManager = instance;
        int phoneCount = instance.getPhoneCount();
        this.mRegistrationManager = registrationManagerBase;
        this.mVoiceRegState = new int[phoneCount];
        this.mLocationManager = (LocationManager) context.getSystemService(GlsIntent.Extras.EXTRA_LOCATION);
        this.mGeolocationListener = new GeolocationListener();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mDataRegState = new int[phoneCount];
        this.mIsEpdgAvaialble = new boolean[phoneCount];
        this.mIsLocationUserConsent = new int[phoneCount];
        this.mIsForceEpdgAvailUpdate = new boolean[phoneCount];
        Arrays.fill(this.mVoiceRegState, 1);
        Arrays.fill(this.mDataRegState, 1);
        Arrays.fill(this.mIsEpdgAvaialble, false);
        Arrays.fill(this.mIsLocationUserConsent, -1);
        Arrays.fill(this.mIsForceEpdgAvailUpdate, false);
        this.mIntentListener = new IntentListener();
        registerDtLocUserConsentObserver();
        setDtLocUserConsent();
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 20);
    }

    public void initSequentially() {
        Log.i(LOG_TAG, "initializing sequentially...");
        this.mIntentListener.init();
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        this.mLocationUpdateHandler = new Handler(handlerThread.getLooper());
    }

    public void handleMessage(Message message) {
        Log.i(LOG_TAG, "handleMessage : what = " + msgToString(message.what));
        int i = message.what;
        boolean z = false;
        if (i != 1) {
            if (i == 2) {
                releaseLocationUpdate();
                this.mIsRequested = false;
            } else if (i == 3) {
                startPeriodicLocationUpdate(message.arg1);
            } else if (i == 4) {
                onServiceStateChanged(message.arg1, (ServiceStateWrapper) message.obj);
            } else if (i == 5) {
                int i2 = message.arg1;
                if (message.arg2 == 1) {
                    z = true;
                }
                onEpdgAvailable(i2, z);
            }
        } else if (hasMessages(1)) {
        } else {
            if (this.mIsRequested) {
                Log.i(LOG_TAG, "Already Requested, Don't request location");
                return;
            }
            int i3 = message.arg1;
            this.mPhoneId = i3;
            if (message.arg2 == 1) {
                z = true;
            }
            this.mLocationUpdateHandler.post(new GeolocationController$$ExternalSyntheticLambda6(this, i3, z));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$handleMessage$0(int i, boolean z) {
        this.mIsRequested = requestLocationUpdate(i, z);
    }

    public boolean startGeolocationUpdate(int i, boolean z) {
        return startGeolocationUpdate(i, z, 0);
    }

    public boolean startGeolocationUpdate(int i, boolean z, int i2) {
        Log.i(LOG_TAG, "startGeoLocationUpdate isEmergency = " + z);
        boolean z2 = (SimUtil.isSoftphoneEnabled() || z) ? true : !isValidLocationInfo(i, this.mGeolocation);
        if (z2) {
            sendMessageDelayed(obtainMessage(1, i, z ? 1 : 0), (long) i2);
        }
        return z2;
    }

    /* access modifiers changed from: package-private */
    public boolean requestLocationUpdate(int i, boolean z) {
        Log.i(LOG_TAG, "requestLocationUpdate : isEmergency = " + z);
        if (z || !getLocationFromLastKnown(i)) {
            return requestLocationToLocationManager(z);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean requestLocationToLocationManager(boolean z) {
        try {
            this.mLocationManager.requestLocationUpdates("fused", new LocationRequest.Builder(0).setMinUpdateIntervalMillis(0).setLocationSettingsIgnored(z).setQuality(100).build(), ConcurrentUtils.DIRECT_EXECUTOR, this.mGeolocationListener);
            sendMessageDelayed(obtainMessage(2), 45000);
            return true;
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void stopGeolocationUpdate() {
        Log.i(LOG_TAG, "stopGeolocationUpdate");
        sendEmptyMessage(2);
    }

    private void releaseLocationUpdate() {
        Log.e(LOG_TAG, "releaseLocationUpdate");
        try {
            this.mLocationManager.removeUpdates(this.mGeolocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            removeMessages(2);
            restoreLocationSettings();
            throw th;
        }
        removeMessages(2);
        restoreLocationSettings();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isValidLocationInfo(int r12, com.sec.internal.constants.ims.gls.LocationInfo r13) {
        /*
            r11 = this;
            r0 = 0
            r1 = 1
            if (r13 != 0) goto L_0x0008
            java.lang.String r2 = "geolocation null"
        L_0x0006:
            r3 = r0
            goto L_0x0037
        L_0x0008:
            java.lang.String r2 = r13.mLocationTime
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x0013
            java.lang.String r2 = "mLocationTime is empty"
            goto L_0x0006
        L_0x0013:
            java.lang.String r2 = r13.mCountry
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x001e
            java.lang.String r2 = "mCountry  is empty"
            goto L_0x0006
        L_0x001e:
            java.lang.String r2 = r13.mLatitude
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x0029
            java.lang.String r2 = "mLatitude  is empty"
            goto L_0x0006
        L_0x0029:
            java.lang.String r2 = r13.mA1
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 == 0) goto L_0x0034
            java.lang.String r2 = "mA1  is empty"
            goto L_0x0006
        L_0x0034:
            java.lang.String r2 = ""
            r3 = r1
        L_0x0037:
            java.lang.String r4 = "GeolocationCon"
            if (r3 != 0) goto L_0x0050
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "isValidLocation: "
            r11.append(r12)
            r11.append(r2)
            java.lang.String r11 = r11.toString()
            android.util.Log.i(r4, r11)
            return r0
        L_0x0050:
            long r5 = java.lang.System.currentTimeMillis()
            java.lang.String r13 = r13.mLocationTime
            long r7 = java.lang.Long.parseLong(r13)
            r9 = 1000(0x3e8, double:4.94E-321)
            long r7 = r7 * r9
            com.sec.internal.ims.core.RegistrationManagerBase r11 = r11.mRegistrationManager
            com.sec.ims.settings.ImsProfile$PROFILE_TYPE r13 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY
            com.sec.ims.settings.ImsProfile r11 = r11.getImsProfile(r12, r13)
            if (r11 == 0) goto L_0x006c
            int r11 = r11.getValidLocationTime()
            goto L_0x006d
        L_0x006c:
            r11 = r0
        L_0x006d:
            if (r11 <= 0) goto L_0x00ae
            long r12 = r5 - r7
            long r2 = (long) r11
            int r12 = (r12 > r2 ? 1 : (r12 == r2 ? 0 : -1))
            if (r12 > 0) goto L_0x0077
            r0 = r1
        L_0x0077:
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "isValidLocation(mGeolocation) ("
            r12.append(r13)
            r12.append(r11)
            java.lang.String r11 = "ms): "
            r12.append(r11)
            r12.append(r0)
            java.lang.String r11 = "(Current: "
            r12.append(r11)
            java.util.Date r11 = new java.util.Date
            r11.<init>(r5)
            r12.append(r11)
            java.lang.String r11 = ") (Loc. Info received: "
            r12.append(r11)
            java.util.Date r11 = new java.util.Date
            r11.<init>(r7)
            r12.append(r11)
            java.lang.String r11 = r12.toString()
            android.util.Log.i(r4, r11)
            r3 = r0
        L_0x00ae:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.GeolocationController.isValidLocationInfo(int, com.sec.internal.constants.ims.gls.LocationInfo):boolean");
    }

    /* access modifiers changed from: package-private */
    public boolean isValidLocation(int i, Location location) {
        int i2;
        int i3;
        int i4;
        if (location == null) {
            Log.e(LOG_TAG, "isValidLocation : location is null");
            return false;
        } else if (location.isFromMockProvider()) {
            Log.e(LOG_TAG, "isValidLocation : location from Mock Provider");
            this.mCountryIso = "";
            this.mGeolocation = null;
            this.mRegistrationManager.sendDeregister(41, i);
            return false;
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            long time = location.getTime();
            ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.EMERGENCY);
            if (imsProfile != null) {
                i4 = imsProfile.getValidLocationTime();
                i3 = imsProfile.getValidLocationAccuracy();
                i2 = imsProfile.getConfidenceLevel();
            } else {
                i2 = 0;
                i4 = 0;
                i3 = 0;
            }
            boolean z = true;
            if (i4 > 0) {
                if (currentTimeMillis - time > ((long) i4)) {
                    z = false;
                }
                Log.i(LOG_TAG, "isValidLocation(location) (" + i4 + "ms): " + z + "(Current: " + new Date(currentTimeMillis) + ") (Loc. Info received: " + new Date(time) + "from provider [" + location.getProvider() + "])");
            }
            if (i3 > 0) {
                float accuracy = location.getAccuracy();
                if (i2 == 90) {
                    accuracy *= 1.65f;
                }
                if (SimUtil.getSimMno(i) != Mno.VZW || accuracy < 1000.0f) {
                    return z;
                }
                return false;
            }
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public Location getLastKnownLocation() {
        Location location;
        String str;
        try {
            location = this.mLocationManager.getLastKnownLocation("fused");
        } catch (SecurityException e) {
            e.printStackTrace();
            location = null;
        }
        if (location == null) {
            try {
                location = this.mLocationManager.getLastKnownLocation("gps");
            } catch (SecurityException e2) {
                e2.printStackTrace();
            }
        }
        if (location == null) {
            try {
                location = this.mLocationManager.getLastKnownLocation("network");
            } catch (SecurityException e3) {
                e3.printStackTrace();
            }
        }
        if (location == null) {
            str = "can not find lastKnownLocation";
        } else {
            str = "lastKnownLocation from " + location.getProvider();
        }
        Log.i(LOG_TAG, str);
        return location;
    }

    /* access modifiers changed from: package-private */
    public void updateGeolocation(int i, String str) {
        LocationInfo constructData = GeoLocationUtility.constructData(str, getProvider((Location) null));
        if (constructData == null) {
            Log.i(LOG_TAG, "updateGeolocation(iso) : geolocation is null. Don't update and maintain previous one");
            return;
        }
        LocationInfo locationInfo = this.mGeolocation;
        if (locationInfo == null || !str.equalsIgnoreCase(locationInfo.mCountry)) {
            this.mGeolocation = constructData;
            Log.i(LOG_TAG, "updateGeolocation(iso) : mGeolocation = " + this.mGeolocation.toString());
            Mno simMno = SimUtil.getSimMno(i);
            this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, !simMno.isTeliaCo() && !simMno.isOneOf(Mno.VODAFONE_AUSTRALIA, Mno.CELLC_SOUTHAFRICA, Mno.GLOBE_PH, Mno.TMOUS));
            return;
        }
        Log.i(LOG_TAG, "updateGeolocation(iso) : iso is same as before. Don't update and maintain previous one");
    }

    /* access modifiers changed from: package-private */
    public void updateGeolocation(Location location) {
        LocationInfo locationInfo;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        int confidenceLevel = imsProfile != null ? imsProfile.getConfidenceLevel() : 68;
        if (location == null || !location.isFromMockProvider()) {
            boolean z = simMno != Mno.VZW;
            if (location == null) {
                locationInfo = GeoLocationUtility.constructData(this.mCountryIso, getProvider(location));
            } else {
                locationInfo = GeoLocationUtility.constructData(location, getProvider(location), this.mContext, z, confidenceLevel);
                if (locationInfo == null || !TextUtils.isEmpty(locationInfo.mCountry)) {
                    if (locationInfo == null) {
                        Log.i(LOG_TAG, "geolocation is null!");
                    } else if (simMno != Mno.TMOUS && !TextUtils.isEmpty(this.mCountryIso) && !this.mCountryIso.equalsIgnoreCase(locationInfo.mCountry)) {
                        locationInfo = GeoLocationUtility.constructData(this.mCountryIso, getProvider((Location) null));
                    }
                } else if (!TextUtils.isEmpty(this.mCountryIso)) {
                    locationInfo.mCountry = this.mCountryIso;
                } else if (TextUtils.isEmpty(locationInfo.mLatitude) || TextUtils.isEmpty(locationInfo.mLongitude)) {
                    locationInfo = null;
                } else {
                    Log.i(LOG_TAG, "updateGeolocation :  latitude = " + locationInfo.mLatitude + ", longitude = " + locationInfo.mLongitude);
                }
            }
            if (locationInfo == null) {
                Log.i(LOG_TAG, "updateGeolocation(loc) : geolocation is null. Don't update and maintain previous one");
                return;
            }
            storeLastAccessedCountry(this.mPhoneId, locationInfo.mCountry);
            this.mGeolocation = locationInfo;
            Log.i(LOG_TAG, "updateGeolocation(loc) : mGeolocation = " + this.mGeolocation.toString());
            this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, false);
            return;
        }
        Log.e(LOG_TAG, "ignore mock location");
    }

    private String getProvider(Location location) {
        String str;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if (simMno == Mno.ATT) {
            str = "Hybrid_A-GPS";
        } else if (simMno == Mno.TMOUS) {
            str = "DBH";
        } else {
            str = "DHCP";
            if (location != null) {
                if ("gps".equals(location.getProvider())) {
                    str = "GPS";
                } else if ("fused".equals(location.getProvider())) {
                    str = "FUSED";
                }
            }
        }
        Log.i(LOG_TAG, "getProvider=" + str);
        return str;
    }

    public boolean updateGeolocationFromLastKnown(int i) {
        Log.i(LOG_TAG, "updateGeolocationFromLastKnown");
        Location lastKnownLocation = getLastKnownLocation();
        if (isValidLocation(i, lastKnownLocation)) {
            IMSLog.c(LogClass.VOLTE_LAST_LOCATION_PRO, "" + i);
            updateGeolocation(lastKnownLocation);
            return true;
        } else if (!isValidLocationInfo(i, this.mGeolocation)) {
            return false;
        } else {
            IMSLog.c(LogClass.VOLTE_LAST_LOCATION_GEO, "" + i);
            this.mRegistrationManager.notifyGeolocationUpdate(this.mGeolocation, false);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getLocationFromLastKnown(int i) {
        enableLocationSettings();
        if (!updateGeolocationFromLastKnown(i)) {
            return false;
        }
        restoreLocationSettings();
        return true;
    }

    public LocationInfo getGeolocation() {
        return this.mGeolocation;
    }

    /* access modifiers changed from: package-private */
    public void enableLocationSettings() {
        this.mIsLocationEnabledToRestore = isLocationServiceEnabled();
        setLocationServiceEnabled(true);
        this.mHasToRestoreLocationSetting = true;
        this.mIsLocationEnabled = isLocationServiceEnabled();
        Log.i(LOG_TAG, "enableLocationSettings : restore = " + this.mIsLocationEnabledToRestore);
    }

    /* access modifiers changed from: package-private */
    public void restoreLocationSettings() {
        if (this.mHasToRestoreLocationSetting) {
            Log.i(LOG_TAG, "restoreLocationSettings : restore = " + this.mIsLocationEnabledToRestore);
            setLocationServiceEnabled(this.mIsLocationEnabledToRestore);
            this.mHasToRestoreLocationSetting = false;
            this.mIsLocationEnabled = isLocationServiceEnabled();
        }
    }

    public boolean isLocationServiceEnabled() {
        boolean isLocationEnabledForUser = this.mLocationManager.isLocationEnabledForUser(UserHandle.SEM_CURRENT);
        Log.i(LOG_TAG, "isLocationServiceEnabled : " + isLocationEnabledForUser);
        return isLocationEnabledForUser;
    }

    /* access modifiers changed from: protected */
    public void setLocationServiceEnabled(boolean z) {
        this.mLocationManager.setLocationEnabledForUser(z, UserHandle.SEM_CURRENT);
    }

    /* access modifiers changed from: protected */
    public String getNetworkCountryIso() {
        return this.mTelephonyManager.getNetworkCountryIso();
    }

    /* access modifiers changed from: protected */
    public BroadcastReceiver getReceiver() {
        return this.mIntentListener.mReceiver;
    }

    /* access modifiers changed from: protected */
    public LocationListener getListener() {
        return this.mGeolocationListener;
    }

    public boolean isCountryCodeLoaded(int i) {
        if (this.mGeolocation == null) {
            return false;
        }
        Mno simMno = SimUtil.getSimMno(i);
        if (simMno == Mno.SPRINT && this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(i)) != 13 && !isValidLocationInfo(i, this.mGeolocation)) {
            Log.i(LOG_TAG, "isCountryCodeLoaded : location expired, return false");
            this.mGeolocation = null;
            this.mCountryIso = "";
            return false;
        } else if (simMno != Mno.TMOUS) {
            return !TextUtils.isEmpty(this.mGeolocation.mCountry);
        } else {
            String str = this.mGeolocation.mCountry;
            if (TextUtils.isEmpty(str)) {
                return false;
            }
            Locale locale = Locale.US;
            if (TextUtils.equals(str.toUpperCase(locale), this.mCountryIso.toUpperCase(locale))) {
                return true;
            }
            return false;
        }
    }

    public String getLastAccessedNetworkCountryIso(int i) {
        if (!TextUtils.isEmpty(this.mCountryIso)) {
            IMSLog.i(LOG_TAG, i, "getLastAccessedNetworkCountryIso: networkCountryIso: " + this.mCountryIso);
            return this.mCountryIso;
        }
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, this.mContext, ImsSharedPrefHelper.LAST_ACCESSED_COUNTRY_ISO, 0, false);
        if (sharedPref == null) {
            IMSLog.i(LOG_TAG, i, "getLastAccessedNetworkCountryIso: Not accessed yet");
            return "";
        }
        String string = sharedPref.getString("cc", "");
        IMSLog.i(LOG_TAG, i, "getLastAccessedNetworkCountryIso: last accessed: " + string + ", timestamp: " + new Date(sharedPref.getLong("timestamp", 0)));
        return string;
    }

    private PendingIntent getRetryRequestLocationIntent(int i) {
        Intent intent = new Intent(this.mContext, GeolocationController.class);
        intent.setAction(INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD);
        intent.putExtra("phoneId", i);
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
    }

    /* access modifiers changed from: private */
    public void startPeriodicLocationUpdate(int i) {
        Log.i(LOG_TAG, "startPeriodicLocationUpdate(" + i + "), mIsEpdgAvaialble: " + this.mIsEpdgAvaialble[i] + " mVoiceRegState: " + this.mVoiceRegState[i]);
        if (this.mIsEpdgAvaialble[i] && this.mVoiceRegState[i] != 0) {
            this.mAlarmManager.cancel(getRetryRequestLocationIntent(i));
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + 900000, getRetryRequestLocationIntent(i));
        }
    }

    public void stopPeriodicLocationUpdate(int i) {
        Log.i(LOG_TAG, "stopPeriodicLocationUpdate(" + i + ")");
        if (isNeedRequestLocation(i, 4)) {
            this.mAlarmManager.cancel(getRetryRequestLocationIntent(i));
        }
    }

    private class GeolocationListener implements LocationListener {
        public void onProviderDisabled(String str) {
        }

        public void onProviderEnabled(String str) {
        }

        private GeolocationListener() {
        }

        public void onLocationChanged(Location location) {
            Log.i(GeolocationController.LOG_TAG, "onLocationChanged : location = " + IMSLog.checker(location));
            if (location != null) {
                GeolocationController geolocationController = GeolocationController.this;
                if (geolocationController.isValidLocation(geolocationController.mPhoneId, location)) {
                    try {
                        Log.i(GeolocationController.LOG_TAG, "onLocationChanged : removing location listener");
                        IMSLog.c(LogClass.VOLTE_UPDATE_LOCATION_PRO, "" + location.getProvider());
                        GeolocationController.this.updateGeolocation(location);
                        GeolocationController.this.sendEmptyMessage(2);
                    } catch (IllegalArgumentException e) {
                        IMSLog.s(GeolocationController.LOG_TAG, "onLocationChanged ex: " + e.getMessage());
                    }
                }
            }
        }
    }

    private String msgToString(int i) {
        if (i == 1) {
            return "START_LOCATION_UPDATE";
        }
        if (i == 2) {
            return "STOP_LOCATION_UPDATE";
        }
        if (i == 3) {
            return "START_PERIODIC_LOCATION_UPDATE";
        }
        if (i == 4) {
            return "SERVICE_STATE_CHANGED";
        }
        if (i == 5) {
            return "EPDG_AVAILABLE";
        }
        return "UNKNOWN(" + i + ")";
    }

    private class IntentListener {
        /* access modifiers changed from: private */
        public final BroadcastReceiver mReceiver;

        private IntentListener() {
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.i(GeolocationController.LOG_TAG, "Received Intent : " + action);
                    int intExtra = intent.getIntExtra("phoneId", 0);
                    if (GeolocationController.INTENT_EPDG_SSID_CHANGED.equals(action)) {
                        if (GeolocationController.this.isNeedRequestLocation(intExtra, 2)) {
                            GeolocationController geolocationController = GeolocationController.this;
                            if (geolocationController.mVoiceRegState[intExtra] != 0) {
                                geolocationController.mGeolocation = null;
                            }
                        }
                    } else if (GeolocationController.INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD.equals(action)) {
                        GeolocationController geolocationController2 = GeolocationController.this;
                        geolocationController2.sendMessage(geolocationController2.obtainMessage(1, intExtra, 0));
                        GeolocationController.this.startPeriodicLocationUpdate(intExtra);
                    } else if (GeolocationController.INTENT_PROVIDERS_CHANGED.equals(action)) {
                        boolean isLocationServiceEnabled = GeolocationController.this.isLocationServiceEnabled();
                        Log.i(GeolocationController.LOG_TAG, "prev loc : " + GeolocationController.this.mIsLocationEnabled + ", cur loc : " + isLocationServiceEnabled);
                        if (GeolocationController.this.mIsLocationEnabled != isLocationServiceEnabled) {
                            GeolocationController.this.mIsLocationEnabled = isLocationServiceEnabled;
                            GeolocationController.this.mIsLocationEnabledToRestore = isLocationServiceEnabled;
                        }
                    } else if ("android.telephony.action.NETWORK_COUNTRY_CHANGED".equals(action)) {
                        int intExtra2 = intent.getIntExtra(PhoneConstants.PHONE_KEY, -1);
                        String stringExtra = intent.getStringExtra("android.telephony.extra.LAST_KNOWN_NETWORK_COUNTRY");
                        IMSLog.i(GeolocationController.LOG_TAG, intExtra2, "Network country code changed: countryIso: " + stringExtra);
                        if (intExtra2 != -1 && !TextUtils.isEmpty(stringExtra)) {
                            Mno mno = SimUtil.getMno(intExtra2);
                            if (mno.isOneOf(Mno.TMOUS, Mno.VZW)) {
                                GeolocationController.this.onNetworkCountryIsoChanged(intExtra2, mno, stringExtra);
                            }
                        }
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(GeolocationController.INTENT_EPDG_SSID_CHANGED);
            intentFilter.addAction(GeolocationController.INTENT_PERIODIC_LOCATION_UPDATE_TIMER_EXPD);
            intentFilter.addAction(GeolocationController.INTENT_PROVIDERS_CHANGED);
            intentFilter.addAction("android.telephony.action.NETWORK_COUNTRY_CHANGED");
            GeolocationController.this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    public void notifyServiceStateChanged(int i, ServiceStateWrapper serviceStateWrapper) {
        sendMessage(obtainMessage(4, i, 0, serviceStateWrapper));
    }

    /* access modifiers changed from: package-private */
    public void onServiceStateChanged(int i, ServiceStateWrapper serviceStateWrapper) {
        Log.d(LOG_TAG, "onServiceStateChanged(" + serviceStateWrapper + ")");
        Mno simMno = SimUtil.getSimMno(i);
        if (simMno != Mno.SPRINT || NetworkUtil.is3gppPsVoiceNetwork(serviceStateWrapper.getDataNetworkType())) {
            if (isNeedRequestLocation(i, 4)) {
                if (this.mVoiceRegState[i] == 0 && serviceStateWrapper.getVoiceRegState() != 0) {
                    sendMessageDelayed(obtainMessage(3, Integer.valueOf(i)), 1800000);
                } else if (this.mVoiceRegState[i] != 0 && serviceStateWrapper.getVoiceRegState() == 0) {
                    stopPeriodicLocationUpdate(i);
                }
            }
            this.mVoiceRegState[i] = serviceStateWrapper.getVoiceRegState();
            this.mDataRegState[i] = serviceStateWrapper.getDataRegState();
            if (this.mVoiceRegState[i] == 0 || this.mDataRegState[i] == 0) {
                onNetworkCountryIsoChanged(i, simMno, getNetworkCountryIso());
                return;
            }
            long longValue = ((Long) Optional.ofNullable(ImsSharedPrefHelper.getSharedPref(i, this.mContext, ImsSharedPrefHelper.LAST_ACCESSED_COUNTRY_ISO, 0, false)).map(new GeolocationController$$ExternalSyntheticLambda4()).orElse(0L)).longValue();
            if (longValue == 0) {
                IMSLog.i(LOG_TAG, i, "getLastAccessedNetworkCountryIso: Not accessed yet");
                this.mCountryIso = "";
                return;
            }
            IMSLog.i(LOG_TAG, i, "getLastAccessedNetworkCountryIso: last accessed: " + new Date(longValue));
            if (System.currentTimeMillis() - longValue > ((long) ((Integer) Optional.ofNullable(this.mRegistrationManager.getEmergencyProfile(this.mPhoneId)).map(new GeolocationController$$ExternalSyntheticLambda5()).orElse(0)).intValue())) {
                this.mCountryIso = "";
                return;
            }
            return;
        }
        Log.e(LOG_TAG, "ignore phone state listener");
    }

    /* access modifiers changed from: private */
    public void onNetworkCountryIsoChanged(int i, Mno mno, String str) {
        IMSLog.i(LOG_TAG, i, "onNetworkCountryIsoChanged: mCountryIso = " + this.mCountryIso + ", iso = " + str);
        if (!TextUtils.isEmpty(str) && !this.mCountryIso.equalsIgnoreCase(str)) {
            if (mno.isOneOf(Mno.TMOUS, Mno.VZW)) {
                storeLastAccessedCountry(i, str);
            }
            this.mCountryIso = str;
            if (!SimUtil.isSoftphoneEnabled()) {
                updateGeolocation(i, this.mCountryIso);
            }
        }
    }

    private void storeLastAccessedCountry(int i, String str) {
        Optional.ofNullable(ImsSharedPrefHelper.getSharedPref(i, this.mContext, ImsSharedPrefHelper.LAST_ACCESSED_COUNTRY_ISO, 0, false)).map(new GeolocationController$$ExternalSyntheticLambda2()).ifPresent(new GeolocationController$$ExternalSyntheticLambda3(str));
    }

    public void notifyEpdgAvailable(int i, int i2) {
        sendMessage(obtainMessage(5, i, i2));
    }

    public void onEpdgAvailable(int i, boolean z) {
        Log.i(LOG_TAG, "setEpdgAvailable : phoneId : " + i + ", prevEpdgState =  " + this.mIsEpdgAvaialble[i] + " curEpdgState : " + z + " mIsForceEpdgAvailUpdate :" + this.mIsForceEpdgAvailUpdate[i]);
        boolean isOneOf = SimUtil.getSimMno(i).isOneOf(Mno.TMOBILE, Mno.TMOBILE_NED);
        if (this.mIsForceEpdgAvailUpdate[i] || z != this.mIsEpdgAvaialble[i]) {
            this.mIsEpdgAvaialble[i] = z;
            if (isNeedRequestLocation(i, 2)) {
                this.mIsForceEpdgAvailUpdate[i] = false;
                if (!this.mIsEpdgAvaialble[i]) {
                    sendEmptyMessage(2);
                    if (isNeedRequestLocation(i, 4)) {
                        stopPeriodicLocationUpdate(i);
                        if (this.mVoiceRegState[i] != 0) {
                            this.mGeolocation = null;
                        }
                    }
                } else if (!isOneOf || this.mIsLocationUserConsent[i] == 1) {
                    sendMessage(obtainMessage(1, i, 0));
                    if (isNeedRequestLocation(i, 4)) {
                        sendMessageDelayed(obtainMessage(3), 45000);
                    }
                }
            }
        }
    }

    private void registerDtLocUserConsentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.sec.ims.settings/dtlocuserconsent"), true, this.mDtLocUserConsentObserver);
    }

    private void setDtLocUserConsent() {
        int i = ImsSharedPrefHelper.getInt(-1, this.mContext, "dtlocuserconsent", "dtlocation", -1);
        Log.i(LOG_TAG, "setDtLocUserConsent- dtlocuserconsent : " + i);
        for (int i2 = 0; i2 < this.mTelephonyManager.getPhoneCount(); i2++) {
            this.mIsLocationUserConsent[i2] = i;
        }
    }

    public void dump() {
        this.mEventLog.dump();
    }

    /* access modifiers changed from: protected */
    /* renamed from: matchTimingReqLocation */
    public boolean lambda$isNeedRequestLocation$3(ImsProfile imsProfile, int i) {
        int requestLocationTiming = imsProfile.getRequestLocationTiming();
        Log.i(LOG_TAG, "matchTimingReqLocation ,match=" + requestLocationTiming + ", timing=" + i);
        return (requestLocationTiming & i) == i;
    }

    /* access modifiers changed from: private */
    public boolean isNeedRequestLocation(int i, int i2) {
        if (((ImsProfile) Arrays.stream(this.mRegistrationManager.getProfileList(i)).filter(new GeolocationController$$ExternalSyntheticLambda0()).filter(new GeolocationController$$ExternalSyntheticLambda1(this, i2)).findFirst().orElse((Object) null)) != null) {
            return true;
        }
        Log.i(LOG_TAG, "profile is null");
        return false;
    }
}

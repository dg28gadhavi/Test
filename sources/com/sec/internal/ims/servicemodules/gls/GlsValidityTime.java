package com.sec.internal.ims.servicemodules.gls;

import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import java.util.Date;
import java.util.GregorianCalendar;

public class GlsValidityTime {
    private final int mTimeZone;
    private final Date mValidityDate;

    public GlsValidityTime(Date date) {
        this.mValidityDate = date;
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        this.mTimeZone = (-(gregorianCalendar.get(15) + gregorianCalendar.get(16))) / ScheduleConstant.UPDATE_SUBSCRIPTION_DELAY_TIME;
    }

    public GlsValidityTime(Date date, int i) {
        this.mValidityDate = date;
        this.mTimeZone = i;
    }

    public Date getValidityDate() {
        return this.mValidityDate;
    }

    public int getTimeZone() {
        return this.mTimeZone;
    }

    public String toString() {
        return "Validity DateTime(" + "date=" + this.mValidityDate.toString() + ", time zone=" + this.mTimeZone + ')';
    }
}

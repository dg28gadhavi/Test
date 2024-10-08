package com.sec.internal.helper.os;

import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import com.sec.internal.log.IMSLog;

public class CellIdentityWrapper {
    public static final CellIdentityWrapper DEFAULT = from((CellIdentity) null);
    private final int mAreaCode;
    private final long mCellId;
    private final CellType mCellType;

    enum CellType {
        UNKNOWN((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return false;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                return 2147483647L;
            }
        },
        GSM((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return i == 2 || i == 1 || i == 16;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityGsm) {
                    return ((CellIdentityGsm) cellIdentity).getLac();
                }
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityGsm) {
                    return (long) ((CellIdentityGsm) cellIdentity).getCid();
                }
                return 2147483647L;
            }
        },
        CDMA((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return false;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                return 2147483647L;
            }
        },
        TDSCDMA((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return i == 17;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityTdscdma) {
                    return ((CellIdentityTdscdma) cellIdentity).getLac();
                }
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityTdscdma) {
                    return (long) ((CellIdentityTdscdma) cellIdentity).getCid();
                }
                return 2147483647L;
            }
        },
        WCDMA((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return i == 3 || i == 10 || i == 8 || i == 9 || i == 15 || i == 30;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityWcdma) {
                    return ((CellIdentityWcdma) cellIdentity).getLac();
                }
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityWcdma) {
                    return (long) ((CellIdentityWcdma) cellIdentity).getCid();
                }
                return 2147483647L;
            }
        },
        LTE((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return i == 13;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityLte) {
                    return ((CellIdentityLte) cellIdentity).getTac();
                }
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityLte) {
                    return (long) ((CellIdentityLte) cellIdentity).getCi();
                }
                return 2147483647L;
            }
        },
        NR((String) null) {
            /* access modifiers changed from: package-private */
            public boolean isMatchedWith(int i) {
                return i == 20;
            }

            /* access modifiers changed from: package-private */
            public int retrieveAreaCode(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityNr) {
                    return ((CellIdentityNr) cellIdentity).getTac();
                }
                return Integer.MAX_VALUE;
            }

            /* access modifiers changed from: package-private */
            public long retrieveCellId(CellIdentity cellIdentity) {
                if (cellIdentity instanceof CellIdentityNr) {
                    return ((CellIdentityNr) cellIdentity).getNci();
                }
                return Long.MAX_VALUE;
            }
        };

        /* access modifiers changed from: package-private */
        public abstract boolean isMatchedWith(int i);

        /* access modifiers changed from: package-private */
        public abstract int retrieveAreaCode(CellIdentity cellIdentity);

        /* access modifiers changed from: package-private */
        public abstract long retrieveCellId(CellIdentity cellIdentity);
    }

    public static CellIdentityWrapper from(CellIdentity cellIdentity) {
        CellType cellType = CellType.UNKNOWN;
        if (cellIdentity instanceof CellIdentityLte) {
            cellType = CellType.LTE;
        } else if (cellIdentity instanceof CellIdentityNr) {
            cellType = CellType.NR;
        } else if (cellIdentity instanceof CellIdentityGsm) {
            cellType = CellType.GSM;
        } else if (cellIdentity instanceof CellIdentityWcdma) {
            cellType = CellType.WCDMA;
        } else if (cellIdentity instanceof CellIdentityTdscdma) {
            cellType = CellType.TDSCDMA;
        } else if (cellIdentity instanceof CellIdentityCdma) {
            cellType = CellType.CDMA;
        }
        return new CellIdentityWrapper(cellType, cellIdentity);
    }

    private CellIdentityWrapper(CellType cellType, CellIdentity cellIdentity) {
        this.mCellType = cellType;
        this.mAreaCode = cellType.retrieveAreaCode(cellIdentity);
        this.mCellId = cellType.retrieveCellId(cellIdentity);
    }

    public String toString() {
        if (this.mCellType == CellType.UNKNOWN) {
            return "Unknown CellIdentity";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CellIdentity<");
        sb.append(this.mCellType);
        sb.append(">: ");
        sb.append("AreaCode: ");
        if (isValidAreaCode()) {
            sb.append(IMSLog.checker(Integer.valueOf(this.mAreaCode)));
        } else {
            sb.append("tac or lac unavailable");
        }
        sb.append(" Cell ID: ");
        if (isValidCellId()) {
            sb.append(IMSLog.checker(Long.valueOf(this.mCellId)));
        } else {
            sb.append("cid unavailable");
        }
        return sb.toString();
    }

    public int getAreaCode() {
        return this.mAreaCode;
    }

    public long getCellId() {
        return this.mCellId;
    }

    public boolean isValid() {
        return isValidAreaCode() && isValidCellId();
    }

    public boolean isValidAreaCode() {
        return this.mAreaCode != Integer.MAX_VALUE;
    }

    public boolean isValidCellId() {
        if (this.mCellType == CellType.NR) {
            if (this.mCellId != Long.MAX_VALUE) {
                return true;
            }
        } else if (this.mCellId != 2147483647L) {
            return true;
        }
        return false;
    }

    public boolean isMatched(int i) {
        return this.mCellType.isMatchedWith(i);
    }

    public boolean isCdma() {
        CellType cellType = this.mCellType;
        return cellType == CellType.TDSCDMA || cellType == CellType.CDMA;
    }
}

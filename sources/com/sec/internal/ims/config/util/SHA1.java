package com.sec.internal.ims.config.util;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;

public class SHA1 {
    protected int H0;
    protected int H1;
    protected int H2;
    protected int H3;
    protected int H4;
    private long currentLen;
    private int currentPos;
    private final int[] w = new int[80];

    public SHA1() {
        reset();
    }

    public final void reset() {
        this.H0 = 1732584193;
        this.H1 = -271733879;
        this.H2 = -1732584194;
        this.H3 = 271733878;
        this.H4 = -1009589776;
        this.currentPos = 0;
        this.currentLen = 0;
    }

    public final void update(byte[] bArr) {
        update(bArr, 0, bArr.length);
    }

    public final void update(byte[] bArr, int i, int i2) {
        int i3;
        int i4 = i2;
        if (i4 >= 4) {
            int i5 = this.currentPos;
            int i6 = i5 >> 2;
            int i7 = i5 & 3;
            if (i7 == 0) {
                int[] iArr = this.w;
                int i8 = i + 1;
                int i9 = i8 + 1;
                int i10 = i9 + 1;
                byte b = ((bArr[i8] & 255) << 16) | ((bArr[i] & 255) << 24) | ((bArr[i9] & 255) << 8);
                i3 = i10 + 1;
                iArr[i6] = b | (bArr[i10] & 255);
                i4 -= 4;
                int i11 = i5 + 4;
                this.currentPos = i11;
                this.currentLen += 32;
                if (i11 == 64) {
                    perform();
                    this.currentPos = 0;
                }
            } else if (i7 == 1) {
                int[] iArr2 = this.w;
                int i12 = i + 1;
                int i13 = i12 + 1;
                int i14 = i13 + 1;
                iArr2[i6] = (bArr[i13] & 255) | ((bArr[i12] & 255) << 8) | ((bArr[i] & 255) << 16) | (iArr2[i6] << 24);
                i4 -= 3;
                int i15 = i5 + 3;
                this.currentPos = i15;
                this.currentLen += 24;
                if (i15 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                i3 = i14;
            } else if (i7 == 2) {
                int[] iArr3 = this.w;
                int i16 = i + 1;
                int i17 = i16 + 1;
                iArr3[i6] = (iArr3[i6] << 16) | (bArr[i16] & 255) | ((bArr[i] & 255) << 8);
                i4 -= 2;
                int i18 = i5 + 2;
                this.currentPos = i18;
                this.currentLen += 16;
                if (i18 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                i3 = i17;
            } else if (i7 != 3) {
                i3 = i;
            } else {
                int[] iArr4 = this.w;
                i3 = i + 1;
                iArr4[i6] = (iArr4[i6] << 8) | (bArr[i] & 255);
                i4--;
                int i19 = i5 + 1;
                this.currentPos = i19;
                this.currentLen += 8;
                if (i19 == 64) {
                    perform();
                    this.currentPos = 0;
                }
            }
            while (i4 >= 8) {
                int[] iArr5 = this.w;
                int i20 = this.currentPos;
                int i21 = i3 + 1;
                int i22 = i21 + 1;
                int i23 = i22 + 1;
                byte b2 = ((bArr[i21] & 255) << 16) | ((bArr[i3] & 255) << 24) | ((bArr[i22] & 255) << 8);
                int i24 = i23 + 1;
                iArr5[i20 >> 2] = b2 | (bArr[i23] & 255);
                int i25 = i20 + 4;
                this.currentPos = i25;
                if (i25 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                int[] iArr6 = this.w;
                int i26 = this.currentPos;
                int i27 = i24 + 1;
                int i28 = i27 + 1;
                int i29 = i28 + 1;
                byte b3 = ((bArr[i27] & 255) << 16) | ((bArr[i24] & 255) << 24) | ((bArr[i28] & 255) << 8);
                i3 = i29 + 1;
                iArr6[i26 >> 2] = b3 | (bArr[i29] & 255);
                int i30 = i26 + 4;
                this.currentPos = i30;
                if (i30 == 64) {
                    perform();
                    this.currentPos = 0;
                }
                this.currentLen += 64;
                i4 -= 8;
            }
        } else {
            i3 = i;
        }
        while (i4 > 0) {
            int i31 = this.currentPos;
            int i32 = i31 >> 2;
            int[] iArr7 = this.w;
            int i33 = i3 + 1;
            iArr7[i32] = (iArr7[i32] << 8) | (bArr[i3] & 255);
            this.currentLen += 8;
            int i34 = i31 + 1;
            this.currentPos = i34;
            if (i34 == 64) {
                perform();
                this.currentPos = 0;
            }
            i4--;
            i3 = i33;
        }
    }

    public final void update(byte b) {
        int i = this.currentPos;
        int i2 = i >> 2;
        int[] iArr = this.w;
        iArr[i2] = (b & 255) | (iArr[i2] << 8);
        this.currentLen += 8;
        int i3 = i + 1;
        this.currentPos = i3;
        if (i3 == 64) {
            perform();
            this.currentPos = 0;
        }
    }

    private final void putInt(byte[] bArr, int i, int i2) {
        bArr[i] = (byte) (i2 >> 24);
        bArr[i + 1] = (byte) (i2 >> 16);
        bArr[i + 2] = (byte) (i2 >> 8);
        bArr[i + 3] = (byte) i2;
    }

    public final void digest(byte[] bArr) {
        digest(bArr, 0);
    }

    public final void digest(byte[] bArr, int i) {
        int i2 = this.currentPos;
        int i3 = i2 >> 2;
        int[] iArr = this.w;
        iArr[i3] = ((iArr[i3] << 8) | 128) << ((3 - (i2 & 3)) << 3);
        int i4 = (i2 & -4) + 4;
        this.currentPos = i4;
        if (i4 == 64) {
            this.currentPos = 0;
            perform();
        } else if (i4 == 60) {
            this.currentPos = 0;
            iArr[15] = 0;
            perform();
        }
        for (int i5 = this.currentPos >> 2; i5 < 14; i5++) {
            this.w[i5] = 0;
        }
        int[] iArr2 = this.w;
        long j = this.currentLen;
        iArr2[14] = (int) (j >> 32);
        iArr2[15] = (int) j;
        perform();
        putInt(bArr, i, this.H0);
        putInt(bArr, i + 4, this.H1);
        putInt(bArr, i + 8, this.H2);
        putInt(bArr, i + 12, this.H3);
        putInt(bArr, i + 16, this.H4);
    }

    private final void perform() {
        for (int i = 16; i < 80; i++) {
            int[] iArr = this.w;
            int i2 = ((iArr[i - 3] ^ iArr[i - 8]) ^ iArr[i - 14]) ^ iArr[i - 16];
            iArr[i] = (i2 >>> 31) | (i2 << 1);
        }
        int i3 = this.H0;
        int i4 = this.H1;
        int i5 = this.H2;
        int i6 = this.H3;
        int i7 = this.H4;
        int i8 = ((i3 << 5) | (i3 >>> 27)) + ((i4 & i5) | ((~i4) & i6));
        int[] iArr2 = this.w;
        int i9 = i8 + iArr2[0] + 1518500249 + i7;
        int i10 = (i4 << 30) | (i4 >>> 2);
        int i11 = ((i9 << 5) | (i9 >>> 27)) + ((i3 & i10) | ((~i3) & i5)) + iArr2[1] + 1518500249 + i6;
        int i12 = (i3 << 30) | (i3 >>> 2);
        int i13 = ((i11 << 5) | (i11 >>> 27)) + (((~i9) & i10) | (i9 & i12)) + iArr2[2] + 1518500249 + i5;
        int i14 = (i9 >>> 2) | (i9 << 30);
        int i15 = i10 + ((i13 << 5) | (i13 >>> 27)) + ((i11 & i14) | ((~i11) & i12)) + iArr2[3] + 1518500249;
        int i16 = (i11 << 30) | (i11 >>> 2);
        int i17 = i12 + ((i15 << 5) | (i15 >>> 27)) + (((~i13) & i14) | (i13 & i16)) + iArr2[4] + 1518500249;
        int i18 = (i13 << 30) | (i13 >>> 2);
        int i19 = i14 + ((i17 << 5) | (i17 >>> 27)) + ((i15 & i18) | ((~i15) & i16)) + iArr2[5] + 1518500249;
        int i20 = (i15 >>> 2) | (i15 << 30);
        int i21 = i16 + ((i19 << 5) | (i19 >>> 27)) + ((i17 & i20) | ((~i17) & i18)) + iArr2[6] + 1518500249;
        int i22 = (i17 << 30) | (i17 >>> 2);
        int i23 = i18 + ((i21 << 5) | (i21 >>> 27)) + ((i19 & i22) | ((~i19) & i20)) + iArr2[7] + 1518500249;
        int i24 = (i19 >>> 2) | (i19 << 30);
        int i25 = i20 + ((i23 << 5) | (i23 >>> 27)) + ((i21 & i24) | ((~i21) & i22)) + iArr2[8] + 1518500249;
        int i26 = (i21 >>> 2) | (i21 << 30);
        int i27 = i22 + ((i25 << 5) | (i25 >>> 27)) + ((i23 & i26) | ((~i23) & i24)) + iArr2[9] + 1518500249;
        int i28 = (i23 >>> 2) | (i23 << 30);
        int i29 = i24 + ((i27 << 5) | (i27 >>> 27)) + ((i25 & i28) | ((~i25) & i26)) + iArr2[10] + 1518500249;
        int i30 = (i25 >>> 2) | (i25 << 30);
        int i31 = i26 + ((i29 << 5) | (i29 >>> 27)) + ((i27 & i30) | ((~i27) & i28)) + iArr2[11] + 1518500249;
        int i32 = (i27 >>> 2) | (i27 << 30);
        int i33 = i28 + ((i31 << 5) | (i31 >>> 27)) + ((i29 & i32) | ((~i29) & i30)) + iArr2[12] + 1518500249;
        int i34 = (i29 >>> 2) | (i29 << 30);
        int i35 = i30 + ((i33 << 5) | (i33 >>> 27)) + ((i31 & i34) | ((~i31) & i32)) + iArr2[13] + 1518500249;
        int i36 = (i31 >>> 2) | (i31 << 30);
        int i37 = i32 + ((i35 << 5) | (i35 >>> 27)) + ((i33 & i36) | ((~i33) & i34)) + iArr2[14] + 1518500249;
        int i38 = (i33 >>> 2) | (i33 << 30);
        int i39 = i34 + ((i37 << 5) | (i37 >>> 27)) + ((i35 & i38) | ((~i35) & i36)) + iArr2[15] + 1518500249;
        int i40 = (i35 >>> 2) | (i35 << 30);
        int i41 = i36 + ((i39 << 5) | (i39 >>> 27)) + ((i37 & i40) | ((~i37) & i38)) + iArr2[16] + 1518500249;
        int i42 = (i37 >>> 2) | (i37 << 30);
        int i43 = i38 + ((i41 << 5) | (i41 >>> 27)) + ((i39 & i42) | ((~i39) & i40)) + iArr2[17] + 1518500249;
        int i44 = (i39 >>> 2) | (i39 << 30);
        int i45 = i40 + ((i43 << 5) | (i43 >>> 27)) + ((i41 & i44) | ((~i41) & i42)) + iArr2[18] + 1518500249;
        int i46 = (i41 >>> 2) | (i41 << 30);
        int i47 = i42 + ((i45 << 5) | (i45 >>> 27)) + ((i43 & i46) | ((~i43) & i44)) + iArr2[19] + 1518500249;
        int i48 = (i43 >>> 2) | (i43 << 30);
        int i49 = i44 + ((i47 << 5) | (i47 >>> 27)) + ((i45 ^ i48) ^ i46) + iArr2[20] + 1859775393;
        int i50 = (i45 << 30) | (i45 >>> 2);
        int i51 = i46 + ((i49 << 5) | (i49 >>> 27)) + ((i47 ^ i50) ^ i48) + iArr2[21] + 1859775393;
        int i52 = (i47 << 30) | (i47 >>> 2);
        int i53 = i48 + ((i51 << 5) | (i51 >>> 27)) + ((i49 ^ i52) ^ i50) + iArr2[22] + 1859775393;
        int i54 = (i49 >>> 2) | (i49 << 30);
        int i55 = i50 + ((i53 << 5) | (i53 >>> 27)) + ((i51 ^ i54) ^ i52) + iArr2[23] + 1859775393;
        int i56 = (i51 >>> 2) | (i51 << 30);
        int i57 = i52 + ((i55 << 5) | (i55 >>> 27)) + ((i53 ^ i56) ^ i54) + iArr2[24] + 1859775393;
        int i58 = (i53 >>> 2) | (i53 << 30);
        int i59 = i54 + ((i57 << 5) | (i57 >>> 27)) + ((i55 ^ i58) ^ i56) + iArr2[25] + 1859775393;
        int i60 = (i55 >>> 2) | (i55 << 30);
        int i61 = i56 + ((i59 << 5) | (i59 >>> 27)) + ((i57 ^ i60) ^ i58) + iArr2[26] + 1859775393;
        int i62 = (i57 >>> 2) | (i57 << 30);
        int i63 = i58 + ((i61 << 5) | (i61 >>> 27)) + ((i59 ^ i62) ^ i60) + iArr2[27] + 1859775393;
        int i64 = (i59 >>> 2) | (i59 << 30);
        int i65 = i60 + ((i63 << 5) | (i63 >>> 27)) + ((i61 ^ i64) ^ i62) + iArr2[28] + 1859775393;
        int i66 = (i61 >>> 2) | (i61 << 30);
        int i67 = i62 + ((i65 << 5) | (i65 >>> 27)) + ((i63 ^ i66) ^ i64) + iArr2[29] + 1859775393;
        int i68 = (i63 >>> 2) | (i63 << 30);
        int i69 = i64 + ((i67 << 5) | (i67 >>> 27)) + ((i65 ^ i68) ^ i66) + iArr2[30] + 1859775393;
        int i70 = (i65 >>> 2) | (i65 << 30);
        int i71 = i66 + ((i69 << 5) | (i69 >>> 27)) + ((i67 ^ i70) ^ i68) + iArr2[31] + 1859775393;
        int i72 = (i67 >>> 2) | (i67 << 30);
        int i73 = i68 + ((i71 << 5) | (i71 >>> 27)) + ((i69 ^ i72) ^ i70) + iArr2[32] + 1859775393;
        int i74 = (i69 >>> 2) | (i69 << 30);
        int i75 = i70 + ((i73 << 5) | (i73 >>> 27)) + ((i71 ^ i74) ^ i72) + iArr2[33] + 1859775393;
        int i76 = (i71 >>> 2) | (i71 << 30);
        int i77 = i72 + ((i75 << 5) | (i75 >>> 27)) + ((i73 ^ i76) ^ i74) + iArr2[34] + 1859775393;
        int i78 = (i73 >>> 2) | (i73 << 30);
        int i79 = i74 + ((i77 << 5) | (i77 >>> 27)) + ((i75 ^ i78) ^ i76) + iArr2[35] + 1859775393;
        int i80 = (i75 >>> 2) | (i75 << 30);
        int i81 = i76 + ((i79 << 5) | (i79 >>> 27)) + ((i77 ^ i80) ^ i78) + iArr2[36] + 1859775393;
        int i82 = (i77 >>> 2) | (i77 << 30);
        int i83 = i78 + ((i81 << 5) | (i81 >>> 27)) + ((i79 ^ i82) ^ i80) + iArr2[37] + 1859775393;
        int i84 = (i79 >>> 2) | (i79 << 30);
        int i85 = i80 + ((i83 << 5) | (i83 >>> 27)) + ((i81 ^ i84) ^ i82) + iArr2[38] + 1859775393;
        int i86 = (i81 >>> 2) | (i81 << 30);
        int i87 = i82 + ((i85 << 5) | (i85 >>> 27)) + ((i83 ^ i86) ^ i84) + iArr2[39] + 1859775393;
        int i88 = (i83 >>> 2) | (i83 << 30);
        int i89 = i84 + (((((i87 << 5) | (i87 >>> 27)) + (((i85 & i88) | (i85 & i86)) | (i88 & i86))) + iArr2[40]) - 1894007588);
        boolean z = (i85 >>> 2) | (i85 << 30);
        int i90 = i86 + (((((i89 << 5) | (i89 >>> 27)) + (((i87 & z) | (i87 & i88)) | (z & i88))) + iArr2[41]) - 1894007588);
        boolean z2 = (i87 >>> 2) | (i87 << 30);
        int i91 = i88 + (((((i90 << 5) | (i90 >>> 27)) + (((i89 & z2) | (i89 & z)) | (z2 & z))) + iArr2[42]) - 1894007588);
        boolean z3 = (i89 >>> 2) | (i89 << 30);
        int i92 = (z ? 1 : 0) + (((((i91 << 5) | (i91 >>> 27)) + (((i90 & z3) | (i90 & z2)) | (z3 & z2))) + iArr2[43]) - 1894007588);
        boolean z4 = (i90 >>> 2) | (i90 << 30);
        int i93 = (z2 ? 1 : 0) + (((((i92 << 5) | (i92 >>> 27) ? 1 : 0) + (((i91 & z4) | (i91 & z3)) | (z4 & z3) ? 1 : 0)) + iArr2[44]) - 1894007588);
        boolean z5 = (i91 >>> 2) | (i91 << 30);
        int i94 = (z3 ? 1 : 0) + (((((i93 << 5) | (i93 >>> 27) ? 1 : 0) + (((i92 & z5) | (i92 & z4)) | (z5 & z4) ? 1 : 0)) + iArr2[45]) - 1894007588);
        boolean z6 = (i92 >>> 2) | (i92 << 30);
        int i95 = (z4 ? 1 : 0) + (((((i94 << 5) | (i94 >>> 27) ? 1 : 0) + (((i93 & z6) | (i93 & z5)) | (z6 & z5) ? 1 : 0)) + iArr2[46]) - 1894007588);
        boolean z7 = (i93 >>> 2) | (i93 << 30);
        int i96 = (z5 ? 1 : 0) + (((((i95 << 5) | (i95 >>> 27) ? 1 : 0) + (((i94 & z7) | (i94 & z6)) | (z7 & z6) ? 1 : 0)) + iArr2[47]) - 1894007588);
        boolean z8 = (i94 >>> 2) | (i94 << 30);
        int i97 = (z6 ? 1 : 0) + (((((i96 << 5) | (i96 >>> 27) ? 1 : 0) + (((i95 & z8) | (i95 & z7)) | (z8 & z7) ? 1 : 0)) + iArr2[48]) - 1894007588);
        boolean z9 = (i95 >>> 2) | (i95 << 30);
        int i98 = (z7 ? 1 : 0) + (((((i97 << 5) | (i97 >>> 27) ? 1 : 0) + (((i96 & z9) | (i96 & z8)) | (z9 & z8) ? 1 : 0)) + iArr2[49]) - 1894007588);
        boolean z10 = (i96 >>> 2) | (i96 << 30);
        int i99 = (z8 ? 1 : 0) + (((((i98 << 5) | (i98 >>> 27) ? 1 : 0) + (((i97 & z10) | (i97 & z9)) | (z10 & z9) ? 1 : 0)) + iArr2[50]) - 1894007588);
        boolean z11 = (i97 >>> 2) | (i97 << 30);
        int i100 = (z9 ? 1 : 0) + (((((i99 << 5) | (i99 >>> 27) ? 1 : 0) + (((i98 & z11) | (i98 & z10)) | (z11 & z10) ? 1 : 0)) + iArr2[51]) - 1894007588);
        boolean z12 = (i98 >>> 2) | (i98 << 30);
        int i101 = (z10 ? 1 : 0) + (((((i100 << 5) | (i100 >>> 27) ? 1 : 0) + (((i99 & z12) | (i99 & z11)) | (z12 & z11) ? 1 : 0)) + iArr2[52]) - 1894007588);
        boolean z13 = (i99 >>> 2) | (i99 << 30);
        int i102 = (z11 ? 1 : 0) + (((((i101 << 5) | (i101 >>> 27) ? 1 : 0) + (((i100 & z13) | (i100 & z12)) | (z13 & z12) ? 1 : 0)) + iArr2[53]) - 1894007588);
        boolean z14 = (i100 >>> 2) | (i100 << 30);
        int i103 = (z12 ? 1 : 0) + (((((i102 << 5) | (i102 >>> 27) ? 1 : 0) + (((i101 & z14) | (i101 & z13)) | (z14 & z13) ? 1 : 0)) + iArr2[54]) - 1894007588);
        boolean z15 = (i101 >>> 2) | (i101 << 30);
        int i104 = ((((z13 ? 1 : 0) + ((i103 << 5) | (i103 >>> 27) ? 1 : 0)) + (((i102 & z15) | (i102 & z14)) | (z15 & z14) ? 1 : 0)) + iArr2[55]) - 1894007588;
        boolean z16 = (i102 >>> 2) | (i102 << 30);
        int i105 = (z14 ? 1 : 0) + (((((i104 << 5) | (i104 >>> 27) ? 1 : 0) + (((i103 & z16) | (i103 & z15)) | (z16 & z15) ? 1 : 0)) + iArr2[56]) - 1894007588);
        boolean z17 = (i103 >>> 2) | (i103 << 30);
        int i106 = (z15 ? 1 : 0) + (((((i105 << 5) | (i105 >>> 27) ? 1 : 0) + (((i104 & z17) | (i104 & z16)) | (z17 & z16) ? 1 : 0)) + iArr2[57]) - 1894007588);
        boolean z18 = (i104 >>> 2) | (i104 << 30);
        int i107 = (z16 ? 1 : 0) + (((((i106 << 5) | (i106 >>> 27) ? 1 : 0) + (((i105 & z18) | (i105 & z17)) | (z18 & z17) ? 1 : 0)) + iArr2[58]) - 1894007588);
        boolean z19 = (i105 >>> 2) | (i105 << 30);
        int i108 = (z17 ? 1 : 0) + (((((i107 << 5) | (i107 >>> 27) ? 1 : 0) + (((i106 & z19) | (i106 & z18)) | (z19 & z18) ? 1 : 0)) + iArr2[59]) - 1894007588);
        boolean z20 = (i106 >>> 2) | (i106 << 30);
        int i109 = (z18 ? 1 : 0) + (((((i108 << 5) | (i108 >>> 27) ? 1 : 0) + ((i107 ^ z20) ^ z19 ? 1 : 0)) + iArr2[60]) - 899497514);
        boolean z21 = (i107 >>> 2) | (i107 << 30);
        int i110 = (z19 ? 1 : 0) + (((((i109 << 5) | (i109 >>> 27) ? 1 : 0) + ((i108 ^ z21) ^ z20 ? 1 : 0)) + iArr2[61]) - 899497514);
        boolean z22 = (i108 >>> 2) | (i108 << 30);
        int i111 = (z20 ? 1 : 0) + (((((i110 << 5) | (i110 >>> 27) ? 1 : 0) + ((i109 ^ z22) ^ z21 ? 1 : 0)) + iArr2[62]) - 899497514);
        boolean z23 = (i109 >>> 2) | (i109 << 30);
        int i112 = (z21 ? 1 : 0) + (((((i111 << 5) | (i111 >>> 27) ? 1 : 0) + ((i110 ^ z23) ^ z22 ? 1 : 0)) + iArr2[63]) - 899497514);
        boolean z24 = (i110 >>> 2) | (i110 << 30);
        int i113 = (z22 ? 1 : 0) + (((((i112 << 5) | (i112 >>> 27) ? 1 : 0) + ((i111 ^ z24) ^ z23 ? 1 : 0)) + iArr2[64]) - 899497514);
        boolean z25 = (i111 >>> 2) | (i111 << 30);
        int i114 = (z23 ? 1 : 0) + (((((i113 << 5) | (i113 >>> 27) ? 1 : 0) + ((i112 ^ z25) ^ z24 ? 1 : 0)) + iArr2[65]) - 899497514);
        boolean z26 = (i112 >>> 2) | (i112 << 30);
        int i115 = (z24 ? 1 : 0) + (((((i114 << 5) | (i114 >>> 27) ? 1 : 0) + ((i113 ^ z26) ^ z25 ? 1 : 0)) + iArr2[66]) - 899497514);
        boolean z27 = (i113 >>> 2) | (i113 << 30);
        int i116 = (z25 ? 1 : 0) + (((((i115 << 5) | (i115 >>> 27) ? 1 : 0) + ((i114 ^ z27) ^ z26 ? 1 : 0)) + iArr2[67]) - 899497514);
        boolean z28 = (i114 >>> 2) | (i114 << 30);
        int i117 = (z26 ? 1 : 0) + (((((i116 << 5) | (i116 >>> 27) ? 1 : 0) + ((i115 ^ z28) ^ z27 ? 1 : 0)) + iArr2[68]) - 899497514);
        boolean z29 = (i115 >>> 2) | (i115 << 30);
        int i118 = (z27 ? 1 : 0) + (((((i117 << 5) | (i117 >>> 27) ? 1 : 0) + ((i116 ^ z29) ^ z28 ? 1 : 0)) + iArr2[69]) - 899497514);
        boolean z30 = (i116 >>> 2) | (i116 << 30);
        int i119 = (z28 ? 1 : 0) + (((((i118 << 5) | (i118 >>> 27) ? 1 : 0) + ((i117 ^ z30) ^ z29 ? 1 : 0)) + iArr2[70]) - 899497514);
        boolean z31 = (i117 >>> 2) | (i117 << 30);
        int i120 = (z29 ? 1 : 0) + (((((i119 << 5) | (i119 >>> 27) ? 1 : 0) + ((i118 ^ z31) ^ z30 ? 1 : 0)) + iArr2[71]) - 899497514);
        boolean z32 = (i118 >>> 2) | (i118 << 30);
        int i121 = (z30 ? 1 : 0) + (((((i120 << 5) | (i120 >>> 27) ? 1 : 0) + ((i119 ^ z32) ^ z31 ? 1 : 0)) + iArr2[72]) - 899497514);
        boolean z33 = (i119 >>> 2) | (i119 << 30);
        int i122 = (z31 ? 1 : 0) + (((((i121 << 5) | (i121 >>> 27) ? 1 : 0) + ((i120 ^ z33) ^ z32 ? 1 : 0)) + iArr2[73]) - 899497514);
        boolean z34 = (i120 >>> 2) | (i120 << 30);
        int i123 = (z32 ? 1 : 0) + (((((i122 << 5) | (i122 >>> 27) ? 1 : 0) + ((i121 ^ z34) ^ z33 ? 1 : 0)) + iArr2[74]) - 899497514);
        boolean z35 = (i121 >>> 2) | (i121 << 30);
        int i124 = (z33 ? 1 : 0) + (((((i123 << 5) | (i123 >>> 27) ? 1 : 0) + ((i122 ^ z35) ^ z34 ? 1 : 0)) + iArr2[75]) - 899497514);
        boolean z36 = (i122 >>> 2) | (i122 << 30);
        int i125 = (z34 ? 1 : 0) + (((((i124 << 5) | (i124 >>> 27) ? 1 : 0) + ((i123 ^ z36) ^ z35 ? 1 : 0)) + iArr2[76]) - 899497514);
        boolean z37 = (i123 >>> 2) | (i123 << 30);
        int i126 = (z35 ? 1 : 0) + (((((i125 << 5) | (i125 >>> 27) ? 1 : 0) + ((i124 ^ z37) ^ z36 ? 1 : 0)) + iArr2[77]) - 899497514);
        boolean z38 = (i124 >>> 2) | (i124 << 30);
        int i127 = (z36 ? 1 : 0) + (((((i126 << 5) | (i126 >>> 27) ? 1 : 0) + ((i125 ^ z38) ^ z37 ? 1 : 0)) + iArr2[78]) - 899497514);
        boolean z39 = (i125 >>> 2) | (i125 << 30);
        this.H0 = i3 + (z37 ? 1 : 0) + (((((i127 << 5) | (i127 >>> 27) ? 1 : 0) + ((i126 ^ z39) ^ z38 ? 1 : 0)) + iArr2[79]) - 899497514);
        this.H1 = i4 + i127;
        this.H2 = i5 + ((i126 >>> 2) | (i126 << 30));
        this.H3 = i6 + (z39 ? 1 : 0);
        this.H4 = i7 + (z38 ? 1 : 0);
    }

    private static String toHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bArr.length; i++) {
            sb.append("0123456789ABCDEF".charAt((bArr[i] >> 4) & 15));
            sb.append("0123456789ABCDEF".charAt(bArr[i] & 15));
        }
        return sb.toString();
    }

    public static void main(String[] strArr) {
        SHA1 sha1 = new SHA1();
        byte[] bArr = new byte[20];
        byte[] bArr2 = new byte[20];
        byte[] bArr3 = new byte[20];
        sha1.update("abc".getBytes());
        sha1.digest(bArr);
        sha1.update("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes());
        sha1.digest(bArr2);
        for (int i = 0; i < 1000000; i++) {
            sha1.update((byte) ReqMsg.request_ish_stop_session);
        }
        sha1.digest(bArr3);
        String hexString = toHexString(bArr);
        String hexString2 = toHexString(bArr2);
        String hexString3 = toHexString(bArr3);
        if (hexString.equals("A9993E364706816ABA3E25717850C26C9CD0D89D")) {
            System.out.println("SHA-1 Test 1 OK.");
        } else {
            System.out.println("SHA-1 Test 1 FAILED.");
        }
        if (hexString2.equals("84983E441C3BD26EBAAE4AA1F95129E5E54670F1")) {
            System.out.println("SHA-1 Test 2 OK.");
        } else {
            System.out.println("SHA-1 Test 2 FAILED.");
        }
        if (hexString3.equals("34AA973CD4C4DAA4F61EEB2BDBAD27316534016F")) {
            System.out.println("SHA-1 Test 3 OK.");
        } else {
            System.out.println("SHA-1 Test 3 FAILED.");
        }
        if (hexString3.equals("34AA973CD4C4DAA4F61EEB2BDBAD27316534016F")) {
            System.out.println("SHA-1 Test 3 OK.");
        } else {
            System.out.println("SHA-1 Test 3 FAILED.");
        }
    }
}

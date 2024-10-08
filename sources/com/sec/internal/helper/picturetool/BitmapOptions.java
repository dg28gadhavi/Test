package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapOptions {
    public static BitmapFactory.Options createData(int i, Bitmap.Config config) throws IllegalArgumentException, NullPointerException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = i;
        options.inPreferredConfig = config;
        return options;
    }
}

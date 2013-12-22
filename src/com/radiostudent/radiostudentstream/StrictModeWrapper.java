package com.radiostudent.radiostudentstream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;

public class StrictModeWrapper {
    public static void init(Context context) {
        // check if android:debuggable is set to true
        int applicationFlags = context.getApplicationInfo().flags;
        if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
        }
    }
}
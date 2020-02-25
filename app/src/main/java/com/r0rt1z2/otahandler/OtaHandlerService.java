package com.r0rt1z2.otahandler;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.os.Process;
import android.view.accessibility.AccessibilityEvent;

public class OtaHandlerService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        // Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            // Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity)
                    Log.i("OTAHandler", componentName.flattenToShortString());
                String main_app = componentName.flattenToShortString();
                ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses("com.amazon.settings.systemupdates");
                am.killBackgroundProcesses("com.amazon.device.software.ota");
                am.killBackgroundProcesses("com.amazon.kindle.otter.oobe.forced.ota");
                if (main_app.contains("systemupdates") || main_app.contains("forced.ota") || main_app.contains("device.software.ota")) {
                    Log.i("OTAHandler", "Recieved OTA as main activity. Go back.");
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {}
}
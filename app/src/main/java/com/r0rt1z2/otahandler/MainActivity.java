package com.r0rt1z2.otahandler;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.r0rtiz2.otahandler.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public boolean isAccessibilityEnabled() {
        /* Check all accessibility services that are running
           From: https://stackoverflow.com/a/5106419/12037376
        */
        int accessibilityEnabled = 0;
        final String LIGHTFLOW_ACCESSIBILITY_SERVICE = "com.r0rt1z2.otahandler/com.r0rt1z2.otahandler.OTAHandlerService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d("OTAHandler", "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d("OTAHandler", "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1) {
            Log.d("OTAHandler", "***ACCESSIBILITY IS ENABLED***: ");

            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d("OTAHandler", "Setting: " + settingValue);
            if (settingValue != null) {
                if (settingValue.contains("r0rt1z2")){
                     Log.d("OTAHandler", "We've found the correct setting - accessibility is switched on!");
                     return true;
                }
            }

            Log.d("OTAHandler", "***END***");
        }
        else {
            Log.d("OTAHandler", "***ACCESSIBILIY IS DISABLED***");
        }
        return accessibilityFound;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        /* Check if pm hide method is enabled */

        SharedPreferences settings = getSharedPreferences("OTAHandler_pmhide", 0);
        boolean hide_method = settings.getBoolean("pm_hide_method", false);

        if(hide_method) {
            Log.i("OTAHandler", "***USE PM-HIDE METHOD***");
        }

        /* Check if pm hide method is enabled */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        Context context = getApplicationContext();
        boolean enabled = isAccessibilityEnabled();
        TextView tv1 = (TextView)findViewById(R.id.textView1);
        Button btn1 = (Button)findViewById(R.id.enable_disable);

        if(enabled) {
            tv1.setText("Accessibility Service is enabled");
            btn1.setText("DISABLE SERVICE");
        }
        else {
            tv1.setText("Accessibility Service is not enabled");
            btn1.setText("ENABLE SERVICE");
        }

        btn1.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, 0);
            }
        });

    }

    /* Init the 3-dot MENU */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.source_code:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/R0rt1z2/OTAHandler"));
                startActivity(browserIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

}


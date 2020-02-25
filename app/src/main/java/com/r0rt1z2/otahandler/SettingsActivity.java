package com.r0rt1z2.otahandler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.r0rtiz2.otahandler.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Switch hide = (Switch) findViewById(R.id.switch1);
        final Switch pmhide = (Switch) findViewById(R.id.switch2);

        SharedPreferences settings = getSharedPreferences("OTAHandler_pmhide", 0);
        boolean hide_method = settings.getBoolean("pm_hide_method", false);

        if(hide_method) {
            pmhide.setText("Yes");
            pmhide.setChecked(true);
        }

        /* Check FireOS Version */
        String fos_ver = getSystemProperty("ro.build.version.fireos");

        if (fos_ver.startsWith("5")) {
            pmhide.setClickable(true);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("Use pm-hide method has been disabled as you're in FireOS " + fos_ver);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            pmhide.setAlpha(.5f);
            pmhide.setClickable(false);
            pmhide.setText("Not supported");
        }

        hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PackageManager packageManager = getPackageManager();
                    ComponentName componentName = new ComponentName(SettingsActivity.this, MainActivity.class);
                    packageManager.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    hide.setText("Hidden");
                } else {
                    PackageManager packageManager = getPackageManager();
                    ComponentName componentName = new ComponentName(SettingsActivity.this, MainActivity.class);
                    packageManager.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    hide.setText("Not hidden");
                }
            }
        });

        pmhide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pmhide.setText("Yes");
                    SharedPreferences settings = getSharedPreferences("OTAHandler_pmhide", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("pm_hide_method", true);
                    editor.commit();
                } else {
                    pmhide.setText("No");
                    SharedPreferences settings = getSharedPreferences("OTAHandler_pmhide", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("pm_hide_method", false);
                    editor.commit();
                }
            }
        });

    }

    public String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
}





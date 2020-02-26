package com.r0rt1z2.otahandler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.r0rtiz2.otahandler.R;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Switch hide = (Switch) findViewById(R.id.switch1);
        final Switch pmhide = (Switch) findViewById(R.id.switch2);

        //SharedPreferences settings = getSharedPreferences("OTAHandler", 0);
        //boolean helper_installed = settings.getBoolean("helper_installed", false);

        /* Check FireOS Version */
        String fos_ver = getSystemProperty("ro.build.version.fireos");

        if (fos_ver.startsWith("5")) {
            pmhide.setClickable(true);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("Install unifiedsharedacebook helper has been disabled as you're in FireOS " + fos_ver);
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
        /* Check FireOS version */

        /* Check unifiedsharefacebook helper version */
        try {
            Context context = getApplicationContext();
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo("com.amazon.unifiedsharefacebook", 0);
            String version = pInfo.versionName;
            if(version.contains("Debug")) {
                pmhide.setChecked(true);
                pmhide.setAlpha(.5f);
                pmhide.setClickable(false);
                pmhide.setText("Already Installed");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        /* Check unifiedsharefacebook helper version */

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
                    //SharedPreferences settings = getSharedPreferences("OTAHandler", 0);
                    //SharedPreferences.Editor editor = settings.edit();
                    //editor.putBoolean("pm_hide_method", true);
                    //editor.commit();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SettingsActivity.this);
                    builder1.setTitle("Warning");
                    builder1.setMessage("Do you want to download the debug version of unifiedsharefacebook to use pm hide in a standard shell?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.i("OTAHandler", "Clicked OK!");
                                    DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    File helper = new File(Environment.DIRECTORY_DOWNLOADS + "/helper.apk");
                                    if(helper.exists()) {
                                        Toast.makeText(SettingsActivity.this, "Deleting old apk...", Toast.LENGTH_SHORT).show();
                                        helper.delete();
                                    }
                                    Uri uri = Uri.parse("https://github.com/R0rt1z2/OTAHandler/raw/master/helper/com.amazon.unifiedsharefacebook.apk");
                                    final DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setTitle("com.amazon.unifiedsharefacebook");
                                    request.setDescription("Downloading");//request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"helper.apk");
                                    downloadmanager.enqueue(request);
                                    Context context = getApplicationContext();
                                    CharSequence text = "Downloading... Please wait.";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                    dialog.cancel();
                                    pmhide.setText("Installing...");
                                }
                            });
                    builder1.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.i("OTAHandler", "Clicked Cancel!");
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                } else {
                    pmhide.setText("No");
                    //SharedPreferences settings = getSharedPreferences("OTAHandler_pmhide", 0);
                    //SharedPreferences.Editor editor = settings.edit();
                    //editor.putBoolean("pm_hide_method", false);
                    //editor.commit();
                }
            }
        });

    }

    /* Installs the downloaded apk. Needs to be specified in the function */
    private void install_helper() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File
                ("/sdcard/Download/helper.apk")), "application/vnd.android.package-archive");
        startActivity(intent);
        //SharedPreferences settings = getSharedPreferences("OTAHandler", 0);
        //SharedPreferences.Editor editor = settings.edit();
        //editor.putBoolean("helper_installed", true);
        //editor.commit();
    }
    /* Installs the downloaded apk. Needs to be specified in the function */


    /* This runs after the download is completed */
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                Toast.makeText(SettingsActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                install_helper();
        }
    };
    /*This runs after the download is completed */

    /* Used to get system props, basically like getprop */
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
    /* Used to get system props, basically like getprop */
}





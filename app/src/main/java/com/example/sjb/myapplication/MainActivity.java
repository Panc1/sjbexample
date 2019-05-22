package com.example.sjb.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.wz.xjqg.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActivityBase {

    private static BottomNavigationView viewStatic;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    if (currentFrag != home) {
                        showFrame(home);
                    }
                    return true;
                case R.id.collection:
                    if (currentFrag != list) {
                        showFrame(list);
                    }
                    return true;
                case R.id.hotTopic:
                    if (currentFrag != topic) {
                        showFrame(topic);
                    }
                    return true;
                case R.id.strategy:
                    if (currentFrag != strategy) {
                        showFrame(strategy);
                    }
                    return true;
                case R.id.myMessage:
                    if (currentFrag != my) {
                        showFrame(my);
                    }
                    return true;
            }
            return false;
        }
    };

    private void showFrame(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
        currentFrag = fragment;
    }

    public static void setBar(final int Id) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                viewStatic.setSelectedItemId(Id);
            }
        }, 100);
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManage.getInstance().addActivity(this);
        initFragment();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationView viewB = navigation;
        viewStatic = navigation;
        BottomNavigationViewHelper.disableShiftMode(viewB);
//        if (Build.VERSION.SDK_INT >= 23)
//            if (hasNecessaryPMSGranted())
        SignIn();
//            else checkAndRequestPermissions();
//        else SignIn();
    }

    private void SignIn() {
        //友盟登录
        String id = getIMEI(this);
        MobclickAgent.onProfileSignIn(id);
        Log.e("Imei", "Imei[" + id + "]");

    }

    private Fragment currentFrag;
    private FragmentHome home;
    private FragmentStrategy strategy;
    private FragmentList list;
    private FragmentTopic topic;
    private FragmentMy my;

    private void initFragment() {
        home = new FragmentHome();
        list = new FragmentList();
        topic = new FragmentTopic();
        strategy = new FragmentStrategy();
        my = new FragmentMy();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayout, home)
                .show(home)
                .commit();
        currentFrag = home;
        timer.schedule(timerTask, 1000 * 15, 1000 * 15);
    }

    private boolean isPause = false;
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            FragmentHandler.sendMessage(new Message());
        }
    };

    @SuppressLint("HandlerLeak")
    Handler FragmentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isPause)
                RefreshFragment();
            super.handleMessage(msg);
        }
    };

    private void RefreshFragment() {
        if (currentFrag != null) {
            if (currentFrag == home)
                home.Refresh();
            else if (currentFrag == my)
                my.Refresh();
            else if (currentFrag == strategy)
                strategy.Refresh();
            else if (currentFrag == topic)
                topic.Refresh();
            else if (currentFrag == list)
                list.Refresh();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (view != null) {
//            Log.i("ansen", "是否有上一个页面:" + view.canGoBack());
//            if (view.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {//点击返回按钮的时候判断有没有上一页
//                view.goBack(); // goBack()表示返回webView的上一页面
//                return true;
//            }
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        Log.e("MainActivity", "onPause");
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("MainActivity", "onResume");
        isPause = false;
        RefreshFragment();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MobclickAgent.onProfileSignOff();
        super.onDestroy();
    }

    private static class BottomNavigationViewHelper {
        @SuppressLint("RestrictedApi")
        public static void disableShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BNVHelper", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BNVHelper", "Unable to change value of shift mode", e);
            }
        }
    }

    @SuppressLint("HardwareIds")
    public static String getIMEI(Context context) {
        String result = "";
        try {
            TelephonyManager telphonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            result = telphonyManager.getDeviceId();
            MobclickAgent.onProfileSignIn(result);
        } catch (Exception e) {
            Log.e("Imei", e.toString());
            result = "";
        }
        return (result != null ? result : "");
    }

    private List<String> mNeedRequestPMSList = new ArrayList<String>();
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    /**
     * 申请SDK运行需要的权限 注意：READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE
     * 两个权限是必须权限，没有这两个权限SDK无法正常获得广告。 WRITE_CALENDAR、ACCESS_FINE_LOCATION
     * 是两个可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
     */
    private void checkAndRequestPermissions() {
        /**
         * READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE 两个权限是必须权限，没有这两个权限SDK无法正常获得广告。
         */
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                .checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mNeedRequestPMSList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (0 != mNeedRequestPMSList.size()) {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(this, temp,
                    REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * 处理权限申请的结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            /**
             * 处理SDK申请权限的结果。
             */
            case REQUEST_PERMISSIONS_CODE:
                if (!hasNecessaryPMSGranted()) {
                    /**
                     * 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
                     */
                    Toast.makeText(
                            this,
                            "应用缺少SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限！请点击\"应用权限\"，打开所需要的权限。",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                } else SignIn();
                break;
            default:
                break;
        }
    }

    /**
     * 判断应用是否已经获得SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限。
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        boolean HasPMS = true;
        if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat
                .checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE))) HasPMS = false;
        if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)))
            HasPMS = false;
        if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
            HasPMS = false;

        return HasPMS;
    }


}

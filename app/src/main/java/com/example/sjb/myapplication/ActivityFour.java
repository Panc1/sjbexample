package com.example.sjb.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wz.xjqg.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wz_03 on 2019/1/9.
 */

public class ActivityFour extends ActivityBase implements View.OnClickListener {

    static final private String TAG = ">>>ActivityFour";
    private TextView textView;
    private WebView webView;
    private String url_home;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Activity myActivity;
    private WebViewClient webViewClient = new WebViewClient() {


        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            Log.e(TAG, "app-onReceivedHttpAuthRequest host:[" + host + "] realm :[" + realm + "]");
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
//            Log.e(TAG, "app-onLoadResource url:[" + url + "]");
            super.onLoadResource(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "app-onReceivedSslError");
            handler.proceed();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.e(TAG, "app-onReceivedHttpError");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (request.isForMainFrame())
                    if (errorResponse.getStatusCode() == 404)
                        webView.loadUrl("about:blank");
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e(TAG, "app-onReceivedError");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (request.isForMainFrame())
                    webView.loadUrl("about:blank");
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e(TAG, "app-onReceivedError");
            if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT)
                webView.loadUrl("about:blank");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e(TAG, "app-onPageStarted");
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e(TAG, "app-onPageFinished");
            swipeRefreshLayout.setRefreshing(false);
            super.onPageFinished(view, url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "app-shouldOverrideUrlLoading");
            if (url.equals(Constants.Home_Site)) {
                Finish();
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onReceivedTitle(WebView view, String title) {


            Log.e(TAG, "app-onReceivedTitle");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (title.contains("404") || title.contains("505") || title.contains("Error"))
                    webView.loadUrl("about:blank");
            }
            textView.setText(title);
            Log.e(TAG, "app-onReceivedTitle  Title:[" + title + "]");
            super.onReceivedTitle(view, title);
        }


    };


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);
        ActivityManage.getInstance().addActivity(this);
        myActivity = this;
        url_home =
//                "file:///android_asset/快速注册.html";
                getIntent().getStringExtra("url");
        textView = (TextView) findViewById(R.id.title);
        ImageView goBack = (ImageView) findViewById(R.id.goBack);
        goBack.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.message);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);


        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "app-onRefresh");
                if (checkNet())
                    webView.reload();
                else showDialog(2, url_home);
            }
        });

        if (checkNet())
            webView.loadUrl(url_home);
        else showDialog(1, url_home);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Save_url = url;
                Save_contentDisposition = contentDisposition;
                Save_mimetype = mimetype;
                LoadDialog = new ProgressDialog(ActivityFour.this);
                LoadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                LoadDialog.setCancelable(false);
                LoadDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
                LoadDialog.setTitle("正在下载");
                LoadDialog.setMax(100);
                LoadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                if (hasNecessaryPMSGranted()) {
                    LoadDialog.show();
                    downloadBySystem(url, contentDisposition, mimetype);
                } else
                    checkAndRequestPermissions();
            }
        });
        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setUseWideViewPort(true);//适应分辨率
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(true);// 支持缩放
        webSettings.setBuiltInZoomControls(true); // 启用内置缩放装置
    }

    private ProgressDialog LoadDialog;
    private String Save_url;
    private String Save_contentDisposition;
    private String Save_mimetype;

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void downloadBySystem(String url, String contentDisposition, String mimeType) {
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        Log.e(TAG, "app-fileName:[" + fileName + "]");
        // 设置通知栏的标题，如果不设置，默认使用文件名
        request.setTitle(fileName);
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (file.exists()) {
            file.delete();
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        Log.e(TAG, "app-downloadId:[" + downloadId + "]");
        checkDownload(downloadManager, downloadId);
        DownloadSuccess(downloadId);
    }

    private void checkDownload(final DownloadManager downloadManager, final long downloadId) {
        //查看下载信息
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                if (cursor != null && cursor.moveToFirst()) {
                    int dataLoaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int dataMax = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (LoadDialog != null)
                        LoadDialog.setProgress(dataLoaded * 100 / dataMax);
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    switch (status) {
                        case DownloadManager.STATUS_PAUSED:
                            Log.e(TAG, "app-下载暂停");
                        case DownloadManager.STATUS_PENDING:
                            Log.e(TAG, "app-下载延迟");
                        case DownloadManager.STATUS_RUNNING:
                            Log.e(TAG, "app-正在下载");
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Log.e(TAG, "app-下载完成");
                            if (LoadDialog != null) LoadDialog.dismiss();
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityFour.this)
                                            .setTitle("下载成功")
                                            .setMessage("一次申请3个以上，将更易下款，额度更多。")
                                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    MainActivity.setBar(R.id.home);
                                                    ActivityManage.getInstance().toHome();
                                                }
                                            });
                                    alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            });
                            timer.cancel();
                            break;
                    }
                }
            }
        };
        timer.schedule(timerTask, 500, 500);
    }

    private void DownloadSuccess(final long downloadId) {
        //监听下载成功
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        intentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Log.e(TAG, "app-BroadcastReceiver Action:[ DownloadManager.ACTION_DOWNLOAD_COMPLETE ]");
                    long myDownLoadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    Log.e(TAG, "app-myDownLoadId:[" + myDownLoadId + "]");
                    if (myDownLoadId == downloadId) {
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        Intent updateApk = new Intent(Intent.ACTION_VIEW);
                        Uri downloadFileUri = downloadManager.getUriForDownloadedFile(myDownLoadId);
                        updateApk.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                        updateApk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(updateApk);
                    }

                } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                    Log.e(TAG, "app-BroadcastReceiver Action:[ DownloadManager.ACTION_NOTIFICATION_CLICKED ]");
                } else if (DownloadManager.ACTION_VIEW_DOWNLOADS.equals(action)) {
                    Log.e(TAG, "app-BroadcastReceiver Action:[ DownloadManager.ACTION_VIEW_DOWNLOADS ]");
                }

            }
        };
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.goBack) {
            Finish();

        }
    }

    private void Finish() {
        ActivityManage.getInstance().removeActivity();
    }

    private boolean isDialogShow = false;

    public void showDialog(final int position, final String url) {
        if (!isDialogShow) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                    isDialogShow = true;
                    AlertDialog alertDialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityFour.this)
                            .setTitle("提示")
                            .setMessage("无法连接至服务器，请检查网络配置")
                            .setCancelable(false)
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isDialogShow = false;
                                    ActivityManage.getInstance().removeAll();
                                }
                            })
                            .setNegativeButton("设置网络", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isDialogShow = false;
                                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .setNeutralButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isDialogShow = false;
                                    if (checkNet())
                                        if (checkNet())
                                            switch (position) {
                                                case 1://OnCreate
                                                    webView.loadUrl(url_home);
                                                    break;
                                                case 2://Refrash
                                                    if (swipeRefreshLayout != null)
                                                        swipeRefreshLayout.setRefreshing(true);
                                                    webView.reload();
                                                    break;
                                                case 3://OverrideUrlLoading
                                                    if (url.equals(Constants.Home_Site))
                                                        Finish();
                                                    break;
                                            }
                                        else
                                            showDialog(position, url);
                                }
                            });
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    public boolean checkNet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected())
            return true;
        return false;
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
            Log.e(TAG, "app-checkAndRequestPermissions:[requestPermissions]");
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(this, temp,
                    REQUEST_PERMISSIONS_CODE);
        } else {
            Log.e(TAG, "app-checkAndRequestPermissions:[downloadBySystem]");
            LoadDialog.show();
            downloadBySystem(Save_url, Save_contentDisposition, Save_mimetype);
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
                            "应用缺少SDK运行必须的WRITE_EXTERNAL_STORAGE两个权限！请点击\"应用权限\"，打开所需要的权限。",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                } else {
                    myActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadDialog.show();
                            downloadBySystem(Save_url, Save_contentDisposition, Save_mimetype);
                        }
                    });
                }
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
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)))
            HasPMS = false;
        if (!(PackageManager.PERMISSION_GRANTED == ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
            HasPMS = false;
        if (HasPMS)
            Log.e(TAG, "app-hasNecessaryPMSGranted:[true]");
        else
            Log.e(TAG, "app-hasNecessaryPMSGranted:[false]");
        return HasPMS;
    }


}

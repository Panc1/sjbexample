package com.example.sjb.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.wz.xjqg.R;

/**
 * Created by wz_03 on 2019/1/9.
 */

public class ActivityThree extends ActivityBase implements View.OnClickListener {

    static final private String TAG = ">>>>ActivityThree";
    private String url;
    private WebViewClient webViewClient = new WebViewClient() {

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
        public boolean shouldOverrideUrlLoading(WebView view, String urlRequest) {
            Log.e(TAG, "app-shouldOverrideUrlLoading");
            if (checkNet())
            {
                if(Build.VERSION.SDK_INT <  23)
                {
                    Log.e(TAG, "app-Build.VERSION.SDK_INT < 23:["+urlRequest+"]");
                    if(urlRequest.contains("yryp6"))
                    {
                        Log.e(TAG, "app-contains:oss-channel.yryp6");
                        Uri uri = Uri.parse(urlRequest);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }else  ActivityManage.getInstance().changeActivity(urlRequest, ActivityFour.class, 4);
                }else{
                    ActivityManage.getInstance().changeActivity(urlRequest, ActivityFour.class, 4);
                }
            }
            else showDialog(3, urlRequest);
            return true;
        }
    };
    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            Log.e(TAG, "app-onReceivedTitle");
            Log.e(TAG, "app-onReceivedTitle Title:[" + title + "]");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (title.contains("404") || title.contains("505") || title.contains("Error"))

                {
                    Log.e(TAG, "app-load(about:blank)");
                    webView.loadUrl("about:blank");
                }
            }
            super.onReceivedTitle(view, title);
        }
    };
    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("", "ActivityThree");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three);
        ActivityManage.getInstance().addActivity(this);
        url = getIntent().getStringExtra("url");
        Log.e("ActivityThree", "url[" + url + "]");
        ImageView goBack = (ImageView) findViewById(R.id.goBack);
        goBack.setOnClickListener(this);
        ImageView toHome = (ImageView) findViewById(R.id.toHome);
        toHome.setOnClickListener(this);
        ImageView share = (ImageView) findViewById(R.id.share);
        share.setOnClickListener(this);
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
                else showDialog(2, url);
            }
        });

        if (checkNet())
            webView.loadUrl(url);
        else showDialog(1, url);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                outActivity();
                break;
            case R.id.toHome:
                MainActivity.setBar(R.id.home);
                ActivityManage.getInstance().toHome();
                break;
            case R.id.share:
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        if (webView != null) {
            Log.e(TAG, "app->>>>OnResume  reload webView");
            if (checkNet()) {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(true);
                if (position != 1) webView.reload();
                else webView.loadUrl(url);
            } else {
                Log.e(TAG, "app->>>>OnResume  Net Disconnect");
                showDialog(position, url);
            }
        }
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        outActivity();
        return super.onKeyDown(keyCode, event);
    }

    private void outActivity() {
        Log.e(TAG, "app-outActivity");
        ActivityManage.getInstance().removeActivity();
    }

    private boolean isDialogShow = false;

    private int position = 1;

    public void showDialog(final int position, final String url) {
        if (!isDialogShow) {
            this.position = position;
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                    isDialogShow = true;
                    AlertDialog alertDialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityThree.this)
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
                                        switch (position) {
                                            case 1://OnCreate
                                                webView.loadUrl(ActivityThree.this.url);
                                                break;
                                            case 2://Refrash
                                                if (swipeRefreshLayout != null)
                                                    swipeRefreshLayout.setRefreshing(true);
                                                webView.reload();
                                                break;
                                            case 3://OverrideUrlLoading
                                                ActivityManage.getInstance().changeActivity(url, ActivityFour.class, 4);
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

}

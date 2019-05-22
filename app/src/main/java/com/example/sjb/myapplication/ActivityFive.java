package com.example.sjb.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import android.widget.TextView;

import com.wz.xjqg.R;

/**
 * Created by wz_03 on 2019/1/9.
 */

public class ActivityFive extends ActivityBase implements View.OnClickListener {

    static final private String TAG = ">>>ActivityFive";
    private TextView textView;
    private WebView webView;
    private String url_home;
    private SwipeRefreshLayout swipeRefreshLayout;
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
            Log.e(TAG, "app-onReceivedTitle Title:[" + title + "]");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (title.contains("404") || title.contains("505") || title.contains("Error"))
                    webView.loadUrl("about:blank");
            }
            textView.setText(title);
            super.onReceivedTitle(view, title);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five);
        ActivityManage.getInstance().addActivity(this);
        url_home = getIntent().getStringExtra("url");
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
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
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

    @Override
    protected void onResume() {
        if (webView != null) {
            if (checkNet()) {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(true);
                if (position != 1) webView.reload();
                else webView.loadUrl(url_home);
            } else {
                showDialog(position, url_home);
            }
        }
        super.onResume();
    }

    private void Finish() {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityFive.this)
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

}

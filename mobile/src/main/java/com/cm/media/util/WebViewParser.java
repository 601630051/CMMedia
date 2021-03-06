package com.cm.media.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import androidx.annotation.Nullable;
import com.cm.media.repository.AppExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebViewParser {
    private static final String TAG = WebViewParser.class.getSimpleName();

    public interface Callback {
        void onSuccess(String url);

        void onFailed();
    }

    private WebView mWebView;
    private volatile boolean stop;
    private CountDownLatch latch;
    private Callback mCallback;
    private volatile String mRealUrl;

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void start(final Context context, String url) {
        mRealUrl = null;
        AppExecutor.getInstance().execute(() -> {
            latch = new CountDownLatch(1);
            stop = false;
            AppExecutor.getInstance().uiPost(() -> {
                createWebView(context);
                mWebView.addJavascriptInterface(new JsObject(), "Android");
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handler.proceed();
                    }


                    @Nullable
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if (stop) {
                            return null;
                        }
                        return super.shouldInterceptRequest(view, request);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        // 获取解析<meta name="share-description" content="获取到的值">
                        view.loadUrl("javascript:window.Android.onGetUrl("
                                + "document.getElementById('player').getAttribute('src')"
                                + ");");
                        super.onPageFinished(view, url);
                    }
                });
                mWebView.loadUrl(url);
            });
            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                stop = true;
                AppExecutor.getInstance().uiPost(() -> {
                    if (mWebView != null) {
                        mWebView.stopLoading();
                        mWebView.removeAllViews();
                        mWebView.destroy();
                        mWebView = null;
                    }
                    if (mCallback == null) {
                        return;
                    }
                    if (!TextUtils.isEmpty(mRealUrl)) {
                        mCallback.onSuccess(mRealUrl);
                    } else {
                        mCallback.onFailed();
                    }
                });
            }
        });

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void createWebView(Context context) {
        mWebView = new WebView(context);
        mWebView.setVisibility(View.INVISIBLE);
        WebSettings webSettings = mWebView.getSettings();
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings
                    .setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        final String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
        webSettings.setUserAgentString(userAgent);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getFileStreamPath("w_b_a_cache")
                .getAbsolutePath());
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

    public final class JsObject {
        @JavascriptInterface
        public void onGetUrl(String url) {
            mRealUrl = VodFormatUtils.parse(url);
            log("=url=" + url);
            if (!TextUtils.isEmpty(mRealUrl)) {
                stop = true;
                if (latch != null) {
                    latch.countDown();
                }
            }
        }
    }
}

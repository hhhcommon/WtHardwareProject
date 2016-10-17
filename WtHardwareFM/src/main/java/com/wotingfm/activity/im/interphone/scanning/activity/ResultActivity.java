package com.wotingfm.activity.im.interphone.scanning.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseActivity.AppBaseActivity;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;

/**
 * 扫描结果界面
 *
 * @author 辛龙
 *         2016年8月8日
 */
public class ResultActivity extends AppBaseActivity {

    @Override
    protected int setViewId() {
        return R.layout.activity_result;
    }

    @Override
    protected void init() {
        Bundle extras = getIntent().getExtras();
        WebView webview = (WebView) findViewById(R.id.webView);
        TextView mResultText = (TextView) findViewById(R.id.result_text);
        if (null != extras) {
            String result = extras.getString("result");
            L.e("扫描结果", result);
            if (result.contains("http")) {
                webview.setVisibility(View.VISIBLE);
                mResultText.setVisibility(View.GONE);
                DialogUtils.showDialog(context);
                WebSettings setting = webview.getSettings();
                setting.setJavaScriptEnabled(true);    // 支持js
                webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);// 解决缓存问题
                webview.loadUrl(result);
                webview.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);            // 使用当前的WebView加载页面
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        DialogUtils.closeDialog();
                    }
                });

                webview.setWebChromeClient(new WebChromeClient());
            } else {
                webview.setVisibility(View.GONE);
                mResultText.setVisibility(View.VISIBLE);
                mResultText.setText(result);
            }
        } else {
            webview.setVisibility(View.GONE);
            mResultText.setVisibility(View.VISIBLE);
            mResultText.setText("抱歉，暂无结果");
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                finish();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // message就是wave函数里alert的字符串，这样你就可以在android客户端里对这个数据进行处理
            result.confirm();
            return true;
        }
    }
}

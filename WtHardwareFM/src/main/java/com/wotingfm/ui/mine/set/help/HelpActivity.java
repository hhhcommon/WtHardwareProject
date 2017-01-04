package com.wotingfm.ui.mine.set.help;

import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.BaseActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.DialogUtils;

/**
 * 帮助--h5
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class HelpActivity extends BaseActivity implements OnClickListener {
    private WebView webview;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        dialog = DialogUtils.Dialogph(context, "正在加载");
        setView();
        setWeb();
    }

    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

        webview = (WebView) findViewById(R.id.webView);
        webview.setOnClickListener(this);
    }

    private void setWeb() {
        String url = GlobalConfig.wthelpUrl;
        WebSettings setting = webview.getSettings();
        setting.setJavaScriptEnabled(true);// 支持 js
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);// 解决缓存问题
        webview.loadUrl(url);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);// 使用当前的 WebView 加载页面
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (dialog != null) dialog.dismiss();
            }
        });
        webview.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
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
            // message 就是 wave 函数里 alert 的字符串，这样你就可以在 android 客户端里对这个数据进行处理
            result.confirm();
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webview = null;
        setContentView(R.layout.activity_null);
    }
}

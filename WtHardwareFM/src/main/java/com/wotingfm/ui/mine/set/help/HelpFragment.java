package com.wotingfm.ui.mine.set.help;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.DialogUtils;

/**
 * 帮助--h5
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class HelpFragment extends Fragment implements OnClickListener {
    private WebView webview;
    private Dialog dialog;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_help, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            dialog = DialogUtils.Dialogph(context, "正在加载");
            setView();
            setWeb();
        }
        return rootView;
    }

    private void setView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

        webview = (WebView) rootView.findViewById(R.id.webView);
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
                MineActivity.close();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webview = null;
    }
}

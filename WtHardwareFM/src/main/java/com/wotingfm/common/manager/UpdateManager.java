package com.wotingfm.common.manager;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 软件更新下载安装
 * @author 辛龙
 * 2016年8月8日
 */
public class UpdateManager extends Activity {
    private Context mContext;
    private String apkUrl = GlobalConfig.apkUrl;// 返回的安装包 url
    // private String apkUrl = "http://www.ym18.cn/appupgrade/yangzhirili.apk";

    /* 下载包安装路径 */
    //	sdcard/
    private final String savePath = Environment.getExternalStorageDirectory() + "/WTFM/APP/";
    private final String saveFileName = savePath + "WoTing.apk";

    /* 进度条与通知ui刷新的handler和msg常量 */
    private final int DOWN_UPDATE = 1;
    private final int DOWN_OVER = 2;
    private int progress;
    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    textProgress.setText("正在更新版本 " + progress + "%");
                    progressBar.setProgress(progress);
                    break;
                case DOWN_OVER:
                    pd.dismiss();
                    installApk();
                    break;
            }
        }
    };

    /**
     * 外部接口让
     * 主 Activity 调用
     */
    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 外部接口让
     * 主 Activity 调用
     */
    public void checkUpdateInfo() {
        showNoticeDialog();
    }

    /**
     * 外部接口让
     * 主 Activity 调用
     */
    public void checkUpdateInfo1() {
        showDownloadDialog();
    }

    private void showNoticeDialog() {
        String updateMsg = "我听FM有最新版本，亲快下载吧~";// 提示语
        Builder builder = new Builder(mContext);
        builder.setTitle("软件更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });

        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private ProgressDialog pd;
    private TextView textProgress;
    private ProgressBar progressBar;

    private void showDownloadDialog() {
        View progressView = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog_view, null);
        textProgress = (TextView) progressView.findViewById(R.id.text_progress);
        progressBar = (ProgressBar) progressView.findViewById(R.id.pb_progressbar);
        pd = new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.show();
        pd.setContentView(progressView);
        downloadApk();
    }

    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                //////////////
                String state = Environment.getExternalStorageState();
                if (!state.equals(Environment.MEDIA_MOUNTED)) {
                    Log.i("录音1", "SD Card is not mounted,It is  " + state + ".");
                }
                File directory = new File(savePath).getParentFile();
                if (!directory.exists() && !directory.mkdirs()) {
                    Log.i("录音2", "Path to file could not be created");
                }
                ///////

                File file = new File(savePath);
                if (!file.exists()) if(!file.mkdir()) L.w("TAG", "MKDir error");
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numRead = is.read(buf);
                    count += numRead;
                    progress = (int) (((float) count / length) * 100);
                    mHandler.sendEmptyMessage(DOWN_UPDATE);// 更新进度
                    if (numRead <= 0) {
                        mHandler.sendEmptyMessage(DOWN_OVER);// 下载完成通知安装
                        break;
                    }
                    fos.write(buf, 0, numRead);
                } while (!interceptFlag);// 点击取消就停止下载.
                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // 下载 apk
    private void downloadApk() {
        Thread downLoadThread = new Thread(mDownApkRunnable);
        downLoadThread.start();
    }

    // 安装 apk
    private void installApk() {
        File apkFile = new File(saveFileName);
        if (!apkFile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}

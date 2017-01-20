package com.wotingfm.ui.music.common.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wotingfm.ui.music.download.activity.DownloadActivity;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.dao.ThreadDao;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.download.model.ThreadInfo;
import com.wotingfm.common.constant.BroadcastConstants;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 */
public class DownloadTask {
    public static int downloadStatus = -1;
    public static Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDao mDao = null;
    private FileInfoDao FID = null;
    private int mFinished = 0;
    public static boolean isPause = false;

    public DownloadTask(Context mContexts, FileInfo mFileInfo) {
        mContext = mContexts;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDao(mContext);
        FID = new FileInfoDao(mContext);
    }

    public void downLoad() {
        List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());// 读取数据库的线程信息
        ThreadInfo threadInfo;
        if (0 == threads.size()) {
            threadInfo = new ThreadInfo(mFileInfo.getId(), mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threads.get(0);
        }
        downloadStatus = -1;

        new DownloadThread(threadInfo).start();// 创建子线程进行下载
    }

    private class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownloadThread(ThreadInfo mInfo) {
            this.mThreadInfo = mInfo;
        }

        @Override
        public void run() {
            // 向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                downloadStatus = 1;
                // 设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                int End = mThreadInfo.getEnd();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + End);
                // 设置文件写入位置
                String name = mFileInfo.getFileName();
                File file = new File(DownloadService.DOWNLOAD_PATH, name);
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent();
                intent.setAction(BroadcastConstants.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                // 开始下载
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpStatus.SC_PARTIAL_CONTENT) {
                    // 读取数据
                    inputStream = connection.getInputStream();
                    byte buf[] = new byte[1024 << 2];
                    int len;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buf)) != -1) {
                        raf.write(buf, 0, len);// 写入文件
                        // 把下载进度发送广播给 Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 100) {
                            time = System.currentTimeMillis();
                            intent.putExtra("url", mThreadInfo.getUrl());
                            intent.putExtra("start", mFinished);
                            intent.putExtra("end", mThreadInfo.getEnd());
                            FID.updataFileProgress(mThreadInfo.getUrl(), mFinished, mThreadInfo.getEnd());
                            Log.e("getStart()", mFinished + "");
                            mContext.sendBroadcast(intent);
                        }
                        // 在下载暂停时，保存下载进度
                        if (isPause) {
                            Log.e("isPause", isPause + "");
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            FID.updataFileProgress(mThreadInfo.getUrl(), mFinished, mThreadInfo.getEnd());
                            Log.e("mFinished", mFinished + "");
                            Log.e("mThreadInfo.getStart()", start + "");
                            Log.e("mThreadInfo.getEnd()", mThreadInfo.getEnd() + "");
                            return;
                        }
                    }
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());// 删除线程信息
                    Log.i("DownloadTask", "下载完毕");
                    // 向 fragment 发送完成消息
                    intent.putExtra("fileInfo", mFileInfo);
                    if (DownloadActivity.isVisible) {
                        intent.setAction(BroadcastConstants.ACTION_FINISHED);
                        mContext.sendBroadcast(intent);
                    } else {
                        intent.setAction(BroadcastConstants.ACTION_FINISHED_NO_DOWNLOADVIEW);
                        mContext.sendBroadcast(intent);
                    }
                } else if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    inputStream = connection.getInputStream();
                    byte buf[] = new byte[1024 << 2];
                    int len;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buf)) != -1) {
                        // 写入文件
                        raf.write(buf, 0, len);
                        // 把下载进度发送广播给 Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 100) {
                            time = System.currentTimeMillis();
                            intent.putExtra("url", mThreadInfo.getUrl());
                            intent.putExtra("start", mFinished);
                            intent.putExtra("end", mThreadInfo.getEnd());
                            FID.updataFileProgress(mThreadInfo.getUrl(), mFinished, mThreadInfo.getEnd());
                            Log.e("getStart()", mFinished + "");
                            mContext.sendBroadcast(intent);
                        }
                        // 在下载暂停时，保存下载进度
                        if (isPause) {
                            Log.e("isPause", isPause + "");
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            FID.updataFileProgress(mThreadInfo.getUrl(), mFinished, mThreadInfo.getEnd());
                            Log.e("mFinised", mFinished + "");
                            Log.e("mThreadInfo.getStart()", start + "");
                            Log.e("mThreadInfo.getEnd()", mThreadInfo.getEnd() + "");
                            return;
                        }
                    }
                    // 删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                    Log.i("DownloadTask", "下载完毕");
                    // 向 fragment 发送完成消息
                    intent.putExtra("fileInfo", mFileInfo);
                    if (DownloadActivity.isVisible) {
                        intent.setAction(BroadcastConstants.ACTION_FINISHED);
                        mContext.sendBroadcast(intent);
                    } else {
                        intent.setAction(BroadcastConstants.ACTION_FINISHED_NO_DOWNLOADVIEW);
                        mContext.sendBroadcast(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}

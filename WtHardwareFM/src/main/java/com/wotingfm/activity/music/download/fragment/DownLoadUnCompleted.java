package com.wotingfm.activity.music.download.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.common.service.DownloadService;
import com.wotingfm.activity.music.common.service.DownloadTask;
import com.wotingfm.activity.music.download.adapter.DownloadAdapter;
import com.wotingfm.activity.music.download.dao.FileInfoDao;
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.ToastUtils;

import java.util.List;

/**
 * 要注意删除事件和下载完毕事件后对数据库表的操作
 */
public class DownLoadUnCompleted extends Fragment {
    private ListView listView;
    private DownloadAdapter adapter;
    private FragmentActivity context;
    private TextView tv_start;// 开始下载按钮
    private List<FileInfo> fileInfoList;// 表中未完成的任务
    private View rootView;
    private boolean dwtype = false;// 判断
    private FileInfoDao FID;// 数据库操作对象
    private LinearLayout lin_status_yes;
    private LinearLayout lin_status_no;
    private String userId;
    private MessageReceivers Receiver;
    private int num = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstant.ACTION_UPDATE);
        filter.addAction(BroadcastConstant.ACTION_FINISHED);
        context.registerReceiver(mReceiver, filter);
        if (Receiver == null) {
            Receiver = new MessageReceivers();
            IntentFilter filters = new IntentFilter();
            filters.addAction(BroadcastConstant.PUSH_DOWN_UNCOMPLETED);
            context.registerReceiver(Receiver, filters);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_download_uncompleted, container, false);
        setView();
        initDao();// 初始化数据库对象
        setlistener();// 给控件设置监听
        setDownLoadSource();// 设置界面数据
        return rootView;
    }

    private void setView() {
        listView = (ListView) rootView.findViewById(R.id.listView);
        tv_start = (TextView) rootView.findViewById(R.id.tv_start);
        lin_status_yes = (LinearLayout) rootView.findViewById(R.id.lin_status_yes);// 有未下载时布局
        lin_status_no = (LinearLayout) rootView.findViewById(R.id.lin_status_no);// 无未下载时布局
    }

    // 初始化数据库对象
    private void initDao() {
        FID = new FileInfoDao(context);
    }

    /**
     * 设置界面数据
     */
    private void setDownLoadSource() {
        userId = CommonUtils.getUserId(context);
        fileInfoList = FID.queryFileinfo("false", userId);// 查询表中未完成的任务
        if (fileInfoList.size() == 0) {
            lin_status_yes.setVisibility(View.GONE);
            lin_status_no.setVisibility(View.VISIBLE);
        } else {
            lin_status_no.setVisibility(View.GONE);
            lin_status_yes.setVisibility(View.VISIBLE);
            if (DownloadTask.mContext != null) {
                if (DownloadTask.isPause == true) {
                    dwtype = false;
                    tv_start.setText("全部开始");
                } else {
                    dwtype = true;
                    tv_start.setText("全部暂停");
                }
            } else {
                //这里改了 原值为false
                dwtype = true;
                tv_start.setText("全部开始");
            }
            Log.e("广播消息", "执行刷新");
            adapter = new DownloadAdapter(context, fileInfoList);
            listView.setAdapter(adapter);
            setonitemlistener();
        }
    }

    private void setlistener() {
        tv_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwtype == true) {
                    /*
					 * 1:全部暂停事件  2：目前为1状态的设置为2 3：所有为0状态的不处理   4： 设置图片和文字
					 * 5：设置downloadstatus标签为-1，下载完当前任务后，不再下载另一条
					 */
                    for (int i = 0; i < fileInfoList.size(); i++) {
                        if (fileInfoList.get(i).getDownloadtype() == 1) {
                            fileInfoList.get(i).setDownloadtype(2);
                            FID.updatedownloadstatus(fileInfoList.get(i).getUrl(), "2");
                            DownloadService.workStop(fileInfoList.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tv_start.setText("全部开始");
                    dwtype = false;
                } else {
					/*
					 * 1:全部开始事件    2：目前为1状态的设置为2 ，2状态的不处理    3：所有为0状态的不处理
					 * 将position为0的数据标记为下载状态=14： 设置图片和文字
					 * 5：设置downloadstatus标签为1，下载完当前任务后，开始下载另一条
					 */
                    if (fileInfoList != null && fileInfoList.size() > 0) {
                        for (int i = 0; i < fileInfoList.size(); i++) {
                            if (fileInfoList.get(i).getDownloadtype() == 1) {
                                fileInfoList.get(i).setDownloadtype(2);
                                FID.updatedownloadstatus(fileInfoList.get(i).getUrl(), "2");
                                DownloadService.workStop(fileInfoList.get(i));
                            }
                        }
                        if (adapter == null) {
                            adapter = new DownloadAdapter(context, fileInfoList);
                            listView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        tv_start.setText("全部暂停");
                        // 如果点击了全部开始 就需要开始下一个下载对象
                        getFileInfo(fileInfoList.get(getnum()));
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (DownloadTask.downloadstatus == -1) {
                                    ToastUtils.show_always(context, fileInfoList.get(num).getFileName() + "的下载出现问题");
                                    getFileInfo(fileInfoList.get(getnum()));
                                }
                            }
                        }, 10000);
                        dwtype = true;
                    }
                }
            }
        });

        // 清空数据库中所有未下载完成的数据  text_clear
        rootView.findViewById(R.id.text_clear).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FID.deletefilebyuserid(userId);
                setDownLoadSource();
            }
        });
    }

    private int getnum() {
        if (num < fileInfoList.size() - 1) {
            num++;
        } else {
            num = 0;
        }
        return num;
    }

    private void setonitemlistener() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (fileInfoList.get(position).getDownloadtype() == 0) {
					/*
					 * 点击该项目时，此时是未下载状态需要把下载中状态的数据变为暂停状态暂停状态的数据不需要改变
					 * 最后把该数据状态变为开始下载中状态
					 */
                    for (int i = 0; i < fileInfoList.size(); i++) {
                        if (fileInfoList.get(i).getDownloadtype() == 1) {
                            fileInfoList.get(i).setDownloadtype(2);
                            FID.updatedownloadstatus(fileInfoList.get(i).getUrl(), "2");
                            DownloadService.workStop(fileInfoList.get(i));
                        }
                    }
                    getFileInfo(fileInfoList.get(position));
                } else if (fileInfoList.get(position).getDownloadtype() == 1) {
					/*
					 * 点击该项目时，此时该项目的状态是下载中 只需要把项目自己变为暂停状态即可
					 */
                    fileInfoList.get(position).setDownloadtype(2);
                    FID.updatedownloadstatus(fileInfoList.get(position).getUrl(), "2");
                    DownloadService.workStop(fileInfoList.get(position));
                    adapter.notifyDataSetChanged();
                } else {
					/*
					 * 点击该项目时，该项目为暂停状态 把其它的播放状态变为暂停状态 最后把自己状态变为下载中状态
					 */
                    for (int i = 0; i < fileInfoList.size(); i++) {
                        if (fileInfoList.get(i).getDownloadtype() == 1) {
                            fileInfoList.get(i).setDownloadtype(2);
                            FID.updatedownloadstatus(fileInfoList.get(i).getUrl(), "2");
                            DownloadService.workStop(fileInfoList.get(i));
                        }
                    }
                    getFileInfo(fileInfoList.get(position));
                }
            }
        });
    }

    /**
     * 给fileinfo初值
     */
    private void getFileInfo(FileInfo fileInfo) {
        fileInfo.setDownloadtype(1);
        FID.updatedownloadstatus(fileInfo.getUrl(), "1");
        DownloadService.workStart(fileInfo);
    }

    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contexts, Intent intent) {
            if (BroadcastConstant.ACTION_UPDATE.equals(intent.getAction())) {
                int start = intent.getIntExtra("start", 0);
                int end = intent.getIntExtra("end", 0);
                String url = intent.getStringExtra("url");
                if (adapter != null) {
                    adapter.updateProgress(url, start, end);
                }
            } else if (BroadcastConstant.ACTION_FINISHED.equals(intent.getAction())) {
                // 下载结束
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                ToastUtils.show_short(contexts, fileInfo.getFileName() + "已经下载完毕");
                FID.updatefileinfo(fileInfo.getFileName());
                //发送更新界面数据广播
                Intent p_intent = new Intent("push_down_completed");
                context.sendBroadcast(p_intent);
                if (dwtype) {
                    fileInfoList = FID.queryFileinfo("false", userId);// 查询表中未完成的任务
                    if (fileInfoList != null && fileInfoList.size() > 0) {
                        fileInfoList.get(0).setDownloadtype(1);
                        FID.updatedownloadstatus(fileInfoList.get(0).getUrl(), "1");
                        DownloadService.workStart(fileInfoList.get(0));
                        adapter = new DownloadAdapter(context, fileInfoList);
                        listView.setAdapter(adapter);
                        setonitemlistener();
                        setDownLoadSource();
                    } else {
                        tv_start.setText("全部开始");
                        adapter = new DownloadAdapter(context, fileInfoList);
                        listView.setAdapter(adapter);
                        setonitemlistener();
                        setDownLoadSource();
                    }
                } else {
                    fileInfoList = FID.queryFileinfo("false", userId);// 查询表中未完成的任务
                    tv_start.setText("全部开始");
                    adapter = new DownloadAdapter(context, fileInfoList);
                    listView.setAdapter(adapter);
                    setonitemlistener();
                    setDownLoadSource();
                }
            }
        }
    };

    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstant.PUSH_DOWN_UNCOMPLETED)) {
                setDownLoadSource();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent1 = new Intent(context, DownloadService.class);
        context.stopService(intent1);
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
        if(mReceiver != null){
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        context = null;
    }
}

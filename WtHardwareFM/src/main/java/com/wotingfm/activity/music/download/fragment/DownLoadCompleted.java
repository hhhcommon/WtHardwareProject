package com.wotingfm.activity.music.download.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.wotingfm.activity.music.download.adapter.DownLoadSequAdapter;
import com.wotingfm.activity.music.download.dao.FileInfoDao;
import com.wotingfm.activity.music.download.downloadlist.activity.DownLoadListActivity;
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.util.CommonUtils;

import java.util.List;

/**
 * 已下载
 */
public class DownLoadCompleted extends Fragment implements OnClickListener {
    private FragmentActivity context;
    private View rootView;
    private View headView;
    private List<FileInfo> fileSequList;    // 专辑list
    private FileInfoDao FID;
    private DownLoadSequAdapter adapter;
    private String userId;
    private ListView mListView;
    private Dialog confirmDialog;
    private MessageReceiver receiver;
    private int index;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        if (receiver == null) {
            receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("push_down_completed");
            context.registerReceiver(receiver, filter);
        }
        rootView = inflater.inflate(R.layout.fragment_download_completed, container, false);
        initDao();
        setDownLoadSource();
        return rootView;
    }

    /**
     * 查询数据库当中已完成的数据，此数据传输到adapter中进行适配
     */
    public void setDownLoadSource() {
        mListView = (ListView) rootView.findViewById(R.id.list_view);
        LinearLayout linearUnLogin = (LinearLayout) rootView.findViewById(R.id.lin_status_no);
        userId = CommonUtils.getUserId(context);
        List<FileInfo> fileInfoList = FID.queryFileinfo("true", userId);// 查询当前userId下已经下载完成的list
        if(fileInfoList.size() > 0){
            linearUnLogin.setVisibility(View.GONE);
            fileSequList = FID.GroupFileinfoAll(userId);
            if (fileSequList.size() > 0) {
                for (int i = 0; i < fileSequList.size(); i++) {
                    if (fileSequList.get(i).getSequid().equals("woting")) {
                        //此处应出现添加headView进首项
                        headView = LayoutInflater.from(context).inflate(R.layout.headview_onlinefragment, null);
                        headView.findViewById(R.id.lin_download_single).setOnClickListener(this);
                        mListView.addHeaderView(headView);
                    } else if (i == fileSequList.size() - 1) {
                        if (headView != null) {
                            mListView.removeHeaderView(headView);
                        }
                    }
                }
                adapter = new DownLoadSequAdapter(context, fileSequList);
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(adapter);
                setItemListener();
            }
        } else {
            mListView.setVisibility(View.GONE);
            linearUnLogin.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置监听事件
     */
    private void setItemListener() {
        adapter.setOnListener(new DownLoadSequAdapter.DownLoadDelete() {
            @Override
            public void deletePosition(int position) {
                index = position;
                deleteConfirmDialog();
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DownLoadListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sequname", fileSequList.get(position).getSequname());
                bundle.putString("sequid", fileSequList.get(position).getSequid());
                intent.putExtras(bundle);
                context.startActivityForResult(intent,1);
            }
        });
    }

    private void initDao() {
        FID = new FileInfoDao(context);
    }

    /**
     * 删除对话框
     */
    private void deleteConfirmDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textCancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
        textCancel.setOnClickListener(this);
        TextView textConfirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        textConfirm.setOnClickListener(this);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("确定删除这个已下载的节目?");
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        confirmDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancle:
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:
                confirmDialog.dismiss();
                FID.deletesequ(fileSequList.get(index).getSequname(), userId);
                setDownLoadSource();//重新适配界面操作
                break;
            case R.id.lin_download_single:
                Intent intent = new Intent(context, DownLoadListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("sequname", "单体节目");
                bundle.putString("sequid", "woting");
                intent.putExtras(bundle);
                context.startActivity(intent);
                break;
        }
    }

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("push_down_completed")) {
                setDownLoadSource();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
        context = null;
    }
}

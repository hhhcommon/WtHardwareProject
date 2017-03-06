package com.wotingfm.ui.music.download.downloadlist.main;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.downloadlist.adapter.DownLoadListAdapter;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.main.PlayerFragment;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载列表
 * @author 辛龙
 * 2016年8月8日
 */
public class DownLoadListFragment extends Fragment implements OnClickListener {
    private SearchPlayerHistoryDao dbDao;
    private FileInfoDao FID;
    private List<FileInfo> fileInfoList = new ArrayList<>();
    private DownLoadListAdapter adapter;
    private DecimalFormat df;

    private Dialog confirmDialog;
    private Dialog confirmDialog1;
    private View viewTop;
    private ListView mListView;// 数据列表
    private TextView textSum;
    private TextView textTotalCache;

    private String sequName;// 专辑名 从上一个界面传递过来的
    private String sequId;// 专辑 ID
    private int positionNow = -1;// 标记当前选中的位置
    private FragmentActivity context;
    private View rootView;

    // 初始化数据库对象
    private void initDao() {
        FID = new FileInfoDao(context);
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_downloadlist, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initDao();
            handleIntent();
            initView();
            confirmDialog();
            df = new DecimalFormat("0.00");
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setListValue();// 给 list 赋初值
    }

    // 处理数据传递
    private void handleIntent() {
        sequName = getArguments().getString("sequname");
        sequId = getArguments().getString("sequid");
    }

    // 初始化视图
    private void initView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回按钮
        TextView textHeadName = (TextView) rootView.findViewById(R.id.head_name_tv);// 专辑名
        textHeadName.setText(sequName);

        mListView = (ListView) rootView.findViewById(R.id.lv_downloadlist);// 数据列表

        textSum = (TextView) rootView.findViewById(R.id.tv_sum);
        textTotalCache = (TextView) rootView.findViewById(R.id.tv_totalcache);
        viewTop = rootView.findViewById(R.id.lin_dinglan);
    }

    // 文件不存在删除记录对话框
    private void confirmDialog() {
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textTitle.setText("文件不存在，是否删除这条记录?");

        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialogView);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialogView.findViewById(R.id.tv_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmDialog != null) confirmDialog.dismiss();

                // 这里添加删除数据库事件
                try {
                    FID.deleteFileInfo(fileInfoList.get(positionNow).getLocalurl(), CommonUtils.getUserId(context));
                    setListValue();
                    context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_COMPLETED));
                    ToastUtils.show_always(context, "此目录内已经没有内容");
                } catch (Exception e) {
                    ToastUtils.show_always(context, "文件删除失败，请稍后重试");
                }
            }
        });
    }

    private void setListValue() {
        int sum = 0;
        fileInfoList = FID.queryFileInfo(sequId, CommonUtils.getUserId(context), 0);
        if (fileInfoList.size() != 0) {
            viewTop.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.VISIBLE);
            adapter = new DownLoadListAdapter(context, fileInfoList);
            mListView.setAdapter(adapter);
            setItemListener();
            setInterface();
            textSum.setText("共" + fileInfoList.size() + "个节目");
            for (int i = 0; i < fileInfoList.size(); i++) {
                sum += fileInfoList.get(i).getEnd();
            }
            if (sum != 0) {
                textTotalCache.setText("共" + df.format(sum / 1000.0 / 1000.0) + "MB");
            }
        } else {
            viewTop.setVisibility(View.GONE);
            adapter = new DownLoadListAdapter(context, fileInfoList);
            mListView.setAdapter(adapter);
            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_COMPLETED));
            ToastUtils.show_always(context, "此目录内已经没有内容");
        }
    }

    private void setInterface() {
        adapter.setonListener(new DownLoadListAdapter.DownloadList() {
            @Override
            public void checkPosition(int position) {
                deleteConfirmDialog(position);
                confirmDialog1.show();
            }
        });
    }

    // 删除对话框
    private void deleteConfirmDialog(final int position) {
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textTitle.setText("是否删除这条记录");

        confirmDialog1 = new Dialog(context, R.style.MyDialog);
        confirmDialog1.setContentView(dialogView);
        confirmDialog1.setCanceledOnTouchOutside(false);
        confirmDialog1.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialogView.findViewById(R.id.tv_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog1.dismiss();
            }
        });

        dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog1.dismiss();
                FID.deleteFileInfo(fileInfoList.get(position).getLocalurl(), CommonUtils.getUserId(context));
                try {
                    File file = new File(fileInfoList.get(position).getLocalurl());
                    if (file.exists()) if(!file.delete()) L.w("TAG", fileInfoList.get(position).getLocalurl() + "失败");
                } catch (Exception e) {
                    e.printStackTrace();
                    L.w("TAG", fileInfoList.get(position).getLocalurl() + "失败");
                }
                setListValue();
                context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_COMPLETED));
            }
        });
    }

    private void setItemListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (fileInfoList != null && fileInfoList.size() != 0) {
                    positionNow = position;
                    FileInfo mFileInfo = fileInfoList.get(position);
                    if (mFileInfo.getLocalurl() != null && !mFileInfo.getLocalurl().equals("")) {
                        File file = new File(mFileInfo.getLocalurl());
                        if (file.exists()) {
                            String playername = mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4);
                            String playerimage = mFileInfo.getImageurl();
                            String playerurl = mFileInfo.getUrl();
                            String playerurI = mFileInfo.getLocalurl();
                            String playlocalrurl = mFileInfo.getLocalurl();
                            String playermediatype = "AUDIO";
                            String playercontentshareurl = mFileInfo.getContentShareURL();
                            String plaplayeralltime = mFileInfo.getPlayAllTime();
                            String playerintime = "0";
                            String playercontentdesc = mFileInfo.getContentDescn();
                            String playernum = mFileInfo.getPlayCount();
                            String playerzantype = "0";
                            String playerfrom = mFileInfo.getPlayFrom();
                            String playerfromid = "";
                            String playerfromurl = "";
                            String playeraddtime = Long.toString(System.currentTimeMillis());
                            String bjuserid = CommonUtils.getUserId(context);
                            String ContentFavorite = mFileInfo.getContentFavorite();
                            String ContentId = mFileInfo.getContentId();
                            String sequName = mFileInfo.getSequname();
                            String sequId = mFileInfo.getSequid();
                            String sequImg = mFileInfo.getSequimgurl();
                            String sequDesc = mFileInfo.getSequdesc();

                            // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                            PlayerHistory history = new PlayerHistory(
                                    playername, playerimage, playerurl, playerurI, playermediatype,
                                    plaplayeralltime, playerintime, playercontentdesc, playernum,
                                    playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playercontentshareurl, ContentFavorite,
                                    ContentId, playlocalrurl, sequName, sequId, sequDesc, sequImg);
                            dbDao.deleteHistory(playerurl);
                            dbDao.addHistory(history);
                            if (PlayerFragment.context != null) {
                                MainActivity.changeOne();
                                Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                                Bundle bundle1 = new Bundle();
                                bundle1.putString("text", mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4));
                                push.putExtras(bundle1);
                                context.sendBroadcast(push);
                            } else {
                                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                                et.putString(StringConstant.PLAYHISTORYENTER, "true");
                                et.putString(StringConstant.PLAYHISTORYENTERNEWS, mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4));
                                if (!et.commit()) L.v("commit", "数据 commit 失败!");
                                MainActivity.changeOne();

                            }
                            PlayerActivity.close();
                            dbDao.closedb();
                        } else {// 此处要调对话框，点击同意删除对应的文件信息
                            ToastUtils.show_always(context, "文件已经被删除，是否删除本条记录");
                            positionNow = position;
                            confirmDialog.show();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                PlayerActivity.close();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListView = null;
        textSum = null;
        textTotalCache = null;
        viewTop = null;
        fileInfoList.clear();
        fileInfoList = null;
        adapter = null;
        confirmDialog = null;
        confirmDialog1 = null;
        df = null;
        dbDao = null;
        FID = null;
    }
}

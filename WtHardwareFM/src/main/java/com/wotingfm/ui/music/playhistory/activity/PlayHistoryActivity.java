package com.wotingfm.ui.music.playhistory.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.AppBaseFragmentActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.playhistory.adapter.PlayHistoryAdapter;
import com.wotingfm.widget.TipView;

import java.util.List;

/**
 * 播放历史
 * @author woting11
 */
public class PlayHistoryActivity extends AppBaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener {
    private SearchPlayerHistoryDao dbDao;	// 播放历史数据库
    private List<PlayerHistory> subList;
    private PlayHistoryAdapter adapter;

    private Dialog confirmDialog;
    private Dialog delDialog;// 长按删除数据确认对话框
    private TextView clearEmpty, openEdit;

    private ListView listView;// 数据列表
    private TipView tipView;// 没有数据的提示

    private int index;// 记录位置

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playhistory);

        dbDao = new SearchPlayerHistoryDao(context);    // 初始化数据库

        initDialog();
        initViews();
        initData();
        delDialog();
    }

    // 初始化视图
    private void initViews() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);  // 左上返回键

        listView = (ListView) findViewById(R.id.list_view);
        listView.setOnItemLongClickListener(this);

        tipView = (TipView) findViewById(R.id.tip_view);

        clearEmpty = (TextView) findViewById(R.id.clear_empty); 	// 清空
        clearEmpty.setOnClickListener(this);

        openEdit = (TextView) findViewById(R.id.open_edit); 		// 编辑
        openEdit.setOnClickListener(this);
    }

    // 初始化数据
    private void initData() {
        subList = dbDao.queryHistory();
        if (subList != null && subList.size() > 0) {
            listView.setAdapter(adapter = new PlayHistoryAdapter(context, subList));
        } else {
            clearEmpty.setVisibility(View.GONE);
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_DATA, "您还没有播放过的节目哟\n快去收听节目吧");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:	// 左上角返回键
                finish();
                break;
            case R.id.clear_empty:		// 清空数据
                confirmDialog.show();
                break;
            case R.id.tv_cancle:// 取消删除
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:// 确定删除
                dbDao.deleteHistoryAll();
                subList.clear();
                adapter.notifyDataSetChanged();
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_DATA, "您还没有播放过的节目哟\n快去收听节目吧");
                clearEmpty.setVisibility(View.GONE);
                confirmDialog.dismiss();
                break;
        }
    }

    // 清空所有数据 对话框
    private void initDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("是否清空全部历史记录");

        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 长按 ExpandableListView 的 Item 弹出删除对话框
    private void delDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("确定删除这条播放记录?");
        delDialog = new Dialog(context, R.style.MyDialog);
        delDialog.setContentView(dialog1);
        delDialog.setCanceledOnTouchOutside(false);
        delDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.dismiss();
            }
        });

        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playType = subList.get(index).getPlayerMediaType();

                // "TTS" 类型的删除条件为 ContentID, 其他类型为 url
                if (playType != null && !playType.equals("") && playType.equals("TTS")) {
                    String contentId = subList.get(index).getContentID();
                    dbDao.deleteHistoryById(contentId);
                } else if (playType != null && !playType.equals("") && playType.equals("RADIO")) {
                    String url = subList.get(index).getPlayerUrl();
                    dbDao.deleteHistory(url);
                } else if (playType != null && !playType.equals("") && playType.equals("AUDIO")) {
                    String url = subList.get(index).getPlayerUrl();
                    dbDao.deleteHistory(url);
                }
                subList.remove(index);
                adapter.notifyDataSetChanged();
                delDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        index = position;
        delDialog.show();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context = null;
        clearEmpty = null;
        openEdit = null;
        setContentView(R.layout.activity_null);
    }
}

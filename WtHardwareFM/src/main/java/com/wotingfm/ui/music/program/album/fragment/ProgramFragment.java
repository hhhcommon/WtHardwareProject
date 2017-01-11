package com.wotingfm.ui.music.program.album.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.wotingfm.R;

/**
 * 专辑列表页
 * @author woting11
 */
public class ProgramFragment extends Fragment implements OnClickListener {
    private View rootView;
    private Context context;
    private String tag = "PROGRAM_VOLLEY_REQUEST_CANCEL_TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_program, container, false);
            initView();
        }
        return rootView;
    }

    // 初始化控件
    private void initView() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_download:// 显示下载列表

                break;
            case R.id.tv_quxiao:// 取消

                break;
            case R.id.lin_quanxuan:// 全选

                break;
            case R.id.tv_download:// 下载

                break;
            case R.id.img_sort:// 排序

                break;
            case R.id.img_sort_down:// 倒序

                break;
        }
    }

    // 初始化数据库命令执行对象
    private void initDao() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootView = null;
        context = null;
    }
}

package com.wotingfm.ui.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.music.program.album.anchor.AnchorDetailsActivity;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.RoundImageView;

/**
 * 专辑详情页
 * @author woting11 2016/06/14
 */
public class DetailsFragment extends Fragment implements OnClickListener {
    private Context context;
    private View rootView;
    private RoundImageView imageHead;
    private TextView textAnchor, textContent, textLabel, textConcern;
    private ImageView imageConcern;
    private Dialog dialog;
    private String contentDesc;
    private String tag = "DETAILS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private boolean isConcern;
    private String PersonId;
    private String ContentPub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_details, container, false);
            initView(rootView);
        }
        return rootView;
    }

    // 初始化控件
    private void initView(View view) {
        imageHead = (RoundImageView) view.findViewById(R.id.round_image_head);// 圆形头像
        textAnchor = (TextView) view.findViewById(R.id.text_anchor_name);// 节目名
        textContent = (TextView) view.findViewById(R.id.text_content);// 内容介绍
        textLabel = (TextView) view.findViewById(R.id.text_label);// 标签
        imageConcern = (ImageView) view.findViewById(R.id.image_concern);// 关注
        textConcern = (TextView) view.findViewById(R.id.text_concern);
        LinearLayout linearConcern = (LinearLayout) view.findViewById(R.id.linear_concern);
        linearConcern.setOnClickListener(this);
        textAnchor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_concern://关注
                if (!isConcern) {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus_concern));
                    textConcern.setText("已关注");
                    ToastUtils.show_always(context, "测试---关注成功");
                } else {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus));
                    textConcern.setText("关注");
                    ToastUtils.show_always(context, "测试---取消关注");
                }
                isConcern = !isConcern;
                break;
            case R.id.text_anchor_name:
                if (!TextUtils.isEmpty(PersonId)) {
                    Intent intent = new Intent(context, AnchorDetailsActivity.class);
                    intent.putExtra("PersonId", PersonId);
                    intent.putExtra("ContentPub", ContentPub);
                    startActivity(intent);
                } else {
                    ToastUtils.show_always(context, "此专辑还没有主播哦");
                }
                break;
        }
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
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        rootView = null;
        imageHead = null;
        textAnchor = null;
        textContent = null;
        textLabel = null;
        imageConcern = null;
        dialog = null;
        contentDesc = null;
        textConcern = null;
    }
}

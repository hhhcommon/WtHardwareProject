package com.wotingfm.ui.interphone.group.groupcontrol.groupmanage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupintroduce.GroupIntroduceFragment;
import com.wotingfm.ui.interphone.message.handlegroupapply.HandleGroupApplyFragment;
import com.wotingfm.ui.interphone.message.joingrouplist.JoinGroupListFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.modifygrouppassword.ModifyGroupPasswordFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.transferauthority.TransferAuthorityFragment;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;

public class GroupManagerFragment extends Fragment implements View.OnClickListener {
    private String groupId;

    private TextView tv_group_type;
    private RelativeLayout rl_set_manager;
    private RelativeLayout rl_transfer_authority;
    private RelativeLayout rl_addGroup;
    private RelativeLayout rl_verify_group;
    private String groupType;
    private RelativeLayout rl_modify_password;
    private ArrayList<GroupInfo> list;
    private FragmentActivity context;
    private View rootView;
    private GroupInfo groupNews;
    private String headUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_group_manager, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            setView();
            handleIntent();
        }
        return rootView;
    }

    // 初始化视图
    private void setView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);                                // 返回
        rootView.findViewById(R.id.rl_group_introduce).setOnClickListener(this);                           // 编辑群组资料

        tv_group_type = (TextView) rootView.findViewById(R.id.tv_group_type);                                 // 群类型

        rl_set_manager = (RelativeLayout) rootView.findViewById(R.id.rl_set_manager);                         // 设置群管理员
        rl_set_manager.setOnClickListener(this);

        rl_transfer_authority = (RelativeLayout) rootView.findViewById(R.id.rl_transfer_authority);           // 变更权限
        rl_transfer_authority.setOnClickListener(this);

        rl_addGroup = (RelativeLayout) rootView.findViewById(R.id.rl_addGroup);                               // 加群消息
        rl_addGroup.setOnClickListener(this);

        rl_verify_group = (RelativeLayout) rootView.findViewById(R.id.rl_verify_group);                       // 审核消息消息
        rl_verify_group.setOnClickListener(this);

        rl_modify_password = (RelativeLayout) rootView.findViewById(R.id.rl_modify_password);                 // 修改密码
        rl_modify_password.setOnClickListener(this);
    }

    // 处理请求
    private void handleIntent() {
        groupNews = (GroupInfo) getArguments().getSerializable("group");
        list = (ArrayList<GroupInfo>) getArguments().getSerializable("userlist");
        headUrl = getArguments().getString("GroupImg");
        if (groupNews != null) {
            if (!TextUtils.isEmpty(groupNews.getGroupId())) {
                groupId = groupNews.getGroupId();
            }
            if (!TextUtils.isEmpty(groupNews.getGroupType())) {
                groupType = groupNews.getGroupType();
            }
            switch (groupType) {
                case "0":// 审核群
                    tv_group_type.setText("审核群");
                    rl_addGroup.setVisibility(View.VISIBLE);                            // 审核消息
                    rl_verify_group.setVisibility(View.VISIBLE);                        // 加群消息
                    rl_modify_password.setVisibility(View.GONE);                        // 修改密码
                    break;
                case "1":// 公开群
                    tv_group_type.setText("公开群");
                    rl_addGroup.setVisibility(View.GONE);                               // 审核消息
                    rl_verify_group.setVisibility(View.GONE);                           // 加群消息
                    rl_modify_password.setVisibility(View.GONE);                        // 修改密码

                    break;
                case "2":// 密码群
                    tv_group_type.setText("密码群");
                    rl_addGroup.setVisibility(View.GONE);                               // 审核消息
                    rl_verify_group.setVisibility(View.GONE);                           // 加群消息
                    rl_modify_password.setVisibility(View.VISIBLE);                     // 修改密码
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
            case R.id.rl_set_manager:
                ToastUtils.show_always(context, "设置管理员");
                break;
            case R.id.rl_transfer_authority:
                TransferAuthorityFragment fg4 = new TransferAuthorityFragment();
                Bundle bundle4 = new Bundle();
                bundle4.putString("GroupId", groupId);
                fg4.setArguments(bundle4);
                fg4.setTargetFragment(this, 1);
                DuiJiangActivity.open(fg4);
                break;
            case R.id.rl_addGroup:
                HandleGroupApplyFragment fg3 = new HandleGroupApplyFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putString("GroupId", groupId);
                fg3.setArguments(bundle3);
                fg3.setTargetFragment(this, 2);
                DuiJiangActivity.open(fg3);
                break;
            case R.id.rl_verify_group:
                JoinGroupListFragment fg = new JoinGroupListFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("GroupId", groupId);
                bundle2.putSerializable("userlist", list);
                fg.setArguments(bundle2);
                fg.setTargetFragment(this, 0);
                DuiJiangActivity.open(fg);
                break;
            case R.id.rl_modify_password:
                ModifyGroupPasswordFragment fg2 = new ModifyGroupPasswordFragment();
                Bundle bundle = new Bundle();
                bundle.putString("GroupId", groupId);
                fg2.setArguments(bundle);
                fg2.setTargetFragment(this, 0);
                DuiJiangActivity.open(fg2);
                break;
            case R.id.rl_group_introduce:
                GroupIntroduceFragment fg1 = new GroupIntroduceFragment();
                Bundle bundle5 = new Bundle();
                bundle5.putSerializable("group", groupNews);
                bundle5.putString("GroupImg", headUrl);
                fg1.setArguments(bundle5);
                DuiJiangActivity.open(fg1);
                break;
        }
    }

    // 更新界面
    public void RefreshFragmentManager() {
        Fragment targetFragment = getTargetFragment();
        ((GroupDetailFragment) targetFragment).RefreshFragment();
        DuiJiangActivity.close();
    }
}

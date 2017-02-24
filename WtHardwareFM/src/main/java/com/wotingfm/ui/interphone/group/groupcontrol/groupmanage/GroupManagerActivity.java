package com.wotingfm.ui.interphone.group.groupcontrol.groupmanage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.AppBaseActivity;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.groupintroduce.GroupIntroduceActivity;
import com.wotingfm.ui.interphone.group.groupcontrol.handlegroupapply.HandleGroupApplyActivity;
import com.wotingfm.ui.interphone.group.groupcontrol.joingrouplist.JoinGroupListActivity;
import com.wotingfm.ui.interphone.group.groupcontrol.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.ui.interphone.group.groupcontrol.transferauthority.TransferAuthorityActivity;

import java.util.ArrayList;


public class GroupManagerActivity extends AppBaseActivity implements View.OnClickListener{

    private String groupId;

    private TextView tv_group_type;
    private RelativeLayout rl_set_manager;
    private RelativeLayout rl_transfer_authority;
    private RelativeLayout rl_addGroup;
    private RelativeLayout rl_verify_group;
    private String groupType;
    private RelativeLayout rl_modify_password;
    private ArrayList<GroupInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manager);
        setView();
        handleIntent();
    }


    // 初始化视图
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);                                // 返回
        findViewById(R.id.rl_group_introduce).setOnClickListener(this);                           // 编辑群组资料

        tv_group_type=(TextView)findViewById(R.id.tv_group_type);                                 // 群类型

        rl_set_manager=(RelativeLayout)findViewById(R.id.rl_set_manager);                         // 设置群管理员
        rl_set_manager.setOnClickListener(this);

        rl_transfer_authority=(RelativeLayout)findViewById(R.id.rl_transfer_authority);           // 变更权限
        rl_transfer_authority.setOnClickListener(this);

        rl_addGroup=(RelativeLayout)findViewById(R.id.rl_addGroup);                               // 加群消息
        rl_addGroup.setOnClickListener(this);

        rl_verify_group=(RelativeLayout)findViewById(R.id.rl_verify_group);                       // 审核消息消息
        rl_verify_group.setOnClickListener(this);

        rl_modify_password=(RelativeLayout)findViewById(R.id.rl_modify_password);                 // 修改密码
        rl_modify_password.setOnClickListener(this);
    }
  /*  */

    // 处理请求
    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        GroupInfo groupNews = (GroupInfo)bundle.getSerializable("group");
        list = (ArrayList<GroupInfo>) bundle.getSerializable("userlist");
        if(groupNews!=null){
            if(!TextUtils.isEmpty(groupNews.getGroupId())){
                 groupId=groupNews.getGroupId();
            }
            if(!TextUtils.isEmpty(groupNews.getGroupType())){
                 groupType=groupNews.getGroupType();
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
           // if (groupCreator != null && groupCreator.equals(CommonUtils.getUserId(context))) 判断管理员的

        }
    }


    // 跳转到新的 Activity
    private void startToActivity(Class toClass) {
        Intent intent = new Intent(context, toClass);
        Bundle bundle = new Bundle();
        bundle.putString("GroupId", groupId);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // 跳转到新的 Activity  带返回值
    private void startToActivity(Class toClass, int requestCode) {
        Intent intent = new Intent(context, toClass);
        Bundle bundle = new Bundle();
        bundle.putString("GroupId", groupId);
        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.rl_set_manager:
                 startActivity(new Intent(context, GroupIntroduceActivity.class));
                break;
            case R.id.rl_transfer_authority:
                startToActivity(TransferAuthorityActivity.class,1);
                break;
            case R.id.rl_addGroup:
                startToActivity(HandleGroupApplyActivity.class, 2);
                break;
            case R.id.rl_verify_group:
                Intent intent2 = new Intent(context, JoinGroupListActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("GroupId", groupId);
                bundle2.putSerializable("userlist", list);
                intent2.putExtras(bundle2);
                startActivity(intent2);
                break;
            case R.id.rl_modify_password:
                startToActivity(ModifyGroupPasswordActivity.class);
                break;
            case R.id.rl_group_introduce:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        setContentView(R.layout.activity_null);
    }
}

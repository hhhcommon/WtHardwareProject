package com.wotingfm.activity.common.interphone.groupmanage.groupdetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.allgroupmember.AllGroupMemberActivity;
import com.wotingfm.activity.common.interphone.groupmanage.memberadd.MemberAddActivity;
import com.wotingfm.activity.common.interphone.groupmanage.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.activity.common.interphone.groupmanage.transferauthority.TransferAuthority;


/**
 * Created by wz on 2016/8/24 0024.
 */
public class GroupDetailAcitivity extends Activity implements View.OnClickListener {

    private Context context;
    private TextView mback;
    private EditText mgroupname;
    private EditText mgroupsign;
    private TextView mtv_talk;
    private LinearLayout lin_ewm;
    private ImageView mImgTouxiang;
    private ImageView mImgewm;
    private TextView mtv_number;
    private GridView gv_allperson;
    private TextView mtv_autoadd;
    private TextView mtv_ewmadd;
    private TextView mtv_exit;
    private RelativeLayout rl_allperson;
    private RelativeLayout rl_transferauthority;
    private RelativeLayout rl_modifygpassword;
    private RelativeLayout rl_addGroup;
    private RelativeLayout rl_vertiygroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_groupdetail);
        setView();
        handleIntent();
        setListener();


    }

    //处理从通讯录传入的值
    private void handleIntent() {
    }

    //设置监听
    private void setListener() {
        mback.setOnClickListener(this);
        mtv_talk.setOnClickListener(this);
        lin_ewm.setOnClickListener(this);
        rl_allperson.setOnClickListener(this);
        mtv_autoadd.setOnClickListener(this);
        mtv_ewmadd.setOnClickListener(this);
        mtv_exit.setOnClickListener(this);
        rl_transferauthority.setOnClickListener(this);
        rl_modifygpassword.setOnClickListener(this);
        rl_addGroup.setOnClickListener(this);
        rl_vertiygroup.setOnClickListener(this);
    }
    //设置界面
    private void setView() {
        mback=(TextView)findViewById(R.id.wt_back);
        mImgTouxiang=(ImageView)findViewById(R.id.image_portrait);
        mgroupname=(EditText)findViewById(R.id.et_group_name);
        mgroupsign=(EditText)findViewById(R.id.et_group_sign);
        mtv_talk=(TextView)findViewById(R.id.starttalk);
        lin_ewm=(LinearLayout)findViewById(R.id.lin_ewm);
        mImgewm=(ImageView)findViewById(R.id.img_ewm);
        rl_allperson=(RelativeLayout)findViewById(R.id.rl_allperson);
        rl_transferauthority=(RelativeLayout)findViewById(R.id.rl_transferauthority);
        rl_modifygpassword=(RelativeLayout)findViewById(R.id.rl_modifygpassword);
        rl_addGroup=(RelativeLayout)findViewById(R.id.rl_addGroup);
        rl_vertiygroup=(RelativeLayout)findViewById(R.id.rl_vertiygroup);
        mtv_number=(TextView)findViewById(R.id.tv_number);
        gv_allperson=(GridView)findViewById(R.id.gridView);
        mtv_autoadd=(TextView)findViewById(R.id.auto_add);
        mtv_ewmadd=(TextView)findViewById(R.id.ewm_add);
        mtv_exit=(TextView)findViewById(R.id.tv_exit);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    // 接口实现方法
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wt_back:
                Toast.makeText(context,"R.id.wt_back",Toast.LENGTH_LONG).show();
                break;
            case R.id.starttalk:
                Toast.makeText(context,"R.id.starttalk",Toast.LENGTH_LONG).show();
                break;
            case R.id.rl_allperson:
                startActivity(new Intent(this, AllGroupMemberActivity.class));
                break;
            case R.id.lin_ewm:
                Toast.makeText(context,"R.id.lin_ewm",Toast.LENGTH_LONG).show();
                break;
            case R.id.auto_add:
                Toast.makeText(context,"R.id.auto_add",Toast.LENGTH_LONG).show();
                break;
            case R.id.ewm_add:
                Toast.makeText(context,"R.id.ewm_add",Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_exit:
                Toast.makeText(context,"R.id.tv_exit",Toast.LENGTH_LONG).show();
                break;
            case R.id.rl_transferauthority:
                startActivity(new Intent(this, TransferAuthority.class));
                break;
            case R.id.rl_modifygpassword:
                startActivity(new Intent(this, ModifyGroupPasswordActivity.class));
                break;
            case R.id.rl_addGroup:

                break;
            case R.id.rl_vertiygroup:
                break;
        }
    }

}

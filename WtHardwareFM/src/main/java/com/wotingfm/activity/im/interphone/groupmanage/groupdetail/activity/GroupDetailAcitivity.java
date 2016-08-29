package com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.activity.AllGroupMemberActivity;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.model.UserInfo;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity.adapter.GroupTalkAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.activity.im.interphone.groupmanage.transferauthority.TransferAuthority;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组详情界面
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
    private String groupid;
    private String imageurl;
    private Dialog dialog;
    private String tag = "TALKGROUPNEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private List<UserInfo> list;
    private Intent pushintent;
    private ArrayList<UserInfo> userlist=new ArrayList<>();
    private GroupTalkAdapter adapter;
    private Boolean IsCreator=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_groupdetail);
        InitActivity();// 初始化透明状态栏，统一回收的方法
        handleIntent();// 处理其他页面传入的数据
        setview();// 设置界面
        setlistener();// 设置监听
            /*   if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {*/
        if(groupid!=null){
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        }else{
            ToastUtils.show_allways(context,"获取组ID失败");
        }
       /* } else {
            ToastUtils.show_allways(context, "网络失败，请检查网络");
        }*/
        pushintent=new Intent("push_refreshlinkman");
    }

    // 获取群成员列表
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId","6c310f2884a7");
            jsonObject.put("GroupId","81ce725fa1d3");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            private String srclist;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    srclist = result.getString("UserList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                list = new Gson().fromJson(srclist, new TypeToken<List<UserInfo>>(){}.getType());
                if (list == null || list.size() == 0) {
                    ToastUtils.show_allways(context,"您当前没有数据");
                    context.sendBroadcast(pushintent);
                }else{
                    //处理组装数据 判断list和Creater大小进行组装
                    //如果是管理员 判断list是否>3 大于3出现删除按钮 如果list>6截取前六条添加增加删除按钮
                    UserInfo add=new UserInfo();
                    add.setType(2);
                    UserInfo del=new UserInfo();
                    add.setType(3);
                    userlist.clear();
                    if(IsCreator){
                        if(list.size()>0&&list.size()<3){
                            list.add(add);
                            userlist.addAll(list);
                        }else if(list.size()>3&&list.size()<7){
                            list.add(add);
                            list.add(del);
                            userlist.addAll(list);
                        }else if(list.size()>=7){
                           for(int i=0;i<6;i++){
                               userlist.add(list.get(i));
                           }
                            userlist.add(add);
                            userlist.add(del);
                        }
                    }else {
                        //如果不是管理员 判断list是否大于8 大于8取前7条 添加添加按钮
                        if(list.size()>7){
                            for(int i=0;i<7;i++){
                                userlist.add(list.get(i));
                            }
                            userlist.add(add);
                        }else{
                            list.add(add);
                            userlist.addAll(list);
                        }
                    }
                    if(adapter==null){
                        adapter=new GroupTalkAdapter(context,userlist);
                        gv_allperson.setAdapter(adapter);
                    }else{
                        adapter.notifyDataSetChanged();
                    }
                    setItemListener();
                }
            }
            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

    }

    private void setItemListener() {
    }

    private void InitActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		//透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	//透明导航栏
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);
    }

    //处理从通讯录传入的值
    private void handleIntent() {
        groupid= this.getIntent().getStringExtra("GroupId");
        imageurl=this.getIntent().getStringExtra("ImageUrl");
    }

    //设置监听
    private void setlistener() {
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
    private void setview() {
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
                Intent intent=new Intent(this, AllGroupMemberActivity.class);
                //此处测试
                intent.putExtra("GroupId","81ce725fa1d3");
                startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mback=null;
        mImgTouxiang=null;
        mgroupname=null;
        mgroupsign=null;
        mtv_talk=null;
        lin_ewm=null;
        mImgewm=null;
        rl_allperson=null;
        rl_transferauthority=null;
        rl_modifygpassword=null;
        rl_addGroup=null;
        rl_vertiygroup=null;
        mtv_number=null;
        gv_allperson=null;
        mtv_autoadd=null;
        mtv_ewmadd=null;
        mtv_exit=null;
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(this);
    }
}

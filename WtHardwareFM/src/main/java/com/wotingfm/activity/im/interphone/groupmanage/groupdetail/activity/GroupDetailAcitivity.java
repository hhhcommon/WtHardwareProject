package com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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
import com.wotingfm.activity.im.interphone.chat.model.TalkListGP;
import com.wotingfm.activity.im.interphone.creategroup.frienddetails.TalkPersonNewsActivity;
import com.wotingfm.activity.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.activity.AllGroupMemberActivity;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.adapter.GroupTalkAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.handlegroupapply.HandleGroupApplyActivity;
import com.wotingfm.activity.im.interphone.groupmanage.joingrouplist.activity.JoinGroupListActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberadd.activity.MemberAddActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberdel.MemberDelActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.activity.im.interphone.groupmanage.transferauthority.TransferAuthority;
import com.wotingfm.activity.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.activity.im.interphone.message.model.GroupInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private String groupid;
    private String imageurl;
    private Dialog dialog;
    private String tag = "TALKGROUPNEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private List<UserInfo> list;
    private Intent pushintent;
    private ArrayList<UserInfo> userlist=new ArrayList<UserInfo>();

    private Boolean IsCreator=false;
    private Dialog confirmdialog;
    private ImageView mimg_update;
    private boolean IsUpadate=false;
    private MessageReceivers Receiver;
    private GroupTalkAdapter adapter;
    private String type;
    private String number;
    private String creator;
    private String signature;
    private String myAlias;
    private String name;
    private String groupdesc;
    private String grouptype;
    private FindGroupNews news;
    private String url12;
    private ImageLoader imgloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_groupdetail);
        InitActivity();// 初始化透明状态栏，统一回收的方法
        handleIntent();// 处理其他页面传入的数据
        setview();// 设置界面
        setData();//设置data View
        setlistener();// 设置监听
        InitConfirmDialog();//初始化确认对话框

        if(Receiver==null) {
            Receiver=new MessageReceivers();
            IntentFilter filters=new IntentFilter();
            filters.addAction(BroadcastConstant.REFRESH_GROUP);
            context.registerReceiver(Receiver, filters);
        }

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

    }

    private void setData() {

        if (name == null || name.equals("")) {
            name = "我听科技";
            mgroupname.setText(name);
        } else {
            mgroupname.setText(name);
        }

        if (signature == null || signature.equals("")) {
            mgroupsign.setText(name);
        } else {
            mgroupsign.setText(signature);
        }

        if (imageurl == null || imageurl.equals("") || imageurl.equals("null")	|| imageurl.trim().equals("")) {
            mImgTouxiang.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            if(imageurl.startsWith("http:")){
                url12=imageurl;
            }else{
                url12 = GlobalConfig.imageurl+imageurl;
            }
            imgloader.DisplayImage(url12.replace("\\/", "/"), mImgTouxiang,false, false, null, null);
        }
        news = new FindGroupNews();
        news.setGroupName(name);
        news.setGroupType(grouptype);
        news.setGroupCreator(creator);
        news.setGroupImg(imageurl);
        news.setGroupId(groupid);
        news.setGroupNum(number);

        if (creator != null && !creator.equals("")) {
            if (creator.equals(CommonUtils.getUserId(context))) {
                //自己是群主
                IsCreator=true;
                if(grouptype!=null&& !grouptype.equals("")){
                    if(grouptype.equals("0")){
                        //审核群

                     // 审核消息
                       // 加群消息 lin_jiaqun
                        rl_modifygpassword.setVisibility(View.VISIBLE);
                        rl_addGroup.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);
                    }else if(grouptype.equals("2")){
                        //密码群
                     	// 加群消息 lin_jiaqun
                        rl_addGroup.setVisibility(View.GONE);
                        rl_modifygpassword.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);
                    }else{
                        //公开群
                        rl_addGroup.setVisibility(View.GONE);
                        rl_modifygpassword.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);

                    }
                }else{
                        IsCreator=false;
                		rl_addGroup.setVisibility(View.GONE);
                        rl_modifygpassword.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.GONE);
                        rl_vertiygroup.setVisibility(View.GONE);
                        mtv_exit.setVisibility(View.GONE);
                }
            } else {
                IsCreator=false;
                rl_addGroup.setVisibility(View.GONE);
                rl_modifygpassword.setVisibility(View.GONE);
                rl_transferauthority.setVisibility(View.GONE);
                rl_vertiygroup.setVisibility(View.GONE);

            }
        }
    }
    private void InitConfirmDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancle = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        confirmdialog = new Dialog(this, R.style.MyDialog);
        confirmdialog.setContentView(dialog1);
        confirmdialog.setCanceledOnTouchOutside(true);
        confirmdialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmdialog.dismiss();
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    confirmdialog.dismiss();*/
                    SendExitRequest();
                /*} else {
                    ToastUtils.show_allways(context, "网络失败，请检查网络");
                }*/
            }
        });
    }

    // 获取群成员列表
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId",CommonUtils.getUserId(context));
            jsonObject.put("GroupId",groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
           String srclist;
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
                    int sum=-1;
                    sum=list.size();
                    if(sum!=-1){
                        mtv_number.setText("("+sum+")");
                    }
                    UserInfo add=new UserInfo();
                    add.setType(2);
                    UserInfo del=new UserInfo();
                    del.setType(3);
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

        gv_allperson.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (userlist.get(position).getType() == 1) {
                    if(userlist.get(position).getUserId().equals(CommonUtils.getUserId(context))){
						/*	ToastUtil.show_allways(context, "点击的是本人");*/
                    }else{
                        boolean isfriend = false;
                        if (GlobalConfig.list_person != null&& GlobalConfig.list_person.size() != 0) {
                            for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                                if (userlist.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                                    isfriend = true;
                                    break;
                                }
                            }
                        } else {
                            // 不是我的好友
                            isfriend = false;
                        }

                        Boolean b=isfriend;
                        if (true) {
                            Intent intent = new Intent(context, TalkPersonNewsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "TalkGroupNewsActivity_p");
                            bundle.putString("id", groupid);
                            bundle.putSerializable("data", userlist.get(position));
                            intent.putExtras(bundle);
                            startActivityForResult(intent, 2);
                            ToastUtils.show_allways(context,"是好友，跳转到好友页面");
                        } else {
                            Intent intent = new Intent(context,FriendAddActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "TalkGroupNewsActivity_p");
                            bundle.putSerializable("data", userlist.get(position));
                            bundle.putString("id", groupid);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, 2);
                            ToastUtils.show_allways(context,"非好友跳转到群陌生人");
                        }
                    }
                } else if (userlist.get(position).getType() == 2) {
                    Intent intent = new Intent(context,MemberAddActivity.class);
                    intent.putExtra("GroupId", groupid);
                    startActivityForResult(intent, 2);
            /*        ToastUtils.show_allways(context,"加");*/
                } else if (userlist.get(position).getType() == 3) {
                    Intent intent = new Intent(context,MemberDelActivity.class);
                    intent.putExtra("GroupId", groupid);
                    startActivityForResult(intent, 3);
                  /*  ToastUtils.show_allways(context,"减");*/
                }
            }
        });
    }

    private void InitActivity() {
        imgloader = new ImageLoader(context);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		//透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	//透明导航栏
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);
    }

    //处理从通讯录传入的值
    private void handleIntent() {
        pushintent=new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN);
        type = this.getIntent().getStringExtra("type");
        if (type == null || type.equals("")) {
        } else if (type.equals("talkoldlistfragment")) {
            // 聊天界面传过来
            TalkListGP data = (TalkListGP) this.getIntent().getSerializableExtra("data");
            number = data.getGroupNum();
            name = data.getName();
            imageurl = data.getPortrait();
            groupid = data.getId();
            if(data.getGroupManager()==null||data.getGroupManager().equals("")){
                creator = data.getGroupCreator();
            }else{
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupdesc = data.getGroupDesc();
            myAlias = data.getGroupMyAlias();
            grouptype = data.getGroupType();
        } else if (type.equals("talkpersonfragment")) {
            // 通讯录界面传过来
            TalkGroupInside data = (TalkGroupInside) this.getIntent().getSerializableExtra("data");
            name = data.getGroupName();
            imageurl = data.getGroupImg();
            groupid = data.getGroupId();
            if(data.getGroupManager()==null||data.getGroupManager().equals("")){
                creator = data.getGroupCreator();
            }else{
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupdesc = data.getGroupMyDesc();
            myAlias = data.getGroupMyAlias();
            number = data.getGroupNum();
            grouptype = data.getGroupType();
        } else if (type.equals("groupaddactivity")) {
            // 申请加入组成功后进入
            FindGroupNews data = (FindGroupNews) this.getIntent().getSerializableExtra("data");
            name = data.getGroupName();
            imageurl = data.getGroupImg();
            groupid = data.getGroupId();
            number = data.getGroupNum();
            if(data.getGroupManager()==null||data.getGroupManager().equals("")){
                creator = data.getGroupCreator();
            }else{
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupdesc = data.getGroupOriDesc();
            myAlias = data.getGroupMyAlias();
            grouptype = data.getGroupType();
        } else if (type.equals("findActivity")) {
            // 处理组邀请时进入
            GroupInfo f = (GroupInfo) this.getIntent().getSerializableExtra("data");
            name = f.getGroupName();
            imageurl = f.getGroupImg();
            groupid = f.getGroupId();
            number = f.getGroupNum();
            if(f.getGroupManager()==null||f.getGroupManager().equals("")){
                creator = f.getGroupCreator();
            }else{
                creator = f.getGroupManager();
            }
            signature = f.getGroupSignature();
            grouptype = f.getGroupType();
			/* myAlias=f.get; */
        } else if (type.equals("FindNewsResultActivityApp")) {
            // 处理组邀请时进入
            FindGroupNews news = (FindGroupNews) this.getIntent().getSerializableExtra("contact");
            imageurl = news.getGroupImg();
            name = news.getGroupName();
            groupid = news.getGroupId();
            number = news.getGroupNum();
            grouptype = news.getGroupType();
            creator = CommonUtils.getUserId(context);
            signature = news.getGroupSignature();
        }
        // 用于查找群内成员
        if (groupid == null || groupid.trim().equals("")) {
            groupid = "00";// 待定，此处为没有获取到groupid
        }
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
        mimg_update.setOnClickListener(this);
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
        mimg_update=(ImageView)findViewById(R.id.img_update);//修改群资料
        //初始化界面布局
        mgroupname.setEnabled(false);
        mgroupsign.setEnabled(false);
        //取消selector
        gv_allperson.setSelector(new ColorDrawable(Color.TRANSPARENT));			// 取消GridView中Item选中时默
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wt_back:
                 finish();
                break;
            case R.id.starttalk:
                Toast.makeText(context,"R.id.starttalk",Toast.LENGTH_LONG).show();
                break;
            case R.id.rl_allperson:
                Intent intent=new Intent(this,AllGroupMemberActivity.class);
                //此处测试
                intent.putExtra("GroupId",groupid);
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
                confirmdialog.show();
                break;
            case R.id.rl_transferauthority:
                Intent intent1=new Intent(this, TransferAuthority.class);
                intent1.putExtra("GroupId",groupid);
                startActivity(intent1);
                break;
            case R.id.rl_modifygpassword:
                Intent intent2=new Intent(this, ModifyGroupPasswordActivity.class);
                intent2.putExtra("GroupId",groupid);
                startActivity(intent2);
                break;
            case R.id.rl_addGroup:
                Intent intent3=new Intent(this, HandleGroupApplyActivity.class);
                intent3.putExtra("GroupId",groupid);
                startActivity(intent3);
                break;
            case R.id.rl_vertiygroup:
                Intent intent4=new Intent(this, JoinGroupListActivity.class);
                intent4.putExtra("GroupId",groupid);
                startActivity(intent4);
                break;
            case R.id.img_update:
                if(!IsUpadate){
                    mgroupname.setEnabled(true);
                    mgroupsign.setEnabled(true);
                    IsUpadate=true;
                }else{
                    String GroupName=mgroupname.getText().toString().trim();
                    String GroupSign=mgroupsign.getText().toString().trim();
                    if(GroupName==null&& GroupName.equals("")){
                           GroupName="";
                    }
                    if(GroupSign==null&&GroupName.equals("")){
                       GroupSign="";
                    }
                  /*  if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在提交本次修改");*/
                        update(GroupName,GroupSign);

                    mgroupname.setEnabled(false);/* } else {
                        ToastUtils.show_allways(context, "网络失败，请检查网络");
                    }*/
                    mgroupsign.setEnabled(false);
                    IsUpadate=false;
                }
                break;
        }
    }

    // 更改群备注及信息
    private void update(String GroupName, String GroupSign) {
        JSONObject jsonObject =VolleyRequest.getJsonObject(context);
        try {
            // 公共请求属性
            jsonObject.put("GroupId", groupid);
            jsonObject.put("GroupName",GroupName);
            jsonObject.put("GroupSignature",GroupSign);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.UpdateGroupInfoUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && !ReturnType.equals("")) {
                    if (ReturnType.equals("1001") || ReturnType.equals("10011")) {

                      sendBroadcast(pushintent);
                    } else {
                        if (ReturnType.equals("0000")) {
                            ToastUtils.show_allways(context,"无法获取相关的参数");
                        } else if (ReturnType.equals("1000")) {
                            ToastUtils.show_allways(context,"无法获取用户组id");
                        } else if (ReturnType.equals("1101")) {
                            ToastUtils.show_allways(context,"成功返回已经在用户组");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_allways(context,"用户不存在");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_allways(context,"用户组不存在");
                        } else if (ReturnType.equals("1011")) {
                            ToastUtils.show_allways(context,"用户不在改组，无法删除");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_allways(context,"异常返回值");
                        } else {
                            ToastUtils.show_allways(context, "消息异常");
                        }
                    }
                } else {
                    ToastUtils.show_allways(context,"ReturnType不能为空");
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

    // 退出群组
    private void SendExitRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(context));
            jsonObject.put("GroupId", groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.ExitGroupurl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && !ReturnType.equals("")) {
                    if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                        ToastUtils.show_allways(context,"已经成功退出该组");
                        sendBroadcast(pushintent);
                        SharedPreferences sp = getSharedPreferences("wotingfm",Context.MODE_PRIVATE);
                            SharedPreferences.Editor et = sp.edit();
                            et.putString(StringConstant.PERSONREFRESHB, "true");
                            et.commit();
                        finish();
                    } else {
                        if (ReturnType.equals("0000")) {
                            ToastUtils.show_allways(context,"无法获取相关的参数");
                        } else if (ReturnType.equals("1000")) {
                            ToastUtils.show_allways(context,"无法获取用户组id");
                        } else if (ReturnType.equals("1101")) {
                            ToastUtils.show_allways(context,"成功返回已经在用户组");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_allways(context,"用户不存在");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_allways(context,"用户组不存在");
                        } else if (ReturnType.equals("1011")) {
                            ToastUtils.show_allways(context,"用户不在改组，无法删除");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_allways(context,"异常返回值");
                        }
                    }
                } else {
                    ToastUtils.show_allways(context,"ReturnType不能为空");
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
    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(BroadcastConstant.REFRESH_GROUP)){
                send();
            /*    ToastUtils.show_allways(context,"收到了群员信息变化的广播");*/
            }
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
        unregisterReceiver(Receiver);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(this);
    }
}

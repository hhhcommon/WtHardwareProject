package com.wotingfm.ui.common.welcome.fragment;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.mine.person.login.LoginActivity;
import com.wotingfm.ui.mine.person.register.RegisterActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.BitmapUtils;

/**
 * 第三张引导页
 * 作者：xinlong on 2016/4/27 21:18
 * 邮箱：645700751@qq.com
 */
public class WelcomeCFragment extends Fragment implements OnClickListener {
    private FragmentActivity context;
    private Bitmap bmp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View rootView = inflater.inflate(R.layout.item_welcomec, container, false);
        ImageView imageView1 = (ImageView) rootView.findViewById(R.id.imageView1);
        bmp = BitmapUtils.readBitMap(context, R.mipmap.welcomec);
        imageView1.setImageBitmap(bmp);
        LinearLayout lin_enter = (LinearLayout) rootView.findViewById(R.id.lin_enter);//进入
        TextView tv_login = (TextView) rootView.findViewById(R.id.tv_login);//登录
        TextView tv_register = (TextView) rootView.findViewById(R.id.tv_register);//注册
        lin_enter.setOnClickListener(this);
        tv_login.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_enter:        //进入主页
                startActivity(new Intent(context, MainActivity.class));
                Editor et = BSApplication.SharedPreferences.edit();
                et.putString(StringConstant.FIRST, "1");
                et.commit();
                getActivity().finish();    //进入主页后，父级activity关闭
                break;
            case R.id.tv_login:            //进入登录状态
                Intent intent_login = new Intent(context, LoginActivity.class);
                intent_login.putExtra("type", 1);
                startActivityForResult(intent_login, 1);
                break;
            case R.id.tv_register:        //进入注册状态
                Intent intent_register = new Intent(context, RegisterActivity.class);
                intent_register.putExtra("type", 1);
                startActivityForResult(intent_register, 2);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:            //从登录界面返回，1登录成功，关闭当前界面
                if (resultCode == 1) {
                    //保存引导页查看状态
                    Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.FIRST, "1");
                    et.commit();
                    startActivity(new Intent(context, MainActivity.class));
                    //进入主页后，父级activity关闭
                    getActivity().finish();
                }
                break;
            case 2:            //从注册界面返回，1登录成功，关闭当前界面
                if (resultCode == 1) {
                    //保存引导页查看状态
                    Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.FIRST, "1");
                    et.commit();
                    //进入主页后，父级activity关闭
                    startActivity(new Intent(context, MainActivity.class));
                    getActivity().finish();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }
}

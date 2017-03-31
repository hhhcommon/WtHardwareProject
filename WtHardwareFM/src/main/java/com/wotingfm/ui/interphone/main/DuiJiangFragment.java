package com.wotingfm.ui.interphone.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.ui.common.scanning.activity.CaptureActivity;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.find.main.FindFragment;
import com.wotingfm.ui.interphone.group.creategroup.create.CreateGroupFragment;
import com.wotingfm.ui.interphone.linkman.fragment.LinkManFragment;
import com.wotingfm.ui.interphone.message.messagecenter.fragment.MessageFragment;
import com.wotingfm.ui.interphone.simulation.SimulationInterphoneFragment;
import com.wotingfm.ui.mine.person.login.LoginActivity;

import java.util.ArrayList;

/**
 * 对讲activity
 * 作者：xinlong on 2016/1/19 21:18
 * 邮箱：645700751@qq.com
 */
public class DuiJiangFragment extends Fragment {
    private static TextView view1;
    private static TextView view2;
    private View lin_more;
    private SharedPreferences sharedPreferences = BSApplication.SharedPreferences;
    private PopupWindow addDialog;
    private static ViewPager mPager;
    private static FragmentActivity context;
    private View rootView;
    public static DuiJiangFragment ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_duijiang, container, false);
            rootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            ct=this;
            context = getActivity();
            InitTextView();        // 初始化视图
            InitViewPager();       // 初始化ViewPager
            dialog();              // 初始化功能弹出框
        }
        return rootView;
    }

    /*
     * "更多" 对话框
     */
    private void dialog() {
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialoga, null);
        View lin_news = dialog.findViewById(R.id.lin_news);    // 新消息
        LinearLayout lin_a = (LinearLayout) dialog.findViewById(R.id.lin_a);        // 添加好友
        LinearLayout lin_b = (LinearLayout) dialog.findViewById(R.id.lin_b);        // 加入群组
        LinearLayout lin_c = (LinearLayout) dialog.findViewById(R.id.lin_c);        // 创建群组
        LinearLayout lin_d = (LinearLayout) dialog.findViewById(R.id.lin_d);        // 扫一扫
        LinearLayout lin_e = (LinearLayout) dialog.findViewById(R.id.lin_e);        // 模拟对讲

        /**
         * 跳转到新消息
         */
        lin_news.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
                if (!login.trim().equals("") && login.equals("true")) {

                } else {

                }
                //				startActivity(new Intent(context, ContactAddInformationActivity.class));
                addDialog.dismiss();
            }
        });

		/*
         * 跳转到添加好友
		 */
        lin_a.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
                if (!login.trim().equals("") && login.equals("true")) {

                    FindFragment fg = new FindFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_PERSON);
                    fg.setArguments(bundle);
                    DuiJiangActivity.open(fg);

                } else {
                    startActivity(new Intent(context, LoginActivity.class));
                }
                addDialog.dismiss();
            }
        });

		/*
         * 跳转到加入群组
		 */
        lin_b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
                if (!login.trim().equals("") && login.equals("true")) {

                    FindFragment fg = new FindFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_GROUP);
                    fg.setArguments(bundle);
                    DuiJiangActivity.open(fg);

                } else {
                    startActivity(new Intent(context, LoginActivity.class));
                }
                addDialog.dismiss();
            }
        });

		/*
		 * 跳转到创建讨论组
		 */
        lin_c.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
                if (!login.trim().equals("") && login.equals("true")) {
                    CreateGroupFragment fg = new CreateGroupFragment();
                    DuiJiangActivity.open(fg);
                } else {
                    startActivity(new Intent(context, LoginActivity.class));
                }
                addDialog.dismiss();
            }
        });

		/*
		 * 跳转到扫描界面
		 */
        lin_d.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
                if (!login.trim().equals("") && login.equals("true")) {
                    startActivity(new Intent(context,CaptureActivity.class));
                } else {
                    startActivity(new Intent(context, LoginActivity.class));
                }
                addDialog.dismiss();
            }
        });

        /*
		 * 跳转模拟对讲
		 */
        lin_e.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                SimulationInterphoneFragment fg = new SimulationInterphoneFragment();
                DuiJiangActivity.open(fg);

                addDialog.dismiss();
            }
        });


        addDialog = new PopupWindow(dialog);
//		addDialog = new PopupWindow(dialog, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        // 使其聚集
        addDialog.setFocusable(true);
        // 在android4.0/5.0系统上点击内容外部区域无法关闭问题
//		addDialog.setBackgroundDrawable(new BitmapDrawable());
        addDialog.setBackgroundDrawable(new ColorDrawable(0x00000000));
        // 设置允许在外点击消失
        addDialog.setOutsideTouchable(true);
        // 控制popupwindow的宽度和高度自适应
        dialog.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        addDialog.setWidth(dialog.getMeasuredWidth());
        addDialog.setHeight(dialog.getMeasuredHeight());


        //		addDialog = new Dialog(context, R.style.MyDialog_duijiang);
        //		addDialog.setContentView(dialog);
        //		addDialog.setCanceledOnTouchOutside(true);
        //		addDialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
        //		addDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    /*
     * 初始化视图
     */
    private void InitTextView() {
        lin_more = rootView.findViewById(R.id.lin_more);
        View lin_news = rootView.findViewById(R.id.lin_news);
		/*
		 * 跳转到新消息界面
		 */
        lin_news.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageFragment fg = new MessageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "group");
                fg.setArguments(bundle);
                DuiJiangActivity.open(fg);
            }
        });
        view1 = (TextView) rootView.findViewById(R.id.tv_guid1);
        view2 = (TextView) rootView.findViewById(R.id.tv_guid2);
        view1.setOnClickListener(new txListener(0));
        view2.setOnClickListener(new txListener(1));

		/*
		 * 弹出功能弹出框
		 */
        lin_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addDialog != null && addDialog.isShowing()) {
                    addDialog.dismiss();
                } else {
                    //					addDialog.show();
                    addDialog.showAsDropDown(lin_more);
                }
            }
        });
    }

    /*
     * TextView 点击事件监听
     */
    public class txListener implements OnClickListener {
        private int index = 0;

        public txListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
            if (index == 0) {
             /*   view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                view2.setTextColor(context.getResources().getColor(R.color.white));
                view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
                view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));*/
                view1.setTextColor(context.getResources().getColor(R.color.white));
                view2.setTextColor(context.getResources().getColor(R.color.gray));
                view1.setTextSize(25);
                view2.setTextSize(17);
            } else if (index == 1) {
               /* view1.setTextColor(context.getResources().getColor(R.color.white));
                view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
                view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));*/
                view1.setTextColor(context.getResources().getColor(R.color.gray));
                view2.setTextColor(context.getResources().getColor(R.color.white));
                view1.setTextSize(17);
                view2.setTextSize(25);
            }
        }
    }

    public static void update() {
        mPager.setCurrentItem(0);
      /*  view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
        view2.setTextColor(context.getResources().getColor(R.color.white));
        view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
        view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));*/
        view1.setTextColor(context.getResources().getColor(R.color.white));
        view1.setTextSize(25);
        view2.setTextColor(context.getResources().getColor(R.color.gray));
        view2.setTextSize(17);
    }

    /*
     * 初始化ViewPager
     */
    public void InitViewPager() {
        mPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        Fragment btFragment = new ChatFragment();                     // 电台首页
        Fragment btFragment1 = new LinkManFragment();                 // 通讯录
        fragmentList.add(btFragment);
        fragmentList.add(btFragment1);
        mPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
        mPager.setCurrentItem(0);                                     // 设置当前显示标签页为第一页
    }

    /*
     * ViewPager 监听事件
     */
    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            if (arg0 == 0) {
             /*   view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                view2.setTextColor(context.getResources().getColor(R.color.white));
                view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
                view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));*/
                view1.setTextColor(context.getResources().getColor(R.color.white));
                view2.setTextColor(context.getResources().getColor(R.color.gray));
                view1.setTextSize(25);
                view2.setTextSize(17);
            } else if (arg0 == 1) {
          /*      view1.setTextColor(context.getResources().getColor(R.color.white));
                view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
                view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));*/
                view1.setTextColor(context.getResources().getColor(R.color.gray));
                view2.setTextColor(context.getResources().getColor(R.color.white));
                view1.setTextSize(17);
                view2.setTextSize(25);
            }
        }
    }


//    // 手机实体返回按键的处理 与 onBackPress 同理
//    long waitTime = 2000;
//    long touchTime = 0;
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
//            long currentTime = System.currentTimeMillis();
//            if ((currentTime - touchTime) >= waitTime) {
//                ToastUtils.show_always(this, "再按一次退出");
//                touchTime = currentTime;
//            } else {
////                MainActivity.onStop();
//                MobclickAgent.onKillProcess(this);
//                finish();
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


}

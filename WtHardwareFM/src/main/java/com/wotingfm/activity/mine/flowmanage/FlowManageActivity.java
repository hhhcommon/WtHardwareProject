package com.wotingfm.activity.mine.flowmanage;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;

/**
 * 流量管理
 */
public class FlowManageActivity extends AppBaseActivity {

    @Override
    protected int setViewId() {
        return R.layout.activity_flow_manage;
    }

    @Override
    protected void init() {
        setTitle("流量管理");// 设置标题
    }
}

package com.wotingfm.activity.music.playhistory.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 播放历史
 * @author woting11
 */
public class PlayHistoryActivity extends FragmentActivity {
    public static boolean isEdit;
    public static final String UPDATA_ACTION_ALL = "UPDATA_ACTION_ALL";
    public static final String UPDATA_ACTION_CHECK = "UPDATA_ACTION_CHECK";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}

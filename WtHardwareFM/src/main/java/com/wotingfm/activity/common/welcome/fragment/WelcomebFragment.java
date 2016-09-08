package com.wotingfm.activity.common.welcome.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wotingfm.R;
import com.wotingfm.util.BitmapUtils;

/**
 * 第二张引导页
 * @author 辛龙
 * 2016年4月27日
 */
public class WelcomebFragment extends Fragment  {
	private FragmentActivity context;
	private ImageView imageView1;
	private Bitmap bmp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FragmentActivity context = this.getActivity();
		View rootView = inflater.inflate(R.layout.item_welcomeb, container, false);
		ImageView imageView1 = (ImageView)rootView.findViewById(R.id.imageView1);
		bmp = BitmapUtils.readBitMap(context, R.mipmap.welcomeb);
		imageView1.setImageBitmap(bmp);
		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(bmp != null && !bmp.isRecycled()) {  
			bmp.recycle();  
		} 
	}
}

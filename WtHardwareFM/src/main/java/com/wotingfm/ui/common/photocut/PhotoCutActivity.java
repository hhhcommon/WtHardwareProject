package com.wotingfm.ui.common.photocut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.interphone.group.creategroup.CreateGroupContentFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.mine.main.MineFragment;
import com.wotingfm.widget.photocut.ClipImageLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 图片剪裁页面
 */
public class PhotoCutActivity extends Fragment {
    private Bitmap bitmap;
    private ClipImageLayout mClipImageLayout;
    private TextView textSave;
    private int type;
    private View rootView;
    private FragmentActivity context;
    private String jump_type;
    private String fragment_type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_photo_cut, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            context = getActivity();
            initView();
        }
        return rootView;
    }

    /**
     * 处理上一个页面传递过来的数据
     */
    private void handleIntent() {
        if (getArguments() == null) {
            return;
        }

        jump_type = getArguments().getString(StringConstant.JUMP_TYPE);
        fragment_type = getArguments().getString(StringConstant.FRAGMENT_TYPE);
        String imageUrl = getArguments().getString(StringConstant.START_PHOTO_ZOOM_URI);
        type = getArguments().getInt(StringConstant.START_PHOTO_ZOOM_TYPE, -1);
        if (imageUrl == null || imageUrl.equals("")) {
            return;
        }
        mClipImageLayout.setImage(Uri.parse(imageUrl));
        textSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bitmap = mClipImageLayout.clip();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                try {
                    if (type == 1) {
                        long a = System.currentTimeMillis();
                        String s = String.valueOf(a);
                        FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/woting/" + s + ".png"));
                        out.write(byteArrayOutputStream.toByteArray());
                        out.flush();
                        out.close();
                        Intent intent = new Intent();
                        intent.putExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH, Environment.getExternalStorageDirectory() + "/woting/" + s + ".png");
                        if (fragment_type != null) {
                            if (fragment_type.equals("MineFragment")) {
                                Fragment targetFragment = getTargetFragment();
                                ((MineFragment) targetFragment).setResultForPhotoZoom(1, intent);
                            }else if(fragment_type.equals("CreateGroupContentFragment")){
                                Fragment targetFragment = getTargetFragment();
                                ((CreateGroupContentFragment) targetFragment).setResultForPhotoZoom(1, intent);
                            }else if(fragment_type.equals("GroupDetailFragment")){
                                Fragment targetFragment = getTargetFragment();
                                ((GroupDetailFragment) targetFragment).setResultForPhotoZoom(1, intent);
                            }
                        }

                    } else {
                        FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/woting/portaitUser.png"));
                        out.write(byteArrayOutputStream.toByteArray());
                        out.flush();
                        out.close();
                        if (fragment_type != null) {
                            if (fragment_type.equals("MineFragment")) {
                                Fragment targetFragment = getTargetFragment();
                                ((MineFragment) targetFragment).setResultForPhotoZoom(1, null);
                            }else if(fragment_type.equals("CreateGroupContentFragment")){
                                Fragment targetFragment = getTargetFragment();
                                ((CreateGroupContentFragment) targetFragment).setResultForPhotoZoom(1, null);
                            }else if(fragment_type.equals("GroupDetailFragment")){
                                Fragment targetFragment = getTargetFragment();
                                ((GroupDetailFragment) targetFragment).setResultForPhotoZoom(1, null);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (jump_type != null) {
                    if (jump_type.equals("duijiang")) {
                        DuiJiangActivity.close();
                    } else if (jump_type.equals("mine")) {
                        MineActivity.close();
                    }
                }
            }
        });
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mClipImageLayout = (ClipImageLayout) rootView.findViewById(R.id.id_clipImageLayout);
        textSave = (TextView) rootView.findViewById(R.id.text_save);
        handleIntent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        if (mClipImageLayout != null) {
            mClipImageLayout.closeResource();
            mClipImageLayout = null;
        }
        textSave = null;
    }
}

package com.wotingfm.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wotingfm.R;

/**
 * 等待提示
 *
 * @author 辛龙
 *         2016年8月5日
 */
public class DialogUtils {
    private static Dialog loadDialog;

    /**
     * 暂时把传递的数据隐藏，只是展示转圈提示
     */
    public static Dialog Dialogph(Context context, String str) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        TextView loadText = (TextView) dialogView.findViewById(R.id.text_wenzi);
        loadText.setText(str);
        Dialog dialog = new Dialog(context, R.style.MyDialog1);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog.show();
        return dialog;
    }

    public static void showDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null);
//        TextView loadText = (TextView) dialog1.findViewById(R.id.text_wenzi);
//        loadText.setText("loading");
        loadDialog = new Dialog(context, R.style.MyDialog1);
        loadDialog.setContentView(dialogView);
        loadDialog.setCanceledOnTouchOutside(false);
        loadDialog.getWindow().setGravity(Gravity.CENTER);
        loadDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        loadDialog.show();
    }

    /**
     * 关闭加载对话框
     */
    public static void closeDialog(){
        if(loadDialog != null && loadDialog.isShowing()){
            loadDialog.dismiss();
        }
    }
}

package com.wotingfm.activity.im.interphone.notify.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wotingfm.activity.im.interphone.notify.model.DBNotifyHistory;
import com.wotingfm.common.database.SqLiteHelper;
import com.wotingfm.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对数据库中的通知列表的操作
 */
public class NotifyHistoryDao {
    private SqLiteHelper helper;
    private Context context;

    public NotifyHistoryDao(Context context) {
        helper = new SqLiteHelper(context);
        this.context = context;
    }

    /**
     * 插入搜索历史表一条数据
     */
    public void addNotifyHistory(DBNotifyHistory history) {
        //通过helper的实现对象获取可操作的数据库db
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into notifyhistory(bjuserid,type,imageurl,content,title,dealtime,addtime) values(?,?,?,?,?,?,?)",
                new Object[]{
                        history.getBJUserId(), history.getTyPe(),
                        history.getImageUrl(), history.getContent(),
                        history.getTitle(), history.getDealTime(), history.getAddTime()
                });//sql语句
        db.close();//关闭数据库对象
    }

    /**
     * 查询数据库里的数据，无参查询语句 供特定使用
     */
    public List<DBNotifyHistory> queryHistory() {
        List<DBNotifyHistory> myList = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String userId = CommonUtils.getUserId(context);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("Select * from notifyhistory  where bjuserid=? order by addtime desc", new String[]{userId});
            while (cursor.moveToNext()) {
                String bjUserId = cursor.getString(1);
                String type = cursor.getString(2);
                String imageUrl = cursor.getString(3);
                String content = cursor.getString(4);
                String title = cursor.getString(5);
                String dealTime = cursor.getString(6);
                String addTime = cursor.getString(7);
                //把每个对象都放到history对象里
                DBNotifyHistory h = new DBNotifyHistory(bjUserId, type, imageUrl, content, title, dealTime, addTime);
                myList.add(h);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return myList;
    }

    /**
     * 删除数据库表中的数据,添加时间是唯一标示（addTime）
     */
    public void deleteHistory(String addTime) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String userId = CommonUtils.getUserId(context);
        String addTimes = addTime;
        db.execSQL("Delete from notifyhistory where addtime=? and bjuserid=?", new String[]{addTimes, userId});
        db.close();
    }
}

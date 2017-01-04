package com.wotingfm.ui.interphone.notify.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wotingfm.common.database.SQLiteHelper;
import com.wotingfm.ui.interphone.linkman.model.DBNotifyHistory;
import com.wotingfm.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 对通知列表的操作
 *
 * @author 辛龙
 *         2016年1月15日
 */
public class NotifyHistoryDao {
    private SQLiteHelper helper;
    private Context context;

    //构造方法
    public NotifyHistoryDao(Context context) {
        helper = new SQLiteHelper(context);
        this.context = context;
    }

    /**
     * 插入搜索历史表一条数据
     */
    public void addNotifyHistory(DBNotifyHistory history) {
        //通过helper的实现对象获取可操作的数据库db
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into notifyhistory(bjuserid,type,imageurl,content,title,dealtime,addtime,showtype,biztype,cmdtype,command,taskid) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{
                        history.getBJUserId(), history.getTyPe(),
                        history.getImageUrl(), history.getContent(),
                        history.getTitle(), history.getDealTime(), history.getAddTime(),
                        history.getShowType(), history.getBizType(), history.getCmdType(),
                        history.getCommand(), history.getTaskId()
                });//sql语句
        db.close();//关闭数据库对象
    }


    /**
     * 查询数据库里的数据，无参查询语句 供特定使用
     */
    public List<DBNotifyHistory> queryHistory() {
        List<DBNotifyHistory> mylist = new ArrayList<DBNotifyHistory>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String userid = CommonUtils.getUserId(context);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("Select * from notifyhistory  where bjuserid=? and showtype=? order by addtime desc", new String[]{userid,"true"});
            while (cursor.moveToNext()) {
                String bjuserid = cursor.getString(1);
                String type = cursor.getString(2);
                String imageurl = cursor.getString(3);
                String content = cursor.getString(4);
                String title = cursor.getString(5);
                String dealtime = cursor.getString(6);
                String addtime = cursor.getString(7);

                String showtype = cursor.getString(8);
                int biztype = cursor.getInt(9);
                int cmdtype = cursor.getInt(10);
                int command = cursor.getInt(11);
                String taskid = cursor.getString(12);

                //把每个对象都放到history对象里
                DBNotifyHistory h = new DBNotifyHistory(bjuserid, type, imageurl, content,
                        title, dealtime, addtime, showtype,biztype,cmdtype,command,taskid);
                mylist.add(h);
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
        return mylist;
    }

    /**
     * 删除数据库表中的数据,添加时间是唯一标示（addtime）
     */
    public void deleteHistory(String addtime) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String userid = CommonUtils.getUserId(context);
        String addtimes = addtime;
        db.execSQL("Delete from notifyhistory where addtime=? and bjuserid=?",
                new String[]{addtimes, userid});
        db.close();
    }
}

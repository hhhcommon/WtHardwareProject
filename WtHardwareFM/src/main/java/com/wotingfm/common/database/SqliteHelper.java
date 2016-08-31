package com.wotingfm.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wotingfm.common.config.GlobalConfig;

/**
 * 数据库
 * Created by Administrator on 2016/8/29 0029.
 */
public class SqLiteHelper extends SQLiteOpenHelper {

    /**
     * 创建数据库表
     */
    public SqLiteHelper(Context paramContext) {
        super(paramContext, "woting.db", null, GlobalConfig.dbversoncode);
    }

    public void onCreate(SQLiteDatabase db) {
        //notifyHistory消息通知表
        db.execSQL("CREATE TABLE IF NOT EXISTS notifyhistory(_id Integer primary key autoincrement, "
                + "bjuserid varchar(50),type varchar(50),imageurl varchar(100),content varchar(100),"
                + "title varchar(50),dealtime varchar(50),addtime varchar(50))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifyhistory");
        onCreate(db);
    }
}

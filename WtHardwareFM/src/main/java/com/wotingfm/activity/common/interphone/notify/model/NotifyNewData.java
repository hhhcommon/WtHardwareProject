package com.wotingfm.activity.common.interphone.notify.model;

/**
 * 消息列表数据
 * Created by Administrator on 2016/8/26 0026.
 */
public class NotifyNewData {
    private String title;
    private String content;
    private String time;
    private String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

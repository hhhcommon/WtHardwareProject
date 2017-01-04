package com.wotingfm.ui.im.interphone.scanning.model;

import com.wotingfm.ui.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.ui.im.interphone.find.result.model.UserInviteMeInside;

import java.io.Serializable;

/**
 * 扫描结果
 *
 * @author 辛龙
 *         2016年5月5日
 */
public class MessageInfo implements Serializable {
    public String Type;
    public UserInviteMeInside UserInviteMeInside;
    public FindGroupNews FindGroupNews;

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public UserInviteMeInside getUserInviteMeInside() {
        return UserInviteMeInside;
    }

    public void setUserInviteMeInside(UserInviteMeInside userInviteMeInside) {
        UserInviteMeInside = userInviteMeInside;
    }

    public FindGroupNews getFindGroupNews() {
        return FindGroupNews;
    }

    public void setFindGroupNews(FindGroupNews findGroupNews) {
        FindGroupNews = findGroupNews;
    }
}

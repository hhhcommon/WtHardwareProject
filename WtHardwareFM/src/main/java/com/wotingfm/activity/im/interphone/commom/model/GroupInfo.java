package com.wotingfm.activity.im.interphone.commom.model;

import java.io.Serializable;

public class GroupInfo implements Serializable {
    private static final long serialVersionUID = 3031434403555346332L;
    public String GroupName;
    public String GroupDesc;
    public String GroupId;

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupDesc() {
        return GroupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        GroupDesc = groupDesc;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }
}

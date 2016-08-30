package com.wotingfm.util;


import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;

import java.util.Comparator;

public class PinyinComparator_a implements Comparator<UserInfo> {

    public int compare(UserInfo o1, UserInfo o2) {
        if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#") || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
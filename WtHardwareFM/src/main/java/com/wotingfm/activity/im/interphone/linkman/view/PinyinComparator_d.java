package com.wotingfm.activity.im.interphone.linkman.view;


import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;

import java.util.Comparator;

public class PinyinComparator_d implements Comparator<fenLeiName> {

	public int compare(fenLeiName o1, fenLeiName o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}

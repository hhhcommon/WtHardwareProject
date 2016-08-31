package com.wotingfm.util;

public class ImageUploadReturnUtil {
	/**
	 * 截取自己需要的数据
	 * @param res
	 * @return
     */
	public static String getResPonse(String res) 
	{
        if(res!=null){
        	res=res.substring(8,res.length()-2);
        }
		return res;
	}
}

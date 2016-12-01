package com.wotingfm.common.constant;

/**
 * Function:
 * DB常量类
 */
public class StringConstant {
    public static final String USERID = "userid";
    public static final String USERNAME = "username";
    public static final String ISLOGIN = "islogin";
    public static final String FIRST = "first";//引导页
    public static final String PREFERENCE = "preference";//偏好设置页
    public static final String PHONENUMBER = "phonenumber";// 用户注册手机号码
    public static final String IMAGEURL = "imageurl";//头像Image地址
    public static final String IMAGEURBIG = "imageurlbig";//头像Image地址

    public static final String USER_NUM = "usernum";// woting 号
    public static final String GENDERUSR = "GENDERUSR";// 性别
    public static final String EMAIL = "EMAIL";// 用户邮箱
    public static final String REGION = "Region";// 用户地区
    public static final String BIRTHDAY = "BIRTHDAY";// 用户生日
    public static final String USER_SIGN = "UserSign";// 用户签名
    public static final String STAR_SIGN = "StarSign";// 用户星座
    public static final String AGE = "age";// 年龄
    public static final String NICK_NAME = "nick_Name";// 昵称

    /*
     * 电台城市列表
     */
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CITYID = "cityid";//选中的城市id 对应导航返回的ADcode
    public static final String CITYNAME = "cityname";//选中的城市
    public static final String CITYTYPE = "false";//是否刷新数据，更改过城市属性
    /*
     * 保存的刷新界面信息
     */
    public static final String PERSONREFRESHB = "personrefreshb";//是否刷新聊天
    /*
     * 保存的对讲信息
     */
    public static final String INTERPHONE = "false";//是否存在对讲记录
    public static final String INTERPHONETYPE = "interphonetype";//对讲类型，group，user
    public static final String INTERPHONENUM = "interphonenum";//对讲拨打号码
    public static final String INTERPHONEIMAGE = "interphoneimage";//对讲头像
    public static final String INTERPHONENAME = "interphonename";//对讲名称
    public static final String INTERPHONEGROUPID = "interphonegroupid";//对讲组id
    /*
     * 保存2G,3G,4G等播放提醒
     */
    public static final String WIFISET = "wifiset";//默认为开启状态
    public static final String WIFISHOW = "wifishow";//是否提醒
    /*
     * 从播放历史进入播放界面的数据
     */
    public static final String PLAYHISTORYENTER = "playhistoryenter";//
    public static final String PLAYHISTORYENTERNEWS = "playhistoryenternews";//
    /*
     * 保存下载界面是否有未展示的下载完成的数据
	 */
//	public static final String REFRESHDOWNLOAD="refreshdownload";//

    // 创建群组类型传递数据时Intent的Key
    public static final String CREATE_GROUP_TYPE = "CREATE_GROUP_TYPE";

    // 跳转到图片剪裁传递的URI
    public static final String START_PHOTO_ZOOM_URI = "START_PHOTO_ZOOM_URI";

    // 跳转到图片剪裁界面的类型
    public static final String START_PHOTO_ZOOM_TYPE = "START_PHOTO_ZOOM_TYPE";

    // 标识 图片剪裁界面完成之后返回的图片路径
    public static final String PHOTO_CUT_RETURN_IMAGE_PATH = "PHOTO_CUT_RETURN_IMAGE_PATH";

    // 标识 查找类型
    public static final String FIND_TYPE = "FIND_TYPE";

    // 标识 查找类型为群组
    public static final String FIND_TYPE_GROUP = "FIND_TYPE_GROUP";

    // 标识 查找类型为个人
    public static final String FIND_TYPE_PERSON = "FIND_TYPE_PERSON";

    public static final String FIND_CONTENT_TO_RESULT = "searchstr";

    // 蓝牙的设置状态
    public static final String BLUETOOTH_SET = "BLUETOOTH_SET";

    // 保存蓝牙是否开放检测状态
    public static final String BLUETOOTH_OPEN_DETECTION_SET = "BLUETOOTH_OPEN_DETECTION_SET";

    // 标记 FM是否打开
    public static final String FM_IS_OPEN = "FM_IS_OPEN";

    // 蓝牙连接 UUID
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    // 传递数据 WiFi 名字
    public static final String WIFI_NAME = "WIFI_NAME";

    // 流量通知
    public static final String FLOW_NOTIFY = "FLOW_NOTIFY";

    // 保存是否已经选择喜欢的节目
    public static final String FAVORITE_PROGRAM_TYPE = "FavoriteProgramType";
}

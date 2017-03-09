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

    // 通知设置 声音通知开关状态
    public static final String NOTIFY_SOUND_STATE = "NOTIFY_SOUND_STATE";

    // 通知设置 群聊通知开关状态
    public static final String NOTIFY_GROUP_CHAT_STATE = "NOTIFY_GROUP_CHAT_STATE";

    // 通知设置 好友通知开关状态
    public static final String NOTIFY_FRIEND_STATE = "NOTIFY_FRIEND_STATE";

    // 通知设置 节目推送通知开关状态
    public static final String NOTIFY_PROGRAM_STATE = "NOTIFY_PROGRAM_STATE";

    // 通知设置 订阅通知开关状态
    public static final String NOTIFY_SUBSCRIBER_STATE = "NOTIFY_SUBSCRIBER_STATE";

    // 通知设置 关注通知开关状态
    public static final String NOTIFY_CONCERN_STATE = "NOTIFY_CONCERN_STATE";

    // 播放器缓存进度  数据传递
    public static final String PLAY_SECOND_PROGRESS = "PLAY_SECOND_PROGRESS";

    // 数据传递  当前播放进度
    public static final String PLAY_CURRENT_TIME = "PLAY_CURRENT_TIME";

    // 数据传递  当前播放总时间
    public static final String PLAY_TOTAL_TIME = "PLAY_TOTAL_TIME";

    // 数据传递  当前播放类型
    public static final String PLAY_MEDIA_TYPE = "PLAY_MEDIA_TYPE";

    // 数据传递  当前播放在列表中的位置
    public static final String PLAY_POSITION = "PLAY_POSITION";

    // 媒体类型 SEQU
    public static final String TYPE_SEQU = "SEQU";

    // 媒体类型 AUDIO
    public static final String TYPE_AUDIO = "AUDIO";

    // 媒体类型 RADIO
    public static final String TYPE_RADIO = "RADIO";

    // 媒体类型 TTS
    public static final String TYPE_TTS = "TTS";

    // 播放界面请求类型  主网络请求
    public static final String PLAY_REQUEST_TYPE_MAIN_PAGE = "MAIN_PAGE";

    // 播放界面请求类型  文字请求
    public static final String PLAY_REQUEST_TYPE_SEARCH_TEXT = "SEARCH_TEXT";

    // 播放界面请求类型  语音请求
    public static final String PLAY_REQUEST_TYPE_SEARCH_VOICE = "SEARCH_VOICE";

    // 数据获取  语音请求内容
    public static final String VOICE_CONTENT = "VoiceContent";

    // 数据获取  文字请求内容
    public static final String TEXT_CONTENT = "text";

    // 跳转类型
    public static final String JUMP_TYPE = "JUMP_TYPE";

    // Fragment类型
    public static final String FRAGMENT_TYPE = "FRAGMENT_TYPE";

    // 保存数据 FM 历史记录
    public static final String FM_HISTORY_RECORD = "FM_HISTORY_RECORD";
}

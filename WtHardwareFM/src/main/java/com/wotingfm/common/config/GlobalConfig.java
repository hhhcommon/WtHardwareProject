package com.wotingfm.common.config;

import android.os.Environment;

import com.wotingfm.common.devicecontrol.WtDeviceControl;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.group.creategroup.model.Freq;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.program.fenlei.model.CatalogName;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存公共属性
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class GlobalConfig {

    // 定位信息保存
    public static String longitude;
    public static String latitude;
    public static String CityName;
    public static String AdCode;
    
    // 缓存路径
    public static String playCacheDirI = Environment.getRootDirectory() + "";                  // 获取手机根目录
    public static String playCacheDirO = Environment.getExternalStorageDirectory().getAbsolutePath();// 获取SD卡根目录
    public static String ksyPlayCache = "/WTFM/playCache/";                                    // 金山云缓存地址
    public static String upLoadCache = "/WTFM/APP/";                                           // app更新下载地址
  
    public static List<CatalogName> CityCatalogList;                                           // 缓存的城市列表
    public static ArrayList<String> staticFacesList;                                           // 从Asset中取出的表情list
    public static List<GroupInfo> list_group;                                                  // 通讯录中的对讲组
    public static List<UserInfo> list_person;                                                  // 通讯录中的好友
    public static LanguageSearchInside playerObject;                                           // 播放器播放对象
    public static List<Freq> FreqList;                                                         // 对讲频率list
    
    // 网络情况 1为成功WiFi已连接，2为cmnet，3为cmwap，4为ctwap， -1为网络未连接
    public static final int NETWORK_STATE_IDLE = -1;
    public static final int NETWORK_STATE_WIFI = 1;
    public static final int NETWORK_STATE_CMNET = 2;
    public static final int NETWORK_STATE_CMWAP = 3;
    public static final int NETWORK_STATE_CTWAP = 4;
    public static int CURRENT_NETWORK_STATE_TYPE = NETWORK_STATE_IDLE;
    public static final int HTTP_CONNECTION_TIMEOUT = 10 * 1000;                                // volley请求超时 时间
    
    public static final int dbVersionCode = 7;    // 数据库版本号
    public static int activityType =1;            // 此时的界面
    public static WtDeviceControl device;         // 硬件设备控制器
    public static String voiceRecognizer;         // 此时的语音搜索界面
    public static boolean isActive;               // 是否活跃状态，有活跃状态才能播放声音，否则即使收到音频包也不播放
    public static boolean isMONI;                 // 标记实体按键是否要按照模拟对讲处理 暂定
    public static String apkUrl = "http://182.92.175.134/download/WoTing.apk";    // apk下载默认路径
    /**
     * 是否吐司
     */
    public static boolean isToast;
    /**
     * 是不是读取配置文件
     */
    public static boolean isCollocation = true;
    /**
     * PersonClientDevice(个人客户端设备) 终端类型1=app,2=设备，3=pc
     */
    public static int PCDType;
    /**
     * socket请求端口
     */
    public static int socketPort;
    /**
     * socket请求ip
     */
    public static String socketUrl;//测试服务器地址
    /**
     * http请求总url
     */
    public static String baseUrl;//测试服务器地址
    // image请求路径前缀
    public static  String imageurl ;// 服务器

    /**
     * 公共部分
     */
    // 启动页登录 应用入口
    public static final String splashUrl ="wt/common/entryApp.do?";
    // 获得版本信息
    // public static final String VersionUrl ="wt/common/getVersion.do?";
    // 获得版本信息
    public static final String VersionUrl ="wt/common/judgeVersion.do?";
    // 注册
    public static final String registerUrl ="wt/passport/user/register.do?";
    // 登录
    public static final String loginUrl ="wt/passport/user/mlogin.do?";
    // 注销
    public static final String logoutUrl ="wt/passport/user/mlogout.do?";
    // 上传头像
    // public static final String uploadtxUrl ="wt/common/uploadImg.do?";
    // 修改密码
    public static final String modifyPasswordUrl ="wt/passport/user/updatePwd.do?";
    // 找回密码
    // public static final String retrievePasswordUrl ="wt//passport/user/retrievePwd.do?";
    // 账号修改
    public static final String updateUserUrl ="wt/passport/user/updateUserInfo.do?";
    // 账号绑定
    public static final String bindExtUserUrl ="wt/passport/user/bindExtUserInfo.do?";
    // 帮助
    public static final String wthelpUrl ="wt/wtHelp.html";
    // 意见反馈
    public static final String FeedBackUrl ="wt/opinion/app/commit.do";
    // 意见反馈历史记录
    public static final String FeedBackListUrl ="wt/opinion/app/getList.do";
    // 第一次请求
    public static final String mainPageUrl ="wt/mainPage.do?";
    // 语音搜索
    public static final String searchvoiceUrl ="wt/searchByVoice.do?";
    // 电台首页展示展示
    // public static final String BroadcastMainPage="wt/broadcast/mainPage.do?";
    // 获取list
    public static final String getListByCatalog ="wt/content/getListByCatalog.do?";
    // 电台分类展示B
    public static final String getListByZoneUrl ="wt/broadcast/getListByZone.do?";
    // 城市列表
    public static final String getZoneListUrl ="wt/common/getZoneList.do?";
    // 热门搜索
    public static final String getHotSearch ="wt/getHotKeys.do";
    // 依照文字搜索
    // public static final String getSearchByText ="wt/searchByText.do";
    // 按照声音搜索
    // public static final String getSearchByVoice ="wt/searchByVoice.do";
    // 某子分类首页内容
    // public static final String getCatalogMainPageUrl ="wt/mainPage.do?";
    // 分类首页
    public static final String BroadcastMainPage ="wt/content/mainPage.do";
    // 图片banner
    public static final String getadvertUrl ="wt/content/getLoopImgs.do";
    // 获取系列节目
    public static final String getSequUrl ="wt/content/getSeqMaInfo.do";
    // 获取历史记录 此处不对 需要后台工作完成后修改
    // public static final String playHistoryUrl ="wt/passport/uploadImg.do?";
    // 对讲-创建对讲小组 统一建组参数 2016.1.21更新 未改名修改了原有接口
    public static final String talkgroupcreatUrl ="wt/passport/group/buildGroup.do";
    // 获取联系人 此接口之前存在 被注释掉了 现在在添加好友进组时使用
    public static final String getfriendlist ="wt/passport/friend/getList.do";
    // 获取创建对讲小组的联系人
    public static final String creattalkgroupUrl ="wt/passport/friend/getList.do";
    // 对讲-创建对讲小组
    // public static final String talkgroupcreatUrl ="wt/passport/group/num/createGroup.do?";
    // 对讲-小组列表
    public static final String talkgrouplistUrl ="wt/passport/group/getGroupList.do?";
    // 对讲-小组联系人
    public static final String grouptalkUrl ="wt/passport/group/getGroupMembers.do?";
    // 退出组
    public static final String ExitGroupurl ="wt/passport/group/exitGroup.do?";
    // 通过口令加入用户组
    public static final String JoinGroupByNumUrl ="wt/passport/group/num/joininGroup.do";
    // 获取联系人
    //public static final String gettalkpersonsurl="wt/passport/friend/getList.do";
    public static final String gettalkpersonsurl ="wt/passport/getGroupsAndFriends.do";
    // 邀请我列表
    public static final String getInvitedMeListUrl ="wt/passport/friend/getInvitedMeList.do?";
    // 拒绝或接受邀请
    public static final String InvitedDealUrl ="wt/passport/friend/inviteDeal.do?";
    // 查询陌生人
    public static final String searchStrangerUrl ="wt/passport/friend/searchStranger.do?";
    // 查找用户组
    public static final String searchStrangerGroupUrl ="wt/passport/group/searchGroup.do?";
    // 邀请陌生人为好友
    public static final String sendInviteUrl ="wt/passport/friend/invite.do";
    public static final String talkoldlistUrl ="wt/passport/getHistoryUG.do?";
    // 邀请某人进入组
    public static final String sendInviteintoGroupUrl ="wt/passport/group/groupInvite.do?";
    // 加入群（公开群 密码群）
    public static final String JoinGroupUrl ="wt/passport/group/joinInGroup.do?";
    // 加入群(验证群)
    public static final String JoinGroupVertifyUrl ="wt/passport/group/groupApply.do";
    // 获取邀请我的组的信息
    public static final String getInvitedMeGroupListUrl ="wt/passport/group/getInviteMeList.do";
    // 处理组邀请请求
    public static final String InvitedGroupDealUrl ="wt/passport/group/inviteDeal.do?";
    // 删除好友
    public static final String delFriendUrl ="wt/passport/friend/delFriend.do?";
    // 修改好友别名
    public static final String updateFriendnewsUrl ="wt/passport/friend/updateFriendInfo.do?";
    // 修改群成员别名
    public static final String updategroupFriendnewsUrl ="wt/passport/group/updateGroupUser.do?";
    // 管理员踢出用户 当踢出用户只剩用户本人时 此群将解散
    public static final String KickOutMembersUrl ="wt/passport/group/kickoutGroup.do";
    // 管理员权限移交
    public static final String changGroupAdminnerUrl ="wt/passport/group/changGroupAdminner.do";
    // 修改群密码 没接口
    public static final String UpdateGroupPassWordUrl ="wt/passport/group/updatePwd.do";
    // 加群消息
    public static final String GetApplyListUrl ="wt/passport/group/getExistApplyUserGroupList.do";
    // 同意或者拒绝组申请
    public static final String DealGroupApplyUrl ="wt/passport/group/applyDeal.do";
    // 审核消息
    public static final String JoinGroupListUrl ="wt/passport/group/getApplyUserList.do";
    // 更改群信息
    public static final String UpdateGroupInfoUrl ="wt/passport/group/updateGroup.do";
    // 接受申请或者拒绝申请
    public static final String applyDealUrl ="wt/passport/group/applyDeal.do";
    // 获取审核列表
    public static final String checkVertifyUrl ="wt/passport/group/getNeedCheckInviteUserGroupList.do";
    // 审核邀请
    public static final String checkDealUrl ="wt/passport/group/checkDeal.do";
    // 依照文字搜索
    public static final String getSearchByText ="wt/searchByText.do";
    // 获取分类
    public static final String getCatalogUrl ="wt/getCatalogInfo.do";
    // 内容主页获取统一接口
    public static final String getContentUrl ="wt/content/getContents.do";
    // 根据contentID获取内容列表
    public static final String getContentById ="wt/content/getContentInfo.do";
    // 得到专辑下节目列表
    public static final String getSmSubMedias = "wt/content/getSmSubMedias.do";
    // 搜索检索热词
    public static final String searchHotKeysUrl ="wt/searchHotKeys.do";
    // 通过手机号码注册
    public static final String registerByPhoneNumUrl ="wt/passport/user/registerByPhoneNum.do";
    // 再发验证码
    public static final String reSendPhoneCheckCodeNumUrl ="wt/passport/user/reSendPhoneCheckCode.do";
    // 验证验证码，并得到手机号所绑定的用户
    public static final String checkPhoneCheckCodeUrl ="wt/passport/user/checkPhoneCheckCode.do";
    // 通过手机号码找回用户
    public static final String retrieveByPhoneNumUrl ="wt/passport/user/retrieveByPhoneNum.do";
    // 根据手机号返回值修改密码
    public static final String updatePwd_AfterCheckPhoneOKUrl ="wt/passport/user/updatePwd_AfterCheckPhoneOK.do";
    // 第三方认证
    public static final String afterThirdAuthUrl ="wt/passport/user/afterThirdAuth.do";
    // 喜欢content/clickFavorite.do
    public static final String clickFavoriteUrl ="wt/content/clickFavorite.do";
    //发送订阅信息（订阅/取消订阅）
    public static final String clickSubscribe = "wt/content/clickSubscribe.do";
    //获取订阅信息
    public static final String getSubscribeList = "wt/content/getSubscribeList.do";
    // 获取路况数据
    public static final String getLKTTS ="wt/lkTTS.do";
    // 获取favorite列表
    public static final String getFavoriteListUrl ="wt/content/getFavoriteList.do";
    // 删除喜欢列表
    public static final String delFavoriteListUrl ="wt/content/delFavorites.do";
    // 获取偏好列表
    public static final String getPreferenceUrl="wt/getPreferenceCatalog.do";
    // 设置偏好列表
    public static final String setPreferenceUrl="wt/setPreference.do";
    // 获取当前内容的评论列表
    public static final String getMyCommentListUrl ="wt/discuss/article/getList.do";
    // 发表评论
    public static final String pushCommentUrl="wt/discuss/add.do";
    // 删除评论
    public static final String delCommentUrl="wt/discuss/del.do";
    //内容主页获取节目单接口
    public static final String getProgrammeUrl ="wt/content/getBCProgramme.do";
    // 获取主播信息
    public static final String getPersonInfo ="wt/person/getPersonInfo.do";
    // 新增主播内容
    public static final String getPersonContents ="wt/person/getPersonContents.do";
}

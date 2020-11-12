package com.wificamera.sniffer.common.constant;

import com.wificamera.sniffer.R;
import com.wificamera.sniffer.MainApplication;

/**
 * Created  on 2019/3/21.
 */
public interface Constants {

    String SERVICE_IP = "http://47.114.75.148:8090/api";
    String APP_TOKEN = "appToken";

    // admin测试数据使用
    boolean SKIP_TO_TEST_ACTIVITY = false; // 是否启用TestActivity
    boolean superAdminTest = MainApplication.getContext().getResources().getBoolean(R.bool.superAdminTest);

    // OkGo 连接超时时间,毫秒ms
    long CONNECT_TIME_OUT = 6000;

    // 申请权限跳转到设置界面code
    int APP_SETTING_DIALOG_REQUEST_CODE = 1;

    // 申请权限code
    int ALL_PERMISSION = 100;

    // 更新apk
    int UPDATE_APP = 102;
    int INSTALL_PERMISSION_CODE = 103;

}

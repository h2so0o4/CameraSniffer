package com.wificamera.sniffer.modules.welcome.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.wificamera.sniffer.R;
import com.wificamera.sniffer.manager.ActivityLifecycleManager;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.constant.SPConstants;
import com.wificamera.sniffer.common.http.callback.json.JsonCallback;
import com.wificamera.sniffer.common.model.BaseCodeJson;
import com.wificamera.sniffer.common.utils.PreferenceUtils;
import com.wificamera.sniffer.modules.home.activity.BottomNavigation2Activity;
import com.wificamera.sniffer.test.TestActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class WelcomeActivity extends Activity {

    private Handler handler = new Handler();
    /*
    * 启动模式：
    * 1：启动界面时间与App加载时间相等
    * 2：设置启动界面2秒后跳转
    * */
    private final static int SELECT_MODE = 1;
    private OkHttpClient.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //setContentView(R.layout.activity_login);
        initWelcome();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断token
                //doPost();
                skipToMainActivity();
            }
        }, 3000);
    }

    private void initWelcome() {
        switch (WelcomeActivity.SELECT_MODE) {
            case 1:
                fastWelcome();
                break;
            case 2:
                slowWelcome();
                break;
        }
    }


    /*方法1：启动界面时间与App加载时间相等*/
    private void fastWelcome() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //耗时任务，比如加载网络数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断token
                        doPost();

                    }
                });
            }
        }).start();
    }

    /*方法2：设置启动界面2秒后跳转*/
    private void slowWelcome() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断token
                //doPost();
                skipToMainActivity();
            }
        }, 6000);
    }


    /**
     * 请求服务器判断token是否过期
     */
    private void doPost() {

        String token = PreferenceUtils.getString(SPConstants.TOKEN, "");
        String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");

        String url = Constants.SERVICE_IP + "/isLogin.do?token=" + token + "&uid=" + uid;

        // 修改请求超时时间为6s，与全局超时时间分开
        builder = new OkHttpClient.Builder();
        /*builder.readTimeout(2000, TimeUnit.MILLISECONDS);*/
        builder.writeTimeout(2000, TimeUnit.MILLISECONDS);
        builder.connectTimeout(2000, TimeUnit.MILLISECONDS);

        skipToLoginActivity();

        /*OkGo.<BaseCodeJson<Void>>get(url)
                .tag(this)
                .client(builder.build())
                .params("token", token)
                .execute(new JsonCallback<BaseCodeJson<Void>>() {
                    @Override
                    public void onSuccess(Response<BaseCodeJson<Void>> response) {

                        // 判断是否开启跳转到测试界面
                        if (Constants.SKIP_TO_TEST_ACTIVITY) {
                            skipToTestActivity();
                        } else {
                            skipToMainActivity();
                        }

                    }

                    @Override
                    public void onError(Response<BaseCodeJson<Void>> response) {
                        super.onError(response);
                        skipToLoginActivity();
                    }
                });*/

    }


    /**
     * 跳转到测试页面
     * */
    private void skipToTestActivity() {
        // token未过期，跳转到主界面
        Intent intent = new Intent(WelcomeActivity.this, TestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }


    /**
     * 跳转到主界面
     * */
    private void skipToMainActivity() {
        // token未过期，跳转到主界面
        Intent intent = new Intent(WelcomeActivity.this, BottomNavigation2Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }

    /**
     * 跳转到登录页面
     * */
    private void skipToLoginActivity() {
        // 跳转到登录页面
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        OkGo.cancelAll(builder.build());

        if (handler != null) {
            //If token is null, all callbacks and messages will be removed.
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

}

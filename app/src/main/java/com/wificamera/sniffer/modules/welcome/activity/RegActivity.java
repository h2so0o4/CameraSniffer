package com.wificamera.sniffer.modules.welcome.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseActivity;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.model.Login;
import com.wificamera.sniffer.common.model.LoginJson;
import com.wificamera.sniffer.common.utils.KeyboardToolUtils;
import com.wificamera.sniffer.common.utils.ToastUtils;
import com.wificamera.sniffer.manager.ActivityLifecycleManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.wificamera.sniffer.modules.welcome.activity.LoginActivity.JsonToJavaObj;

public class RegActivity extends BaseActivity {

    private EditText phone;
    private EditText et_password;
    private EditText et_repassword;
    private TextView cb_toLogin;
    private Button btn_reg;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        //适用于网络请求数据量很小 子线程进行http请求
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        QMUIStatusBarHelper.setStatusBarLightMode(this);
        cb_toLogin = findViewById(R.id.cb_toLogin);
        cb_toLogin.setClickable(true);
        cb_toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToLoginActivity();
            }
        });

        btn_reg = findViewById(R.id.btn_reg);
        btn_reg.setClickable(true);
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    reg();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //验证密码是否合法
    private boolean isCorrectPWD() {
        et_password = findViewById(R.id.et_password);
        et_repassword = findViewById(R.id.et_repassword);

        final String password1 = et_password.getText().toString();
        final String password2 = et_repassword.getText().toString();

        if (password1.length() < 6) {
            Toast.makeText(RegActivity.this, "请输入长度大于6位的密码！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isChinese(password1)) {
            Toast.makeText(RegActivity.this, "请输入字母或数字组成的密码！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password1.equals(password2)) {
            Toast.makeText(RegActivity.this, "两次输入密码不一致！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password1.contains(" ")) {
            Toast.makeText(RegActivity.this, "请勿输入空白数据！", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void reg() throws JSONException, IOException {
        //表单验证通过
        if (isCorrectPWD()) {
            phone = findViewById(R.id.et_account);
            et_password = findViewById(R.id.et_password);

            final String userAccount = phone.getText().toString();
            final String password = et_password.getText().toString();

            String url = Constants.SERVICE_IP + "/register.do?password=" + password + "&phone=" + userAccount;

            //客户端
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(2000, TimeUnit.MILLISECONDS)
                    .writeTimeout(2000, TimeUnit.MILLISECONDS)
                    .build();

            Login login = new Login(userAccount, password);
            Gson gson = new Gson();
            String jsonStr = gson.toJson(login);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, jsonStr);

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(url)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                //Log.e(LoginActivity.class.getSimpleName(),response.body().string());
                JSONObject jsonObject = new JSONObject(response.body().string());

                LoginJson loginJson = JsonToJavaObj(jsonObject.toString());

                String msg = loginJson.getMsg();
                Toast.makeText(RegActivity.this, "" + msg, Toast.LENGTH_SHORT).show();

                if (loginJson.isSuccess()) {
                    skipToLoginActivity();
                }
            }
        }
    }
    /**
     * 判断该字符串是否为中文
     * @param string
     * @return
     */
    public static boolean isChinese (String string){
        int n = 0;
        for (int i = 0; i < string.length(); i++) {
            n = (int) string.charAt(i);
            if (!(19968 <= n && n < 40869)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 跳转到登录
     * */
    private void skipToLoginActivity () {
        // 隐藏软键盘
        KeyboardToolUtils.hideSoftInput(RegActivity.this);
        // 退出界面之前把状态栏还原为白色字体与图标
        QMUIStatusBarHelper.setStatusBarDarkMode(RegActivity.this);
        Intent intent = new Intent(RegActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }


    /**
     * 重写返回键，实现双击退出程序效果
     */
    @Override
    public boolean onKeyDown ( int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                ToastUtils.normal("再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                OkGo.getInstance().cancelAll();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                ActivityLifecycleManager.get().appExit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

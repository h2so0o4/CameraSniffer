package com.wificamera.sniffer.modules.welcome.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;

import android.os.StrictMode;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseActivity;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.constant.SPConstants;
import com.wificamera.sniffer.common.http.callback.json.JsonDialogCallback;
import com.wificamera.sniffer.common.model.BaseCodeJson;
import com.wificamera.sniffer.common.model.Login;
import com.wificamera.sniffer.common.model.LoginJson;
import com.wificamera.sniffer.common.utils.AlertDialogUtils;
import com.wificamera.sniffer.common.utils.AnimationToolUtils;
import com.wificamera.sniffer.common.utils.AppUtils;
import com.wificamera.sniffer.common.utils.Des3Utils;
import com.wificamera.sniffer.common.utils.KeyboardToolUtils;
import com.wificamera.sniffer.common.utils.PreferenceUtils;
import com.wificamera.sniffer.common.utils.ToastUtils;
import com.wificamera.sniffer.manager.ActivityLifecycleManager;
import com.wificamera.sniffer.modules.home.activity.BottomNavigation2Activity;
import com.wificamera.sniffer.modules.welcome.model.AppLoginPo;
import com.wificamera.sniffer.test.TestActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements Validator.ValidationListener {

    @BindView(R.id.logo)
    ImageView mLogo;

    @Order(1)
    @NotEmpty(message = "手机号不能为空")
    @BindView(R.id.et_account)
    EditText mEtAccount;

    @Order(2)
    @NotEmpty(message = "密码不能为空")
    @Password(min = 1, scheme = Password.Scheme.ANY,message = "密码不能少于1位")
    @BindView(R.id.et_password)
    EditText mEtPassword;

    @BindView(R.id.iv_clean_account)
    ImageView mIvCleanAccount;
    @BindView(R.id.clean_password)
    ImageView mCleanPassword;
    @BindView(R.id.iv_show_pwd)
    ImageView mIvShowPwd;

    @BindView(R.id.cb_checkbox)
    CheckBox cbCheckbox;
    @BindView(R.id.content)
    LinearLayout mContent;
    @BindView(R.id.scrollView)
    NestedScrollView mScrollView;

    private long exitTime = 0;
    private int screenHeight = 0;//屏幕高度
    private int keyHeight = 0; //软件盘弹起后所占高度
    private final float scale = 0.9f; //logo缩放比例
    private int height = 0;
    boolean isCheckBox;

    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //适用于网络请求数据量很小 子线程进行http请求
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        QMUIStatusBarHelper.setStatusBarLightMode(this);

        validator = new Validator(this);
        validator.setValidationListener(this);


        //判断是否登陆
        /*try {
            if(isLogin())
                skipToMainActivity();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        skipToMainActivity();
        initUser();
        initEvent();
    }

    @OnClick({R.id.iv_clean_account, R.id.clean_password, R.id.iv_show_pwd,R.id.forget_password, R.id.btn_login,R.id.cb_toReg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_clean_account:
                mEtAccount.getText().clear();
                mEtPassword.getText().clear();
                break;
            case R.id.clean_password:
                mEtPassword.getText().clear();
                break;
            case R.id.iv_show_pwd:
                changePasswordEye();
                break;
            case R.id.forget_password:
                forgotPassword();
                break;
            case R.id.cb_toReg:
                skipToRegActivity();
                break;
            case R.id.btn_login:
                // 登录，启动表单验证，重写方法：onValidationSucceeded
                validator.validate();
                break;
        }
    }

    /**
     * 初始化登录历史记录
     * */
    private void initUser() {
        //从sharePreference拿出保存的账号密码
        String username = PreferenceUtils.getString(SPConstants.USER_NAME, "");
        String password = PreferenceUtils.getString(SPConstants.PASSWORD, "");
        mEtAccount.getText().clear();
        mEtPassword.getText().clear();

        if (!TextUtils.isEmpty(username)) {
            mEtAccount.setText(username);
            mEtAccount.setSelection(mEtAccount.getText().length());
            mIvCleanAccount.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(password)) {
            //password = Des3Utils.decode(password);
            mEtPassword.setText(password);
            mEtPassword.setSelection(mEtPassword.getText().length());
            mCleanPassword.setVisibility(View.VISIBLE);
            cbCheckbox.setChecked(true);
            isCheckBox = true;
        } else {
            cbCheckbox.setChecked(false);
            isCheckBox = false;
        }
    }

    /**
     * 忘记密码
     * */
    private void forgotPassword() {
        ToastUtils.info("请与管理员联系修改密码！邮箱1907934598@qq.com");
    }

    // *************************以下为登录验证及跳转界面相关*************************//

    /**
     * 登录表单验证成功后，请求后台验证账号密码
     * */
    private void attemptLogin() throws IOException, JSONException {

        final String userAccount = mEtAccount.getText().toString();
        final String password = mEtPassword.getText().toString();

        String url = Constants.SERVICE_IP + "/login.do?password=" + password +"&phone=" + userAccount;

        //客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(2000, TimeUnit.MILLISECONDS)
                .writeTimeout(2000, TimeUnit.MILLISECONDS)
                .build();

        Login login = new Login(userAccount,password);
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

            String uid = loginJson.getData().getUid();
            String token = loginJson.getData().getJwtToken();

            if(loginJson.isSuccess()){
                saveUserDataToSharePreference(userAccount,password,uid,token);
                skipToMainActivity();
            }else{
                String msg = loginJson.getMsg();
                Toast.makeText(LoginActivity.this,""+msg,Toast.LENGTH_SHORT).show();
            }



        }



        //密码加密
        //String newPassword = AppUtils.encryptSha256(password);

       /* OkGo.<BaseCodeJson<AppLoginPo>>post(url)
                .tag(this)
                .params("phone",userAccount)
                .params("password",password)
                .execute(new JsonDialogCallback<BaseCodeJson<AppLoginPo>>(this) {
                    @Override
                    public void onSuccess(Response<BaseCodeJson<AppLoginPo>> response) {
                        BaseCodeJson<AppLoginPo> loginJson = response.body();
                        if (loginJson != null) {
                            whichDepartment(userAccount,password,loginJson.getResult());
                        }
                    }
                });*/
    }


    //登录json数据解析
    public static LoginJson JsonToJavaObj(String json) throws JSONException {
        if(json==null)
            return null;

        JSONObject jsonObject = null;
        //封装java对象
        LoginJson loginJson = new LoginJson();

        jsonObject = new JSONObject(json);

        //第一层解析
        Integer code = jsonObject.optInt("code");
        boolean success = jsonObject.optBoolean("success");
        String msg = jsonObject.optString("msg");
        JSONObject data = jsonObject.optJSONObject("data");

        //第一层封装
        loginJson.setCode(code);
        loginJson.setSuccess(success);
        loginJson.setMsg(msg);

        LoginJson.DataBean dataBean = new LoginJson.DataBean();
        loginJson.setData(dataBean);

        if(code==0 && loginJson.getMsg()==""){
            //第二层解析
            String jwtToken = data.optString("jwtToken");
            String uid = data.optString("uid");

            //第二层封装
            dataBean.setJwtToken(jwtToken);
            dataBean.setUid(uid);
        }
        return loginJson;

    }

   /* *//**
     * 判断该账号是否有所属部门
     * *//*
    private void whichDepartment(String userAccount, String password, AppLoginPo appLoginPo) {
        String firstDepId = appLoginPo.getFirstDepId();

        if (firstDepId == null) {

            // 测试数据
            if (Constants.superAdminTest && 1 == appLoginPo.getId()) {
                // 测试数据
                superAdminTest(appLoginPo);
                // 存储用户必要的数据
                saveUserDataToSharePreference(userAccount, password,appLoginPo);
                //判断跳转到测试界面
                if (Constants.SKIP_TO_TEST_ACTIVITY) {
                    skipToTestActivity();
                    return;
                }
                //跳转到主页
                skipToMainActivity();
                return;
            }

            AlertDialogUtils.showDialog(
                    this,
                    "登录提示：",
                    "该账户未确认组织类型，无法登录，请与管理员联系！",
                    "重新登录");
        } else {

            // 测试数据
            if (Constants.superAdminTest) {
                superAdminTest(appLoginPo);
            }

            // 存储用户必要的数据
            saveUserDataToSharePreference(userAccount, password,appLoginPo);
            //判断跳转到测试界面
            if (Constants.SKIP_TO_TEST_ACTIVITY) {
                skipToTestActivity();
                return;
            }

            //跳转到主页
            skipToMainActivity();
        }

    }*/

    /**
     * 根据返回的结果，存储用户必要的数据到SharePreference
     * */
    private void saveUserDataToSharePreference(String userName, String password,String uid,String token) {
        //保存用户账号密码到sharePreference
        PreferenceUtils.setString(SPConstants.USER_NAME, userName);
        PreferenceUtils.remove(SPConstants.PASSWORD);
        // 如果用户点击了“记住密码”，保存密码
        if (isCheckBox) {
            PreferenceUtils.setString(SPConstants.PASSWORD, password);
        }

        // 保存用户ID
        PreferenceUtils.setString(SPConstants.USER_ID, uid);

        //保存token到sharePreference
        PreferenceUtils.setString(SPConstants.TOKEN, token);
        //获取token
        HttpParams params = new HttpParams();
        params.put(Constants.APP_TOKEN, PreferenceUtils.getString(SPConstants.TOKEN, ""));
        OkGo.getInstance().init(getApplication())
                .addCommonParams(params);
        //启动本地数据库
        LitePal.getDatabase();
    }

    /**
     * 跳转到测试界面
     * */
    private void skipToTestActivity() {
        // 隐藏软键盘
        KeyboardToolUtils.hideSoftInput(LoginActivity.this);
        // 退出界面之前把状态栏还原为白色字体与图标
        QMUIStatusBarHelper.setStatusBarDarkMode(LoginActivity.this);
        Intent intent = new Intent(LoginActivity.this, TestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }

    /**
     * 跳转到主界面
     * */
    private void skipToMainActivity() {
        // 隐藏软键盘
        KeyboardToolUtils.hideSoftInput(LoginActivity.this);
        // 退出界面之前把状态栏还原为白色字体与图标
        QMUIStatusBarHelper.setStatusBarDarkMode(LoginActivity.this);
        Intent intent = new Intent(LoginActivity.this, BottomNavigation2Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }

    /**
     * 跳转到注册界面
     * */
    private void skipToRegActivity() {
        // 隐藏软键盘
        KeyboardToolUtils.hideSoftInput(LoginActivity.this);
        // 退出界面之前把状态栏还原为白色字体与图标
        QMUIStatusBarHelper.setStatusBarDarkMode(LoginActivity.this);
        Intent intent = new Intent(LoginActivity.this, RegActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }

    private boolean isLogin() throws IOException, JSONException {

        String token = PreferenceUtils.getString(SPConstants.TOKEN, "");
        String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");

        String url = Constants.SERVICE_IP + "/isLogin.do?token=" + token + "&uid=" + uid;

        //客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(2000, TimeUnit.MILLISECONDS)
                .writeTimeout(2000, TimeUnit.MILLISECONDS)
                .build();




        //提交的内容
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            //使用JSONObject 处理结果
            JSONObject jsonObject = new JSONObject(response.body().string());
            LoginJson loginJson = JsonToJavaObj(jsonObject.toString());
            return loginJson.isSuccess();
        }
        return false;
    }

    // *************************以上为登录验证及跳转界面相关*************************//

    /**
     * 测试数据
     * */
    private void superAdminTest(AppLoginPo appLoginPo) {
        int id = appLoginPo.getId();
        if (id == 1) {
            // TODO: do something
        }
    }


    //---------------------------------分割线-------------------------------------//

    /**
     * 点击眼睛图片显示或隐藏密码
     * */
    private void changePasswordEye() {
        if (mEtPassword.getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mIvShowPwd.setImageResource(R.drawable.icon_pass_visuable);
        } else {
            mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mIvShowPwd.setImageResource(R.drawable.icon_pass_gone);
        }

        // 将光标移至文字末尾
        String pwd = mEtPassword.getText().toString();
        if (!TextUtils.isEmpty(pwd))
            mEtPassword.setSelection(pwd.length());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {

        // 获取屏幕高度
        screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        // 弹起高度为屏幕高度的1/3
        keyHeight = screenHeight / 3;

        // 输入账号状态监听，在右边显示或隐藏clean
        addIconClearListener(mEtAccount,mIvCleanAccount);
        // 监听EtPassword输入状态，在右边显示或隐藏clean
        addIconClearListener(mEtPassword,mCleanPassword);

        /*
        * 记住密码Checkbox点击监听器
        * */
        cbCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCheckBox = isChecked;
            }
        });

        /*
         * 禁止键盘弹起的时候可以滚动
         */
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        // ScrollView监听滑动状态
        mScrollView.addOnLayoutChangeListener(new ViewGroup.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
              /* old是改变前的左上右下坐标点值，没有old的是改变后的左上右下坐标点值
              现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起*/
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
                    int dist = mContent.getBottom() - mScrollView.getHeight();
                    if (dist > 0) {
                        ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(mContent, "translationY", 0.0f, -dist);
                        mAnimatorTranslateY.setDuration(300);
                        mAnimatorTranslateY.setInterpolator(new LinearInterpolator());
                        mAnimatorTranslateY.start();
                        AnimationToolUtils.zoomIn(mLogo, scale, dist);
                    }

                } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
                    if ((mContent.getBottom() - oldBottom) > 0) {
                        ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(mContent, "translationY", mContent.getTranslationY(), 0);
                        mAnimatorTranslateY.setDuration(300);
                        mAnimatorTranslateY.setInterpolator(new LinearInterpolator());
                        mAnimatorTranslateY.start();
                        //键盘收回后，logo恢复原来大小，位置同样回到初始位置
                        AnimationToolUtils.zoomOut(mLogo, scale);
                    }
                }
            }
        });
    }

    /**
     * 设置文本框与右侧删除图标监听器
     */
    private void addIconClearListener(final EditText et, final ImageView iv) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //如果文本框长度大于0，则显示删除图标，否则不显示
                if (s.length() > 0) {
                    iv.setVisibility(View.VISIBLE);
                } else {
                    iv.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        // 注解验证全部通过验证，开始后台验证
        try {
            attemptLogin();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // 显示上面注解中添加的错误提示信息
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                ToastUtils.error(message);
            }
        }
    }

    /**
     * 重写返回键，实现双击退出程序效果
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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

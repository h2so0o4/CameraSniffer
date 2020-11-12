package com.wificamera.sniffer.modules.mine.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen.library.SuperTextView;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseFragment;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.constant.SPConstants;
import com.wificamera.sniffer.common.model.LoginJson;
import com.wificamera.sniffer.common.utils.AlertDialogUtils;
import com.wificamera.sniffer.common.utils.KeyboardToolUtils;
import com.wificamera.sniffer.common.utils.PreferenceUtils;
import com.wificamera.sniffer.common.utils.ToastUtils;
import com.wificamera.sniffer.common.utils.UpdateAppUtils;
import com.orhanobut.logger.Logger;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.wificamera.sniffer.manager.ActivityLifecycleManager;
import com.wificamera.sniffer.modules.about.AboutActivity;
import com.wificamera.sniffer.modules.welcome.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.wificamera.sniffer.modules.welcome.activity.LoginActivity.JsonToJavaObj;

public class MyFragment extends BaseFragment {


    @BindView(R.id.h_background)
    ImageView hBackground;
    @BindView(R.id.h_head)
    ImageView hHead;
    @BindView(R.id.h_user_name)
    TextView hUserName;
    @BindView(R.id.stv_change_pwd)
    SuperTextView stvChangePwd;
    @BindView(R.id.stv_update)
    SuperTextView stvUpdate;
    @BindView(R.id.stv_logout)
    SuperTextView stvLogout;

    /*
    private static final String BUNDLE_TITLE = "key_title";
    public static MyFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE,title);
        MyFragment myFragment = new MyFragment();
        myFragment.setArguments(bundle);
        return myFragment;
    }
    */

    private Context context;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_my, null);
        ButterKnife.bind(this, root);



        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("我的fragment创建");
        context = getContext();

        SuperTextView stv_user_name = getActivity().findViewById(R.id.stv_user_name);
        String phone = PreferenceUtils.getString(SPConstants.USER_NAME, "");
        stv_user_name.setRightBottomString(phone);

        initView();
    }

    @OnClick({R.id.stv_change_pwd,R.id.stv_logout})
    public void onViewClicked(View view)  {
        switch (view.getId()) {
            case R.id.stv_logout:
                try{
                    logout();
                }
                catch(Exception e){
                }

                break;
            case R.id.stv_change_pwd:
                ToastUtils.info("请与管理员联系修改密码！邮箱1907934598@qq.com");
                break;
        }
    }

    //登出
    private void logout() throws JSONException, IOException {


        String token = PreferenceUtils.getString(SPConstants.TOKEN, "");
        String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");

        String url = Constants.SERVICE_IP + "/logout.do?token=" + token + "&uid=" + uid;

        //客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
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
            if(loginJson.isSuccess())
                skipToLoginActivity();
        }
    }

    private void skipToLoginActivity() {
        // 隐藏软键盘
        KeyboardToolUtils.hideSoftInput(getActivity());
        // 退出界面之前把状态栏还原为白色字体与图标
        QMUIStatusBarHelper.setStatusBarDarkMode(getActivity());
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 结束所有Activity
        ActivityLifecycleManager.get().finishAllActivity();
    }



    private void initView() {
        /*
         * 版本更新，点击事件
         * */
        stvUpdate.setOnSuperTextViewClickListener(new SuperTextView.OnSuperTextViewClickListener() {
            @Override
            public void onClickListener(SuperTextView superTextView) {
                //检查权限，并启动版本更新
                /*checkPermission();*/
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(intent);
                

            }
        });
    }



    /***********************************检查App更新********************************************/
    @AfterPermissionGranted(Constants.UPDATE_APP)
    @Override
    public void checkPermission() {
        // 检查文件读写权限
        String[] params = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(context,params)) {

            //Android 8.0后，安装应用需要检查打开未知来源应用权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkInstallPermission();
            } else {
                UpdateAppUtils.updateApp(context);
            }
        } else {
            //未获取权限
            EasyPermissions.requestPermissions(this, "更新版本需要读写本地权限！", Constants.UPDATE_APP, params);
        }
    }

    /**
     * Android 8.0后，安装应用需要检查打开未知来源应用权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkInstallPermission() {
        // 判断是否已打开未知来源应用权限
        boolean haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();

        if (haveInstallPermission) {
            //已经打开权限，直接启动版本更新
            UpdateAppUtils.updateApp(context);
        } else {
            AlertDialogUtils.showDialog(getContext(),
                    "请打开未知来源应用权限",
                    "为了正常升级APP，请点击设置-高级设置-允许安装未知来源应用，本功能只限用于APP版本升级。",
                    "权限设置",
                    new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            // 跳转到系统打开未知来源应用权限，在onActivityResult中启动更新
                            toInstallPermissionSettingIntent(context);
                            dialog.dismiss();
                        }
                    });
        }
    }

    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void toInstallPermissionSettingIntent(Context context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, Constants.INSTALL_PERMISSION_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.INSTALL_PERMISSION_CODE:
                checkPermission();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("我的fragment销毁");
    }

    /**
     * 当在activity设置viewPager + BottomNavigation + fragment时，
     * 为防止viewPager左滑动切换界面，与fragment左滑返回上一界面冲突引起闪退问题，
     * 必须加上此方法，禁止fragment左滑返回上一界面。
     *
     * 切记！切记！切记！否则会闪退！
     *
     * 当在fragment设置viewPager + BottomNavigation + fragment时，则不会出现这个问题。
     */
    @Override
    protected boolean canDragBack() {
        return false;
    }

}

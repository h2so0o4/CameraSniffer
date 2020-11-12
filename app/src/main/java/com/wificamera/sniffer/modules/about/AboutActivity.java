package com.wificamera.sniffer.modules.about;

import android.Manifest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.wificamera.sniffer.BuildConfig;
import com.wificamera.sniffer.R;
import com.yanzhenjie.alertdialog.AlertDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.zhy.http.okhttp.OkHttpUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.wificamera.sniffer.MainApplication.getContext;

public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.version)
    TextView mVersionTextView;

    private CommonProgressDialog pBar;

    public static String APPDOWNLOAD_URL = "http://121.89.197.101/CameraSniffer.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        checkPermission();

        mVersionTextView.setText(QMUIPackageHelper.getAppVersion(getContext()));
        showDiyDialog("13MB","测试");

    }

/*    public void updateApp() {
        //as引用库 com.qianwen:okhttp-utils:3.8.0
        OkHttpUtils.get()
                // 服务器json地址（json文件或者接口）
                .url(StringUtil.UPDATE_URL)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Response response, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        //本地APP versionCode
                        int versionCode = BuildConfig.VERSION_CODE;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            // 服务器端APP versionCode
                            int vesion = Integer.valueOf(jsonObject.optString("new_version").trim());
                            //app大小
                            String size = jsonObject.optString("target_size");
                            //更新日志
                            String log = jsonObject.optString("update_log");
                            //本地版本小于服务器版本显示更新对话框
                            if (versionCode < vesion) {
                                showDiyDialog(size, log);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }*/

    private void showDiyDialog(String targetSize, String updateLog) {
        String msg = "";
        if (!TextUtils.isEmpty(targetSize)) {
            msg = "新版本大小：" + targetSize + "\n\n";
        }

        if (!TextUtils.isEmpty(updateLog)) {
            msg += updateLog;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(String.format("是否升级到最新版本？"))
                .setMessage(msg)
                .setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadApk();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void downloadApk() {
        //显示下载进度
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        new Thread(new DownloadApk(dialog)).start();
    }

    private class DownloadApk implements Runnable {
        private ProgressDialog dialog;
        InputStream is;
        FileOutputStream fos;

        public DownloadApk(ProgressDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            //下载地址
            String url = APPDOWNLOAD_URL;
            Request request = new Request.Builder().get().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    //获取内容总长度
                    long contentLength = response.body().contentLength();
                    //设置最大值
                    dialog.setMax((int) contentLength);
                    //保存到sd卡
                    File apkFile = new File("mnt/sdcard", "HomeTheaters.apk");
                    fos = new FileOutputStream(apkFile);
                    //获得输入流
                    is = response.body().byteStream();
                    //定义缓冲区大小
                    byte[] bys = new byte[1024];
                    int progress = 0;
                    int len = -1;
                    while ((len = is.read(bys)) != -1) {
                        try {
                            Thread.sleep(1);
                            fos.write(bys, 0, len);
                            fos.flush();
                            progress += len;
                            //设置进度
                            dialog.setProgress(progress);
                        } catch (InterruptedException e) {

                        }
                    }
                    //下载完成,提示用户安装
                    installApk(apkFile);
                }
            } catch (IOException e) {

            } finally {
                //关闭io流
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    is = null;
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fos = null;
                }
            }
            dialog.dismiss();
        }
    }

    private void installApk(File file) {

        Uri uri = FileProvider.getUriForFile(AboutActivity.this, "com.wificamera.sniffer.fileProvider", file);

        //Android 7.0及以上
        if (Build.VERSION.SDK_INT >= 24) {
            if (Build.VERSION.SDK_INT >= 26) {
                boolean hasInstallPermission = this.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    //请求安装未知应用来源的权限
                    startInstallPermissionSettingActivity();
                }
            }

            //调用系统安装程序
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
        }
        else {
            Log.e("MainActivity", "小于24");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            this.startActivity(intent);
        }
    }

    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:"+getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 666);
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);

        } else {
            /*Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();*/

        }
    }

}




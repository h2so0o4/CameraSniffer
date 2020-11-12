package com.wificamera.sniffer.common.http.callback.files;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.Formatter;
import android.util.Log;

import com.wificamera.sniffer.MainApplication;
import com.wificamera.sniffer.common.notification.DownloadNotification;
import com.wificamera.sniffer.common.utils.AlertDialogUtils;
import com.wificamera.sniffer.common.utils.NetworkUtils;
import com.wificamera.sniffer.common.utils.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.File;
import java.util.Locale;

/**
 * Created  on 2019/4/15.
 * 带加载进度条的下载回调接口，默认OkGo文件回调
 */
public abstract class FileProgressDialogCallBack extends FileCallback {

    private ProgressDialog progressDialog;
    private DownloadNotification downloadNotification;

    public FileProgressDialogCallBack(Context context) {
        super(null);
        initDialog(context);
    }

    public FileProgressDialogCallBack(Context context, String destFileName) {
        super(destFileName);
        initDialog(context);
    }

    public FileProgressDialogCallBack(Context context, String destFileDir, String destFileName) {
        super(destFileDir, destFileName);
        initDialog(context);
    }


    @Override
    public void onStart(Request<File, ? extends Request> request) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public void onFinish() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (downloadNotification != null) {
            downloadNotification.downloadComplete();
        }
    }

    @Override
    public void onError(Response<File> response) {
        super.onError(response);
        if (downloadNotification != null) {
            downloadNotification.downloadError();
        }

        ToastUtils.error(response.body().getName() + "下载失败，请重新下载！");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void downloadProgress(Progress progress) {
        String downloadLength = Formatter.formatFileSize(MainApplication.getContext(), progress.currentSize);
        String totalLength = Formatter.formatFileSize(MainApplication.getContext(), progress.totalSize);

        if (progress.totalSize > 0) {
            progressDialog.setMax((int) progress.totalSize);
        }
        progressDialog.setProgress((int) (progress.fraction * progress.totalSize));
        progressDialog.setProgressNumberFormat(String.format(Locale.CHINA, "%s / %s", downloadLength, totalLength));
        Log.d("downloadProgress", progress.fileName + " 下载中……" + String.format(Locale.CHINA, "%s / %s", downloadLength, totalLength));

        if (downloadNotification != null) {
            downloadNotification.showProgress(progress);
        }
    }

    /**
     * 判断网络状态。移动/WiFi，弹出警告框
     */
    private void initDialog(final Context context) {
        if (NetworkUtils.isActiveNetworkMobile(context)) {
            String title = "温馨提示！";
            String message = "当前网络为移动网络，可能会消耗大量移动流量数据，确定要下载吗？";
            AlertDialogUtils.showDialog(context, title, message, new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    downloadingDialog(context);
                    dialog.dismiss();
                }
            });
        } else {
            downloadingDialog(context);
        }
    }

    /**
     * 显示进度条对话框
     * */
    private void downloadingDialog(final Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("正在下载");

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "停止下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OkGo.getInstance().cancelTag(context);
                ToastUtils.info("已停止下载！");
                dialog.dismiss();
            }
        });

        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadNotification = new DownloadNotification(context);
                dialog.dismiss();
            }
        });
    }

}

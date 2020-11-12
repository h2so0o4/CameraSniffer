package com.wificamera.sniffer.common.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wificamera.sniffer.MainApplication;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.utils.AlertDialogUtils;
import com.wificamera.sniffer.common.utils.ToastUtils;
import com.qmuiteam.qmui.arch.QMUIActivity;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * Created  on 2019/3/21.
 */
@SuppressLint("Registered")
public class BaseActivity extends QMUIActivity implements EasyPermissions.PermissionCallbacks {

    @Override
    protected int backViewInitOffset() {
        return QMUIDisplayHelper.dp2px(MainApplication.getContext(), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.APP_SETTING_DIALOG_REQUEST_CODE) {
            //在这儿，你可以再对权限进行检查，从而给出提示，或进行下一步操作
            checkPermission();
            ToastUtils.info("从设置中返回");
        }
    }

    //----------------以下是请求权限base，子类activity只需要重写checkPermission()方法即可----------------//

    /**
     * 检查权限，子类要申请权限，需要重写该方法
     * */
    public void checkPermission() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        //若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不再提示'，且拒绝权限。
        //跳转到设置界面去，让用户手动开启。
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            String title = "需要权限，才能正常使用：";
            String message = "如果没有请求的权限，此应用可能无法正常工作。请打开应用设置以修改应用权限。";
            AlertDialogUtils.showDialog(this, title, message, "去设置", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", MainApplication.getContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, Constants.APP_SETTING_DIALOG_REQUEST_CODE);
                    dialog.dismiss();
                }
            });
        }
    }
}

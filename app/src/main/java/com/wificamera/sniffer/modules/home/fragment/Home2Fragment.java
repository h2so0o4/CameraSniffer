package com.wificamera.sniffer.modules.home.fragment;


import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseFragment;
import com.orhanobut.logger.Logger;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.wificamera.sniffer.modules.welcome.activity.MapActivity;
import com.wificamera.sniffer.modules.welcome.activity.ScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wificamera.sniffer.modules.welcome.activity.ScanActivity.LOCATION_CODE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Home2Fragment extends BaseFragment {
    /*@BindView(R.id.showdevicebutton)
    Button showDeviceButton;*/
    @BindView(R.id.showmapbutton)
    Button showMapButton;

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    public Home2Fragment() {
    }

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home2, null);
        ButterKnife.bind(this, root);


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("功能");
        initView();
        initTopBar();
    }

    private void initTopBar() {
        mTopBar.setTitle("功能");
    }
/*
    @OnClick(R.id.showdevicebutton)
    public void onViewClicked() {
        //ToastUtils.info("功能");
        Toast.makeText(getActivity(),"查看所有设备",Toast.LENGTH_LONG).show();
    }
*/

    private void initView() {
        /*showDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"查看所有设备",Toast.LENGTH_SHORT).show();
            }
        });*/
        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager lm;//【位置管理】
                lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (ok) {//开了定位服务
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //请求权限
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);

                    } else {
                        // 有权限
                        //Toast.makeText(getActivity(), "有权限", Toast.LENGTH_SHORT).show();
                        //页面跳转
                        Intent intent = new Intent(getActivity(), MapActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 1315);
                }


            }
        });
    }




    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("第二页销毁");
    }


    /**
     * 当在activity设置viewPager + BottomNavigation + fragment时，
     * 为防止viewPager左滑动切换界面，与fragment左滑返回上一界面冲突引起闪退问题，
     * 必须加上此方法，禁止fragment左滑返回上一界面。
     *
     * 切记！切记！切记！否则会闪退！
     *
     * 当在fragment设置viewPager + BottomNavigation + fragment时，则不会出现这个问题。
     * */
    @Override
    protected boolean canDragBack() {
        return false;
    }
}

package com.wificamera.sniffer.modules.home.fragment;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseFragment;
import com.orhanobut.logger.Logger;
import com.wificamera.sniffer.modules.welcome.activity.ScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class Home1Fragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    Button scanbutton;

    public Home1Fragment() {
    }

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home1, null);
        ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("第一页创建");
        initTopBar();
    }

    @OnClick(R.id.scanbutton)
    public void onViewClicked() {
        //ToastUtils.info("主页");
        Intent intent = new Intent(getActivity(), ScanActivity.class);
        startActivity(intent);
    }

    private void initTopBar() {
        //mTopBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.app_color_theme_4));
        mTopBar.setTitle("摄像头探测器");
    }

    /*private void initTopBar() {
        mTopBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.app_color_theme_4));

        mTopBar.setTitle("沉浸式状态栏示例");
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("第一页销毁");
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

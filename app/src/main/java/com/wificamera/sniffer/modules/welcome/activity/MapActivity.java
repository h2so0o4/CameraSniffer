package com.wificamera.sniffer.modules.welcome.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.google.gson.Gson;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.constant.SPConstants;
import com.wificamera.sniffer.common.model.LocJson;
import com.wificamera.sniffer.common.utils.CoordinateUtil;
import com.wificamera.sniffer.common.utils.PreferenceUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.amap.api.maps2d.AMap.MAP_TYPE_NORMAL;
import static com.amap.api.maps2d.MapsInitializer.setApiKey;
import static com.wificamera.sniffer.modules.welcome.activity.ScanActivity.LOCATION_CODE;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private LocationManager locationManager;
    private String locationProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        QMUITopBarLayout mTopBar;
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setTitle("可疑摄像头分布地图");

        String apiKey = "ae0244943412dfea32419fb4f50f6da8";
        setApiKey(apiKey);

        MapView mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        //将地图定位至当前位置
        Location location = getLocantion();
        AMapLocation aMapLocation = fromGpsToAmap(location);
        double longitude = aMapLocation.getLongitude();
        double latitude = aMapLocation.getLatitude();
        LatLng HEXIN = new LatLng(latitude,longitude);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HEXIN, 17));

        //显示自己的位置坐标mark
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.title("我的位置");
        markerOptions.visible(true);
        Marker marker = aMap.addMarker(markerOptions);
        marker.showInfoWindow();
        
        aMap.setMapType(MAP_TYPE_NORMAL);

        try {
            setMark();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void setMark() throws IOException {
        String url = Constants.SERVICE_IP + "/history/getInfo.do";
        String token = PreferenceUtils.getString(SPConstants.TOKEN, "");
        String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");

        //客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(2000, TimeUnit.MILLISECONDS)
                .writeTimeout(2000, TimeUnit.MILLISECONDS)
                .build();


        Request request = new Request.Builder()
                .get()
                .addHeader("x-access-token", token)
                .addHeader("uid", uid)
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        //解析json数据
        String jsonStr = response.body().string();
        Gson gosn = new Gson();
        LocJson locJson = gosn.fromJson(jsonStr,LocJson.class);
        List list = locJson.getData().getList();
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(new LatLng(locJson.getData().getList().get(i).getLatitude(),locJson.getData().getList().get(i).getLongitude()));
            markerOption.draggable(false);//设置Marker可拖动
            markerOption.title(locJson.getData().getList().get(i).getRemark());
            /*markerOption.icon(BitmapDescriptorFactory.fromView(getMyView(locJson.getData().getList().get(i).getRemark()) ));*/

            aMap.addMarker(markerOption);
        }

    }

    public AMapLocation fromGpsToAmap(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLng = CoordinateUtil.transformFromWGSToGCJ(latLng);
        AMapLocation aMapLocation = new AMapLocation(location);
        aMapLocation.setLatitude(latLng.latitude);
        aMapLocation.setLongitude(latLng.longitude);

        return aMapLocation;
    }

    private Location getLocantion() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //获取权限（如果没有开启权限，会弹出对话框，询问是否开启权限）
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                //监视地理位置变化
                locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) {
                    return location;
                }
            }
        } /*else {
            //监视地理位置变化
            locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                //不为空,显示地理位置经纬度
                Toast.makeText(this, location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                return location;
            }
        }*/
        return null;
    }

    public LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
        }
        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
        }
        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                /*//不为空,显示地理位置经纬度
                Toast.makeText(ScanActivity.this, location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();*/
            }
        }
    };
}

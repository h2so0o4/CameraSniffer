package com.wificamera.sniffer.modules.welcome.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.model.LatLng;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.wificamera.sniffer.R;
import com.wificamera.sniffer.common.base.BaseActivity;
import com.wificamera.sniffer.common.constant.Constants;
import com.wificamera.sniffer.common.constant.SPConstants;
import com.wificamera.sniffer.common.model.LocJson;
import com.wificamera.sniffer.common.model.LoginJson;
import com.wificamera.sniffer.common.utils.CoordinateUtil;
import com.wificamera.sniffer.common.utils.PreferenceUtils;
import com.wificamera.sniffer.modules.home.fragment.CirCleProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.locks.*;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.wificamera.sniffer.modules.welcome.activity.LoginActivity.JsonToJavaObj;


public class ScanActivity extends BaseActivity {
    // 锁对象，扫描线程和主线程同步
    private Lock scanLock = new ReentrantLock();
    CirCleProgressBar cirCleProgressBar;
    //MediaPlayer mediaPlayer = new MediaPlayer();//这个我定义了一个成员函数
    Socket socket = null;
    //匹配C类地址的IP
    public static final String regexCIp = "^192\\.168\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    //匹配A类地址
    public static final String regexAIp = "^10\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    //匹配B类地址
    public static final String regexBIp = "^172\\.(1[6-9]|2\\d|3[0-1])\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)\\.(\\d{1}|[1-9]\\d|1\\d{2}|2[0-4]\\d|25\\d)$";
    private static final String TAG = ScanActivity.class.getSimpleName();
    /**
     * 核心池大小
     **/
    private static final int CORE_POOL_SIZE = 2;
    /**
     * 线程池最大线程数
     **/
    private static final int MAX_IMUM_POOL_SIZE = 255;
    private String mDevAddress;// 本机IP地址-完整
    private String mLocAddress;// 局域网IP地址头,如：192.168.1.
    private Runtime mRun = Runtime.getRuntime();// 获取当前运行环境，来执行ping，相当于windows的cmd
    private Process mProcess = null;// 进程
    private String mPing = "ping -c 1 -w 10 ";// 其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    private List<String> mIpList = new ArrayList<String>();// ping成功的IP地址
    private List<String> mAliveIpList = new ArrayList<String>();// 局域网存活IP地址
    private ThreadPoolExecutor mExecutor;// 线程池对象
    String ipstr;

    private LocationManager locationManager;//地理位置信息
    public static final int LOCATION_CODE = 301;
    private String locationProvider = null;

    // 消息
    protected static final int MSG_PROGRESSPLUS = 1;
    protected static final int MSG_SCANFINISHED = 2;
    // 进度条控制
    private float progressValue = 0;
    private int threadCount = 0;

    QMUITopBarLayout mTopBar;

    private TextView susIP;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESSPLUS:
                    Log.e("",threadCount+"");
                    progressValue=(float)threadCount/(255-2)*100;
                    progressValue=progressValue>100?100:progressValue;
                    Log.e("",progressValue+"");
                    cirCleProgressBar.setCurrentProgress((float) progressValue);
                    cirCleProgressBar.setText(true, (int) progressValue + "%");

                    if(progressValue==100){
                        Message msg2 = Message.obtain();
                        msg2.what = MSG_SCANFINISHED;
                        mHandler.sendMessage(msg2);
                    }
                    break;
                case MSG_SCANFINISHED:
                    for(int i=0;i<mAliveIpList.size();i++){
                        Log.i("ip:",mAliveIpList.get(i)+"");
                    }
                    if (mIpList.size() > 0) {
                        ipstr+="数量："+mIpList.size()+"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n";
                        for (int i = 0; i < mIpList.size(); i++) {
                            ipstr += mIpList.get(i) + "\n";
                        }

                        susIP = findViewById(R.id.susIP);
                        susIP.setMovementMethod(ScrollingMovementMethod.getInstance());//可滚动文本框


                        susIP.setText(ipstr);


                        AlertDialog alertDialog1 = new AlertDialog.Builder(ScanActivity.this)
                                .setTitle("扫描须知")//标题
                                .setMessage("  已扫描出可疑摄像头IP地址！\n\n" +
                                        "  请仔细检查身边是否有可疑设备(把检测出的IP地址在浏览器中打开可以更好地查看该设备详细信息)。\n\n" +
                                        "  强烈建议您前往功能页面查看设备地图显示，判断附近是否为摄像头高发区域。\n\n"+
                                        "  如果您确定您身边存在偷窥摄像头，您可以选择将当前地理位置信息上传，该信息将作为检测摄像头的依据,谢谢配合！")//内容
                                .setIcon(R.mipmap.ic_launcher_round)//图标
                                .setNegativeButton("确定", new DialogInterface.OnClickListener() {//添加取消
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(ScanActivity.this, "注意保护隐私哦！！！", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .create();
                        alertDialog1.show();
                    } else {
                        Toast.makeText(ScanActivity.this, "未找到可疑摄像头，注意保护隐私哦！！！", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        }
    });


    /**
     * TODO<扫描局域网内ip，找到对应服务器>
     *
     * @return void
     */
    public synchronized List<String> scan() {
        //scanLock.lock();
        mAliveIpList.clear();
        mIpList.clear();
        threadCount = 0;

        if(!isWifiConnect()){
            Toast.makeText(ScanActivity.this,"扫描失败，请检查WIFI网络",Toast.LENGTH_SHORT).show();
            return null;
        }

        mDevAddress = getHostIp();// 获取本机IP地址
        mLocAddress = getLocAddrIndex(mDevAddress);// 获取本地ip前缀
        Log.e(TAG, "开始扫描设备,本机Ip为：" + mDevAddress);

        if (TextUtils.isEmpty(mLocAddress)) {
            Log.e(TAG, "扫描失败，请检查wifi网络");
            Toast.makeText(ScanActivity.this,"扫描失败，请检查wifi网络",Toast.LENGTH_SHORT).show();
            return null;
        }

        /**
         * 1.核心池大小 2.线程池最大线程数 3.表示线程没有任务执行时最多保持多久时间会终止
         * 4.参数keepAliveTime的时间单位，有7种取值,当前为毫秒
         * 5.一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响
         * ，一般来说，这里的阻塞队列有以下几种选择：
         */
        //Log.e("MAX_IMUM_POOL_SIZE",MAX_IMUM_POOL_SIZE+"");
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE,
                2000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                100));

        // 新建线程池
        for (int i = 1; i < 255; i++) {// 创建256个线程分别去ping
            final int lastAddress = i;// 存放ip最后一位地址 1-255

            Runnable run = new Runnable() {

                @Override
                public void run() {

                    String ping = ScanActivity.this.mPing + mLocAddress
                            + lastAddress;  //cmd命令代码
                    String currnetIp = mLocAddress + lastAddress; //当前ip地址
                    if (mDevAddress.equals(currnetIp)) // 如果与本机IP地址相同,跳过
                        return;

                    try {
                        mProcess = mRun.exec(ping); //cmd控制台执行命令

                        int result = mProcess.waitFor(); //成功：0   失败：1

                        //Log.e(TAG, "正在扫描的IP地址为：" + currnetIp + "返回值为：" + result);
                        if (result == 0) {
                            Log.e(TAG, "扫描成功,Ip地址为：" + currnetIp);
                            mAliveIpList.add(currnetIp);
                            //摄像头常用554端口
                            socket = new Socket(currnetIp, 554);
                            boolean isConnected = socket.isConnected() && !socket.isClosed();
                            if (isConnected) {
                                /*String message = getMessage(socket,currnetIp);
                                Log.e(TAG, ""+message);*/

                                Log.e(TAG, "端口开放Ip地址为：" + currnetIp);
                                mIpList.add(currnetIp);

                            }
                            //关闭Socket
                            socket.close();

                        } else {
                            // 扫描失败
                            //Log.e(TAG, "扫描失败");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "扫描异常" + e.toString());
                    } finally {
                        scanLock.lock();
                        threadCount ++;
                        scanLock.unlock();
                        Message msg = Message.obtain();
                        msg.what = MSG_PROGRESSPLUS;
                        mHandler.sendMessage(msg);
                        if (mProcess != null)
                            mProcess.destroy();
                    }
                }
            };

            mExecutor.execute(run);
        }
        mExecutor.shutdown();
        return mIpList;
    }


    /**
     * TODO<获取本地ip地址>
     *
     * @return String
     */
    public static String getHostIp() {
        String hostIp;
        Pattern ip = Pattern.compile("(" + regexAIp + ")|" + "(" + regexBIp + ")|" + "(" + regexCIp + ")");
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress address;
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                address = inetAddresses.nextElement();
                String hostAddress = address.getHostAddress();
                Matcher matcher = ip.matcher(hostAddress);
                if (matcher.matches()) {
                    hostIp = hostAddress;
                    return hostIp;
                }

            }
        }
        return null;
    }

    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress // 本机IP地址
     * @return String
     */
    private String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    //给ip地址发包并返回指纹信息
    private String getMessage(Socket client,String ip) throws IOException {
        String request = "GET /index.html HTTP/1.1\r\n" +
                "Host: " + ip + ":554\r\n";
        PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
        pWriter.println(request);

        String tem;
        String sreverMes = "";
        // 这里要注意二进制字节流转换为字符流编码要使用UTF-8
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream(), "utf-8"));

        /*// 获取当前系统时间
        long startTime =  System.currentTimeMillis();
        new Runnable() {
            @Override
            public void run() {
                long endTime =  System.currentTimeMillis();
                long usedTime = (endTime-startTime)/1000;
                if(usedTime>2) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };*/

        while((tem = bufferedReader.readLine()).length() != 0) {
            sreverMes+=tem+"\n";
        }

        System.out.println(sreverMes);
        return sreverMes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cirCleProgressBar = findViewById(R.id.ccb);
        Button btn = findViewById(R.id.btn);
        Button bt_up = findViewById(R.id.bt_up);

        final boolean[] isoncl = {true};

        //cameraView = findViewById(R.id.camera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIpList.clear();
                ipstr = "";
                if(isoncl[0]){
                    //你要运行的方法
                    scan();
                    isoncl[0] =false; //点击一次后就改成false，这样就实现只点击一次了
                }
            }
        });

        bt_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uploadLoc();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }



    private void uploadLoc() throws JSONException, IOException {
        if(mIpList.size() > 0){
            Location location = getLocantion();
            AMapLocation aMapLocation = fromGpsToAmap(location);
            if(location != null){
                double longitude = aMapLocation.getLongitude();
                double latitude = aMapLocation.getLatitude();
                if(!isSimilarLoc(longitude,latitude)){
                    String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");
                    String token = PreferenceUtils.getString(SPConstants.TOKEN, "");

                    EditText mark = findViewById(R.id.mark);

                    String remark = mark.getText().toString();
                    if(!remark.equals("")){
                        String url = Constants.SERVICE_IP + "/history/saveInfo.do?latitude=" + latitude + "&longitude=" + longitude + "&remark=" + remark;

                        JSONObject obj = new JSONObject();
                        obj.put("uid", uid);
                        obj.put("x-access-token", token);
                        obj.put("longitude", longitude);
                        obj.put("latitude", latitude);

                        //客户端
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                                .readTimeout(2000, TimeUnit.MILLISECONDS)
                                .writeTimeout(2000, TimeUnit.MILLISECONDS)
                                .build();

                        MediaType mediaType = MediaType.parse("application/json");
                        RequestBody requestBody = RequestBody.create(mediaType, obj.toString());

                        Request request = new Request.Builder()
                                .post(requestBody)
                                .addHeader("x-access-token",token)
                                .addHeader("uid",uid)
                                .url(url)
                                .build();

                        Response response = okHttpClient.newCall(request).execute();

                        if (response.isSuccessful()) {
                            JSONObject jsonObject = new JSONObject(response.body().string());

                            LoginJson loginJson = JsonToJavaObj(jsonObject.toString());
                            String msg = loginJson.getMsg();
                            Toast.makeText(ScanActivity.this,""+msg,Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ScanActivity.this,"服务器连接失败！",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(ScanActivity.this,"备注不能为空！",Toast.LENGTH_SHORT).show();
                    }


                }
                else{
                    Toast.makeText(ScanActivity.this,"您已经上传过附近的可疑摄像头了！",Toast.LENGTH_SHORT).show();
                }


            }
            else{
                Toast.makeText(ScanActivity.this,"请打开定位选项及权限！",Toast.LENGTH_SHORT).show();
            }


        }
        else{
            Toast.makeText(ScanActivity.this,"未扫描出可疑IP地址，禁止上传！",Toast.LENGTH_SHORT).show();
        }
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

    public boolean isSimilarLoc(double longitude,double latitude) throws IOException {
        String url = Constants.SERVICE_IP + "/history/getCurrentUserHistoryInfo.do";
        String token = PreferenceUtils.getString(SPConstants.TOKEN, "");
        String uid = PreferenceUtils.getString(SPConstants.USER_ID, "");

        //客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();


        Request request = new Request.Builder()
                .get()
                .addHeader("x-access-token", token)
                .addHeader("uid", uid)
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            //解析json数据
            String jsonStr = response.body().string();
            Gson gosn = new Gson();
            LocJson locJson = gosn.fromJson(jsonStr,LocJson.class);
            int total = locJson.getData().getTotal();
            //遍历list查找是否以前上传的经纬度存在距离较近的情况
            for (int n = 0; n < total; n++) {
                //获取以前上传的经纬度
                double latitude1 = locJson.getData().getList().get(n).getLatitude();
                double longitude1 = locJson.getData().getList().get(n).getLongitude();

                //经度差和纬度差
                double laTemp = Math.abs(latitude - latitude1);
                double longTemp = Math.abs(longitude - longitude1);

                //两地相差小于100米
                if (laTemp < 0.001 && longTemp < 0.001) {
                    return true;
                }
            }


        }
        return false;
    }

    public AMapLocation fromGpsToAmap(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLng = CoordinateUtil.transformFromWGSToGCJ(latLng);
        AMapLocation aMapLocation = new AMapLocation(location);
        aMapLocation.setLatitude(latLng.latitude);
        aMapLocation.setLongitude(latLng.longitude);

        return aMapLocation;
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


    /*
    @Override
    public void onSensorChanged(SensorEvent event) {
        if( count++ == 20){ //磁场传感器很敏感，每20个变化，显示一次数值
            double value = Math.sqrt(event.values[0]*event.values[0]
                    + event.values[1]*event.values[1]+event.values[2]*event.values[2]);
            //String str = String.format("X:%8.4f , Y:%8.4f , Z:%8.4f \n总值为：%8.4f", event.values[0],event.values[1],event.values[2],value);
            count = 1;
            cirCleProgressBar.setCurrentProgress((float)value);
            cirCleProgressBar.setText(true, (int)value+"ut");
            if((int)value>70&&(int)value<300)
            {
                cirCleProgressBar.setCircleColor(Color.rgb(255,193,7));
                cirCleProgressBar.setTextColor(Color.rgb(255,193,7));
                Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
                vibrator.vibrate(300);
            }else if((int)value>300)
            {
                cirCleProgressBar.setCircleColor(Color.rgb(216,27,96));
                cirCleProgressBar.setTextColor(Color.rgb(216,27,96));
                Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
                vibrator.vibrate(300);
                mediaPlayer.start();
            }else
            {
                cirCleProgressBar.setCircleColor(Color.rgb(3,169,244));
                cirCleProgressBar.setTextColor(Color.rgb(3,169,244));
            }
        }
    }*/
    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private long mExitTime;

    private void initTopBar() {
        //mTopBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.app_color_theme_4));
        mTopBar.setTitle("扫描");
    }

    //是否连接WIFI
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*//与上次点击返回键时刻作差
            *//*if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;*/
            ScanActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}



package cn.letsit.code;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
    private static final int PRIVATE_CODE = 1315;//开启GPS权限
    static final String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE};
    private Button submit;
    private EditText editTextAddress;
    private EditText editTextDeviceId;
    private Button picture;
    private AlertDialog AlertFailure;
    private AlertDialog locationSuccess;
    private AlertDialog locationError;
   private  AlertDialog AlerDialogsuccess;
    private String address="";
    private String deviceId="";
    private Button lngLat;
    private String base64ImageData;
    private ImageView imageView;
    private Intent intent;
    AlertDialog alertDialog1;
    private File file;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final int PICK = 1;
    public double lat=0.0;
   public  double lng=0.0;
    private String url = "http://www.topiot.co/api/v1/deviceloaction/upload";
    public static long time = 0;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage = findViewById(R.id.message);
        imageView = findViewById(R.id.imageView);
        editTextAddress=findViewById(R.id.address);
        editTextDeviceId=findViewById(R.id.deviceId);
        picture = findViewById(R.id.picture);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 拍照
                //设置图片的保存路径,作为全局变量
                System.out.println("打开摄像头拍照");
                intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK);
            }
        });
        lngLat = findViewById(R.id.locate);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(null);
        locationManager.getProviders(true);
        //final String provider=LocationManager.NETWORK_PROVIDER;
        lngLat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("获取经纬度");
                showGPSContacts();
            }
        });
        // 点击上传
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("上传图片");
                address = editTextAddress.getText().toString();
                deviceId = editTextDeviceId.getText().toString();
                System.out.println("address:" + address);


                if (address==""||deviceId==""||lng==0.0||lat==0.0) {

                    alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("错误")//标题
                            .setMessage("内容未完善！")//内容
                            .setPositiveButton("重新完善内容", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, "重新完善内容", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }).setIcon(R.mipmap.ic_launcher).create();//图标
                    alertDialog1.show();


                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                            RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
                            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                    .addFormDataPart("file", deviceId, fileBody)
                                    .addFormDataPart("address", address)
                                    .addFormDataPart("deviceId", deviceId)
                                    .addFormDataPart("latlong", lat + ":" + lng).build();
                            Request request = new Request.Builder().url(url).post(requestBody).build();
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    Log.i("response:", e.toString());
                                    Looper.prepare();
                                  AlertFailure= new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("上传失败")//标题
                                            .setMessage("上传失败请联系开发者:17615855621！")//内容
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            }).setIcon(R.mipmap.ic_launcher).create();//图标
                                    AlertFailure.show();

                                    Looper.loop();
                                    System.out.println(e.getMessage());
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    Log.i("response:", response.toString());
                                    Looper.prepare();
                                   AlerDialogsuccess= new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("上传成功")//标题
                                            .setMessage("上传成功！")//内容
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            }).setIcon(R.mipmap.ic_launcher).create();//图标
                                    AlerDialogsuccess.show();
                                    Looper.loop();
                                    System.out.println("上传成功");
                                }
                            });


                        }
                    }).start();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK:
                if (resultCode == RESULT_OK) {
                    //将拍摄的照片显示出来
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    base64ImageData = bitmapToBase64(bitmap);
                    imageView.setImageBitmap(bitmap);
                    file= compressImage(bitmap);
                    // 拍照完成并且显示完成
                    // todo 上传图片和位置信息

                }
                break;
            default:
                break;
        }
    }

    public File compressImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String filename = format.format(date);
        File file = new File(Environment.getExternalStorageDirectory(), filename + ".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }


    //如上参需要64位编码可调用此方法，不需要可以忽略
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 检测GPS、位置权限是否开启
     */
    public void showGPSContacts() {
        LocationManager lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限。
                    ActivityCompat.requestPermissions(MainActivity.this, LOCATIONGPS,
                            BAIDU_READ_PHONE_STATE);
                } else {
                    getLocation();//getLocation为定位方法
                }
            } else {
                getLocation();//getLocation为定位方法
            }
        } else {
            Toast.makeText(MainActivity.this, "系统检测到未开启GPS定位服务,请开启", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, PRIVATE_CODE);
        }
    }

    /**
     * 获取具体位置的经纬度
     */
    private void getLocation() {
        // 获取位置管理服务
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) MainActivity.this.getSystemService(serviceName);
        // 查找到服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
        /**这段代码不需要深究，是locationManager.getLastKnownLocation(provider)自动生成的，不加会出错**/
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
        updateLocation(location);
    }

    /**
     * 获取到当前位置的经纬度
     *
     * @param location
     */
    private void updateLocation(Location location) {
        if (location != null) {
            lat = location.getLatitude();
             lng= location.getLongitude();
            locationSuccess = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示信息")//标题
                    .setMessage("获取定位成功！")//内容
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "获取定位成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }).setIcon(R.mipmap.ic_launcher).create();//图标
            locationSuccess.show();

            System.out.println("lat"+lat+":"+"location"+lng);
        } else {
            System.out.println("获取失败");
            locationError = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("定位失败")//标题
                    .setMessage("获取定位失败联系开发者：17615855621！")//内容
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "获取定位失败", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }).setIcon(R.mipmap.ic_launcher).create();//图标
            locationError.show();
        }
    }
}

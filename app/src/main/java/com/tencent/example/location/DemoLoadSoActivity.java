package com.tencent.example.location;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 演示App如何自定义加载libtencentloc.so
 * <p/>
 * App自行加载libtencentloc.so, 能使用更为灵活的方式, 可以有效规避App安装不完全(通常是so未安装成功, 实际中极小概率出现)
 * 时的 UnsatisfiedLinkError 问题
 * <p/>
 * <strong>注意: 不建议自行加载libtencentloc.so</strong>
 */
public class DemoLoadSoActivity extends Activity implements TencentLocationListener {
    private static final String TAG = "DemoLoadSoActivity";
    private TencentLocationManager mLocationManager;
    private TextView mLocationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        mLocationStatus = (TextView) findViewById(R.id.status);

        // 调用定位前应首先禁止定位SDK自动加载 "libtencentloc.so"
        TencentLocationManagerOptions.setLoadLibraryEnabled(false);
        mLocationManager = TencentLocationManager.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }

    /**
     * 演示如何从assets中拷贝so并加载. 注意, 这是一个简单版本, 实际项目中在主线程中进行IO操作可能不合适!
     */
    private void customLoadLibrary() {
        String soName = "my_libtencentloc.so";
        File soFile = getFileStreamPath(soName);
        if (soFile.exists()) {
            System.load(soFile.getAbsolutePath());
        } else {
            if (copyFromAssets(soName, soFile)) {
                System.load(soFile.getAbsolutePath());
            } else {
                // 无法拷贝!!!
                throw new IllegalStateException();
            }
        }
    }

    public void startLocation(View view) {
        boolean loaded = false;
        try {
            // 优先使用默认加载方式
            System.loadLibrary("tencentloc");
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "System.loadLibrary() failed, try customLoadLibrary()");
            try {
                // 自定义加载作为备选方案
                customLoadLibrary();
                loaded = true;
            } catch (UnsatisfiedLinkError e2) {
                Log.e(TAG, "System.load() failed");
            } catch (IllegalStateException e2) {
                Log.e(TAG, "copyFromAssets() failed");
            }
        }
        if (!loaded) {
            Toast.makeText(this, "无法使用定位功能", Toast.LENGTH_SHORT).show();
            return;
        }

        // App必须确保加载so成功, 否则 requestLocationUpdates 会因为找不到JNI方法而失败!
        int error = mLocationManager.requestLocationUpdates(TencentLocationRequest.create().setInterval(3000), this);
        if (error != 0) {
            Log.e(TAG, "Location failed: error=" + error);
        }
    }

    public void stopLocation(View view) {
        mLocationManager.removeUpdates(this);
    }

    public void clearStatus(View view) {
        mLocationStatus.setText(null);
    }

    public void settings(View view) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (error == TencentLocation.ERROR_OK) {
            // 定位成功
            String msg = location.getLatitude() + " " + location.getLongitude();
            updateLocationStatus(msg);
        } else {
            // 定位失败
            updateLocationStatus("定位失败: " + reason);
        }
    }

    @Override
    public void onStatusUpdate(String provider, int status, String desc) {
        Log.i(TAG, "Provider=" + provider + ", status=" + status + ", desc=" + desc);
    }

    private void updateLocationStatus(String message) {
        mLocationStatus.append(message);
        mLocationStatus.append("\n---\n");
    }

    private boolean copyFromAssets(String name, File dest) {
        int len;
        byte[] buf = new byte[4096];
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getAssets().open(name);
            out = new FileOutputStream(dest);
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            return false;
        } finally {
            DemoUtils.closeQuietly(in);
            DemoUtils.closeQuietly(out);
        }
        return true;
    }
}

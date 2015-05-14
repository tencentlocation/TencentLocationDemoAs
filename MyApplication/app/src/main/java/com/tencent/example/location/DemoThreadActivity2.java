package com.tencent.example.location;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 * 在新线程中发起定位.
 *
 */
public class DemoThreadActivity2 extends Activity implements
		TencentLocationListener {

	private Handler mHandler;
	private HandlerThread mThread;

	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);

		mThread = new HandlerThread("Thread_demo_" + (int) (Math.random() * 10));
		mThread.start();
		mHandler = new Handler(mThread.getLooper());

		mLocationManager = TencentLocationManager.getInstance(this);
		// 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
		mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/**
		 * 注意, 本示例中 requestLocationUpdates 和 removeUpdates 都可能被多次重复调用.
		 * <p>
		 * 重复调用 requestLocationUpdates, 将忽略之前的 reqest 并自动取消之前的 listener, 并使用最新的
		 * request 和 listener 继续定位
		 * <p>
		 * 重复调用 removeUpdates, 将定位停止
		 */

		// 退出 activity 前一定要停止定位!
		stopLocation(null);
		// 清空
		mHandler.removeCallbacksAndMessages(null);
		// 停止线程
		mThread.getLooper().quit();
	}

	// ====== view listener

	// 响应点击"停止"
	public void stopLocation(View view) {
		mLocationManager.removeUpdates(this);

		updateLocationStatus("停止定位");
	}

	// 响应点击"开始"
	public void startLocation(View view) {

		// 创建定位请求
		final TencentLocationRequest request = TencentLocationRequest.create();

		// 修改定位请求参数, 定位周期 3000 ms
		request.setInterval(3000);

		// 在 mThread 线程发起定位
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mLocationManager.requestLocationUpdates(request,
						DemoThreadActivity2.this);
			}
		});

		updateLocationStatus("开始定位: " + request + ", 坐标系="
				+ DemoUtils.toString(mLocationManager.getCoordinateType()));
	}

	public void clearStatus(View view) {
		mLocationStatus.setText(null);
	}

	// ====== view listener

	// ====== location callback

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		String msg = null;
		if (error == TencentLocation.ERROR_OK) {

			// 当前线程名字
			String threadName = Thread.currentThread().getName();

			// 定位成功
			StringBuilder sb = new StringBuilder();
			sb.append("(纬度=").append(location.getLatitude()).append(",经度=")
					.append(location.getLongitude()).append(",精度=")
					.append(location.getAccuracy()).append("), 来源=")
					.append(location.getProvider()).append(", 地址=")
					.append(location.getAddress());
			sb.append(", 当前线程=" + threadName);
			msg = sb.toString();
		} else {
			// 定位失败
			msg = "定位失败: " + reason;
		}

		final String message = msg;
		// 注意! 如果 onLocationChanged 不是在主线程中回调, 一定不要直接修改 UI
		mLocationStatus.post(new Runnable() {

			@Override
			public void run() {
				updateLocationStatus(message);

			}
		});
	}

	@Override
	public void onStatusUpdate(String name, int status, String desc) {
		// ignore
	}

	// ====== location callback

	private void updateLocationStatus(String message) {
		mLocationStatus.append(message);
		mLocationStatus.append("\n---\n");
	}

}

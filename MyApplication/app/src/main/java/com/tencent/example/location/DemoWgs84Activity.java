package com.tencent.example.location;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 * 使用 WGS84 坐标定位.
 * <p>
 * 无网络 + 有GPS 条件下, 使用 WGS84 坐标可定位, 而使用 GCJ-02 坐标无法定位!
 * 
 */
public class DemoWgs84Activity extends Activity implements
		TencentLocationListener {

	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);
		mLocationManager = TencentLocationManager.getInstance(this);

		/* 保证调整坐标系前已停止定位 */
		mLocationManager.removeUpdates(null);
		// 设置 wgs84 坐标系
		mLocationManager
				.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_WGS84);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*
		 * 注意, 本示例中 requestLocationUpdates 和 removeUpdates 都可能被多次重复调用.
		 * <p>
		 * 重复调用 requestLocationUpdates, 将忽略之前的 request 并自动取消之前的 listener, 并使用最新的
		 * request 和 listener 继续定位
		 * <p>
		 * 重复调用 removeUpdates, 定位停止
		 */

		// 退出 activity 前一定要停止定位!
		stopLocation(null);
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
		TencentLocationRequest request = TencentLocationRequest.create();

		// 修改定位请求参数, 定位周期 3000 ms
		request.setInterval(3000);

		// 开始定位
		mLocationManager.requestLocationUpdates(request, this);

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
			// 定位成功
			StringBuilder sb = new StringBuilder();
			sb.append("(纬度=").append(location.getLatitude()).append(",经度=")
					.append(location.getLongitude()).append(",精度=")
					.append(location.getAccuracy()).append("), 来源=")
					.append(location.getProvider()).append(", 地址=")
					// 注意, 根据国家相关法规, wgs84坐标下无法提供地址信息
					.append("{84坐标下不提供地址!}");
			msg = sb.toString();
		} else {
			// 定位失败
			msg = "定位失败: " + reason;
		}
		updateLocationStatus(msg);
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

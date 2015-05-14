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
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 * 演示 onStatusUpdate 接口.
 *
 */
public class DemoStatusActivity extends Activity implements
		TencentLocationListener {
	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);
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

		// 修改定位请求参数, 周期为 5000 ms
		request.setInterval(5000);

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
					.append(location.getAddress());
			msg = sb.toString();
		} else {
			// 定位失败
			msg = "定位失败: " + reason;
		}

		// TODO 不将回调信息打印到 UI
		// updateLocationStatus(msg);
		Log.i("DemoStatusActivity", msg);
	}

	@Override
	public void onStatusUpdate(String name, int status, String desc) {
		String message = "{name=" + name + ", new status=" + status + ", desc="
				+ desc + "}";

		if (status == STATUS_DENIED) {
			/* 检测到定位权限被内置或第三方的权限管理或安全软件禁用, 导致当前应用**很可能无法定位**
			 * 必要时可对这种情况进行特殊处理, 比如弹出提示或引导
			 */
			Toast.makeText(this, "定位权限被禁用!", Toast.LENGTH_SHORT).show();
		}

		updateLocationStatus(message);
	}

	// ====== location callback

	private void updateLocationStatus(String message) {
		mLocationStatus.append(message);
		mLocationStatus.append("\n---\n");
	}

}

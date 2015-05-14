package com.tencent.example.location;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.map.GeoPoint;
import com.tencent.tencentmap.mapsdk.map.MapActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.Overlay;
import com.tencent.tencentmap.mapsdk.map.Projection;

/**
 * 在腾讯地图上显示我的位置.
 *
 * <p>
 * 地图SDK相关内容请参考<a
 * href="http://open.map.qq.com/android_v1/index.html">腾讯地图SDK</a>
 */
public class DemoMapActivity extends MapActivity implements
		TencentLocationListener {

	private TextView mStatus;
	private MapView mMapView;
	private LocationOverlay mLocationOverlay;

	private TencentLocation mLocation;
	private TencentLocationManager mLocationManager;

	// 用于记录定位参数, 以显示到 UI
	private String mRequestParams;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_map);

		mStatus = (TextView) findViewById(R.id.status);
		mStatus.setTextColor(Color.RED);
		initMapView();

		mLocationManager = TencentLocationManager.getInstance(this);
		// 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
		mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
	}

	private void initMapView() {
		mMapView = (MapView) findViewById(R.id.mapviewOverlay);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(9);

		Bitmap bmpMarker = BitmapFactory.decodeResource(getResources(),
				R.drawable.mark_location);
		mLocationOverlay = new LocationOverlay(bmpMarker);
		mMapView.addOverlay(mLocationOverlay);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocation();
	}

	// ===== view listeners
	public void myLocation(View view) {
		if (mLocation != null) {
			mMapView.getController().animateTo(of(mLocation));
		}
	}

	// ===== view listeners

	// ====== location callback

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		if (error == TencentLocation.ERROR_OK) {
			mLocation = location;

			// 定位成功
			StringBuilder sb = new StringBuilder();
			sb.append("定位参数=").append(mRequestParams).append("\n");
			sb.append("(纬度=").append(location.getLatitude()).append(",经度=")
					.append(location.getLongitude()).append(",精度=")
					.append(location.getAccuracy()).append("), 来源=")
					.append(location.getProvider()).append(", 地址=")
					.append(location.getAddress());

			// 更新 status
			mStatus.setText(sb.toString());

			// 更新 location 图层
			mLocationOverlay.setAccuracy(mLocation.getAccuracy());
			mLocationOverlay.setGeoCoords(of(mLocation));
			mMapView.invalidate();
		}
	}

	@Override
	public void onStatusUpdate(String name, int status, String desc) {
		// ignore
	}

	// ====== location callback

	private void startLocation() {
		TencentLocationRequest request = TencentLocationRequest.create();
		request.setInterval(5000);
		mLocationManager.requestLocationUpdates(request, this);

		mRequestParams = request.toString() + ", 坐标系="
				+ DemoUtils.toString(mLocationManager.getCoordinateType());
	}

	private void stopLocation() {
		mLocationManager.removeUpdates(this);
	}

	// ====== util methods

	private static GeoPoint of(TencentLocation location) {
		GeoPoint ge = new GeoPoint((int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
		return ge;
	}
}

class LocationOverlay extends Overlay {

	GeoPoint geoPoint;
	Bitmap bmpMarker;
	float fAccuracy = 0f;

	public LocationOverlay(Bitmap mMarker) {
		bmpMarker = mMarker;
	}

	public void setGeoCoords(GeoPoint point) {
		if (geoPoint == null) {
			geoPoint = new GeoPoint(point.getLatitudeE6(),
					point.getLongitudeE6());
		} else {
			geoPoint.setLatitudeE6(point.getLatitudeE6());
			geoPoint.setLongitudeE6(point.getLongitudeE6());
		}
	}

	public void setAccuracy(float fAccur) {
		fAccuracy = fAccur;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView) {
		if (geoPoint == null) {
			return;
		}
		Projection mapProjection = mapView.getProjection();
		Paint paint = new Paint();
		Point ptMap = mapProjection.toPixels(geoPoint, null);
		paint.setColor(Color.BLUE);
		paint.setAlpha(8);
		paint.setAntiAlias(true);

		float fRadius = mapProjection.metersToEquatorPixels(fAccuracy);
		canvas.drawCircle(ptMap.x, ptMap.y, fRadius, paint);
		paint.setStyle(Style.STROKE);
		paint.setAlpha(200);
		canvas.drawCircle(ptMap.x, ptMap.y, fRadius, paint);

		if (bmpMarker != null) {
			paint.setAlpha(255);
			canvas.drawBitmap(bmpMarker, ptMap.x - bmpMarker.getWidth() / 2,
					ptMap.y - bmpMarker.getHeight() / 2, paint);
		}

		super.draw(canvas, mapView);
	}
}

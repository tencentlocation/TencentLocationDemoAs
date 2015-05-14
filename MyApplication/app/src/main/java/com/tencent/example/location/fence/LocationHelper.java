package com.tencent.example.location.fence;

import android.content.Context;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public class LocationHelper implements TencentLocationListener {

	private Context mContext;
	private TencentLocation mLastLocation;
	private int mLastError;
	private boolean mStarted;

	private Runnable mTmp;

	public LocationHelper(Context context) {
		super();
		this.mContext = context;
	}

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		mLastError = error;
		mLastLocation = location;
		if (mTmp != null) {
			mTmp.run();
		}

		// 自动停止
		stop();
	}

	@Override
	public void onStatusUpdate(String arg0, int arg1, String arg2) {
		// ignore
	}

	public void start(Runnable r) {
		if (!mStarted) {
			mStarted = true;
			mTmp = r;
			TencentLocationRequest request = TencentLocationRequest.create()
					.setInterval(5000)
					.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO);
			TencentLocationManager.getInstance(mContext)
					.requestLocationUpdates(request, this);
		}
	}

	public void stop() {
		if (mStarted) {
			TencentLocationManager.getInstance(mContext).removeUpdates(this);
			mStarted = false;
			mTmp = null;
		}
	}

	public boolean isStarted() {
		return mStarted;
	}

	public TencentLocation getLastLocation() {
		if (mLastError == 0) {
			return mLastLocation;
		}
		return null;
	}
}

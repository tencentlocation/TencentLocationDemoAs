package com.tencent.example.location.fence;

import android.location.Location;
import android.util.Log;

import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.tencentmap.mapsdk.map.GeoPoint;

public class Utils {
	public static GeoPoint of(TencentLocation location) {
		GeoPoint ge = new GeoPoint((int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
		return ge;
	}

	public static GeoPoint of(double latitude, double longitude) {
		GeoPoint ge = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));
		return ge;
	}

	public static void d(String tag, String msg) {
		Log.i(tag, msg);
	}

	public static double fmt(double d) {
		long i = (long) (d * 1e6);
		return i / 1e6;
	}

	public static String toString(TencentGeofence geofence) {
		return geofence.getTag() + " " + geofence.getLatitude() + ","
				+ geofence.getLongitude();
	}

	public static String toString(Location location) {
		return location.getLatitude() + "," + location.getLongitude();
	}
}

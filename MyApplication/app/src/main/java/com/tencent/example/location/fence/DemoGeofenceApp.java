package com.tencent.example.location.fence;

import java.util.ArrayList;

import android.app.Application;

import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.tencentmap.mapsdk.search.PoiItem;

/**
 * 继承 Application, 用于在全局范围内保存 TencentGeofence.
 * 
 * <p>
 * 注意, TencentGeofenceManager 不会将将当前的 TencentGeofence 持久化保存. 应用程序需要根据实际需求,
 * 使用数据库, 文件, SharedPreference 或 网络等方式自行保存 TencentGeofence.
 * 
 */
public class DemoGeofenceApp extends Application {

	/**
	 * 用于底图上显示已添加的 TencentGeofence
	 */
	private static ArrayList<PoiItem> sFenceItems = new ArrayList<PoiItem>();

	/**
	 * 用于在 ListView 中显示已添加的 TencentGeofence
	 */
	private static ArrayList<TencentGeofence> sFences = new ArrayList<TencentGeofence>();

	/**
	 * 记录已触发的 TencentGeofence 事件
	 */
	private static ArrayList<String> sEvents = new ArrayList<String>();

	@Override
	public void onCreate() {
		super.onCreate();
//		TencentExtraKeys.setTencentLog(new TencentLog() {
//
//			@Override
//			public void println(String arg0, int arg1, String arg2) {
//				Log.i(arg0, arg2);
//			}
//
//		});
	}

	public static ArrayList<PoiItem> getFenceItems() {
		return sFenceItems;
	}

	public static ArrayList<TencentGeofence> getFence() {
		return sFences;
	}

	/**
	 * 返回最新添加的围栏
	 */
	public static TencentGeofence getLastFence() {
		if (sFences.isEmpty()) {
			return null;
		}
		return sFences.get(sFences.size() - 1);
	}

	public static ArrayList<String> getEvents() {
		return sEvents;
	}
}

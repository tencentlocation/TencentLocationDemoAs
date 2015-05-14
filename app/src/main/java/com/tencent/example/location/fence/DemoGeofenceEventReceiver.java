package com.tencent.example.location.fence;

import java.util.Date;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.tencent.example.location.R;

/**
 * receiver, 处理触发的地理围栏事件.
 */
public class DemoGeofenceEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null
				|| !DemoGeofenceService.ACTION_TRIGGER_GEOFENCE.equals(intent
						.getAction())) {
			return;
		}

		// Tag
		// String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
		// 围栏已触发, 可根据需要决定是否将其删除
		// TODO 注意, 这里仅通知TencentGeofenceManager删除围栏, 但并没有同步删除 UI 上的相应元素
		// 移除地理围栏
		// DemoGeofenceService.startMe(context,
		//		DemoGeofenceService.ACTION_DEL_GEOFENCE, tag);

		NotificationManager notiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.notify(new Random().nextInt(),
				createNotification(context, intent));

		DemoGeofenceApp.getEvents().add(
				DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date()) + " "
						+ toString(intent));
	}

	private Notification createNotification(Context context, Intent intent) {
		// Tag
		String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
		// 进入围栏还是退出围栏
		boolean enter = intent.getBooleanExtra(
				LocationManager.KEY_PROXIMITY_ENTERING, true);
		// 其他自定义的 extra 字段
		double lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
		double lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("围栏事件通知");
		builder.setContentText(toString(enter, tag, lat, lng));
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setVibrate(new long[] { 0, 200, 100, 200, 100, 200 });
		builder.setContentIntent(createPendingIntent(context));
		Notification noti = builder.build();
		noti.defaults = Notification.DEFAULT_ALL;
		return noti;
	}

	private PendingIntent createPendingIntent(Context context) {
		Intent intent = new Intent(context, DemoGeofenceEventActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static String toString(boolean enter, String tag, double lat,
			double lng) {
		if (enter) {
			return "已进入 " + tag + ",(" + lat + "," + lng + ")";
		} else {
			return "已退出 " + tag + ",(" + lat + "," + lng + ")";
		}
	}

	private static String toString(Intent intent) {
		// Tag
		String tag = intent.getStringExtra("KEY_GEOFENCE_ID");
		// 进入围栏还是退出围栏
		boolean enter = intent.getBooleanExtra(
				LocationManager.KEY_PROXIMITY_ENTERING, true);
		// 其他自定义的 extra 字段
		double lat = intent.getDoubleExtra("KEY_GEOFENCE_LAT", 0);
		double lng = intent.getDoubleExtra("KEY_GEOFENCE_LNG", 0);
		return toString(enter, tag, lat, lng);
	}
}

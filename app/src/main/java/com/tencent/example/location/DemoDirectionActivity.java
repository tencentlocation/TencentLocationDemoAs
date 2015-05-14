package com.tencent.example.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public class DemoDirectionActivity extends Activity implements
		TencentLocationListener {

	private TencentLocationManager mLocationManager;
	private TextView mStatus;
	private DirectionView mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction);
		mStatus = (TextView) findViewById(R.id.status);
		mView = (DirectionView) findViewById(R.id.dir);

		mLocationManager = TencentLocationManager.getInstance(this);
		mLocationManager.requestLocationUpdates(TencentLocationRequest.create()
				.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
				.setInterval(500).setAllowDirection(true), this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		if (error == TencentLocation.ERROR_OK) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double direction = location.getExtra().getDouble(TencentLocation.EXTRA_DIRECTION);
			// System.out.println(latitude + " " + longitude + " " + dirction);
			mStatus.setText("纬度=" + latitude + ",经度=" + longitude + "\n方向=" + (int)direction);
			mView.updateDirection(direction);
		} else {
			mView.updateDirection(0);
		}
	}

	@Override
	public void onStatusUpdate(String name, int status, String desc) {
		// ignore
	}

	public static class DirectionView extends View {
		private static final float OFFSET = 0f;

		private Handler mHandler;

		private Paint mPaint;
		private Paint mPen;
		private double mDir;
		private Bitmap mBmp;

		private int mBmpW;
		private int mBmpH;

		public void updateDirection(double direction) {
			if (!Double.isNaN(direction)) {
				mDir = direction;
				invalidate();
			}
		}

		public DirectionView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}



		public DirectionView(final Context context) {
			super(context);
			init(context);
		}

		@SuppressLint("HandlerLeak")
		private void init(final Context context) {
			// onLocationChanged() 仅在"位置"发生变化时回调
			// 要实现更流畅的指南针效果, 可通过定时消息, 加快获取"方向"
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					TencentLocation location = TencentLocationManager.getInstance(context).getLastKnownLocation();
					if (location != null) {
						double direction = location.getExtra().getDouble(TencentLocation.EXTRA_DIRECTION);
						updateDirection(direction);
					}
				}
			};

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setStyle(Style.STROKE);
			mPaint.setStrokeWidth(2f);
			mPaint.setColor(Color.RED);

			mPen = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPen.setTextSize(getRawSize(TypedValue.COMPLEX_UNIT_SP, 20));
			mPen.setColor(Color.BLACK);

			mBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pointer);
			mBmpW = mBmp.getWidth() / 2;
			mBmpH = mBmp.getHeight() / 2;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			int w = getMeasuredWidth();
			int h = getMeasuredHeight();

			int r = w < h ? w / 2 : h / 2;
			r -= 30;
			canvas.drawCircle(w / 2, h / 2, r, mPaint);

			canvas.drawText("北", w / 2, h / 2 - r + OFFSET, mPen);
			canvas.drawText("南", w / 2, h / 2 + r - OFFSET, mPen);
			canvas.drawText("西", w / 2 - r + OFFSET, h / 2, mPen);
			canvas.drawText("东", w / 2 + r - 30, h / 2, mPen);

			canvas.save();
			// 由于方向图标箭头向左, 有必要调整到向北
			canvas.rotate(-90, w / 2, h / 2);

			// 根据定位SDK获得的方向旋转箭头
			canvas.rotate((float) mDir, w / 2, h / 2);

			canvas.translate(w / 2 - mBmpW, h / 2 - mBmpH);
			canvas.drawBitmap(mBmp, 0, 0, null);
			canvas.restore();

			mHandler.sendEmptyMessageDelayed(0, 50);
		}

		@Override
		protected void onDetachedFromWindow() {
			super.onDetachedFromWindow();
			mHandler.removeCallbacksAndMessages(null);
		}

		private float getRawSize(int unit, float size) {
		       Context c = getContext();
		       Resources r;

		       if (c == null)
		           r = Resources.getSystem();
		       else
		           r = c.getResources();

		       return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
		}

	}
}

package com.tencent.example.location.fence;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.example.location.R;
import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.tencentmap.mapsdk.map.GeoPoint;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.PoiOverlay;
import com.tencent.tencentmap.mapsdk.search.PoiItem;

/**
 * 添加和删除 TencentGeofence, 添加或删除后 DemoGeofenceService 将开始运行围栏测试.
 *
 * <p>
 * 添加步骤:
 * <ol>
 * <li>拖动底图到目标位置(左下角红色坐标会随之改变)
 * <li>点击"添加"按钮, 输入名字并保存
 * </ol>
 *
 * 添加时 名字(Tag) 不能重复且不能为空. 如果添加的时当前位置附近的位置, 可先点击"定位"按钮
 *
 * <p>
 * 删除步骤:
 * <ol>
 * <li>点击 List 选中要删除的 TencentGeofence
 * <li>点击"删除"按钮
 * </ol>
 *
 */
public class DemoGeofenceEditorActivty extends Activity implements
		OnTouchListener {
	private LocationHelper mLocationHelper;

	private MapView mMapView;
	private TextView mPosition;
	private ListView mFenceList;

	private ArrayAdapter<TencentGeofence> mFenceListAdapter;

	private PoiOverlay mFenceOverlay;
	private List<PoiItem> mFenceItems;

	private final Location mCenter = new Location("");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geofence_editor);
		mLocationHelper = new LocationHelper(this);
		initUi();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onStop() {
		mMapView.onStop();
		super.onStop();
	}

	// ============== ui handler & listener
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.my_loc: // 获取当前位置
			doMyLoc();
			break;

		case R.id.add: // 添加新的围栏
			doPreAdd();
			break;
		case R.id.del: // 删除选中的围栏
			int selected = mFenceList.getCheckedItemPosition();
			doDel(selected);
			break;

		case R.id.stop:
			DemoGeofenceService.stopMe(this); // 停止测试
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			updatePosition();
		}
		return false;
	}

	// ============== ui handler & listener

	// ============== do methods
	private void doMyLoc() {
		if (mLocationHelper.getLastLocation() != null) {
			animateTo(mLocationHelper.getLastLocation()); // 已有最新位置
		} else if (mLocationHelper.isStarted()) {
			toast(this, "正在定位"); // 当前正在定位
		} else {
			toast(this, "开始定位");
			mLocationHelper.start(new Runnable() {
				public void run() {
					animateTo(mLocationHelper.getLastLocation());
				}
			});
		}
	}

	private void doPreAdd() {
		View root = getLayoutInflater().inflate(R.layout.dialog_geofence, null);
		TextView tvLocation = (TextView) root.findViewById(R.id.location);
		tvLocation.setText(Utils.toString(mCenter));

		new AlertDialog.Builder(this).setTitle("保存围栏").setView(root)
				.setPositiveButton("确定", new AddGeofenceOnClickListener(root))
				.setNegativeButton("取消", null).show();
	}

	private void doAdd(String tag) {
		toast(this, tag);

		double lat = mCenter.getLatitude();
		double lng = mCenter.getLongitude();
		// 创建地理围栏
		TencentGeofence.Builder builder = new TencentGeofence.Builder();
		TencentGeofence geofence = builder.setTag(tag) // 设置 Tag
				.setCircularRegion(lat, lng, 500) // 设置中心点和半径
				.setExpirationDuration(3 * 3600 * 1000) // 设置有效期
				.build();
		// 更新 adapter view
		mFenceListAdapter.add(geofence);

		// 更新 overlay
		mFenceItems.add(createPoiItem(geofence));
		mFenceOverlay.setPoiItems(mFenceItems);

		// 添加地理围栏
		DemoGeofenceService.startMe(this,
				DemoGeofenceService.ACTION_ADD_GEOFENCE, tag);
	}

	private void doDel(int selected) {
		if (selected == ListView.INVALID_POSITION || selected >= mFenceListAdapter.getCount()) {
			toast(this, "没有选中");
			return;
		}

		// 更新 adapter view
		TencentGeofence item = mFenceListAdapter.getItem(selected);
		mFenceListAdapter.remove(item);
		// 更新 overlay
		mFenceItems.remove(selected);
		mFenceOverlay.setPoiItems(mFenceItems);

		// 移除地理围栏
		DemoGeofenceService.startMe(this,
				DemoGeofenceService.ACTION_DEL_GEOFENCE, item.getTag());
	}

	// ============== do methods

	// ============== util methods
	private void initUi() {
		// poi item & poi overlay
		mFenceItems = DemoGeofenceApp.getFenceItems();
		mFenceOverlay = new PoiOverlay(getResources().getDrawable(
				R.drawable.sendtocar_balloon));
		mFenceOverlay.setPoiItems(mFenceItems);

		// mapview
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.addOverlay(mFenceOverlay);
		mMapView.setOnTouchListener(this);

		// list & adapter
		mPosition = (TextView) findViewById(R.id.position);
		mFenceList = (ListView) findViewById(R.id.geofence_list);

		mFenceListAdapter = new ArrayAdapter<TencentGeofence>(this,
				android.R.layout.simple_list_item_checked,
				DemoGeofenceApp.getFence()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TencentGeofence geofence = getItem(position);
				TextView tv = (TextView) super.getView(position, convertView,
						parent);
				tv.setText(Utils.toString(geofence));
				return tv;
			}
		};
		mFenceList.setAdapter(mFenceListAdapter);
		mFenceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		updatePosition();
	}

	private void updatePosition() {
		GeoPoint c = mMapView.getMapCenter();
		double lat = c.getLatitudeE6() / 1E6;
		double lng = c.getLongitudeE6() / 1E6;

		mPosition.setText(lat + "," + lng);
		mCenter.setLatitude(lat);
		mCenter.setLongitude(lng);
	}

	private void animateTo(TencentLocation location) {
		if (location == null) {
			return;
		}
		mMapView.getController().animateTo(Utils.of(location));
		// 修改 mapview 中心点
		mMapView.getController().setCenter(Utils.of(location));
		// 注意一定要更新当前位置 mCenter
		updatePosition();
	}

	// 生成 poi item
	private PoiItem createPoiItem(TencentGeofence geofence) {
		PoiItem item = new PoiItem();
		item.point = Utils.of(geofence.getLatitude(), geofence.getLongitude());
		item.name = geofence.getTag();
		item.address = Utils.fmt(geofence.getLatitude()) + ","
				+ Utils.fmt(geofence.getLongitude()) + ","
				+ geofence.getRadius();
		return item;
	}

	// ============== util methods
	
	static void toast(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	class AddGeofenceOnClickListener implements OnClickListener {

		private View mView;

		public AddGeofenceOnClickListener(View view) {
			super();
			this.mView = view;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (isFinishing()) {
				return;
			}

			EditText etName = (EditText) mView.findViewById(R.id.name);
			String name = etName.getText().toString();

			if (!TextUtils.isEmpty(name)) {
				doAdd(name);
			} else {
				toast(DemoGeofenceEditorActivty.this, "围栏名字不能为空");
			}
		}
	}

}

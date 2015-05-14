package com.tencent.example.location.fence;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/*
 * 显示已发生的 TencentGeofence 事件.
 */
public class DemoGeofenceEventActivity extends ListActivity {

	private List<String> mEvents;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEvents = new ArrayList<String>();
		getListView().setAdapter(
				new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, mEvents));
	}

	@Override
	protected void onStart() {
		super.onStart();
		mEvents.clear();
		mEvents.addAll(DemoGeofenceApp.getEvents());
	}

}

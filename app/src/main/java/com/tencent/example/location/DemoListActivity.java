package com.tencent.example.location;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DemoListActivity extends ListActivity implements OnClickListener {
	private static final String DEMOS_JSON_FILE = "demos.json";

	private static final String TAG = "DemoListActivity";

	private static final String LABEL = "label";

	private static final String NAME = "name";

	private List<DemoEntry> mDemos = new ArrayList<DemoEntry>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 加载 demo 列表
		loadDemos(DEMOS_JSON_FILE);
		getListView().setAdapter(
				new ArrayAdapter<DemoEntry>(this,
						android.R.layout.simple_list_item_1,
						android.R.id.text1, mDemos));
		String key = DemoUtils.getKey(this);

		// 检查 key 的结构
		if (TextUtils.isEmpty(key)
				|| !Pattern.matches("\\w{5}(-\\w{5}){5}", key)) {
			new AlertDialog.Builder(this).setTitle("错误的key")
					.setMessage("运行前请在manifest中设置正确的key")
					.setPositiveButton("确定", this).show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		finish();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// 启动目标 demo
		try {
			startActivity(mDemos.get(position).getIntent(this));
		} catch (Exception e) {
			Log.e(TAG, "无法启动 " + mDemos.get(position).name);
		}
	}

	private void loadDemos(String jsonFile) {
		byte[] data = new byte[1024 * 4];
		String content = null;
		try {
			InputStream in = getAssets().open(jsonFile);
			int len = in.read(data);
			in.close();

			content = new String(data, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (content != null) {
			try {
				JSONArray demoArray = new JSONArray(content);
				int size = demoArray.length();
				for (int i = 0; i < size; i++) {
					JSONObject demoObj = demoArray.getJSONObject(i);
					mDemos.add(new DemoEntry(demoObj.optString(NAME), demoObj
							.optString(LABEL)));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		for (DemoEntry e : mDemos) {
			System.out.println(e.getIntent(this));
		}
	}

	static class DemoEntry {
		final String name;
		final String label;

		public DemoEntry(String name, String label) {
			super();
			this.name = name;
			this.label = label;
		}

		Intent getIntent(Context context) {
			try {
				String className = name;
				System.out.println(className);
				return new Intent(context, Class.forName(className));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public String toString() {
			return label;
		}
	}
}

package biz.bokhorst.xprivacy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import static de.robv.android.xposed.XposedHelpers.findField;

public class XInstalledAppDetails extends XHook {

	public XInstalledAppDetails(String methodName, String permissionName) {
		super(methodName, permissionName);
	}

	@Override
	protected void before(MethodHookParam param) throws Throwable {
		// Do nothing
	}

	private class PermissionsAdapter extends ArrayAdapter<String> {
		private List<String> mPermissions;
		ApplicationInfo mAppInfo;

		public PermissionsAdapter(Context context, int resource, List<String> objects, ApplicationInfo appInfo) {
			super(context, resource, objects);
			this.mPermissions = objects;
			mAppInfo = appInfo;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get view info
			String permissionName = mPermissions.get(position);
			TextView textView = (TextView) super.getView(position, convertView, parent);

			// Get resources
			PackageManager pm = textView.getContext().getPackageManager();
			String packageName = this.getClass().getPackage().getName();
			Resources resources;
			try {
				resources = pm.getResourcesForApplication(packageName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return textView;
			}

			// Localize text
			int stringId = resources.getIdentifier("perm_" + permissionName, "string", packageName);
			if (stringId != 0)
				textView.setText(resources.getString(stringId));

			// Check if permission granted
			String[] aPermissions = XPrivacy.cPermissions.get(permissionName);
			boolean permissionGranted = (aPermissions.length == 0);
			for (String aPermission : aPermissions)
				if (pm.checkPermission("android.permission." + aPermission, mAppInfo.packageName) == PackageManager.PERMISSION_GRANTED) {
					permissionGranted = true;
					break;
				}
			if (!permissionGranted)
				textView.setTextColor(Color.GRAY);

			// Check last usage
			ContentResolver cr = textView.getContext().getContentResolver();
			Cursor cursor = cr.query(XPrivacyProvider.URI_LASTUSED, null, permissionName,
					new String[] { Integer.toString(mAppInfo.uid) }, null);
			if (cursor.moveToNext()) {
				long lastUsage = cursor.getLong(cursor.getColumnIndex(XPrivacyProvider.COL_LASTUSED));
				cursor.close();
				if (lastUsage != 0)
					textView.setTypeface(null, Typeface.BOLD_ITALIC);
			}

			return textView;
		}
	}

	@Override
	protected void after(MethodHookParam param) throws Throwable {
		// TODO: usage statistics
		super.after(param);

		// Get app entry
		Field fieldAppEntry = findField(param.thisObject.getClass(), "mAppEntry");
		Object appEntry = fieldAppEntry.get(param.thisObject);
		if (appEntry == null) {
			warning("appEntry is null");
			return;
		}

		// Get app info
		Field fieldInfo = findField(appEntry.getClass(), "info");
		final ApplicationInfo appInfo = (ApplicationInfo) fieldInfo.get(appEntry);
		if (appInfo == null) {
			warning("appInfo is null");
			return;
		}

		// Get root view
		Field fieldRootView = findField(param.thisObject.getClass(), "mRootView");
		View rootView = (View) fieldRootView.get(param.thisObject);
		if (rootView == null) {
			warning("rootView is null");
			return;
		}

		// Get first child
		final LinearLayout detailsView = (LinearLayout) ((ViewGroup) rootView).getChildAt(0);
		if (detailsView == null) {
			warning("detailsView is null");
			return;
		}

		// Get package manager / resources
		String xPackageName = this.getClass().getPackage().getName();
		PackageManager pm = detailsView.getContext().getPackageManager();
		Resources resources = pm.getResourcesForApplication(xPackageName);

		// Add privacy view
		LinearLayout privacyView = (LinearLayout) detailsView.findViewById(R.id.vwAppSettings);
		if (privacyView != null)
			detailsView.removeView(privacyView);

		// Load privacy view
		Context xContext = detailsView.getContext().createPackageContext(xPackageName, 0);
		LayoutInflater inflater = (LayoutInflater) xContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		privacyView = (LinearLayout) inflater.inflate(resources.getLayout(R.layout.xsettings), null);

		// Check if internet access
		if (pm.checkPermission("android.permission.INTERNET", appInfo.packageName) == PackageManager.PERMISSION_GRANTED)
			privacyView.removeView(privacyView.findViewById(R.id.tvInternet));

		// Fill privacy list view adapter
		ListView privacyListView = (ListView) privacyView.findViewById(R.id.lvPrivacy);
		List<String> permissionsList = new ArrayList<String>(XPrivacy.cPermissions.keySet());
		PermissionsAdapter privacyListAdapter = new PermissionsAdapter(privacyView.getContext(),
				android.R.layout.simple_list_item_multiple_choice, permissionsList, appInfo);
		privacyListView.setAdapter(privacyListAdapter);
		XUtil.setListViewHeightBasedOnChildren(privacyListView);

		// Set privacy values
		for (int position = 0; position < privacyListView.getAdapter().getCount(); position++) {
			String permissionName = (String) privacyListView.getItemAtPosition(position);
			privacyListView.setItemChecked(position,
					isAllowed(detailsView.getContext(), appInfo.uid, permissionName, false));
		}

		// Listen for privacy changes
		privacyListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView privacyListView = (ListView) parent;
				String permissionName = (String) privacyListView.getItemAtPosition(position);
				boolean allowed = privacyListView.isItemChecked(position);
				setAllowed(detailsView.getContext(), appInfo.uid, permissionName, allowed);
			}
		});

		// Display privacy view
		detailsView.addView(privacyView);
		detailsView.invalidate();
	}
}
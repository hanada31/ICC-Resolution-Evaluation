package net.osmand.plus.mapcontextmenu.other;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.widget.Toast;

import net.osmand.data.LatLon;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.actions.ShareDialog;
import net.osmand.plus.mapcontextmenu.BaseMenuController;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ShareMenu extends BaseMenuController {

	private LatLon latLon;
	private String title;
	private String address;

	private static final String KEY_SHARE_MENU_LATLON = "key_share_menu_latlon";
	private static final String KEY_SHARE_MENU_POINT_TITLE = "key_share_menu_point_title";

	public enum ShareItem {
		MESSAGE(R.drawable.ic_action_message, R.string.shared_string_send),
		CLIPBOARD(R.drawable.ic_action_copy, R.string.shared_string_copy),
		NAME(R.drawable.ic_action_copy, R.string.copy_location_name),
		COORDINATES(R.drawable.ic_action_copy, R.string.copy_coordinates),
		GEO(R.drawable.ic_world_globe_dark, R.string.share_geo),
		QR_CODE(R.drawable.ic_action_qrcode, R.string.shared_string_qr_code);

		final int iconResourceId;
		final int titleResourceId;

		ShareItem(int iconResourceId, int titleResourceId) {
			this.iconResourceId = iconResourceId;
			this.titleResourceId = titleResourceId;
		}

		public int getIconResourceId() {
			return iconResourceId;
		}

		public int getTitleResourceId() {
			return titleResourceId;
		}
	}

	private ShareMenu(MapActivity mapActivity) {
		super(mapActivity);
	}

	public List<ShareItem> getItems() {
		List<ShareItem> list = new LinkedList<>();
		list.add(ShareItem.MESSAGE);
		list.add(ShareItem.CLIPBOARD);
		list.add(ShareItem.NAME);
		list.add(ShareItem.COORDINATES);
		list.add(ShareItem.GEO);
		list.add(ShareItem.QR_CODE);
		return list;
	}

	public LatLon getLatLon() {
		return latLon;
	}

	public String getTitle() {
		return title;
	}

	public static void show(LatLon latLon, String title, String address, @NonNull MapActivity mapActivity) {

		ShareMenu menu = new ShareMenu(mapActivity);

		menu.latLon = latLon;
		menu.title = title;
		menu.address = address;

		ShareMenuFragment.showInstance(menu);
	}

	public void share(ShareItem item) {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity == null) {
			return;
		}
		final int zoom = mapActivity.getMapView().getZoom();
		final String geoUrl = MapUtils.buildGeoUrl(latLon.getLatitude(), latLon.getLongitude(), zoom);
		final String httpUrl = "https://osmand.net/go?lat=" + ((float) latLon.getLatitude())
				+ "&lon=" + ((float) latLon.getLongitude()) + "&z=" + zoom;
		StringBuilder sb = new StringBuilder();
		if (!Algorithms.isEmpty(title)) {
			sb.append(title).append("\n");
		}
		if (!Algorithms.isEmpty(address) && !address.equals(title) && !address.equals(mapActivity.getString(R.string.no_address_found))) {
			sb.append(address).append("\n");
		}
		sb.append(mapActivity.getString(R.string.shared_string_location)).append(": ");
		if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
			sb.append("\n");
		}
		sb.append(geoUrl).append("\n").append(httpUrl);
		String sms = sb.toString();
		switch (item) {
			case MESSAGE:
				ShareDialog.sendMessage(mapActivity, sms);
				break;
			case CLIPBOARD:
				ShareDialog.sendToClipboard(mapActivity, sms);
				break;
			case NAME:
				if (!Algorithms.isEmpty(title)) {
					ShareDialog.sendToClipboard(mapActivity, title);
				} else {
					Toast.makeText(mapActivity,
							R.string.toast_empty_name_error,
							Toast.LENGTH_LONG).show();
				}
				break;
			case COORDINATES:
				OsmandSettings st = ((OsmandApplication) mapActivity.getApplicationContext()).getSettings();
				int f = st.COORDINATES_FORMAT.get();
				ShareDialog.sendToClipboard(mapActivity,
						OsmAndFormatter.getFormattedCoordinates(latLon.getLatitude(), latLon.getLongitude(), f));
				break;
			case GEO:
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUrl));
				mapActivity.startActivity(mapIntent);
				break;
			case QR_CODE:
				Bundle bundle = new Bundle();
				bundle.putFloat("LAT", (float) latLon.getLatitude());
				bundle.putFloat("LONG", (float) latLon.getLongitude());
				ShareDialog.sendQRCode(mapActivity, "LOCATION_TYPE", bundle, null);
				break;
		}
	}

	public void saveMenu(Bundle bundle) {
		bundle.putSerializable(KEY_SHARE_MENU_LATLON, latLon);
		bundle.putString(KEY_SHARE_MENU_POINT_TITLE, title);
	}

	public static ShareMenu restoreMenu(Bundle bundle, MapActivity mapActivity) {

		ShareMenu menu = new ShareMenu(mapActivity);

		menu.title = bundle.getString(KEY_SHARE_MENU_POINT_TITLE);
		Object latLonObj = bundle.getSerializable(KEY_SHARE_MENU_LATLON);
		if (latLonObj != null) {
			menu.latLon = (LatLon) latLonObj;
		}

		return menu;
	}

}

package net.osmand.plus.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.Location;
import net.osmand.TspAnt;
import net.osmand.data.FavouritePoint;
import net.osmand.data.LatLon;
import net.osmand.data.LocationPoint;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.SimpleBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerHalfItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.helpers.WaypointHelper.AmenityLocationPoint;
import net.osmand.plus.helpers.WaypointHelper.LocationPointWrapper;
import net.osmand.plus.routepreparationmenu.AddPointBottomSheetDialog;
import net.osmand.plus.routepreparationmenu.MapRouteInfoMenu;
import net.osmand.plus.views.controls.StableArrayAdapter;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class WaypointDialogHelper {
	private MapActivity mapActivity;
	private OsmandApplication app;
	private WaypointHelper waypointHelper;
	private List<WaypointDialogHelperCallback> helperCallbacks= new ArrayList<>();

	private boolean flat;
	private List<LocationPointWrapper> deletedPoints;

	public interface WaypointDialogHelperCallback {
		void reloadAdapter();

		void deleteWaypoint(int position);
	}

	public void addHelperCallback(WaypointDialogHelperCallback callback) {
		helperCallbacks.add(callback);
	}

	public void removeHelperCallback(WaypointDialogHelperCallback callback) {
		helperCallbacks.remove(callback);
	}

	public WaypointDialogHelper(MapActivity mapActivity) {
		this.app = mapActivity.getMyApplication();
		waypointHelper = this.app.getWaypointHelper();
		this.mapActivity = mapActivity;
	}

	public static void updatePointInfoView(final OsmandApplication app, final Activity activity,
										   View localView, final LocationPointWrapper ps,
										   final boolean mapCenter, final boolean nightMode,
										   final boolean edit, final boolean topBar) {
		WaypointHelper wh = app.getWaypointHelper();
		final LocationPoint point = ps.getPoint();
		TextView text = (TextView) localView.findViewById(R.id.waypoint_text);
		if (!topBar) {
			AndroidUtils.setTextPrimaryColor(activity, text, nightMode);
		}
		TextView textShadow = (TextView) localView.findViewById(R.id.waypoint_text_shadow);
		if (!edit) {
			localView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					showOnMap(app, activity, point, mapCenter);
				}
			});
		}
		TextView textDist = (TextView) localView.findViewById(R.id.waypoint_dist);
		((ImageView) localView.findViewById(R.id.waypoint_icon)).setImageDrawable(ps.getDrawable(activity, app, nightMode));
		int dist = -1;
		boolean startPoint = ps.type == WaypointHelper.TARGETS && ((TargetPoint) ps.point).start;
		if (!startPoint) {
			if (!wh.isRouteCalculated()) {
				if (activity instanceof MapActivity) {
					dist = (int) MapUtils.getDistance(((MapActivity) activity).getMapView().getLatitude(), ((MapActivity) activity)
							.getMapView().getLongitude(), point.getLatitude(), point.getLongitude());
				}
			} else {
				dist = wh.getRouteDistance(ps);
			}
		}

		if (dist > 0) {
			textDist.setText(OsmAndFormatter.getFormattedDistance(dist, app));
		} else {
			textDist.setText("");
		}

		TextView textDeviation = (TextView) localView.findViewById(R.id.waypoint_deviation);
		if (textDeviation != null) {
			if (dist > 0 && ps.deviationDistance > 0) {
				String devStr = "+" + OsmAndFormatter.getFormattedDistance(ps.deviationDistance, app);
				textDeviation.setText(devStr);
				if (!topBar) {
					int colorId = nightMode ? R.color.text_color_secondary_dark : R.color.text_color_secondary_light;
					AndroidUtils.setTextSecondaryColor(activity, textDeviation, nightMode);
					if (ps.deviationDirectionRight) {
						textDeviation.setCompoundDrawablesWithIntrinsicBounds(
								app.getUIUtilities().getIcon(R.drawable.ic_small_turn_right, colorId),
								null, null, null);
					} else {
						textDeviation.setCompoundDrawablesWithIntrinsicBounds(
								app.getUIUtilities().getIcon(R.drawable.ic_small_turn_left, colorId),
								null, null, null);
					}
				}
				textDeviation.setVisibility(View.VISIBLE);
			} else {
				textDeviation.setText("");
				textDeviation.setVisibility(View.GONE);
			}
		}

		String descr;
		PointDescription pd = point.getPointDescription(app);
		if (Algorithms.isEmpty(pd.getName())) {
			descr = pd.getTypeName();
		} else {
			descr = pd.getName();
		}

		if (textShadow != null) {
			textShadow.setText(descr);
		}
		text.setText(descr);

		String pointDescription = "";
		TextView descText = (TextView) localView.findViewById(R.id.waypoint_desc_text);
		if (descText != null) {
			AndroidUtils.setTextSecondaryColor(activity, descText, nightMode);
			switch (ps.type) {
				case WaypointHelper.TARGETS:
					TargetPoint targetPoint = (TargetPoint) ps.point;
					if (targetPoint.start) {
						pointDescription = activity.getResources().getString(R.string.starting_point);
					} else {
						pointDescription = targetPoint.getPointDescription(activity).getTypeName();
					}
					break;

				case WaypointHelper.FAVORITES:
					FavouritePoint favPoint = (FavouritePoint) ps.point;
					pointDescription = Algorithms.isEmpty(favPoint.getCategory()) ? activity.getResources().getString(R.string.shared_string_favorites) : favPoint.getCategory();
					break;
			}
		}

		if (Algorithms.objectEquals(descr, pointDescription)) {
			pointDescription = "";
		}
		if (dist > 0 && !Algorithms.isEmpty(pointDescription)) {
			pointDescription = "  •  " + pointDescription;
		}
		if (descText != null) {
			descText.setText(pointDescription);
		}
	}

	public List<Object> getTargetPoints() {
		final List<Object> points = new ArrayList<>();
		for (int i = 0; i < WaypointHelper.MAX; i++) {
			List<LocationPointWrapper> tp = waypointHelper.getWaypoints(i);
			if ((i == WaypointHelper.WAYPOINTS || i == WaypointHelper.TARGETS) && waypointHelper.isTypeVisible(i)) {
				if (i == WaypointHelper.TARGETS) {
					TargetPoint start = app.getTargetPointsHelper().getPointToStart();
					if (start == null) {
						LatLon latLon;
						Location loc = app.getLocationProvider().getLastKnownLocation();
						if (loc != null) {
							latLon = new LatLon(loc.getLatitude(), loc.getLongitude());
						} else {
							latLon = new LatLon(mapActivity.getMapView().getLatitude(),
									mapActivity.getMapView().getLongitude());
						}
						start = TargetPoint.createStartPoint(latLon,
								new PointDescription(PointDescription.POINT_TYPE_MY_LOCATION,
										mapActivity.getString(R.string.shared_string_my_location)));
					} else {
						String oname = start.getOnlyName().length() > 0 ? start.getOnlyName()
								: (mapActivity.getString(R.string.route_descr_map_location)
								+ " " + mapActivity.getString(R.string.route_descr_lat_lon, start.getLatitude(), start.getLongitude()));

						start = TargetPoint.createStartPoint(new LatLon(start.getLatitude(), start.getLongitude()),
								new PointDescription(PointDescription.POINT_TYPE_LOCATION,
										oname));
					}
					points.add(new LocationPointWrapper(null, WaypointHelper.TARGETS, start, 0f, 0));

				}
				if (tp != null && tp.size() > 0) {
					points.addAll(tp);
				}
			}
		}
		return points;
	}

	public List<Object> getActivePoints(List<Object> points) {
		List<Object> activePoints = new ArrayList<>();
		for (Object p : points) {
			if (p instanceof LocationPointWrapper) {
				LocationPointWrapper w = (LocationPointWrapper) p;
				if (w.type == WaypointHelper.TARGETS) {
					activePoints.add(p);
				}
			}
		}
		return activePoints;
	}

	private static void updateRouteInfoMenu(Activity ctx) {
		if (ctx instanceof MapActivity) {
			((MapActivity) ctx).getMapRouteInfoMenu().updateMenu();
		}
	}

	// switch start & finish
	public static void switchStartAndFinish(TargetPointsHelper targetPointsHelper, TargetPoint finish,
											 Activity ctx, TargetPoint start, OsmandApplication app,
											 WaypointDialogHelper helper) {
		if (finish == null) {
			app.showShortToastMessage(R.string.mark_final_location_first);
		} else {
			targetPointsHelper.setStartPoint(new LatLon(finish.getLatitude(),
					finish.getLongitude()), false, finish.getPointDescription(ctx));
			if (start == null) {
				Location loc = app.getLocationProvider().getLastKnownLocation();
				if (loc != null) {
					targetPointsHelper.navigateToPoint(new LatLon(loc.getLatitude(),
							loc.getLongitude()), true, -1);
				}
			} else {
				targetPointsHelper.navigateToPoint(new LatLon(start.getLatitude(),
						start.getLongitude()), true, -1, start.getPointDescription(ctx));
			}
			updateControls(ctx, helper);
		}
	}

	public static void updateControls(Activity ctx, WaypointDialogHelper helper) {
		if (helper != null && helper.helperCallbacks != null) {
			for (WaypointDialogHelperCallback callback : helper.helperCallbacks) {
				callback.reloadAdapter();
			}
		}
		updateRouteInfoMenu(ctx);
	}

	private static void clearAllIntermediatePoints(Activity ctx, TargetPointsHelper targetPointsHelper, WaypointDialogHelper helper) {
		targetPointsHelper.clearAllIntermediatePoints(true);
		updateControls(ctx, helper);
	}

	public static void replaceStartWithFirstIntermediate(TargetPointsHelper targetPointsHelper, Activity ctx,
														  WaypointDialogHelper helper) {
		List<TargetPoint> intermediatePoints = targetPointsHelper.getIntermediatePointsWithTarget();
		TargetPoint firstIntermediate = intermediatePoints.remove(0);
		targetPointsHelper.setStartPoint(new LatLon(firstIntermediate.getLatitude(),
				firstIntermediate.getLongitude()), false, firstIntermediate.getPointDescription(ctx));
		targetPointsHelper.reorderAllTargetPoints(intermediatePoints, true);

		updateControls(ctx, helper);
	}

	public static void deletePoint(final OsmandApplication app, Activity ctx,
								   final ArrayAdapter adapter,
								   final WaypointDialogHelper helper,
								   final Object item,
								   final List<LocationPointWrapper> deletedPoints,
								   final boolean needCallback) {

		if (item instanceof LocationPointWrapper && adapter != null) {
			LocationPointWrapper point = (LocationPointWrapper) item;
			if (point.type == WaypointHelper.TARGETS && adapter instanceof StableArrayAdapter) {
				StableArrayAdapter stableAdapter = (StableArrayAdapter) adapter;
				if (helper != null && !helper.helperCallbacks.isEmpty() && needCallback) {
					for (WaypointDialogHelperCallback callback : helper.helperCallbacks) {
						callback.deleteWaypoint(stableAdapter.getPosition(item));
					}
				}
				updateRouteInfoMenu(ctx);
			} else {
				ArrayList<LocationPointWrapper> arr = new ArrayList<>();
				arr.add(point);
				app.getWaypointHelper().removeVisibleLocationPoint(arr);

				deletedPoints.add(point);

				adapter.setNotifyOnChange(false);
				adapter.remove(point);
				if (adapter instanceof StableArrayAdapter) {
					StableArrayAdapter stableAdapter = (StableArrayAdapter) adapter;
					stableAdapter.getObjects().remove(item);
					stableAdapter.refreshData();
				}
				adapter.notifyDataSetChanged();
			}
		}
	}

	public static void showOnMap(OsmandApplication app, Activity a, LocationPoint locationPoint, boolean center) {
		if (!(a instanceof MapActivity)) {
			return;
		}
		Object object = locationPoint;
		if (locationPoint instanceof AmenityLocationPoint) {
			object = ((AmenityLocationPoint) locationPoint).a;
		}
		app.getSettings().setMapLocationToShow(locationPoint.getLatitude(), locationPoint.getLongitude(),
				15, locationPoint.getPointDescription(a), false, object);
		MapActivity.launchMapActivityMoveToTop(a);
	}

	@SuppressLint("StaticFieldLeak")
	public static void sortAllTargets(final OsmandApplication app, final Activity activity,
									  final WaypointDialogHelper helper) {

		new AsyncTask<Void, Void, int[]>() {

			ProgressDialog dlg = null;
			long startDialogTime = 0;
			List<TargetPoint> intermediates;

			protected void onPreExecute() {
				startDialogTime = System.currentTimeMillis();
				dlg = new ProgressDialog(activity);
				dlg.setTitle("");
				dlg.setMessage(activity.getResources().getString(R.string.intermediate_items_sort_by_distance));
				dlg.show();
			}

			protected int[] doInBackground(Void[] params) {

				TargetPointsHelper targets = app.getTargetPointsHelper();
				intermediates = targets.getIntermediatePointsWithTarget();

				Location cll = app.getLocationProvider().getLastKnownLocation();
				ArrayList<TargetPoint> lt = new ArrayList<>(intermediates);
				TargetPoint start;

				if (cll != null) {
					LatLon ll = new LatLon(cll.getLatitude(), cll.getLongitude());
					start = TargetPoint.create(ll, null);
				} else if (app.getTargetPointsHelper().getPointToStart() != null) {
					TargetPoint ps = app.getTargetPointsHelper().getPointToStart();
					LatLon ll = new LatLon(ps.getLatitude(), ps.getLongitude());
					start = TargetPoint.create(ll, null);
				} else {
					start = lt.get(0);
				}
				TargetPoint end = lt.remove(lt.size() - 1);
				ArrayList<LatLon> al = new ArrayList<>();
				for (TargetPoint p : lt) {
					al.add(p.point);
				}
				try {
					return new TspAnt().readGraph(al, start.point, end.point).solve();
				} catch (Exception e) {
					return null;
				}
			}

			protected void onPostExecute(int[] result) {
				if (dlg != null) {
					long t = System.currentTimeMillis();
					if (t - startDialogTime < 500) {
						app.runInUIThread(new Runnable() {
							@Override
							public void run() {
								dlg.dismiss();
							}
						}, 500 - (t - startDialogTime));
					} else {
						dlg.dismiss();
					}
				}
				if (result == null) {
					return;
				}
				List<TargetPoint> alocs = new ArrayList<>();
				for (int i : result) {
					if (i > 0) {
						TargetPoint loc = intermediates.get(i - 1);
						alocs.add(loc);
					}
				}
				intermediates.clear();
				intermediates.addAll(alocs);

				TargetPointsHelper targets = app.getTargetPointsHelper();
				List<TargetPoint> cur = targets.getIntermediatePointsWithTarget();
				boolean eq = true;
				for (int j = 0; j < cur.size() && j < intermediates.size(); j++) {
					if (cur.get(j) != intermediates.get(j)) {
						eq = false;
						break;
					}
				}
				if (!eq) {
					targets.reorderAllTargetPoints(intermediates, true);
				}
				if (!helper.helperCallbacks.isEmpty()) {
					for (WaypointDialogHelperCallback callback : helper.helperCallbacks) {
						callback.reloadAdapter();
					}
				}
				updateRouteInfoMenu(activity);
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static class TargetOptionsBottomSheetDialogFragment extends MenuBottomSheetDialogFragment {

		public static final String TAG = "TargetOptionsBottomSheetDialogFragment";

		@Override
		public void createMenuItems(Bundle savedInstanceState) {
			items.add(new TitleItem(getString(R.string.shared_string_options)));

			BaseBottomSheetItem sortDoorToDoorItem = new SimpleBottomSheetItem.Builder()
					.setIcon(getContentIcon(R.drawable.ic_action_sort_door_to_door))
					.setTitle(getString(R.string.intermediate_items_sort_by_distance))
					.setLayoutId(R.layout.bottom_sheet_item_simple)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							MapActivity mapActivity = getMapActivity();
							if (mapActivity != null) {
								WaypointDialogHelper.sortAllTargets(
										mapActivity.getMyApplication(),
										mapActivity,
										mapActivity.getDashboard().getWaypointDialogHelper()
								);
							}
							dismiss();
						}
					})
					.create();
			items.add(sortDoorToDoorItem);

			BaseBottomSheetItem reorderStartAndFinishItem = new SimpleBottomSheetItem.Builder()
					.setIcon(getContentIcon(R.drawable.ic_action_sort_reverse_order))
					.setTitle(getString(R.string.switch_start_finish))
					.setLayoutId(R.layout.bottom_sheet_item_simple)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							MapActivity mapActivity = getMapActivity();
							if (mapActivity != null) {
								OsmandApplication app = mapActivity.getMyApplication();
								TargetPointsHelper targetsHelper = app.getTargetPointsHelper();
								WaypointDialogHelper.switchStartAndFinish(
										targetsHelper,
										targetsHelper.getPointToNavigate(),
										mapActivity,
										targetsHelper.getPointToStart(),
										app,
										mapActivity.getDashboard().getWaypointDialogHelper()
								);
							}
							dismiss();
						}
					})
					.create();
			items.add(reorderStartAndFinishItem);

			items.add(new DividerHalfItem(getContext()));

			final BaseBottomSheetItem[] addWaypointItem = new BaseBottomSheetItem[1];
			addWaypointItem[0] = new SimpleBottomSheetItem.Builder()
					.setIcon(getContentIcon(R.drawable.ic_action_plus))
					.setTitle(getString(R.string.add_intermediate_point))
					.setLayoutId(R.layout.bottom_sheet_item_simple)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onWaypointItemClick(addWaypointItem);
						}
					})
					.create();
			items.add(addWaypointItem[0]);

			BaseBottomSheetItem clearIntermediatesItem = new SimpleBottomSheetItem.Builder()
					.setIcon(getContentIcon(R.drawable.ic_action_clear_all))
					.setTitle(getString(R.string.clear_all_intermediates))
					.setLayoutId(R.layout.bottom_sheet_item_simple)
					.setDisabled(getMyApplication().getTargetPointsHelper().getIntermediatePoints().isEmpty())
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							MapActivity mapActivity = getMapActivity();
							if (mapActivity != null) {
								WaypointDialogHelper.clearAllIntermediatePoints(
										mapActivity,
										mapActivity.getMyApplication().getTargetPointsHelper(),
										mapActivity.getDashboard().getWaypointDialogHelper()
								);
							}
							dismiss();
						}
					})
					.create();
			items.add(clearIntermediatesItem);
		}

		@Override
		protected int getDismissButtonTextId() {
			return R.string.shared_string_close;
		}

		private void openAddPointDialog(MapActivity mapActivity) {
			Bundle args = new Bundle();
			args.putString(AddPointBottomSheetDialog.POINT_TYPE_KEY, MapRouteInfoMenu.PointType.INTERMEDIATE.name());
			AddPointBottomSheetDialog fragment = new AddPointBottomSheetDialog();
			fragment.setArguments(args);
			fragment.setUsedOnMap(false);
			fragment.show(mapActivity.getSupportFragmentManager(), AddPointBottomSheetDialog.TAG);
		}

		private void onWaypointItemClick(BaseBottomSheetItem[] addWaypointItem) {
			final MapActivity mapActivity = getMapActivity();
			if (mapActivity != null) {
				openAddPointDialog(mapActivity);
			}
		}

		@Nullable
		private MapActivity getMapActivity() {
			Activity activity = getActivity();
			if (activity instanceof MapActivity) {
				return (MapActivity) activity;
			}
			return null;
		}
	}
}
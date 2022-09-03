package net.osmand.plus.mapcontextmenu.controllers;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.AvoidSpecificRoads.AvoidRoadInfo;
import net.osmand.plus.mapcontextmenu.MenuBuilder;
import net.osmand.plus.mapcontextmenu.MenuController;
import net.osmand.plus.routing.RoutingHelper;

public class ImpassibleRoadsMenuController extends MenuController {

	private AvoidRoadInfo avoidRoadInfo;

	public ImpassibleRoadsMenuController(@NonNull MapActivity mapActivity,
										 @NonNull PointDescription pointDescription,
										 @NonNull AvoidRoadInfo avoidRoadInfo) {
		super(new MenuBuilder(mapActivity), pointDescription, mapActivity);
		this.avoidRoadInfo = avoidRoadInfo;
		final OsmandApplication app = mapActivity.getMyApplication();
		leftTitleButtonController = new TitleButtonController() {
			@Override
			public void buttonPressed() {
				MapActivity activity = getMapActivity();
				if (activity != null) {
					app.getAvoidSpecificRoads().removeImpassableRoad(
							ImpassibleRoadsMenuController.this.avoidRoadInfo);
					RoutingHelper rh = app.getRoutingHelper();
					if (rh.isRouteCalculated() || rh.isRouteBeingCalculated()) {
						rh.recalculateRouteDueToSettingsChange();
					}
					activity.getContextMenu().close();
				}
			}
		};
		leftTitleButtonController.caption = mapActivity.getString(R.string.shared_string_remove);
		leftTitleButtonController.leftIconId = R.drawable.ic_action_delete_dark;
	}

	@Override
	protected void setObject(Object object) {
		avoidRoadInfo = (AvoidRoadInfo) object;
	}

	@Override
	protected Object getObject() {
		return avoidRoadInfo;
	}

	@NonNull
	@Override
	public String getTypeStr() {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			return mapActivity.getString(R.string.road_blocked);
		} else {
			return "";
		}
	}

	@Override
	public Drawable getRightIcon() {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			return ContextCompat.getDrawable(mapActivity, R.drawable.map_pin_avoid_road);
		} else {
			return null;
		}
	}
}

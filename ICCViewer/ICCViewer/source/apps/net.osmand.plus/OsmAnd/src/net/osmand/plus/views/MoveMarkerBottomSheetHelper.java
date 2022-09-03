package net.osmand.plus.views;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.data.RotatedTileBox;
import net.osmand.data.PointDescription;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;

public class MoveMarkerBottomSheetHelper {
	private final View mView;
	private final TextView mDescription;
	private final Context mContext;
	private final ContextMenuLayer mContextMenuLayer;
	private boolean applyingPositionMode;

	public MoveMarkerBottomSheetHelper(MapActivity activity, ContextMenuLayer contextMenuLayer) {
		mContextMenuLayer = contextMenuLayer;
		this.mView = activity.findViewById(R.id.move_marker_bottom_sheet);
		ImageView icon = (ImageView) mView.findViewById(R.id.icon);
		this.mDescription = (TextView) mView.findViewById(R.id.description);
		this.mContext = activity;

		UiUtilities iconsCache = activity.getMyApplication().getUIUtilities();
		icon.setImageDrawable(iconsCache.getIcon(R.drawable.ic_action_photo_dark, R.color.marker_green));
		mView.findViewById(R.id.apply_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mContextMenuLayer.applyNewMarkerPosition();
			}
		});
		mView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hide();
				mContextMenuLayer.cancelMovingMarker();
			}
		});
	}
	
	public void onDraw(RotatedTileBox rt) {
		PointF point = mContextMenuLayer.getMovableCenterPoint(rt);
		double lat = rt.getLatFromPixel(point.x, point.y);
		double lon = rt.getLonFromPixel(point.x, point.y);
		//mDescription.setText(mContext.getString(R.string.lat_lon_pattern, lat, lon));
		mDescription.setText(PointDescription.getLocationName(mContext, lat, lon, true));
	}
	
	public boolean isVisible() {
		return mView.getVisibility() == View.VISIBLE;
	}

	public void show(Drawable drawable) {
		exitApplyPositionMode();
		mView.setVisibility(View.VISIBLE);
		((ImageView) mView.findViewById(R.id.icon)).setImageDrawable(drawable);
	}

	public void hide() {
		exitApplyPositionMode();
		mView.setVisibility(View.GONE);
	}

	public void enterApplyPositionMode() {
		if (!applyingPositionMode) {
			applyingPositionMode = true;
			mView.findViewById(R.id.apply_button).setEnabled(false);
		}
	}

	public void exitApplyPositionMode() {
		if (applyingPositionMode) {
			applyingPositionMode = false;
			mView.findViewById(R.id.apply_button).setEnabled(true);
		}
	}
}

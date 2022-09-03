package net.osmand.plus.quickaction.actions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.osmand.CallbackWithObject;
import net.osmand.GPXUtilities.GPXFile;
import net.osmand.IndexConstants;
import net.osmand.plus.GpxSelectionHelper;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.GpxUiHelper;
import net.osmand.plus.quickaction.QuickAction;

public class ShowHideGpxTracksAction extends QuickAction {

	public static final int TYPE = 28;

	public ShowHideGpxTracksAction() {
		super(TYPE);
	}

	public ShowHideGpxTracksAction(QuickAction quickAction) {
		super(quickAction);
	}

	@Override
	public void execute(final MapActivity activity) {
		final GpxSelectionHelper selectedGpxHelper = activity.getMyApplication()
			.getSelectedGpxHelper();
		if (selectedGpxHelper.isShowingAnyGpxFiles()) {
			selectedGpxHelper.clearAllGpxFilesToShow(true);
		} else {
			selectedGpxHelper.restoreSelectedGpxFiles();
		}
	}

	@Override
	public void drawUI(ViewGroup parent, MapActivity activity) {

		View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.quick_action_with_text, parent, false);

		((TextView) view.findViewById(R.id.text))
			.setText(R.string.quick_action_show_hide_gpx_tracks_descr);

		parent.addView(view);
	}

	@Override
	public String getActionText(OsmandApplication application) {
		return application.getSelectedGpxHelper().isShowingAnyGpxFiles()
			? application.getString(R.string.quick_action_gpx_tracks_hide)
			: application.getString(R.string.quick_action_gpx_tracks_show);
	}

	@Override
	public boolean isActionWithSlash(OsmandApplication application) {
		return application.getSelectedGpxHelper().isShowingAnyGpxFiles();
	}
}

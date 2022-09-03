package net.osmand.plus.wikivoyage.menu;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.osmand.GPXUtilities.GPXFile;
import net.osmand.GPXUtilities.Metadata;
import net.osmand.GPXUtilities.WptPt;
import net.osmand.data.PointDescription;
import net.osmand.plus.GpxSelectionHelper.SelectedGpxFile;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.controllers.WptPtMenuController;
import net.osmand.plus.wikivoyage.article.WikivoyageArticleDialogFragment;
import net.osmand.plus.wikivoyage.data.TravelArticle;

public class WikivoyageWptPtMenuController extends WptPtMenuController {

	private WikivoyageWptPtMenuController(@NonNull MapActivity mapActivity, @NonNull PointDescription pointDescription, @NonNull WptPt wpt, @NonNull TravelArticle article) {
		super(new WikivoyageWptPtMenuBuilder(mapActivity, wpt), mapActivity, pointDescription, wpt);
		final long tripId = article.getTripId();
		final String lang = article.getLang();
		leftTitleButtonController = new TitleButtonController() {
			@Override
			public void buttonPressed() {
				MapActivity mapActivity = getMapActivity();
				if (mapActivity != null) {
					WikivoyageArticleDialogFragment.showInstance(mapActivity.getMyApplication(),
							mapActivity.getSupportFragmentManager(), tripId, lang);
				}
			}
		};
		leftTitleButtonController.caption = mapActivity.getString(R.string.context_menu_read_article);
		leftTitleButtonController.leftIconId = R.drawable.ic_action_read_text;
	}

	private static TravelArticle getTravelArticle(@NonNull MapActivity mapActivity, @NonNull WptPt wpt) {
		SelectedGpxFile selectedGpxFile = mapActivity.getMyApplication().getSelectedGpxHelper().getSelectedGPXFile(wpt);
		GPXFile gpxFile = selectedGpxFile != null ? selectedGpxFile.getGpxFile() : null;
		Metadata metadata = gpxFile != null ? gpxFile.metadata : null;
		String title = metadata != null ? metadata.getArticleTitle() : null;
		String lang = metadata != null ? metadata.getArticleLang() : null;
		if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(lang)) {
			return mapActivity.getMyApplication().getTravelDbHelper().getArticle(title, lang);
		}
		return null;
	}

	public static WptPtMenuController getInstance(@NonNull MapActivity mapActivity, @NonNull PointDescription pointDescription, @NonNull WptPt wpt) {
		TravelArticle travelArticle = getTravelArticle(mapActivity, wpt);
		if (travelArticle != null) {
			return new WikivoyageWptPtMenuController(mapActivity, pointDescription, wpt, travelArticle);
		}
		return null;
	}
}

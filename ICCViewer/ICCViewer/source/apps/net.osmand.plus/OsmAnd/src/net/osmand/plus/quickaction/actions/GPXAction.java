package net.osmand.plus.quickaction.actions;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import net.osmand.data.LatLon;
import net.osmand.plus.GeocodingLookupService;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.editors.EditCategoryDialogFragment;
import net.osmand.plus.mapcontextmenu.editors.SelectCategoryDialogFragment;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.widgets.AutoCompleteTextViewEx;

public class GPXAction extends QuickAction {

	public static final int TYPE = 6;

	public static final String KEY_NAME = "name";
	public static final String KEY_DIALOG = "dialog";
	public static final String KEY_CATEGORY_NAME = "category_name";
	public static final String KEY_CATEGORY_COLOR = "category_color";

	public GPXAction() {
		super(TYPE);
	}

	public GPXAction(QuickAction quickAction) {
		super(quickAction);
	}

	@Override
	public void execute(final MapActivity activity) {

		final LatLon latLon = activity.getMapView()
				.getCurrentRotatedTileBox()
				.getCenterLatLon();

		final String title = getParams().get(KEY_NAME);

		if (title == null || title.isEmpty()) {

			final Dialog progressDialog = new ProgressDialog(activity);
			progressDialog.setCancelable(false);
			progressDialog.setTitle(R.string.search_address);
			progressDialog.show();

			GeocodingLookupService.AddressLookupRequest lookupRequest = new GeocodingLookupService.AddressLookupRequest(latLon,

					new GeocodingLookupService.OnAddressLookupResult() {

						@Override
						public void geocodingDone(String address) {

							progressDialog.dismiss();
							activity.getContextMenu().addWptPt(latLon, address,
									getParams().get(KEY_CATEGORY_NAME),
									Integer.valueOf(getParams().get(KEY_CATEGORY_COLOR)),
									!Boolean.valueOf(getParams().get(KEY_DIALOG)));
						}

					}, null);

			activity.getMyApplication().getGeocodingLookupService().lookupAddress(lookupRequest);

		} else activity.getContextMenu().addWptPt(latLon, title,
				getParams().get(KEY_CATEGORY_NAME),
				Integer.valueOf(getParams().get(KEY_CATEGORY_COLOR)),
				!Boolean.valueOf(getParams().get(KEY_DIALOG)));
	}

	@Override
	public void drawUI(final ViewGroup parent, final MapActivity activity) {

		final View root = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.quick_action_add_gpx, parent, false);

		parent.addView(root);

		AutoCompleteTextViewEx categoryEdit = (AutoCompleteTextViewEx) root.findViewById(R.id.category_edit);
		SwitchCompat showDialog = (SwitchCompat) root.findViewById(R.id.saveButton);
		ImageView categoryImage = (ImageView) root.findViewById(R.id.category_image);
		EditText name = (EditText) root.findViewById(R.id.name_edit);

		if (!getParams().isEmpty()) {

			showDialog.setChecked(Boolean.valueOf(getParams().get(KEY_DIALOG)));
			categoryImage.setColorFilter(Integer.valueOf(getParams().get(KEY_CATEGORY_COLOR)));
			name.setText(getParams().get(KEY_NAME));
			categoryEdit.setText(getParams().get(KEY_CATEGORY_NAME));

			if (getParams().get(KEY_NAME).isEmpty() && Integer.valueOf(getParams().get(KEY_CATEGORY_COLOR)) == 0) {

				categoryEdit.setText("");
				categoryImage.setColorFilter(activity.getResources().getColor(R.color.icon_color_default_light));
			}

		} else {

			categoryEdit.setText("");
			categoryImage.setColorFilter(activity.getResources().getColor(R.color.icon_color_default_light));

			getParams().put(KEY_CATEGORY_NAME, "");
			getParams().put(KEY_CATEGORY_COLOR, "0");
		}

		categoryEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {

				SelectCategoryDialogFragment dialogFragment = SelectCategoryDialogFragment.createInstance("");

				dialogFragment.show(
						activity.getSupportFragmentManager(),
						SelectCategoryDialogFragment.TAG);

				dialogFragment.setSelectionListener(new SelectCategoryDialogFragment.CategorySelectionListener() {
					@Override
					public void onCategorySelected(String category, int color) {

						fillGroupParams(root, category, color);
					}
				});
			}
		});

		SelectCategoryDialogFragment dialogFragment = (SelectCategoryDialogFragment)
				activity.getSupportFragmentManager().findFragmentByTag(SelectCategoryDialogFragment.TAG);

		if (dialogFragment != null) {

			dialogFragment.setSelectionListener(new SelectCategoryDialogFragment.CategorySelectionListener() {
				@Override
				public void onCategorySelected(String category, int color) {

					fillGroupParams(root, category, color);
				}
			});

		} else {

			EditCategoryDialogFragment dialog = (EditCategoryDialogFragment)
					activity.getSupportFragmentManager().findFragmentByTag(EditCategoryDialogFragment.TAG);

			if (dialog != null) {

				dialogFragment.setSelectionListener(new SelectCategoryDialogFragment.CategorySelectionListener() {
					@Override
					public void onCategorySelected(String category, int color) {

						fillGroupParams(root, category, color);
					}
				});
			}
		}
	}

	@Override
	public boolean fillParams(View root, MapActivity activity) {

		getParams().put(KEY_NAME, ((EditText) root.findViewById(R.id.name_edit)).getText().toString());
		getParams().put(KEY_DIALOG, Boolean.toString(((SwitchCompat) root.findViewById(R.id.saveButton)).isChecked()));

		return true;
	}

	private void fillGroupParams(View root, String name, int color) {

		if (color == 0) color = root.getContext().getResources().getColor(R.color.icon_color_default_light);

		((AutoCompleteTextViewEx) root.findViewById(R.id.category_edit)).setText(name);
		((ImageView) root.findViewById(R.id.category_image)).setColorFilter(color);

		getParams().put(KEY_CATEGORY_NAME, name);
		getParams().put(KEY_CATEGORY_COLOR, String.valueOf(color));
	}
}

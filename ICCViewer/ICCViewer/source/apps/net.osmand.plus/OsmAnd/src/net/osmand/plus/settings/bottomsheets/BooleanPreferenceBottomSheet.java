package net.osmand.plus.settings.bottomsheets;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import net.osmand.AndroidUtils;
import net.osmand.PlatformUtil;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.BooleanPreference;
import net.osmand.plus.OsmandSettings.OsmandPreference;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithCompoundButton;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithDescription;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.settings.OnPreferenceChanged;
import net.osmand.plus.settings.preferences.SwitchPreferenceEx;

import org.apache.commons.logging.Log;

public class BooleanPreferenceBottomSheet extends BasePreferenceBottomSheet {

	public static final String TAG = BooleanPreferenceBottomSheet.class.getSimpleName();

	private static final Log LOG = PlatformUtil.getLog(BooleanPreferenceBottomSheet.class);

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		OsmandApplication app = getMyApplication();
		if (app == null) {
			return;
		}
		final SwitchPreferenceEx switchPreference = getSwitchPreferenceEx();
		if (switchPreference == null) {
			return;
		}
		OsmandPreference preference = app.getSettings().getPreference(switchPreference.getKey());
		if (!(preference instanceof BooleanPreference)) {
			return;
		}

		String title = switchPreference.getTitle().toString();
		items.add(new TitleItem(title));

		final OsmandSettings.BooleanPreference pref = (BooleanPreference) preference;
		CharSequence summaryOn = switchPreference.getSummaryOn();
		CharSequence summaryOff = switchPreference.getSummaryOff();
		final String on = summaryOn == null || summaryOn.toString().equals("")
				? getString(R.string.shared_string_enabled) : summaryOn.toString();
		final String off = summaryOff == null || summaryOff.toString().equals("")
				? getString(R.string.shared_string_disabled) : summaryOff.toString();
		final int activeColor = AndroidUtils.resolveAttribute(app, R.attr.active_color_basic);
		final int disabledColor = AndroidUtils.resolveAttribute(app, android.R.attr.textColorSecondary);
		boolean checked = pref.getModeValue(getAppMode());

		final BottomSheetItemWithCompoundButton[] preferenceBtn = new BottomSheetItemWithCompoundButton[1];
		preferenceBtn[0] = (BottomSheetItemWithCompoundButton) new BottomSheetItemWithCompoundButton.Builder()
				.setChecked(checked)
				.setTitle(checked ? on : off)
				.setTitleColorId(checked ? activeColor : disabledColor)
				.setCustomView(getCustomButtonView(checked))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean newValue = !pref.getModeValue(getAppMode());
						if (switchPreference.callChangeListener(newValue)) {
							switchPreference.setChecked(newValue);
							preferenceBtn[0].setTitle(newValue ? on : off);
							preferenceBtn[0].setChecked(newValue);
							preferenceBtn[0].setTitleColorId(newValue ? activeColor : disabledColor);
							updateCustomButtonView(v, newValue);

							Fragment target = getTargetFragment();
							if (target instanceof OnPreferenceChanged) {
								((OnPreferenceChanged) target).onPreferenceChanged(switchPreference.getKey());
							}
						}
					}
				})
				.create();
		if (isProfileDependent()) {
			preferenceBtn[0].setCompoundButtonColorId(getAppMode().getIconColorInfo().getColor(nightMode));
		}
		items.add(preferenceBtn[0]);

		String description = switchPreference.getDescription();
		if (description != null) {
			BaseBottomSheetItem preferenceDescription = new BottomSheetItemWithDescription.Builder()
					.setDescription(description)
					.setLayoutId(R.layout.bottom_sheet_item_preference_descr)
					.create();
			items.add(preferenceDescription);
		}
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	private View getCustomButtonView(boolean checked) {
		View customView = UiUtilities.getInflater(getContext(), nightMode).inflate(R.layout.bottom_sheet_item_preference_switch, null);
		updateCustomButtonView(customView, checked);

		return customView;
	}

	private void updateCustomButtonView(View customView, boolean checked) {
		OsmandApplication app = requiredMyApplication();
		View buttonView = customView.findViewById(R.id.button_container);

		int colorRes = getAppMode().getIconColorInfo().getColor(nightMode);
		int color = checked ? getResolvedColor(colorRes) : AndroidUtils.getColorFromAttr(app, R.attr.divider_color_basic);
		int bgColor = UiUtilities.getColorWithAlpha(color, checked ? 0.1f : 0.5f);
		int selectedColor = UiUtilities.getColorWithAlpha(color, checked ? 0.3f : 0.5f);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			int bgResId = R.drawable.rectangle_rounded_right;
			int selectableResId = R.drawable.ripple_rectangle_rounded_right;

			Drawable bgDrawable = app.getUIUtilities().getPaintedIcon(bgResId, bgColor);
			Drawable selectable = app.getUIUtilities().getPaintedIcon(selectableResId, selectedColor);
			Drawable[] layers = {bgDrawable, selectable};
			AndroidUtils.setBackground(buttonView, new LayerDrawable(layers));
		} else {
			int bgResId = R.drawable.rectangle_rounded_right;
			Drawable bgDrawable = app.getUIUtilities().getPaintedIcon(bgResId, bgColor);
			AndroidUtils.setBackground(buttonView, bgDrawable);
		}
	}

	private SwitchPreferenceEx getSwitchPreferenceEx() {
		return (SwitchPreferenceEx) getPreference();
	}

	public static void showInstance(@NonNull FragmentManager fm, String prefId, Fragment target, boolean usedOnMap,
									@Nullable ApplicationMode appMode, boolean profileDependent) {
		try {
			if (fm.findFragmentByTag(BooleanPreferenceBottomSheet.TAG) == null) {
				Bundle args = new Bundle();
				args.putString(PREFERENCE_ID, prefId);

				BooleanPreferenceBottomSheet fragment = new BooleanPreferenceBottomSheet();
				fragment.setArguments(args);
				fragment.setUsedOnMap(usedOnMap);
				fragment.setAppMode(appMode);
				fragment.setTargetFragment(target, 0);
				fragment.setProfileDependent(profileDependent);
				fragment.show(fm, BooleanPreferenceBottomSheet.TAG);
			}
		} catch (RuntimeException e) {
			LOG.error("showInstance", e);
		}
	}
}
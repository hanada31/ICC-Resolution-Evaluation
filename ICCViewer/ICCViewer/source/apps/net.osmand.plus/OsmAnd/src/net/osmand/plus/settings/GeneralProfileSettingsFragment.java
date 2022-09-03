package net.osmand.plus.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.osmand.data.PointDescription;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.CommonPreference;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.Version;
import net.osmand.plus.base.MapViewTrackingUtilities;
import net.osmand.plus.settings.bottomsheets.ChangeGeneralProfilesPrefBottomSheet;
import net.osmand.plus.settings.preferences.ListPreferenceEx;
import net.osmand.plus.settings.preferences.SwitchPreferenceEx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneralProfileSettingsFragment extends BaseSettingsFragment implements OnPreferenceChanged {

	public static final String TAG = GeneralProfileSettingsFragment.class.getSimpleName();

	@Override
	protected void setupPreferences() {
		setupAppThemePref();
		setupRotateMapPref();
		setupCenterPositionOnMapPref();
		setupMapScreenOrientationPref();

		setupDrivingRegionPref();
		setupUnitsOfLengthPref();
		setupCoordinatesFormatPref();
		setupAngularUnitsPref();
		setupSpeedSystemPref();

		setupKalmanFilterPref();
		setupMagneticFieldSensorPref();
		setupMapEmptyStateAllowedPref();
		setupExternalInputDevicePref();
		setupTrackballForMovementsPref();
	}

	@Override
	protected void onBindPreferenceViewHolder(Preference preference, PreferenceViewHolder holder) {
		super.onBindPreferenceViewHolder(preference, holder);

		String prefId = preference.getKey();
		if (settings.EXTERNAL_INPUT_DEVICE.getId().equals(prefId)) {
			boolean checked = settings.EXTERNAL_INPUT_DEVICE.getModeValue(getSelectedAppMode()) != OsmandSettings.NO_EXTERNAL_DEVICE;

			SwitchCompat switchView = (SwitchCompat) holder.findViewById(R.id.switchWidget);
			switchView.setOnCheckedChangeListener(null);
			switchView.setChecked(checked);
			switchView.setOnCheckedChangeListener(externalInputDeviceListener);
		}
	}

	CompoundButton.OnCheckedChangeListener externalInputDeviceListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ListPreferenceEx externalInputDevice = (ListPreferenceEx) findPreference(settings.EXTERNAL_INPUT_DEVICE.getId());
			if (isChecked) {
				getPreferenceManager().showDialog(externalInputDevice);
				buttonView.setChecked(false);
			} else {
				if (externalInputDevice.callChangeListener(OsmandSettings.NO_EXTERNAL_DEVICE)) {
					externalInputDevice.setValue(OsmandSettings.NO_EXTERNAL_DEVICE);
				} else {
					buttonView.setChecked(true);
				}
			}
		}
	};

	private void setupAppThemePref() {
		final ListPreferenceEx appTheme = (ListPreferenceEx) findPreference(settings.OSMAND_THEME.getId());
		appTheme.setEntries(new String[] {getString(R.string.dark_theme), getString(R.string.light_theme)});
		appTheme.setEntryValues(new Integer[] {OsmandSettings.OSMAND_DARK_THEME, OsmandSettings.OSMAND_LIGHT_THEME});
		appTheme.setIcon(getOsmandThemeIcon());
	}

	private Drawable getOsmandThemeIcon() {
		return getActiveIcon(settings.isLightContent() ? R.drawable.ic_action_sun : R.drawable.ic_action_moon);
	}

	private void setupRotateMapPref() {
		final ListPreferenceEx rotateMap = (ListPreferenceEx) findPreference(settings.ROTATE_MAP.getId());
		rotateMap.setEntries(new String[] {getString(R.string.rotate_map_none_opt), getString(R.string.rotate_map_bearing_opt), getString(R.string.rotate_map_compass_opt)});
		rotateMap.setEntryValues(new Integer[] {OsmandSettings.ROTATE_MAP_NONE, OsmandSettings.ROTATE_MAP_BEARING, OsmandSettings.ROTATE_MAP_COMPASS});
		rotateMap.setIcon(getRotateMapIcon());
	}

	private Drawable getRotateMapIcon() {
		switch (settings.ROTATE_MAP.getModeValue(getSelectedAppMode())) {
			case OsmandSettings.ROTATE_MAP_NONE:
				return getActiveIcon(R.drawable.ic_action_direction_north);
			case OsmandSettings.ROTATE_MAP_BEARING:
				return getActiveIcon(R.drawable.ic_action_direction_movement);
			default:
				return getActiveIcon(R.drawable.ic_action_direction_compass);
		}
	}

	private void setupCenterPositionOnMapPref() {
		Drawable disabled = getContentIcon(R.drawable.ic_action_display_position_bottom);
		Drawable enabled = getActiveIcon(R.drawable.ic_action_display_position_center);
		Drawable icon = getPersistentPrefIcon(enabled, disabled);

		SwitchPreferenceCompat centerPositionOnMap = (SwitchPreferenceCompat) findPreference(settings.CENTER_POSITION_ON_MAP.getId());
		centerPositionOnMap.setIcon(icon);
	}

	private void setupMapScreenOrientationPref() {
		final ListPreferenceEx mapScreenOrientation = (ListPreferenceEx) findPreference(settings.MAP_SCREEN_ORIENTATION.getId());
		mapScreenOrientation.setEntries(new String[] {getString(R.string.map_orientation_portrait), getString(R.string.map_orientation_landscape), getString(R.string.map_orientation_default)});
		mapScreenOrientation.setEntryValues(new Integer[] {ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED});
		mapScreenOrientation.setIcon(getMapScreenOrientationIcon());
	}

	private Drawable getMapScreenOrientationIcon() {
		switch (settings.MAP_SCREEN_ORIENTATION.getModeValue(getSelectedAppMode())) {
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				return getActiveIcon(R.drawable.ic_action_phone_portrait_orientation);
			case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
				return getActiveIcon(R.drawable.ic_action_phone_landscape_orientation);
			default:
				return getActiveIcon(R.drawable.ic_action_phone_device_orientation);
		}
	}

	private void setupDrivingRegionPref() {
		ApplicationMode selectedMode = getSelectedAppMode();
		Preference defaultDrivingRegion = findPreference(settings.DRIVING_REGION.getId());
		defaultDrivingRegion.setIcon(getActiveIcon(R.drawable.ic_action_car_dark));
		defaultDrivingRegion.setSummary(getString(settings.DRIVING_REGION_AUTOMATIC.getModeValue(selectedMode) ? R.string.driving_region_automatic : settings.DRIVING_REGION.getModeValue(selectedMode).name));
	}

	private void setupUnitsOfLengthPref() {
		OsmandSettings.MetricsConstants[] metricsConstants = OsmandSettings.MetricsConstants.values();
		String[] entries = new String[metricsConstants.length];
		Integer[] entryValues = new Integer[metricsConstants.length];

		for (int i = 0; i < entries.length; i++) {
			entries[i] = metricsConstants[i].toHumanString(app);
			entryValues[i] = metricsConstants[i].ordinal();
		}

		ListPreferenceEx unitsOfLength = (ListPreferenceEx) findPreference(settings.METRIC_SYSTEM.getId());
		unitsOfLength.setEntries(entries);
		unitsOfLength.setEntryValues(entryValues);
		unitsOfLength.setIcon(getActiveIcon(R.drawable.ic_action_ruler_unit));
	}

	private void setupCoordinatesFormatPref() {
		Preference coordinatesFormat = findPreference(settings.COORDINATES_FORMAT.getId());
		coordinatesFormat.setIcon(getActiveIcon(R.drawable.ic_action_coordinates_widget));
		coordinatesFormat.setSummary(PointDescription.formatToHumanString(app, settings.COORDINATES_FORMAT.getModeValue(getSelectedAppMode())));
	}

	private void setupAngularUnitsPref() {
		OsmandSettings.AngularConstants[] ac = OsmandSettings.AngularConstants.values();
		String[] entries = new String[ac.length];
		Integer[] entryValues = new Integer[ac.length];

		for (int i = 0; i < entries.length; i++) {
			if (ac[i] == OsmandSettings.AngularConstants.DEGREES) {
				entries[i] = OsmandSettings.AngularConstants.DEGREES.toHumanString(app) + " 180";
				entryValues[i] = OsmandSettings.AngularConstants.DEGREES.ordinal();
			} else if (ac[i] == OsmandSettings.AngularConstants.DEGREES360) {
				entries[i] = OsmandSettings.AngularConstants.DEGREES.toHumanString(app) + " 360";
				entryValues[i] = OsmandSettings.AngularConstants.DEGREES360.ordinal();
			} else {
				entries[i] = ac[i].toHumanString(app);
				entryValues[i] = OsmandSettings.AngularConstants.MILLIRADS.ordinal();
			}
		}

		ListPreferenceEx angularUnits = (ListPreferenceEx) findPreference(settings.ANGULAR_UNITS.getId());
		angularUnits.setEntries(entries);
		angularUnits.setEntryValues(entryValues);
		angularUnits.setIcon(getActiveIcon(R.drawable.ic_action_angular_unit));
	}

	private void setupSpeedSystemPref() {
		OsmandSettings.SpeedConstants[] speedConstants = OsmandSettings.SpeedConstants.values();
		String[] entries = new String[speedConstants.length];
		Integer[] entryValues = new Integer[speedConstants.length];

		for (int i = 0; i < entries.length; i++) {
			entries[i] = speedConstants[i].toHumanString(app);
			entryValues[i] = speedConstants[i].ordinal();
		}

		ListPreferenceEx speedSystem = (ListPreferenceEx) findPreference(settings.SPEED_SYSTEM.getId());
		speedSystem.setEntries(entries);
		speedSystem.setEntryValues(entryValues);
		speedSystem.setDescription(R.string.default_speed_system_descr);
		speedSystem.setIcon(getActiveIcon(R.drawable.ic_action_speed));
	}

	private void setupKalmanFilterPref() {
		SwitchPreferenceEx kalmanFilterPref = (SwitchPreferenceEx) findPreference(settings.USE_KALMAN_FILTER_FOR_COMPASS.getId());
		kalmanFilterPref.setTitle(getString(R.string.use_kalman_filter_compass));
		kalmanFilterPref.setDescription(getString(R.string.use_kalman_filter_compass_descr));
	}

	private void setupMagneticFieldSensorPref() {
		SwitchPreferenceEx useMagneticSensorPref = (SwitchPreferenceEx) findPreference(settings.USE_MAGNETIC_FIELD_SENSOR_COMPASS.getId());
		useMagneticSensorPref.setTitle(getString(R.string.use_magnetic_sensor));
		useMagneticSensorPref.setDescription(getString(R.string.use_magnetic_sensor_descr));
	}

	private void setupMapEmptyStateAllowedPref() {
		SwitchPreferenceEx mapEmptyStateAllowedPref = (SwitchPreferenceEx) findPreference(settings.MAP_EMPTY_STATE_ALLOWED.getId());
		mapEmptyStateAllowedPref.setTitle(getString(R.string.tap_on_map_to_hide_interface));
		mapEmptyStateAllowedPref.setDescription(getString(R.string.tap_on_map_to_hide_interface_descr));
	}

	private void setupExternalInputDevicePref() {
		ListPreferenceEx externalInputDevice = (ListPreferenceEx) findPreference(settings.EXTERNAL_INPUT_DEVICE.getId());
		externalInputDevice.setSummary(R.string.sett_no_ext_input);
		externalInputDevice.setEntries(new String[] {
				getString(R.string.sett_generic_ext_input),
				getString(R.string.sett_wunderlinq_ext_input),
				getString(R.string.sett_parrot_ext_input)
		});

		externalInputDevice.setEntryValues(new Integer[] {
				OsmandSettings.GENERIC_EXTERNAL_DEVICE,
				OsmandSettings.WUNDERLINQ_EXTERNAL_DEVICE,
				OsmandSettings.PARROT_EXTERNAL_DEVICE}
		);
	}

	private void setupTrackballForMovementsPref() {
		SwitchPreferenceEx mapEmptyStateAllowedPref = (SwitchPreferenceEx) findPreference(settings.USE_TRACKBALL_FOR_MOVEMENTS.getId());
		mapEmptyStateAllowedPref.setTitle(getString(R.string.use_trackball));
		mapEmptyStateAllowedPref.setDescription(getString(R.string.use_trackball_descr));

		boolean visible = false;
		if (!Version.isBlackberry(app)) {
			int nav = getResources().getConfiguration().navigation;
			visible = nav == Configuration.NAVIGATION_DPAD || nav == Configuration.NAVIGATION_TRACKBALL ||
					nav == Configuration.NAVIGATION_WHEEL || nav == Configuration.NAVIGATION_UNDEFINED;
		}
		mapEmptyStateAllowedPref.setVisible(visible);
	}

	private void showDrivingRegionDialog() {
		Context themedContext = UiUtilities.getThemedContext(getActivity(), isNightMode());
		AlertDialog.Builder b = new AlertDialog.Builder(themedContext);

		b.setTitle(getString(R.string.driving_region));

		final List<OsmandSettings.DrivingRegion> drs = new ArrayList<>();
		drs.add(null);
		drs.addAll(Arrays.asList(OsmandSettings.DrivingRegion.values()));
		int sel = -1;
		ApplicationMode selectedMode = getSelectedAppMode();
		OsmandSettings.DrivingRegion selectedDrivingRegion = settings.DRIVING_REGION.getModeValue(selectedMode);
		if (settings.DRIVING_REGION_AUTOMATIC.getModeValue(selectedMode)) {
			sel = 0;
		}
		for (int i = 1; i < drs.size(); i++) {
			if (sel == -1 && drs.get(i) == selectedDrivingRegion) {
				sel = i;
				break;
			}
		}

		final int selected = sel;
		final ArrayAdapter<OsmandSettings.DrivingRegion> singleChoiceAdapter =
				new ArrayAdapter<OsmandSettings.DrivingRegion>(themedContext, R.layout.single_choice_description_item, R.id.text1, drs) {
					@NonNull
					@Override
					public View getView(int position, View convertView, @NonNull ViewGroup parent) {
						View v = convertView;
						if (v == null) {
							v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_choice_description_item, parent, false);
						}
						OsmandSettings.DrivingRegion item = getItem(position);
						AppCompatCheckedTextView title = (AppCompatCheckedTextView) v.findViewById(R.id.text1);
						TextView desc = (TextView) v.findViewById(R.id.description);
						if (item != null) {
							title.setText(getString(item.name));
							desc.setVisibility(View.VISIBLE);
							desc.setText(item.getDescription(v.getContext()));
						} else {
							title.setText(getString(R.string.driving_region_automatic));
							desc.setVisibility(View.GONE);
						}
						title.setChecked(position == selected);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							UiUtilities.setupCompoundButtonDrawable(app, isNightMode(), getActiveProfileColor(), title.getCheckMarkDrawable());
						}
						return v;
					}
				};

		b.setAdapter(singleChoiceAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ApplicationMode selectedMode = getSelectedAppMode();
				if (drs.get(which) == null) {
					settings.DRIVING_REGION_AUTOMATIC.setModeValue(selectedMode, true);
					MapViewTrackingUtilities mapViewTrackingUtilities = getMyApplication().getMapViewTrackingUtilities();
					if (mapViewTrackingUtilities != null) {
						mapViewTrackingUtilities.resetDrivingRegionUpdate();
					}
				} else {
					settings.DRIVING_REGION_AUTOMATIC.setModeValue(selectedMode, false);
					settings.DRIVING_REGION.setModeValue(selectedMode, drs.get(which));
				}
				updateAllSettings();
			}
		});

		b.setNegativeButton(R.string.shared_string_cancel, null);
		b.show();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(settings.DRIVING_REGION.getId())) {
			showDrivingRegionDialog();
			return true;
		}
		return super.onPreferenceClick(preference);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String prefId = preference.getKey();

		OsmandSettings.OsmandPreference pref = settings.getPreference(prefId);
		if (pref instanceof CommonPreference && !((CommonPreference) pref).hasDefaultValueForMode(getSelectedAppMode())) {
			FragmentManager fragmentManager = getFragmentManager();
			if (fragmentManager != null && newValue instanceof Serializable) {
				ChangeGeneralProfilesPrefBottomSheet.showInstance(fragmentManager, prefId,
						(Serializable) newValue, this, false, getSelectedAppMode());
			}
			return false;
		}

		return true;
	}

	@Override
	public void onPreferenceChanged(String prefId) {
		Preference preference = findPreference(prefId);
		if (preference != null) {
			if (settings.OSMAND_THEME.getId().equals(prefId)) {
				preference.setIcon(getOsmandThemeIcon());
			} else if (settings.ROTATE_MAP.getId().equals(prefId)) {
				preference.setIcon(getRotateMapIcon());
			} else if (settings.MAP_SCREEN_ORIENTATION.getId().equals(prefId)) {
				preference.setIcon(getMapScreenOrientationIcon());
			}
		}
	}

	@Override
	public void updateSetting(String prefId) {
		if (settings.OSMAND_THEME.getId().equals(prefId)) {
			recreate();
			return;
		}
		super.updateSetting(prefId);
	}
}
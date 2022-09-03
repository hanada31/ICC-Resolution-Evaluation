package net.osmand.plus.settings;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.settings.bottomsheets.ChangeGeneralProfilesPrefBottomSheet;
import net.osmand.plus.settings.preferences.EditTextPreferenceEx;
import net.osmand.plus.settings.preferences.ListPreferenceEx;

import java.io.Serializable;

import static net.osmand.plus.UiUtilities.CompoundButtonType.TOOLBAR;
import static net.osmand.plus.monitoring.OsmandMonitoringPlugin.MAX_INTERVAL_TO_SEND_MINUTES;
import static net.osmand.plus.monitoring.OsmandMonitoringPlugin.MINUTES;
import static net.osmand.plus.monitoring.OsmandMonitoringPlugin.SECONDS;

public class LiveMonitoringFragment extends BaseSettingsFragment {

	@Override
	protected void setupPreferences() {
		Preference liveMonitoringInfo = findPreference("live_monitoring_info");
		liveMonitoringInfo.setIconSpaceReserved(false);

		setupLiveMonitoringUrlPref();
		setupLiveMonitoringIntervalPref();
		setupLiveMonitoringBufferPref();
		enableDisablePreferences(settings.LIVE_MONITORING.getModeValue(getSelectedAppMode()));
	}

	@Override
	protected void createToolbar(LayoutInflater inflater, View view) {
		super.createToolbar(inflater, view);

		view.findViewById(R.id.toolbar_switch_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ApplicationMode appMode = getSelectedAppMode();
				boolean checked = !settings.LIVE_MONITORING.getModeValue(appMode);
				settings.LIVE_MONITORING.setModeValue(appMode, checked);
				updateToolbarSwitch();
				enableDisablePreferences(checked);
			}
		});
		TextView title = (TextView) view.findViewById(R.id.switchButtonText);
		title.setTextColor(ContextCompat.getColor(app, isNightMode() ? R.color.active_color_primary_dark : R.color.active_color_primary_light));
	}

	@Override
	protected void updateToolbar() {
		super.updateToolbar();
		updateToolbarSwitch();
	}

	private void updateToolbarSwitch() {
		View view = getView();
		if (view == null) {
			return;
		}
		boolean checked = settings.LIVE_MONITORING.getModeValue(getSelectedAppMode());
		int color = checked ? getActiveProfileColor() : ContextCompat.getColor(app, R.color.preference_top_switch_off);

		View selectableView = view.findViewById(R.id.selectable_item);
		View switchContainer = view.findViewById(R.id.toolbar_switch_container);

		AndroidUtils.setBackground(switchContainer, new ColorDrawable(color));

		SwitchCompat switchView = (SwitchCompat) selectableView.findViewById(R.id.switchWidget);
		switchView.setChecked(checked);
		UiUtilities.setupCompoundButton(switchView, isNightMode(), TOOLBAR);

		TextView title = (TextView) selectableView.findViewById(R.id.switchButtonText);
		title.setText(checked ? R.string.shared_string_enabled : R.string.shared_string_disabled);
		title.setTextColor(ContextCompat.getColor(app, isNightMode() ? R.color.text_color_tab_active_dark : R.color.text_color_tab_active_light));

		Drawable drawable = UiUtilities.getColoredSelectableDrawable(app, getActiveProfileColor(), 0.3f);
		AndroidUtils.setBackground(selectableView, drawable);
	}

	private void setupLiveMonitoringUrlPref() {
		ApplicationMode appMode = getSelectedAppMode();
		String summary;
		if (settings.LIVE_MONITORING_URL.isSetForMode(appMode)) {
			summary = settings.LIVE_MONITORING_URL.getModeValue(appMode);
		} else {
			summary = getString(R.string.shared_string_disabled);
		}

		EditTextPreferenceEx liveMonitoringUrl = (EditTextPreferenceEx) findPreference(settings.LIVE_MONITORING_URL.getId());
		liveMonitoringUrl.setSummary(summary);
		liveMonitoringUrl.setDescription(R.string.live_monitoring_adress_descr);
		liveMonitoringUrl.setIcon(getPersistentPrefIcon(R.drawable.ic_world_globe_dark));
	}

	private void setupLiveMonitoringIntervalPref() {
		Integer[] entryValues = new Integer[SECONDS.length + MINUTES.length];
		String[] entries = new String[entryValues.length];
		int k = 0;
		for (int second : SECONDS) {
			entryValues[k] = second * 1000;
			entries[k] = second + " " + getString(R.string.int_seconds);
			k++;
		}
		for (int minute : MINUTES) {
			entryValues[k] = (minute * 60) * 1000;
			entries[k] = minute + " " + getString(R.string.int_min);
			k++;
		}

		ListPreferenceEx liveMonitoringInterval = (ListPreferenceEx) findPreference(settings.LIVE_MONITORING_INTERVAL.getId());
		liveMonitoringInterval.setEntries(entries);
		liveMonitoringInterval.setEntryValues(entryValues);
		liveMonitoringInterval.setIcon(getPersistentPrefIcon(R.drawable.ic_action_time_span));
		liveMonitoringInterval.setDescription(R.string.live_monitoring_interval_descr);
	}

	private void setupLiveMonitoringBufferPref() {
		Integer[] entryValues = new Integer[MAX_INTERVAL_TO_SEND_MINUTES.length];
		String[] entries = new String[entryValues.length];

		for (int i = 0; i < MAX_INTERVAL_TO_SEND_MINUTES.length; i++) {
			int minute = MAX_INTERVAL_TO_SEND_MINUTES[i];
			entryValues[i] = (minute * 60) * 1000;
			entries[i] = minute + " " + getString(R.string.int_min);
		}

		ListPreferenceEx liveMonitoringBuffer = (ListPreferenceEx) findPreference(settings.LIVE_MONITORING_MAX_INTERVAL_TO_SEND.getId());
		liveMonitoringBuffer.setEntries(entries);
		liveMonitoringBuffer.setEntryValues(entryValues);
		liveMonitoringBuffer.setIcon(getPersistentPrefIcon(R.drawable.ic_action_time_span));
		liveMonitoringBuffer.setDescription(R.string.live_monitoring_max_interval_to_send_desrc);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String prefId = preference.getKey();

		OsmandSettings.OsmandPreference pref = settings.getPreference(prefId);
		if (pref instanceof OsmandSettings.CommonPreference && !((OsmandSettings.CommonPreference) pref).hasDefaultValueForMode(getSelectedAppMode())) {
			FragmentManager fragmentManager = getFragmentManager();
			if (fragmentManager != null && newValue instanceof Serializable) {
				ChangeGeneralProfilesPrefBottomSheet.showInstance(fragmentManager, prefId,
						(Serializable) newValue, this, false, getSelectedAppMode());
			}
			return false;
		}

		return true;
	}
}
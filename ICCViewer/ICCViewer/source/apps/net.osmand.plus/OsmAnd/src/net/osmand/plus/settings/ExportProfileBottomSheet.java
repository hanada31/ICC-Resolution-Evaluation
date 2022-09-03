package net.osmand.plus.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import net.osmand.AndroidUtils;
import net.osmand.IndexConstants;
import net.osmand.PlatformUtil;
import net.osmand.data.LatLon;
import net.osmand.map.ITileSource;
import net.osmand.map.TileSourceManager;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.SQLiteTileSource;
import net.osmand.plus.SettingsHelper;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithCompoundButton;
import net.osmand.plus.base.bottomsheetmenu.SimpleBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.helpers.AvoidSpecificRoads.AvoidRoadInfo;
import net.osmand.plus.poi.PoiUIFilter;
import net.osmand.plus.profiles.AdditionalDataWrapper;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.quickaction.QuickActionFactory;
import net.osmand.plus.settings.bottomsheets.BasePreferenceBottomSheet;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExportProfileBottomSheet extends BasePreferenceBottomSheet {

	private static final Log LOG = PlatformUtil.getLog(ExportProfileBottomSheet.class);
	public static final String TAG = ExportProfileBottomSheet.class.getSimpleName();
	private static final String INCLUDE_ADDITIONAL_DATA_KEY = "INCLUDE_ADDITIONAL_DATA_KEY";
	private boolean includeAdditionalData = false;
	private OsmandApplication app;
	private ApplicationMode profile;
	private List<AdditionalDataWrapper> dataList = new ArrayList<>();
	private ExportImportSettingsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			includeAdditionalData = savedInstanceState.getBoolean(INCLUDE_ADDITIONAL_DATA_KEY);
		}
		super.onCreate(savedInstanceState);
		app = requiredMyApplication();
		dataList = getAdditionalData();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INCLUDE_ADDITIONAL_DATA_KEY, includeAdditionalData);
	}

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		final Context context = getContext();
		if (context == null) {
			return;
		}
		LayoutInflater inflater = UiUtilities.getInflater(app, nightMode);

		profile = getAppMode();

		int profileColor = profile.getIconColorInfo().getColor(nightMode);
		int colorNoAlpha = ContextCompat.getColor(context, profileColor);

		Drawable backgroundIcon = UiUtilities.getColoredSelectableDrawable(context, colorNoAlpha, 0.3f);
		Drawable[] layers = {new ColorDrawable(UiUtilities.getColorWithAlpha(colorNoAlpha, 0.10f)), backgroundIcon};

		items.add(new TitleItem(getString(R.string.export_profile)));

		BaseBottomSheetItem profileItem = new BottomSheetItemWithCompoundButton.Builder()
				.setChecked(true)
				.setCompoundButtonColorId(profileColor)
				.setButtonTintList(ColorStateList.valueOf(getResolvedColor(profileColor)))
				.setDescription(BaseSettingsFragment.getAppModeDescription(context, profile))
				.setIcon(getIcon(profile.getIconRes(), profileColor))
				.setTitle(profile.toHumanString())
				.setBackground(new LayerDrawable(layers))
				.setLayoutId(R.layout.preference_profile_item_with_radio_btn)
				.create();
		items.add(profileItem);

		if (!dataList.isEmpty()) {
			final View additionalDataView = inflater.inflate(R.layout.bottom_sheet_item_additional_data, null);
			ExpandableListView listView = additionalDataView.findViewById(R.id.list);
			adapter = new ExportImportSettingsAdapter(app, nightMode);
			View listHeader = inflater.inflate(R.layout.item_header_export_expand_list, null);
			final View topSwitchDivider = listHeader.findViewById(R.id.topSwitchDivider);
			final View bottomSwitchDivider = listHeader.findViewById(R.id.bottomSwitchDivider);
			final SwitchCompat switchItem = listHeader.findViewById(R.id.switchItem);
			switchItem.setTextColor(getResources().getColor(nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light));
			switchItem.setChecked(includeAdditionalData);
			switchItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					includeAdditionalData = !includeAdditionalData;
					topSwitchDivider.setVisibility(includeAdditionalData ? View.VISIBLE : View.GONE);
					bottomSwitchDivider.setVisibility(includeAdditionalData ? View.VISIBLE : View.GONE);
					if (includeAdditionalData) {
						adapter.updateSettingsList(getAdditionalData());
						adapter.selectAll(true);
					} else {
						adapter.selectAll(false);
						adapter.clearSettingsList();
					}
					updateSwitch(switchItem);
					setupHeightAndBackground(getView());
				}
			});
			listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
				@Override
				public void onGroupExpand(int i) {
					setupHeightAndBackground(getView());
				}
			});

			updateSwitch(switchItem);
			listView.addHeaderView(listHeader);
			listView.setAdapter(adapter);
			final SimpleBottomSheetItem titleItem = (SimpleBottomSheetItem) new SimpleBottomSheetItem.Builder()
					.setCustomView(additionalDataView)
					.create();
			items.add(titleItem);
		}
	}

	private void updateSwitch(View view) {
		if (includeAdditionalData) {
			UiUtilities.setMargins(view, 0, 0, 0, 0);
			view.setPadding(AndroidUtils.dpToPx(app, 32), 0, AndroidUtils.dpToPx(app, 32), 0);
		} else {
			UiUtilities.setMargins(view, AndroidUtils.dpToPx(app, 16), 0, AndroidUtils.dpToPx(app, 16), 0);
			view.setPadding(AndroidUtils.dpToPx(app, 16), 0, AndroidUtils.dpToPx(app, 16), 0);
		}
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.shared_string_export;
	}

	@Override
	protected void onRightBottomButtonClick() {
		super.onRightBottomButtonClick();
		prepareFile();
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	@Override
	protected boolean useScrollableItemsContainer() {
		return false;
	}

	@Override
	protected boolean useExpandableList() {
		return true;
	}

	private List<AdditionalDataWrapper> getAdditionalData() {
		List<AdditionalDataWrapper> dataList = new ArrayList<>();

		QuickActionFactory factory = new QuickActionFactory();
		List<QuickAction> actionsList = factory.parseActiveActionsList(app.getSettings().QUICK_ACTION_LIST.get());
		if (!actionsList.isEmpty()) {
			dataList.add(new AdditionalDataWrapper(
					AdditionalDataWrapper.Type.QUICK_ACTIONS, actionsList));
		}

		List<PoiUIFilter> poiList = app.getPoiFilters().getUserDefinedPoiFilters(false);
		if (!poiList.isEmpty()) {
			dataList.add(new AdditionalDataWrapper(
					AdditionalDataWrapper.Type.POI_TYPES,
					poiList
			));
		}

		List<ITileSource> iTileSources = new ArrayList<>();
		Set<String> tileSourceNames = app.getSettings().getTileSourceEntries(true).keySet();
		for (String name : tileSourceNames) {
			File f = app.getAppPath(IndexConstants.TILES_INDEX_DIR + name);
			if (f != null) {
				ITileSource template;
				if (f.getName().endsWith(SQLiteTileSource.EXT)) {
					template = new SQLiteTileSource(app, f, TileSourceManager.getKnownSourceTemplates());
				} else {
					template = TileSourceManager.createTileSourceTemplate(f);
				}
				if (template != null && template.getUrlTemplate() != null) {
					iTileSources.add(template);
				}
			}
		}
		if (!iTileSources.isEmpty()) {
			dataList.add(new AdditionalDataWrapper(
					AdditionalDataWrapper.Type.MAP_SOURCES,
					iTileSources
			));
		}

		Map<String, File> externalRenderers = app.getRendererRegistry().getExternalRenderers();
		if (!externalRenderers.isEmpty()) {
			dataList.add(new AdditionalDataWrapper(
					AdditionalDataWrapper.Type.CUSTOM_RENDER_STYLE,
					new ArrayList<>(externalRenderers.values())
			));
		}

		File routingProfilesFolder = app.getAppPath(IndexConstants.ROUTING_PROFILES_DIR);
		if (routingProfilesFolder.exists() && routingProfilesFolder.isDirectory()) {
			File[] fl = routingProfilesFolder.listFiles();
			if (fl != null && fl.length > 0) {
				dataList.add(new AdditionalDataWrapper(
						AdditionalDataWrapper.Type.CUSTOM_ROUTING,
						Arrays.asList(fl)
				));
			}
		}

		Map<LatLon, AvoidRoadInfo> impassableRoads = app.getAvoidSpecificRoads().getImpassableRoads();
		if (!impassableRoads.isEmpty()) {
			dataList.add(new AdditionalDataWrapper(
					AdditionalDataWrapper.Type.AVOID_ROADS,
					new ArrayList<>(impassableRoads.values())
			));
		}
		return dataList;
	}

	private List<SettingsHelper.SettingsItem> prepareSettingsItemsForExport() {
		List<SettingsHelper.SettingsItem> settingsItems = new ArrayList<>();
		settingsItems.add(new SettingsHelper.ProfileSettingsItem(app, profile));
		if (includeAdditionalData) {
			settingsItems.addAll(prepareAdditionalSettingsItems());
		}
		return settingsItems;
	}

	private List<SettingsHelper.SettingsItem> prepareAdditionalSettingsItems() {
		List<SettingsHelper.SettingsItem> settingsItems = new ArrayList<>();
		List<QuickAction> quickActions = new ArrayList<>();
		List<PoiUIFilter> poiUIFilters = new ArrayList<>();
		List<ITileSource> tileSourceTemplates = new ArrayList<>();
		List<AvoidRoadInfo> avoidRoads = new ArrayList<>();
		for (Object object : adapter.getDataToOperate()) {
			if (object instanceof QuickAction) {
				quickActions.add((QuickAction) object);
			} else if (object instanceof PoiUIFilter) {
				poiUIFilters.add((PoiUIFilter) object);
			} else if (object instanceof TileSourceManager.TileSourceTemplate
					|| object instanceof SQLiteTileSource) {
				tileSourceTemplates.add((ITileSource) object);
			} else if (object instanceof File) {
				settingsItems.add(new SettingsHelper.FileSettingsItem(app, (File) object));
			} else if (object instanceof AvoidRoadInfo) {
				avoidRoads.add((AvoidRoadInfo) object);
			}
		}
		if (!quickActions.isEmpty()) {
			settingsItems.add(new SettingsHelper.QuickActionSettingsItem(app, quickActions));
		}
		if (!poiUIFilters.isEmpty()) {
			settingsItems.add(new SettingsHelper.PoiUiFilterSettingsItem(app, poiUIFilters));
		}
		if (!tileSourceTemplates.isEmpty()) {
			settingsItems.add(new SettingsHelper.MapSourcesSettingsItem(app, tileSourceTemplates));
		}
		if (!avoidRoads.isEmpty()) {
			settingsItems.add(new SettingsHelper.AvoidRoadsSettingsItem(app, avoidRoads));
		}
		return settingsItems;
	}

	private void prepareFile() {
		if (app != null) {
			File tempDir = app.getAppPath(IndexConstants.TEMP_DIR);
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			String fileName = profile.toHumanString();
			app.getSettingsHelper().exportSettings(tempDir, fileName, new SettingsHelper.SettingsExportListener() {
				@Override
				public void onSettingsExportFinished(@NonNull File file, boolean succeed) {
					if (succeed) {
						shareProfile(file, profile);
					} else {
						app.showToastMessage(R.string.export_profile_failed);
					}
				}
			}, prepareSettingsItemsForExport());
		}
	}

	private void shareProfile(@NonNull File file, @NonNull ApplicationMode profile) {
		try {
			final Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.exported_osmand_profile, profile.toHumanString()));
			sendIntent.putExtra(Intent.EXTRA_STREAM, AndroidUtils.getUriForFile(getMyApplication(), file));
			sendIntent.setType("*/*");
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(sendIntent);
			dismiss();
		} catch (Exception e) {
			Toast.makeText(requireContext(), R.string.export_profile_failed, Toast.LENGTH_SHORT).show();
			LOG.error("Share profile error", e);
		}
	}

	public static boolean showInstance(@NonNull FragmentManager fragmentManager,
									   Fragment target,
									   @NonNull ApplicationMode appMode) {
		try {
			ExportProfileBottomSheet fragment = new ExportProfileBottomSheet();
			fragment.setAppMode(appMode);
			fragment.setTargetFragment(target, 0);
			fragment.show(fragmentManager, TAG);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}

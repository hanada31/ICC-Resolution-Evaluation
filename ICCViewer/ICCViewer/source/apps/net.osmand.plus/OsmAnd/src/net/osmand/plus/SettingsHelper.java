package net.osmand.plus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.osmand.IndexConstants;
import net.osmand.PlatformUtil;
import net.osmand.data.LatLon;
import net.osmand.map.ITileSource;
import net.osmand.map.TileSourceManager;
import net.osmand.osm.MapPoiTypes;
import net.osmand.osm.PoiCategory;
import net.osmand.plus.ApplicationMode.ApplicationModeBean;
import net.osmand.plus.ApplicationMode.ApplicationModeBuilder;
import net.osmand.plus.OsmandSettings.OsmandPreference;
import net.osmand.plus.helpers.AvoidSpecificRoads;
import net.osmand.plus.helpers.AvoidSpecificRoads.AvoidRoadInfo;
import net.osmand.plus.poi.PoiUIFilter;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.quickaction.QuickActionRegistry;
import net.osmand.util.Algorithms;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static net.osmand.IndexConstants.OSMAND_SETTINGS_FILE_EXT;

/*
	Usage:

	SettingsHelper helper = app.getSettingsHelper();
	File file = new File(app.getAppPath(null), "settings.zip");

	List<SettingsItem> items = new ArrayList<>();
	items.add(new GlobalSettingsItem(app.getSettings()));
	items.add(new ProfileSettingsItem(app.getSettings(), ApplicationMode.DEFAULT));
	items.add(new ProfileSettingsItem(app.getSettings(), ApplicationMode.CAR));
	items.add(new ProfileSettingsItem(app.getSettings(), ApplicationMode.PEDESTRIAN));
	items.add(new ProfileSettingsItem(app.getSettings(), ApplicationMode.BICYCLE));
	items.add(new FileSettingsItem(app, new File(app.getAppPath(GPX_INDEX_DIR), "Day 2.gpx")));
	items.add(new FileSettingsItem(app, new File(app.getAppPath(GPX_INDEX_DIR), "Day 3.gpx")));
	items.add(new FileSettingsItem(app, new File(app.getAppPath(RENDERERS_DIR), "default.render.xml")));
	items.add(new DataSettingsItem(new byte[] {'t', 'e', 's', 't', '1'}, "data1"));
	items.add(new DataSettingsItem(new byte[] {'t', 'e', 's', 't', '2'}, "data2"));

	helper.exportSettings(file, items);

	helper.importSettings(file);
 */

public class SettingsHelper {

	public static final String SETTINGS_LATEST_CHANGES_KEY = "settings_latest_changes";
	public static final String SETTINGS_VERSION_KEY = "settings_version";

	private static final Log LOG = PlatformUtil.getLog(SettingsHelper.class);
	private static final int BUFFER = 1024;

	private OsmandApplication app;
	private Activity activity;

	private boolean importing;
	private boolean importSuspended;
	private boolean collectOnly;
	private ImportAsyncTask importTask;

	public interface SettingsImportListener {
		void onSettingsImportFinished(boolean succeed, boolean empty, @NonNull List<SettingsItem> items);
	}

	public interface SettingsExportListener {
		void onSettingsExportFinished(@NonNull File file, boolean succeed);
	}

	public SettingsHelper(OsmandApplication app) {
		this.app = app;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
		if (importing && !collectOnly) {
			importTask.processNextItem();
		}
	}

	public void resetActivity(Activity activity) {
		if (this.activity == activity) {
			if (importing) {
				importTask.suspendImport();
				importSuspended = true;
			}
			this.activity = null;
		}
	}

	public boolean isImporting() {
		return importing;
	}

	public enum SettingsItemType {
		GLOBAL,
		PROFILE,
		PLUGIN,
		DATA,
		FILE,
		QUICK_ACTION,
		POI_UI_FILTERS,
		MAP_SOURCES,
		AVOID_ROADS
	}

	public abstract static class SettingsItem {

		private SettingsItemType type;

		boolean shouldReplace = false;

		SettingsItem(@NonNull SettingsItemType type) {
			this.type = type;
		}

		SettingsItem(@NonNull SettingsItemType type, @NonNull JSONObject json) throws JSONException {
			this.type = type;
			readFromJson(json);
		}

		@NonNull
		public SettingsItemType getType() {
			return type;
		}

		@NonNull
		public abstract String getName();

		@NonNull
		public abstract String getPublicName(@NonNull Context ctx);

		@NonNull
		public abstract String getFileName();

		public boolean shouldReadOnCollecting() {
			return false;
		}

		public void setShouldReplace(boolean shouldReplace) {
			this.shouldReplace = shouldReplace;
		}

		static SettingsItemType parseItemType(@NonNull JSONObject json) throws IllegalArgumentException, JSONException {
			return SettingsItemType.valueOf(json.getString("type"));
		}

		public boolean exists() {
			return false;
		}

		public void apply() {
			// non implemented
		}

		void readFromJson(@NonNull JSONObject json) throws JSONException {
		}

		void writeToJson(@NonNull JSONObject json) throws JSONException {
			json.put("type", type.name());
			json.put("name", getName());
		}

		String toJson() throws JSONException {
			JSONObject json = new JSONObject();
			writeToJson(json);
			return json.toString();
		}

		@NonNull
		abstract SettingsItemReader getReader();

		@NonNull
		abstract SettingsItemWriter getWriter();

		@Override
		public int hashCode() {
			return (getType().name() + getName()).hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof SettingsItem)) {
				return false;
			}

			SettingsItem item = (SettingsItem) other;
			return item.getType() == getType()
					&& item.getName().equals(getName())
					&& item.getFileName().equals(getFileName());
		}
	}

	public abstract static class CollectionSettingsItem<T> extends SettingsItem {

		protected List<T> items = new ArrayList<>();
		protected List<T> duplicateItems = new ArrayList<>();
		protected List<T> existingItems;

		CollectionSettingsItem(@NonNull SettingsItemType type, @NonNull List<T> items) {
			super(type);
			this.items = items;
		}

		CollectionSettingsItem(@NonNull SettingsItemType type, @NonNull JSONObject json) throws JSONException {
			super(type, json);
		}

		@NonNull
		public List<T> getItems() {
			return items;
		}

		@NonNull
		public List<T> excludeDuplicateItems() {
			if (!items.isEmpty()) {
				for (T item : items) {
					if (isDuplicate(item)) {
						duplicateItems.add(item);
					}
				}
			}
			items.removeAll(duplicateItems);
			return duplicateItems;
		}

		public abstract boolean isDuplicate(@NonNull T item);

		@NonNull
		public abstract T renameItem(@NonNull T item);
	}

	public abstract static class SettingsItemReader<T extends SettingsItem> {

		private T item;

		public SettingsItemReader(@NonNull T item) {
			this.item = item;
		}

		public abstract void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException;
	}

	public abstract static class SettingsItemWriter<T extends SettingsItem> {

		private T item;

		public SettingsItemWriter(T item) {
			this.item = item;
		}

		public T getItem() {
			return item;
		}

		public abstract boolean writeToStream(@NonNull OutputStream outputStream) throws IOException;
	}

	public abstract static class OsmandSettingsItem extends SettingsItem {

		private OsmandSettings settings;

		protected OsmandSettingsItem(@NonNull SettingsItemType type, @NonNull OsmandSettings settings) {
			super(type);
			this.settings = settings;
		}

		protected OsmandSettingsItem(@NonNull SettingsItemType type, @NonNull OsmandSettings settings, @NonNull JSONObject json) throws JSONException {
			super(type, json);
			this.settings = settings;
		}

		public OsmandSettings getSettings() {
			return settings;
		}
	}

	public abstract static class OsmandSettingsItemReader extends SettingsItemReader<OsmandSettingsItem> {

		private OsmandSettings settings;

		public OsmandSettingsItemReader(@NonNull OsmandSettingsItem item, @NonNull OsmandSettings settings) {
			super(item);
			this.settings = settings;
		}

		protected abstract void readPreferenceFromJson(@NonNull OsmandPreference<?> preference,
													   @NonNull JSONObject json) throws JSONException;

		@Override
		public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
			StringBuilder buf = new StringBuilder();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				String str;
				while ((str = in.readLine()) != null) {
					buf.append(str);
				}
			} catch (IOException e) {
				throw new IOException("Cannot read json body", e);
			}
			String jsonStr = buf.toString();
			if (Algorithms.isEmpty(jsonStr)) {
				throw new IllegalArgumentException("Cannot find json body");
			}
			final JSONObject json;
			try {
				json = new JSONObject(jsonStr);
			} catch (JSONException e) {
				throw new IllegalArgumentException("Json parse error", e);
			}
			settings.getContext().runInUIThread(new Runnable() {
				@Override
				public void run() {
					Map<String, OsmandPreference<?>> prefs = settings.getRegisteredPreferences();
					Iterator<String> iter = json.keys();
					while (iter.hasNext()) {
						String key = iter.next();
						OsmandPreference<?> p = prefs.get(key);
						if (p != null) {
							try {
								readPreferenceFromJson(p, json);
							} catch (Exception e) {
								LOG.error("Failed to read preference: " + key, e);
							}
						} else {
							LOG.warn("No preference while importing settings: " + key);
						}
					}
				}
			});
		}
	}

	public abstract static class OsmandSettingsItemWriter extends SettingsItemWriter<OsmandSettingsItem> {

		private OsmandSettings settings;

		public OsmandSettingsItemWriter(OsmandSettingsItem item, OsmandSettings settings) {
			super(item);
			this.settings = settings;
		}

		protected abstract void writePreferenceToJson(@NonNull OsmandPreference<?> preference,
													  @NonNull JSONObject json) throws JSONException;

		@Override
		public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
			JSONObject json = new JSONObject();
			Map<String, OsmandPreference<?>> prefs = settings.getRegisteredPreferences();
			for (OsmandPreference<?> pref : prefs.values()) {
				try {
					writePreferenceToJson(pref, json);
				} catch (JSONException e) {
					LOG.error("Failed to write preference: " + pref.getId(), e);
				}
			}
			if (json.length() > 0) {
				try {
					String s = json.toString(2);
					outputStream.write(s.getBytes("UTF-8"));
				} catch (JSONException e) {
					LOG.error("Failed to write json to stream", e);
				}
				return true;
			}
			return false;
		}
	}

	public static class GlobalSettingsItem extends OsmandSettingsItem {

		public GlobalSettingsItem(@NonNull OsmandSettings settings) {
			super(SettingsItemType.GLOBAL, settings);
		}

		@NonNull
		@Override
		public String getName() {
			return "general_settings";
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return ctx.getString(R.string.general_settings_2);
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".json";
		}

		@Override
		public boolean exists() {
			return true;
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new OsmandSettingsItemReader(this, getSettings()) {
				@Override
				protected void readPreferenceFromJson(@NonNull OsmandPreference<?> preference, @NonNull JSONObject json) throws JSONException {
					preference.readFromJson(json, null);
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new OsmandSettingsItemWriter(this, getSettings()) {
				@Override
				protected void writePreferenceToJson(@NonNull OsmandPreference<?> preference, @NonNull JSONObject json) throws JSONException {
					preference.writeToJson(json, null);
				}
			};
		}
	}

	public static class ProfileSettingsItem extends OsmandSettingsItem {

		private OsmandApplication app;
		private ApplicationMode appMode;
		private ApplicationModeBuilder builder;
		private ApplicationModeBean modeBean;
		private Set<String> appModeBeanPrefsIds;

		public ProfileSettingsItem(@NonNull OsmandApplication app, @NonNull ApplicationMode appMode) {
			super(SettingsItemType.PROFILE, app.getSettings());
			this.app = app;
			this.appMode = appMode;
			appModeBeanPrefsIds = new HashSet<>(Arrays.asList(app.getSettings().appModeBeanPrefsIds));
		}

		public ProfileSettingsItem(@NonNull OsmandApplication app, @NonNull ApplicationModeBean modeBean) {
			super(SettingsItemType.PROFILE, app.getSettings());
			this.app = app;
			this.modeBean = modeBean;
			builder = ApplicationMode.fromModeBean(app, modeBean);
			appMode = builder.getApplicationMode();
			appModeBeanPrefsIds = new HashSet<>(Arrays.asList(app.getSettings().appModeBeanPrefsIds));
		}

		public ProfileSettingsItem(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.PROFILE, app.getSettings(), json);
			this.app = app;
			readFromJson(app.getSettings().getContext(), json);
			appModeBeanPrefsIds = new HashSet<>(Arrays.asList(app.getSettings().appModeBeanPrefsIds));
		}

		public ApplicationMode getAppMode() {
			return appMode;
		}

		public ApplicationModeBean getModeBean() {
			return modeBean;
		}

		@NonNull
		@Override
		public String getName() {
			return appMode.getStringKey();
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			if (appMode.isCustomProfile()) {
				return modeBean.userProfileName;
			} else if (appMode.getNameKeyResource() != -1) {
				return ctx.getString(appMode.getNameKeyResource());
			} else {
				return getName();
			}
		}

		@NonNull
		@Override
		public String getFileName() {
			return "profile_" + getName() + ".json";
		}

		void readFromJson(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			String appModeJson = json.getString("appMode");
			modeBean = ApplicationMode.fromJson(appModeJson);
			builder = ApplicationMode.fromModeBean(app, modeBean);
			ApplicationMode appMode = builder.getApplicationMode();
			if (!appMode.isCustomProfile()) {
				appMode = ApplicationMode.valueOfStringKey(appMode.getStringKey(), appMode);
			}
			this.appMode = appMode;
		}

		@Override
		public boolean exists() {
			return builder != null && ApplicationMode.valueOfStringKey(getName(), null) != null;
		}

		private void renameProfile() {
			int number = 0;
			while (true) {
				number++;
				String key = modeBean.stringKey + "_" + number;
				if (ApplicationMode.valueOfStringKey(key, null) == null) {
					modeBean.stringKey = key;
					modeBean.userProfileName = modeBean.userProfileName + "_" + number;
					break;
				}
			}
		}

		@Override
		public void apply() {
			if (!appMode.isCustomProfile() && !shouldReplace) {
				ApplicationMode parent = ApplicationMode.valueOfStringKey(modeBean.stringKey, null);
				renameProfile();
				ApplicationMode.ApplicationModeBuilder builder = ApplicationMode
						.createCustomMode(parent, modeBean.stringKey, app)
						.setIconResName(modeBean.iconName)
						.setUserProfileName(modeBean.userProfileName)
						.setRoutingProfile(modeBean.routingProfile)
						.setRouteService(modeBean.routeService)
						.setIconColor(modeBean.iconColor)
						.setLocationIcon(modeBean.locIcon)
						.setNavigationIcon(modeBean.navIcon);
				app.getSettings().copyPreferencesFromProfile(parent, builder.getApplicationMode());
				appMode = ApplicationMode.saveProfile(builder, app);
			} else if (!shouldReplace && exists()) {
				renameProfile();
				builder = ApplicationMode.fromModeBean(app, modeBean);
				appMode = ApplicationMode.saveProfile(builder, app);
			} else {
				builder = ApplicationMode.fromModeBean(app, modeBean);
				appMode = ApplicationMode.saveProfile(builder, app);
			}
			ApplicationMode.changeProfileAvailability(appMode, true, app);
		}

		@Override
		void writeToJson(@NonNull JSONObject json) throws JSONException {
			super.writeToJson(json);
			json.put("appMode", new JSONObject(appMode.toJson()));
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new OsmandSettingsItemReader(this, getSettings()) {
				@Override
				protected void readPreferenceFromJson(@NonNull OsmandPreference<?> preference, @NonNull JSONObject json) throws JSONException {
					if (!appModeBeanPrefsIds.contains(preference.getId())) {
						preference.readFromJson(json, appMode);
					}
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new OsmandSettingsItemWriter(this, getSettings()) {
				@Override
				protected void writePreferenceToJson(@NonNull OsmandPreference<?> preference, @NonNull JSONObject json) throws JSONException {
					if (!appModeBeanPrefsIds.contains(preference.getId())) {
						preference.writeToJson(json, appMode);
					}
				}
			};
		}
	}

	public abstract static class StreamSettingsItemReader extends SettingsItemReader<StreamSettingsItem> {

		public StreamSettingsItemReader(@NonNull StreamSettingsItem item) {
			super(item);
		}

	}

	public static class StreamSettingsItemWriter extends SettingsItemWriter<StreamSettingsItem> {

		public StreamSettingsItemWriter(StreamSettingsItem item) {
			super(item);
		}

		@Override
		public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
			boolean hasData = false;
			InputStream is = getItem().inputStream;
			if (is != null) {
				byte[] data = new byte[BUFFER];
				int count;
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					outputStream.write(data, 0, count);
					if (!hasData) {
						hasData = true;
					}
				}
				Algorithms.closeStream(is);
			}
			return hasData;
		}
	}

	public abstract static class StreamSettingsItem extends SettingsItem {

		@Nullable
		private InputStream inputStream;
		protected String name;

		public StreamSettingsItem(@NonNull SettingsItemType type, @NonNull String name) {
			super(type);
			this.name = name;
		}

		StreamSettingsItem(@NonNull SettingsItemType type, @NonNull JSONObject json) throws JSONException {
			super(type, json);
		}

		public StreamSettingsItem(@NonNull SettingsItemType type, @NonNull InputStream inputStream, @NonNull String name) {
			super(type);
			this.inputStream = inputStream;
			this.name = name;
		}

		@Nullable
		public InputStream getInputStream() {
			return inputStream;
		}

		protected void setInputStream(@Nullable InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@NonNull
		@Override
		public String getName() {
			return name;
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return getName();
		}

		@Override
		void readFromJson(@NonNull JSONObject json) throws JSONException {
			super.readFromJson(json);
			name = json.getString("name");
		}

		@NonNull
		@Override
		public SettingsItemWriter getWriter() {
			return new StreamSettingsItemWriter(this);
		}
	}

	public static class DataSettingsItem extends StreamSettingsItem {

		@Nullable
		private byte[] data;

		public DataSettingsItem(@NonNull String name) {
			super(SettingsItemType.DATA, name);
		}

		DataSettingsItem(@NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.DATA, json);
		}

		public DataSettingsItem(@NonNull byte[] data, @NonNull String name) {
			super(SettingsItemType.DATA, name);
			this.data = data;
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".dat";
		}

		@Nullable
		public byte[] getData() {
			return data;
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new StreamSettingsItemReader(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					int nRead;
					byte[] data = new byte[BUFFER];
					while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}

					buffer.flush();
					DataSettingsItem.this.data = buffer.toByteArray();
				}
			};
		}

		@NonNull
		@Override
		public SettingsItemWriter getWriter() {
			setInputStream(new ByteArrayInputStream(data));
			return super.getWriter();
		}
	}

	public static class FileSettingsItem extends StreamSettingsItem {

		private File file;

		public FileSettingsItem(@NonNull OsmandApplication app, @NonNull File file) {
			super(SettingsItemType.FILE, file.getPath().replace(app.getAppPath(null).getPath(), ""));
			this.file = file;
		}

		FileSettingsItem(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.FILE, json);
			this.file = new File(app.getAppPath(null), name);
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName();
		}

		public File getFile() {
			return file;
		}

		@Override
		public boolean exists() {
			return file.exists();
		}

		private File renameFile(File file) {
			int number = 0;
			String path = file.getAbsolutePath();
			while (true) {
				number++;
				String copyName = path.replaceAll(file.getName(), file.getName().replaceFirst("[.]", "_" + number + "."));
				File copyFile = new File(copyName);
				if (!copyFile.exists()) {
					return copyFile;
				}
			}
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new StreamSettingsItemReader(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					OutputStream output;
					if (!file.exists() || shouldReplace) {
						output = new FileOutputStream(file);
					} else {
						output = new FileOutputStream(renameFile(file));
					}
					byte[] buffer = new byte[BUFFER];
					int count;
					try {
						while ((count = inputStream.read(buffer)) != -1) {
							output.write(buffer, 0, count);
						}
						output.flush();
					} finally {
						Algorithms.closeStream(output);
					}
				}
			};
		}

		@NonNull
		@Override
		public SettingsItemWriter getWriter() {
			try {
				setInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				LOG.error("Failed to set input stream from file: " + file.getName(), e);
			}
			return super.getWriter();
		}
	}

	public static class QuickActionSettingsItem extends CollectionSettingsItem<QuickAction> {

		private OsmandApplication app;
		private QuickActionRegistry actionRegistry;

		public QuickActionSettingsItem(@NonNull OsmandApplication app,
									   @NonNull List<QuickAction> items) {
			super(SettingsItemType.QUICK_ACTION, items);
			this.app = app;
			actionRegistry = app.getQuickActionRegistry();
			existingItems = actionRegistry.getQuickActions();
		}

		QuickActionSettingsItem(@NonNull OsmandApplication app,
								@NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.QUICK_ACTION, json);
			this.app = app;
			actionRegistry = app.getQuickActionRegistry();
			existingItems = actionRegistry.getQuickActions();
		}

		@Override
		public boolean isDuplicate(@NonNull QuickAction item) {
			return !actionRegistry.isNameUnique(item, app);
		}

		@NonNull
		@Override
		public QuickAction renameItem(@NonNull QuickAction item) {
			return actionRegistry.generateUniqueName(item, app);
		}

		@Override
		public void apply() {
			if (!items.isEmpty() || !duplicateItems.isEmpty()) {
				List<QuickAction> newActions = new ArrayList<>(existingItems);
				if (!duplicateItems.isEmpty()) {
					if (shouldReplace) {
						for (QuickAction duplicateItem : duplicateItems) {
							for (QuickAction savedAction : existingItems) {
								if (duplicateItem.getName(app).equals(savedAction.getName(app))) {
									newActions.remove(savedAction);
								}
							}
						}
					} else {
						for (QuickAction duplicateItem : duplicateItems) {
							renameItem(duplicateItem);
						}
					}
					newActions.addAll(duplicateItems);
				}
				newActions.addAll(items);
				actionRegistry.updateQuickActions(newActions);
			}
		}

		@Override
		public boolean shouldReadOnCollecting() {
			return true;
		}

		@NonNull
		@Override
		public String getName() {
			return "quick_actions";
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return "quick_actions";
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".json";
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new SettingsItemReader<QuickActionSettingsItem>(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					StringBuilder buf = new StringBuilder();
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						String str;
						while ((str = in.readLine()) != null) {
							buf.append(str);
						}
					} catch (IOException e) {
						throw new IOException("Cannot read json body", e);
					}
					String jsonStr = buf.toString();
					if (Algorithms.isEmpty(jsonStr)) {
						throw new IllegalArgumentException("Cannot find json body");
					}
					final JSONObject json;
					try {
						Gson gson = new Gson();
						Type type = new TypeToken<HashMap<String, String>>() {
						}.getType();
						json = new JSONObject(jsonStr);
						JSONArray itemsJson = json.getJSONArray("items");
						for (int i = 0; i < itemsJson.length(); i++) {
							JSONObject object = itemsJson.getJSONObject(i);
							String name = object.getString("name");
							int actionType = object.getInt("type");
							String paramsString = object.getString("params");
							HashMap<String, String> params = gson.fromJson(paramsString, type);
							QuickAction quickAction = new QuickAction(actionType);
							if (!name.isEmpty()) {
								quickAction.setName(name);
							}
							quickAction.setParams(params);
							items.add(quickAction);
						}
					} catch (JSONException e) {
						throw new IllegalArgumentException("Json parse error", e);
					}
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new SettingsItemWriter<QuickActionSettingsItem>(this) {
				@Override
				public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
					JSONObject json = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					Gson gson = new Gson();
					Type type = new TypeToken<HashMap<String, String>>() {
					}.getType();
					if (!items.isEmpty()) {
						try {
							for (QuickAction action : items) {
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("name", action.hasCustomName(app)
										? action.getName(app) : "");
								jsonObject.put("type", action.getType());
								jsonObject.put("params", gson.toJson(action.getParams(), type));
								jsonArray.put(jsonObject);
							}
							json.put("items", jsonArray);
						} catch (JSONException e) {
							LOG.error("Failed write to json", e);
						}
					}
					if (json.length() > 0) {
						try {
							String s = json.toString(2);
							outputStream.write(s.getBytes("UTF-8"));
						} catch (JSONException e) {
							LOG.error("Failed to write json to stream", e);
						}
						return true;
					}
					return false;
				}
			};
		}
	}

	public static class PoiUiFilterSettingsItem extends CollectionSettingsItem<PoiUIFilter> {

		private OsmandApplication app;

		public PoiUiFilterSettingsItem(@NonNull OsmandApplication app, @NonNull List<PoiUIFilter> items) {
			super(SettingsItemType.POI_UI_FILTERS, items);
			this.app = app;
			existingItems = app.getPoiFilters().getUserDefinedPoiFilters(false);
		}

		PoiUiFilterSettingsItem(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.POI_UI_FILTERS, json);
			this.app = app;
			existingItems = app.getPoiFilters().getUserDefinedPoiFilters(false);
		}

		@Override
		public void apply() {
			if (!items.isEmpty() || !duplicateItems.isEmpty()) {
				for (PoiUIFilter duplicate : duplicateItems) {
					items.add(shouldReplace ? duplicate : renameItem(duplicate));
				}
				for (PoiUIFilter filter : items) {
					app.getPoiFilters().createPoiFilter(filter, false);
				}
				app.getSearchUICore().refreshCustomPoiFilters();
			}
		}

		@Override
		public boolean isDuplicate(@NonNull PoiUIFilter item) {
			String savedName = item.getName();
			for (PoiUIFilter filter : existingItems) {
				if (filter.getName().equals(savedName)) {
					return true;
				}
			}
			return false;
		}

		@NonNull
		@Override
		public PoiUIFilter renameItem(@NonNull PoiUIFilter item) {
			int number = 0;
			while (true) {
				number++;
				PoiUIFilter renamedItem = new PoiUIFilter(item,
						item.getName() + "_" + number,
						item.getFilterId() + "_" + number);
				if (!isDuplicate(renamedItem)) {
					return renamedItem;
				}
			}
		}

		@NonNull
		@Override
		public String getName() {
			return "poi_ui_filters";
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return "poi_ui_filters";
		}

		@Override
		public boolean shouldReadOnCollecting() {
			return true;
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".json";
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new SettingsItemReader<PoiUiFilterSettingsItem>(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					StringBuilder buf = new StringBuilder();
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						String str;
						while ((str = in.readLine()) != null) {
							buf.append(str);
						}
					} catch (IOException e) {
						throw new IOException("Cannot read json body", e);
					}
					String jsonStr = buf.toString();
					if (Algorithms.isEmpty(jsonStr)) {
						throw new IllegalArgumentException("Cannot find json body");
					}
					final JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						JSONArray jsonArray = json.getJSONArray("items");
						Gson gson = new Gson();
						Type type = new TypeToken<HashMap<String, LinkedHashSet<String>>>() {
						}.getType();
						MapPoiTypes poiTypes = app.getPoiTypes();
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject object = jsonArray.getJSONObject(i);
							String name = object.getString("name");
							String filterId = object.getString("filterId");
							String acceptedTypesString = object.getString("acceptedTypes");
							HashMap<String, LinkedHashSet<String>> acceptedTypes = gson.fromJson(acceptedTypesString, type);
							Map<PoiCategory, LinkedHashSet<String>> acceptedTypesDone = new HashMap<>();
							for (Map.Entry<String, LinkedHashSet<String>> mapItem : acceptedTypes.entrySet()) {
								final PoiCategory a = poiTypes.getPoiCategoryByName(mapItem.getKey());
								acceptedTypesDone.put(a, mapItem.getValue());
							}
							PoiUIFilter filter = new PoiUIFilter(name, filterId, acceptedTypesDone, app);
							items.add(filter);
						}
					} catch (JSONException e) {
						throw new IllegalArgumentException("Json parse error", e);
					}
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new SettingsItemWriter<PoiUiFilterSettingsItem>(this) {
				@Override
				public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
					JSONObject json = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					Gson gson = new Gson();
					Type type = new TypeToken<HashMap<PoiCategory, LinkedHashSet<String>>>() {
					}.getType();
					if (!items.isEmpty()) {
						try {
							for (PoiUIFilter filter : items) {
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("name", filter.getName());
								jsonObject.put("filterId", filter.getFilterId());
								jsonObject.put("acceptedTypes", gson.toJson(filter.getAcceptedTypes(), type));
								jsonArray.put(jsonObject);
							}
							json.put("items", jsonArray);
						} catch (JSONException e) {
							LOG.error("Failed write to json", e);
						}
					}
					if (json.length() > 0) {
						try {
							String s = json.toString(2);
							outputStream.write(s.getBytes("UTF-8"));
						} catch (JSONException e) {
							LOG.error("Failed to write json to stream", e);
						}
						return true;
					}
					return false;
				}
			};
		}
	}

	public static class MapSourcesSettingsItem extends CollectionSettingsItem<ITileSource> {

		private OsmandApplication app;
		private List<String> existingItemsNames;

		public MapSourcesSettingsItem(@NonNull OsmandApplication app, @NonNull List<ITileSource> items) {
			super(SettingsItemType.MAP_SOURCES, items);
			this.app = app;
			existingItemsNames = new ArrayList<>(app.getSettings().getTileSourceEntries().values());
		}

		MapSourcesSettingsItem(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.MAP_SOURCES, json);
			this.app = app;
			existingItemsNames = new ArrayList<>(app.getSettings().getTileSourceEntries().values());
		}

		@Override
		public void apply() {
			if (!items.isEmpty() || !duplicateItems.isEmpty()) {
				if (shouldReplace) {
					for (ITileSource tileSource : duplicateItems) {
						if (tileSource instanceof SQLiteTileSource) {
							File f = app.getAppPath(IndexConstants.TILES_INDEX_DIR + tileSource.getName() + IndexConstants.SQLITE_EXT);
							if (f != null && f.exists()) {
								if (f.delete()) {
									items.add(tileSource);
								}
							}
						} else if (tileSource instanceof TileSourceManager.TileSourceTemplate) {
							File f = app.getAppPath(IndexConstants.TILES_INDEX_DIR + tileSource.getName());
							if (f != null && f.exists() && f.isDirectory()) {
								if (f.delete()) {
									items.add(tileSource);
								}
							}
						}
					}
				} else {
					for (ITileSource tileSource : duplicateItems) {
						items.add(renameItem(tileSource));
					}
				}
				for (ITileSource tileSource : items) {
					if (tileSource instanceof TileSourceManager.TileSourceTemplate) {
						app.getSettings().installTileSource((TileSourceManager.TileSourceTemplate) tileSource);
					} else if (tileSource instanceof SQLiteTileSource) {
						((SQLiteTileSource) tileSource).createDataBase();
					}
				}
			}
		}

		@NonNull
		@Override
		public ITileSource renameItem(@NonNull ITileSource item) {
			int number = 0;
			while (true) {
				number++;
				if (item instanceof SQLiteTileSource) {
					SQLiteTileSource oldItem = (SQLiteTileSource) item;
					SQLiteTileSource renamedItem = new SQLiteTileSource(
							oldItem,
							oldItem.getName() + "_" + number,
							app);
					if (!isDuplicate(renamedItem)) {
						return renamedItem;
					}
				} else if (item instanceof TileSourceManager.TileSourceTemplate) {
					TileSourceManager.TileSourceTemplate oldItem = (TileSourceManager.TileSourceTemplate) item;
					oldItem.setName(oldItem.getName() + "_" + number);
					if (!isDuplicate(oldItem)) {
						return oldItem;
					}
				}
			}
		}

		@Override
		public boolean isDuplicate(@NonNull ITileSource item) {
			for (String name : existingItemsNames) {
				if (name.equals(item.getName())) {
					return true;
				}
			}
			return false;
		}

		@NonNull
		@Override
		public String getName() {
			return "map_sources";
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return "map_sources";
		}

		@Override
		public boolean shouldReadOnCollecting() {
			return true;
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".json";
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new SettingsItemReader<MapSourcesSettingsItem>(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					StringBuilder buf = new StringBuilder();
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						String str;
						while ((str = in.readLine()) != null) {
							buf.append(str);
						}
					} catch (IOException e) {
						throw new IOException("Cannot read json body", e);
					}
					String jsonStr = buf.toString();
					if (Algorithms.isEmpty(jsonStr)) {
						throw new IllegalArgumentException("Cannot find json body");
					}
					final JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						JSONArray jsonArray = json.getJSONArray("items");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject object = jsonArray.getJSONObject(i);
							boolean sql = object.optBoolean("sql");
							String name = object.optString("name");
							int minZoom = object.optInt("minZoom");
							int maxZoom = object.optInt("maxZoom");
							String url = object.optString("url");
							String randoms = object.optString("randoms");
							boolean ellipsoid = object.optBoolean("ellipsoid", false);
							boolean invertedY = object.optBoolean("inverted_y", false);
							String referer = object.optString("referer");
							boolean timesupported = object.optBoolean("timesupported", false);
							long expire = object.optLong("expire");
							boolean inversiveZoom = object.optBoolean("inversiveZoom", false);
							String ext = object.optString("ext");
							int tileSize = object.optInt("tileSize");
							int bitDensity = object.optInt("bitDensity");
							int avgSize = object.optInt("avgSize");
							String rule = object.optString("rule");

							ITileSource template;
							if (!sql) {
								template = new TileSourceManager.TileSourceTemplate(name, url, ext, maxZoom, minZoom, tileSize, bitDensity, avgSize);
							} else {
								template = new SQLiteTileSource(app, name, minZoom, maxZoom, url, randoms, ellipsoid, invertedY, referer, timesupported, expire, inversiveZoom);
							}
							items.add(template);
						}
					} catch (JSONException e) {
						throw new IllegalArgumentException("Json parse error", e);
					}
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new SettingsItemWriter<MapSourcesSettingsItem>(this) {
				@Override
				public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
					JSONObject json = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					if (!items.isEmpty()) {
						try {
							for (ITileSource template : items) {
								JSONObject jsonObject = new JSONObject();
								boolean sql = template instanceof SQLiteTileSource;
								jsonObject.put("sql", sql);
								jsonObject.put("name", template.getName());
								jsonObject.put("minZoom", template.getMinimumZoomSupported());
								jsonObject.put("maxZoom", template.getMaximumZoomSupported());
								jsonObject.put("url", template.getUrlTemplate());
								jsonObject.put("randoms", template.getRandoms());
								jsonObject.put("ellipsoid", template.isEllipticYTile());
								jsonObject.put("inverted_y", template.isInvertedYTile());
								jsonObject.put("referer", template.getReferer());
								jsonObject.put("timesupported", template.isTimeSupported());
								jsonObject.put("expire", template.getExpirationTimeMillis());
								jsonObject.put("inversiveZoom", template.getInversiveZoom());
								jsonObject.put("ext", template.getTileFormat());
								jsonObject.put("tileSize", template.getTileSize());
								jsonObject.put("bitDensity", template.getBitDensity());
								jsonObject.put("avgSize", template.getAvgSize());
								jsonObject.put("rule", template.getRule());
								jsonArray.put(jsonObject);
							}
							json.put("items", jsonArray);

						} catch (JSONException e) {
							LOG.error("Failed write to json", e);
						}
					}
					if (json.length() > 0) {
						try {
							String s = json.toString(2);
							outputStream.write(s.getBytes("UTF-8"));
						} catch (JSONException e) {
							LOG.error("Failed to write json to stream", e);
						}
						return true;
					}
					return false;
				}
			};
		}
	}

	public static class AvoidRoadsSettingsItem extends CollectionSettingsItem<AvoidRoadInfo> {

		private OsmandApplication app;
		private OsmandSettings settings;
		private AvoidSpecificRoads specificRoads;

		public AvoidRoadsSettingsItem(@NonNull OsmandApplication app, @NonNull List<AvoidRoadInfo> items) {
			super(SettingsItemType.AVOID_ROADS, items);
			this.app = app;
			settings = app.getSettings();
			specificRoads = app.getAvoidSpecificRoads();
			existingItems = new ArrayList<>(specificRoads.getImpassableRoads().values());
		}

		AvoidRoadsSettingsItem(@NonNull OsmandApplication app, @NonNull JSONObject json) throws JSONException {
			super(SettingsItemType.AVOID_ROADS, json);
			this.app = app;
			settings = app.getSettings();
			specificRoads = app.getAvoidSpecificRoads();
			existingItems = new ArrayList<>(specificRoads.getImpassableRoads().values());
		}

		@NonNull
		@Override
		public String getName() {
			return "avoid_roads";
		}

		@NonNull
		@Override
		public String getPublicName(@NonNull Context ctx) {
			return "avoid_roads";
		}

		@NonNull
		@Override
		public String getFileName() {
			return getName() + ".json";
		}

		@Override
		public void apply() {
			if (!items.isEmpty() || !duplicateItems.isEmpty()) {
				for (AvoidRoadInfo duplicate : duplicateItems) {
					if (shouldReplace) {
						LatLon latLon = new LatLon(duplicate.latitude, duplicate.longitude);
						if (settings.removeImpassableRoad(latLon)) {
							settings.addImpassableRoad(duplicate);
						}
					} else {
						settings.addImpassableRoad(renameItem(duplicate));
					}
				}
				for (AvoidRoadInfo avoidRoad : items) {
					settings.addImpassableRoad(avoidRoad);
				}
				specificRoads.loadImpassableRoads();
				specificRoads.initRouteObjects(true);
			}
		}

		@Override
		public boolean isDuplicate(@NonNull AvoidRoadInfo item) {
			return existingItems.contains(item);
		}

		@Override
		public boolean shouldReadOnCollecting() {
			return true;
		}

		@NonNull
		@Override
		public AvoidRoadInfo renameItem(@NonNull AvoidRoadInfo item) {
			int number = 0;
			while (true) {
				number++;
				AvoidRoadInfo renamedItem = new AvoidRoadInfo();
				renamedItem.name = item.name + "_" + number;
				if (!isDuplicate(renamedItem)) {
					renamedItem.id = item.id;
					renamedItem.latitude = item.latitude;
					renamedItem.longitude = item.longitude;
					renamedItem.appModeKey = item.appModeKey;
					return renamedItem;
				}
			}
		}

		@NonNull
		@Override
		SettingsItemReader getReader() {
			return new SettingsItemReader<AvoidRoadsSettingsItem>(this) {
				@Override
				public void readFromStream(@NonNull InputStream inputStream) throws IOException, IllegalArgumentException {
					StringBuilder buf = new StringBuilder();
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						String str;
						while ((str = in.readLine()) != null) {
							buf.append(str);
						}
					} catch (IOException e) {
						throw new IOException("Cannot read json body", e);
					}
					String jsonStr = buf.toString();
					if (Algorithms.isEmpty(jsonStr)) {
						throw new IllegalArgumentException("Cannot find json body");
					}
					final JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						JSONArray jsonArray = json.getJSONArray("items");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject object = jsonArray.getJSONObject(i);
							double latitude = object.optDouble("latitude");
							double longitude = object.optDouble("longitude");
							String name = object.optString("name");
							String appModeKey = object.optString("appModeKey");
							AvoidRoadInfo roadInfo = new AvoidRoadInfo();
							roadInfo.id = 0;
							roadInfo.latitude = latitude;
							roadInfo.longitude = longitude;
							roadInfo.name = name;
							if (ApplicationMode.valueOfStringKey(appModeKey, null) != null) {
								roadInfo.appModeKey = appModeKey;
							} else {
								roadInfo.appModeKey = app.getRoutingHelper().getAppMode().getStringKey();
							}
							items.add(roadInfo);
						}
					} catch (JSONException e) {
						throw new IllegalArgumentException("Json parse error", e);
					}
				}
			};
		}

		@NonNull
		@Override
		SettingsItemWriter getWriter() {
			return new SettingsItemWriter<AvoidRoadsSettingsItem>(this) {
				@Override
				public boolean writeToStream(@NonNull OutputStream outputStream) throws IOException {
					JSONObject json = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					if (!items.isEmpty()) {
						try {
							for (AvoidRoadInfo avoidRoad : items) {
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("latitude", avoidRoad.latitude);
								jsonObject.put("longitude", avoidRoad.longitude);
								jsonObject.put("name", avoidRoad.name);
								jsonObject.put("appModeKey", avoidRoad.appModeKey);
								jsonArray.put(jsonObject);
							}
							json.put("items", jsonArray);
						} catch (JSONException e) {
							LOG.error("Failed write to json", e);
						}
					}
					if (json.length() > 0) {
						try {
							String s = json.toString(2);
							outputStream.write(s.getBytes("UTF-8"));
						} catch (JSONException e) {
							LOG.error("Failed to write json to stream", e);
						}
						return true;
					}
					return false;
				}
			};
		}
	}

	private static class SettingsItemsFactory {

		private OsmandApplication app;
		private List<SettingsItem> items = new ArrayList<>();

		SettingsItemsFactory(OsmandApplication app, String jsonStr) throws IllegalArgumentException, JSONException {
			this.app = app;
			JSONObject json = new JSONObject(jsonStr);
			JSONArray itemsJson = json.getJSONArray("items");
			for (int i = 0; i < itemsJson.length(); i++) {
				JSONObject itemJson = itemsJson.getJSONObject(i);
				SettingsItem item;
				try {
					item = createItem(itemJson);
					if (item != null) {
						items.add(item);
					}
				} catch (IllegalArgumentException e) {
					LOG.error("Error creating item from json: " + itemJson, e);
				}
			}
			if (items.size() == 0) {
				throw new IllegalArgumentException("No items");
			}
		}

		@NonNull
		public List<SettingsItem> getItems() {
			return items;
		}

		@Nullable
		public SettingsItem getItemByFileName(@NonNull String fileName) {
			for (SettingsItem item : items) {
				if (item.getFileName().equals(fileName)) {
					return item;
				}
			}
			return null;
		}

		@Nullable
		private SettingsItem createItem(@NonNull JSONObject json) throws IllegalArgumentException, JSONException {
			SettingsItem item = null;
			SettingsItemType type = SettingsItem.parseItemType(json);
			OsmandSettings settings = app.getSettings();
			switch (type) {
				case GLOBAL:
					item = new GlobalSettingsItem(settings);
					break;
				case PROFILE:
					item = new ProfileSettingsItem(app, json);
					break;
				case PLUGIN:
					break;
				case DATA:
					item = new DataSettingsItem(json);
					break;
				case FILE:
					item = new FileSettingsItem(app, json);
					break;
				case QUICK_ACTION:
					item = new QuickActionSettingsItem(app, json);
					break;
				case POI_UI_FILTERS:
					item = new PoiUiFilterSettingsItem(app, json);
					break;
				case MAP_SOURCES:
					item = new MapSourcesSettingsItem(app, json);
					break;
				case AVOID_ROADS:
					item = new AvoidRoadsSettingsItem(app, json);
					break;
			}
			return item;
		}
	}

	private static class SettingsExporter {

		private Map<String, SettingsItem> items;
		private Map<String, String> additionalParams;

		SettingsExporter() {
			items = new LinkedHashMap<>();
			additionalParams = new LinkedHashMap<>();
		}

		void addSettingsItem(SettingsItem item) throws IllegalArgumentException {
			if (items.containsKey(item.getName())) {
				throw new IllegalArgumentException("Already has such item: " + item.getName());
			}
			items.put(item.getName(), item);
		}

		void addAdditionalParam(String key, String value) {
			additionalParams.put(key, value);
		}

		void exportSettings(File file) throws JSONException, IOException {
			JSONObject json = new JSONObject();
			json.put("osmand_settings_version", OsmandSettings.VERSION);
			for (Map.Entry<String, String> param : additionalParams.entrySet()) {
				json.put(param.getKey(), param.getValue());
			}
			JSONArray itemsJson = new JSONArray();
			for (SettingsItem item : items.values()) {
				itemsJson.put(new JSONObject(item.toJson()));
			}
			json.put("items", itemsJson);
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file), BUFFER);
			ZipOutputStream zos = new ZipOutputStream(os);
			try {
				ZipEntry entry = new ZipEntry("items.json");
				zos.putNextEntry(entry);
				zos.write(json.toString(2).getBytes("UTF-8"));
				zos.closeEntry();
				for (SettingsItem item : items.values()) {
					entry = new ZipEntry(item.getFileName());
					zos.putNextEntry(entry);
					item.getWriter().writeToStream(zos);
					zos.closeEntry();
				}
				zos.flush();
				zos.finish();
			} finally {
				Algorithms.closeStream(zos);
				Algorithms.closeStream(os);
			}
		}
	}

	private static class SettingsImporter {

		private OsmandApplication app;

		SettingsImporter(@NonNull OsmandApplication app) {
			this.app = app;
		}

		List<SettingsItem> collectItems(@NonNull File file) throws IllegalArgumentException, IOException {
			return processItems(file, null);
		}

		void importItems(@NonNull File file, @NonNull List<SettingsItem> items) throws IllegalArgumentException, IOException {
			processItems(file, items);
		}

		private List<SettingsItem> processItems(@NonNull File file, @Nullable List<SettingsItem> items) throws IllegalArgumentException, IOException {
			boolean collecting = items == null;
			if (collecting) {
				items = new ArrayList<>();
			} else {
				if (items.size() == 0) {
					throw new IllegalArgumentException("No items");
				}
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			InputStream ois = new BufferedInputStream(zis);
			try {
				ZipEntry entry = zis.getNextEntry();
				if (entry != null && entry.getName().equals("items.json")) {
					String itemsJson = null;
					try {
						itemsJson = Algorithms.readFromInputStream(ois).toString();
					} catch (IOException e) {
						LOG.error("Error reading items.json: " + itemsJson, e);
						throw new IllegalArgumentException("No items");
					} finally {
						zis.closeEntry();
					}
					if (collecting) {
						try {
							SettingsItemsFactory itemsFactory = new SettingsItemsFactory(app, itemsJson);
							items.addAll(itemsFactory.getItems());
						} catch (IllegalArgumentException e) {
							LOG.error("Error parsing items: " + itemsJson, e);
							throw new IllegalArgumentException("No items");
						} catch (JSONException e) {
							LOG.error("Error parsing items: " + itemsJson, e);
							throw new IllegalArgumentException("No items");
						}
					}
					while ((entry = zis.getNextEntry()) != null) {
						String fileName = entry.getName();
						SettingsItem item = null;
						for (SettingsItem settingsItem : items) {
							if (settingsItem != null && settingsItem.getFileName().equals(fileName)) {
								item = settingsItem;
								break;
							}
						}
						if (item != null && collecting && item.shouldReadOnCollecting()
								|| item != null && !collecting && !item.shouldReadOnCollecting()) {
							try {
								item.getReader().readFromStream(ois);
							} catch (IllegalArgumentException e) {
								LOG.error("Error reading item data: " + item.getName(), e);
							} catch (IOException e) {
								LOG.error("Error reading item data: " + item.getName(), e);
							} finally {
								zis.closeEntry();
							}
						}
					}
				} else {
					throw new IllegalArgumentException("No items found");
				}
			} catch (IOException ex) {
				LOG.error("Failed to read next entry", ex);
			} finally {
				Algorithms.closeStream(ois);
				Algorithms.closeStream(zis);
			}
			return items;
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class ImportAsyncTask extends AsyncTask<Void, Void, List<SettingsItem>> {

		private File file;
		private String latestChanges;
		private boolean askBeforeImport;
		private int version;

		private SettingsImportListener listener;
		private SettingsImporter importer;
		private List<SettingsItem> items = new ArrayList<>();
		private List<SettingsItem> processedItems = new ArrayList<>();
		private SettingsItem currentItem;
		private AlertDialog dialog;

		ImportAsyncTask(@NonNull File settingsFile, String latestChanges, int version, boolean askBeforeImport,
						@Nullable SettingsImportListener listener) {
			this.file = settingsFile;
			this.listener = listener;
			this.latestChanges = latestChanges;
			this.version = version;
			this.askBeforeImport = askBeforeImport;
			importer = new SettingsImporter(app);
			collectOnly = true;
		}

		ImportAsyncTask(@NonNull File settingsFile, @NonNull List<SettingsItem> items, String latestChanges, int version, @Nullable SettingsImportListener listener) {
			this.file = settingsFile;
			this.listener = listener;
			this.items = items;
			this.latestChanges = latestChanges;
			this.version = version;
			importer = new SettingsImporter(app);
			collectOnly = false;
		}

		@Override
		protected void onPreExecute() {
			if (importing) {
				finishImport(listener, false, false, items);
			}
			importing = true;
			importSuspended = false;
			importTask = this;
		}

		@Override
		protected List<SettingsItem> doInBackground(Void... voids) {
			if (collectOnly) {
				try {
					return importer.collectItems(file);
				} catch (IllegalArgumentException e) {
					LOG.error("Failed to collect items from: " + file.getName(), e);
				} catch (IOException e) {
					LOG.error("Failed to collect items from: " + file.getName(), e);
				}
			} else {
				return this.items;
			}
			return null;
		}

		@Override
		protected void onPostExecute(@Nullable List<SettingsItem> items) {
			if (items != null) {
				this.items = items;
			}
			if (collectOnly) {
				listener.onSettingsImportFinished(true, false, this.items);
			} else {
				if (items != null && items.size() > 0) {
					processNextItem();
				}
			}
		}

		private void processNextItem() {
			if (activity == null) {
				return;
			}
			if (items.size() == 0 && !importSuspended) {
				if (processedItems.size() > 0) {
					new ImportItemsAsyncTask(file, listener, processedItems).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					finishImport(listener, false, true, items);
				}
				return;
			}
			final SettingsItem item;
			if (importSuspended && currentItem != null) {
				item = currentItem;
			} else if (items.size() > 0) {
				item = items.remove(0);
				currentItem = item;
			} else {
				item = null;
			}
			importSuspended = false;
			if (item != null) {
				acceptItem(item);
			} else {
				processNextItem();
			}
		}

		private void suspendImport() {
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
		}

		private void acceptItem(SettingsItem item) {
			item.apply();
			processedItems.add(item);
			processNextItem();
		}

		public List<SettingsItem> getItems() {
			return this.items;
		}

		public File getFile() {
			return this.file;
		}
	}

	@Nullable
	public List<SettingsItem> getSettingsItems() {
		return this.importTask.getItems();
	}

	@Nullable
	public File getSettingsFile() {
		return this.importTask.getFile();
	}

	@SuppressLint("StaticFieldLeak")
	private class ImportItemsAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private SettingsImporter importer;
		private File file;
		private SettingsImportListener listener;
		private List<SettingsItem> items;

		ImportItemsAsyncTask(@NonNull File file,
							 @Nullable SettingsImportListener listener,
							 @NonNull List<SettingsItem> items) {
			importer = new SettingsImporter(app);
			this.file = file;
			this.listener = listener;
			this.items = items;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			try {
				importer.importItems(file, items);
				return true;
			} catch (IllegalArgumentException e) {
				LOG.error("Failed to import items from: " + file.getName(), e);
			} catch (IOException e) {
				LOG.error("Failed to import items from: " + file.getName(), e);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			finishImport(listener, success, false, items);
		}
	}

	private void finishImport(@Nullable SettingsImportListener listener, boolean success, boolean empty, @NonNull List<SettingsItem> items) {
		importing = false;
		importSuspended = false;
		importTask = null;
		if (listener != null) {
			listener.onSettingsImportFinished(success, empty, items);
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class ExportAsyncTask extends AsyncTask<Void, Void, Boolean> {

		private SettingsExporter exporter;
		private File file;
		private SettingsExportListener listener;
		private ProgressDialog progress;

		ExportAsyncTask(@NonNull File settingsFile,
						@Nullable SettingsExportListener listener,
						@NonNull List<SettingsItem> items) {
			this.file = settingsFile;
			this.listener = listener;
			this.exporter = new SettingsExporter();
			for (SettingsItem item : items) {
				exporter.addSettingsItem(item);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (activity != null) {
				progress = ProgressDialog.show(activity, app.getString(R.string.export_profile), app.getString(R.string.shared_string_preparing));
			}
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			try {
				exporter.exportSettings(file);
				return true;
			} catch (JSONException e) {
				LOG.error("Failed to export items to: " + file.getName(), e);
			} catch (IOException e) {
				LOG.error("Failed to export items to: " + file.getName(), e);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (activity != null) {
				progress.dismiss();
				if (listener != null) {
					listener.onSettingsExportFinished(file, success);
				}
			}
		}
	}

	public void importSettings(@NonNull File settingsFile, String latestChanges, int version,
							   boolean askBeforeImport, @Nullable SettingsImportListener listener) {
		new ImportAsyncTask(settingsFile, latestChanges, version, askBeforeImport, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void importSettings(@NonNull File settingsFile, @NonNull List<SettingsItem> items, String latestChanges, int version, @Nullable SettingsImportListener listener) {
		new ImportAsyncTask(settingsFile, items, latestChanges, version, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void exportSettings(@NonNull File fileDir, @NonNull String fileName,
							   @Nullable SettingsExportListener listener,
							   @NonNull List<SettingsItem> items) {
		new ExportAsyncTask(new File(fileDir, fileName + OSMAND_SETTINGS_FILE_EXT), listener, items)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void exportSettings(@NonNull File fileDir, @NonNull String fileName, @Nullable SettingsExportListener listener,
							   @NonNull SettingsItem... items) {
		exportSettings(fileDir, fileName, listener, new ArrayList<>(Arrays.asList(items)));
	}
}
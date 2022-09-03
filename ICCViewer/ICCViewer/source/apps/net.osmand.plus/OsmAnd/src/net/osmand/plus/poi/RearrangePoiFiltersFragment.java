package net.osmand.plus.poi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.CallbackWithObject;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.render.RenderingIcons;
import net.osmand.plus.views.controls.ReorderItemTouchHelperCallback;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static net.osmand.plus.poi.PoiUIFilter.CUSTOM_FILTER_ID;
import static net.osmand.plus.poi.RearrangePoiFiltersFragment.ItemType.DESCRIPTION;
import static net.osmand.plus.poi.RearrangePoiFiltersFragment.ItemType.POI;
import static net.osmand.plus.poi.RearrangePoiFiltersFragment.ItemType.SPACE;

public class RearrangePoiFiltersFragment extends DialogFragment {

	public static final String TAG = "RearrangePoiFiltersFragment";

	private static final Log LOG = PlatformUtil.getLog(RearrangePoiFiltersFragment.class);

	private boolean usedOnMap;
	private CallbackWithObject<Boolean> resultCallback;

	private List<ListItem> items = new ArrayList<>();
	private EditPoiFiltersAdapter adapter;
	private boolean orderModified;
	private boolean activationModified;
	private boolean wasReset = false;
	private boolean isChanged = false;

	private HashMap<String, Integer> poiFiltersOrders = new HashMap<>();
	private List<String> availableFiltersKeys = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean nightMode = isNightMode(requireMyApplication(), usedOnMap);
		int themeId = nightMode ? R.style.OsmandDarkTheme : R.style.OsmandLightTheme;
		setStyle(STYLE_NO_FRAME, themeId);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final OsmandApplication app = requireMyApplication();

		boolean nightMode = isNightMode(app, usedOnMap);

		View mainView = UiUtilities.getInflater(app, nightMode).inflate(R.layout.edit_arrangement_list_fragment, container, false);
		ImageButton closeButton = mainView.findViewById(R.id.close_button);
		closeButton.setImageResource(R.drawable.ic_action_remove_dark);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		TextView toolbarTitle = mainView.findViewById(R.id.toolbar_title);
		toolbarTitle.setText(R.string.rearrange_categories);

		RecyclerView recyclerView = mainView.findViewById(R.id.profiles_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(app));

		adapter = new EditPoiFiltersAdapter(app, nightMode);
		initFiltersOrders(app, false);

		final ItemTouchHelper touchHelper = new ItemTouchHelper(new ReorderItemTouchHelperCallback(adapter));
		touchHelper.attachToRecyclerView(recyclerView);
		
		orderModified = app.getSettings().POI_FILTERS_ORDER.get() != null;
		activationModified = app.getSettings().INACTIVE_POI_FILTERS.get() != null;

		adapter.setListener(new PoiAdapterListener() {

			private int fromPosition;
			private int toPosition;

			@Override
			public void onDragStarted(RecyclerView.ViewHolder holder) {
				fromPosition = holder.getAdapterPosition();
				touchHelper.startDrag(holder);
			}

			@Override
			public void onDragOrSwipeEnded(RecyclerView.ViewHolder holder) {
				toPosition = holder.getAdapterPosition();
				if (toPosition >= 0 && fromPosition >= 0 && toPosition != fromPosition) {
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onButtonClicked(int pos) {
				ListItem item = items.get(pos);
				if (item.value instanceof PoiUIFilterDataObject) {
					isChanged = true;
					activationModified = true;
					PoiUIFilterDataObject poiInfo = (PoiUIFilterDataObject) item.value;
					poiInfo.toggleActive();
					if (!poiInfo.isActive) {
						availableFiltersKeys.add(poiInfo.filterId);
					} else {
						availableFiltersKeys.remove(poiInfo.filterId);
					}
					updateItems();
				}
			}
		});
		recyclerView.setAdapter(adapter);

		View cancelButton = mainView.findViewById(R.id.dismiss_button);
		UiUtilities.setupDialogButton(nightMode, cancelButton, UiUtilities.DialogButtonType.SECONDARY, R.string.shared_string_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		mainView.findViewById(R.id.buttons_divider).setVisibility(View.VISIBLE);

		View applyButton = mainView.findViewById(R.id.right_bottom_button);
		UiUtilities.setupDialogButton(nightMode, applyButton, UiUtilities.DialogButtonType.PRIMARY, R.string.shared_string_apply);
		applyButton.setVisibility(View.VISIBLE);
		applyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isChanged) {
					if (activationModified) {
						app.getPoiFilters().saveInactiveFilters(availableFiltersKeys);
					} else if (wasReset) {
						app.getPoiFilters().saveInactiveFilters(null);
					}
					if (orderModified) {
						List<PoiUIFilter> dataToSave = new ArrayList<>();
						for (PoiUIFilter filter : getSortedPoiUiFilters(app)) {
							String filterId = filter.getFilterId();
							Integer order = poiFiltersOrders.get(filterId);
							if (order == null) {
								order = filter.getOrder();
							}
							boolean isActive = !availableFiltersKeys.contains(filterId);
							filter.setActive(isActive);
							filter.setOrder(order);
							if (isActive) {
								dataToSave.add(filter);
							}
						}
						Collections.sort(dataToSave);
						List<String> filterIds = new ArrayList<>();
						for (PoiUIFilter filter : dataToSave) {
							filterIds.add(filter.getFilterId());
						}
						app.getPoiFilters().saveFiltersOrder(filterIds);
					} else if (wasReset) {
						app.getPoiFilters().saveFiltersOrder(null);
					}
				}
				if (resultCallback != null) {
					resultCallback.processResult(isChanged);
				}
				dismiss();
			}
		});

		return mainView;
	}

	private void initFiltersOrders(OsmandApplication app, boolean arrangementByDefault) {
		poiFiltersOrders.clear();
		availableFiltersKeys.clear();
		List<PoiUIFilter> filters = getSortedPoiUiFilters(app);
		if (arrangementByDefault) {
			Collections.sort(filters, new Comparator<PoiUIFilter>() {
				@Override
				public int compare(PoiUIFilter o1, PoiUIFilter o2) {
					if (o1.filterId.equals(o2.filterId)) {
						String filterByName1 = o1.filterByName == null ? "" : o1.filterByName;
						String filterByName2 = o2.filterByName == null ? "" : o2.filterByName;
						return filterByName1.compareToIgnoreCase(filterByName2);
					} else {
						return o1.name.compareToIgnoreCase(o2.name);
					}
				}
			});
			for (int i = 0; i < filters.size(); i++) {
				PoiUIFilter filter = filters.get(i);
				poiFiltersOrders.put(filter.getFilterId(), i);
			}
		} else {
			for (int i = 0; i < filters.size(); i++) {
				PoiUIFilter filter = filters.get(i);
				poiFiltersOrders.put(filter.getFilterId(), i);
				if (!filter.isActive) {
					availableFiltersKeys.add(filter.getFilterId());
				}
			}
		}
		updateItems();
	}

	private void updateItems() {
		final OsmandApplication app = requireMyApplication();
		List<ListItem> active = getPoiFilters(true);
		List<ListItem> available = getPoiFilters(false);
		items.clear();
		items.add(new ListItem(DESCRIPTION, app.getString(R.string.create_custom_categories_list_promo)));
		items.add(new ListItem(ItemType.SPACE, app.getResources().getDimension(R.dimen.content_padding)));
		items.addAll(active);
		items.add(new ListItem(ItemType.DIVIDER, 0));
		if (availableFiltersKeys != null && availableFiltersKeys.size() > 0) {
			items.add(new ListItem(ItemType.HEADER, app.getString(R.string.shared_string_available)));
			items.addAll(available);
			items.add(new ListItem(ItemType.DIVIDER, 1));
		}
		/*items.add(new ListItem(ItemType.BUTTON, new ControlButton(app.getString(R.string.add_custom_category),
				R.drawable.ic_action_plus, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				QuickSearchCustomPoiFragment.showDialog(RearrangePoiFiltersFragment.this, app.getPoiFilters().getCustomPOIFilter().getFilterId());
			}
		})));*/
		items.add(new ListItem(ItemType.BUTTON, new ControlButton(app.getString(R.string.reset_to_default),
				R.drawable.ic_action_reset_to_default_dark, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isChanged = true;
				wasReset = true;
				activationModified = false;
				orderModified = false;
				initFiltersOrders(app, true);
			}
		})));
		items.add(new ListItem(DESCRIPTION, 
//				app.getString(R.string.add_new_custom_category_button_promo) + '\n' + 
				app.getString(R.string.reset_to_default_category_button_promo)));

		adapter.setItems(items);
	}

	public static void showInstance(@NonNull DialogFragment parentFragment, boolean usedOnMap, CallbackWithObject<Boolean> callback) {
		try {
			RearrangePoiFiltersFragment fragment = new RearrangePoiFiltersFragment();
			fragment.setUsedOnMap(usedOnMap);
			fragment.setResultCallback(callback);
			fragment.show(parentFragment.getChildFragmentManager(), RearrangePoiFiltersFragment.TAG);
		} catch (RuntimeException e) {
			LOG.error("showInstance", e);
		}
	}

	public List<ListItem> getPoiFilters(boolean isActive) {
		OsmandApplication app = requireMyApplication();
		List<ListItem> result = new ArrayList<>();
		for (PoiUIFilter f : getSortedPoiUiFilters(app)) {
			addFilterToList(result, f, isActive);
		}
		Collections.sort(result, new Comparator<ListItem>() {
			@Override
			public int compare(ListItem o1, ListItem o2) {
				int order1 = ((PoiUIFilterDataObject) o1.value).order;
				int order2 = ((PoiUIFilterDataObject) o2.value).order;
				return (order1 < order2) ? -1 : ((order1 == order2) ? 0 : 1);
			}
		});
		return result;
	}

	private void addFilterToList(List<ListItem> list, PoiUIFilter f, boolean isActive) {
		String filterId = f.getFilterId();
		if (!isActive && availableFiltersKeys.contains(filterId) || isActive && !availableFiltersKeys.contains(filterId)) {
			Integer order = poiFiltersOrders.get(filterId);
			if (order == null) {
				order = f.getOrder();
			}
			PoiUIFilterDataObject poiInfo = new PoiUIFilterDataObject();
			poiInfo.filterId = filterId;
			poiInfo.name = f.getName();
			poiInfo.order = order;
			String iconRes = f.getIconId();
			if (iconRes != null && RenderingIcons.containsBigIcon(iconRes)) {
				poiInfo.iconRes = RenderingIcons.getBigIconResourceId(iconRes);
			} else {
				poiInfo.iconRes = R.drawable.mx_user_defined;
			}
			poiInfo.isActive = !availableFiltersKeys.contains(filterId);
			list.add(new ListItem(POI, poiInfo));
		}
	}

	private static List<PoiUIFilter> getSortedPoiUiFilters(@NonNull OsmandApplication app) {
		List<PoiUIFilter> filters = app.getPoiFilters().getSortedPoiFilters(false);
		//remove custom filter
		for (int i = filters.size() - 1; i >= 0; i--) {
			PoiUIFilter filter = filters.get(i);
			if (filter.getFilterId().equals(CUSTOM_FILTER_ID)) {
				filters.remove(filter);
				break;
			}
		}
		return filters;
	}

	public void setUsedOnMap(boolean usedOnMap) {
		this.usedOnMap = usedOnMap;
	}

	public void setResultCallback(CallbackWithObject<Boolean> resultCallback) {
		this.resultCallback = resultCallback;
	}

	@NonNull
	protected OsmandApplication requireMyApplication() {
		FragmentActivity activity = requireActivity();
		return (OsmandApplication) activity.getApplication();
	}

	public static boolean isNightMode(OsmandApplication app, boolean usedOnMap) {
		if (app != null) {
			return usedOnMap ? app.getDaynightHelper().isNightModeForMapControls() : !app.getSettings().isLightContent();
		}
		return false;
	}

	public class PoiUIFilterDataObject {
		String filterId;
		String name;
		int iconRes;
		int order;
		boolean isActive;

		public void toggleActive() {
			isActive = !isActive;
		}
	}

	protected class ControlButton {
		private String title;
		private int iconRes;
		private View.OnClickListener listener;

		public ControlButton(String title, int iconRes, View.OnClickListener listener) {
			this.title = title;
			this.iconRes = iconRes;
			this.listener = listener;
		}
	}

	protected enum ItemType {
		DESCRIPTION,
		POI,
		HEADER,
		DIVIDER,
		SPACE,
		BUTTON
	}

	private class ListItem {
		ItemType type;
		Object value;

		public ListItem(ItemType type, Object value) {
			this.type = type;
			this.value = value;
		}
	}

	private class EditPoiFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
			implements ReorderItemTouchHelperCallback.OnItemMoveCallback {

		private OsmandApplication app;
		private UiUtilities uiUtilities;

		private List<ListItem> items = new ArrayList<>();
		private boolean nightMode;
		private PoiAdapterListener listener;

		public EditPoiFiltersAdapter(OsmandApplication app, boolean nightMode) {
			setHasStableIds(true);
			this.app = app;
			this.uiUtilities = app.getUIUtilities();
			this.nightMode = nightMode;
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTypeId) {
			Context ctx = parent.getContext();
			LayoutInflater inflater = UiUtilities.getInflater(ctx, nightMode);
			ItemType type = viewTypeId < ItemType.values().length ? ItemType.values()[viewTypeId] : SPACE;
			View itemView;
			RecyclerView.ViewHolder viewHolder;
			switch (type) {
				case POI:
					itemView = inflater.inflate(R.layout.order_poi_list_item, parent, false);
					viewHolder = new PoiViewHolder(itemView);
					break;
				case SPACE:
					itemView = new View(ctx);
					viewHolder = new SpaceViewHolder(itemView);
					break;
				case BUTTON:
					itemView = inflater.inflate(R.layout.preference_button, parent, false);
					viewHolder = new ButtonViewHolder(itemView);
					break;
				case HEADER:
					itemView = inflater.inflate(R.layout.preference_category_with_descr, parent, false);
					viewHolder = new HeaderViewHolder(itemView);
					break;
				case DIVIDER:
					itemView = inflater.inflate(R.layout.divider, parent, false);
					viewHolder = new DividerViewHolder(itemView);
					break;
				case DESCRIPTION:
					itemView = inflater.inflate(R.layout.bottom_sheet_item_description_long, parent, false);
					viewHolder = new DescriptionViewHolder(itemView);
					break;
				default:
					throw new IllegalArgumentException("Unsupported view type");
			}
			return viewHolder;
		}

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, int position) {
			ListItem item = items.get(position);
			boolean nightMode = isNightMode(app, usedOnMap);
			int activeColorResId = nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light;
			if (holder instanceof PoiViewHolder) {
				PoiViewHolder h = (PoiViewHolder) holder;
				PoiUIFilterDataObject poiInfo = (PoiUIFilterDataObject) item.value;
				int osmandOrangeColorResId = nightMode ? R.color.osmand_orange_dark : R.color.osmand_orange;
				h.title.setText(poiInfo.name);
				h.icon.setImageDrawable(uiUtilities.getIcon(poiInfo.iconRes, osmandOrangeColorResId));
				h.moveIcon.setVisibility(poiInfo.isActive ? View.VISIBLE : View.GONE);
				h.actionIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = holder.getAdapterPosition();
						if (listener != null && pos != RecyclerView.NO_POSITION) {
							listener.onButtonClicked(pos);
						}
					}
				});
				if (poiInfo.isActive) {
					h.actionIcon.setImageDrawable(uiUtilities.getIcon(R.drawable.ic_action_remove, R.color.color_osm_edit_delete));
					h.moveIcon.setOnTouchListener(new View.OnTouchListener() {
						@Override
						public boolean onTouch(View view, MotionEvent event) {
							if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
								listener.onDragStarted(holder);
							}
							return false;
						}
					});
				} else {
					h.actionIcon.setImageDrawable(uiUtilities.getIcon(R.drawable.ic_action_add, R.color.color_osm_edit_create));
				}
			} else if (holder instanceof SpaceViewHolder) {
				float space = (float) item.value;
				((SpaceViewHolder) holder).setSpace((int) space);
			} else if (holder instanceof ButtonViewHolder) {
				ControlButton buttonInfo = (ControlButton) item.value;
				ButtonViewHolder h = (ButtonViewHolder) holder;
				h.buttonView.setOnClickListener(buttonInfo.listener);
				h.icon.setImageDrawable(uiUtilities.getIcon(buttonInfo.iconRes, activeColorResId));
				h.title.setText(buttonInfo.title);
				Drawable drawable = UiUtilities.getColoredSelectableDrawable(app, ContextCompat.getColor(app, activeColorResId), 0.3f);
				AndroidUtils.setBackground(h.buttonView, drawable);
			} else if (holder instanceof HeaderViewHolder) {
				String header = (String) item.value;
				((HeaderViewHolder) holder).tvTitle.setText(header);
			} else if (holder instanceof DescriptionViewHolder) {
				String description = (String) item.value;
				((DescriptionViewHolder) holder).tvDescription.setText(description);
			}
		}

		public void setListener(PoiAdapterListener listener) {
			this.listener = listener;
		}

		public void setItems(List<ListItem> items) {
			this.items = items;
			notifyDataSetChanged();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public int getItemViewType(int position) {
			ListItem item = items.get(position);
			return item.type.ordinal();
		}

		@Override
		public boolean onItemMove(int from, int to) {
			Object itemFrom = items.get(from).value;
			Object itemTo = items.get(to).value;
			if (itemFrom instanceof PoiUIFilterDataObject && itemTo instanceof PoiUIFilterDataObject) {
				isChanged = true;
				orderModified = true;
				PoiUIFilterDataObject poiFrom = (PoiUIFilterDataObject) itemFrom;
				PoiUIFilterDataObject poiTo = (PoiUIFilterDataObject) itemTo;

				int orderFrom = poiFrom.order;
				int orderTo = poiTo.order;

				poiFrom.order = orderTo;
				poiTo.order = orderFrom;

				poiFiltersOrders.put(poiFrom.filterId, orderTo);
				poiFiltersOrders.put(poiTo.filterId, orderFrom);

				Collections.swap(items, from, to);
				notifyItemMoved(from, to);
				return true;
			}
			return false;
		}

		@Override
		public long getItemId(int position) {
			ListItem item = items.get(position);
			if (item.value instanceof PoiUIFilterDataObject) {
				return ((PoiUIFilterDataObject) item.value).filterId.hashCode();
			} else if (item.value instanceof ControlButton) {
				return ((ControlButton) item.value).title.hashCode();
			} else if (item.value != null) {
				return item.value.hashCode();
			}
			return item.hashCode();
		}

		@Override
		public void onItemDismiss(RecyclerView.ViewHolder holder) {
			listener.onDragOrSwipeEnded(holder);
		}

		private class DividerViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {
			View divider;

			public DividerViewHolder(View itemView) {
				super(itemView);
				divider = itemView.findViewById(R.id.divider);
			}

			@Override
			public boolean isMovingDisabled() {
				return true;
			}
		}

		private class HeaderViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {
			private TextView tvTitle;
			private TextView tvDescription;

			public HeaderViewHolder(View itemView) {
				super(itemView);
				tvTitle = itemView.findViewById(android.R.id.title);
				tvDescription = itemView.findViewById(android.R.id.summary);
				tvDescription.setVisibility(View.GONE);
			}

			@Override
			public boolean isMovingDisabled() {
				return true;
			}
		}

		private class ButtonViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {

			private View buttonView;
			private ImageView icon;
			private TextView title;

			public ButtonViewHolder(View itemView) {
				super(itemView);
				buttonView = itemView;
				icon = itemView.findViewById(android.R.id.icon);
				title = itemView.findViewById(android.R.id.title);
			}

			@Override
			public boolean isMovingDisabled() {
				return true;
			}
		}

		private class SpaceViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {

			View space;

			public SpaceViewHolder(View itemView) {
				super(itemView);
				space = itemView;
			}

			public void setSpace(int hSpace) {
				ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, hSpace);
				space.setLayoutParams(lp);
			}

			@Override
			public boolean isMovingDisabled() {
				return true;
			}
		}

		private class PoiViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {

			private TextView title;
			private TextView description;
			private ImageView icon;
			private ImageView actionIcon;
			private ImageView moveIcon;
			private View itemsContainer;

			public PoiViewHolder(View itemView) {
				super(itemView);
				title = itemView.findViewById(R.id.title);
				actionIcon = itemView.findViewById(R.id.action_icon);
				icon = itemView.findViewById(R.id.icon);
				moveIcon = itemView.findViewById(R.id.move_icon);
				itemsContainer = itemView.findViewById(R.id.selectable_list_item);
			}

			@Override
			public boolean isMovingDisabled() {
				int position = getAdapterPosition();
				if (position != RecyclerView.NO_POSITION) {
					ListItem item = items.get(position);
					if (item.value instanceof PoiUIFilterDataObject) {
						PoiUIFilterDataObject pdo = (PoiUIFilterDataObject) item.value;
						return !pdo.isActive;
					}
				}
				return false;
			}
		}

		private class DescriptionViewHolder extends RecyclerView.ViewHolder implements ReorderItemTouchHelperCallback.UnmovableItem {

			private TextView tvDescription;

			public DescriptionViewHolder(View itemView) {
				super(itemView);
				tvDescription = itemView.findViewById(R.id.description);
			}

			@Override
			public boolean isMovingDisabled() {
				return true;
			}
		}
	}

	public interface PoiAdapterListener {

		void onDragStarted(RecyclerView.ViewHolder holder);

		void onDragOrSwipeEnded(RecyclerView.ViewHolder holder);

		void onButtonClicked(int view);
	}
}
